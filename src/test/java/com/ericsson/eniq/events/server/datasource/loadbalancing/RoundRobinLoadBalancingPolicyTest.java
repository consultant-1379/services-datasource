/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.events.server.datasource.EniqDataSource;
import com.ericsson.eniq.events.server.datasource.EniqDataSourceImpl;
import com.ericsson.eniq.events.server.test.common.BaseJMockUnitTest;

/**
 * @author eemecoy
 *
 */
public class RoundRobinLoadBalancingPolicyTest extends BaseJMockUnitTest {

    private LoadBalancingPolicy objToTest;

    @Before
    public void setup() {
        objToTest = new RoundRobinLoadBalancingPolicy();
    }

    @Test
    public void testSelectDataSourceToUseOneDataSourceAvailable() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final EniqDataSource mockedDataSource = createMockedDataSource("mockedDataSource1");
        availableDataSources.add(mockedDataSource);

        final DataSource firstDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(firstDataSourceSuggested, is((DataSource) mockedDataSource));
        final DataSource secondDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(secondDataSourceSuggested, is((DataSource) mockedDataSource));
        final DataSource thirdDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(thirdDataSourceSuggested, is((DataSource) mockedDataSource));
    }

    @Test
    public void testSelectDataSourceToUseTwoDataSourcesAvailable() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final EniqDataSource mockedDataSource1 = createMockedDataSource("mockedDataSource1");
        availableDataSources.add(mockedDataSource1);
        final EniqDataSource mockedDataSource2 = createMockedDataSource("mockedDataSource2");
        availableDataSources.add(mockedDataSource2);

        final DataSource firstDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(firstDataSourceSuggested, is((DataSource) mockedDataSource2));
        final DataSource secondDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(secondDataSourceSuggested, is((DataSource) mockedDataSource1));
        final DataSource thirdDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(thirdDataSourceSuggested, is((DataSource) mockedDataSource2));
    }

    @Test
    public void testSelectDataSourceToUseThreeDataSourcesAvailable() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final EniqDataSource mockedDataSource1 = createMockedDataSource("mockedDataSource1");
        availableDataSources.add(mockedDataSource1);
        final EniqDataSource mockedDataSource2 = createMockedDataSource("mockedDataSource2");
        availableDataSources.add(mockedDataSource2);
        final EniqDataSource mockedDataSource3 = createMockedDataSource("mockedDataSource3");
        availableDataSources.add(mockedDataSource3);

        final DataSource firstDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(firstDataSourceSuggested, is((DataSource) mockedDataSource2));
        final DataSource secondDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(secondDataSourceSuggested, is((DataSource) mockedDataSource3));
        final DataSource thirdDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(thirdDataSourceSuggested, is((DataSource) mockedDataSource1));
        final DataSource fourthDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(fourthDataSourceSuggested, is((DataSource) mockedDataSource2));
        final DataSource fifthDataSourceSuggested = objToTest.selectDataSource(availableDataSources);
        assertThat(fifthDataSourceSuggested, is((DataSource) mockedDataSource3));
    }

    private EniqDataSource createMockedDataSource(final String mockName) {
        return new EniqDataSourceImpl(mockery.mock(DataSource.class, mockName),"poolName",0);
    }
}
