/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.data.crac_api;

import com.powsybl.iidm.network.Network;

/**
 * Interface for Critical Network Element & Contingencies
 * State object represents the contingency. This type of elements can be violated
 * by maximum value or minimum value. So they have thresholds.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public interface Cnec {

    State getState();

    NetworkElement getCriticalNetworkElement();

    boolean isMinThresholdViolated(Network network);

    boolean isMaxThresholdViolated(Network network);
}