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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * {@link DataWriter} - Base class for all writers used throughout modules.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DataWriter {

    protected TimeZone timeZone;

    protected JSONWriter jsonwriter;

    private static final DecimalFormat floatFormat =
        new DecimalFormat("#######0.##", new DecimalFormatSymbols(Locale.ENGLISH));

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

    public void writeParameter(final String name, final int value) throws JSONException {
        jsonwriter.key(name);
        jsonwriter.value(value);
    }

    public void writeParameter(final String name, final long value) throws JSONException {
        jsonwriter.key(name);
        jsonwriter.value(value);
    }

    public void writeParameter(final String name, final boolean value) throws JSONException {
        jsonwriter.key(name);
        jsonwriter.value(value);
    }

    public void writeParameter(final String name, final Date value) throws JSONException {
        if (value != null) {
            jsonwriter.key(name);
            jsonwriter.value(value.getTime());
        }
    }

    public void writeParameter(final String name, final Date value, final TimeZone timeZone) throws JSONException {
        writeParameter(name, value, value, timeZone);
    }

    public void writeParameter(final String name, final Date value, final Date offsetDate, final TimeZone timeZone)
            throws JSONException {
        if (value != null) {
            jsonwriter.key(name);
            final int offset = timeZone.getOffset(offsetDate.getTime());
            jsonwriter.value(value.getTime() + offset);
        }
    }

    public void writeParameter(final String name, final byte[] value) throws JSONException {
        if (value != null) {
            jsonwriter.key(name);
            jsonwriter.value(new String(value));
        }
    }

    public void writeValue(final String value) throws JSONException {
        if (value != null && value.length() > 0) {
            jsonwriter.value(value);
        } else {
            jsonwriter.value(JSONObject.NULL);
        }
    }

    public void writeValue(final int value) throws JSONException {
        jsonwriter.value(value);
    }

    public void writeValue(final long value) throws JSONException {
        jsonwriter.value(value);
    }

    public void writeValue(final boolean value) throws JSONException {
        jsonwriter.value(value);
    }

    public void writeValue(final Date value) throws JSONException {
        if (value == null) {
            jsonwriter.value(JSONObject.NULL);
        } else {
            jsonwriter.value(value.getTime());
        }
    }

    public void writeValue(final Date value, final TimeZone timeZone) throws JSONException {
        writeValue(value, value, timeZone);
    }

    public void writeValue(final Date value, final Date offsetDate, final TimeZone timeZone) throws JSONException {
        final int offset = timeZone.getOffset(offsetDate.getTime());
        if (value == null) {
            jsonwriter.value(JSONObject.NULL);
        } else {
            jsonwriter.value(value.getTime() + offset);
        }
    }

    public void writeValue(final byte[] value) throws JSONException {
        if (value == null) {
            jsonwriter.value(JSONObject.NULL);
        } else {
            jsonwriter.value(new String(value));
        }
    }

    // new implementation

    /**
     * Puts given name-<code>String</code>-pair into specified JSON object
     * provided that <code>String</code> value is not <code>null</code> and not
     * empty
     *
     * @param name
     *            The name to which the value is bound
     * @param value
     *            The <code>String</code> value
     * @param jsonObj
     *            The JSON object to put into
     * @throws JSONException
     *             If putting into JSON object fails
     */
    public static void writeParameter(final String name, final String value,
        final JSONObject jsonObj) throws JSONException {
        if (value != null && value.length() > 0) {
            jsonObj.put(name, value);
        }
    }

    /**
     * Conditionally puts given name-<code>String</code>-pair into specified
     * JSON object provided that <code>String</code> value is not
     * <code>null</code> and not empty.
     * @param name The name to which the value is bound
     * @param value The <code>String</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final String value,
        final JSONObject jsonObj, final boolean condition) throws JSONException {
        if (condition) {
            writeParameter(name, value, jsonObj);
        }
    }

    /**
     * Puts given name-<code>int</code>-pair into specified JSON object
     *
     * @param name The name to which the value is bound
     * @param value The <code>int</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final int value,
        final JSONObject jsonObj) throws JSONException {
        jsonObj.put(name, value);
    }

    /**
     * Conditionally puts given <code>int</code> value into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>int</code> value
     * @param jsonObj The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to omit value
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final int value,
        final JSONObject jsonObj, final boolean condition) throws JSONException {
        if (condition) {
            writeParameter(name, value, jsonObj);
        }
    }

    /**
     * Puts given name-<code>long</code>-pair into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>long</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final long value,
        final JSONObject jsonObj) throws JSONException {
        // Large values of long must be written as string. See bug 11311.
        writeParameter(name, String.valueOf(value), jsonObj);
    }

    /**
     * Conditionally puts given <code>long</code> value into specified JSON
     * object.
     * @param name The value's name
     * @param value The <code>long</code> value
     * @param jsonObj The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code>
     * to omit value
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final long value,
        final JSONObject jsonObj, final boolean condition) throws JSONException {
        // Large values of long must be written as string. See bug 11311.
        writeParameter(name, String.valueOf(value), jsonObj, condition);
    }

    /**
     * Conditionally puts given <code>float</code> value into specified JSON
     * object.
     * @param name The value's name
     * @param value The <code>float</code> value
     * @param jsonObj The JSON object to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to omit value
     * @throws JSONException If a JSON error occurs
     */
    public static void writeParameter(final String name, final float value,
        final JSONObject jsonObj, final boolean condition) throws JSONException {
        // Floats must be written as strings.
        writeParameter(name, floatFormat.format(value), jsonObj, condition);
    }

    /**
     * Puts given name-<code>boolean</code>-pair into specified JSON object
     *
     * @param name The value's name
     * @param value The <code>boolean</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final boolean value, final JSONObject jsonObj)
            throws JSONException {
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
    public static void writeParameter(final String name, final boolean value, final JSONObject jsonObj,
            final boolean condition) throws JSONException {
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
    public static void writeParameter(final String name, final Date value, final JSONObject jsonObj)
            throws JSONException {
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
    public static void writeParameter(final String name, final Date value, final TimeZone timeZone,
            final JSONObject jsonObj) throws JSONException {
        writeParameter(name, value, value, timeZone, jsonObj);
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
    public static void writeParameter(final String name, final Date value, final Date offsetDate,
            final TimeZone timeZone, final JSONObject jsonObj) throws JSONException {
        if (value != null) {
            jsonObj.put(name, value.getTime() + timeZone.getOffset(offsetDate.getTime()));
        }
    }

    /**
     * Puts given name-<code>byte[]</code>-pair into specified JSON object
     * provided that <code>byte[]</code> value is not <code>null</code>
     *
     * @param name The value's name
     * @param value The <code>byte[]</code> value
     * @param jsonObj The JSON object to put into
     * @throws JSONException If putting into JSON object fails
     */
    public static void writeParameter(final String name, final byte[] value, final JSONObject jsonObj)
            throws JSONException {
        if (value != null) {
            jsonObj.put(name, new String(value));
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
    public static void writeValue(final String value, final JSONArray jsonArray,
        final boolean condition) {
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
     * Conditionally puts given <code>float</code> value into specified JSON
     * array.
     * @param value The <code>float</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code>
     * to put {@link JSONObject#NULL}
     */
    public static void writeValue(final float value, final JSONArray jsonArray,
        final boolean condition) {
        // Floats must be written as strings
        writeValue(floatFormat.format(value), jsonArray, condition);
    }

    /**
     * Puts given <code>long</code> value into specified JSON array
     *
     * @param value The <code>long</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final long value, final JSONArray jsonArray) {
        // Large values of long must be written as string. See bug 11311.
        writeValue(String.valueOf(value), jsonArray);
    }

    /**
     * Conditionally puts given <code>long</code> value into specified JSON array
     *
     * @param value The <code>long</code> value
     * @param jsonArray The JSON array to put into
     * @param condition <code>true</code> to put; otherwise <code>false</code> to put {@link JSONObject#NULL}
     */
    public static void writeValue(final long value, final JSONArray jsonArray,
        final boolean condition) {
        // Large values of long must be written as string. See bug 11311.
        writeValue(String.valueOf(value), jsonArray, condition);
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
     * Puts given <code>java.util.Date</code>'s time value into specified JSON array with respect to specified time zone
     *
     * @param value The <code>Date</code> value
     * @param timeZone The time zone
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final Date value, final TimeZone timeZone, final JSONArray jsonArray) {
        writeValue(value, value, timeZone, jsonArray);
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
     * Puts given <code>byte[]</code> value into specified JSON array
     *
     * @param value The <code>Date</code> value
     * @param jsonArray The JSON array to put into
     */
    public static void writeValue(final byte[] value, final JSONArray jsonArray) {
        if (value == null) {
            jsonArray.put(JSONObject.NULL);
        } else {
            jsonArray.put(new String(value));
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
}
