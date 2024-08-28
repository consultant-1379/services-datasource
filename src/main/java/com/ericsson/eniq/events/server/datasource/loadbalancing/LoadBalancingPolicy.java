/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import java.util.List;

import javax.sql.DataSource;

import com.ericsson.eniq.events.server.datasource.EniqDataSource;

/**
 * Interface for Load Balancing Policies for use in selecting a data source to use
 * This is to ensure that the load is distributed across multiple IQ readers if they exist
 * 
 * @author eemecoy
 *
 */
public interface LoadBalancingPolicy {

    /**
     * Based on the algorithm in the implementing class, pick a data
     * source to use from the available list provided
     * 
     * For straightforward policies (ie round robin) - the loadBalancingPolicyParameters
     * should be an empty list
     * For more complex policies (eg IMSILoadBalancingPolicy) - parameters specific to the policy
     * are included in the loadBalancingPolicyParameters map (see subclass for more details)
     * 
     * @param availableDataSources
     * @return
     */
    DataSource selectDataSource(final List<EniqDataSource> availableDataSources);

}
