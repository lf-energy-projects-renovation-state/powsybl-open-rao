/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.open_rao.data.rao_result_impl;

import com.powsybl.open_rao.data.crac_api.Instant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philippe Edwards {@literal <philippe.edwards at rte-france.com>}
 */
public class VoltageCnecResult {

    private static final ElementaryVoltageCnecResult DEFAULT_RESULT = new ElementaryVoltageCnecResult();
    private final Map<Instant, ElementaryVoltageCnecResult> results;

    VoltageCnecResult() {
        results = new HashMap<>();
    }

    public ElementaryVoltageCnecResult getResult(Instant optimizedInstant) {
        return results.getOrDefault(optimizedInstant, DEFAULT_RESULT);
    }

    public ElementaryVoltageCnecResult getAndCreateIfAbsentResultForOptimizationState(Instant optimizedInstant) {
        results.putIfAbsent(optimizedInstant, new ElementaryVoltageCnecResult());
        return results.get(optimizedInstant);
    }
}