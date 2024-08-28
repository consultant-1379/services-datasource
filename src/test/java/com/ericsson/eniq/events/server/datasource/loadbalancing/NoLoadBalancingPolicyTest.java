/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.events.server.datasource.EniqDataSource;
import com.ericsson.eniq.events.server.datasource.EniqDataSourceImpl;
import com.ericsson.eniq.events.server.test.common.BaseJMockUnitTest;

/**
 * @author eemecoy
 *
 */
public class NoLoadBalancingPolicyTest extends BaseJMockUnitTest {

    private NoLoadBalancingPolicy objToTest;

    @Before
    public void setup() {
        objToTest = new NoLoadBalancingPolicy();
    }

    @Test
    public void testNoLoadBalancingPolicyAlwaysDirectsQueriesToDefaultSource() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final EniqDataSource defaultDataSource = new EniqDataSourceImpl(mockery.mock(DataSource.class,
                "default data source"),"poolName1",0);
        availableDataSources.add(defaultDataSource);
        final EniqDataSource additionalDataSource1 = new EniqDataSourceImpl(mockery.mock(DataSource.class,
                "additional data source 1"),"poolName2",0);
        availableDataSources.add(additionalDataSource1);
        final EniqDataSource additionalDataSource2 = new EniqDataSourceImpl(mockery.mock(DataSource.class,
                "additional data source 2"),"poolName3",0);
        availableDataSources.add(additionalDataSource2);
        for (int i = 0; i < 3; i++) {
            final DataSource result = objToTest.selectDataSource(availableDataSources);
            assertThat(result, is((DataSource) defaultDataSource));
        }
    }
}
