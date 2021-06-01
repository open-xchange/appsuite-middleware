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

package com.openexchange.chronos.json.converter.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Collections;

/**
 *
 * {@link ExtendedPropertiesMapping}>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ExtendedPropertiesMapping<O> extends DefaultJsonMapping<ExtendedProperties, O> {

    /**
     * Initializes a new {@link ExtendedPropertiesMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID The mapped column identifier
     */
    public ExtendedPropertiesMapping(String ajaxName, Integer columnID) {
		super(ajaxName, columnID);
	}

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        if (from.has(getAjaxName())) {
            set(to, deserializeExtendedProperties(from.getJSONObject(getAjaxName())));
        }
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        return serializeExtendedProperties(get(from));
    }

    public static ExtendedProperties deserializeExtendedProperties(JSONObject jsonObject) throws JSONException {
        if (null == jsonObject) {
            return null;
        }
        ExtendedProperties extendedProperties = new ExtendedProperties();
        for (Entry<String, Object> entry : jsonObject.entrySet()) {
            String name = entry.getKey();
            if (JSONObject.class.isInstance(entry.getValue())) {
                extendedProperties.add(deserializeExtendedProperty(name, (JSONObject) entry.getValue()));
            } else if (JSONArray.class.isInstance(entry.getValue())) {
                extendedProperties.addAll(deserializeExtendedProperties(name, (JSONArray) entry.getValue()));
            } else {
                throw new JSONException("unsupported property value: " + entry.getValue());
            }
        }
        return extendedProperties;
    }

    public static JSONObject serializeExtendedProperties(ExtendedProperties extendedProperties) throws JSONException {
        if (null == extendedProperties) {
            return null;
        }
        Map<String, List<ExtendedProperty>> propertiesByName = new HashMap<String, List<ExtendedProperty>>(extendedProperties.size());
        for (ExtendedProperty extendedProperty : extendedProperties) {
            Collections.put(propertiesByName, extendedProperty.getName(), extendedProperty);
        }
        JSONObject jsonObject = new JSONObject(extendedProperties.size());
        for (Entry<String, List<ExtendedProperty>> entry : propertiesByName.entrySet()) {
            List<ExtendedProperty> properties = entry.getValue();
            String name = entry.getKey();
            if (1 == properties.size()) {
                jsonObject.put(name, serializeExtendedProperty(properties.get(0).getValue(), properties.get(0).getParameters()));
            } else {
                JSONArray jsonArray = new JSONArray(properties.size());
                for (ExtendedProperty property : properties) {
                    jsonArray.put(serializeExtendedProperty(property.getValue(), property.getParameters()));
                }
                jsonObject.put(name, jsonArray);
            }
        }
        return jsonObject;
    }

    private static ExtendedProperty deserializeExtendedProperty(String name, JSONObject jsonObject) throws JSONException {
        if (null == jsonObject || 0 == jsonObject.length()) {
            return new ExtendedProperty(name, null);
        }
        if (1 == jsonObject.length() && jsonObject.has("value")) {
            return new ExtendedProperty(name, jsonObject.get("value"));
        }
        List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>(jsonObject.length());
        Object value = null;
        for (Entry<String, Object> entry : jsonObject.entrySet()) {
            if ("value".equals(entry.getKey())) {
                value = entry.getValue();
            } else {
                parameters.add(new ExtendedPropertyParameter(entry.getKey(), (String) entry.getValue()));
            }
        }
        return new ExtendedProperty(name, value, parameters);
    }

    private static List<ExtendedProperty> deserializeExtendedProperties(String name, JSONArray jsonArray) throws JSONException {
        if (null == jsonArray || 0 == jsonArray.length()) {
            return java.util.Collections.emptyList();
        }
        List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            extendedProperties.add(deserializeExtendedProperty(name, jsonArray.getJSONObject(i)));
        }
        return extendedProperties;
    }

    private static JSONObject serializeExtendedProperty(Object value, List<ExtendedPropertyParameter> parameters) throws JSONException {
        if (null == parameters || parameters.isEmpty()) {
            return new JSONObject(1).put("value", value);
        }
        JSONObject jsonObject = new JSONObject(1 + parameters.size());
        jsonObject.put("value", value);
        for (ExtendedPropertyParameter parameter : parameters) {
            jsonObject.put(parameter.getName(), parameter.getValue());
        }
        return jsonObject;
    }

}
