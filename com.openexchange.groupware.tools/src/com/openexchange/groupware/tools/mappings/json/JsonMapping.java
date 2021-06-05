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
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.session.Session;

/**
 * {@link JsonMapping} - Extends the generic mapping by JSON specific
 * operations.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface JsonMapping<T, O> extends Mapping<T, O> {

	/**
	 * Gets the Ajax name of the mapped property.
	 *
	 * @return the readable name
	 */
	String getAjaxName();

	/**
     * Gets the column ID of the mapped property or null.
     *
     * @return the column ID or null
     */
    Integer getColumnID();

	/**
	 * Serializes the value of the mapped property from an object and sets it
	 * in the supplied JSON object.
	 *
	 * @param object the object to read the value from
	 * @param jsonObject the JSON object to populate
	 * @throws JSONException
	 * @throws OXException
	 */
	void serialize(O from, JSONObject to) throws JSONException, OXException;

	/**
	 * Serializes the value of the mapped property from an object and sets it
	 * in the supplied JSON object.
	 *
	 * @param object the object to read the value from
	 * @param jsonObject the JSON object to populate
	 * @param timeZone the client time zone to consider
	 * @throws JSONException
	 * @throws OXException
	 */
	void serialize(O from, JSONObject to, TimeZone timeZone) throws JSONException, OXException;

	/**
	 * Serializes the value of the mapped property from an object and sets it
	 * in the supplied JSON object.
	 *
	 * @param object the object to read the value from
	 * @param jsonObject the JSON object to populate
	 * @param timeZone the client time zone to consider
	 * @param session the underlying session
	 * @throws JSONException
	 * @throws OXException
	 */
	void serialize(O from, JSONObject to, TimeZone timeZone, Session session) throws JSONException, OXException;

    /**
     * Deserializes the value of the mapped property from a JSON object and
     * sets it in the supplied object.
     *
     * @param from the JSON object to read the value from
     * @param to the object to populate
     * @throws JSONException
     * @throws OXException
     */
    void deserialize(JSONObject from, O to) throws JSONException, OXException;

    /**
     * Deserializes the value of the mapped property from a JSON object and
     * sets it in the supplied object.
     *
     * @param from the JSON object to read the value from
     * @param to the object to populate
     * @param timeZone the client time zone to consider, or <code>null</code> if not relevant
     * @throws JSONException
     * @throws OXException
     */
    void deserialize(JSONObject from, O to, TimeZone timeZone) throws JSONException, OXException;

	/**
	 * Serializes the value of the mapped property from an object into JSON.
	 *
	 * @param object the object to read the value from
	 * @param timeZone the client time zone to consider
	 * @param session the underlying session
	 * @return the serialized value
	 * @throws JSONException
	 * @throws OXException
	 */
	Object serialize(O from, TimeZone timeZone, Session session) throws JSONException, OXException;

}
