/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.monitoring.angle_monitoring;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.*;
import com.farao_community.farao.data.crac_api.cnec.AngleCnec;
import com.farao_community.farao.data.crac_api.network_action.ActionType;
import com.farao_community.farao.data.crac_api.network_action.NetworkAction;
import com.farao_community.farao.data.crac_creation.creator.api.parameters.CracCreationParameters;
import com.farao_community.farao.data.crac_creation.creator.cim.CimCrac;
import com.farao_community.farao.data.crac_creation.creator.cim.crac_creator.CimCracCreationContext;
import com.farao_community.farao.data.crac_creation.creator.cim.crac_creator.CimCracCreator;
import com.farao_community.farao.data.crac_creation.creator.cim.importer.CimCracImporter;
import com.farao_community.farao.data.rao_result_api.RaoResult;
import com.google.common.base.Suppliers;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.glsk.cim.CimGlskDocument;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Angle monitoring with Crac Factory test class
 *
 * @author Godelaine de Montmorillon {@literal <godelaine.demontmorillon at rte-france.com>}
 */
public class AngleMonitoringTest {
    private static final double ANGLE_TOLERANCE = 0.5;
    private int numberOfLoadFlowsInParallel = 2;
    private OffsetDateTime glskOffsetDateTime;
    private Network network;
    private Crac crac;
    private RaoResult raoResult;
    private LoadFlowParameters loadFlowParameters;
    private CimGlskDocument cimGlskDocument;
    private AngleMonitoringResult angleMonitoringResult;
    // Crac Factory
    private AngleCnec acPrev;
    private AngleCnec acCur1;
    private NetworkAction naL1Cur;

    @Before
    public void generalSetUp() {
        loadFlowParameters = new LoadFlowParameters();
        loadFlowParameters.setDc(false);

        raoResult = Mockito.mock(RaoResult.class);
        when(raoResult.getActivatedNetworkActionsDuringState(any())).thenReturn(Collections.emptySet());
        when(raoResult.getActivatedRangeActionsDuringState(any())).thenReturn(Collections.emptySet());
    }

    public void setUpCimCrac(String fileName, OffsetDateTime parametrableOffsetDateTime, CracCreationParameters cracCreationParameters) {
        Properties importParams = new Properties();
        importParams.put("iidm.import.cgmes.source-for-iidm-id", "rdfID");
        network = Importers.loadNetwork(Paths.get(new File(AngleMonitoringTest.class.getResource("/MicroGrid.zip").getFile()).toString()), LocalComputationManager.getDefault(), Suppliers.memoize(ImportConfig::load).get(), importParams);
        InputStream is = getClass().getResourceAsStream(fileName);
        CimCracImporter cracImporter = new CimCracImporter();
        CimCrac cimCrac = cracImporter.importNativeCrac(is);
        CimCracCreator cimCracCreator = new CimCracCreator();
        CimCracCreationContext cracCreationContext = cimCracCreator.createCrac(cimCrac, network, parametrableOffsetDateTime, cracCreationParameters);
        crac = cracCreationContext.getCrac();
        cimGlskDocument = CimGlskDocument.importGlsk(getClass().getResourceAsStream("/GlskB45MicroGridTest.xml"));
        glskOffsetDateTime = OffsetDateTime.parse("2021-04-02T05:30Z");
    }

    public void setUpCracFactory(String networkFileName) {
        network = Importers.loadNetwork(networkFileName, getClass().getResourceAsStream("/" + networkFileName));
        crac = CracFactory.findDefault().create("test-crac");
        cimGlskDocument = CimGlskDocument.importGlsk(getClass().getResourceAsStream("/GlskB45test.xml"));
        glskOffsetDateTime = OffsetDateTime.parse("2017-04-12T02:30Z");
    }

