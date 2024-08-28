/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.RoundRobinLoadBalancingPolicy;
import com.ericsson.eniq.events.server.test.common.BaseJMockUnitTest;
import com.ericsson.eniq.events.server.utils.config.AMXPropertyReader;
import com.ericsson.eniq.events.server.utils.config.CannotReadAMXPropertyException;

/**
 * @author eemecoy
 *
 */
public class DataSourceManagerTest extends BaseJMockUnitTest {

    private static final String CONNECTION_PREFIX = "mocked connection for ";

    private static final String DEFAULT_ENIQ_DATA_SOURCE_NAME = "dwhrep/jdbc/eniqPool";

    private DataSourceManager objToTest;

    InitialContext mockedInitialContext;

    private LoadBalancingPolicy roundRobinLoadBalancingPolicy;

    public AMXPropertyReader mockedAMXPropertyReader;

    @Before
    public void setup() {
        mockedInitialContext = mockery.mock(InitialContext.class);
        roundRobinLoadBalancingPolicy = new RoundRobinLoadBalancingPolicy();
        objToTest = new StubbedDataSourceManager();
        mockedAMXPropertyReader = mockery.mock(AMXPropertyReader.class);
        objToTest.setAmxPropertyReader(mockedAMXPropertyReader);
    }

    @Test
    public void testUsesDefaultPoolSizeIfExceptionFromGettingPoolSizeAttribute() throws Exception {
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties();
        throwExceptionOnGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        objToTest.setUpDataSources();
        validateDataSourcesSetUpCorrectly(objToTest.availableDataSources, DEFAULT_ENIQ_DATA_SOURCE_NAME);
        assertThat(Integer.toString(objToTest.availableDataSources.get(0).getWeight()),
                is(DataSourceManager.DEFAULT_DATA_SOURCE_WEIGHT));
    }

    private void throwExceptionOnGetAttributesOnAMXFor(final String dataSource) throws CannotReadAMXPropertyException {
        mockery.checking(new Expectations() {
            {
                one(mockedAMXPropertyReader).getAttribute("jdbc-resource", dataSource, "PoolName");
                will(throwException(new CannotReadAMXPropertyException(null)));
            }
        });

    }

