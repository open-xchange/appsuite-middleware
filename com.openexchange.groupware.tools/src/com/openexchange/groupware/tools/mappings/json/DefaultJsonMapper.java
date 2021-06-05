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

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.session.Session;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
     * The mapped fields
     */
    protected final E[] mappedFields;

	/**
	 * Initializes a new {@link DefaultJsonMapper}.
	 */
	public DefaultJsonMapper() {
		super();
		this.mappings = createMappings();
		this.columnMap = new TIntObjectHashMap<E>(this.mappings.size());
        for (final Entry<E, ? extends JsonMapping<? extends Object, O>> entry : this.mappings.entrySet()) {
            if (entry.getValue().getColumnID() != null) {
                this.columnMap.put(entry.getValue().getColumnID().intValue(), entry.getKey());
            }
        }
        this.mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
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
    public List<O> deserialize(JSONArray jsonArray, E[] fields) throws OXException, JSONException {
        return deserialize(jsonArray, fields, (TimeZone) null);
    }

    @Override
    public List<O> deserialize(JSONArray jsonArray, E[] fields, String timeZoneID) throws OXException, JSONException {
        return deserialize(jsonArray, fields, null != timeZoneID ? getTimeZone(timeZoneID) : null);
    }

    @Override
    public List<O> deserialize(JSONArray jsonArray, E[] fields, TimeZone timeZone) throws OXException, JSONException {
        List<O> objects = new ArrayList<O>(jsonArray.length());
        List<E> nonNullFields = Arrays.stream(fields).filter( f -> f != null).collect(Collectors.toList());
        boolean nestedArray = jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONArray;
        if(nestedArray) {
            for (int i = 0; i < jsonArray.length(); i++) {
                objects.add(deserialize(asJsonObject(jsonArray.getJSONArray(i), fields), nonNullFields.toArray(newArray(nonNullFields.size())), timeZone));
            }
        }
        else {
            if(jsonArray.length() > 0) {
                objects.add(deserialize(asJsonObject(jsonArray, fields), nonNullFields.toArray(newArray(nonNullFields.size())), timeZone));
            }
        }
        return objects;
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
    public E[] getMappedFields(int...columnIDs) {
        if (null != columnIDs && columnIDs.length > 0) {
            ArrayList<E> ret = new ArrayList<E>();
            for (int c : columnIDs) {
                ret.add(getMappedField(c));
            }
            return ret.toArray(newArray(ret.size()));
        }
        return null;
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
    public E[] getMappedFields(String... ajaxNames) {
        if (null != ajaxNames && ajaxNames.length > 0) {
            List<String> names = Arrays.asList(ajaxNames);
            //@formatter:off
            List<E> fields = mappings.entrySet().stream().
                filter(entry -> names.contains(entry.getValue().getAjaxName())). // filter entries which have one of the supplied ajax names
                map(entry -> entry.getKey()). //get the mapping from the entries
                collect(Collectors.toList());
            //@formatter:on
            return fields.toArray(newArray(fields.size()));
        }
        return null;
    }

	@Override
	public E[] getFields(final int[] columnIDs) throws OXException {
		return this.getFields(columnIDs, null, (E[])null);
	}

    @SuppressWarnings("unchecked")
    @Override
    public E[] getFields(final int[] columnIDs, final E... mandatoryFields) throws OXException {
		return this.getFields(columnIDs, null, mandatoryFields);
    }

	@SuppressWarnings("unchecked")
    @Override
    public E[] getFields(final int[] columnIDs, final EnumSet<E> illegalFields, final E... mandatoryFields) throws OXException {
		if (null == columnIDs) {
			throw new IllegalArgumentException("columnIDs");
		}
		final List<E> fields = new ArrayList<E>(columnIDs.length);
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
    public int[] getColumnIDs(E[] fields) throws OXException {
	    if (null == fields) {
	        return null;
	    }
        int[] columnIDs = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columnIDs[i] = get(fields[i]).getColumnID().intValue();
        }
        return columnIDs;
	}

	@Override
    public EnumMap<E, ? extends JsonMapping<? extends Object, O>> getMappings() {
		return this.mappings;
	}

    /**
     * Gets all mapped fields in an array.
     *
     * @return The mapped fields
     */
    @Override
    public E[] getMappedFields() {
        return mappedFields;
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
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultJsonMapper.class);
            LOG.error("", e);
        }
        return TimeZone.getTimeZone(ID);
    }

    private JSONObject asJsonObject(JSONArray itemArray, E[] fields) throws OXException, JSONException {
        if (fields.length != itemArray.length()) {
            throw new IllegalArgumentException("unexpected array length, epexted: " + fields.length + ", was: " + itemArray.length());
        }
        JSONObject jsonObject = new JSONObject(fields.length);
        for (int i = 0; i < fields.length; i++) {
            E field = fields[i];
            if(field != null) {
                JsonMapping<? extends Object, O> mapping = get(field);
                jsonObject.put(mapping.getAjaxName(), itemArray.get(i));
            }
        }
        return jsonObject;
    }

}
