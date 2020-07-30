/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.search_tree_rao;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static com.farao_community.farao.search_tree_rao.SearchTreeRaoParameters.StopCriterion.MAXIMUM_MARGIN;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SearchTreeRaoParametersConfigLoaderTest {

    private static final double DOUBLE_TOLERANCE = 0.1;

    private PlatformConfig platformConfig;
    private SearchTreeRaoParametersConfigLoader configLoader;

    @Before
    public void setUp() {
        platformConfig = Mockito.mock(PlatformConfig.class);
        configLoader = new SearchTreeRaoParametersConfigLoader();
    }

    @Test
    public void testLoad() {
        ModuleConfig searchTreeRaoParametersModule = Mockito.mock(ModuleConfig.class);
        Mockito.when(searchTreeRaoParametersModule.getEnumProperty(eq("stop-criterion"), eq(SearchTreeRaoParameters.StopCriterion.class), any())).thenReturn(MAXIMUM_MARGIN);
        Mockito.when(searchTreeRaoParametersModule.getIntProperty(eq("maximum-search-depth"), anyInt())).thenReturn(2);
        Mockito.when(searchTreeRaoParametersModule.getDoubleProperty(eq("relative-network-action-minimum-impact-threshold"), anyDouble())).thenReturn(0.1);
        Mockito.when(searchTreeRaoParametersModule.getDoubleProperty(eq("absolute-network-action-minimum-impact-threshold"), anyDouble())).thenReturn(20.0);
        Mockito.when(searchTreeRaoParametersModule.getIntProperty(eq("leaves-in-parallel"), anyInt())).thenReturn(4);

        Mockito.when(platformConfig.getOptionalModuleConfig("search-tree-rao-parameters")).thenReturn(Optional.of(searchTreeRaoParametersModule));

        SearchTreeRaoParameters parameters = configLoader.load(platformConfig);
        assertEquals(MAXIMUM_MARGIN, parameters.getStopCriterion());
        assertEquals(2, parameters.getMaximumSearchDepth());
        assertEquals(0.1, parameters.getRelativeNetworkActionMinimumImpactThreshold(), DOUBLE_TOLERANCE);
        assertEquals(20.0, parameters.getAbsoluteNetworkActionMinimumImpactThreshold(), DOUBLE_TOLERANCE);
        assertEquals(4, parameters.getLeavesInParallel());
    }

    @Test
    public void getExtensionName() {
        assertEquals("SearchTreeRaoParameters", configLoader.getExtensionName());
    }

    @Test
    public void getCategoryName() {
        assertEquals("rao-parameters", configLoader.getCategoryName());
    }

    @Test
    public void getExtensionClass() {
        assertEquals(SearchTreeRaoParameters.class, configLoader.getExtensionClass());
    }
}