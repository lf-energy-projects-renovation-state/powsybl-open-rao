/*
 *
 *  * Copyright (c) 2020, RTE (http://www.rte-france.com)
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.data.crac_impl.remedial_action.range_action;

import com.farao_community.farao.data.crac_api.NetworkElement;
import com.farao_community.farao.data.crac_api.UsageRule;
import com.farao_community.farao.data.crac_impl.range_domain.Range;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Alexandre Montigny {@literal <alexandre.montigny at rte-france.com>}
 */
public class HvdcRangeTest extends AbstractElementaryRangeActionTest {

    private HvdcRange hvdcRange;

    @Before
    public void setUp() throws Exception {
        String hvdcRangeId = "id";
        ArrayList<UsageRule> usageRules = createUsageRules();
        List<Range> ranges = createRanges();
        NetworkElement mockedNetworkElement = Mockito.mock(NetworkElement.class);
        hvdcRange = new HvdcRange(
                hvdcRangeId,
                hvdcRangeId,
                hvdcRangeId,
                usageRules,
                ranges,
                mockedNetworkElement
        );
    }

    @Test
    public void getMinAndMaxValueWithSingleRange() {
        Network mockedNetwork = Mockito.mock(Network.class);
        Range anyRange = Mockito.mock(Range.class);
        assertEquals(HvdcRange.hvdcRangeTempValue, hvdcRange.getMaxValueWithRange(mockedNetwork, anyRange), 0);
        assertEquals(HvdcRange.hvdcRangeTempValue, hvdcRange.getMinValueWithRange(mockedNetwork, anyRange), 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void apply() {
        Network mockedNetwork = Mockito.mock(Network.class);
        double anySetpoint = 123.4;
        hvdcRange.apply(mockedNetwork, anySetpoint);
    }

    @Test
    public void addRange() {
        Range range1 = Mockito.mock(Range.class);
        hvdcRange.addRange(range1);
        assertEquals(2, hvdcRange.ranges.size(), 0);
    }

    @Test
    public void getMaxNegaPosVariation() {
        Network mockedNetwork = Mockito.mock(Network.class);
        HvdcLine mockedHvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(mockedNetwork.getHvdcLine(mockedHvdcLine.getId())).thenReturn(mockedHvdcLine);
        Mockito.when(mockedHvdcLine.getActivePowerSetpoint()).thenReturn(10.);

        assertEquals(10,  hvdcRange.getMaxNegativeVariation(mockedNetwork), 0);
        assertEquals(10,  hvdcRange.getMaxPositiveVariation(mockedNetwork), 0);
    }
}