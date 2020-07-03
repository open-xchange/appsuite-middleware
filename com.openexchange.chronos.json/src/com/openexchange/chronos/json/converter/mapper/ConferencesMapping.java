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
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.session.Session;

/**
 * {@link ConferencesMapping}>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public abstract class ConferencesMapping<O> extends ListItemMapping<Conference, O, JSONObject> {

    /**
     * Initializes a new {@link ConferencesMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID The mapped column identifier
     */
    public ConferencesMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    protected Conference deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException {
        JSONObject jsonObject = array.getJSONObject(index);
        return deserialize(jsonObject, timeZone);
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        List<Conference> value = get(from);
        if (null == value) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(value.size());
        for (Conference conference : value) {
            jsonArray.put(serialize(conference, timeZone));
        }
        return jsonArray;
    }

    @Override
    public Conference deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
        return deserializeConference(from);
    }

    @Override
    public JSONObject serialize(Conference from, TimeZone timeZone) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (from.containsId()) {
            jsonObject.put(ChronosJsonFields.Conference.ID, from.getId());
        }
        if (from.containsUri()) {
            jsonObject.put(ChronosJsonFields.Conference.URI, from.getUri());
        }
        if (from.containsLabel()) {
            jsonObject.put(ChronosJsonFields.Conference.LABEL, from.getLabel());
        }
        if (from.containsFeatures()) {
            List<String> features = from.getFeatures();
            if (null == features) {
                jsonObject.put(ChronosJsonFields.Conference.FEATURES, (JSONArray) null);
            } else {
                JSONArray jsonArray = new JSONArray(features.size());
                for (int i = 0; i < features.size(); i++) {
                    jsonArray.put(i, features.get(i));
                }
                jsonObject.put(ChronosJsonFields.Conference.FEATURES, jsonArray);
            }
        }
        if (from.containsExtendedParameters()) {
            List<ExtendedPropertyParameter> parameters = from.getExtendedParameters();
            if (null == parameters) {
                jsonObject.put(ChronosJsonFields.Conference.EXTENDED_PARAMETERS, (JSONObject) null);
            } else {
                JSONObject o = new JSONObject(parameters.size());
                for (ExtendedPropertyParameter parameter : parameters) {
                    o.put(parameter.getName(), parameter.getValue());
                }
                jsonObject.put(ChronosJsonFields.Conference.EXTENDED_PARAMETERS, o);
            }
        }
        return jsonObject;
    }

    /**
     * Deserializes a conference from the supplied json object.
     *
     * @param from The JSON object to parse the conference from
     * @return The parsed conference
     */
    public static Conference deserializeConference(JSONObject from) throws JSONException {
        if (null == from) {
            return null;
        }
        Conference conference = new Conference();
        if (from.has(ChronosJsonFields.Conference.ID)) {
            conference.setId(from.optInt(ChronosJsonFields.Conference.ID, 0));
        }
        if (from.has(ChronosJsonFields.Conference.URI)) {
            conference.setUri(from.optString(ChronosJsonFields.Conference.URI, null));
        }
        if (from.has(ChronosJsonFields.Conference.LABEL)) {
            conference.setLabel(from.optString(ChronosJsonFields.Conference.LABEL, null));
        }
        if (from.has(ChronosJsonFields.Conference.FEATURES)) {
            JSONArray jsonArray = from.optJSONArray(ChronosJsonFields.Conference.FEATURES);
            if (null == jsonArray) {
                conference.setFeatures(null);
            } else {
                List<String> features = new ArrayList<String>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    features.add(jsonArray.getString(i));
                }
                conference.setFeatures(features);
            }
        }
        if (from.has(ChronosJsonFields.Conference.EXTENDED_PARAMETERS)) {
            JSONObject jsonObject = from.getJSONObject(ChronosJsonFields.Conference.EXTENDED_PARAMETERS);
            if (null == jsonObject) {
                conference.setExtendedParameters(null);
            } else {
                List<ExtendedPropertyParameter> extendedParameters = new ArrayList<ExtendedPropertyParameter>(jsonObject.length());
                for (Entry<String, Object> entry : jsonObject.entrySet()) {
                    extendedParameters.add(new ExtendedPropertyParameter(entry.getKey(), String.valueOf(entry.getValue())));
                }
                conference.setExtendedParameters(extendedParameters);
            }
        }
        return conference;
    }

}
