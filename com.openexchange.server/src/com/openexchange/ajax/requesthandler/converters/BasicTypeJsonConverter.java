/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.requesthandler.converters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicTypeJsonConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BasicTypeJsonConverter implements ResultConverter {

    private static final String JSON = "json";

    /**
     * The basic converters.
     */
    public static final List<ResultConverter> CONVERTERS = Collections.<ResultConverter> unmodifiableList(Arrays.<ResultConverter> asList(
        new BasicTypeJsonConverter("string"),
        new BasicTypeJsonConverter("int"),
        new BasicTypeJsonConverter("float"),
        new BasicTypeJsonConverter("boolean")));

    private final String inputFormat;

    /**
     * Initializes a new {@link BasicTypeJsonConverter}.
     *
     * @param inputFormat The input format
     */
    protected BasicTypeJsonConverter(final String inputFormat) {
        this.inputFormat = inputFormat;
    }

    @Override
    public String getInputFormat() {
        return inputFormat;
    }

    @Override
    public String getOutputFormat() {
        return JSON;
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Collection) {
            result.setResultObject(new JSONArray((Collection<?>) resultObject), JSON);
        } else if (resultObject instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, ? extends Object> map = (Map<String, ? extends Object>) resultObject;
            result.setResultObject(new JSONObject(map), JSON);
        } else if (resultObject instanceof String) {
            result.setResultObject(resultObject, JSON);
        } else if (resultObject instanceof Number) {
            result.setResultObject(resultObject, JSON);
        } else {
            result.setResultObject(asJSObject(resultObject.toString()), JSON);
        }
    }

    private static Object asJSObject(final String propertyValue) {
        if (null == propertyValue) {
            return null;
        }
        try {
            return new JSONTokener(propertyValue).nextValue();
        } catch (Exception e) {
            return propertyValue;
        }
    }

}
