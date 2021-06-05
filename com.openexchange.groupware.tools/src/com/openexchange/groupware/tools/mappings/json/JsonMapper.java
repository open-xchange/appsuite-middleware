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

import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapper;
import com.openexchange.session.Session;

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
	 * Gets the fields whose mapping denotes one of the the supplied column IDs
	 *
	 * @param columnIDs The column IDs
	 * @return The fields, or <code>null</code> if no such fileds were found
	 */
    E[] getMappedFields(int...columnIDs);

    /**
     * Gets the field whose mapping denotes the supplied ajax name.
     *
     * @param ajaxName the ajax name
     * @return the field, or <code>null</code> if no such field was found
     */
    E getMappedField(String ajaxName);

    /**
     * Gets the fields whose mappings denotes the supplied ajax names.
     *
     * @param ajaxNames the ajax names
     * @return the fields, or <code>null</null> if no such fields were found
     */
    E[] getMappedFields(String...ajaxNames);

	/**
	 * Gets the fields whose mappings denotes the supplied column IDs. The
	 * field order is preserved.
	 *
	 * @param columnIDs the column IDs
	 * @return the fields
	 * @throws OXException if there is no mapping for a columnID
	 */
	E[] getFields(int[] columnIDs) throws OXException;

	/**
	 * Gets the fields whose mappings denotes the supplied column IDs and
	 * optionally adds mandatory fields to the result if not yet present. The
	 * field order is preserved, while the not yet defined mandatory fields
	 * are appended at the end of the array.
	 *
	 * @param columnIDs the column IDs
	 * @param mandatoryFields the mandatory fields
	 * @return the fields
	 * @throws OXException if there is no mapping for a columnID
	 */
    E[] getFields(int[] columnIDs, E... mandatoryFields) throws OXException;

	/**
	 * Gets the fields whose mappings denotes the supplied column IDs,
	 * optionally removes illegal fields and adds mandatory fields to the
	 * result if not yet present. The field order is preserved, while the not
	 * yet defined mandatory fields are appended at the end of the array.
	 *
	 * @param columnIDs the column IDs
	 * @param illegalFields the illegal fields
	 * @param mandatoryFields the mandatory fields
	 * @return the fields
	 * @throws OXException if there is no mapping for a columnID
	 */
    E[] getFields(int[] columnIDs, EnumSet<E> illegalFields, E... mandatoryFields) throws OXException;

    /**
     * Gets the column identifiers associated with the supplied fields.
     * 
     * @param fields The fields to get the column ids for
     * @return The column ids
     * @throws OXException if there is no mapping for a field
     */
    int[] getColumnIDs(E[] fields) throws OXException;

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
     * Deserializes an object from JSON.
     *
     * @param jsonObject the JSON object to create the object from
     * @param fields the fields present in the object
     * @param timeZone the client time zone to consider, or <code>null</code> if not relevant
     * @return the object
     * @throws OXException
     * @throws JSONException
     */
    O deserialize(JSONObject jsonObject, E[] fields, TimeZone timeZone) throws OXException, JSONException;

    /**
     * Deserializes an object from JSON.
     *
     * @param jsonObject the JSON object to create the object from
     * @param fields the fields present in the object
     * @param timeZoneID the client time zone identifier to consider, or <code>null</code> if not relevant
     * @return the object
     * @throws OXException
     * @throws JSONException
     */
    O deserialize(JSONObject jsonObject, E[] fields, String timeZoneID) throws OXException, JSONException;

    /**
     * Deserializes an object from a JSONArray
     *
     * @param jsonArray The JSONArray to deserialize
     * @param fields An array of fields representing the Object, where each field in the
     * @return The objects deserialized from the given JSONArray
     * @throws OXException
     * @throws JSONException
     */
    List<O> deserialize(JSONArray jsonArray, E[] fields) throws OXException, JSONException;

    /**
     * Deserializes an object from a JSONArray
     *
     * @param jsonArray The JSONArray to deserialize
     * @param fields An array of fields representing the Object, where each field in the
     * @return The objects deserialized from the given JSONArray
     * @param timeZoneID the client time zone identifier to consider, or <code>null</code> if not relevant
     * @throws OXException
     * @throws JSONException
     */
    List<O> deserialize(JSONArray jsonArray, E[] fields, String timeZoneID) throws OXException, JSONException;

    /**
     * Deserializes an object from a JSONArray
     *
     * @param jsonArray The JSONArray to deserialize
     * @param fields An array of fields representing the Object, where each field in the
     * @return The objects deserialized from the given JSONArray
     * @param timeZoneID the client time zone identifier to consider, or <code>null</code> if not relevant
     * @throws OXException
     * @throws JSONException
     */
    List<O> deserialize(JSONArray jsonArray, E[] fields, TimeZone timeZone) throws OXException, JSONException;

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
	JSONObject serialize(O object, E[] fields, TimeZone timeZone) throws JSONException, OXException;

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
	JSONObject serialize(O object, E[] fields, String timeZoneID) throws JSONException, OXException;

	/**
	 * Serializes the supplied object to JSON.
	 *
	 * @param object the object to read the values from
	 * @param fields the fields to be set
	 * @param timeZoneID the client time zone identifier to consider
	 * @param session the underlying session
	 * @return the JSON object
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONObject serialize(O object, E[] fields, String timeZoneID, Session session) throws JSONException, OXException;

	/**
	 * Serializes the supplied object to JSON.
	 *
	 * @param object the object to read the values from
	 * @param fields the fields to be set
	 * @param timeZone the client time zone to consider
	 * @param session the underlying session
	 * @return the JSON object
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONObject serialize(O object, E[] fields, TimeZone timeZone, Session session) throws JSONException, OXException;

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
	void serialize(O object, JSONObject to, E[] fields, TimeZone timeZone) throws JSONException, OXException;

	/**
	 * Serializes the supplied object to JSON.
	 *
	 * @param object the object to read the values from
	 * @param to the JSONObject to serialize into
	 * @param fields the fields to be set
	 * @param timeZone the client time zone to consider
	 * @param session the underlying session
	 * @throws JSONException
	 * @throws OXException
	 */
	void serialize(O object, JSONObject to, E[] fields, TimeZone timeZone, Session session) throws JSONException, OXException;

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
    void serialize(O object, JSONObject to, E[] fields, String timeZoneID) throws JSONException, OXException;

    /**
     * Serializes the supplied object to JSON.
     *
     * @param object the object to read the values from
     * @param to the JSONObject to serialize into
     * @param fields the fields to be set
     * @param timeZoneID the client time zone identifier to consider
     * @param session the underlying session
     * @throws JSONException
     * @throws OXException
     */
    void serialize(O object, JSONObject to, E[] fields, String timeZoneID, Session session) throws JSONException, OXException;

	/**
	 * Serializes the supplied objects to JSON arrays inside a JSON array.
	 *
	 * @param objects the object to read the values from
	 * @param fields the fields to set in the arrays, in the expected order
	 * @param timeZoneID the client time zone identifier to consider
	 * @param session the underlying session
	 * @return the JSON array
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONArray serialize(List<O> objects, E[] fields, String timeZoneID, Session session) throws JSONException, OXException;

	/**
	 * Serializes the supplied objects to JSON arrays inside a JSON array.
	 *
	 * @param objects the object to read the values from
	 * @param fields the fields to set in the arrays, in the expected order
	 * @param timeZone the client time zone to consider
	 * @param session the underlying session
	 * @return the JSON array
	 * @throws JSONException
	 * @throws OXException
	 */
	JSONArray serialize(List<O> objects, E[] fields, TimeZone timeZone, Session session) throws JSONException, OXException;

    /**
     * Gets all mapped fields in an array.
     *
     * @return The mapped fields
     */
    E[] getMappedFields();

}
