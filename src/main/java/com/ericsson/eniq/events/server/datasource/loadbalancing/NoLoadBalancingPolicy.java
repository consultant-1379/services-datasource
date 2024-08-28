/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import com.ericsson.eniq.events.server.datasource.EniqDataSource;

/**
 * This load balancing policy really performs no load balancing at all - it directs all queries to the
 * the default load balancing policy
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
public class NoLoadBalancingPolicy implements LoadBalancingPolicy {

    /* (non-Javadoc)
     * @see com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy#selectDataSource(java.util.List)
     */
    @Override
    public DataSource selectDataSource(final List<EniqDataSource> availableDataSources) {
        return availableDataSources.get(0);
    }

}
