/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.eniq.events.server.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Class that wraps the SQL DataSource interface and implementation to provide an extra method (getWeight) for use in load balancing policies
 * 
 */
public class EniqDataSourceImpl implements EniqDataSource {

    private final DataSource dataSource;

    /**
     * weight that should be applied to the data source - the higher the weight, the more often the data source will be used in queries when the
     * WeightedRoundRobinLoadBalancingPolicy is in use
     */
    private final int weightFactor;

    /**
     * represents the connection pool name
     */
    private final String poolName;

    /**
     * 
     * @param dataSource
     * @param poolName
     * name of connection pool used for the current database connection
     * @param weightFactor
     * weight that should be applied to the data source - the higher the weight, the more often the data source will be used in queries when the
     * WeightedRoundRobinLoadBalancingPolicy is in use
     */
    public EniqDataSourceImpl(final DataSource dataSource, final String poolName, final int weightFactor) {
        this.dataSource = dataSource;
        this.weightFactor = weightFactor;
        this.poolName = poolName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);

    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public int getWeight() {
        return weightFactor;
    }

    @Override
    public String getPoolName() {
        return poolName;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

}
