package com.powsybl.openrao.data.crac.io.nc.craccreator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.google.common.base.Suppliers;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.action.*;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.parameters.CracCreationParameters;
import com.powsybl.openrao.data.crac.api.cnec.AngleCnec;
import com.powsybl.openrao.data.crac.api.cnec.Cnec;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnec;
import com.powsybl.openrao.data.crac.api.cnec.VoltageCnec;
import com.powsybl.openrao.data.crac.api.networkaction.ActionType;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.rangeaction.PstRangeAction;
import com.powsybl.openrao.data.crac.api.threshold.BranchThreshold;
import com.powsybl.openrao.data.crac.api.threshold.Threshold;
import com.powsybl.openrao.data.crac.api.usagerule.OnConstraint;
import com.powsybl.openrao.data.crac.api.usagerule.OnContingencyState;
import com.powsybl.openrao.data.crac.api.usagerule.OnInstant;
import com.powsybl.openrao.data.crac.api.usagerule.UsageMethod;
import com.powsybl.openrao.data.crac.io.commons.api.ElementaryCreationContext;
import com.powsybl.openrao.data.crac.io.commons.api.ImportStatus;
import com.powsybl.openrao.data.crac.io.nc.parameters.Border;
import com.powsybl.openrao.data.crac.io.nc.parameters.NcCracCreationParameters;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public final class NcCracCreationTestUtil {

    public static final String PREVENTIVE_INSTANT_ID = "preventive";
    public static final String OUTAGE_INSTANT_ID = "outage";
    public static final String AUTO_INSTANT_ID = "auto";
    public static final String CURATIVE_1_INSTANT_ID = "curative 1";
    public static final String CURATIVE_2_INSTANT_ID = "curative 2";
    public static final String CURATIVE_3_INSTANT_ID = "curative 3";
    public static final Network NETWORK = getNetworkFromResource("/networks/16Nodes.zip");

    private NcCracCreationTestUtil() {
    }

    public static ListAppender<ILoggingEvent> getLogs(Class<?> logsClass) {
        Logger logger = (Logger) LoggerFactory.getLogger(logsClass);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender;
    }

    public static void assertContingencyEquality(Contingency actualContingency, String expectedContingencyId, String expectedContingencyName, Set<String> expectedNetworkElementsIds) {
        assertEquals(expectedContingencyId, actualContingency.getId());
        assertEquals(Optional.of(expectedContingencyName), actualContingency.getName());
        assertEquals(expectedNetworkElementsIds, actualContingency.getElements().stream().map(ContingencyElement::getId).collect(Collectors.toSet()));
    }

    public static void assertContingencyNotImported(NcCracCreationContext cracCreationContext, String contingencyId, ImportStatus importStatus, String importStatusDetail) {
        assertTrue(cracCreationContext.getContingencyCreationContexts().stream().anyMatch(context -> !context.isImported() && contingencyId.equals(context.getNativeObjectId()) && importStatus.equals(context.getImportStatus()) && importStatusDetail.equals(context.getImportStatusDetail())));
    }

    public static void assertCnecNotImported(NcCracCreationContext cracCreationContext, String assessedElementId, ImportStatus importStatus, String importStatusDetail) {
        assertTrue(cracCreationContext.getCnecCreationContexts().stream().anyMatch(context -> !context.isImported() && assessedElementId.equals(context.getNativeObjectId()) && importStatus.equals(context.getImportStatus()) && importStatusDetail.equals(context.getImportStatusDetail())));
    }

    private static void assertCnecEquality(Cnec<?> cnec, String expectedCnecIdAndName, Set<String> expectedNetworkElementsIds, String expectedInstant, String expectedContingencyId, String expectedOperator, String expectedBorder) {
        assertEquals(expectedCnecIdAndName, cnec.getId());
        assertEquals(expectedCnecIdAndName, cnec.getName());
        assertEquals(expectedNetworkElementsIds, cnec.getNetworkElements().stream().map(NetworkElement::getId).collect(Collectors.toSet()));
        assertEquals(expectedInstant, cnec.getState().getInstant().getId());
        if (expectedContingencyId == null) {
            assertFalse(cnec.getState().getContingency().isPresent());
        } else {
            assertEquals(expectedContingencyId, cnec.getState().getContingency().get().getId());
        }
        assertEquals(expectedOperator, cnec.getOperator());
        assertEquals(expectedBorder, cnec.getBorder());
    }

    public static void assertFlowCnecEquality(FlowCnec flowCnec, String expectedFlowCnecIdAndName, String expectedNetworkElementId, String expectedInstant, String expectedContingencyId, Double expectedThresholdMaxLeft, Double expectedThresholdMinLeft, Double expectedThresholdMaxRight, Double expectedThresholdMinRight, Set<TwoSides> expectedThresholdSides, String expectedOperator, String expectedBorder) {
        assertCnecEquality(flowCnec, expectedFlowCnecIdAndName, Set.of(expectedNetworkElementId), expectedInstant, expectedContingencyId, expectedOperator, expectedBorder);

        List<BranchThreshold> thresholds = flowCnec.getThresholds().stream().sorted(Comparator.comparing(BranchThreshold::getSide)).toList();
        for (BranchThreshold threshold : thresholds) {
            TwoSides side = threshold.getSide();
            assertEquals(side == TwoSides.ONE ? expectedThresholdMaxLeft : expectedThresholdMaxRight, threshold.max().orElse(null));
            assertEquals(side == TwoSides.ONE ? expectedThresholdMinLeft : expectedThresholdMinRight, threshold.min().orElse(null));
        }

        assertEquals(expectedThresholdSides, flowCnec.getMonitoredSides());
    }

    public static void assertAngleCnecEquality(AngleCnec angleCnec, String expectedAngleCnecIdAndName, String expectedImportingNetworkElementId, String expectedExportingNetworkElementId, String expectedInstant, String expectedContingencyId, Double expectedThresholdMax, Double expectedThresholdMin, String expectedOperator, String expectedBorder) {
        assertCnecEquality(angleCnec, expectedAngleCnecIdAndName, Set.of(expectedImportingNetworkElementId, expectedExportingNetworkElementId), expectedInstant, expectedContingencyId, expectedOperator, expectedBorder);
        assertEquals(expectedImportingNetworkElementId, angleCnec.getImportingNetworkElement().getId());
        assertEquals(expectedExportingNetworkElementId, angleCnec.getExportingNetworkElement().getId());

        Threshold threshold = angleCnec.getThresholds().stream().toList().iterator().next();
        assertEquals(expectedThresholdMax, threshold.max().orElse(null));
        assertEquals(expectedThresholdMin, threshold.min().orElse(null));
    }

    public static void assertVoltageCnecEquality(VoltageCnec voltageCnec, String expectedVoltageCnecIdAndName, String expectedNetworkElementId, String expectedInstant, String expectedContingencyId, Double expectedThresholdMax, Double expectedThresholdMin, String expectedOperator, String expectedBorder) {
        assertCnecEquality(voltageCnec, expectedVoltageCnecIdAndName, Set.of(expectedNetworkElementId), expectedInstant, expectedContingencyId, expectedOperator, expectedBorder);

        Threshold threshold = voltageCnec.getThresholds().stream().toList().iterator().next();
        assertEquals(expectedThresholdMax, threshold.max().orElse(null));
        assertEquals(expectedThresholdMin, threshold.min().orElse(null));
    }

    public static void assertPstRangeActionImported(NcCracCreationContext cracCreationContext, String id, String networkElement, boolean isAltered, int numberOfUsageRules, String expectedOperator) {
        ElementaryCreationContext ncElementaryCreationContext = cracCreationContext.getRemedialActionCreationContext(id);
        assertNotNull(ncElementaryCreationContext);
        assertTrue(ncElementaryCreationContext.isImported());
        assertEquals(isAltered, ncElementaryCreationContext.isAltered());
        assertNotNull(cracCreationContext.getCrac().getPstRangeAction(id));
        String actualNetworkElement = cracCreationContext.getCrac().getPstRangeAction(id).getNetworkElement().toString();
        assertEquals(networkElement, actualNetworkElement);
        assertEquals(numberOfUsageRules, cracCreationContext.getCrac().getPstRangeAction(id).getUsageRules().size());
        assertEquals(expectedOperator, cracCreationContext.getCrac().getPstRangeAction(id).getOperator());
    }

    public static void assertNetworkActionImported(NcCracCreationContext cracCreationContext, String id, Set<String> networkElements, boolean isAltered, int numberOfUsageRules, String expectedOperator) {
        ElementaryCreationContext ncElementaryCreationContext = cracCreationContext.getRemedialActionCreationContext(id);
        assertNotNull(ncElementaryCreationContext);
        assertTrue(ncElementaryCreationContext.isImported());
        assertEquals(isAltered, ncElementaryCreationContext.isAltered());
        assertNotNull(cracCreationContext.getCrac().getNetworkAction(id));
        Set<String> actualNetworkElements = cracCreationContext.getCrac().getNetworkAction(id).getNetworkElements().stream().map(NetworkElement::getId).collect(Collectors.toSet());
        assertEquals(networkElements, actualNetworkElements);
        assertEquals(numberOfUsageRules, cracCreationContext.getCrac().getNetworkAction(id).getUsageRules().size());
        assertEquals(expectedOperator, cracCreationContext.getCrac().getNetworkAction(id).getOperator());
    }

    public static void assertHasOnInstantUsageRule(NcCracCreationContext cracCreationContext, String raId, Instant instant, UsageMethod usageMethod) {
        assertTrue(
            cracCreationContext.getCrac().getRemedialAction(raId).getUsageRules().stream().filter(OnInstant.class::isInstance)
                .map(OnInstant.class::cast)
                .anyMatch(ur -> ur.getInstant().equals(instant) && ur.getUsageMethod().equals(usageMethod))
        );
    }

    public static void assertHasOnInstantUsageRule(NcCracCreationContext cracCreationContext, String raId, String instant, UsageMethod usageMethod) {
        assertHasOnInstantUsageRule(cracCreationContext, raId, cracCreationContext.getCrac().getInstant(instant), usageMethod);
    }

    public static void assertHasOnContingencyStateUsageRule(NcCracCreationContext cracCreationContext, String raId, String contingencyId, Instant instant, UsageMethod usageMethod) {
        assertTrue(
            cracCreationContext.getCrac().getRemedialAction(raId).getUsageRules().stream().filter(OnContingencyState.class::isInstance)
                .map(OnContingencyState.class::cast)
                .anyMatch(ur -> ur.getContingency().getId().equals(contingencyId) && ur.getInstant().equals(instant) && ur.getUsageMethod().equals(usageMethod))
        );
    }

    public static void assertHasOnContingencyStateUsageRule(NcCracCreationContext cracCreationContext, String raId, String contingencyId, String instant, UsageMethod usageMethod) {
        assertHasOnContingencyStateUsageRule(cracCreationContext, raId, contingencyId, cracCreationContext.getCrac().getInstant(instant), usageMethod);
    }

    public static void assertHasOnConstraintUsageRule(NcCracCreationContext cracCreationContext, String raId, String cnecId, Instant instant, UsageMethod usageMethod, Class<? extends Cnec<?>> cnecType) {
        assertTrue(
            cracCreationContext.getCrac().getRemedialAction(raId).getUsageRules().stream().filter(OnConstraint.class::isInstance)
                .map(OnConstraint.class::cast)
                .anyMatch(ur -> ur.getCnec().getId().equals(cnecId) && cnecType.isInstance(ur.getCnec()) && ur.getInstant().equals(instant) && ur.getUsageMethod().equals(usageMethod))
        );
    }

    public static void assertHasOnConstraintUsageRule(NcCracCreationContext cracCreationContext, String raId, String flowCnecId, String instant, UsageMethod usageMethod, Class<? extends Cnec<?>> cnecType) {
        assertHasOnConstraintUsageRule(cracCreationContext, raId, flowCnecId, cracCreationContext.getCrac().getInstant(instant), usageMethod, cnecType);
    }

    public static void assertRaNotImported(NcCracCreationContext cracCreationContext, String raId, ImportStatus importStatus, String importStatusDetail) {
        ElementaryCreationContext context = cracCreationContext.getRemedialActionCreationContext(raId);
        assertNotNull(context);
        assertFalse(context.isImported());
        assertEquals(importStatusDetail, context.getImportStatusDetail());
        assertEquals(importStatus, context.getImportStatus());
    }

    public static void assertSimpleTopologicalActionImported(NetworkAction networkAction, String raId, String raName, String switchId, ActionType actionType, String expectedOperator) {
        assertEquals(raId, networkAction.getId());
        assertEquals(raName, networkAction.getName());
        assertEquals(1, networkAction.getElementaryActions().size());
        Action elementaryAction = networkAction.getElementaryActions().iterator().next();
        assertTrue(elementaryAction instanceof SwitchAction);
        assertEquals(switchId, ((SwitchAction) elementaryAction).getSwitchId());
        assertEquals(actionType == ActionType.OPEN, ((SwitchAction) elementaryAction).isOpen());
        assertEquals(expectedOperator, networkAction.getOperator());
    }

    public static void assertSimpleGeneratorActionImported(NetworkAction networkAction, String raId, String raName, String networkElementId, double setpoint, String expectedOperator) {
        assertEquals(raId, networkAction.getId());
        assertEquals(raName, networkAction.getName());
        assertEquals(1, networkAction.getElementaryActions().size());
        Action elementaryAction = networkAction.getElementaryActions().iterator().next();
        assertTrue(elementaryAction instanceof GeneratorAction);
        assertEquals(networkElementId, ((GeneratorAction) elementaryAction).getGeneratorId());
        assertEquals(setpoint, ((GeneratorAction) elementaryAction).getActivePowerValue().getAsDouble());
        assertEquals(expectedOperator, networkAction.getOperator());
    }

    public static void assertSimpleLoadActionImported(NetworkAction networkAction, String raId, String raName, String networkElementId, double setpoint, String expectedOperator) {
        assertEquals(raId, networkAction.getId());
        assertEquals(raName, networkAction.getName());
        assertEquals(1, networkAction.getElementaryActions().size());
        Action elementaryAction = networkAction.getElementaryActions().iterator().next();
        assertTrue(elementaryAction instanceof LoadAction);
        assertEquals(networkElementId, ((LoadAction) elementaryAction).getLoadId());
        assertEquals(setpoint, ((LoadAction) elementaryAction).getActivePowerValue().getAsDouble());
        assertEquals(expectedOperator, networkAction.getOperator());
    }

    public static void assertSimpleShuntCompensatorActionImported(NetworkAction networkAction, String raId, String raName, String networkElementId, double setpoint, String expectedOperator) {
        assertEquals(raId, networkAction.getId());
        assertEquals(raName, networkAction.getName());
        assertEquals(1, networkAction.getElementaryActions().size());
        Action elementaryAction = networkAction.getElementaryActions().iterator().next();
        assertTrue(elementaryAction instanceof ShuntCompensatorPositionAction);
        assertEquals(networkElementId, ((ShuntCompensatorPositionAction) elementaryAction).getShuntCompensatorId());
        assertEquals(setpoint, ((ShuntCompensatorPositionAction) elementaryAction).getSectionCount());
        assertEquals(expectedOperator, networkAction.getOperator());
    }

    public static void assertPstRangeActionImported(PstRangeAction pstRangeAction, String expectedId, String expectedName, String expectedPstId, Integer expectedMinTap, Integer expectedMaxTap, String expectedOperator) {
        assertEquals(expectedId, pstRangeAction.getId());
        assertEquals(expectedName, pstRangeAction.getName());
        assertEquals(expectedPstId, pstRangeAction.getNetworkElement().getId());
        assertEquals(expectedOperator, pstRangeAction.getOperator());
        if (expectedMinTap == null && expectedMaxTap == null) {
            return;
        }
        assertEquals(expectedMinTap == null ? Integer.MIN_VALUE : expectedMinTap, pstRangeAction.getRanges().get(0).getMinTap());
        assertEquals(expectedMaxTap == null ? Integer.MAX_VALUE : expectedMaxTap, pstRangeAction.getRanges().get(0).getMaxTap());
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive, CracCreationParameters cracCreationParameters) {
        Network network = getNetworkFromResource(ncArchive);
        return getNcCracCreationContext(ncArchive, network, cracCreationParameters);
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive) {
        return getNcCracCreationContext(ncArchive, cracCreationDefaultParametersWithSweCsaExtension());
    }

    public static CracCreationParameters cracCreationDefaultParametersWithSweCsaExtension() {
        CracCreationParameters cracCreationParameters = new CracCreationParameters();
        cracCreationParameters.addExtension(NcCracCreationParameters.class, new NcCracCreationParameters());
        cracCreationParameters.getExtension(NcCracCreationParameters.class).setCapacityCalculationRegionEicCode("10Y1001C--00095L");
        cracCreationParameters.getExtension(NcCracCreationParameters.class).setCurativeInstants(Map.of("curative 1", 300, "curative 2", 600, "curative 3", 1200));
        cracCreationParameters.getExtension(NcCracCreationParameters.class).setTsosWhichDoNotUsePatlInFinalState(Set.of("REE"));
        cracCreationParameters.getExtension(NcCracCreationParameters.class).setBorders(Set.of(new Border("ES-FR", "10YDOM--ES-FR--D", "RTE"), new Border("ES-PT", "10YDOM--ES-PT--T", "REN")));
        cracCreationParameters.getExtension(NcCracCreationParameters.class).setRestrictedCurativeBatchesPerTso(Map.of("REE", Set.of("curative 1")));
        return cracCreationParameters;
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive, Network network) {
        return getNcCracCreationContext(ncArchive, network, OffsetDateTime.parse("2023-03-29T12:00Z"), cracCreationDefaultParametersWithSweCsaExtension());
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive, Network network, CracCreationParameters cracCreationParameters) {
        return getNcCracCreationContext(ncArchive, network, OffsetDateTime.parse("2023-03-29T12:00Z"), cracCreationParameters);
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive, Network network, String timestamp) {
        return getNcCracCreationContext(ncArchive, network, OffsetDateTime.parse(timestamp), cracCreationDefaultParametersWithSweCsaExtension());
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive, Network network, OffsetDateTime offsetDateTime) {
        return getNcCracCreationContext(ncArchive, network, offsetDateTime, cracCreationDefaultParametersWithSweCsaExtension());
    }

    public static NcCracCreationContext getNcCracCreationContext(String ncArchive, Network network, OffsetDateTime offsetDateTime, CracCreationParameters cracCreationParameters) {
        try (InputStream inputStream = NcCracCreationTestUtil.class.getResourceAsStream(ncArchive)) {
            cracCreationParameters.getExtension(NcCracCreationParameters.class).setTimestamp(offsetDateTime);
            return (NcCracCreationContext) Crac.readWithContext(ncArchive, inputStream, network, cracCreationParameters);
        } catch (IOException e) {
            throw new OpenRaoException(e);
        }
    }

    public static Network getNetworkFromResource(String filename) {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, false);
        return Network.read(Paths.get(new File(NcCracCreationTestUtil.class.getResource(filename).getFile()).toString()), LocalComputationManager.getDefault(), Suppliers.memoize(ImportConfig::load).get(), importParams);
    }
}
