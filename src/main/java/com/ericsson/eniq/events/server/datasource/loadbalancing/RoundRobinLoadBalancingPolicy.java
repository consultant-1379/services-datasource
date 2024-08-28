/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import com.ericsson.eniq.events.server.datasource.EniqDataSource;

/**
 * Implement a round robin load balancing policy
 * This maintains an index of the last visited data source - based on this, it provides the
 * next data source to use (the next one in the list, if at the end of the list, then it goes
 * back to the top of the list)
 * 
 * @author eemecoy
 *
 */
@Singleton
@Startup
/*Reason to use @TransactionManagement(TransactionManagementType.BEAN)
 * Avoid two exceptions:
 * 1.Local transaction already has 1 non-XA Resource: cannot add more resources.
 * 2.This Managed Connection is not valid as the physical connection is not usable
 */
@TransactionManagement(TransactionManagementType.BEAN)
public class RoundRobinLoadBalancingPolicy implements LoadBalancingPolicy {

    private final AtomicInteger dataSourceIndex = new AtomicInteger(0);

    @Override
    /**
     * see javadoc in interface for more details
     * 
     */
    public DataSource selectDataSource(final List<EniqDataSource> availableDataSources) {

        final int indexToUse = dataSourceIndex.incrementAndGet() % availableDataSources.size();
        return availableDataSources.get(indexToUse);
    }

}
