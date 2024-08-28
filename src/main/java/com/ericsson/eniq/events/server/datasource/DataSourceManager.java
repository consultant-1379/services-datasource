/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource;

import static com.ericsson.eniq.events.server.logging.performance.ServicesPerformanceThreadLocalHolder.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.ericsson.eniq.events.server.common.ApplicationConfigConstants;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.logging.ServicesLogger;
import com.ericsson.eniq.events.server.utils.config.AMXPropertyReader;
import com.ericsson.eniq.events.server.utils.config.CannotReadAMXPropertyException;

/**
 * Class responsible for providing the DataSource DB connection
 * If only one connection/data source is available, then this is returned
 * If, for load balancing purposes, more than one connection is available, then a
 * connection is chosen based on the load balancing policy chosen at an earlier stage
 * 
 * Prior to the first query, this method checks if the default data source is present
 * If not, an exception is thrown
 * Also at this point, this class checks for the presence of any additional data sources
 * that may be configured by the operator
 *  
 * 
 * Not letting this class implement DataSource at the moment as that brings six or seven unnecessary methods
 * along with getConnection()
 * 
 * @author eemecoy
 *
 */
@Startup
@Singleton
/*Reason to use @TransactionManagement(TransactionManagementType.BEAN)
 * Avoid two exceptions:
 * 1.Local transaction already has 1 non-XA Resource: cannot add more resources.
 * 2.This Managed Connection is not valid as the physical connection is not usable
 */
@TransactionManagement(TransactionManagementType.BEAN)
public class DataSourceManager {

    static final String ENIQ_EVENTS_ADDITIONAL_DATA_SOURCES_PROPERTY_NAME = "ENIQ_EVENTS_ADDITIONAL_DATA_SOURCES";

    public static final String DEFAULT_ENIQ_DATA_SOURCE_PROPERTY_NAME = "ENIQ_EVENTS_DEFAULT_DATA_SOURCE";

    public static final String EXPORT_CSV_ENIQ_DATA_SOURCE_PROPERTY_NAME = "ENIQ_EVENTS_EXPORT_CSV_DATA_SOURCE";

    static final String DATA_SOURCES_LIST_DELIMITER = ",";

    static final String DEFAULT_DATA_SOURCE_WEIGHT = "3";

    @Resource(name = ApplicationConfigConstants.ENIQ_EVENT_PROPERTIES)
    private Properties eniqEventsProperties;

    List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();

    List<EniqDataSource> csvDataSource = new ArrayList<EniqDataSource>();

    @EJB
    private AMXPropertyReader amxPropertyReader;

    private boolean dataSourcesSetUp;

    /**
     * checks if the default data source is present
     * If not, an exception is thrown
     * Also checks for the presence of any additional data sources
     * that may be configured by the operators
     * Note, if these additional data sources don't exist, an exception isn't thrown, just logged
     */
    public void setUpDataSources() throws DataSourceConfigurationException {
        //important for some policies - the default data source must be first in the list       
        availableDataSources.add(getDefaultDataSource());
        availableDataSources.addAll(getAdditionalDataSources());
    }

    /**
     * Looks up the default ENIQ data source in JNDI
     * @return
     * @throws DataSourceConfigurationException - if default ENIQ data source doesn't exist
     */
    private EniqDataSource getDefaultDataSource() throws DataSourceConfigurationException {
        final String defaultEniqDataSourceName = (String) eniqEventsProperties
                .get(DEFAULT_ENIQ_DATA_SOURCE_PROPERTY_NAME);
        try {
            final Context context = createInitialContext();
            return lookUpDataSourceInJNDI(context, defaultEniqDataSourceName);
        } catch (final NamingException e) {
            throw new DataSourceConfigurationException("The ENIQ Events default data source "
                    + defaultEniqDataSourceName + " could not be found in Glassfish", e);
        }
    }

