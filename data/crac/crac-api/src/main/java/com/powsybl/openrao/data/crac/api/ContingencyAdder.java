/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElementType;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public interface ContingencyAdder extends IdentifiableAdder<ContingencyAdder> {

    ContingencyAdder withContingencyElement(String contingencyElementId, ContingencyElementType contingencyElementType);

    Contingency add();
}
