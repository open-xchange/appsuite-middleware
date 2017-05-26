/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.storage.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.CombinedInputStream;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExtendedPropertiesCodec {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtendedPropertiesCodec.class);

    private static final byte TYPE_JSON_DEFLATE = 5;

    public static InputStream encode(ExtendedProperties extendedProperties) throws IOException {
        return encode(extendedProperties, TYPE_JSON_DEFLATE);
    }

    private static InputStream encode(ExtendedProperties extendedProperties, byte type) throws IOException {
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return null;
        }
        InputStream inputStream;
        switch (type) {
            case TYPE_JSON_DEFLATE:
                inputStream = encodeDeflatedJson(extendedProperties);
                break;
            default:
                throw new IOException(new UnsupportedEncodingException(String.valueOf(type)));
        }
        return new CombinedInputStream(new byte[] { type }, inputStream);
    }

    private static InputStream encodeDeflatedJson(ExtendedProperties extendedProperties) throws IOException {
        try {
            return new DeflaterInputStream(new JSONInputStream(encodeJsonProperties(extendedProperties), "US-ASCII"));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static JSONArray encodeJsonProperties(ExtendedProperties extendedProperties) throws JSONException {
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return null;
        }
        JSONArray jsonExtendedProperties = new JSONArray(extendedProperties.size());
        for (int i = 0; i < extendedProperties.size(); i++) {
            jsonExtendedProperties.add(i, encodeJsonProperty(extendedProperties.get(i)));
        }
        return jsonExtendedProperties;
    }

    private static JSONObject encodeJsonProperty(ExtendedProperty extendedProperty) throws JSONException {
        JSONObject jsonExtendedProperty = new JSONObject();
        jsonExtendedProperty.put("name", extendedProperty.getName());
        jsonExtendedProperty.put("value", extendedProperty.getValue());
        List<ExtendedPropertyParameter> parameters = extendedProperty.getParameters();
        if (null == parameters || parameters.isEmpty()) {
            return jsonExtendedProperty;
        }
        JSONArray jsonParameters = new JSONArray(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            ExtendedPropertyParameter parameter = parameters.get(i);
            jsonParameters.add(i, new JSONObject().putOpt("name", parameter.getName()).putOpt("value", parameter.getValue()));
        }
        jsonExtendedProperty.put("parameters", jsonParameters);
        return jsonExtendedProperty;
    }

    public static ExtendedProperties decode(InputStream inputStream) throws IOException {
        if (null == inputStream) {
            return null;
        }
        int type = inputStream.read();
        if (-1 == type) {
            return null; // eol
        }
        switch (type) {
            case TYPE_JSON_DEFLATE:
                return decodeDeflatedJson(inputStream);
            default:
                throw new IOException(new UnsupportedEncodingException(String.valueOf(type)));
        }
    }

    private static ExtendedProperties decodeDeflatedJson(InputStream inputStream) throws IOException {
        try (InflaterInputStream inflaterStream = new InflaterInputStream(inputStream); AsciiReader reader = new AsciiReader(inflaterStream)) {
            return decodeJsonProperties(new JSONArray(reader));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static ExtendedProperties decodeJsonProperties(JSONArray jsonExtendedProperties) throws JSONException {
        if (null == jsonExtendedProperties || jsonExtendedProperties.isEmpty()) {
            return null;
        }
        List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>(jsonExtendedProperties.length());
        for (int i = 0; i < jsonExtendedProperties.length(); i++) {
            extendedProperties.add(decodeJsonProperty(jsonExtendedProperties.getJSONObject(i)));
        }
        return extendedProperties.isEmpty() ? null : new ExtendedProperties(extendedProperties);
    }

    private static ExtendedProperty decodeJsonProperty(JSONObject jsonExtendedProperty) throws JSONException {
        String name = jsonExtendedProperty.optString("name", null);
        String value = jsonExtendedProperty.optString("value", null);
        JSONArray jsonParameters = jsonExtendedProperty.optJSONArray("parameters");
        if (null == jsonParameters || jsonParameters.isEmpty()) {
            return new ExtendedProperty(name, value);
        }
        List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>(jsonParameters.length());
        for (int i = 0; i < jsonParameters.length(); i++) {
            JSONObject jsonParameter = jsonParameters.getJSONObject(i);
            parameters.add(new ExtendedPropertyParameter(jsonParameter.optString("name", null), jsonParameter.optString("value", null)));
        }
        return new ExtendedProperty(name, value, parameters);
    }

    /**
     * Deserializes a an arbitrary map (as used in an account's configuration field) from the supplied input stream.
     *
     * @param data The input stream to deserialize
     * @return The deserialized map
     */
    public static ExtendedProperties decode(String data) throws SQLException {
        if (null == data) {
            return null;
        }

        List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String name = jsonObject.getString("name");
                String value = jsonObject.getString("value");

                JSONArray paramtersArray = jsonObject.optJSONArray("parameters");

                if (null == paramtersArray || paramtersArray.isEmpty()) {
                    extendedProperties.add(new ExtendedProperty(name, value));
                } else {
                    List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>();
                    for (int j = 0; j < paramtersArray.length(); j++) {
                        JSONObject jsonParameter = paramtersArray.getJSONObject(i);
                        parameters.add(new ExtendedPropertyParameter(jsonParameter.getString("name"), jsonParameter.getString("value")));
                    }
                }

            }
        } catch (JSONException e) {
            throw new SQLException(e);
        }

        return new ExtendedProperties(extendedProperties);
    }

    /**
     * Serializes an arbitrary meta map (as used in an account's configuration field) to an input stream.
     *
     * @param extendedProperties The map to serialize, or <code>null</code>
     * @return The serialized map data, or <code>null</code> if the map is empty
     */
    public static String serializeExtendedProperties(ExtendedProperties extendedProperties) throws SQLException {
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return null;
        }
        try {
            JSONArray jsonArray = new JSONArray(extendedProperties.size());
            for (ExtendedProperty extendedProperty : extendedProperties) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", extendedProperty.getName());
                jsonObject.put("value", extendedProperty.getValue());
                List<ExtendedPropertyParameter> parameters = extendedProperty.getParameters();
                if (null != parameters && 0 < parameters.size()) {
                    JSONArray parametersArray = new JSONArray(parameters.size());
                    for (ExtendedPropertyParameter entry : parameters) {
                        JSONObject jsonParamter = new JSONObject();
                        jsonParamter.put("name", entry.getName());
                        jsonParamter.put("value", entry.getValue());
                        parametersArray.put(jsonParamter);
                    }
                    jsonObject.put("paramters", parametersArray);
                }
                jsonArray.put(jsonObject);
            }
            return jsonArray.toString();
        } catch (JSONException e) {
            throw new SQLException(e);
        }
    }


}
