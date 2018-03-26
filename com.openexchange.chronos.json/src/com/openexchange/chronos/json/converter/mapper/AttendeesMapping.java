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
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.exception.OXException;
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
    protected Attendee deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
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
        return jsonObject;
    }

    /**
     * Deserializes an attendee from the supplied json object.
     *
     * @param jsonObject The json object to parse the attendee from
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
            attendee.setRsvp(from.isNull(ChronosJsonFields.Attendee.RSVP) ? null : from.getBoolean(ChronosJsonFields.Attendee.RSVP));
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

        return attendee;
    }

}
