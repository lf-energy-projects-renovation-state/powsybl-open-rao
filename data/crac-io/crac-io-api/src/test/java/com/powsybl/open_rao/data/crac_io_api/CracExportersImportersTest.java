/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.open_rao.data.crac_io_api;

import com.powsybl.open_rao.data.crac_api.Crac;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Viktor Terrier {@literal <viktor.terrier at rte-france.com>}
 */
class CracExportersImportersTest {

    private String cracFile = "/empty.txt";

    @Test
    void testExport() {
        Crac crac = Mockito.mock(Crac.class);
        Path path = Paths.get(new File(getClass().getResource(cracFile).getFile()).getAbsolutePath());
        assertThrows(NotImplementedException.class, () -> CracExporters.exportCrac(crac, "Mock", path));
    }

    @Test
    void testImport() {
        Crac crac = CracImporters.importCrac(Paths.get(new File(getClass().getResource(cracFile).getFile()).getAbsolutePath()));
        assertNotNull(crac);
    }

    @Test
    void testImportWithInstant() {
        Crac crac = CracImporters.importCrac(Paths.get(new File(getClass().getResource(cracFile).getFile()).getAbsolutePath()));
        assertNotNull(crac);
    }
}