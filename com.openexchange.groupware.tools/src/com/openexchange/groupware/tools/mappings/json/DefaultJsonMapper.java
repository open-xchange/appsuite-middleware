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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.session.Session;

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
	protected final TIntObjectMap<E> columnMap;

	/**
	 * Initializes a new {@link DefaultJsonMapper}.
	 */
	public DefaultJsonMapper() {
		super();
		this.mappings = createMappings();
		this.columnMap = new TIntObjectHashMap<E>(this.mappings.size());
		for (final Entry<E, ? extends JsonMapping<? extends Object, O>> entry : this.mappings.entrySet()) {
			this.columnMap.put(entry.getValue().getColumnID(), entry.getKey());
		}
	}

	@Override
	public JSONObject serialize(final O object, final E[] fields) throws JSONException, OXException {
		return this.serialize(object, fields, (TimeZone)null);
	}

    @Override
    public JSONObject serialize(final O object, final E[] fields, final String timeZoneID) throws JSONException, OXException {
        return this.serialize(object, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null);
    }

	@Override
    public void serialize(O object, JSONObject to, E[] fields, String timeZoneID, Session session) throws JSONException, OXException {
	    this.serialize(object, to, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null, session);
    }

    @Override
    public void serialize(O object, JSONObject to, E[] fields, TimeZone timeZone, Session session) throws JSONException, OXException {
        for (final E field : fields) {
            final JsonMapping<? extends Object, O> mapping = this.get(field);
            if (null != mapping) {
                mapping.serialize(object, to, timeZone, session);
            }
        }
    }

	@Override
	public JSONObject serialize(O object, E[] fields, TimeZone timeZone, Session session) throws JSONException, OXException {
		JSONObject jsonObject = new JSONObject(fields.length);
		this.serialize(object, jsonObject, fields, timeZone, session);
		return jsonObject;
	}

	@Override
	public JSONObject serialize(O object, E[] fields, String timeZoneID, Session session) throws JSONException, OXException {
		return this.serialize(object, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null, session);
	}

	@Override
	public JSONObject serialize(O object, E[] fields, TimeZone timeZone) throws JSONException, OXException {
		return this.serialize(object, fields, timeZone, null);
	}

	@Override
	public void serialize(final O object, final JSONObject to, final E[] fields, final String timeZoneID) throws JSONException, OXException {
		this.serialize(object, to, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null);
	}

	@Override
	public void serialize(O object, JSONObject to, E[] fields, final TimeZone timeZone) throws JSONException, OXException {
		this.serialize(object, to, fields, timeZone, null);
	}

//	@Override
//	public JSONArray serialize(final List<O> objects, final E[] fields) throws JSONException, OXException {
//		return this.serialize(objects, fields, null);
//	}
//
//	@Override
//	public JSONArray serialize(final List<O> objects, final E[] fields, String timeZoneID) throws JSONException, OXException {
//		return this.serialize(objects, fields, timeZoneID, null);
//	}
//
//	@Override
//	public JSONArray serialize(List<O> objects, E[] fields, String timeZoneID, Session session) throws JSONException, OXException {
//		final JSONArray jsonArray = new JSONArray();
//		TimeZone timeZone = null != timeZoneID ? getTimeZone(timeZoneID) : null;
//		for (final O object : objects) {
//			jsonArray.put(this.serialize(object, fields, timeZone, session));
//		}
//		return jsonArray;
//	}

	@Override
	public JSONArray serialize(List<O> objects, E[] fields, String timeZoneID, Session session) throws JSONException, OXException {
		return this.serialize(objects, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null, session);
	}

	@Override
	public JSONArray serialize(List<O> objects, E[] fields, TimeZone timeZone, Session session) throws JSONException, OXException {
		JSONArray jsonArray = new JSONArray(objects.size());
		final int length = fields.length;
		for (O object : objects) {
            JSONArray itemArray = new JSONArray(length);
			for (E field : fields) {
				itemArray.put(get(field).serialize(object, timeZone, session));
			}
			jsonArray.put(itemArray);
		}
		return jsonArray;
	}

    @Override
    public O deserialize(final JSONObject jsonObject, final E[] fields) throws OXException, JSONException {
        return this.deserialize(jsonObject, fields, (TimeZone)null);
    }

    @Override
    public O deserialize(JSONObject jsonObject, E[] fields, String timeZoneID) throws OXException, JSONException {
        return this.deserialize(jsonObject, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null);
    }

    @Override
    public O deserialize(JSONObject jsonObject, E[] fields, TimeZone timeZone) throws OXException, JSONException {
        O object = newInstance();
        for (E field : fields) {
            JsonMapping<? extends Object, O> mapping = this.get(field);
            String ajaxName = mapping.getAjaxName();
            if (null != ajaxName && 0 < ajaxName.length() && jsonObject.has(ajaxName)) {
                mapping.deserialize(jsonObject, object, timeZone);
            }
        }
        return object;
    }

    @Override
    public JsonMapping<? extends Object, O> get(final E field) throws OXException {
        final JsonMapping<? extends Object, O> mapping = this.opt(field);
        if (null == mapping) {
            throw OXException.notFound(field.toString());
        }
        return mapping;
    }

    @Override
    public JsonMapping<? extends Object, O> opt(final E field) {
        if (null == field) {
            throw new IllegalArgumentException("field");
        }
        return this.mappings.get(field);
    }

    @Override
    public E getMappedField(final int columnID) {
        return this.columnMap.get(columnID);
    }

    @Override
    public E getMappedField(String ajaxName) {
        if (null != ajaxName) {
            for (Entry<E, ? extends JsonMapping<? extends Object, O>> entry: mappings.entrySet()) {
                if (ajaxName.equals(entry.getValue().getAjaxName())) {
                    return entry.getKey();
                }
            }
        }
        return null;
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
		final List<E> fields = new ArrayList<E>();
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

    private static final ConcurrentMap<String, Future<TimeZone>> ZONE_CACHE = new ConcurrentHashMap<String, Future<TimeZone>>();

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    protected static TimeZone getTimeZone(final String ID) {
        Future<TimeZone> f = ZONE_CACHE.get(ID);
        if (f == null) {
            final FutureTask<TimeZone> ft = new FutureTask<TimeZone>(new Callable<TimeZone>() {

                @Override
                public TimeZone call() throws Exception {
                    return TimeZone.getTimeZone(ID);
                }
            });
            f = ZONE_CACHE.putIfAbsent(ID, ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultJsonMapper.class);
            LOG.error("", e);
        }
        return TimeZone.getTimeZone(ID);
    }

}
