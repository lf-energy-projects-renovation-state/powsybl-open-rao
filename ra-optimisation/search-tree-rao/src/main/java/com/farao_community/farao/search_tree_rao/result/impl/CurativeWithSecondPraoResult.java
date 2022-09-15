package com.farao_community.farao.search_tree_rao.result.impl;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.RemedialAction;
import com.farao_community.farao.data.crac_api.State;
import com.farao_community.farao.data.crac_api.cnec.Cnec;
import com.farao_community.farao.data.crac_api.cnec.FlowCnec;
import com.farao_community.farao.data.crac_api.network_action.NetworkAction;
import com.farao_community.farao.data.crac_api.range_action.PstRangeAction;
import com.farao_community.farao.data.crac_api.range_action.RangeAction;
import com.farao_community.farao.data.rao_result_api.ComputationStatus;
import com.farao_community.farao.search_tree_rao.result.api.*;
import com.powsybl.sensitivity.SensitivityVariableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CurativeWithSecondPraoResult implements OptimizationResult {

    private final State state; // the optimized state of the curative RAO
    private final OptimizationResult firstCraoResult; // contains information about the perimeter and activated network actions
    private final OptimizationResult secondPraoResult; // contains information about activated range actions
    private final Set<RemedialAction<?>> remedialActionsExcludedFromSecondPreventive; // information about whether CRAs were re-optimized in 2nd PRAO
    private final FlowResult postCraSensitivityFlowResult; // contains final flows
    private final ObjectiveFunctionResult postCraSensitivityObjectiveResult; // contains final flows
    private final SensitivityResult postCraSensitivitySensitivityResult; // contains final flows

    private CurativeWithSecondPraoResult(State state, OptimizationResult firstCraoResult, OptimizationResult secondPraoResult, Set<RemedialAction<?>> remedialActionsExcludedFromSecondPreventive, FlowResult postCraSensitivityFlowResult, ObjectiveFunctionResult postCraSensitivityObjectiveResult, SensitivityResult postCraSensitivitySensitivityResult) {
        this.state = state;
        this.firstCraoResult = firstCraoResult;
        this.secondPraoResult = secondPraoResult;
        this.remedialActionsExcludedFromSecondPreventive = remedialActionsExcludedFromSecondPreventive;
        this.postCraSensitivityFlowResult = postCraSensitivityFlowResult;
        this.postCraSensitivityObjectiveResult = postCraSensitivityObjectiveResult;
        this.postCraSensitivitySensitivityResult = postCraSensitivitySensitivityResult;
    }

    public CurativeWithSecondPraoResult(State state, OptimizationResult firstCraoResult, OptimizationResult secondPraoResult, Set<RemedialAction<?>> remedialActionsExcludedFromSecondPreventive, PrePerimeterResult postCraPrePerimeterResult) {
        this(state, firstCraoResult, secondPraoResult, remedialActionsExcludedFromSecondPreventive, postCraPrePerimeterResult, postCraPrePerimeterResult, postCraPrePerimeterResult);
    }

    public CurativeWithSecondPraoResult(State state, OptimizationResult firstCraoResult, OptimizationResult secondPraoResult, Set<RemedialAction<?>> remedialActionsExcludedFromSecondPreventive) {
        this(state, firstCraoResult, secondPraoResult, remedialActionsExcludedFromSecondPreventive, secondPraoResult, secondPraoResult, secondPraoResult);
    }

    private void checkState(State stateToCheck) {
        if (!state.equals(stateToCheck)) {
            throw new FaraoException(String.format("State %s is not the same as this result's state (%s)", stateToCheck, state.getId()));
        }
    }

    private void checkCnec(Cnec<?> cnec) {
        if (!cnec.getState().equals(state)) {
            throw new FaraoException(String.format("Cnec %s has a different state than this result's state (%s)", cnec.getId(), state.getId()));
        }
    }

    private boolean isCraIncludedInSecondPreventiveRao(RemedialAction<?> remedialAction) {
        return !remedialActionsExcludedFromSecondPreventive.contains(remedialAction);
    }

    @Override
    public double getFlow(FlowCnec flowCnec, Unit unit) {
        checkCnec(flowCnec);
        return postCraSensitivityFlowResult.getFlow(flowCnec, unit);
    }

    @Override
    public double getCommercialFlow(FlowCnec flowCnec, Unit unit) {
        checkCnec(flowCnec);
        return postCraSensitivityFlowResult.getCommercialFlow(flowCnec, unit);
    }

    @Override
    public double getPtdfZonalSum(FlowCnec flowCnec) {
        checkCnec(flowCnec);
        return postCraSensitivityFlowResult.getPtdfZonalSum(flowCnec);
    }

    @Override
    public Map<FlowCnec, Double> getPtdfZonalSums() {
        return firstCraoResult.getPtdfZonalSums().keySet().stream().collect(Collectors.toMap(cnec -> cnec, this::getPtdfZonalSum));
    }

    @Override
    public boolean isActivated(NetworkAction networkAction) {
        if (isCraIncludedInSecondPreventiveRao(networkAction)) {
            return secondPraoResult.isActivated(networkAction);
        } else {
            return firstCraoResult.isActivated(networkAction);
        }
    }

    @Override
    public Set<NetworkAction> getActivatedNetworkActions() {
        // Hard to check which were included in 2nd preventive RAO. We'll suppose none was.
        return firstCraoResult.getActivatedNetworkActions();
    }

    @Override
    public double getFunctionalCost() {
        // Careful : this returns functional cost over all curative perimeters, but it should be enough for normal use
        // since we never really need functional cost per perimeter at the end of the RAO
        return postCraSensitivityObjectiveResult.getFunctionalCost();
    }

    @Override
    public List<FlowCnec> getMostLimitingElements(int number) {
        // Careful : this returns most limiting elements over all curative perimeters, but it should be enough for normal use
        return postCraSensitivityObjectiveResult.getMostLimitingElements(number);
    }

    @Override
    public double getVirtualCost() {
        // Careful : this returns virtual cost over all curative perimeters, but it should be enough for normal use
        return postCraSensitivityObjectiveResult.getVirtualCost();
    }

    @Override
    public Set<String> getVirtualCostNames() {
        return postCraSensitivityObjectiveResult.getVirtualCostNames();
    }

    @Override
    public double getVirtualCost(String virtualCostName) {
        // Careful : this returns virtual cost over all curative perimeters, but it should be enough for normal use
        return postCraSensitivityObjectiveResult.getVirtualCost(virtualCostName);
    }

    @Override
    public List<FlowCnec> getCostlyElements(String virtualCostName, int number) {
        // Careful : this returns costly elements over all curative perimeters, but it should be enough for normal use
        return postCraSensitivityObjectiveResult.getCostlyElements(virtualCostName, number);
    }

    @Override
    public Set<RangeAction<?>> getRangeActions() {
        return firstCraoResult.getRangeActions();
    }

    @Override
    public Set<RangeAction<?>> getActivatedRangeActions(State state) {
        checkState(state);
        Set<RangeAction<?>> activated = firstCraoResult.getActivatedRangeActions(state).stream().filter(ra -> !isCraIncludedInSecondPreventiveRao(ra)).collect(Collectors.toSet());
        activated.addAll(secondPraoResult.getActivatedRangeActions(state));
        return activated;
    }

    @Override
    public double getOptimizedSetpoint(RangeAction<?> rangeAction, State state) {
        checkState(state);
        if (isCraIncludedInSecondPreventiveRao(rangeAction)) {
            return secondPraoResult.getOptimizedSetpoint(rangeAction, state);
        } else {
            return firstCraoResult.getOptimizedSetpoint(rangeAction, state);
        }
    }

    @Override
    public Map<RangeAction<?>, Double> getOptimizedSetpointsOnState(State state) {
        checkState(state);
        return firstCraoResult.getRangeActions().stream().collect(Collectors.toMap(ra -> ra, ra -> getOptimizedSetpoint(ra, state)));
    }

    @Override
    public int getOptimizedTap(PstRangeAction pstRangeAction, State state) {
        checkState(state);
        if (isCraIncludedInSecondPreventiveRao(pstRangeAction)) {
            return secondPraoResult.getOptimizedTap(pstRangeAction, state);
        } else {
            return firstCraoResult.getOptimizedTap(pstRangeAction, state);
        }
    }

    @Override
    public Map<PstRangeAction, Integer> getOptimizedTapsOnState(State state) {
        checkState(state);
        return firstCraoResult.getRangeActions().stream()
            .filter(PstRangeAction.class::isInstance).map(PstRangeAction.class::cast)
            .collect(Collectors.toMap(pst -> pst, pst -> getOptimizedTap(pst, state)));
    }

    @Override
    public ComputationStatus getSensitivityStatus() {
        return postCraSensitivitySensitivityResult.getSensitivityStatus();
    }

    @Override
    public double getSensitivityValue(FlowCnec flowCnec, RangeAction<?> rangeAction, Unit unit) {
        checkCnec(flowCnec);
        if (!firstCraoResult.getRangeActions().contains(rangeAction)) {
            throw new FaraoException(String.format("RangeAction %s does not belong to this result's state (%s)", rangeAction.getId(), state));
        }
        return postCraSensitivitySensitivityResult.getSensitivityValue(flowCnec, rangeAction, unit);
    }

    @Override
    public double getSensitivityValue(FlowCnec flowCnec, SensitivityVariableSet linearGlsk, Unit unit) {
        checkCnec(flowCnec);
        return postCraSensitivitySensitivityResult.getSensitivityValue(flowCnec, linearGlsk, unit);
    }
}