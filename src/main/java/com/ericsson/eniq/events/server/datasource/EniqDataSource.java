/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource;

import javax.sql.DataSource;

/**
 * Wrapper on the javax.sql.DataSource interface - adds one extra method for ENIQ Events specific use
 * @author eemecoy
 *
 */
public interface EniqDataSource extends DataSource {

    /**
     * Get the weight associated with this DataSource - this is used in weighted load balancing policies
     * DataSources with higher weights may be used more frequently
     * @return
     */
    int getWeight();

    /**
     * Get the name of the connection pool to which this connection belongs
     * @return
     */
	String getPoolName();

}
