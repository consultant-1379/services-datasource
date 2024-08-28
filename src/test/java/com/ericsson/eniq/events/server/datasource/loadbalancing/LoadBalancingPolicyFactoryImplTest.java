/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author eemecoy
 *
 */
public class LoadBalancingPolicyFactoryImplTest {

    private LoadBalancingPolicyFactoryImpl objToTest;

    private RoundRobinLoadBalancingPolicy roundRobinLoadBalancingPolicy;

    private NoLoadBalancingPolicy noLoadBalancingPolicy;

    @Before
    public void setup() {
        objToTest = new LoadBalancingPolicyFactoryImpl();
        roundRobinLoadBalancingPolicy = new RoundRobinLoadBalancingPolicy();
        objToTest.setRoundRobinLoadBalancingPolicy(roundRobinLoadBalancingPolicy);
        noLoadBalancingPolicy = new NoLoadBalancingPolicy();
        objToTest.setNoLoadBalancingPolicy(noLoadBalancingPolicy);
    }

    @Test
    public void testGetDefaultPolicy() {
        final LoadBalancingPolicy result = objToTest.getDefaultLoadBalancingPolicy();
        assertThat(result, is((LoadBalancingPolicy) roundRobinLoadBalancingPolicy));
    }

    @Test
    public void testGetImsiLoadBalancingPolicy() {
        assertNotNull(objToTest.getImsiLoadBalancingPolicy("1234"));
    }

    @Test
    public void testGetNoLoadBalancingPolicy() {
        assertThat(objToTest.getNoLoadBalancingPolicy(), is((LoadBalancingPolicy) noLoadBalancingPolicy));
    }

}
