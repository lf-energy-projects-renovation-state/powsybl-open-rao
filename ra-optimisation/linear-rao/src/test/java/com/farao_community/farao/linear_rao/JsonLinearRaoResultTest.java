/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.linear_rao;

import com.farao_community.farao.ra_optimisation.RaoComputationResult;
import com.farao_community.farao.ra_optimisation.json.JsonRaoComputationResult;
import com.powsybl.commons.AbstractConverterTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public class JsonLinearRaoResultTest extends AbstractConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLinearRaoResultTest.class);

    @Test
    public void roundTripTest() {
        // read
        RaoComputationResult result = JsonRaoComputationResult.read(getClass().getResourceAsStream("/LinearRaoResults.json"));
        assertNotNull(result.getExtension(LinearRaoResult.class));

        // write
        OutputStream baos = new ByteArrayOutputStream();
        JsonRaoComputationResult.write(result, baos);

        //compare
        compareTxt(getClass().getResourceAsStream("/LinearRaoResults.json"), baos.toString());
    }

    @Test
    public void anotherRoundTest() {
        RaoComputationResult outputResult = new RaoComputationResult(RaoComputationResult.Status.SUCCESS, null, null);
        LinearRaoResult outputResultExtension = new LinearRaoResult(LinearRaoResult.SecurityStatus.UNSECURED);
        double minMargin = -1;
        outputResultExtension.setMinMargin(minMargin);
        outputResult.addExtension(LinearRaoResult.class, outputResultExtension);

        OutputStream outputStream = new ByteArrayOutputStream();
        JsonRaoComputationResult.write(outputResult, outputStream);
        assertTrue(outputStream.toString().contains("minimum-margin"));

        InputStream inputStream = new ByteArrayInputStream(outputStream.toString().getBytes());
        RaoComputationResult inputResult = JsonRaoComputationResult.read(inputStream);
        LinearRaoResult inputExtension = inputResult.getExtension(LinearRaoResult.class);
        assertEquals(RaoComputationResult.Status.SUCCESS, inputResult.getStatus());
        assertEquals(LinearRaoResult.SecurityStatus.UNSECURED, inputExtension.getSecurityStatus());
        assertEquals(minMargin, inputExtension.getMinMargin(), 1E-10);
    }
}