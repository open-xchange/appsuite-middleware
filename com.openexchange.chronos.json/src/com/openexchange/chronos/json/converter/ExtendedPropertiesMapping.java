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

package com.openexchange.chronos.json.converter;

import java.util.ArrayList;
import java.util.List;
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
            set(to, deserializeExtendedProperties(from.getJSONArray(getAjaxName())));
        }
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        return serializeExtendedProperties(get(from));
    }

    private static ExtendedProperties deserializeExtendedProperties(JSONArray jsonArray) throws JSONException {
        if (null == jsonArray) {
            return null;
        }
        ExtendedProperties extendedProperties = new ExtendedProperties();
        for (int x = 0; x < jsonArray.length(); x++) {
            extendedProperties.add(deserializeExtendedProperty(jsonArray.getJSONObject(x)));
        }
        return extendedProperties;
    }

    private static JSONArray serializeExtendedProperties(ExtendedProperties extendedProperties) throws JSONException {
        if (null == extendedProperties) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(extendedProperties.size());
        for (ExtendedProperty extendedProperty : extendedProperties) {
            jsonArray.put(serializeExtendedProperty(extendedProperty));
        }
        return jsonArray;
    }

    private static JSONObject serializeExtendedProperty(ExtendedProperty extendedProperty) throws JSONException {
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

    private static ExtendedProperty deserializeExtendedProperty(JSONObject extendedProperty) throws JSONException {
        String name = extendedProperty.getString("name");
        String value = extendedProperty.getString("value");
        JSONArray jsonParameters = extendedProperty.optJSONArray("parameters");
        if (null == jsonParameters) {
            return new ExtendedProperty(name, value);
        }
        List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>(jsonParameters.length());
        for (int i = 0; i < jsonParameters.length(); i++) {
            JSONObject jsonParameter = jsonParameters.getJSONObject(i);
            parameters.add(new ExtendedPropertyParameter(jsonParameter.optString("name", null), jsonParameter.optString("value", null)));
        }
        return new ExtendedProperty(name, value, parameters);
    }

}
