/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.open_rao.data.cne_exporter_commons;

import com.powsybl.open_rao.data.crac_api.Crac;
import com.powsybl.open_rao.data.crac_api.Instant;
import com.powsybl.open_rao.data.rao_result_api.RaoResult;
import com.powsybl.open_rao.rao_api.parameters.RaoParameters;
import com.powsybl.open_rao.rao_api.parameters.extensions.LoopFlowParametersExtension;
import com.powsybl.open_rao.rao_api.parameters.extensions.MnecParametersExtension;
import com.powsybl.iidm.network.Network;

import static com.powsybl.open_rao.data.cne_exporter_commons.CneConstants.PATL_MEASUREMENT_TYPE;
import static com.powsybl.open_rao.data.cne_exporter_commons.CneConstants.TATL_MEASUREMENT_TYPE;

/**
 * @author Viktor Terrier {@literal <viktor.terrier at rte-france.com>}
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class CneHelper {

    private Crac crac;
    private Network network;
    private boolean relativePositiveMargins;
    private boolean withLoopflows;
    private RaoResult raoResult;
    private CneExporterParameters exporterParameters;
    private double mnecAcceptableMarginDiminution;

    public CneHelper(Crac crac, Network network, RaoResult raoResult, RaoParameters raoParameters, CneExporterParameters exporterParameters) {
        this.crac = crac;
        this.network = network;
        this.raoResult = raoResult;
        this.exporterParameters = exporterParameters;

        relativePositiveMargins = raoParameters.getObjectiveFunctionParameters().getType().relativePositiveMargins();
        withLoopflows = raoParameters.hasExtension(LoopFlowParametersExtension.class);
        mnecAcceptableMarginDiminution = raoParameters.hasExtension(MnecParametersExtension.class) ? raoParameters.getExtension(MnecParametersExtension.class).getAcceptableMarginDecrease() : 0;
    }

    public RaoResult getRaoResult() {
        return raoResult;
    }

    public boolean isWithLoopflows() {
        return withLoopflows;
    }

    public double getMnecAcceptableMarginDiminution() {
        return mnecAcceptableMarginDiminution;
    }

    public Network getNetwork() {
        return network;
    }

    public Crac getCrac() {
        return crac;
    }

    public String instantToCodeConverter(Instant instant) {
        if (instant.isPreventive()) { // Before contingency
            return PATL_MEASUREMENT_TYPE;
        } else { // After contingency, before any post-contingency RA
            return TATL_MEASUREMENT_TYPE;
        }
    }

    public CneExporterParameters getExporterParameters() {
        return exporterParameters;
    }

    public boolean isRelativePositiveMargins() {
        return relativePositiveMargins;
    }
}