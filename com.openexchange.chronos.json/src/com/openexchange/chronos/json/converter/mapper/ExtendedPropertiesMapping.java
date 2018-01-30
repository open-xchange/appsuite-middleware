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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
