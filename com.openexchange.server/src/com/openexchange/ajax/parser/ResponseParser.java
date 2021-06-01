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

package com.openexchange.ajax.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.fields.ResponseFields.ParsingFields;
import com.openexchange.ajax.fields.ResponseFields.TruncatedFields;
import com.openexchange.exception.Categories;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Parsing;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.EnumComponent;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ResponseParser {

    /**
     * Prevent instantiation.
     */
    private ResponseParser() {
        super();
    }

    /**
     * Deserializes a response into the Response object.
     * @param body JSON response string.
     * @return the parsed object.
     * @throws JSONException if parsing fails.
     */
    public static Response parse(final String body) throws JSONException {
        return parse(new JSONObject(body));
    }

    /**
     * Deserializes a JSON response into the Response object.
     * @param json JSON response.
     * @return the parsed object.
     * @throws JSONException if parsing fails.
     */
    public static Response parse(final JSONObject json) throws JSONException {
        final Response retval = new Response(json);
        parse(retval, json);
        return retval;
    }

    public static void parse(final Response response, final JSONObject json) throws JSONException {
        if (json.has(ResponseFields.DATA)) {
            response.setData(json.get(ResponseFields.DATA));
        }
        if (json.has(ResponseFields.TIMESTAMP)) {
            response.setTimestamp(new Date(json.getLong(ResponseFields.TIMESTAMP)));
        }
        OXException exception = parseException(json);
        if (null != exception) {
            if (null != exception.getCategories() && 0 < exception.getCategories().size() && Category.CATEGORY_WARNING.equals(exception.getCategories().get(0))) {
                response.addWarning(exception);
            } else {
                response.setException(exception);
            }
        }
        if (json.has(ResponseFields.WARNINGS)) {
            JSONObject warningObject = json.optJSONObject(ResponseFields.WARNINGS);
            JSONArray warningArray = json.optJSONArray(ResponseFields.WARNINGS);
            if (warningObject != null) {
                OXException warning = parseException(warningObject);
                if (warning != null)
                    response.addWarning(warning);
            } else if (warningArray != null) {
                for (Object item : warningArray) {
                    OXException warning = parseException((JSONObject) item);
                    if (warning != null)
                        response.addWarning(warning);
                }
            }
        }
    }

    /**
     * Parses an {@link OXException} from the common error fields ( {@link ResponseFields#ERROR}, {@link ResponseFields#ERROR_CODE}, ...)
     * in the supplied JSON object.
     *
     * @param json The json object to parse the exception from
     * @return The parsed exception, or <code>null</code> if none was parsed
     */
    public static OXException parseException(JSONObject json) throws JSONException {
        final String message = json.optString(ResponseFields.ERROR, null);
        final String code = json.optString(ResponseFields.ERROR_CODE, null);
        if (message != null || code != null) {
            final String prefix = parseComponent(code);
            final int number = parseErrorNumber(code);
            final Object jsonCategories = json.opt(ResponseFields.ERROR_CATEGORIES);
            final List<Category> categories;
            if (jsonCategories instanceof JSONArray) {
                final JSONArray jsonArray = (JSONArray) jsonCategories;
                final int length = jsonArray.length();
                categories = new ArrayList<Category>(length);
                for (int i = 0; i < length; i++) {
                    categories.add(Categories.getKnownCategoryByName(jsonArray.getString(i)));
                }
                OXException.sortCategories(categories);
            } else {
                if (jsonCategories != null) {
                    categories = Collections.singletonList(Categories.getKnownCategoryByName(jsonCategories.toString()));
                } else {
                    categories = Arrays.asList(Category.CATEGORY_ERROR);
                }
            }
            final Object[] args = parseErrorMessageArgs(json.optJSONArray(ResponseFields.ERROR_PARAMS));
            final String logMessage = json.optString(ResponseFields.ERROR_DESC);
            final OXException exception = new OXException(number, message, args);
            exception.setLogMessage(logMessage);
            exception.setPrefix(prefix);
            for (final Category cat : categories) {
                exception.addCategory(cat);
            }
            if (json.has(ResponseFields.ERROR_ID)) {
                exception.setExceptionId(json.getString(ResponseFields.ERROR_ID));
            }
            parseProblematics(json.optJSONArray(ResponseFields.PROBLEMATIC), exception);
            // Check for stack trace
            if (json.hasAndNotNull(ResponseFields.ERROR_STACK)) {
                final JSONArray jStack = json.getJSONArray(ResponseFields.ERROR_STACK);
                parseStackTrace(exception, jStack);
            }
            return exception;
        }
        return null;
    }

    private static void parseStackTrace(final OXException exception, final JSONArray jStack) {
        final int length = jStack.length();

        Throwable t = exception;
        List<StackTraceElement> stack = new ArrayList<StackTraceElement>(length);

        // Start at second line: i = 1
        for (int i = 1; i < length; i++) {
            final String line = jStack.optString(i, null);
            StackTraceElement ste = null;
            if (null != line) {
                if (line.startsWith("Caused by: ")) {
                    t.setStackTrace(stack.toArray(new StackTraceElement[0]));
                    stack = new ArrayList<StackTraceElement>(length);
                    final Throwable parent = t;
                    t = new Throwable(line);
                    parent.initCause(t);
                } else {
                    final int parenthesisStart = line.indexOf('(');
                    final int methodStart = line.substring(0, parenthesisStart).lastIndexOf('.');
                    final String className = line.substring(0, methodStart);
                    final String methodName = line.substring(methodStart+1, parenthesisStart);
                    if (line.regionMatches(parenthesisStart + 1, "Native Method)", 0, "Native Method)".length())) {
                        ste = new StackTraceElement(className, methodName, null, -2);
                    } else if (line.regionMatches(parenthesisStart + 1, "Unknown Source)", 0, "Unknown Source)".length())) {
                        ste = new StackTraceElement(className, methodName, null, -1);
                    } else {
                        final int colonPos = line.indexOf(':', parenthesisStart + 1);
                        if (colonPos < 0) {
                            ste = new StackTraceElement(className, methodName, line.substring(parenthesisStart + 1, line.indexOf(')', parenthesisStart + 1)), -1);
                        } else {
                            final String fileName = line.substring(parenthesisStart + 1, colonPos);
                            final int lineNumber = Integer.parseInt(line.substring(colonPos + 1, line.indexOf(')', parenthesisStart + 1)));
                            ste = new StackTraceElement(className, methodName, fileName, lineNumber);
                        }
                    }
                }
            }
            if (null != ste) {
                stack.add(ste);
            }
        }
        t.setStackTrace(stack.toArray(new StackTraceElement[0]));
    }

    /**
     * Parses the component part of the error code.
     *
     * @param code
     *            error code to parse.
     * @return the parsed component or {@link EnumComponent#NONE}.
     */
    private static String parseComponent(final String code) {
        if (code == null || code.length() == 0) {
            return EnumComponent.NONE.getAbbreviation();
        }
        final int pos = code.lastIndexOf('-');
        if (pos != -1) {
            final String abbr = code.substring(0, pos);
            final EnumComponent component = EnumComponent.byAbbreviation(abbr);
            if (component != null) {
                return component.getAbbreviation();
            }
            return abbr;
        }
        return EnumComponent.NONE.getAbbreviation();
    }

    /**
     * Parses the error number out of the error code.
     *
     * @param code
     *            error code to parse.
     * @return the parsed error number or 0.
     */
    private static int parseErrorNumber(final String code) {
        if (code == null || code.length() == 0) {
            return 0;
        }
        final int pos = code.lastIndexOf('-');
        if (pos != -1) {
            try {
                return Integer.parseInt(code.substring(pos + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Parses the error message arguments.
     *
     * @param jArgs
     *            the json array with the error message arguments or
     *            <code>null</code>.
     * @param exception
     *            the error message arguments will be stored in this exception.
     */
    private static Object[] parseErrorMessageArgs(final JSONArray jArgs) {
        if (null == jArgs) {
            return new Object[0];
        }

        int length = jArgs.length();
        Object[] args = new Object[length];
        for (int i = length; i-- > 0;) {
            args[i] = jArgs.opt(i);
        }
        return args;
    }

    private static void parseProblematics(final JSONArray probs, final OXException exc) throws JSONException {
        if (null == probs) {
            return;
        }
        final List<ProblematicAttribute> problematics = new ArrayList<ProblematicAttribute>();
        for (int i = 0; i < probs.length(); i++) {
            final JSONObject json = probs.getJSONObject(i);
            if (json.has(TruncatedFields.ID)) {
                problematics.add(parseTruncated(json));
            } else if (json.has(ParsingFields.NAME)) {
                problematics.add(parseParsing(json));
            }
        }
        for (final ProblematicAttribute problematic : problematics) {
            exc.addProblematic(problematic);
        }
    }

    private static Truncated parseTruncated(final JSONObject json) throws JSONException {
        final int id = json.getInt(TruncatedFields.ID);
        return new Truncated() {
            @Override
            public int getId() {
                return id;
            }
            @Override
            public int getLength() {
                return 0;
            }
            @Override
            public int getMaxSize() {
                return 0;
            }
        };
    }

    private static Parsing parseParsing(final JSONObject json) throws JSONException {
        final String attribute = json.getString(ParsingFields.NAME);
        return new Parsing() {
            @Override
            public String getAttribute() {
                return attribute;
            }
        };
    }

    public static final class StringComponent implements Component {

        private static final long serialVersionUID = 1159589477110476030L;
        private final String abbr;

        public StringComponent(final String abbr) {
            super();
            this.abbr = abbr;
        }

        @Override
        public String getAbbreviation() {
            return abbr;
        }
    }
}