    /**
     * Looks up the CSV ENIQ data source in JNDI
     * @return
     * @throws DataSourceConfigurationException - if default ENIQ data source doesn't exist
     */
    private void configureCSVDataSource() throws DataSourceConfigurationException {
        final String csvEniqDataSourceName = (String) eniqEventsProperties
                .get(EXPORT_CSV_ENIQ_DATA_SOURCE_PROPERTY_NAME);
        try {
            final Context context = createInitialContext();
            final EniqDataSource dataSource = lookUpDataSourceInJNDI(context, csvEniqDataSourceName);
            csvDataSource.add(dataSource);
        } catch (final NamingException e) {
            throw new DataSourceConfigurationException("The ENIQ Events CSV data source " + csvEniqDataSourceName
                    + " could not be found in Glassfish", e);
        }
    }

    /**
     * Get a database connection
     * 
     *  Get a data source from the selection of data sources available
     * If there is just one data source, this is returned
     * Otherwise, the load balancing policy is used to select a data source
     * 
     * @param loadBalancingPolicy               load balancing policy to use when selecting data source 
     * @return
     * @return {@link Connection <tt>Connection</tt>}
     * @throws SQLException Database connection errors
     * @throws DataSourceConfigurationException 
     */
    public Connection getConnection(final LoadBalancingPolicy loadBalancingPolicy) throws SQLException,
            DataSourceConfigurationException {

        checkDataSourcesSetUp();

        DataSource dataSourceToUse;
        if (availableDataSources.size() == 1) {
            dataSourceToUse = availableDataSources.get(0);
        } else {
            dataSourceToUse = loadBalancingPolicy.selectDataSource(availableDataSources);
        }
        final String poolName = ((EniqDataSource) dataSourceToUse).getPoolName();
        setPoolName(poolName);
        return dataSourceToUse.getConnection();
    }

    /**
     * Get a database connection for CSV data stream
     * 
     * Get a data source from the selection of data sources available
     * If there is just one data source, this is returned
     * Otherwise, the load balancing policy is used to select a data source
     * 
     * @param loadBalancingPolicy               load balancing policy to use when selecting data source 
     * @return {@link Connection <tt>Connection</tt>}
     * @throws SQLException Database connection errors
     * @throws DataSourceConfigurationException 
     */
    public Connection getCSVConnection(final LoadBalancingPolicy loadBalancingPolicy) throws SQLException,
            DataSourceConfigurationException {

        if (csvDataSource.isEmpty()) {
            configureCSVDataSource();
        }
        DataSource dataSourceToUse;
        if (csvDataSource.size() == 1) {
            dataSourceToUse = csvDataSource.get(0);
        } else {
            dataSourceToUse = loadBalancingPolicy.selectDataSource(csvDataSource);
        }
        final String poolName = ((EniqDataSource) dataSourceToUse).getPoolName();
        setPoolName(poolName);
        return dataSourceToUse.getConnection();
    }

    /**
     * Bit of a hack - was setting up the data sources using the @PostConstruct annotation on
     * the setUpDataSources() method, which mean this method was called on deployment or glassfish restart
     * However, it seems that AMX/JMX (whith the setUpDataSources() method uses) isn't set up prior to 
     * war redeployment on a glassfish restart - see https://glassfish.dev.java.net/issues/show_bug.cgi?id=12796 
     * @throws DataSourceConfigurationException
     */
    private void checkDataSourcesSetUp() throws DataSourceConfigurationException {
        if (!dataSourcesSetUp) {
            setUpDataSources();
            dataSourcesSetUp = true;
        }
    }

