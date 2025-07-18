/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.openrao.searchtreerao.linearoptimisation.algorithms.fillers;

import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.State;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.data.crac.api.rangeaction.RangeAction;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import com.powsybl.openrao.raoapi.parameters.extensions.SearchTreeRaoCostlyMinMarginParameters;
import com.powsybl.openrao.raoapi.parameters.extensions.SearchTreeRaoRangeActionsOptimizationParameters;
import com.powsybl.openrao.searchtreerao.commons.optimizationperimeters.OptimizationPerimeter;
import com.powsybl.openrao.searchtreerao.linearoptimisation.algorithms.linearproblem.OpenRaoMPConstraint;
import com.powsybl.openrao.searchtreerao.linearoptimisation.algorithms.linearproblem.OpenRaoMPVariable;
import com.powsybl.openrao.searchtreerao.linearoptimisation.algorithms.linearproblem.LinearProblem;
import com.powsybl.openrao.searchtreerao.linearoptimisation.algorithms.linearproblem.LinearProblemBuilder;
import com.powsybl.openrao.searchtreerao.result.api.RangeActionSetpointResult;
import com.powsybl.openrao.searchtreerao.result.impl.RangeActionSetpointResultImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini{@literal <joris.mancini at rte-france.com>}
 * @author Baptiste Seguinot{@literal <baptiste.seguinot at rte-france.com>}
 */
class MaxMinMarginFillerTest extends AbstractFillerTest {
    private LinearProblem linearProblem;
    private MarginCoreProblemFiller coreProblemFiller;
    private MaxMinMarginFiller maxMinMarginFiller;

    @BeforeEach
    public void setUp() throws IOException {
        init();
        network.getTwoWindingsTransformer(RANGE_ACTION_ELEMENT_ID).getPhaseTapChanger().setTapPosition(TAP_INITIAL);
        double initialAlpha = network.getTwoWindingsTransformer(RANGE_ACTION_ELEMENT_ID).getPhaseTapChanger().getCurrentStep().getAlpha();
        RangeActionSetpointResult initialRangeActionSetpointResult = new RangeActionSetpointResultImpl(Map.of(pstRangeAction, initialAlpha));

        OptimizationPerimeter optimizationPerimeter = Mockito.mock(OptimizationPerimeter.class);
        Mockito.when(optimizationPerimeter.getFlowCnecs()).thenReturn(Set.of(cnec1));

        Map<State, Set<RangeAction<?>>> rangeActions = new HashMap<>();
        rangeActions.put(cnec1.getState(), Set.of(pstRangeAction));
        Mockito.when(optimizationPerimeter.getRangeActionsPerState()).thenReturn(rangeActions);

        RaoParameters raoParameters = new RaoParameters();
        raoParameters.getRangeActionsOptimizationParameters().setPstRAMinImpactThreshold(0.01);
        raoParameters.getRangeActionsOptimizationParameters().setHvdcRAMinImpactThreshold(0.01);
        raoParameters.getRangeActionsOptimizationParameters().setInjectionRAMinImpactThreshold(0.01);

        coreProblemFiller = new MarginCoreProblemFiller(
            optimizationPerimeter,
            initialRangeActionSetpointResult,
            raoParameters.getRangeActionsOptimizationParameters(),
            null,
            Unit.MEGAWATT,
            false,
            SearchTreeRaoRangeActionsOptimizationParameters.PstModel.CONTINUOUS,
            null);
    }

    private void createMaxMinMarginFiller(Unit unit) {
        SearchTreeRaoCostlyMinMarginParameters minMarginsParameters = new SearchTreeRaoCostlyMinMarginParameters();
        minMarginsParameters.setShiftedViolationPenalty(1000.0);
        maxMinMarginFiller = new MaxMinMarginFiller(Set.of(cnec1), unit, false, minMarginsParameters, null);
    }

    private void buildLinearProblem() {
        linearProblem = new LinearProblemBuilder()
            .withProblemFiller(coreProblemFiller)
            .withProblemFiller(maxMinMarginFiller)
            .withSolver(SearchTreeRaoRangeActionsOptimizationParameters.Solver.SCIP)
            .withInitialRangeActionActivationResult(getInitialRangeActionActivationResult())
            .build();
        linearProblem.fill(flowResult, sensitivityResult);
    }

