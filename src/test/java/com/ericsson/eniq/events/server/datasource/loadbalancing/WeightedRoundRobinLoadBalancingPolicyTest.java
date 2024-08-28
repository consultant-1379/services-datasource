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
public class WeightedRoundRobinLoadBalancingPolicyTest extends BaseJMockUnitTest {

    private WeightedRoundRobinLoadBalancingPolicy objToTest;

    @Before
    public void setup() {
        objToTest = new WeightedRoundRobinLoadBalancingPolicy();
    }

    @Test
    public void testGetDataSourcesReturnsAccordingToWeight() {
        final List<EniqDataSource> availableDataSources = new ArrayList<EniqDataSource>();

        final EniqDataSource mockedDataSourceWeight4 = new EniqDataSourceImpl(
                mockery.mock(DataSource.class, "weight4"),"poolName4",4);
        availableDataSources.add(mockedDataSourceWeight4);

        final EniqDataSource mockedDataSourceWeight3 = new EniqDataSourceImpl(
                mockery.mock(DataSource.class, "weight3"),"poolName3", 3);
        availableDataSources.add(mockedDataSourceWeight3);

        final EniqDataSource mockedDataSourceWeight2 = new EniqDataSourceImpl(
                mockery.mock(DataSource.class, "weight2"),"poolName2", 2);
        availableDataSources.add(mockedDataSourceWeight2);

        final List<DataSource> suggestedDataSources = new ArrayList<DataSource>();
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));
        suggestedDataSources.add(objToTest.selectDataSource(availableDataSources));

        final List<DataSource> expectedResult = new ArrayList<DataSource>();
        expectedResult.add(mockedDataSourceWeight4);
        expectedResult.add(mockedDataSourceWeight4);
        expectedResult.add(mockedDataSourceWeight3);
        expectedResult.add(mockedDataSourceWeight4);
        expectedResult.add(mockedDataSourceWeight3);
        expectedResult.add(mockedDataSourceWeight2);
        expectedResult.add(mockedDataSourceWeight4);

        assertThat(suggestedDataSources, is(expectedResult));

    }

    @Test
    public void testcalculateGreatestCommonDivisorForAList() {
        final List<EniqDataSource> dataSources = new ArrayList<EniqDataSource>();
        dataSources.add(new EniqDataSourceImpl(null,"poolName1",24));
        dataSources.add(new EniqDataSourceImpl(null,"poolName2",12));
        dataSources.add(new EniqDataSourceImpl(null,"poolName3",36));
        dataSources.add(new EniqDataSourceImpl(null,"poolName4",52));
        assertThat(objToTest.calculateGreatestCommonDivisor(dataSources), is(4));
    }

    @Test
    public void testCalculateMaxWeight() {
        final List<EniqDataSource> dataSources = new ArrayList<EniqDataSource>();
        dataSources.add(new EniqDataSourceImpl(null,"poolName1",24));
        dataSources.add(new EniqDataSourceImpl(null,"poolName2",12));
        dataSources.add(new EniqDataSourceImpl(null,"poolName3",36));
        dataSources.add(new EniqDataSourceImpl(null,"poolName4",52));
        assertThat(objToTest.getMaximumWeight(dataSources), is(52));
    }
}
