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

import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Bean2JSONConverter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class Bean2JSONConverter implements ResultConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getInputFormat() {
        return "bean";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.BAD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object object = result.getResultObject();
        try {
            if (object instanceof Collection) {
                JSONArray arr = new JSONArray();
                for (Object content : ((Collection<? extends Object>) object)) {
                    arr.put(new JSONObject(MAPPER.writeValueAsString(content)));
                }
                result.setResultObject(arr, "json");
                return;
            }

            result.setResultObject(new JSONObject(MAPPER.writeValueAsString(object)), "json");
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage(), x);
        } catch (JsonProcessingException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage(), x);
        }
    }
}
