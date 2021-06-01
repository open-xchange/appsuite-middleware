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

package com.openexchange.jslob.json.converter;

import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JSlobJSONResultConverter} - The result converter for JSlob module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSlobJSONResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link JSONResultConverter}.
     */
    public JSlobJSONResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "jslob";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        try {
            Object resultObject = result.getResultObject();
            if (resultObject instanceof JSlob) {
                JSlob jslob = JSlob.class.cast(resultObject);
                result.setResultObject(convertJSlob(jslob), "json");
                return;
            }
            if (null == resultObject) {
                return;
            }
            /*
             * Collection of JSlobs
             */
            @SuppressWarnings("unchecked") Collection<JSlob> jslobs = (Collection<JSlob>) resultObject;
            JSONArray jArray = new JSONArray();
            for (final JSlob jslob : jslobs) {
                jArray.put(convertJSlob(jslob));
            }
            result.setResultObject(jArray, "json");
        } catch (JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertJSlob(JSlob jslob) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", jslob.getId().getId());
        json.put("tree", jslob.getJsonObject());
        json.put("meta", jslob.getMetaObject());
        return json;
    }

}
