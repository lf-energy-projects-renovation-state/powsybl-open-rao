/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.data.crac_impl.remedial_action.range_action;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.data.crac_impl.AlreadySynchronizedException;
import com.farao_community.farao.data.crac_api.NetworkElement;
import com.farao_community.farao.data.crac_api.RangeDefinition;
import com.farao_community.farao.data.crac_impl.range_domain.Range;
import com.farao_community.farao.data.crac_impl.range_domain.RangeType;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class PstWithRangeTest extends AbstractElementaryRangeActionTest {
    private String networkElementId;
    private PstWithRange pst;
    private Network network;
    private PhaseTapChanger phaseTapChanger;
    private NetworkElement networkElement;

    @Before
    public void setUp() {
        network = Importers.loadNetwork(
            "TestCase12Nodes.uct",
            getClass().getResourceAsStream("/TestCase12Nodes.uct")
        );
        networkElementId = "BBE2AA1  BBE3AA1  1";
        networkElement = new NetworkElement(networkElementId);
        pst = new PstWithRange("pst_range_id", networkElement);
        phaseTapChanger = network.getTwoWindingsTransformer(networkElementId).getPhaseTapChanger();
    }

    @Test
    public void pstOtherConstructor() {
        PstWithRange pstRange1 = new PstWithRange("id", networkElement);
        assertEquals("", pstRange1.getOperator());
        PstWithRange pstRange2 = new PstWithRange("id", "name", "operator", networkElement);
        assertEquals("operator", pstRange2.getOperator());
    }

    @Test
    public void pstEquals() {
        PstWithRange pstRange1 = new PstWithRange("pst_range_id", networkElement);
        assertEquals(pst.hashCode(), pstRange1.hashCode());
        assertEquals(pst, pstRange1);
        PstWithRange pstDifferent = new PstWithRange("pst_range_id_2", new NetworkElement("neOther"));
        assertNotEquals(pst.hashCode(), pstDifferent.hashCode());
        assertNotEquals(pst, pstDifferent);
    }

    @Test
    public void apply() {
        assertEquals(0, network.getTwoWindingsTransformer(networkElementId).getPhaseTapChanger().getTapPosition());
        pst.apply(network, network.getTwoWindingsTransformer(networkElementId).getPhaseTapChanger().getStep(12).getAlpha());
        assertEquals(12, network.getTwoWindingsTransformer(networkElementId).getPhaseTapChanger().getTapPosition());
    }

    @Test
    public void applyOutOfBound() {
        try {
            pst.apply(network, 50);
            fail();
        } catch (FaraoException e) {
            assertEquals("Angle value", e.getMessage().substring(0, 11)); // In order to avoid numeric values in the expected String
        }
    }

    @Test
    public void applyOnUnknownPst() {
        PstWithRange unknownPstWithRange = new PstWithRange(
                "unknown_pstrange_id",
                new NetworkElement("unknown pst", "unknown pst"));
        try {
            unknownPstWithRange.apply(network, 50);
            fail();
        } catch (FaraoException e) {
            assertEquals("PST unknown pst does not exist in the current network", e.getMessage());
        }
    }

    @Test
    public void applyOnTransformerWithNoPhaseShifter() {
        Network network = Importers.loadNetwork(
            "TestCase12Nodes_no_pst.uct",
            getClass().getResourceAsStream("/TestCase12Nodes_no_pst.uct"));
        String notPstRangeElementId = "BBE2AA1  BBE3AA1  1";
        PstWithRange notAPstWithRange = new PstWithRange("not_pstrange_id", networkElement);
        try {
            notAPstWithRange.apply(network, 50);
            fail();
        } catch (FaraoException e) {
            assertEquals(String.format("Transformer %s is not a PST but is defined as a PstRange", notPstRangeElementId), e.getMessage());
        }
    }

    @Test
    public void pstWithoutSpecificRange() {
        PstWithRange pstRangeWithoutSpecificRange = new PstWithRange("id", networkElement);
        pstRangeWithoutSpecificRange.synchronize(network);
        assertEquals(phaseTapChanger.getStep(phaseTapChanger.getLowTapPosition()).getAlpha(), pstRangeWithoutSpecificRange.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(phaseTapChanger.getHighTapPosition()).getAlpha(), pstRangeWithoutSpecificRange.getMaxValue(network), 0);
    }

    @Test
    public void pstWithAbsoluteStartOneRange() {
        pst.addRange(new Range(3, 13, RangeType.ABSOLUTE_FIXED, RangeDefinition.STARTS_AT_ONE));
        pst.synchronize(network);
        assertEquals(phaseTapChanger.getStep(phaseTapChanger.getLowTapPosition() + 2).getAlpha(), pst.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(phaseTapChanger.getLowTapPosition() + 12).getAlpha(), pst.getMaxValue(network), 0);
    }

    @Test
    public void pstWithAbsoluteCenteredZeroRange() {
        pst.addRange(new Range(-3, 3, RangeType.ABSOLUTE_FIXED, RangeDefinition.CENTERED_ON_ZERO));
        pst.synchronize(network);
        int neutralTap = (phaseTapChanger.getHighTapPosition() + phaseTapChanger.getLowTapPosition()) / 2;
        assertEquals(phaseTapChanger.getStep(neutralTap - 3).getAlpha(), pst.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(neutralTap + 3).getAlpha(), pst.getMaxValue(network), 0);
    }

    @Test
    public void pstWithRelativeDynamicRange() {
        pst.addRange(new Range(-3, 3, RangeType.RELATIVE_DYNAMIC, RangeDefinition.CENTERED_ON_ZERO));
        pst.synchronize(network);
        int initialTapPosition = phaseTapChanger.getTapPosition();
        assertEquals(phaseTapChanger.getStep(initialTapPosition - 3).getAlpha(), pst.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(initialTapPosition + 3).getAlpha(), pst.getMaxValue(network), 0);

        int newTapPosition = initialTapPosition + 5;
        phaseTapChanger.setTapPosition(newTapPosition);
        assertEquals(phaseTapChanger.getStep(newTapPosition - 3).getAlpha(), pst.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(newTapPosition + 3).getAlpha(), pst.getMaxValue(network), 0);
    }

    @Test
    public void pstWithRelativeFixedRange() {
        pst.addRange(new Range(-3, 3, RangeType.RELATIVE_FIXED, RangeDefinition.CENTERED_ON_ZERO));
        pst.synchronize(network);
        int initialTapPosition = phaseTapChanger.getTapPosition();
        assertEquals(phaseTapChanger.getStep(initialTapPosition - 3).getAlpha(), pst.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(initialTapPosition + 3).getAlpha(), pst.getMaxValue(network), 0);

        int newTapPosition = initialTapPosition + 5;
        phaseTapChanger.setTapPosition(newTapPosition);
        assertEquals(phaseTapChanger.getStep(initialTapPosition - 3).getAlpha(), pst.getMinValue(network), 0);
        assertEquals(phaseTapChanger.getStep(initialTapPosition + 3).getAlpha(), pst.getMaxValue(network), 0);
    }

    @Test
    public void desynchronize() {
        pst.addRange(new Range(-3, 3, RangeType.RELATIVE_FIXED, RangeDefinition.CENTERED_ON_ZERO));
        pst.synchronize(network);
        int initialTapPosition = phaseTapChanger.getTapPosition();
        assertEquals(phaseTapChanger.getStep(initialTapPosition + 3).getAlpha(), pst.getMaxValue(network), 0);
        pst.desynchronize();

        try {
            pst.getMaxValue(network);
            fail();
        } catch (FaraoException e) {
            assertEquals("PST pst_range_id have not been synchronized so its max value cannot be accessed", e.getMessage());
        }
    }

    @Test
    public void getMinValueWithNoSynchronizationFails() {
        try {
            pst.getMinValue(network);
            fail();
        } catch (FaraoException e) {
            assertEquals("PST pst_range_id have not been synchronized so its min value cannot be accessed", e.getMessage());
        }
    }

    @Test
    public void getMaxNegativeVariationWithNoSynchronizationFails() {
        try {
            pst.getMaxNegativeVariation(network);
            fail();
        } catch (FaraoException e) {
            assertEquals("PST pst_range_id have not been synchronized so its min value cannot be accessed", e.getMessage());
        }
    }

    @Test
    public void getMaxPositiveVariationWithNoSynchronizationFails() {
        try {
            pst.getMaxPositiveVariation(network);
            fail();
        } catch (FaraoException e) {
            assertEquals("PST pst_range_id have not been synchronized so its max value cannot be accessed", e.getMessage());
        }
    }

    @Test
    public void synchronizetwiceFails() {
        pst.synchronize(network);
        try {
            pst.synchronize(network);
        } catch (AlreadySynchronizedException e) {
            assertEquals("PST pst_range_id has already been synchronized", e.getMessage());
        }
    }
}