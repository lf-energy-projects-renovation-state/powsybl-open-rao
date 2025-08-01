/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.openrao.searchtreerao.castor.algorithm;

import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.State;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnec;
import com.powsybl.openrao.data.crac.api.rangeaction.RangeAction;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import com.powsybl.openrao.searchtreerao.commons.SensitivityComputer;
import com.powsybl.openrao.searchtreerao.commons.ToolProvider;
import com.powsybl.openrao.searchtreerao.commons.objectivefunction.ObjectiveFunction;
import com.powsybl.openrao.searchtreerao.result.api.*;
import com.powsybl.openrao.searchtreerao.result.impl.*;
import com.powsybl.openrao.sensitivityanalysis.AppliedRemedialActions;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class aims at performing the sensitivity analysis after the optimization of a perimeter. The result can be used as a
 * starting point for the next perimeter, but it is also needed for the costs, margins and flows of elements after an optimization instant.
 *
 * @author Philippe Edwards {@literal <philippe.edwards at rte-france.com>}
 */
public class PostPerimeterSensitivityAnalysis extends AbstractMultiPerimeterSensitivityAnalysis {

    public PostPerimeterSensitivityAnalysis(Crac crac,
                                            Set<FlowCnec> flowCnecs,
                                            Set<RangeAction<?>> rangeActions,
                                            RaoParameters raoParameters,
                                            ToolProvider toolProvider) {
        super(crac, flowCnecs, rangeActions, raoParameters, toolProvider);
    }

    public PostPerimeterSensitivityAnalysis(Crac crac,
                                            Set<State> states,
                                            RaoParameters raoParameters,
                                            ToolProvider toolProvider) {
        super(crac, states, raoParameters, toolProvider);
    }

    /**
     * This method requires:
     * <ul>
     *     <li> the initialFlowResult to be able to compute mnec and loopflow thresholds </li>
     *     <li> the previousResultsFuture for countries not sharing CRAs </li>
     *     <li> the optimizationResult of the given perimeter for action cost </li>
     * </ul>
     */
    public Future<PostPerimeterResult> runBasedOnInitialPreviousAndOptimizationResults(Network network,
                                                                                       FlowResult initialFlowResult,
                                                                                       Future<PrePerimeterResult> previousResultsFuture,
                                                                                       Set<String> operatorsNotSharingCras,
                                                                                       OptimizationResult optimizationResult,
                                                                                       AppliedRemedialActions appliedCurativeRemedialActions) {

        AtomicReference<FlowResult> flowResult = new AtomicReference<>();
        AtomicReference<SensitivityResult> sensitivityResult = new AtomicReference<>();
        boolean actionWasTaken = actionWasTaken(optimizationResult);
        if (actionWasTaken) {
            SensitivityComputer sensitivityComputer = buildSensitivityComputer(initialFlowResult, appliedCurativeRemedialActions);

            sensitivityComputer.compute(network);
            flowResult.set(sensitivityComputer.getBranchResult(network));
            sensitivityResult.set(sensitivityComputer.getSensitivityResult());
        }

        // Thread is executed once previousResultsFuture is fetched
        return Executors.newSingleThreadExecutor().submit(() -> {
            if (!actionWasTaken) {
                flowResult.set(previousResultsFuture.get());
                sensitivityResult.set(previousResultsFuture.get());
            }
            ObjectiveFunction objectiveFunction = ObjectiveFunction.build(
                flowCnecs,
                toolProvider.getLoopFlowCnecs(flowCnecs),
                initialFlowResult,
                previousResultsFuture.get(),
                operatorsNotSharingCras,
                raoParameters,
                optimizationResult.getActivatedRangeActionsPerState().keySet()
            );

            ObjectiveFunctionResult objectiveFunctionResult = objectiveFunction.evaluate(
                flowResult.get(),
                new RemedialActionActivationResultImpl(optimizationResult, optimizationResult)
            );

            return new PostPerimeterResult(optimizationResult, new PrePerimeterSensitivityResultImpl(
                flowResult.get(),
                sensitivityResult.get(),
                RangeActionSetpointResultImpl.buildWithSetpointsFromNetwork(network, rangeActions),
                objectiveFunctionResult
            ));
        });
    }

    private boolean actionWasTaken(OptimizationResult optimizationResult) {
        if (!optimizationResult.getActivatedNetworkActions().isEmpty()) {
            return true;
        }
        return optimizationResult.getActivatedRangeActionsPerState().values().stream()
            .anyMatch(set -> !set.isEmpty());
    }
}
