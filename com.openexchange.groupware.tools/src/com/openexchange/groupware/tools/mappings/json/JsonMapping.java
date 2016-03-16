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
	 * Gets the column ID of the mapped property.
	 *
	 * @return the column ID
	 */
	int getColumnID();

	/**
	 * Serializes the value of the mapped property from an object and sets it
	 * in the supplied JSON object.
	 *
	 * @param object the object to read the value from
	 * @param jsonObject the JSON object to populate
	 * @throws JSONException
	 */
	void serialize(O from, JSONObject to) throws JSONException;

	/**
	 * Serializes the value of the mapped property from an object and sets it
	 * in the supplied JSON object.
	 *
	 * @param object the object to read the value from
	 * @param jsonObject the JSON object to populate
	 * @param timeZone the client time zone to consider
	 * @throws JSONException
	 */
	void serialize(O from, JSONObject to, TimeZone timeZone) throws JSONException;

	/**
	 * Serializes the value of the mapped property from an object and sets it
	 * in the supplied JSON object.
	 *
	 * @param object the object to read the value from
	 * @param jsonObject the JSON object to populate
	 * @param timeZone the client time zone to consider
	 * @param session the underlying session
	 * @throws JSONException
	 */
	void serialize(O from, JSONObject to, TimeZone timeZone, Session session) throws JSONException;

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
	 */
	Object serialize(O from, TimeZone timeZone, Session session) throws JSONException;

}
