/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource;

import javax.ejb.ApplicationException;
import javax.naming.NamingException;

/**
 * Indicates problem with determining the available Data Sources for accessing the ENIQ DWH database
 * @author eemecoy
 *
 */
@ApplicationException(rollback = true)
public class DataSourceConfigurationException extends Exception {

    /**
     * @param details
     */
    public DataSourceConfigurationException(final String details) {
        super(details);
    }

    /**
     * @param rootException
     */
    public DataSourceConfigurationException(final Throwable rootException) {
        super(rootException);
    }

    /**
     * @param details
     * @param rootException
     */
    public DataSourceConfigurationException(final String details, final NamingException rootException) {
        super(details, rootException);
    }

}
