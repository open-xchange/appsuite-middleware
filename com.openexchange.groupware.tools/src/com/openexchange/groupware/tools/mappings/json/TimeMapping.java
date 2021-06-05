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

package com.openexchange.groupware.tools.mappings.json;

import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TimeMapping} - JSON specific mapping implementation for Times,
 * i.e. {@link Date}s in the client timezone.
 *
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class TimeMapping<O> extends DefaultJsonMapping<Date, O> {

    public TimeMapping(final String ajaxName, final Integer columnID) {
		super(ajaxName, columnID);
	}

    @Override
    public void deserialize(JSONObject from, O to) throws JSONException, OXException {
        this.set(to, from.isNull(getAjaxName()) ? null : new Date(from.getLong(getAjaxName())));
    }

    @Override
    public void deserialize(JSONObject from, O to, TimeZone timeZone) throws JSONException, OXException {
        this.set(to, from.isNull(getAjaxName()) ? null : new Date(subtractTimeZoneOffset(from.getLong(getAjaxName()), timeZone)));
    }

	@Override
	public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
		Date value = this.get(from);
		return null != value ? L(addTimeZoneOffset(value.getTime(), timeZone)) : JSONObject.NULL;
	}

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    private static long subtractTimeZoneOffset(final long date, final TimeZone timeZone) {
        return null == timeZone ? date : date - timeZone.getOffset(date);
    }

}