    /**
     * 
     * get any additional data sources configured      
     * 
     * Note, if these additional data sources don't exist, an exception isn't thrown, just logged
     * @return 
     * @throws NamingException 
    
     */
    private List<EniqDataSource> getAdditionalDataSources() {
        final List<String> additionalDataSourceNames = getExtraDataSourcesConfigured();
        final List<EniqDataSource> additionalDataSources = new ArrayList<EniqDataSource>();
        Context context;
        try {
            context = createInitialContext();
            for (final String additionalDataSourceName : additionalDataSourceNames) {
                final EniqDataSource additionalDataSource = lookUpDataSourceInJNDI(context, additionalDataSourceName);
                ServicesLogger.detailed(getClass().getName(), "", "Additional data source found",
                        additionalDataSourceName, additionalDataSource);
                additionalDataSources.add(additionalDataSource);
            }
        } catch (final NamingException e) {
            ServicesLogger.warn(getClass().getName(), "getAdditionalDataSources",
                    "Problem accessing additional configured data source ", e);
        }
        return additionalDataSources;
    }

    /**
     * Look up a given data source name in JNDI, also retrieves the max pool size for this data source
     * 
     * @param context
     * @param availableJDBCResourceName
     * @return
     * @throws NamingException
     */
    private EniqDataSource lookUpDataSourceInJNDI(final Context context, final String availableJDBCResourceName)
            throws NamingException {

        String poolName;
        String maxPoolSize;
        try {
            poolName = amxPropertyReader.getAttribute("jdbc-resource", availableJDBCResourceName, "PoolName");
            maxPoolSize = amxPropertyReader.getAttribute("jdbc-connection-pool", poolName, "MaxPoolSize");
        } catch (final CannotReadAMXPropertyException e) {
            ServicesLogger.warn(getClass().getName(), "lookUpDataSourceInJNDI",
                    "Exception accessing properties of data source " + availableJDBCResourceName + ", will default to "
                            + DEFAULT_DATA_SOURCE_WEIGHT, e);
            maxPoolSize = DEFAULT_DATA_SOURCE_WEIGHT;
        }
        return new EniqDataSourceImpl((DataSource) context.lookup(availableJDBCResourceName),
                availableJDBCResourceName, Integer.parseInt(maxPoolSize));
    }

    /**
     * Get the extra JDBC resource names available for use by ENIQ Events Services
     * These are stored in the property ENIQ_EVENTS_JDBC_RESOURCES in the ENIQ Events
     * JDNI properties
     * 
     * @return
     */
    private List<String> getExtraDataSourcesConfigured() {
        final String resourcesAsOneString = eniqEventsProperties
                .getProperty(ENIQ_EVENTS_ADDITIONAL_DATA_SOURCES_PROPERTY_NAME);
        return parseStringAndReturnElements(resourcesAsOneString, DATA_SOURCES_LIST_DELIMITER);
    }

    /**
     * extracted out to get under unit test
     * @return
     * @throws NamingException
     */
    InitialContext createInitialContext() throws NamingException {
        return new InitialContext();
    }

    /**
     * parse the string and return elements - returns an empty list if string is null
     * @param resourcesAsOneString
     * @param listDelimiter 
     * @return
     */
    private List<String> parseStringAndReturnElements(final String resourcesAsOneString, final String listDelimiter) {
        if (resourcesAsOneString == null) {
            return new ArrayList<String>();
        }
        final StringTokenizer stringTokenizer = new StringTokenizer(resourcesAsOneString, listDelimiter);
        final List<String> jdbcResources = new ArrayList<String>();
        while (stringTokenizer.hasMoreTokens()) {
            jdbcResources.add(stringTokenizer.nextToken());
        }
        return jdbcResources;
    }

    /**
     * added to get under test
     * @param eniqEventsProperties the eniqEventsProperties to set
     */
    public void setEniqEventsProperties(final Properties eniqEventsProperties) {
        this.eniqEventsProperties = eniqEventsProperties;
    }

    /**
     * exposed for unit test
     * @param amxPropertyReader
     */
    public void setAmxPropertyReader(final AMXPropertyReader amxPropertyReader) {
        this.amxPropertyReader = amxPropertyReader;
    }

}