    public void mockPreventiveState() {
        acPrev = addAngleCnec("acPrev", Instant.PREVENTIVE, null, "VL1", "VL2", -200., 500.);
        crac.newNetworkAction()
                .withId("Open L1 - 1")
                .newTopologicalAction().withNetworkElement("L1").withActionType(ActionType.OPEN).add()
                .newOnAngleConstraintUsageRule().withInstant(Instant.PREVENTIVE).withAngleCnec(acPrev.getId()).add()
                .add();
    }

    public void mockCurativeStates() {
        crac.newContingency().withId("coL1").withNetworkElement("L1").add();
        crac.newContingency().withId("coL2").withNetworkElement("L2").add();
        crac.newContingency().withId("coL1L2").withNetworkElement("L1").withNetworkElement("L2").add();
        acCur1 = addAngleCnec("acCur1", Instant.CURATIVE, "coL1", "VL1", "VL2", -300., null);
    }

    private AngleCnec addAngleCnec(String id, Instant instant, String contingency, String importingNetworkElement, String exportingNetworkElement, Double min, Double max) {
        return crac.newAngleCnec()
                .withId(id)
                .withInstant(instant)
                .withContingency(contingency)
                .withImportingNetworkElement(importingNetworkElement)
                .withExportingNetworkElement(exportingNetworkElement)
                .withMonitored()
                .newThreshold().withUnit(Unit.DEGREE).withMin(min).withMax(max).add()
                .add();
    }

    private void runAngleMonitoring() {
        angleMonitoringResult = new AngleMonitoring(crac, network, raoResult, cimGlskDocument).run("OpenLoadFlow", loadFlowParameters, numberOfLoadFlowsInParallel, glskOffsetDateTime);
    }

    @Test
    public void testUnknownAngleMonitoring() {
        // LoadFlow diverges
        setUpCracFactory("networkKO.xiidm");
        mockCurativeStates();
        runAngleMonitoring();
        assertTrue(angleMonitoringResult.isUnknown());
        angleMonitoringResult.getAppliedCras().forEach((state, networkActions) -> assertTrue(networkActions.isEmpty()));
        assertTrue(angleMonitoringResult.getAngleCnecsWithAngle().stream().noneMatch(angleResult -> !angleResult.getAngle().isNaN()));
        assertEquals(angleMonitoringResult.printConstraints(), List.of("Unknown status on AngleCnecs."));
    }

    @Test
    public void testNoAngleCnecsDefined() {
        setUpCracFactory("network.xiidm");
        runAngleMonitoring();
        assertTrue(angleMonitoringResult.isSecure());
    }

    @Test
    public void testPreventiveStateOnly() {
        setUpCracFactory("network.xiidm");
        mockPreventiveState();
        runAngleMonitoring();
        assertTrue(angleMonitoringResult.isUnsecure());
        angleMonitoringResult.getAppliedCras().forEach((state, networkActions) -> assertTrue(networkActions.isEmpty()));
        assertEquals(angleMonitoringResult.printConstraints(), List.of("AngleCnec acPrev (with importing network element VL1 and exporting network element VL2) at state preventive has an angle of -211°."));
        assertEquals(angleMonitoringResult.getAngle(acPrev, Unit.DEGREE), -211, ANGLE_TOLERANCE);
    }

    @Test
    public void testCurativeStateOnlyWithNoRa() {
        setUpCracFactory("network.xiidm");
        mockCurativeStates();
        runAngleMonitoring();
        assertTrue(angleMonitoringResult.isUnsecure());
        angleMonitoringResult.getAppliedCras().forEach((state, networkActions) -> assertTrue(networkActions.isEmpty()));
        assertEquals(angleMonitoringResult.printConstraints(), List.of("AngleCnec acCur1 (with importing network element VL1 and exporting network element VL2) at state coL1 - curative has an angle of -442°."));
    }

