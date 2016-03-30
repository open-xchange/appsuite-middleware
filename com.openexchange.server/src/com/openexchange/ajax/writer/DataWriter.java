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

package com.openexchange.ajax.writer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.json.JSONWriter;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link DataWriter} - Base class for all writers used throughout modules.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DataWriter {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataWriter.class);

    protected TimeZone timeZone;

    static final TimeZone UTC = TimeZoneUtils.getTimeZone("UTC");

    protected JSONWriter jsonwriter;

    /**
     * Constructor for subclasses.
     */
    protected DataWriter(final TimeZone timeZone, final JSONWriter writer) {
        super();
        this.timeZone = timeZone;
        this.jsonwriter = writer;
    }

    public void writeParameter(final String name, final String value) throws JSONException {
        if (value != null && value.length() > 0) {
            jsonwriter.key(name);
            jsonwriter.value(value);
        }
    }

    public void writeParameter(final String name, final Date value) throws JSONException {
        if (value != null) {
            jsonwriter.key(name);
            jsonwriter.value(value.getTime());
        }
    }

    // new implementation

    /**
     * Puts given name-<code>String</code>-pair into specified JSON object provided that <code>String</code> value is not <code>null</code>
     * and not empty
     *
     * @param name The name to which the value is bound
     * @param value The <code>String</code> value
     * @param json The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final String value, final JSONObject json) throws JSONException {
        if (value != null && value.length() > 0) {
            json.put(name, value);
        }
    }

    /**
     * Conditionally puts given name-<code>String</code>-pair into specified
     * JSON object provided that <code>String</code> value is not
     * <code>null</code> and not empty.
     * @param name The name to which the value is bound
     * @param value The <code>String</code> value
     * @param json The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final String value, final JSONObject json, final boolean condition) throws JSONException {
        if (condition) {
            writeParameter(name, value, json);
        }
    }

    /**
     * Puts given <code>JSONValue</code>'s time value into specified JSON array
     *
     * @param name The name to which the value is bound
     * @param value The <code>JSON</code> value
     * @param jsonArray The JSON array to put into
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final JSONValue value, final JSONObject json) throws JSONException {
        if (value != null) {
            json.put(name, value);
        }
    }

    /**
     * Conditionally puts given <code>JSONValue</code> value into specified JSON array
     *
     * @param name The name to which the value is bound
     * @param value The <code>JSON</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final JSONValue value, final JSONObject json, final boolean condition) throws JSONException {
        if (condition) {
            json.put(name, value);
        }
    }

    /**
     * Puts given name-<code>int</code>-pair into specified JSON object
     *
     * @param name The name to which the value is bound
     * @param value The <code>int</code> value
     * @param json The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final int value, final JSONObject json) throws JSONException {
        json.put(name, value);
    }

    /**
     * Conditionally puts given <code>int</code> value into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>int</code> value
     * @param json The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to omit value
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final int value, final JSONObject json, final boolean condition) throws JSONException {
        if (condition) {
            writeParameter(name, value, json);
        }
    }

    /**
     * Puts given name-<code>long</code>-pair into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>long</code> value
     * @param json The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final long value, final JSONObject json) throws JSONException {
        // Large values of long must be written as string. See bug 11311.
        writeParameter(name, Long.toString(value), json);
    }

    /**
     * Conditionally puts given <code>long</code> value into specified JSON object.
     * @param name The value's name
     * @param value The <code>long</code> value
     * @param json The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to omit value
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final long value, final JSONObject json, final boolean condition) throws JSONException {
        // Large values of long must be written as string. See bug 11311.
        writeParameter(name, Long.toString(value), json, condition);
    }

    public static void writeParameter(final String name, final Long value, final JSONObject json, final boolean condition) throws JSONException {
        if (null == value) {
            writeNull(name, json, condition);
        } else {
            writeParameter(name, Long.toString(value.longValue()), json, condition);
        }
    }

    public static void writeParameter(String name, Integer value, JSONObject json, boolean condition) throws JSONException {
        if (null == value) {
            writeNull(name, json, condition);
        } else {
            writeParameter(name, value.intValue(), json, condition);
        }
    }

    /**
     * Puts given name-<code>Date</code>-pair into specified JSON object with respect to time zone
     * provided that <code>Date</code> value is not <code>null</code>
     *
     * @param name The value's name
     * @param value The <code>Date</code> value
     * @param offsetDate The offset <code>Date</code> value
     * @param timeZone The time zone
     * @param jsonObj The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to omit value
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final BigDecimal value, final JSONObject jsonObj, final boolean condition) throws JSONException {
        if (condition) {
            writeParameter(name, value, jsonObj);
        }
    }

    /**
     * Puts given name-{@link BigDecimal}-pair into specified JSON object provided that {@link BigDecimal} value is not <code>null</code>.
     *
     * @param name the value's name
     * @param value the {@link BigDecimal} value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    private static void writeParameter(final String name, final BigDecimal value, final JSONObject jsonObj) throws JSONException {
        if (null == value) {
            jsonObj.put(name, JSONObject.NULL);
        } else {
            jsonObj.put(name, value);
        }
    }

    /**
     * Puts given name-<code>boolean</code>-pair into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>boolean</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final boolean value, final JSONObject jsonObj) throws JSONException {
        jsonObj.put(name, value);
    }

    /**
     * Conditionally puts given <code>boolean</code> value into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>boolean</code> value
     * @param jsonObj The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to omit value
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final boolean value, final JSONObject jsonObj, final boolean condition) throws JSONException {
        if (condition) {
            jsonObj.put(name, value);
        }
    }

    /**
     * Puts given name-<code>Date</code>-pair into specified JSON object
     * provided that <code>Date</code> value is not <code>null</code>
     *
     * @param name The value's name
     * @param value The <code>Date</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final Date value, final JSONObject jsonObj) throws JSONException {
        if (value != null) {
            jsonObj.put(name, value.getTime());
        }
    }

    /**
     * Puts given name-<code>Date</code>-pair into specified JSON object with respect to time zone
     * provided that <code>Date</code> value is not <code>null</code>
     *
     * @param name The value's name
     * @param value The <code>Date</code> value
     * @param timeZone The time zone
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final Date value, final TimeZone timeZone, final JSONObject jsonObj) throws JSONException {
        writeParameter(name, value, value, timeZone, jsonObj);
    }

    static void writeParameter(final String name, final Date value, final TimeZone timeZone, final JSONObject json, final boolean condition) throws JSONException {
        if (condition) {
            writeParameter(name, value, timeZone, json);
        }
    }

    /**
     * Puts given name-<code>Date</code>-pair into specified JSON object with respect to time zone
     * provided that <code>Date</code> value is not <code>null</code>
     *
     * @param name The value's name
     * @param value The <code>Date</code> value
     * @param offsetDate The offset <code>Date</code> value
     * @param timeZone The time zone
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final Date value, final Date offsetDate, final TimeZone timeZone, final JSONObject jsonObj) throws JSONException {
        if (value != null) {
            jsonObj.put(name, value.getTime() + timeZone.getOffset(offsetDate.getTime()));
        }
    }

    /**
     * Puts given <code>String</code> value into specified JSON array
     * <p>
     * {@link JSONObject#NULL} is put into specified JSON array if either
     * <code>String</code> value is <code>null</code> or empty.
     *
     * @param value The <code>String</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final String value, final JSONArray jsonArray) {
        if (value != null && value.length() > 0) {
            jsonArray.put(value);
        } else {
            jsonArray.put(JSONObject.NULL);
        }
    }

    /**
     * Conditionally puts given <code>String</code> value into specified JSON
     * array. {@link JSONObject#NULL} is put into specified JSON array if either
     * <code>String</code> value is <code>null</code> or empty.
     * @param value The <code>String</code> value
     * @param jsonArray The JSON array to put into
     * @param condition conditionally write the value.
     */
    static void writeValue(final String value, final JSONArray jsonArray, final boolean condition) {
        if (condition) {
            writeValue(value, jsonArray);
        } else {
            jsonArray.put(JSONObject.NULL);
        }
    }

    /**
     * Puts given <code>int</code> value into specified JSON array
     *
     * @param value The <code>int</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final int value, final JSONArray jsonArray) {
        jsonArray.put(value);
    }

    /**
     * Conditionally puts given <code>int</code> value into specified JSON array
     *
     * @param value The <code>int</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     */
    public static void writeValue(final int value, final JSONArray jsonArray, final boolean condition) {
        if (condition) {
            jsonArray.put(value);
        } else {
            jsonArray.put(JSONObject.NULL);
        }
    }

    /**
     * Conditionally puts given {@link BigDecimal} value into specified JSON array.
     * @param value the {@link BigDecimal} value
     * @param jsonArray the JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     */
    public static void writeValue(final BigDecimal value, final JSONArray jsonArray, final boolean condition) {
        if (!condition || null == value) {
            jsonArray.put(JSONObject.NULL);
        } else {
            jsonArray.put(value);
        }
    }

    /**
     * Conditionally puts given <code>long</code> value into specified JSON array
     *
     * @param value The <code>long</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     */
    public static void writeValue(final long value, final JSONArray jsonArray, final boolean condition) {
        // Large values of long must be written as string. See bug 11311.
        writeValue(Long.toString(value), jsonArray, condition);
    }

    public static void writeValue(final Long value, final JSONArray json, final boolean condition) {
        // Large values of long must be written as string. See bug 11311.
        if (condition) {
            writeValue(Long.toString(value.longValue()), json);
        } else {
            writeNull(json);
        }
    }

    public static void writeValue(Integer value, JSONArray json, boolean condition) {
        if (condition) {
            writeValue(value.intValue(), json);
        } else {
            writeNull(json);
        }
    }

    protected static void writeNull(final JSONArray json) {
        json.put(JSONObject.NULL);
    }

    protected static void writeNull(final String name, final JSONObject json, final boolean condition) throws JSONException {
        if (condition) {
            json.put(name, JSONObject.NULL);
        }
    }

    /**
     * Puts given <code>boolean</code> value into specified JSON array
     *
     * @param value The <code>boolean</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final boolean value, final JSONArray jsonArray) {
        jsonArray.put(value);
    }

    /**
     * Conditionally puts given <code>boolean</code> value into specified JSON array
     *
     * @param value The <code>boolean</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     */
    public static void writeValue(final boolean value, final JSONArray jsonArray, final boolean condition) {
        if (condition) {
            jsonArray.put(value);
        } else {
            jsonArray.put(JSONObject.NULL);
        }
    }

    /**
     * Puts given <code>java.util.Date</code>'s time value into specified JSON array
     *
     * @param value The <code>Date</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final Date value, final JSONArray jsonArray) {
        if (value == null) {
            jsonArray.put(JSONObject.NULL);
        } else {
            jsonArray.put(value.getTime());
        }
    }

    public static void writeValue(final Date value, final JSONArray json, final boolean condition) {
        if (condition) {
            json.put(value.getTime());
        } else {
            json.put(JSONObject.NULL);
        }
    }

    /**
     * Puts given <code>JSONValue</code>'s time value into specified JSON array
     *
     * @param value The <code>JSON</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final JSONValue value, final JSONArray jsonArray) {
        if (value == null) {
            jsonArray.put(JSONObject.NULL);
        } else {
            jsonArray.put(value);
        }
    }

    /**
     * Conditionally puts given <code>JSONValue</code> value into specified JSON array
     *
     * @param value The <code>JSON</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     */
    public static void writeValue(final JSONValue value, final JSONArray json, final boolean condition) {
        if (condition) {
            json.put(value);
        } else {
            json.put(JSONObject.NULL);
        }
    }

    /**
     * Puts given <code>java.util.Date</code>'s time value into specified JSON array with respect to specified time zone
     *
     * @param value The <code>Date</code> value
     * @param timeZone The time zone
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final Date value, final TimeZone timeZone, final JSONArray jsonArray) {
        writeValue(value, value, timeZone, jsonArray);
    }

    static void writeValue(final Date value, final TimeZone timeZone, final JSONArray json, final boolean condition) {
        if (condition) {
            writeValue(value, timeZone, json);
        } else {
            json.put(JSONObject.NULL);
        }
    }

    /**
     * Puts given <code>java.util.Date</code>'s time value into specified JSON array with respect to specified time zone
     *
     * @param value The <code>Date</code> value
     * @param offsetDate The offset <code>Date</code> value
     * @param timeZone The time zone
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final Date value, final Date offsetDate, final TimeZone timeZone,
            final JSONArray jsonArray) {
        if (value == null) {
            jsonArray.put(JSONObject.NULL);
        } else {
            final int offset = timeZone.getOffset(offsetDate.getTime());
            jsonArray.put(value.getTime() + offset);
        }
    }

    /**
     * Puts {@link JSONObject#NULL} into specified JSON array
     *
     * @param jsonArray The JSON array to put {@link JSONObject#NULL} into
     */
    public static void writeValueNull(final JSONArray jsonArray) {
        jsonArray.put(JSONObject.NULL);
    }

    protected void writeFields(final DataObject obj, final TimeZone tz, final JSONObject json, final Session session) throws JSONException {
        final WriterProcedure<DataObject> procedure = new WriterProcedure<DataObject>(obj, json, tz, session);
        if (!WRITER_MAP.forEachValue(procedure)) {
            final JSONException je = procedure.getError();
            if (null != je) {
                throw je;
            }
        }
    }

    protected boolean writeField(final DataObject obj, final int column, final TimeZone tz, final JSONArray json, final Session session) throws JSONException {
        final FieldWriter<DataObject> writer = WRITER_MAP.get(column);
        if (null == writer) {
            return false;
        }
        writer.write(obj, tz, json, session);
        return true;
    }

    protected static interface FieldWriter<T> {

        /**
         * Writes specified value to given JSON array.
         *
         * @param obj The value to write
         * @param timeZone The time zone
         * @param json The JSON array
         * @param session The associated session
         *
         * @throws JSONException If writing to JSON array fails
         */
        void write(T obj, TimeZone timeZone, JSONArray json, Session session) throws JSONException;

        /**
         * Writes specified value to given JSON object.
         *
         * @param obj The value to write
         * @param timeZone The time zone
         * @param json The JSON array
         * @param session The associated session
         * @throws JSONException If writing to JSON object fails
         */
        void write(T obj, TimeZone timeZone, JSONObject json, Session session) throws JSONException;
    }

    private static final FieldWriter<DataObject> OBJECT_ID_WRITER = new FieldWriter<DataObject>() {
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) {
            writeValue(obj.getObjectID(), json, obj.containsObjectID());
        }
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            writeParameter(DataFields.ID, obj.getObjectID(), json, obj.containsObjectID());
        }
    };

    private static final FieldWriter<DataObject> CREATED_BY_WRITER = new FieldWriter<DataObject>() {
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) {
            writeValue(obj.getCreatedBy(), json, obj.containsCreatedBy());
        }
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            writeParameter(DataFields.CREATED_BY, obj.getCreatedBy(), json, obj.containsCreatedBy());
        }
    };

    private static final FieldWriter<DataObject> CREATION_DATE_WRITER = new FieldWriter<DataObject>() {
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) {
            writeValue(obj.getCreationDate(), timeZone, json, obj.containsCreationDate());
        }
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            writeParameter(DataFields.CREATION_DATE, obj.getCreationDate(), timeZone, json);
        }
    };

    private static final FieldWriter<DataObject> MODIFIED_BY_WRITER = new FieldWriter<DataObject>() {
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) {
            writeValue(obj.getModifiedBy(), json, obj.containsModifiedBy());
        }
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            writeParameter(DataFields.MODIFIED_BY, obj.getModifiedBy(), json, obj.containsModifiedBy());
        }
    };

    private static final FieldWriter<DataObject> LAST_MODIFIED_WRITER = new FieldWriter<DataObject>() {
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) {
            writeValue(obj.getLastModified(), timeZone, json, obj.containsLastModified());
        }
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            writeParameter(DataFields.LAST_MODIFIED, obj.getLastModified(), timeZone, json, obj.containsLastModified());
        }
    };

    private static final FieldWriter<DataObject> LAST_MODIFIED_UTC_WRITER = new FieldWriter<DataObject>() {
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) {
            writeValue(obj.getLastModified(), UTC, json, obj.containsLastModified());
        }
        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            writeParameter(DataFields.LAST_MODIFIED_UTC, obj.getLastModified(), UTC, json, obj.containsLastModified());
        }
    };

    private static final FieldWriter<DataObject> META_WRITER = new FieldWriter<DataObject>() {

        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONArray json, final Session session) throws JSONException {
            // Get meta map
            Map<String, Object> map = obj.getMap();

            // Write meta map
            if (null == map || map.isEmpty()) {
                writeValue((String) null, json, false);
            } else {
                writeValue((JSONValue) JSONCoercion.coerceToJSON(map), json, true);
            }
        }

        @Override
        public void write(final DataObject obj, final TimeZone timeZone, final JSONObject json, final Session session) throws JSONException {
            // Get meta map
            Map<String, Object> map = obj.getMap();

            // Write meta map
            if (null != map && !map.isEmpty()) {
                writeParameter(DataFields.META, (JSONValue) JSONCoercion.coerceToJSON(map), json, true);
            }
        }
    };

    static {
        final TIntObjectMap<FieldWriter<DataObject>> m = new TIntObjectHashMap<FieldWriter<DataObject>>(8, 1);
        m.put(DataObject.OBJECT_ID, OBJECT_ID_WRITER);
        m.put(DataObject.CREATED_BY, CREATED_BY_WRITER);
        m.put(DataObject.CREATION_DATE, CREATION_DATE_WRITER);
        m.put(DataObject.MODIFIED_BY, MODIFIED_BY_WRITER);
        m.put(DataObject.LAST_MODIFIED, LAST_MODIFIED_WRITER);
        m.put(DataObject.LAST_MODIFIED_UTC, LAST_MODIFIED_UTC_WRITER);
        m.put(DataObject.META, META_WRITER);
        WRITER_MAP = m;
    }

    private static final TIntObjectMap<FieldWriter<DataObject>> WRITER_MAP;

}
