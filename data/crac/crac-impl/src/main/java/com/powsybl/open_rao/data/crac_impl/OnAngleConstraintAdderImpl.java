/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.open_rao.data.crac_impl;

import com.powsybl.open_rao.commons.OpenRaoException;
import com.powsybl.open_rao.data.crac_api.Instant;
import com.powsybl.open_rao.data.crac_api.cnec.AngleCnec;
import com.powsybl.open_rao.data.crac_api.usage_rule.OnAngleConstraint;
import com.powsybl.open_rao.data.crac_api.usage_rule.OnAngleConstraintAdder;
import com.powsybl.open_rao.data.crac_api.usage_rule.UsageMethod;

import java.util.Objects;

import static com.powsybl.open_rao.data.crac_impl.AdderUtils.assertAttributeNotNull;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class OnAngleConstraintAdderImpl<T extends AbstractRemedialActionAdder<T>> implements OnAngleConstraintAdder<T> {

    public static final String ON_ANGLE_CONSTRAINT = "OnAngleConstraint";
    private T owner;
    private String instantId;
    private String angleCnecId;
    private UsageMethod usageMethod;

    OnAngleConstraintAdderImpl(AbstractRemedialActionAdder<T> owner) {
        this.owner = (T) owner;
    }

    @Override
    public OnAngleConstraintAdder<T> withInstant(String instantId) {
        this.instantId = instantId;
        return this;
    }

    @Override
    public OnAngleConstraintAdder<T> withUsageMethod(UsageMethod usageMethod) {
        this.usageMethod = usageMethod;
        return this;
    }

    @Override
    public OnAngleConstraintAdder<T> withAngleCnec(String angleCnecId) {
        this.angleCnecId = angleCnecId;
        return this;
    }

    @Override
    public T add() {
        assertAttributeNotNull(instantId, ON_ANGLE_CONSTRAINT, "instant", "withInstant()");
        assertAttributeNotNull(angleCnecId, ON_ANGLE_CONSTRAINT, "angle cnec", "withAngleCnec()");
        assertAttributeNotNull(usageMethod, ON_ANGLE_CONSTRAINT, "usage method", "withUsageMethod()");

        Instant instant = owner.getCrac().getInstant(instantId);
        if (instant.isOutage()) {
            throw new OpenRaoException("OnAngleConstraint usage rules are not allowed for OUTAGE instant.");
        }
        if (instant.isPreventive()) {
            owner.getCrac().addPreventiveState();
        }

        AngleCnec angleCnec = owner.getCrac().getAngleCnec(angleCnecId);
        if (Objects.isNull(angleCnec)) {
            throw new OpenRaoException(String.format("AngleCnec %s does not exist in crac. Consider adding it first.", angleCnecId));
        }

        AbstractRemedialActionAdder.checkOnConstraintUsageRules(instant, angleCnec);

        OnAngleConstraint onAngleConstraint = new OnAngleConstraintImpl(usageMethod, instant, angleCnec);
        owner.addUsageRule(onAngleConstraint);
        return owner;
    }
}