/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.linear_rao;

import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Pengbo Wang {@literal <pengbo.wang at rte-international.com>}
 */
public class LinearRaoProblem extends MPSolver {

    private Map<String, MPVariable> flowVariablesMap;
    private Map<String, MPVariable> positivePstShiftVariablesMap;
    private Map<String, MPVariable> negativePstShiftVariablesMap;

    protected LinearRaoProblem(long cPtr, boolean cMemoryOwn, Map<String, MPVariable> flowVariablesMap, Map<String, MPVariable> positivePstShiftVariablesMap, Map<String, MPVariable> negativePstShiftVariablesMap) {
        super(cPtr, cMemoryOwn);
        this.flowVariablesMap = flowVariablesMap;
        this.positivePstShiftVariablesMap = positivePstShiftVariablesMap;
        this.negativePstShiftVariablesMap = negativePstShiftVariablesMap;
    }

    public MPVariable flowVariable(String cnecId) {
        return flowVariablesMap.get(cnecId);
    }

    public List<MPVariable> flowVariables() {
        return new ArrayList<>(this.flowVariablesMap.values());
    }

    public MPVariable positivePstShiftVariable(String cnecId) {
        return positivePstShiftVariablesMap.get(cnecId);
    }

    public List<MPVariable> positivePstShiftVariables() {
        return new ArrayList<>(positivePstShiftVariablesMap.values());
    }

    public MPVariable negativePstShiftVariable(String cnecId) {
        return negativePstShiftVariablesMap.get(cnecId);
    }

    public List<MPVariable> negativePstShiftVariables() {
        return new ArrayList<>(negativePstShiftVariablesMap.values());
    }
}