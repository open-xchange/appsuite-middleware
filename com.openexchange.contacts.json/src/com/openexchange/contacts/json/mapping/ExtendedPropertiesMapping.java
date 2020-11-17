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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contacts.json.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.contact.common.ExtendedProperties;
import com.openexchange.contact.common.ExtendedProperty;
import com.openexchange.contact.common.ExtendedPropertyParameter;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link ExtendedPropertiesMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public abstract class ExtendedPropertiesMapping<O> extends DefaultJsonMapping<ExtendedProperties, O> {

    private static final String VALUE = "value";

    /**
     * Initializes a new {@link ExtendedPropertiesMapping}.
     * 
     * @param ajaxName
     * @param columnID
     */
    public ExtendedPropertiesMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    /**
     * Serialises the specified {@link ExtendedProperties} to a {@link JSONObject}
     *
     * @param extendedProperties The {@link ExtendedProperties} to serialise
     * @return The serialised {@link JSONException}
     * @throws JSONException if a JSON error is occurred
     */
    public static JSONObject serializeExtendedProperties(ExtendedProperties extendedProperties) throws JSONException {
        if (null == extendedProperties) {
            return null;
        }
        Map<String, List<ExtendedProperty>> propertiesByName = new HashMap<>(extendedProperties.size());
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

    /**
     * Serialises the specified value
     *
     * @param value The value to serialise
     * @param parameters The property's parameters
     * @return the serialised {@link JSONObject}
     * @throws JSONException if a JSON error is occurred
     */
    private static JSONObject serializeExtendedProperty(Object value, List<ExtendedPropertyParameter> parameters) throws JSONException {

        if (null == parameters || parameters.isEmpty()) {
            return new JSONObject(1).put(VALUE, value);
        }
        JSONObject jsonObject = new JSONObject(1 + parameters.size());
        jsonObject.put(VALUE, value);
        for (ExtendedPropertyParameter parameter : parameters) {
            jsonObject.put(parameter.getName(), parameter.getValue());
        }
        return jsonObject;
    }

    /**
     * Deserialises the specified {@link JSONObject} to an {@link ExtendedProperties} object
     *
     * @param jsonObject the {@link JSONObject} to deserialise
     * @return The deserialised {@link ExtendedProperties} object
     * @throws JSONException if a JSON error is occurred
     */
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
                throw new JSONException("Unsupported property value: " + entry.getValue());
            }
        }
        return extendedProperties;
    }

    /**
     * Deserialises the specified {@link JSONObject} to an {@link ExtendedProperty}
     *
     * @param name The property's name
     * @param jsonObject The object to deserialise
     * @return the deserialised {@link ExtendedPropertyt}
     * @throws JSONException if a JSON error is occurred
     */
    private static ExtendedProperty deserializeExtendedProperty(String name, JSONObject jsonObject) throws JSONException {
        if (null == jsonObject || 0 == jsonObject.length()) {
            return new ExtendedProperty(name, null);
        }
        if (1 == jsonObject.length() && jsonObject.has(VALUE)) {
            return new ExtendedProperty(name, jsonObject.get(VALUE));
        }
        List<ExtendedPropertyParameter> parameters = new ArrayList<>(jsonObject.length());
        Object value = null;
        for (Entry<String, Object> entry : jsonObject.entrySet()) {
            if (VALUE.equals(entry.getKey())) {
                value = entry.getValue();
            } else {
                parameters.add(new ExtendedPropertyParameter(entry.getKey(), (String) entry.getValue()));
            }
        }
        return new ExtendedProperty(name, value, parameters);
    }

    /**
     * Deserialises the specified extended properties from the {@link JSONArray}
     *
     * @param name The name of the property
     * @param jsonArray the {@link JSONArray} that contains the properties
     * @return A {@link List} with all deserialised properties
     * @throws JSONException if a JSON error is occurred
     */
    private static List<ExtendedProperty> deserializeExtendedProperties(String name, JSONArray jsonArray) throws JSONException {
        if (null == jsonArray || 0 == jsonArray.length()) {
            return java.util.Collections.emptyList();
        }
        List<ExtendedProperty> extendedProperties = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            extendedProperties.add(deserializeExtendedProperty(name, jsonArray.getJSONObject(i)));
        }
        return extendedProperties;
    }

}
