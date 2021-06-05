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

package com.openexchange.api.client.common.parser;

import static com.openexchange.java.Autoboxing.L;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.common.JSONUtils;
import com.openexchange.api.client.common.OXExceptionParser;
import com.openexchange.exception.OXException;

/**
 * {@link CommonApiResponse} - An object representing the common response from an OX server.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 * @see <a href="https://documentation.open-xchange.com/components/middleware/http/latest/index.html">Documentation</a>
 */
public class CommonApiResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonApiResponse.class);

    private Object data;
    private OXException oxException;
    private Long timestamp;

    /**
     * Initializes a new {@link CommonApiResponse}.
     */
    private CommonApiResponse() {
        super();
    }

    /**
     * Gets the data
     *
     * @return The data
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data
     *
     * @param data The data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Gets a values indicating whether the <code>data</code> field is set or not
     *
     * @return <code>true</code> if data is set, <code>false</code> otherwise
     */
    public boolean hasData() {
        return null != data;
    }

    /**
     * Gets the oxException
     *
     * @return The oxException
     */
    public OXException getOXException() {
        return oxException;
    }

    /**
     * Sets the oxException
     *
     * @param oxException The oxException to set
     */
    public void setOXException(OXException oxException) {
        this.oxException = oxException;
    }

    /**
     * Gets a values indicating whether a {@link OXException} is set or not
     *
     * @return <code>true</code> if an {@link OXException} is set, <code>false</code> otherwise
     */
    public boolean hasOXException() {
        return null != oxException;
    }

    /**
     * Gets the timestamp
     *
     * @return The timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     *
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets a values indicating whether the <code>timestamp</code> field is set or not
     *
     * @return <code>true</code> if the time stamp is set, <code>false</code> otherwise
     */
    public boolean hasTimestamp() {
        return null != timestamp;
    }

    /*
     * ----------------- ADDITIONAL UTILITIES -----------------
     */

    /**
     * Gets the data
     * 
     * @param clazz The class to cast to
     * @param <T> The type of the object to cast to, defined by the class parameter
     * @return The data hold by this object casted to the desired class or <code>null</code>
     */
    public <T> T getData(Class<T> clazz) {
        if (null != data && null != clazz && clazz.isAssignableFrom(data.getClass())) {
            return clazz.cast(data);
        }
        LOGGER.trace("Can't cast data {} of type {} to desired class {}", data, null == data ? null : data.getClass(), clazz);
        return null;
    }

    /**
     * Gets a value indicating whether the data is a {@link JSONObject} or not
     *
     * @return <code>true</code> if the data is a {@link JSONObject}
     */
    public boolean isJSONObject() {
        return hasData() && null != getData(JSONObject.class);
    }

    /**
     * Get the data as a {@link JSONObject}
     *
     * @return The {@link JSONObject}
     * @throws OXException if the data can't be parsed to a {@link JSONObject}
     */
    public JSONObject getJSONObject() throws OXException {
        JSONObject jsonObject = getData(JSONObject.class);
        if (null == jsonObject) {
            throw ApiClientExceptions.JSON_ERROR.create("Not an JSON object");
        }
        return jsonObject;
    }

    /**
     * Gets a value indicating whether the data is a {@link JSONArray} or not
     *
     * @return <code>true</code> if the data is a {@link JSONArray}
     */
    public boolean isJSONArray() {
        return hasData() && null != getData(JSONArray.class);
    }

    /**
     * Get the data as a {@link JSONArray}
     *
     * @return The {@link JSONArray}
     * @throws OXException if the data can't be parsed to a {@link JSONArray}
     */
    public JSONArray getJSONArray() throws OXException {
        JSONArray jsonArray = getData(JSONArray.class);
        if (null == jsonArray) {
            throw ApiClientExceptions.JSON_ERROR.create("Not an JSON array");
        }
        return jsonArray;
    }

    /**
     * Builds a {@link CommonApiResponse} based on the given response
     *
     * @param response The HTTP response
     * @return A {@link CommonApiResponse}
     */
    public static CommonApiResponse build(HttpResponse response) {
        JSONValue json = JSONUtils.getJSON(response);
        CommonApiResponse resp = new CommonApiResponse();

        /*
         * This parser is designed for parsing the common response, JSON must be set
         */
        if (null == json) {
            throw new IllegalStateException("Unable to parse common OX response. JSON is missing in reponse");
        }

        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            /*
             * Extract OXException as whole object instead of multiple fields
             */
            try {
                resp.oxException = OXExceptionParser.parseException(jsonObject);
            } catch (JSONException e) {
                LOGGER.debug("Unable to parse JSON", e);
            }

            /*
             * If "data" field is set, extract value, else use this JSON as response value
             */
            Object data = jsonObject.opt(ResponseFields.DATA);
            if (null != data) {
                resp.data = data;
            } else {
                resp.data = json;
            }

            /*
             * Extract time stamp
             */
            long timestamp = jsonObject.optLong(ResponseFields.TIMESTAMP);
            if (timestamp > 0) {
                resp.timestamp = L(timestamp);
            }
        } else {
            resp.data = json;
        }
        return resp;
    }

}
