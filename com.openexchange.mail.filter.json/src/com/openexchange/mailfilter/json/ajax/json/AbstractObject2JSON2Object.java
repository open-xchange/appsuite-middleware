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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.json.ajax.json;

import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;

/**
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public abstract class AbstractObject2JSON2Object<T> {

    /**
     * Default constructor.
     */
    protected AbstractObject2JSON2Object() {
        super();
    }

    public JSONObject write(final T obj) throws JSONException {
        final JSONObject json = new JSONObject();
        for (final Mapper<T> mapper : allMapper()) {
            if (!mapper.isNull(obj)) {
                json.put(mapper.getAttrName(), mapper.getAttribute(obj));
            }
        }
        return json;
    }

    public T parse(final JSONObject json) throws JSONException, SieveException, OXException {
        final T obj = createObject();
        for (final Mapper<T> mapper : allMapper()) {
            final String attrName = mapper.getAttrName();
            if (json.has(attrName)) {
                try {
                    mapper.setAttribute(obj, json.get(attrName));
                } catch (final ClassCastException e) {
                    throw new JSONException(e);
                }
            }
        }
        return obj;
    }

    public T parse(final T obj, final JSONObject json) throws JSONException, SieveException, OXException {
        for (final Mapper<T> mapper : allMapper()) {
            final String attrName = mapper.getAttrName();
            if (json.has(attrName)) {
                try {
                    mapper.setAttribute(obj, json.get(attrName));
                } catch (final ClassCastException e) {
                    throw new JSONException(e);
                }
            }
        }
        return obj;
    }

    /**
     * Convenience method that just invokes {@link #write(Object[], String[])}
     * with a <code>String</code> array as second argument that only contains
     * given field.
     *
     * @param objs -
     *                the data objects
     * @param field -
     *                the field to write
     * @return
     * @throws JSONException
     */
    public JSONArray write(final T[] objs, final String field) throws JSONException {
        return write(objs, new String[] { field });
    }

    /**
     * Writes given fields of data objects into separate JSONArrays which in
     * turn are put into a surrounding JSONArray
     *
     * @param objs -
     *                the data objects
     * @param fields -
     *                the fields to write
     * @return resulting <code>JSONArray</code>
     * @throws JSONException
     */
    public JSONArray write(final T[] objs, final String[] fields) throws JSONException {
        final int objsLen = objs.length;
        if (objsLen <= 0) {
            return new JSONArray(0);
        }
        final JSONArray array = new JSONArray(objsLen);
        final int fieldsLength = fields.length;
        for (final T t : objs) {
            final JSONObject tmpo = new JSONObject(fieldsLength);
            for (final String field : fields) {
                final Mapper<T> mapper = getMapper(field);
                if (!mapper.isNull(t)) {
                    tmpo.put(mapper.getAttrName(), mapper.getAttribute(t));
                }
            }
            array.put(tmpo);
        }
        return array;
    }

    /**
     * Convenience method that just invokes {@link #write(Object[], String[])}
     * with {@link #getListFields()} as second argument
     *
     * @param objs -
     *                the data objects
     * @return resulting <code>JSONArray</code>
     * @throws JSONException
     */
    public JSONArray write(final T[] objs) throws JSONException {
        return write(objs, getListFields());
    }

    public static interface Mapper<T> {
        String getAttrName();

        boolean isNull(T obj);

        Object getAttribute(T obj) throws JSONException;

        void setAttribute(T obj, Object attr) throws JSONException, SieveException, OXException;
    }

    protected abstract Mapper<T>[] allMapper();

    protected abstract T createObject();

    protected abstract String[] getListFields();

    protected abstract Mapper<T> getMapper(final String attrName);
}
