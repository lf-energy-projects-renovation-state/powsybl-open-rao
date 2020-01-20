/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.util;

import com.farao_community.farao.data.crac_api.Cnec;
import com.farao_community.farao.data.crac_api.State;
import com.powsybl.sensitivity.SensitivityComputationResults;

import java.util.Map;

/**
 * @author Pengbo Wang {@literal <pengbo.wang at rte-international.com>}
 */
public class SystematicSensitivityAnalysisResult {
    private Map<State, SensitivityComputationResults> stateSensiMap;
    private Map<Cnec, Double> cnecMarginMap;
    private Map<Cnec, Double> cnecMaxThresholdMap;

    public SystematicSensitivityAnalysisResult(Map<State, SensitivityComputationResults> stateSensiMap, Map<Cnec, Double> cnecMarginMap, Map<Cnec, Double> cnecMaxThresholdMap) {
        this.stateSensiMap = stateSensiMap;
        this.cnecMarginMap = cnecMarginMap;
        this.cnecMaxThresholdMap = cnecMaxThresholdMap;
    }

    public Map<State, SensitivityComputationResults> getStateSensiMap() {
        return stateSensiMap;
    }

    public Map<Cnec, Double> getCnecMarginMap() {
        return cnecMarginMap;
    }

    public Map<Cnec, Double> getCnecMaxThresholdMap() {
        return cnecMaxThresholdMap;
    }
}