    @Test
    public void testThatTheDefaultDataSourceIsAlwaysFirstInListOfDataSources() throws Exception {
        final String extraJdbcResource1 = "jdbcResource1";
        final String extraJdbcResource2 = "jdbcResource2";
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties(extraJdbcResource1, extraJdbcResource2);
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME, extraJdbcResource1, extraJdbcResource2);
        final DataSource defaultDataSource = expectLookupInJNDIForOneDataSourceAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(extraJdbcResource1, extraJdbcResource2);
        objToTest.setUpDataSources();
        assertThat(objToTest.availableDataSources.get(0).getConnection(), is(defaultDataSource.getConnection()));
    }

    private void expectGetAttributesOnAMXFor(final String... dataSources) throws CannotReadAMXPropertyException {
        for (final String dataSource : dataSources) {
            final String poolName = "poolNameFor" + dataSource;
            mockery.checking(new Expectations() {
                {
                    one(mockedAMXPropertyReader).getAttribute("jdbc-resource", dataSource, "PoolName");
                    will(returnValue(poolName));
                    one(mockedAMXPropertyReader).getAttribute("jdbc-connection-pool", poolName, "MaxPoolSize");
                    will(returnValue("3"));
                }
            });

        }

    }

    @Test(expected = DataSourceConfigurationException.class)
    public void testSetUpDataSourcesThrowsExceptionWhenDefaultDataSourceDoesntExist() throws Exception {
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties();
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        throwExceptionOnLookupInJNDIFor(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        objToTest.setUpDataSources();
        validateDataSourcesSetUpCorrectly(objToTest.availableDataSources, DEFAULT_ENIQ_DATA_SOURCE_NAME);
    }

    private void validateDataSourcesSetUpCorrectly(final List<EniqDataSource> availableDataSources,
            final String... dataSourcesThatShouldExist) throws SQLException {
        assertThat(availableDataSources.size(), is(dataSourcesThatShouldExist.length));
        for (final String dataSource : dataSourcesThatShouldExist) {
            assertThat(checkThisListContainsThisDataSourceName(availableDataSources, dataSource), is(true));
        }
    }

    private boolean checkThisListContainsThisDataSourceName(final List<EniqDataSource> availableDataSources,
            final String dataSource) throws SQLException {
        for (final DataSource dataSourceCreated : availableDataSources) {
            final String connection = CONNECTION_PREFIX + dataSource;
            if (connection.equals(dataSourceCreated.getConnection().toString())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testSetUpDataSourcesDoesntThrowExceptionWhenExtraDataSourceDontExist() throws Exception {
        final String extraDataSource = "extraEniqPool";
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties(extraDataSource);
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME, extraDataSource);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        throwExceptionOnLookupInJNDIFor(extraDataSource);
        objToTest.setUpDataSources();
        validateDataSourcesSetUpCorrectly(objToTest.availableDataSources, DEFAULT_ENIQ_DATA_SOURCE_NAME);
    }

    private void throwExceptionOnLookupInJNDIFor(final String defaultEniqDataSource) throws NamingException {
        mockery.checking(new Expectations() {
            {
                one(mockedInitialContext).lookup(defaultEniqDataSource);
                will(throwException(new NamingException()));
            }
        });

    }

    @Test
    public void testSetUpDataSourcesNoExtraDataSourcesInPlace() throws Exception {
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties();
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        objToTest.setUpDataSources();
        validateDataSourcesSetUpCorrectly(objToTest.availableDataSources, DEFAULT_ENIQ_DATA_SOURCE_NAME);
    }

    @Test
    public void testSetUpDataSourcesOneExtraDataSource() throws Exception {
        final String extraPoolName = "eniqPoolExtra";
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties(extraPoolName);
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME, extraPoolName);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(extraPoolName);
        objToTest.setUpDataSources();
        validateDataSourcesSetUpCorrectly(objToTest.availableDataSources, DEFAULT_ENIQ_DATA_SOURCE_NAME, extraPoolName);
    }

    private void expectLookupInJDNIForDataSourcesAndAllowGetConnection(final String... poolNames)
            throws NamingException, SQLException {
        for (final String poolName : poolNames) {
            expectLookupInJNDIForOneDataSourceAndAllowGetConnection(poolName);
        }
    }

    private DataSource expectLookupInJNDIForOneDataSourceAndAllowGetConnection(final String poolName)
            throws NamingException, SQLException {
        final DataSource mockedDataSource = mockery.mock(DataSource.class, poolName);
        final Connection mockedConnection = mockery.mock(Connection.class, CONNECTION_PREFIX + poolName);
        mockery.checking(new Expectations() {
            {
                one(mockedInitialContext).lookup(poolName);
                will(returnValue(mockedDataSource));
                allowing(mockedDataSource).getConnection();
                will(returnValue(mockedConnection));
            }
        });
        return new EniqDataSourceImpl(mockedDataSource, "poolName", 0);
    }

    @Test
    public void testGetConnectionWhenOnlyDefaultConnectionExists() throws Exception {
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties();
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        final Connection connection = objToTest.getConnection(roundRobinLoadBalancingPolicy);
        assertNotNull(connection);
    }

    @Test
    public void testGetConnectionWhenTwoConnectionsExist() throws Exception {
        final String resource1 = "eniqPool1";
        final String resource2 = "eniqPool2";
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties(resource1, resource2);
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME, resource1, resource2);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(resource1, resource2);
        final Connection connection = objToTest.getConnection(roundRobinLoadBalancingPolicy);
        assertNotNull(connection);
    }

    @Test
    public void testGetConnectionWhenFiveConnectionsExist() throws Exception {
        final String resource1 = "eniqPool1";
        final String resource2 = "eniqPool2";
        final String resource3 = "eniqPool3";
        final String resource4 = "eniqPool4";
        final String resource5 = "eniqPool5";
        setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties(resource1, resource2, resource3, resource4, resource5);
        expectGetAttributesOnAMXFor(DEFAULT_ENIQ_DATA_SOURCE_NAME, resource1, resource2, resource3, resource4,
                resource5);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(DEFAULT_ENIQ_DATA_SOURCE_NAME);
        expectLookupInJDNIForDataSourcesAndAllowGetConnection(resource1, resource2, resource3, resource4, resource5);
        final Connection connection = objToTest.getConnection(roundRobinLoadBalancingPolicy);
        assertNotNull(connection);
    }

    private void setUpTheJDBCResourcePropertiesInEniqEventsJNDIProperties(final String... jdbcResources) {
        final StringBuilder listOfJDBCResourcesSB = new StringBuilder();
        for (final String resource : jdbcResources) {
            listOfJDBCResourcesSB.append(resource);
            listOfJDBCResourcesSB.append(",");
        }
        if (listOfJDBCResourcesSB.length() > 0) {
            //if there's anyhing in the string, take off the last comma
            listOfJDBCResourcesSB.deleteCharAt(listOfJDBCResourcesSB.length() - 1);
        }

        final Properties eniqEventsProperties = new Properties();
        if (jdbcResources.length > 0) {
            eniqEventsProperties.put(DataSourceManager.ENIQ_EVENTS_ADDITIONAL_DATA_SOURCES_PROPERTY_NAME,
                    listOfJDBCResourcesSB.toString());
        }
        eniqEventsProperties.put(DataSourceManager.DEFAULT_ENIQ_DATA_SOURCE_PROPERTY_NAME,
                DEFAULT_ENIQ_DATA_SOURCE_NAME);
        objToTest.setEniqEventsProperties(eniqEventsProperties);
    }

    class StubbedDataSourceManager extends DataSourceManager {

        /* (non-Javadoc)
         * @see com.ericsson.eniq.events.server.datasource.DataSourceManager#getInitialContext()
         */
        @Override
        InitialContext createInitialContext() throws NamingException {
            return mockedInitialContext;
        }

    }
}
