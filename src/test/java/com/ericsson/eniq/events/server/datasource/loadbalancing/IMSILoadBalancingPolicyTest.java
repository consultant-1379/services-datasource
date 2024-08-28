/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.datasource.loadbalancing;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;

import com.ericsson.eniq.events.server.datasource.EniqDataSource;
import com.ericsson.eniq.events.server.datasource.EniqDataSourceImpl;
import com.ericsson.eniq.events.server.test.common.BaseJMockUnitTest;

/**
 * @author eemecoy
 *
 */
public class IMSILoadBalancingPolicyTest extends BaseJMockUnitTest {

    @Test
    public void testSelectDataSourceIsDifferentForIMSIsWithDifferentModulo() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final EniqDataSource mockedDataSource1 = mockery.mock(EniqDataSource.class, "mock1");
        availableDataSources.add(mockedDataSource1);
        final EniqDataSource mockedDataSource2 = mockery.mock(EniqDataSource.class, "mock2");
        availableDataSources.add(mockedDataSource2);
        final LoadBalancingPolicy imsiPolicy1 = new IMSILoadBalancingPolicy("1234");
        final DataSource suggestedDataSourceForIMSI1 = imsiPolicy1.selectDataSource(availableDataSources);

        final LoadBalancingPolicy imsiPolicy2 = new IMSILoadBalancingPolicy("1235");
        final DataSource suggestedDataSourceForIMSI2 = imsiPolicy2.selectDataSource(availableDataSources);
        assertThat(suggestedDataSourceForIMSI1, not(suggestedDataSourceForIMSI2));

    }

    @Test
    public void testSelectDataSourceIsTheSameForTheSameIMSI() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final EniqDataSource mockedDataSource1 = mockery.mock(EniqDataSource.class, "mock1");
        availableDataSources.add(mockedDataSource1);
        final EniqDataSource mockedDataSource2 = mockery.mock(EniqDataSource.class, "mock2");
        availableDataSources.add(mockedDataSource2);
        final EniqDataSource mockedDataSource3 = mockery.mock(EniqDataSource.class, "mock3");
        availableDataSources.add(mockedDataSource3);
        final String imsi = "208070019558076";
        final LoadBalancingPolicy imsiPolicy1 = new IMSILoadBalancingPolicy(imsi);
        final DataSource firstSuggestedDataSource = imsiPolicy1.selectDataSource(availableDataSources);
        final LoadBalancingPolicy imsiPolicy2 = new IMSILoadBalancingPolicy(imsi);
        final DataSource secondSuggestedDataSource = imsiPolicy2.selectDataSource(availableDataSources);
        assertThat(firstSuggestedDataSource, is(secondSuggestedDataSource));
        final LoadBalancingPolicy imsiPolicy3 = new IMSILoadBalancingPolicy(imsi);
        final DataSource thirdSuggestedDataSource = imsiPolicy3.selectDataSource(availableDataSources);
        assertThat(firstSuggestedDataSource, is(thirdSuggestedDataSource));
    }

    @Test
    public void testSelectDataSourceIsAlwaysWhenJustTheOneDataSource() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final DataSource mockedDataSource = mockery.mock(DataSource.class);
        final EniqDataSource eniqDataSource = new EniqDataSourceImpl(mockedDataSource, "poolName", 0);
        availableDataSources.add(eniqDataSource);
        final String imsi = "208070019558076";

        final LoadBalancingPolicy imsiPolicy1 = new IMSILoadBalancingPolicy(imsi);
        final DataSource firstSuggestedDataSource = imsiPolicy1.selectDataSource(availableDataSources);
        assertThat(firstSuggestedDataSource, is((DataSource) eniqDataSource));

        final LoadBalancingPolicy imsiPolicy2 = new IMSILoadBalancingPolicy(imsi);
        final DataSource secondSuggestedDataSource = imsiPolicy2.selectDataSource(availableDataSources);
        assertThat(secondSuggestedDataSource, is((DataSource) eniqDataSource));

        final LoadBalancingPolicy imsiPolicy3 = new IMSILoadBalancingPolicy(imsi);
        final DataSource thirdSuggestedDataSource = imsiPolicy3.selectDataSource(availableDataSources);
        assertThat(thirdSuggestedDataSource, is((DataSource) eniqDataSource));
    }

    @Test
    public void testSelectDataSourceWithLongIMSI() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();
        final DataSource mockedDataSource = mockery.mock(DataSource.class);
        final EniqDataSource eniqDataSource = new EniqDataSourceImpl(mockedDataSource, "poolName", 0);
        availableDataSources.add(eniqDataSource);
        final String imsi = "999999999999999999"; //public static final long   MAX_VALUE       9223372036854775807L

        final LoadBalancingPolicy imsiPolicy1 = new IMSILoadBalancingPolicy(imsi);
        final DataSource firstSuggestedDataSource = imsiPolicy1.selectDataSource(availableDataSources);
        assertThat(firstSuggestedDataSource, is((DataSource) eniqDataSource));

        final LoadBalancingPolicy imsiPolicy2 = new IMSILoadBalancingPolicy(imsi);
        final DataSource secondSuggestedDataSource = imsiPolicy2.selectDataSource(availableDataSources);
        assertThat(secondSuggestedDataSource, is((DataSource) eniqDataSource));

        final LoadBalancingPolicy imsiPolicy3 = new IMSILoadBalancingPolicy(imsi);
        final DataSource thirdSuggestedDataSource = imsiPolicy3.selectDataSource(availableDataSources);
        assertThat(thirdSuggestedDataSource, is((DataSource) eniqDataSource));
    }

}