    @Test
    void fillWithMaxMinMarginInMegawatt() {
        createMaxMinMarginFiller(Unit.MEGAWATT);
        buildLinearProblem();

        OpenRaoMPVariable flowCnec1 = linearProblem.getFlowVariable(cnec1, TwoSides.ONE, Optional.empty());
        OpenRaoMPVariable upwardVariation = linearProblem.getRangeActionVariationVariable(pstRangeAction, cnec1.getState(), LinearProblem.VariationDirectionExtension.UPWARD);
        OpenRaoMPVariable downwardVariation = linearProblem.getRangeActionVariationVariable(pstRangeAction, cnec1.getState(), LinearProblem.VariationDirectionExtension.DOWNWARD);

        // check minimum margin variable
        OpenRaoMPVariable minimumMargin = linearProblem.getMinimumMarginVariable(Optional.empty());
        assertNotNull(minimumMargin);

        // check minimum margin constraints
        OpenRaoMPConstraint cnec1AboveThreshold = linearProblem.getMinimumMarginConstraint(cnec1, TwoSides.ONE, LinearProblem.MarginExtension.ABOVE_THRESHOLD, Optional.empty());
        OpenRaoMPConstraint cnec1BelowThreshold = linearProblem.getMinimumMarginConstraint(cnec1, TwoSides.ONE, LinearProblem.MarginExtension.BELOW_THRESHOLD, Optional.empty());
        assertNotNull(cnec1AboveThreshold);
        assertNotNull(cnec1BelowThreshold);
        assertEquals(-linearProblem.infinity(), cnec1BelowThreshold.lb(), linearProblem.infinity() * 1e-3);
        assertEquals(-MIN_FLOW_1, cnec1BelowThreshold.ub(), DOUBLE_TOLERANCE);
        assertEquals(-linearProblem.infinity(), cnec1AboveThreshold.lb(), linearProblem.infinity() * 1e-3);
        assertEquals(MAX_FLOW_1, cnec1AboveThreshold.ub(), DOUBLE_TOLERANCE);
        assertEquals(-1, cnec1BelowThreshold.getCoefficient(flowCnec1), DOUBLE_TOLERANCE);
        assertEquals(1, cnec1AboveThreshold.getCoefficient(flowCnec1), DOUBLE_TOLERANCE);
        assertEquals(1, cnec1BelowThreshold.getCoefficient(minimumMargin), DOUBLE_TOLERANCE);
        assertEquals(1, cnec1AboveThreshold.getCoefficient(minimumMargin), DOUBLE_TOLERANCE);

        // check objective
        assertEquals(0.01, linearProblem.getObjective().getCoefficient(upwardVariation), DOUBLE_TOLERANCE); // penalty cost
        assertEquals(0.01, linearProblem.getObjective().getCoefficient(downwardVariation), DOUBLE_TOLERANCE); // penalty cost
        assertEquals(-1.0, linearProblem.getObjective().getCoefficient(minimumMargin), DOUBLE_TOLERANCE); // penalty cost
        assertTrue(linearProblem.minimization());

        // check the number of variables and constraints
        // total number of variables 6 :
        //      - 4 due to CoreFiller
        //      - minimum margin variable
        // total number of constraints 5 :
        //      - 2 due to CoreFiller
        //      - 2 per CNEC (min margin constraints)
        assertEquals(5, linearProblem.numVariables());
        assertEquals(4, linearProblem.numConstraints());
    }

