package com.powsybl.openrao.data.craccreation.creator.csaprofile.craccreator.cnec;

import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.cracapi.Instant;
import com.powsybl.openrao.data.cracapi.cnec.AngleCnec;
import com.powsybl.openrao.data.cracapi.usagerule.UsageMethod;
import com.powsybl.openrao.data.craccreation.creator.csaprofile.craccreator.CsaProfileCracCreationContext;
import com.powsybl.openrao.data.craccreation.creator.csaprofile.craccreator.CsaProfileCracCreationTestUtil;
import com.powsybl.openrao.data.craccreation.creator.csaprofile.craccreator.CsaProfileElementaryCreationContext;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.powsybl.openrao.data.craccreation.creator.csaprofile.craccreator.CsaProfileCracCreationTestUtil.getCsaCracCreationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AngleCnecCreationTest {

    @Test
    void testAngleCnecImportFromValidProfiles() {
        Network network = Mockito.mock(Network.class);
        BusbarSection terminal1Mock = Mockito.mock(BusbarSection.class);
        BusbarSection terminal2Mock = Mockito.mock(BusbarSection.class);
        Switch switchMock = Mockito.mock(Switch.class);
        Branch networkElementMock = Mockito.mock(Branch.class);

        Mockito.when(terminal1Mock.getId()).thenReturn("bdfd51d2-f48a-424e-a42d-0f6e712094bb");
        Mockito.when(terminal2Mock.getId()).thenReturn("601ac88b-14bc-448a-b8a7-e0b8874a478d");
        Mockito.when(terminal1Mock.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(terminal2Mock.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(switchMock.getId()).thenReturn("40ed5398-3a74-4581-a3c1-688f9764a2b5");
        Mockito.when(networkElementMock.getId()).thenReturn("1bac939d-d873-48e0-9640-5743f389f3de");
        Mockito.when(switchMock.isOpen()).thenReturn(false);
        Mockito.when(network.getIdentifiable("bdfd51d2-f48a-424e-a42d-0f6e712094bb")).thenReturn((Identifiable) terminal1Mock);
        Mockito.when(network.getIdentifiable("601ac88b-14bc-448a-b8a7-e0b8874a478d")).thenReturn((Identifiable) terminal2Mock);
        Mockito.when(network.getIdentifiable("40ed5398-3a74-4581-a3c1-688f9764a2b5")).thenReturn((Identifiable) switchMock);
        Mockito.when(network.getIdentifiable("1bac939d-d873-48e0-9640-5743f389f3de")).thenReturn(networkElementMock);

        CsaProfileCracCreationContext cracCreationContext = getCsaCracCreationContext("/csa-13/CSA_13_3_ValidProfiles.zip", network);

        assertEquals(4, cracCreationContext.getCrac().getAngleCnecs().size());
        List<AngleCnec> angleCnecs = cracCreationContext.getCrac().getAngleCnecs().stream()
                .sorted(Comparator.comparing(AngleCnec::getId)).toList();

        // RTE_AE1 - preventive
        AngleCnec angleCnec1 = angleCnecs.iterator().next();
        assertEquals("RTE_AE1 (9a047603-2161-468e-8918-ca07cf8882b4) - preventive", angleCnec1.getId());
        assertEquals("RTE_AE1 (9a047603-2161-468e-8918-ca07cf8882b4) - preventive", angleCnec1.getName());
        assertEquals("601ac88b-14bc-448a-b8a7-e0b8874a478d", angleCnec1.getImportingNetworkElement().getId());
        assertEquals("bdfd51d2-f48a-424e-a42d-0f6e712094bb", angleCnec1.getExportingNetworkElement().getId());
        assertTrue(angleCnec1.getLowerBound(Unit.DEGREE).isPresent());
        assertEquals(-60.0, angleCnec1.getLowerBound(Unit.DEGREE).get());
        assertFalse(angleCnec1.getUpperBound(Unit.DEGREE).isPresent());

        // RTE_AE2 - RTE_CO1 - curative
        AngleCnec angleCnec2 = angleCnecs.get(1);
        assertEquals("RTE_AE2 (a7d3b8d0-2f9f-4feb-8973-5797895fa6e6) - RTE_CO1 - curative", angleCnec2.getId());
        assertEquals("RTE_AE2 (a7d3b8d0-2f9f-4feb-8973-5797895fa6e6) - RTE_CO1 - curative", angleCnec2.getName());
        assertEquals("bdfd51d2-f48a-424e-a42d-0f6e712094bb", angleCnec2.getImportingNetworkElement().getId());
        assertEquals("601ac88b-14bc-448a-b8a7-e0b8874a478d", angleCnec2.getExportingNetworkElement().getId());
        assertFalse(angleCnec2.getLowerBound(Unit.DEGREE).isPresent());
        assertTrue(angleCnec2.getUpperBound(Unit.DEGREE).isPresent());
        assertEquals(35.0, angleCnec2.getUpperBound(Unit.DEGREE).get());

        // RTE_AE2 - preventive
        AngleCnec angleCnec3 = angleCnecs.get(2);
        assertEquals("RTE_AE2 (a7d3b8d0-2f9f-4feb-8973-5797895fa6e6) - preventive", angleCnec3.getId());
        assertEquals("RTE_AE2 (a7d3b8d0-2f9f-4feb-8973-5797895fa6e6) - preventive", angleCnec3.getName());
        assertEquals("bdfd51d2-f48a-424e-a42d-0f6e712094bb", angleCnec3.getImportingNetworkElement().getId());
        assertEquals("601ac88b-14bc-448a-b8a7-e0b8874a478d", angleCnec3.getExportingNetworkElement().getId());
        assertFalse(angleCnec3.getLowerBound(Unit.DEGREE).isPresent());
        assertTrue(angleCnec3.getUpperBound(Unit.DEGREE).isPresent());
        assertEquals(35.0, angleCnec3.getUpperBound(Unit.DEGREE).get());

        // RTE_AE3 - RTE_CO1 - curative
        AngleCnec angleCnec4 = angleCnecs.get(3);
        assertEquals("RTE_AE3 (99bd49f8-fa11-4d41-8e2b-8440a1581c6d) - RTE_CO1 - curative", angleCnec4.getId());
        assertEquals("RTE_AE3 (99bd49f8-fa11-4d41-8e2b-8440a1581c6d) - RTE_CO1 - curative", angleCnec4.getName());
        assertEquals("601ac88b-14bc-448a-b8a7-e0b8874a478d", angleCnec4.getImportingNetworkElement().getId());
        assertEquals("bdfd51d2-f48a-424e-a42d-0f6e712094bb", angleCnec4.getExportingNetworkElement().getId());
        assertTrue(angleCnec4.getLowerBound(Unit.DEGREE).isPresent());
        assertEquals(-120.0, angleCnec4.getLowerBound(Unit.DEGREE).get());
        assertTrue(angleCnec4.getUpperBound(Unit.DEGREE).isPresent());
        assertEquals(120.0, angleCnec4.getUpperBound(Unit.DEGREE).get());

        // TODO: add onAngleConstraint usage rules checks when CSA-11 is merged
        // TODO: add ER profile with wrong header
    }

    @Test
    void testAngleCnecImportFromInvalidProfiles() {
        Network network = Mockito.mock(Network.class);
        BusbarSection terminal1Mock = Mockito.mock(BusbarSection.class);
        BusbarSection terminal2Mock = Mockito.mock(BusbarSection.class);
        Mockito.when(terminal1Mock.getId()).thenReturn("7ce8103f-e4d4-4f1a-94a0-ffaf76049e38");
        Mockito.when(terminal2Mock.getId()).thenReturn("008952f4-0b93-4622-af28-49934dde3db3");
        Mockito.when(terminal1Mock.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(terminal2Mock.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(network.getIdentifiable("7ce8103f-e4d4-4f1a-94a0-ffaf76049e38")).thenReturn((Identifiable) terminal1Mock);
        Mockito.when(network.getIdentifiable("008952f4-0b93-4622-af28-49934dde3db3")).thenReturn((Identifiable) terminal2Mock);

        CsaProfileCracCreationContext cracCreationContext = getCsaCracCreationContext("/csa-13/CSA_13_4_InvalidProfiles.zip", network);

        assertEquals(0, cracCreationContext.getCrac().getAngleCnecs().size());

        List<CsaProfileElementaryCreationContext> cnecCreationContexts = cracCreationContext.getCnecCreationContexts().stream()
                .sorted(Comparator.comparing(CsaProfileElementaryCreationContext::getNativeId)).toList();
        assertEquals(6, cnecCreationContexts.size());

        // Missing AngleReferenceTerminal
        assertEquals("61f31133-5d71-4d60-bc17-70bed6610101", cnecCreationContexts.iterator().next().getNativeId());
        assertEquals("Assessed Element 61f31133-5d71-4d60-bc17-70bed6610101 ignored because angle limit equipment is missing in network : eb090246-2037-481f-baba-36ab347ff119.", cnecCreationContexts.iterator().next().getImportStatusDetail());

        // Importing and exporting network elements are the same terminal
        assertEquals("690b90c4-892c-4638-a083-6cf8e8e1cfc2", cnecCreationContexts.get(1).getNativeId());
        assertEquals("Assessed Element 690b90c4-892c-4638-a083-6cf8e8e1cfc2 ignored because AngleCNEC's importing and exporting equipments are the same : 7ce8103f-e4d4-4f1a-94a0-ffaf76049e38.", cnecCreationContexts.get(1).getImportStatusDetail());

        // Negative normal value
        assertEquals("c2657640-ff0a-4026-9b18-0e745647ceb0", cnecCreationContexts.get(2).getNativeId());
        assertEquals("Assessed Element c2657640-ff0a-4026-9b18-0e745647ceb0 ignored because the angle limit's normal value is negative.", cnecCreationContexts.get(2).getImportStatusDetail());

        // Undefined VoltageAngleLimit.isFlowToRefTerminal + OperationalLimitType.direction HIGH
        assertEquals("ca931f31-1f48-43bd-9ff4-59d5701d6552", cnecCreationContexts.get(3).getNativeId());
        assertEquals("Assessed Element ca931f31-1f48-43bd-9ff4-59d5701d6552 ignored because of an ambiguous angle limit direction definition from an undefined VoltageAngleLimit.isFlowToRefTerminal and an OperationalLimit.OperationalLimitType : http://iec.ch/TC57/CIM100#OperationalLimitDirectionKind.high.", cnecCreationContexts.get(3).getImportStatusDetail());

        // Undefined VoltageAngleLimit.isFlowToRefTerminal + OperationalLimitType.direction LOW
        assertEquals("e7ce6d03-dd09-4390-8ea8-5e26bf56c009", cnecCreationContexts.get(4).getNativeId());
        assertEquals("Assessed Element e7ce6d03-dd09-4390-8ea8-5e26bf56c009 ignored because of an ambiguous angle limit direction definition from an undefined VoltageAngleLimit.isFlowToRefTerminal and an OperationalLimit.OperationalLimitType : http://iec.ch/TC57/CIM100#OperationalLimitDirectionKind.low.", cnecCreationContexts.get(4).getImportStatusDetail());

        // Missing OperationalLimitSet.Terminal
        assertEquals("eaff2f9c-3fcd-41a3-ac11-79d89bf3a393", cnecCreationContexts.get(5).getNativeId());
        assertEquals("Assessed Element eaff2f9c-3fcd-41a3-ac11-79d89bf3a393 ignored because angle limit equipment is missing in network : eb090246-2037-481f-baba-36ab347ff119.", cnecCreationContexts.get(5).getImportStatusDetail());
    }

    @Test
    void checkOnConstraintWith4AngleCnecs() {
        Network network = Mockito.spy(Network.create("Test", "code"));
        BusbarSection terminal1Mock = Mockito.mock(BusbarSection.class);
        BusbarSection terminal2Mock = Mockito.mock(BusbarSection.class);
        Switch switchMock = Mockito.mock(Switch.class);
        Branch networkElementMock = Mockito.mock(Branch.class);

        Mockito.when(terminal1Mock.getId()).thenReturn("60038442-5c02-21a9-22ad-f0554a65a466");
        Mockito.when(terminal2Mock.getId()).thenReturn("65e9a6a7-8488-7b17-6344-cb7d61b7920b");
        Mockito.when(terminal1Mock.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(terminal2Mock.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(switchMock.getId()).thenReturn("f9c8d9ce-6c44-4293-b60e-93c658411d68");
        Mockito.when(networkElementMock.getId()).thenReturn("3a88a6a7-66fe-4988-9019-b3b288fd54ee");
        Mockito.when(switchMock.isOpen()).thenReturn(false);
        Mockito.when(network.getIdentifiable("60038442-5c02-21a9-22ad-f0554a65a466")).thenReturn((Identifiable) terminal1Mock);
        Mockito.when(network.getIdentifiable("65e9a6a7-8488-7b17-6344-cb7d61b7920b")).thenReturn((Identifiable) terminal2Mock);
        Mockito.when(network.getSwitch("f9c8d9ce-6c44-4293-b60e-93c658411d68")).thenReturn(switchMock);
        Mockito.when(network.getIdentifiable("3a88a6a7-66fe-4988-9019-b3b288fd54ee")).thenReturn(networkElementMock);

        CsaProfileCracCreationContext cracCreationContext = getCsaCracCreationContext("/csa-11/CSA_11_4_OnAngleConstraint.zip", network);
        Instant preventiveInstant = cracCreationContext.getCrac().getInstant("preventive");
        Instant curativeInstant = cracCreationContext.getCrac().getInstant("curative");

        CsaProfileCracCreationTestUtil.assertAngleCnecEquality(cracCreationContext.getCrac().getAngleCnec("RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - RTE_CO1 - curative"),
                "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - RTE_CO1 - curative",
                "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - RTE_CO1 - curative",
                "60038442-5c02-21a9-22ad-f0554a65a466",
                "65e9a6a7-8488-7b17-6344-cb7d61b7920b",
                curativeInstant,
                "6c9656a6-84c2-4967-aabc-51f63a7abdf1",
                30.,
                -30.,
                true);

        CsaProfileCracCreationTestUtil.assertAngleCnecEquality(cracCreationContext.getCrac().getAngleCnec("RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - preventive"),
                "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - preventive",
                "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - preventive",
                "60038442-5c02-21a9-22ad-f0554a65a466",
                "65e9a6a7-8488-7b17-6344-cb7d61b7920b",
                preventiveInstant,
                null,
                30.,
                -30.,
                true);

        CsaProfileCracCreationTestUtil.assertAngleCnecEquality(cracCreationContext.getCrac().getAngleCnec("RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - RTE_CO2 - curative"),
                "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - RTE_CO2 - curative",
                "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - RTE_CO2 - curative",
                "65e9a6a7-8488-7b17-6344-cb7d61b7920b",
                "60038442-5c02-21a9-22ad-f0554a65a466",
                curativeInstant,
                "410a7075-51df-4c5c-aa80-0bb1bbe41190",
                15.,
                -15.,
                true);

        CsaProfileCracCreationTestUtil.assertAngleCnecEquality(cracCreationContext.getCrac().getAngleCnec("RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - preventive"),
                "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - preventive",
                "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - preventive",
                "65e9a6a7-8488-7b17-6344-cb7d61b7920b",
                "60038442-5c02-21a9-22ad-f0554a65a466",
                preventiveInstant,
                null,
                15.,
                -15.,
                true);

        //4 remedial actions and a total of 8 onAngleConstraint usage rules.
        assertEquals(4, cracCreationContext.getCrac().getRemedialActions().size());
        CsaProfileCracCreationTestUtil.assertNetworkActionImported(cracCreationContext, "6c283463-9aac-4d9b-9d0b-6710c5b2aa00", Set.of("f9c8d9ce-6c44-4293-b60e-93c658411d68"), true, 2);
        CsaProfileCracCreationTestUtil.assertNetworkActionImported(cracCreationContext, "0af9ce7e-8013-4362-96a0-40ac0a970eb6", Set.of("f9c8d9ce-6c44-4293-b60e-93c658411d68"), false, 2);
        CsaProfileCracCreationTestUtil.assertNetworkActionImported(cracCreationContext, "f17a745b-60a1-4acd-887f-ebc8349b4597", Set.of("f9c8d9ce-6c44-4293-b60e-93c658411d68"), true, 2);
        CsaProfileCracCreationTestUtil.assertNetworkActionImported(cracCreationContext, "a8f21a9a-49dc-4c2a-9745-405392f0d87b", Set.of("f9c8d9ce-6c44-4293-b60e-93c658411d68"), false, 2);

        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "6c283463-9aac-4d9b-9d0b-6710c5b2aa00", "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - preventive", preventiveInstant, UsageMethod.AVAILABLE);
        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "6c283463-9aac-4d9b-9d0b-6710c5b2aa00", "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - RTE_CO1 - curative", preventiveInstant, UsageMethod.AVAILABLE);

        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "0af9ce7e-8013-4362-96a0-40ac0a970eb6", "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - preventive", preventiveInstant, UsageMethod.AVAILABLE);
        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "0af9ce7e-8013-4362-96a0-40ac0a970eb6", "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - RTE_CO2 - curative", preventiveInstant, UsageMethod.AVAILABLE);

        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "f17a745b-60a1-4acd-887f-ebc8349b4597", "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - preventive", preventiveInstant, UsageMethod.AVAILABLE);
        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "f17a745b-60a1-4acd-887f-ebc8349b4597", "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - RTE_CO2 - curative", preventiveInstant, UsageMethod.AVAILABLE);

        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "a8f21a9a-49dc-4c2a-9745-405392f0d87b", "RTE_AE1 (e2b71e64-ce03-4aa3-9adc-7ff910adae36) - RTE_CO1 - curative", curativeInstant, UsageMethod.AVAILABLE);
        CsaProfileCracCreationTestUtil.assertHasOnAngleConstraintUsageRule(cracCreationContext, "a8f21a9a-49dc-4c2a-9745-405392f0d87b", "RTE_AE2 (a418e290-0d0c-4f40-b7fa-31fca1a2607d) - RTE_CO2 - curative", curativeInstant, UsageMethod.AVAILABLE);
    }

}