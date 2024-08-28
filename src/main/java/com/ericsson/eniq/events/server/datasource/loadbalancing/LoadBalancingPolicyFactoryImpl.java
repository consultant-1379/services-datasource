/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * Implementation of LoadBalancingPolicyFactory interface - see interface's javadoc
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
public class LoadBalancingPolicyFactoryImpl implements LoadBalancingPolicyFactory {

    @EJB(beanName = "RoundRobinLoadBalancingPolicy")
    private LoadBalancingPolicy roundRobinLoadBalancingPolicy;

    @EJB(beanName = "NoLoadBalancingPolicy")
    private LoadBalancingPolicy noLoadBalancingPolicy;

    @EJB(beanName = "WeightedRoundRobinLoadBalancingPolicy")
    private LoadBalancingPolicy weightedRoundRobinLoadBalancingPolicy;

    /* (non-Javadoc)
     * @see com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory#getDefaultLoadBalancingPolicy()
     */
    @Override
    public LoadBalancingPolicy getDefaultLoadBalancingPolicy() {
        return roundRobinLoadBalancingPolicy;
    }

    /* (non-Javadoc)
     * @see com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory#getImsiLoadBalancingPolicy()
     */
    @Override
    public LoadBalancingPolicy getImsiLoadBalancingPolicy(final String imsi) {
        return new IMSILoadBalancingPolicy(imsi);
    }

    /* (non-Javadoc)
     * @see com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory#getRoundRobinLoadBalancingPolicy()
     */
    @Override
    public LoadBalancingPolicy getRoundRobinLoadBalancingPolicy() {
        return roundRobinLoadBalancingPolicy;
    }

    /* (non-Javadoc)
     * @see com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory#getNoLoadBalancingPolicy()
     */
    @Override
    public LoadBalancingPolicy getNoLoadBalancingPolicy() {
        return noLoadBalancingPolicy;
    }

    /**
     * @return the weightedRoundRobinLoadBalancingPolicy
     */
    @Override
    public LoadBalancingPolicy getWeightedRoundRobinLoadBalancingPolicy() {
        return weightedRoundRobinLoadBalancingPolicy;
    }

    /**
     * exposed to get under test
     *
     * @param roundRobinLoadBalancingPolicy the roundRobinLoadBalancingPolicy to set
     */
    public void setRoundRobinLoadBalancingPolicy(final LoadBalancingPolicy roundRobinLoadBalancingPolicy) {
        this.roundRobinLoadBalancingPolicy = roundRobinLoadBalancingPolicy;
    }

    /**
     * exposed for tests
     * @param noLoadBalancingPolicy
     */
    public void setNoLoadBalancingPolicy(final LoadBalancingPolicy noLoadBalancingPolicy) {
        this.noLoadBalancingPolicy = noLoadBalancingPolicy;
    }

    /**
     * exposed to get under test
     *
     * @param weightedRoundRobinLoadBalancingPolicy the weightedRoundRobinLoadBalancingPolicy to set
     */
    public void setWeightedRoundRobinLoadBalancingPolicy(final LoadBalancingPolicy weightedRoundRobinLoadBalancingPolicy) {
        this.weightedRoundRobinLoadBalancingPolicy = weightedRoundRobinLoadBalancingPolicy;
    }
}
