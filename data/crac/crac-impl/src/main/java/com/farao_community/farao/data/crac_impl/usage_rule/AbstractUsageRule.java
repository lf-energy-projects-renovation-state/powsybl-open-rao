/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.data.crac_impl.usage_rule;

import com.farao_community.farao.data.crac_api.State;
import com.farao_community.farao.data.crac_api.UsageMethod;
import com.farao_community.farao.data.crac_api.UsageRule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Business object of a usage rule in the CRAC file
 *
 * @author Viktor Terrier {@literal <viktor.terrier at rte-france.com>}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FreeToUse.class, name = "free-to-use"),
        @JsonSubTypes.Type(value = OnConstraint.class, name = "on-constraint"),
        @JsonSubTypes.Type(value = OnContingency.class, name = "on-contingency")
    })
public abstract class AbstractUsageRule implements UsageRule {

    protected UsageMethod usageMethod;
    protected State state;

    @JsonCreator
    public AbstractUsageRule(@JsonProperty("usageMethod") UsageMethod usageMethod,
                             @JsonProperty("state") State state) {
        this.usageMethod = usageMethod;
        this.state = state;
    }

    public UsageMethod getUsageMethod() {
        return usageMethod;
    }

    public void setUsageMethod(UsageMethod usageMethod) {
        this.usageMethod = usageMethod;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractUsageRule rule = (AbstractUsageRule) o;
        return usageMethod.equals(rule.getUsageMethod()) && state.equals(rule.getState());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}