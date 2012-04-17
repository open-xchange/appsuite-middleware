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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapper;

/**
 * {@link JsonMapper} - Generic JSON mapper definition for field-wise 
 * operations on objects
 *
 * @param <O> the type of the object
 * @param <E> the enum type for the fields
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface JsonMapper<O, E extends Enum<E>> extends Mapper<O, E> {
	
	@Override
	JsonMapping<? extends Object, O> get(E field) throws OXException;

	/**
	 * Gets the field whose mapping denotes the supplied column ID. 
	 * 
	 * @param columnID the column ID
	 * @return the field, or <code>null</code> if no such field was found
	 */
	E getMappedField(int columnID);

	/**
	 * Deserializes an object from JSON.
	 * 
	 * @param jsonObject the JSON object to create the object from 
	 * @param fields the fields present in the object
	 * @return the object
	 * @throws OXException
	 * @throws JSONException
	 */
	O deserialize(JSONObject jsonObject, E[] fields) throws OXException, JSONException;

	/**
	 * Deserializes objects from JSON.
	 * 
	 * @param jsonArray the JSON array to create the objects from
	 * @param fields the fields present object
	 * @return the objects
	 * @throws OXException
	 * @throws JSONException
	 */
	List<O> deserialize(JSONArray jsonArray, E[] fields) throws OXException, JSONException;
	
	/**
	 * Serializes the supplied object to JSON.
	 * 
	 * @param object the object to read the values from
	 * @param fields the fields to be set
	 * @return the JSON object
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONObject serialize(O object, E[] fields) throws JSONException, OXException;
	
	/**
	 * Serializes the supplied object to JSON.
	 * 
	 * @param object the object to read the values from
	 * @param fields the fields to be set
	 * @param timeZone the client time zone to consider
	 * @return the JSON object
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONObject serialize(O object, E[] fields, final TimeZone timeZone) throws JSONException, OXException;
	
	/**
	 * Serializes the supplied object to JSON.
	 * 
	 * @param object the object to read the values from
	 * @param fields the fields to be set
	 * @param timeZoneID the client time zone identifier to consider
	 * @return the JSON object
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONObject serialize(O object, E[] fields, final String timeZoneID) throws JSONException, OXException;
	
	/**
	 * Serializes the supplied object to JSON.
	 * 
	 * @param object the object to read the values from
	 * @param to the JSONObject to serialize into
	 * @param fields the fields to be set
	 * @param timeZone the client time zone to consider
	 * @throws JSONException
	 * @throws OXException
	 */
	void serialize(O object, JSONObject to, E[] fields, final TimeZone timeZone) throws JSONException, OXException;

	/**
	 * Serializes the supplied object to JSON.
	 * 
	 * @param object the object to read the values from
	 * @param to the JSONObject to serialize into
	 * @param fields the fields to be set
	 * @param timeZoneID the client time zone identifier to consider
	 * @throws JSONException
	 * @throws OXException
	 */
	void serialize(final O object, final JSONObject to, final E[] fields, final String timeZoneID) throws JSONException, OXException;

	/**
	 * Serializes the supplied objects to JSON.
	 * 
	 * @param objects the object to read the values from
	 * @param fields the fields to be set
	 * @return the JSON array
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONArray serialize(List<O> objects, E[] fields) throws JSONException, OXException;

	/**
	 * Gets an int array of column IDs from the supplied fields.
	 * 
	 * @param fields the fields
	 * @return the column IDs in an array
	 * @throws OXException
	 */
	int[] getColumnIDs(E[] fields) throws OXException;


	/**
	 * Gets the fields whose mappings denotes the supplied column IDs.
	 *  
	 * @param columnIDs the column IDs
	 * @return the fields
	 * @throws OXException
	 */
	E[] getFields(int[] columnIDs) throws OXException;

	/**
	 * Gets the fields whose mappings denotes the supplied column IDs and 
	 * optionally adds mandatory fields to the result if not yet present.
	 * 
	 * @param columnIDs the column IDs
	 * @param mandatoryFields the mandatory fields
	 * @return the fields
	 * @throws OXException
	 */
    E[] getFields(final int[] columnIDs, final E... mandatoryFields) throws OXException;

	/**
	 * Gets the fields whose mappings denotes the supplied column IDs, 
	 * optionally removes illegal fields and adds mandatory fields to the 
	 * result if not yet present.
	 * 
	 * @param columnIDs the column IDs
	 * @param illegalFields the illegal fields
	 * @param mandatoryFields the mandatory fields
	 * @return the fields
	 * @throws OXException
	 */
    E[] getFields(final int[] columnIDs, final EnumSet<E> illegalFields, final E... mandatoryFields) throws OXException;

}
