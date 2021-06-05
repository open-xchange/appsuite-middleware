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

import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.groupware.tools.mappings.json.ListItemMapping;
import com.openexchange.session.Session;

/**
 *
 * {@link AttendeesMapping}>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class AttendeesMapping<O> extends ListItemMapping<Attendee, O, JSONObject> {

    /**
     * Initializes a new {@link AttendeesMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID The mapped column identifier
     */
    public AttendeesMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    protected Attendee deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException {
        JSONObject jsonObject = array.getJSONObject(index);
        return deserialize(jsonObject, timeZone);
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        List<Attendee> value = get(from);
        if (null == value) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(value.size());
        for (Attendee attendee : value) {

            jsonArray.put(serialize(attendee, timeZone));
        }
        return jsonArray;
    }

    @Override
    public Attendee deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
        return deserializeAttendee(from);
    }

    @Override
    public JSONObject serialize(Attendee from, TimeZone timeZone) throws JSONException {
        JSONObject jsonObject = EventMapper.serializeCalendarUser(from);
        if (null != from.getCuType()) {
            jsonObject.put(ChronosJsonFields.Attendee.CU_TYPE, from.getCuType().getValue());
        }
        if (null != from.getRole()) {
            jsonObject.put(ChronosJsonFields.Attendee.ROLE, from.getRole().getValue());
        }
        if (null != from.getPartStat()) {
            jsonObject.put(ChronosJsonFields.Attendee.PARTICIPATION_STATUS, from.getPartStat().getValue());
        }
        if (null != from.getComment()) {
            jsonObject.put(ChronosJsonFields.Attendee.COMMENT, from.getComment());
        }
        if (null != from.getRsvp()) {
            jsonObject.put(ChronosJsonFields.Attendee.RSVP, from.getRsvp());
        }
        if (null != from.getFolderId()) {
            jsonObject.put(ChronosJsonFields.Attendee.FOLDER, from.getFolderId());
        }
        if (null != from.getMember()) {
            jsonObject.put(ChronosJsonFields.Attendee.MEMBER, from.getMember());
        }
        if (from.containsExtendedParameters()) {
            List<ExtendedPropertyParameter> parameters = from.getExtendedParameters();
            if (null == parameters) {
                jsonObject.put(ChronosJsonFields.Attendee.EXTENDED_PARAMETERS, (JSONObject) null);
            } else {
                JSONObject o = new JSONObject(parameters.size());
                for (ExtendedPropertyParameter parameter : parameters) {
                    o.put(parameter.getName(), parameter.getValue());
                }
                jsonObject.put(ChronosJsonFields.Attendee.EXTENDED_PARAMETERS, o);
            }
        }
        return jsonObject;
    }

    /**
     * Deserializes an attendee from the supplied json object.
     *
     * @param from The json object to parse the attendee from
     * @return The parsed attendee
     */
    public static Attendee deserializeAttendee(JSONObject from) throws JSONException {
        Attendee attendee = EventMapper.deserializeCalendarUser(from, Attendee.class);
        if (from.has(ChronosJsonFields.Attendee.CU_TYPE)) {
            attendee.setCuType(from.isNull(ChronosJsonFields.Attendee.CU_TYPE) ? null : new CalendarUserType(from.getString(ChronosJsonFields.Attendee.CU_TYPE)));
        }
        if (from.has(ChronosJsonFields.Attendee.ROLE)) {
            attendee.setRole(from.isNull(ChronosJsonFields.Attendee.ROLE) ? null : new ParticipantRole(from.getString(ChronosJsonFields.Attendee.ROLE)));
        }
        if (from.has(ChronosJsonFields.Attendee.PARTICIPATION_STATUS)) {
            attendee.setPartStat(from.isNull(ChronosJsonFields.Attendee.PARTICIPATION_STATUS) ? null : new ParticipationStatus(from.getString(ChronosJsonFields.Attendee.PARTICIPATION_STATUS)));
        }
        if (from.has(ChronosJsonFields.Attendee.COMMENT)) {
            attendee.setComment(from.isNull(ChronosJsonFields.Attendee.COMMENT) ? null : from.getString(ChronosJsonFields.Attendee.COMMENT));
        }
        if (from.has(ChronosJsonFields.Attendee.RSVP)) {
            attendee.setRsvp(from.isNull(ChronosJsonFields.Attendee.RSVP) ? null : B(from.getBoolean(ChronosJsonFields.Attendee.RSVP)));
        }
        if (from.has(ChronosJsonFields.Attendee.FOLDER)) {
            attendee.setFolderId(from.isNull(ChronosJsonFields.Attendee.FOLDER) ? null : from.getString(ChronosJsonFields.Attendee.FOLDER));
        }
        if (from.has(ChronosJsonFields.Attendee.MEMBER)) {
            if (from.isNull(ChronosJsonFields.Attendee.MEMBER)) {
                attendee.setMember(null);
            } else {
                JSONArray array = from.getJSONArray(ChronosJsonFields.Attendee.MEMBER);
                List<String> list = new ArrayList<>(array.length());
                for (Object o : array.asList()) {
                    list.add(o.toString());
                }
                attendee.setMember(list);
            }
        }
        if (from.has(ChronosJsonFields.Attendee.EXTENDED_PARAMETERS)) {
            JSONObject jsonObject = from.getJSONObject(ChronosJsonFields.Attendee.EXTENDED_PARAMETERS);
            if (null == jsonObject) {
                attendee.setExtendedParameters(null);
            } else {
                List<ExtendedPropertyParameter> extendedParameters = new ArrayList<ExtendedPropertyParameter>(jsonObject.length());
                for (Entry<String, Object> entry : jsonObject.entrySet()) {
                    extendedParameters.add(new ExtendedPropertyParameter(entry.getKey(), String.valueOf(entry.getValue())));
                }
                attendee.setExtendedParameters(extendedParameters);
            }
        }
        return attendee;
    }

}
