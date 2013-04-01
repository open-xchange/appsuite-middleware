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

package com.openexchange.ajax.writer;

import static com.openexchange.ajax.fields.ResponseFields.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.fields.ResponseFields.ParsingFields;
import com.openexchange.ajax.fields.ResponseFields.TruncatedFields;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.Categories;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Parsing;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.log.Log;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * JSON writer for the response container object.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ResponseWriter {

    /**
     * A set of reserved identifiers.
     */
    private static final Set<String> RESERVED_IDENTIFIERS = ResponseFields.RESERVED_IDENTIFIERS;

    private static volatile Locale defaultLocale;
    /**
     * The default locale.
     */
    private static Locale defaultLocale() {
        Locale tmp = defaultLocale;
        if (null == tmp) {
            synchronized (ResponseWriter.class) {
                tmp = defaultLocale;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        tmp = Locale.US;
                    } else {
                        final String property = service.getProperty("com.openexchange.i18n.defaultLocale");
                        tmp = null == property ? Locale.US : LocaleTools.getLocale(property);
                        if (null == tmp) {
                            tmp = Locale.US;
                        }
                    }
                    defaultLocale = tmp;
                }
            }
        }
        return tmp;
    }

    private ResponseWriter() {
        super();
    }

    private static volatile Boolean includeStackTraceOnError;

    private static boolean includeStackTraceOnError() {
        Boolean b = includeStackTraceOnError;
        if (null == b) {
            synchronized (ResponseWriter.class) {
                b = includeStackTraceOnError;
                if (null == b) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return false;
                    }
                    b = Boolean.valueOf(service.getBoolProperty("com.openexchange.ajax.response.includeStackTraceOnError", false));
                    includeStackTraceOnError = b;
                }
            }
        }
        return b.booleanValue();
    }

    /**
     * Gets the JSON object resulting from specified response using default locale.
     *
     * @param response The response
     * @return The JSON object
     * @throws JSONException If writing JSON fails
     */
    public static JSONObject getJSON(final Response response) throws JSONException {
        final JSONObject json = new JSONObject(8);
        write(response, json, defaultLocale());
        return json;
    }

    /**
     * Gets the JSON object resulting from specified response using given locale.
     *
     * @param response The response
     * @param locale The locale
     * @return The JSON object
     * @throws JSONException If writing JSON fails
     */
    public static JSONObject getJSON(final Response response, final Locale locale) throws JSONException {
        final JSONObject json = new JSONObject(8);
        write(response, json, locale);
        return json;
    }

    /**
     * Writes specified response to given JSON object using default locale.
     *
     * @param response The response to write
     * @param json The JOSN object to write to
     * @throws JSONException If writing JSON fails
     */
    public static void write(final Response response, final JSONObject json) throws JSONException {
        write(response, json, defaultLocale());
    }

    /**
     * Writes specified response to given JSON object using passed locale.
     *
     * @param response The response to write
     * @param json The JOSN object to write to
     * @param locale The locale
     * @throws JSONException If writing JSON fails
     */
    public static void write(final Response response, final JSONObject json, final Locale locale) throws JSONException {
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
        final List<OXException> warnings = response.getWarnings();
        final OXException exception = response.getException();
        if (null == exception) {
            /*
             * Any warning available? Set first warning as "exception" for compatibility reasons
             */
            if (null != warnings && !warnings.isEmpty()) {
                addException(json, warnings.get(0), locale, response.includeStackTraceOnError());
            }
        } else {
            addException(json, exception, locale, response.includeStackTraceOnError());
        }
        /*
         * Add warnings
         */
        addWarnings(json, warnings, locale, response.includeStackTraceOnError());
        /*
         * Add properties
         */
        addProperties(json, response.getProperties());
    }

    /**
     * Writes given properties to specified JSON object.
     *
     * @param json The JSON object
     * @param properties The properties
     * @throws JSONException If writing JSON fails
     */
    public static void addProperties(final JSONObject json, final Map<String, Object> properties) throws JSONException {
        if (null == properties || properties.isEmpty()) {
            return;
        }
        for (final Entry<String, Object> entry : properties.entrySet()) {
            final String name = entry.getKey();
            if (null != name && !RESERVED_IDENTIFIERS.contains(name)) {
                final Object value = entry.getValue();
                if (null != value) {
                    json.put(name, value);
                }
            } else {
                Log.loggerFor(ResponseWriter.class).warn("Response property discarded. Illegal property name: " + name == null ? "null" : name);
            }
        }
    }

    /**
     * Writes specified warning to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param warning The warning
     * @param locale The locale
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addWarning(final JSONObject json, final OXException warning, final Locale locale) throws JSONException {
        if (null == warning) {
            return;
        }
        final JSONObject jsonWarning = new JSONObject(8);
        addException(jsonWarning, warning.setCategory(Category.CATEGORY_WARNING), locale);
        json.put(WARNINGS, jsonWarning);
    }

    /**
     * Writes specified warnings to given JSON object using default locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param warnings The warnings
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addWarnings(final JSONObject json, final List<OXException> warnings) throws JSONException {
        addWarnings(json, warnings, defaultLocale(), false);
    }

    /**
     * Writes specified warnings to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param warnings The warnings
     * @param locale The locale
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addWarnings(final JSONObject json, final List<OXException> warnings, final Locale locale) throws JSONException {
        addWarnings(json, warnings, locale, false);
    }

    /**
     * Writes specified warnings to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param warnings The warnings
     * @param locale The locale
     * @param includeStackTraceOnError <code>true</code> to append stack trace elements to JSON object; otherwise <code>false</code>
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addWarnings(final JSONObject json, final List<OXException> warnings, final Locale locale, final boolean includeStackTraceOnError) throws JSONException {
        if (null == warnings || warnings.isEmpty()) {
            return;
        }
        if (1 == warnings.size()) {
            final JSONObject jsonWarning = new JSONObject(8);
            final OXException warning = warnings.get(0).setCategory(Category.CATEGORY_WARNING);
            addException(jsonWarning, warning, locale);
            json.put(WARNINGS, jsonWarning);
            // Check if error has already been set
            if (!json.hasAndNotNull(ERROR)) {
                addException(json, warning, locale, includeStackTraceOnError);
            }
        } else {
            final JSONArray jsonArray = new JSONArray(warnings.size());
            for (final OXException warning : warnings) {
                final JSONObject jsonWarning = new JSONObject();
                addException(jsonWarning, warning.setCategory(Category.CATEGORY_WARNING), locale);
                jsonArray.put(jsonWarning);
            }
            json.put(WARNINGS, jsonArray);
            if (!warnings.isEmpty() && !json.hasAndNotNull(ERROR)) {
                addException(json, warnings.get(0).setCategory(Category.CATEGORY_WARNING), locale, includeStackTraceOnError);
            }
        }
    }

    /**
     * Writes specified exception to given JSON object using default locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param exception The exception to write
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addException(final JSONObject json, final OXException exception) throws JSONException {
        addException(json, exception, defaultLocale());
    }

    /**
     * Writes specified exception to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param errorKey The key value for the error value inside the JSON object
     * @param exception The exception to write
     * @param locale The locale
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addException(final JSONObject json, final OXException exception, final Locale locale) throws JSONException {
        addException(json, ERROR, exception, locale, false);
    }

    /**
     * Writes specified exception to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param errorKey The key value for the error value inside the JSON object
     * @param exception The exception to write
     * @param locale The locale
     * @param includeStackTraceOnError <code>true</code> to append stack trace elements to JSON object; otherwise <code>false</code>
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addException(final JSONObject json, final OXException exception, final Locale locale, final boolean includeStackTraceOnError) throws JSONException {
        addException(json, ERROR, exception, locale, includeStackTraceOnError);
    }

    /**
     * Writes specified exception to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param errorKey The key value for the error value inside the JSON object
     * @param exception The exception to write
     * @param locale The locale
     * @param includeStackTraceOnError <code>true</code> to append stack trace elements to JSON object; otherwise <code>false</code>
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addException(final JSONObject json, String errorKey, final OXException exception, final Locale locale, final boolean includeStackTraceOnError) throws JSONException {
        final Locale l;
        {
            final String property = exception.getProperty(OXExceptionConstants.PROPERTY_LOCALE);
            if (null == property) {
                l = LocaleTools.getSaneLocale(locale);
            } else {
                final Locale parsedLocale = LocaleTools.getLocale(property);
                l = null == parsedLocale ? LocaleTools.getSaneLocale(locale) : parsedLocale;
            }
        }
        json.put(errorKey, exception.getDisplayMessage(l));
        /*
         * Put argument JSON array for compatibility reasons
         */
        {
            Object[] args = exception.getLogArgs();
            if ((null == args) || (0 == args.length)) {
                args = exception.getDisplayArgs();
            }
            // Enforce first condition; review later on
            if ((null == args) || (0 == args.length)) {
                json.put(ERROR_PARAMS, new JSONArray(0));
            } else {
                final JSONArray jArray = new JSONArray(args.length);
                for (final Object arg : args) {
                    jArray.put(arg);
                }
                json.put(ERROR_PARAMS, jArray);
            }
        }
        /*
         * Categories
         */
        {
            final List<Category> categories = exception.getCategories();
            if (1 == categories.size()) {
                json.put(ERROR_CATEGORIES, categories.get(0).toString());
            } else {
                final JSONArray jArray = new JSONArray(categories.size());
                for (final Category category : categories) {
                    jArray.put(category.toString());
                }
                json.put(ERROR_CATEGORIES, jArray);
            }
            // For compatibility
            if (!categories.isEmpty()) {
                final int number = Categories.getFormerCategoryNumber(categories.get(0));
                if (number > 0) {
                    json.put(ERROR_CATEGORY, number);
                }
            }
        }
        json.put(ERROR_CODE, exception.getErrorCode());
        json.put(ERROR_ID, exception.getExceptionId());
        toJSON(json, exception.getProblematics());
        if (Category.CATEGORY_TRUNCATED.equals(exception.getCategory())) {
            addTruncated(json, exception.getProblematics());
        }
        if (includeStackTraceOnError || includeStackTraceOnError()) {
            // Write exception
            StackTraceElement[] traceElements = exception.getStackTrace();
            final JSONArray jsonStack = new JSONArray(traceElements.length << 1);
            jsonStack.put(exception.getSoleMessage());
            Throwable cause = exception;
            final StringBuilder tmp = new StringBuilder(64);
            while (null != traceElements && traceElements.length > 0) {
                for (final StackTraceElement stackTraceElement : traceElements) {
                    tmp.setLength(0);
                    writeElementTo(stackTraceElement, tmp);
                    jsonStack.put(tmp.toString());
                }
                cause = cause.getCause();
                if (null == cause) {
                    traceElements = null;
                } else {
                    tmp.setLength(0);
                    jsonStack.put(tmp.append("Caused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage()).toString());
                    traceElements = cause.getStackTrace();
                }
            }
            json.put(ERROR_STACK, jsonStack);
        }
    }

    private static void writeElementTo(final StackTraceElement element, final StringBuilder sb) {
        sb.append(element.getClassName()).append('.').append(element.getMethodName());
        if (element.isNativeMethod()) {
            sb.append("(Native Method)");
        } else {
            final String fileName = element.getFileName();
            if (null == fileName) {
                sb.append("(Unknown Source)");
            } else {
                sb.append('(').append(fileName);
                final int lineNumber = element.getLineNumber();
                if (lineNumber >= 0) {
                    sb.append(':').append(lineNumber);
                }
                sb.append(')');
            }
        }
    }

    private static void toJSON(final JSONObject json, final ProblematicAttribute[] problematics) throws JSONException {
        final JSONArray array = new JSONArray(problematics.length);
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
        final JSONObject json = new JSONObject(3);
        json.put(TruncatedFields.ID, truncated.getId());
        json.put(TruncatedFields.LENGTH, truncated.getLength());
        json.put(TruncatedFields.MAX_SIZE, truncated.getMaxSize());
        return json;
    }

    public static JSONObject toJSON(final Parsing parsing) throws JSONException {
        final JSONObject json = new JSONObject(1);
        json.put(ParsingFields.NAME, parsing.getAttribute());
        return json;
    }

    /**
     * This method adds the old truncated ids.
     */
    private static void addTruncated(final JSONObject json, final ProblematicAttribute[] problematics) throws JSONException {
        final JSONArray array = new JSONArray(problematics.length);
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
    public static void write(final Response response, final JSONWriter writer, final Locale locale) throws JSONException {
        writer.object();
        final JSONObject json = new JSONObject(8);
        write(response, json, locale);
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
        final Locale locale = response.getLocale();
        write(response, writer, locale == null ? defaultLocale() : locale);
    }

    /**
     * Serializes a Response object to the writer.
     *
     * @param response Response object to serialize.
     * @param writer the serialized object will be written to this writer.
     * @param locale The locale
     * @throws JSONException if writing fails.
     * @throws IOException If an I/O error occurs during writing
     */
    public static void write(final Response response, final Writer writer, final Locale locale) throws JSONException, IOException {
        write(response, writer, locale, false);
    }

    /**
     * Serializes a Response object to the writer.
     *
     * @param response Response object to serialize.
     * @param writer the serialized object will be written to this writer.
     * @param locale The locale
     * @param asciiOnly <code>true</code> to only write ASCII characters; otherwise <code>false</code>
     * @throws JSONException if writing fails.
     * @throws IOException If an I/O error occurs during writing
     */
    public static void write(final Response response, final Writer writer, final Locale locale, final boolean asciiOnly) throws JSONException, IOException {
        final JSONObject json = new JSONObject();
        ResponseWriter.write(response, json, locale);
        try {
            json.write(writer, asciiOnly);
        } catch (final JSONException e) {
            if (e.getCause() instanceof IOException) {
                /*
                 * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                 * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
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
     * Writes specified warnings to {@link JSONWriter} instance using default locale.
     *
     * @param warnings The warnings
     * @param writer The JSON writer
     * @throws JSONException If writing JSON fails
     */
    public static void writeWarnings(final List<OXException> warnings, final JSONWriter writer) throws JSONException {
        writeWarnings(warnings, writer, defaultLocale());
    }

    /**
     * Writes specified warnings to {@link JSONWriter} instance.
     *
     * @param warnings The warnings
     * @param writer The JSON writer
     * @param locale The locale
     * @throws JSONException If writing JSON fails
     */
    public static void writeWarnings(final List<OXException> warnings, final JSONWriter writer, final Locale locale) throws JSONException {
        if (null == warnings || warnings.isEmpty()) {
            return;
        }
        writer.key(WARNINGS);
        if (1 == warnings.size()) {
            final OXException warning = warnings.get(0);
            writer.object();
            try {
                writeException(warning/*.setCategory(Category.CATEGORY_WARNING)*/, writer, locale);
            } finally {
                writer.endObject();
            }
            if (writer instanceof OXJSONWriter) {
                final JSONValue jv = ((OXJSONWriter) writer).getObject();
                if (jv.isObject()) {
                    final JSONObject json = jv.toObject();
                    if (!json.hasAndNotNull(ERROR)) {
                        addException(json, warning, locale);
                    }
                }
            }
        } else {
            writer.array();
            try {
                for (final OXException warning : warnings) {
                    writer.object();
                    try {
                        writeException(warning/*.setCategory(Category.CATEGORY_WARNING)*/, writer, locale);
                    } finally {
                        writer.endObject();
                    }
                }
            } finally {
                writer.endArray();
            }
            if (!warnings.isEmpty() && (writer instanceof OXJSONWriter)) {
                final JSONValue jv = ((OXJSONWriter) writer).getObject();
                if (jv.isObject()) {
                    final JSONObject json = jv.toObject();
                    if (!json.hasAndNotNull(ERROR)) {
                        addException(json, warnings.get(0), locale);
                    }
                }
            }
        }
    }

    /**
     * Writes given instance of <code>OXException</code> into given instance of <code>JSONWriter</code> assuming that writer's mode is
     * already set to writing a JSON object
     *
     * @param exc - the exception to write
     * @param writer - the writer to write to
     * @throws JSONException - if writing fails
     */
    public static void writeException(final OXException exc, final JSONWriter writer) throws JSONException {
        writeException(exc, writer, defaultLocale());
    }

    /**
     * Writes given instance of <code>OXException</code> into given instance of <code>JSONWriter</code> assuming that writer's mode is
     * already set to writing a JSON object
     *
     * @param exc - the exception to write
     * @param writer - the writer to write to
     * @param locale The locale to use for internationalization of the error message
     * @throws JSONException - if writing fails
     */
    public static void writeException(final OXException exc, final JSONWriter writer, final Locale locale) throws JSONException {
        writer.key(ERROR).value(exc.getDisplayMessage(locale));
        /*
         * Put argument JSON array for compatibility reasons
         */
        {
            Object[] args = exc.getLogArgs();
            if ((null == args) || (0 == args.length)) {
                args = exc.getDisplayArgs();
            }
            // Enforce first condition; review later on
            if ((null == args) || (0 == args.length)) {
                writer.key(ResponseFields.ERROR_PARAMS).value(new JSONArray());
            } else {
                final JSONArray jArray = new JSONArray();
                for (final Object arg : args) {
                    jArray.put(arg);
                }
                writer.key(ResponseFields.ERROR_PARAMS).value(jArray);
            }
        }
        {
            final List<Category> categories = exc.getCategories();
            if (1 == categories.size()) {
                final Category category = categories.get(0);
                writer.key(ERROR_CATEGORIES).value(category.toString());
                final int number = Categories.getFormerCategoryNumber(category);
                if (number > 0) {
                    writer.key(ERROR_CATEGORY).value(number);
                }
            } else {
                writer.key(ERROR_CATEGORIES);
                writer.array();
                try {
                    for (final Category category : categories) {
                        writer.value(category.toString());
                    }
                } finally {
                    writer.endArray();
                }
            }
            // For compatibility
            if (!categories.isEmpty()) {
                final int number = Categories.getFormerCategoryNumber(categories.get(0));
                if (number > 0) {
                    writer.key(ERROR_CATEGORY).value(number);
                }
            }
        }
        writer.key(ERROR_CODE).value(exc.getErrorCode());
        writer.key(ERROR_ID).value(exc.getExceptionId());
        writeProblematic(exc, writer);
        writeTruncated(exc, writer);
        if (exc.getLogArgs() != null) {
            final JSONArray array = new JSONArray();
            for (final Object tmp : exc.getLogArgs()) {
                array.put(tmp);
            }
            writer.key(ResponseFields.ERROR_PARAMS).value(array);
        }
        // Write stack trace
        if (includeStackTraceOnError()) {
            writer.key(ERROR_STACK);
            writer.array();
            try {
                writer.value(exc.getSoleMessage());
                final StackTraceElement[] traceElements = exc.getStackTrace();
                if (null != traceElements && traceElements.length > 0) {
                    final StringBuilder tmp = new StringBuilder(64);
                    for (final StackTraceElement stackTraceElement : traceElements) {
                        tmp.setLength(0);
                        writeElementTo(stackTraceElement, tmp);
                        writer.value(tmp.toString());
                    }
                }
            } finally {
                writer.endArray();
            }
        }
    }

    private static void writeProblematic(final OXException exc, final JSONWriter writer) throws JSONException {
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

    private static void writeTruncated(final OXException exc, final JSONWriter writer) throws JSONException {
        final ProblematicAttribute[] problematics = exc.getProblematics();
        if (problematics.length > 0) {
            final JSONArray array = new JSONArray();
            for (final ProblematicAttribute problematic : problematics) {
                if (problematic instanceof Truncated) {
                    array.put(((Truncated) problematic).getId());
                }
            }
            writer.key(TRUNCATED).value(array);
        }
    }

}