    @Test
    void fillWithMaxMinMarginInAmpere() {
        createMaxMinMarginFiller(Unit.AMPERE);
        buildLinearProblem();

        OpenRaoMPVariable flowCnec1 = linearProblem.getFlowVariable(cnec1, TwoSides.ONE, Optional.empty());
        OpenRaoMPVariable upwardVariation = linearProblem.getRangeActionVariationVariable(pstRangeAction, cnec1.getState(), LinearProblem.VariationDirectionExtension.UPWARD);
        OpenRaoMPVariable downwardVariation = linearProblem.getRangeActionVariationVariable(pstRangeAction, cnec1.getState(), LinearProblem.VariationDirectionExtension.DOWNWARD);

        // check minimum margin variable
        OpenRaoMPVariable minimumMargin = linearProblem.getMinimumMarginVariable(Optional.empty());
        assertNotNull(minimumMargin);

        // check minimum margin constraints
        OpenRaoMPConstraint cnec1AboveThreshold = linearProblem.getMinimumMarginConstraint(cnec1, TwoSides.ONE, LinearProblem.MarginExtension.ABOVE_THRESHOLD, Optional.empty());
        OpenRaoMPConstraint cnec1BelowThreshold = linearProblem.getMinimumMarginConstraint(cnec1, TwoSides.ONE, LinearProblem.MarginExtension.BELOW_THRESHOLD, Optional.empty());
        assertNotNull(cnec1AboveThreshold);
        assertNotNull(cnec1BelowThreshold);
        assertEquals(-linearProblem.infinity(), cnec1BelowThreshold.lb(), linearProblem.infinity() * 1e-3);
        assertEquals(-MIN_FLOW_1, cnec1BelowThreshold.ub(), DOUBLE_TOLERANCE);
        assertEquals(-linearProblem.infinity(), cnec1AboveThreshold.lb(), linearProblem.infinity() * 1e-3);
        assertEquals(MAX_FLOW_1, cnec1AboveThreshold.ub(), DOUBLE_TOLERANCE);
        assertEquals(-1, cnec1BelowThreshold.getCoefficient(flowCnec1), DOUBLE_TOLERANCE);
        assertEquals(1, cnec1AboveThreshold.getCoefficient(flowCnec1), DOUBLE_TOLERANCE);

        assertEquals(380.0 * Math.sqrt(3) / 1000, cnec1BelowThreshold.getCoefficient(minimumMargin), DOUBLE_TOLERANCE);
        assertEquals(380.0 * Math.sqrt(3) / 1000, cnec1AboveThreshold.getCoefficient(minimumMargin), DOUBLE_TOLERANCE);

        // check objective
        assertEquals(0.01, linearProblem.getObjective().getCoefficient(upwardVariation), DOUBLE_TOLERANCE); // penalty cost
        assertEquals(0.01, linearProblem.getObjective().getCoefficient(downwardVariation), DOUBLE_TOLERANCE); // penalty cost
        assertEquals(-1.0, linearProblem.getObjective().getCoefficient(minimumMargin), DOUBLE_TOLERANCE); // penalty cost
        assertTrue(linearProblem.minimization());

        // check the number of variables and constraints
        assertEquals(5, linearProblem.numVariables());
        assertEquals(4, linearProblem.numConstraints());
    }

    @Test
    void fillWithMissingFlowVariables() {
        createMaxMinMarginFiller(Unit.MEGAWATT);
        linearProblem = new LinearProblemBuilder()
            .withProblemFiller(maxMinMarginFiller)
            .withSolver(SearchTreeRaoRangeActionsOptimizationParameters.Solver.SCIP)
            .build();

        // AbsoluteRangeActionVariables present, but no the FlowVariables
        linearProblem.addRangeActionVariationVariable(0.0, pstRangeAction, cnec1.getState(), LinearProblem.VariationDirectionExtension.UPWARD);
        linearProblem.addRangeActionVariationVariable(0.0, pstRangeAction, cnec1.getState(), LinearProblem.VariationDirectionExtension.DOWNWARD);
        Exception e = assertThrows(OpenRaoException.class, () -> linearProblem.fill(flowResult, sensitivityResult));
        assertEquals("Variable Tieline BE FR - N - preventive_one_flow_variable has not been created yet", e.getMessage());
    }

    @Test
    void fillWithMissingRangeActionVariables() {
        try {
            createMaxMinMarginFiller(Unit.MEGAWATT);
            linearProblem = new LinearProblemBuilder()
                .withProblemFiller(maxMinMarginFiller)
                .withSolver(SearchTreeRaoRangeActionsOptimizationParameters.Solver.SCIP)
                .build();

            // FlowVariables present , but not the absoluteRangeActionVariables present,
            // This should work since range actions can be filtered out by the MarginCoreProblemFiller if their number
            // exceeds the max-pst-per-tso parameter
            linearProblem.addFlowVariable(0.0, 0.0, cnec1, TwoSides.ONE, Optional.empty());
            linearProblem.addFlowVariable(0.0, 0.0, cnec2, TwoSides.TWO, Optional.empty());
            linearProblem.fill(flowResult, sensitivityResult);
        } catch (Exception e) {
            fail();
        }
    }
}

