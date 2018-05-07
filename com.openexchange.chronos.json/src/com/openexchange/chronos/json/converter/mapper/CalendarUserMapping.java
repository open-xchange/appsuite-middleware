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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Factory;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.session.Session;

/**
 *
 * {@link CalendarUserMapping}>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class CalendarUserMapping<T extends CalendarUser, O> extends DefaultJsonMapping<T, O> implements Factory<T> {

    /**
     * Initializes a new {@link CalendarUserMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID The mapped column identifier
     */
    public CalendarUserMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        JSONObject jsonObject = from.optJSONObject(getAjaxName());
        set(to, null == jsonObject ? null : deserialize(jsonObject, newInstance()));
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        T calendarUser = get(from);
        return null == calendarUser ? null : serialize(calendarUser);
    }

    private static <T extends CalendarUser> T deserialize(JSONObject jsonObject, T calendarUser) {
        if (jsonObject.has(ChronosJsonFields.CalendarUser.URI)) {
            calendarUser.setUri(jsonObject.optString(ChronosJsonFields.CalendarUser.URI, null));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.CN)) {
            calendarUser.setCn(jsonObject.optString(ChronosJsonFields.CalendarUser.CN, null));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.EMAIL)) {
            calendarUser.setEMail(jsonObject.optString(ChronosJsonFields.CalendarUser.EMAIL, null));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.SENT_BY)) {
            calendarUser.setSentBy(deserialize(jsonObject.optJSONObject(ChronosJsonFields.CalendarUser.SENT_BY), new CalendarUser()));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.ENTITY)) {
            calendarUser.setEntity(jsonObject.optInt(ChronosJsonFields.CalendarUser.ENTITY, 0));
        }
        return calendarUser;
    }

    private static <T extends CalendarUser> JSONObject serialize(T calendarUser) throws JSONException {
        if (null == calendarUser) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.URI, calendarUser.getUri());
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.CN, calendarUser.getCn());
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.EMAIL, calendarUser.getEMail());
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.SENT_BY, serialize(calendarUser.getSentBy()));
        if (0 < calendarUser.getEntity()) {
            jsonObject.put(ChronosJsonFields.CalendarUser.ENTITY, calendarUser.getEntity());
        }
        return jsonObject;
    }

}
