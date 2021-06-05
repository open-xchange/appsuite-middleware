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

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DateMapping} - JSON specific mapping implementation for Dates.
 *
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DateMapping<O> extends DefaultJsonMapping<Date, O> {

	/**
	 * Initializes a new {@link DateMapping}.
	 *
	 * @param ajaxName The AJAX name
	 * @param columnID The column identifier
	 */
    public DateMapping(final String ajaxName, final Integer columnID) {
		super(ajaxName, columnID);
	}

	@Override
	public void deserialize(JSONObject from, O to) throws JSONException, OXException {
	    final String ajaxName = getAjaxName();
        if (from.isNull(ajaxName)) {
            set(to, null);
        } else {
            final Object object = from.get(ajaxName);
            if (object instanceof Number) {
                set(to, new Date(((Number) object).longValue()));
            } else if (null != object) {
                final String sObject = object.toString();
                if (!com.openexchange.java.Strings.isEmpty(sObject)) {
                    try {
                        set(to, new Date(Long.parseLong(sObject)));
                    } catch (NumberFormatException e) {
                        throw new JSONException("JSONObject[\"" + ajaxName + "\"] is not a number: " + object);
                    }
                }
            } else {
                throw new JSONException("JSONObject[\"" + ajaxName + "\"] is not a number: " + object);
            }
        }
	}

	@Override
	public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
		final Date value = this.get(from);
		return null == value ? JSONObject.NULL : Long.valueOf(value.getTime());
	}

}
