/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;

/**
 * Database connection class for different data source
 * 
 * @author ehaoswa
 * @author edeccox
 * 
 * @since Feb 2010
 */
@Singleton
@Startup
/*Reason to use @TransactionManagement(TransactionManagementType.BEAN)
 * Avoid two exceptions:
 * 1.Local transaction already has 1 non-XA Resource: cannot add more resources.
 * 2.This Managed Connection is not valid as the physical connection is not usable
 */
@TransactionManagement(TransactionManagementType.BEAN)
public class DBConnectionManager {

    /**
    * Connection pool to dwhrep
    */
    public static final String DWHREP_DATA_SOURCE_NAME = "dwhrep/jdbc/eniqPool";

    private DataSource dwhrepDataSource;

    @EJB
    private DataSourceManager dataSourceManager;

    /**
     * @param dataSourceManager the dataSourceManager to set
     */
    public void setDataSourceManager(final DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    /**
    * exposed to get query classes under test
    * @param dwhrepDataSource the dataSource to set
    */
    //EHAOSWA 15/08/2011 put resource annotaion at access method level
    @Resource(name = DWHREP_DATA_SOURCE_NAME)
    public void setDwhrepDataSource(final DataSource dwhrepDataSource) {
        this.dwhrepDataSource = dwhrepDataSource;
    }

    /**
     * Get the database connection
     * @param loadBalancingPolicy load balancing policy to use when determining data source connection
     * @return {@link Connection <tt>Connection</tt>}
     * @throws SQLException Database connection errors
     * @throws DataSourceConfigurationException 
     */
    public Connection getConnection(final LoadBalancingPolicy loadBalancingPolicy) throws SQLException,
            DataSourceConfigurationException {
        return dataSourceManager.getConnection(loadBalancingPolicy);
    }

    /**
     * Get the database connection for CSV stream
     * @param loadBalancingPolicy load balancing policy to use when determining data source connection
     * @return {@link Connection <tt>Connection</tt>}
     * @throws SQLException Database connection errors
     * @throws DataSourceConfigurationException 
     */
    public Connection getCSVConnection(final LoadBalancingPolicy loadBalancingPolicy) throws SQLException,
            DataSourceConfigurationException {
        return dataSourceManager.getCSVConnection(loadBalancingPolicy);
    }

    /**
     * Get the dwhrep database connection
     * @return {@link Connection <tt>Connection</tt>}
     * @throws SQLException Database connection errors
     */
    public Connection getDwhrepConnection() throws SQLException {
        return dwhrepDataSource.getConnection();
    }

    /**
     * added for test classes
     * 
     * @return the dwhrep data source
     */
    public DataSource getDwhrepDataSource() {
        return dwhrepDataSource;
    }
}
