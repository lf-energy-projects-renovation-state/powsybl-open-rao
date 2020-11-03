/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.rao_commons;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.rao_api.json.JsonRaoParameters;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
@AutoService(JsonRaoParameters.ExtensionSerializer.class)
public class JsonRaoPtdfParameters implements JsonRaoParameters.ExtensionSerializer<RaoPtdfParameters> {

    @Override
    public void serialize(RaoPtdfParameters extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeArrayFieldStart("boundaries");
        for (String countryPair : extension.getBoundariesAsString()) {
            jsonGenerator.writeString(countryPair);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    @Override
    public RaoPtdfParameters deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        RaoPtdfParameters parameters = new RaoPtdfParameters();
        while (!jsonParser.nextToken().isStructEnd()) {
            if (jsonParser.getCurrentName().equals("boundaries")) {
                if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                    List<String> boundaries = new ArrayList<>();
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        boundaries.add(jsonParser.getValueAsString());
                    }
                    parameters.setBoundariesFromCountryCodes(boundaries);
                }
            } else {
                throw new FaraoException("Unexpected field: " + jsonParser.getCurrentName());
            }
        }
        return parameters;
    }

    @Override
    public String getExtensionName() {
        return "RaoPtdfParameters";
    }

    @Override
    public String getCategoryName() {
        return "rao-parameters";
    }

    @Override
    public Class<? super RaoPtdfParameters> getExtensionClass() {
        return RaoPtdfParameters.class;
    }
}