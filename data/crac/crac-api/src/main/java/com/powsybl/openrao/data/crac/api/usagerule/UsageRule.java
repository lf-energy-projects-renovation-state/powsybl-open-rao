/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.State;

/**
 * The UsageRule defines conditions under which a RemedialAction can be used.
 *
 * @author Viktor Terrier {@literal <viktor.terrier at rte-france.com>}
 */
public interface UsageRule {

    /**
     * Get the {@link UsageMethod} of the usage rule
     */
    UsageMethod getUsageMethod();

    /**
     * Get the {@link UsageMethod} of the usage rule on a given state
     */
    UsageMethod getUsageMethod(State state);

    /**
     * Get the Instant of the usage rule
     */
    Instant getInstant();
}
