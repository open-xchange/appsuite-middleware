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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;


/**
 * {@link DefaultJsonMapper} - Abstract {@link JsonMapper} implementation.
 *
 * @param <O> the type of the object
 * @param <E> the enum type for the fields
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultJsonMapper<O, E extends Enum<E>> extends DefaultMapper<O, E> implements JsonMapper<O, E> {

	/**
	 * Map containing all available mappings.
	 */
	protected final EnumMap<E, ? extends JsonMapping<? extends Object, O>> mappings;
	
	/**
	 * Maps column IDs to enum values
	 */
	protected final Map<Integer, E> columnMap;
	
	/**
	 * Initializes a new {@link DefaultJsonMapper}.
	 */
	public DefaultJsonMapper() {
		super();
		this.mappings = createMappings();
		this.columnMap = new HashMap<Integer, E>(this.mappings.size());
		for (final Entry<E, ? extends JsonMapping<? extends Object, O>> entry : this.mappings.entrySet()) {
			this.columnMap.put(Integer.valueOf(entry.getValue().getColumnID()), entry.getKey());
		}
	}
	
	@Override
	public JSONObject serialize(final O object, final E[] fields) throws JSONException, OXException {
        final JSONObject json = new JSONObject();
        for (final E field : fields) {
        	this.get(field).serialize(object, json);
		}
        return json;
	}

	@Override
	public JSONArray serialize(final List<O> objects, final E[] fields) throws JSONException, OXException {
		final JSONArray jsonArray = new JSONArray();
		for (final O object : objects) {
			jsonArray.put(this.serialize(object, fields));
		}
		return jsonArray;
	}
	
	@Override
	public O deserialize(final JSONObject jsonObject, final E[] fields) throws OXException, JSONException {
		final O object = newInstance();
        for (final E field : fields) {
        	final JsonMapping<? extends Object, O> mapping = this.get(field);
        	final String ajaxName = mapping.getAjaxName();
        	if (null != ajaxName && 0 < ajaxName.length() && jsonObject.hasAndNotNull(ajaxName)) {
        		mapping.deserialize(jsonObject, object);
        	}
        }
		return object;
	}

	@Override
	public List<O> deserialize(final JSONArray jsonArray, final E[] fields) throws OXException, JSONException {
		final List<O> objects = new ArrayList<O>();
		for (int i = 0; i < jsonArray.length(); i++) {
			objects.add(this.deserialize(jsonArray.getJSONObject(i), fields));			
		}
		return objects;
	}

	@Override
	public JsonMapping<? extends Object, O> get(final E field) throws OXException {
		if (null == field) {
			throw new IllegalArgumentException("field");
		}
		final JsonMapping<? extends Object, O> mapping = this.mappings.get(field);
		if (null == mapping) {
			throw OXException.notFound(field.toString());
		}
		return mapping;
	}
	
	@Override
	public E getMappedField(final int columnID) {
		return this.columnMap.get(Integer.valueOf(columnID));
	}
	
	@Override
	public int[] getColumnIDs(final E[] fields) throws OXException {
		if (null == fields) {
			throw new IllegalArgumentException("fields");
		}
		final int[] columnIDs = new int[fields.length];
		for (int i = 0; i < fields.length; i++) {
			columnIDs[i] = this.get(fields[i]).getColumnID();
		}
		return columnIDs;
	}

	@Override
	public E[] getFields(final int[] columnIDs) throws OXException {
		return this.getFields(columnIDs, null, (E[])null);
	}
	
	@Override
    public E[] getFields(final int[] columnIDs, final E... mandatoryFields) throws OXException {
		return this.getFields(columnIDs, null, mandatoryFields);
    }

	@Override
    public E[] getFields(final int[] columnIDs, final EnumSet<E> illegalFields, final E... mandatoryFields) throws OXException {
		if (null == columnIDs) {
			throw new IllegalArgumentException("columnIDs");
		}
		final Set<E> fields = new HashSet<E>();
		for (final int columnID : columnIDs) {
			final E field = this.getMappedField(columnID);
			if (null != field && (null == illegalFields || false == illegalFields.contains(field))) {
                fields.add(field);
            }
		}
        if (null != mandatoryFields) {
            for (final E field : mandatoryFields) {
				fields.add(field);
            }
        }
        return fields.toArray(newArray(fields.size()));
    }

	@Override
	protected EnumMap<E, ? extends JsonMapping<? extends Object, O>> getMappings() {
		return this.mappings;
	}
	
	/**
	 * Creates the mappings for all possible values of the underlying enum. 
	 * 
	 * @return the mappings
	 */
	protected abstract EnumMap<E, ? extends JsonMapping<? extends Object, O>> createMappings();

}
