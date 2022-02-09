/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.data.crac_api.range_action;

import com.farao_community.farao.data.crac_api.RemedialActionAdder;
import com.farao_community.farao.data.crac_api.range.StandardRangeAdder;

/**
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public interface StandardRangeActionAdder<T extends StandardRangeActionAdder<T>> extends RemedialActionAdder<T> {

    StandardRangeAdder<T> newRange();

    T withGroupId(String groupId);

}