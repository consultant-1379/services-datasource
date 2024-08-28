/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;


/**
 * Factory to create specified LoadBalancingPolicy's
 * 
 * @author eemecoy
 *
 */
public interface LoadBalancingPolicyFactory {

    /**
     * Fetch the default Load Balancing Policy
     * @return
     */
    LoadBalancingPolicy getDefaultLoadBalancingPolicy();

    /**
     * Get the (singleton) round robin load balancing policy
     * @return
     */
    LoadBalancingPolicy getRoundRobinLoadBalancingPolicy();

    /**
     * Create an IMSI load balancing policy
     * 
     * @param imsi
     * @return
     */
    LoadBalancingPolicy getImsiLoadBalancingPolicy(String imsi);

    /**
     * Create a No load Balancing Policy
     * @return
     */
    LoadBalancingPolicy getNoLoadBalancingPolicy();

    /**
     * Get the (singleton) weighted round robin load balancing policy
     * @return
     */
    LoadBalancingPolicy getWeightedRoundRobinLoadBalancingPolicy();

}
