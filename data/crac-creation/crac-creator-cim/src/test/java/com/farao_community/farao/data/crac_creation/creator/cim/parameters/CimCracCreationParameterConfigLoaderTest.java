/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.data.crac_creation.creator.cim.parameters;

import com.farao_community.farao.data.crac_creation.creator.api.parameters.CracCreationParameters;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public class CimCracCreationParameterConfigLoaderTest {

    @Test
    public void configLoaderTest() {
        PlatformConfig platformConfig = Mockito.mock(PlatformConfig.class);
        ModuleConfig cimCracCreatorModuleConfig = Mockito.mock(ModuleConfig.class);

        Mockito.when(cimCracCreatorModuleConfig.getStringListProperty(eq("range-action-groups"), any())).thenReturn(List.of("ra1 + ra2", "ra3 + ra4 + ra5"));
        Mockito.when(platformConfig.getOptionalModuleConfig("cim-crac-creation-parameters")).thenReturn(Optional.of(cimCracCreatorModuleConfig));

        CracCreationParameters parameters = CracCreationParameters.load(platformConfig);

        assertNotNull(parameters.getExtension(CimCracCreationParameters.class));
        assertEquals(2, parameters.getExtension(CimCracCreationParameters.class).getRangeActionGroupsAsString().size());
        assertEquals("ra1 + ra2", parameters.getExtension(CimCracCreationParameters.class).getRangeActionGroupsAsString().get(0));
        assertEquals("ra3 + ra4 + ra5", parameters.getExtension(CimCracCreationParameters.class).getRangeActionGroupsAsString().get(1));
    }
}