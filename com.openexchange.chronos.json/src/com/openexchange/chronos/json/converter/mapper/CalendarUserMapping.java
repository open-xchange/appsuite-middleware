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