    @Test
    public void testCurativeStateOnlyWithAvailableTopoRa() {
        setUpCracFactory("network.xiidm");
        mockCurativeStates();
        naL1Cur = crac.newNetworkAction()
                .withId("Open L1 - 2")
                .newTopologicalAction().withNetworkElement("L1").withActionType(ActionType.OPEN).add()
                .newOnAngleConstraintUsageRule().withInstant(Instant.CURATIVE).withAngleCnec(acCur1.getId()).add()
                .add();
        runAngleMonitoring();
        assertTrue(angleMonitoringResult.isUnsecure());
        angleMonitoringResult.getAppliedCras().forEach((state, networkActions) -> assertTrue(networkActions.isEmpty()));
        assertEquals(angleMonitoringResult.printConstraints(), List.of("AngleCnec acCur1 (with importing network element VL1 and exporting network element VL2) at state coL1 - curative has an angle of -442°."));
    }

    @Test
    public void testCurativeStateOnlyWithAvailableInjectionRa() {
        setUpCracFactory("network.xiidm");
        mockCurativeStates();
        naL1Cur = crac.newNetworkAction()
                .withId("Injection L1 - 2")
                .newInjectionSetPoint().withNetworkElement("LD2").withSetpoint(50.).add()
                .newOnAngleConstraintUsageRule().withInstant(Instant.CURATIVE).withAngleCnec(acCur1.getId()).add()
                .add();
        runAngleMonitoring();
        assertTrue(angleMonitoringResult.isSecure());
        assertEquals(Set.of(naL1Cur.getId()), angleMonitoringResult.getAppliedCras("coL1 - curative"));
        assertEquals(angleMonitoringResult.printConstraints(), List.of("All AngleCnecs are secure."));
    }

    @Test (expected = FaraoException.class)
    public void testGetAngleExceptions1() {
        setUpCracFactory("network.xiidm");
        mockPreventiveState();
        runAngleMonitoring();
        mockCurativeStates();
        angleMonitoringResult.getAngle(acCur1, Unit.DEGREE);
    }

    @Test (expected = FaraoException.class)
    public void testGetAngleExceptions2() {
        setUpCracFactory("network.xiidm");
        mockPreventiveState();
        runAngleMonitoring();
        angleMonitoringResult.getAngle(acPrev, Unit.KILOVOLT);
    }

    @Test
    public void testCracCim() {
        setUpCimCrac("/CIM_21_7_1.xml", OffsetDateTime.parse("2021-04-02T05:00Z"), new CracCreationParameters());
        assertEquals(2, crac.getAngleCnecs().size());
        assertEquals(Set.of("AngleCnec1", "AngleCnec2"), crac.getAngleCnecs().stream().map(Identifiable::getId).collect(Collectors.toSet()));
        runAngleMonitoring();
        // Status checks
        assertEquals("UNSECURE", angleMonitoringResult.getStatus().toString());
        assertTrue(angleMonitoringResult.isUnsecure());
        assertTrue(!angleMonitoringResult.isSecure());
        assertTrue(!angleMonitoringResult.isUnknown());
        // Applied cras
        State state = crac.getState("Co-1", Instant.CURATIVE);
        assertEquals(1, angleMonitoringResult.getAppliedCras(state).size());
        assertTrue(angleMonitoringResult.getAppliedCras(state).contains(crac.getNetworkAction("RA-1")));
        assertEquals(1, angleMonitoringResult.getAppliedCras("Co-1 - curative").size());
        assertTrue(angleMonitoringResult.getAppliedCras("Co-1 - curative").contains("RA-1"));
        assertEquals(2, angleMonitoringResult.getAppliedCras().size());
        // AngleCnecsWithAngle
        assertEquals(2, angleMonitoringResult.getAngleCnecsWithAngle().size());
        assertEquals(299.54, angleMonitoringResult.getAngle(crac.getAngleCnec("AngleCnec1"), Unit.DEGREE), ANGLE_TOLERANCE);
        assertEquals(angleMonitoringResult.printConstraints(), List.of("AngleCnec AngleCnec1 (with importing network element _d77b61ef-61aa-4b22-95f6-b56ca080788d and exporting network element _8d8a82ba-b5b0-4e94-861a-192af055f2b8) at state Co-1 - curative has an angle of 300°."));
    }
}