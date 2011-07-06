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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import static com.openexchange.ajax.fields.ResponseFields.DATA;
import static com.openexchange.ajax.fields.ResponseFields.ERROR;
import static com.openexchange.ajax.fields.ResponseFields.ERROR_CATEGORY;
import static com.openexchange.ajax.fields.ResponseFields.ERROR_CODE;
import static com.openexchange.ajax.fields.ResponseFields.ERROR_ID;
import static com.openexchange.ajax.fields.ResponseFields.ERROR_PARAMS;
import static com.openexchange.ajax.fields.ResponseFields.PROBLEMATIC;
import static com.openexchange.ajax.fields.ResponseFields.TIMESTAMP;
import static com.openexchange.ajax.fields.ResponseFields.TRUNCATED;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.fields.ResponseFields.ParsingFields;
import com.openexchange.ajax.fields.ResponseFields.TruncatedFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.AbstractOXException.Parsing;
import com.openexchange.groupware.AbstractOXException.ProblematicAttribute;
import com.openexchange.groupware.AbstractOXException.Truncated;

/**
 * JSON writer for the response container objekt.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ResponseWriter {

    private ResponseWriter() {
        super();
    }

    public static JSONObject getJSON(final Response response) throws JSONException {
        final JSONObject json = new JSONObject();
        write(response, json);
        return json;
    }

    private static final String ERR = "[Not available]";

    public static void write(final Response response, final JSONObject json) throws JSONException {
        /*-
         * TODO: Also check for
         *       "JSONObject.NULL.equals(response.getData())"
         *       when performing null comparison?
         */
        if (null != response.getData()) {
            json.put(DATA, response.getData());
        }
        if (null != response.getTimestamp()) {
            json.put(TIMESTAMP, response.getTimestamp().getTime());
        }
        if (null != response.getException()) {
            final AbstractOXException exception = response.getException();
            addException(json, exception,response.hasWarning());
        }
    }

    public static void addException(final JSONObject json, final OXException exception, final boolean isWarning) throws JSONException {
        json.put(ERROR, null == exception.getOrigMessage() ? ERR : exception.getOrigMessage());
        if (exception.getMessageArgs() != null) {
            final JSONArray array = new JSONArray();
            for (final Object tmp : exception.getMessageArgs()) {
                array.put(tmp);
            }
            json.put(ERROR_PARAMS, array);
        }
        json.put(ERROR_CATEGORY, isWarning ? Category.WARNING.getCode() : exception.getCategory()
                .getCode());
        json.put(ERROR_CODE, exception.getErrorCode());
        json.put(ERROR_ID, exception.getExceptionID());
        toJSON(json, exception.getProblematics());
        if (Category.TRUNCATED == exception.getCategory()) {
            addTruncated(json, exception.getProblematics());
        }
    }

    private static void toJSON(final JSONObject json, final ProblematicAttribute[] problematics) throws JSONException {
        final JSONArray array = new JSONArray();
        for (final ProblematicAttribute problematic : problematics) {
            array.put(toJSON(problematic));
        }
        if (array.length() > 0) {
            json.put(PROBLEMATIC, array);
        }
    }

    public static JSONObject toJSON(final ProblematicAttribute problematic) throws JSONException {
        if (problematic instanceof Truncated) {
            return toJSON((Truncated) problematic);
        } else if (problematic instanceof Parsing) {
            return toJSON((Parsing) problematic);
        }
        return new JSONObject();
    }

    public static JSONObject toJSON(final Truncated truncated) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(TruncatedFields.ID, truncated.getId());
        json.put(TruncatedFields.LENGTH, truncated.getLength());
        json.put(TruncatedFields.MAX_SIZE, truncated.getMaxSize());
        return json;
    }

    public static JSONObject toJSON(final Parsing parsing) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(ParsingFields.NAME, parsing.getAttribute());
        return json;
    }

    /**
     * This method adds the old truncated ids.
     */
    private static void addTruncated(final JSONObject json, final ProblematicAttribute[] problematics)
            throws JSONException {
        final JSONArray array = new JSONArray();
        for (final ProblematicAttribute problematic : problematics) {
            if (Truncated.class.isAssignableFrom(problematic.getClass())) {
                array.put(((Truncated) problematic).getId());
            }
        }
        json.put(TRUNCATED, array);
    }

    /**
     * Serializes a Response object to given instance of <code>{@link JSONWriter}</code> .
     * 
     * @param response - the <code>{@link Response}</code> object to serialize.
     * @param writer - the <code>{@link JSONWriter}</code> to write to
     * @throws JSONException - if writing fails
     */
    public static void write(final Response response, final JSONWriter writer) throws JSONException {
        writer.object();
        final JSONObject json = new JSONObject();
        write(response, json);
        final Set<Map.Entry<String, Object>> entrySet = json.entrySet();
        final int len = entrySet.size();
        final Iterator<Map.Entry<String, Object>> iter = entrySet.iterator();
        for (int i = 0; i < len; i++) {
            final Map.Entry<String, Object> e = iter.next();
            writer.key(e.getKey()).value(e.getValue());
        }
        writer.endObject();
    }

    /**
     * Serializes a Response object to the writer.
     * 
     * @param response Response object to serialize.
     * @param writer the serialized object will be written to this writer.
     * @throws JSONException if writing fails.
     * @throws IOException If an I/O error occurs during writing
     */
    public static void write(final Response response, final Writer writer) throws JSONException, IOException {
        final JSONObject json = new JSONObject();
        ResponseWriter.write(response, json);
        try {
            json.write(writer);
        } catch (final JSONException e) {
            if (e.getCause() instanceof IOException) {
                /*
                 * Throw proper I/O error since a serious socket error could
                 * been occurred which prevents further communication. Just
                 * throwing a JSON error possibly hides this fact by trying to
                 * write to/read from a broken socket connection.
                 */
                throw (IOException) e.getCause();
            }
            /*
             * Just re-throw JSON error probably caused by a JSON syntax error.
             */
            throw e;
        }
    }

    /**
     * Writes given instance of <code>AbstractOXException</code> into given instance of <code>JSONWriter</code> assuming that writer's mode
     * is already set to writing a JSON object
     * 
     * @param exc - the exception to write
     * @param writer - the writer to write to
     * @throws JSONException - if writing fails
     */
    public static void writeException(final AbstractOXException exc, final JSONWriter writer) throws JSONException {
        writer.key(ResponseFields.ERROR).value(exc.getOrigMessage());
        if (exc.getMessageArgs() != null) {
            final JSONArray array = new JSONArray();
            for (final Object tmp : exc.getMessageArgs()) {
                array.put(tmp);
            }
            writer.key(ResponseFields.ERROR_PARAMS).value(array);
        }
        writer.key(ResponseFields.ERROR_CATEGORY).value(exc.getCategory().getCode());
        writer.key(ResponseFields.ERROR_CODE).value(exc.getErrorCode());
        writer.key(ResponseFields.ERROR_ID).value(exc.getExceptionID());
        writeProblematic(exc, writer);
        writeTruncated(exc, writer);
    }

    private static void writeProblematic(final AbstractOXException exc, final JSONWriter writer) throws JSONException {
        final ProblematicAttribute[] problematics = exc.getProblematics();
        if (problematics.length > 0) {
            writer.key(PROBLEMATIC);
            writer.array();
            for (final ProblematicAttribute problematic : problematics) {
                writer.value(toJSON(problematic));
            }
            writer.endArray();
        }
    }

    private static void writeTruncated(final AbstractOXException exc, final JSONWriter writer) throws JSONException {
        final ProblematicAttribute[] problematics = exc.getProblematics();
        if (problematics.length > 0) {
            final JSONArray array = new JSONArray();
            for (final ProblematicAttribute problematic : problematics) {
                if (problematic instanceof Truncated) {
                    array.put(((Truncated) problematic).getId());
                }
            }
            writer.key(ResponseFields.TRUNCATED).value(array);
        }
    }

    /**
     * Writes given instance of <code>AbstractOXException</code> as a warning into given instance of <code>JSONWriter</code> assuming that
     * writer's mode is already set to writing a JSON object
     * 
     * @param warning - the warning to write
     * @param writer - the writer to write to
     * @throws JSONException - if writing fails
     */
    public static void writeWarning(final AbstractOXException warning, final JSONWriter writer) throws JSONException {
        writer.key(ResponseFields.ERROR).value(warning.getOrigMessage());
        if (warning.getMessageArgs() != null) {
            final JSONArray array = new JSONArray();
            for (final Object tmp : warning.getMessageArgs()) {
                array.put(tmp);
            }
            writer.key(ResponseFields.ERROR_PARAMS).value(array);
        }
        writer.key(ResponseFields.ERROR_CATEGORY).value(Category.WARNING.getCode());
        writer.key(ResponseFields.ERROR_CODE).value(warning.getErrorCode());
        writer.key(ResponseFields.ERROR_ID).value(warning.getExceptionID());
        writeProblematic(warning, writer);
        writeTruncated(warning, writer);
    }
}
