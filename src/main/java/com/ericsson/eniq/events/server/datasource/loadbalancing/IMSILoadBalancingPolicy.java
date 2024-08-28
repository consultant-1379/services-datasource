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
 * Load balancing policy for IMSI queries
 * Main requirement is that the same IQ reader is used for the same IMSI
 * Current algorithm is running modulo on the IMSI number against the number of data sources to get an integer, and using that integer 
 * as the index to access the data source list
 * @author eemecoy
 *
 */
public class IMSILoadBalancingPolicy implements LoadBalancingPolicy {

    private final long queryNumber;

    public IMSILoadBalancingPolicy(final String imsi) {
        queryNumber = Long.parseLong(imsi);
    }

    @Override
    /**
     * see javadoc in interface
     * For the IMSILoadBalancingPolicy, the map must contain an entry for the KEY_IMSI, the value of that entry must be
     * of type long
     */
    public DataSource selectDataSource(final List<EniqDataSource> availableDataSources) {
        final int indexToUse = (int) (queryNumber % availableDataSources.size());
        return availableDataSources.get(indexToUse);
    }

}
