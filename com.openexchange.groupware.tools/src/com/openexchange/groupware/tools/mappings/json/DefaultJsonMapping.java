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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.session.Session;

/**
 * {@link DefaultJsonMapping} - Default JSON specific mapping implementation.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultJsonMapping<T, O> extends DefaultMapping<T, O> implements JsonMapping<T, O> {

	private final String ajaxName;
    private final Integer columnID;

    public DefaultJsonMapping(String ajaxName, Integer columnID) {
		this.ajaxName = ajaxName;
		this.columnID = columnID;
	}

	@Override
	public String getAjaxName() {
		return this.ajaxName;
	}

	@Override
    public Integer getColumnID() {
		return this.columnID;
	}

    @Override
    public void deserialize(JSONObject from, O to, TimeZone timeZone) throws JSONException, OXException {
        this.deserialize(from, to);
    }

	@Override
	public void serialize(O from, JSONObject to) throws JSONException, OXException {
		this.serialize(from, to, null);
	}

	@Override
	public void serialize(O from, JSONObject to, TimeZone timeZone) throws JSONException, OXException {
		this.serialize(from, to, timeZone, null);
	}

	@Override
	public void serialize(O from, JSONObject to, TimeZone timeZone, Session session) throws JSONException, OXException {
		to.put(getAjaxName(), serialize(from, timeZone, session));
	}

	/**
	 * Override this <code>serialize</code>-method if needed.
	 * @throws OXException
	 */
    @SuppressWarnings("unused")
    @Override
	public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException, OXException {
		final T value = this.get(from);
		return null != value ? value : JSONObject.NULL;
	}

}
