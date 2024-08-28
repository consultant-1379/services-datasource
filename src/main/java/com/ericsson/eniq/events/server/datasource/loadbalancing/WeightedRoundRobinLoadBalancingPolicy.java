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
 * Applies a weighted round robin load balancing policy for selecting data sources
 * Each data source is associated with a weight
 * Data sources with higher weights are chosen more often
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
public class WeightedRoundRobinLoadBalancingPolicy implements LoadBalancingPolicy {

    AtomicInteger index = new AtomicInteger(-1);

    AtomicInteger currentWeight = new AtomicInteger(0);

    /* (non-Javadoc)
     * @see com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy#selectDataSource(java.util.List)
     */
    @Override
    public DataSource selectDataSource(final List<EniqDataSource> availableDataSources) {

        final int greatestCommonDivisor = calculateGreatestCommonDivisor(availableDataSources);
        final int maximumWeight = getMaximumWeight(availableDataSources);
        final int listSize = availableDataSources.size();
        while (true) {
            index.set((index.incrementAndGet()) % listSize);
            if (index.get() == 0) {
                currentWeight.addAndGet(-greatestCommonDivisor);
                if (currentWeight.get() <= 0) {
                    currentWeight.set(maximumWeight);
                }
            }
            final EniqDataSource dataSource = availableDataSources.get(index.get());
            final int weightOfThisDataSource = dataSource.getWeight();
            if (weightOfThisDataSource >= currentWeight.get()) {
                return dataSource;
            }
        }
    }

    /**
     * Get the maximum weight in the list of data sources
     * @param availableDataSources
     * @return
     */
    int getMaximumWeight(final List<EniqDataSource> availableDataSources) {
        int maximumWeight = 0;
        for (final EniqDataSource dataSource : availableDataSources) {
            final int dataSourceWeight = dataSource.getWeight();
            if (dataSourceWeight > maximumWeight) {
                maximumWeight = dataSourceWeight;
            }
        }
        return maximumWeight;
    }

    /**
     * Calculate the greatest common divisor for a list of data sources, each with an associated weight
     * @param availableDataSources
     * @return
     */
    int calculateGreatestCommonDivisor(final List<EniqDataSource> availableDataSources) {

        int greatestCommonDivisor = 0;
        for (final EniqDataSource dataSource : availableDataSources) {
            greatestCommonDivisor = calculateGreatestCommonDivisor(greatestCommonDivisor, dataSource.getWeight());
        }
        return greatestCommonDivisor;
    }

    /**
     * recursive method to determine the greatest common divisor for two integers
     * 
     * @param a
     * @param b
     * @return
     */
    private int calculateGreatestCommonDivisor(final int a, final int b) {
        if (b == 0) {
            return a;
        }
        return calculateGreatestCommonDivisor(b, a % b);
    }
}
