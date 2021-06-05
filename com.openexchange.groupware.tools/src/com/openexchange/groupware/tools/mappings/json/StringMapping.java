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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;

/**
 * {@link StringMapping} - JSON specific mapping implementation for Strings.
 *
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class StringMapping<O> extends DefaultJsonMapping<String, O> {

    public StringMapping(final String ajaxName, final Integer columnID) {
		super(ajaxName, columnID);
	}

	@Override
	public void deserialize(JSONObject from, O to) throws JSONException, OXException {
		this.set(to, from.isNull(getAjaxName()) ? null : from.getString(getAjaxName()));
	}

	@Override
	public boolean truncate(final O object, final int length) throws OXException {
		final String value = this.get(object);
		if (null != value && length < value.length()) {
			this.set(object, value.substring(0, length));
			return true;
		}
		return false;
	}

    @Override
    public boolean replaceAll(O object, String regex, String replacement) throws OXException {
        String value = get(object);
        if (null != value) {
            String replacedValue = value.replaceAll(regex, replacement);
            if (false == value.equals(replacedValue)) {
                set(object, replacedValue);
                return true;
            }
        }
        return false;
    }

}
