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
import org.dmfs.rfc5545.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 *
 * {@link DateTimeMapping} - JSON specific mapping implementation for DateTimes.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 * @param <O> the type of the object
 */
public abstract class DateTimeMapping<O> extends DefaultJsonMapping<DateTime, O> {

    private static final String TIME_ZONE = "tzid";
    private static final String VALUE = "value";

    public DateTimeMapping(final String ajaxName, final Integer columnID) {
		super(ajaxName, columnID);
	}

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        JSONObject dateTimeJSON = from.getJSONObject(getAjaxName());
        String value = dateTimeJSON.getString(VALUE);
        String tz = null;
        if (dateTimeJSON.has(TIME_ZONE)) {
            tz = dateTimeJSON.getString(TIME_ZONE);
        }
        this.set(to, from.isNull(getAjaxName()) ? null : parse(tz, value));
    }

    @Override
    public void deserialize(JSONObject from, O to, TimeZone timeZone) throws JSONException, OXException {
        deserialize(from, to);
    }

	@Override
	public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        DateTime value = this.get(from);
        if (value == null) {
            return JSONObject.NULL;
        }
        JSONObject result = new JSONObject();
        if (value.getTimeZone() != null && value.getTimeZone()!=TimeZone.getTimeZone("UTC")) {
            result.put(TIME_ZONE, value.getTimeZone().getID());
        }
        result.put(VALUE, value.toString());
        return result;
	}

    private DateTime parse(String timeZoneId, String value) throws OXException {
        TimeZone timeZone = null;
        if (null != timeZoneId) {
            timeZone = CalendarUtils.optTimeZone(timeZoneId, null);
            if (null == timeZone) {
                throw CalendarExceptionCodes.INVALID_TIMEZONE.create(timeZoneId);
            }
        }
        try {
            DateTime dateTime = DateTime.parse(timeZone, value);
            dateTime.getTimestamp(); // For input validation
            return dateTime;
        } catch (Exception e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, getAjaxName(), value);
        }
    }

}
