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

package com.openexchange.ajax.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Parsing;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * DataParser
 * TODO make protected fields private.
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public abstract class DataParser {

    public static final int NO_INT = 0;

    protected boolean parseAll;
    protected TimeZone timeZone;

    protected DataParser() {
        this(false, null);
    }

    protected DataParser(final TimeZone timeZone) {
        this(false, timeZone);
    }

    protected DataParser(final boolean parseAll, final TimeZone timeZone) {
        super();
        this.parseAll = parseAll;
        this.timeZone = timeZone;
    }

    protected void parseElementDataObject(final DataObject dataobject, final JSONObject jsonobject) throws JSONException, OXException {
        if (jsonobject.has(DataFields.ID)) {
            dataobject.setObjectID(parseInt(jsonobject, DataFields.ID));
        }

        if (parseAll && jsonobject.has(DataFields.CREATED_BY)) {
            dataobject.setCreatedBy(parseInt(jsonobject, DataFields.CREATED_BY));
        }

        if (parseAll && jsonobject.has(DataFields.CREATION_DATE)) {
            dataobject.setCreationDate(parseTime(jsonobject, DataFields.CREATION_DATE, timeZone));
        }

        if (parseAll && jsonobject.has(DataFields.MODIFIED_BY)) {
            dataobject.setModifiedBy(parseInt(jsonobject, DataFields.MODIFIED_BY));
        }

        if (parseAll && jsonobject.has(DataFields.LAST_MODIFIED)) {
            dataobject.setLastModified(parseTime(jsonobject, DataFields.LAST_MODIFIED, timeZone));
        }
    }

    /**
     * Parses optional field out of specified JSON object.
     *
     * @param jsonObj
     *            The JSON object to parse
     * @param name
     *            The optional field name
     * @return The optional field's value or <code>null</code> if there's no
     *         such field
     */
    public static String parseString(final JSONObject jsonObj, final String name) {
        String retval = null;
        if (jsonObj.hasAndNotNull(name)) {
            final String test = jsonObj.optString(name);
            if (0 != test.length()) {
                retval = test;
            }
        }
        return retval;
    }

    public static int parseInt(final JSONObject json, final String name) throws JSONException, OXException {
        if (!json.has(name)) {
            return NO_INT;
        }
        final String tmp = json.getString(name);
        if (com.openexchange.java.Strings.isEmpty(tmp) || json.isNull(name) || "null".equalsIgnoreCase(tmp)) {
            return 0;
        }
        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException e) {
            throw OXJSONExceptionCodes.NUMBER_PARSING.create(e, tmp, name);
        }
    }

    public static boolean parseBoolean(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return false;
        }

        return jsonObj.getBoolean(name);
    }

    public static BigDecimal parseBigDecimal(JSONObject jsonObj, String name) throws JSONException, OXException {
        if (!jsonObj.has(name)) {
            return null;
        }
        Object obj = jsonObj.get(name);
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        final String tmp = jsonObj.getString(name);
        if (com.openexchange.java.Strings.isEmpty(tmp) || jsonObj.isNull(name) || "null".equalsIgnoreCase(tmp)) {
            return null;
        }
        try {
            return new BigDecimal(tmp);
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    private static final Pattern DIGITS = Pattern.compile("^\\-?\\d+$");

    public static Long parseLong(final JSONObject jsonObj, final String name) throws OXException {
        final String tmp;
        try {
            tmp = jsonObj.getString(name);
        } catch (final JSONException e) {
            return null;
        }
        if (com.openexchange.java.Strings.isEmpty(tmp) || jsonObj.isNull(name) || "null".equalsIgnoreCase(tmp)) {
            return null;
        }
        final Parsing parsing = new Parsing() {
            @Override
            public String getAttribute() {
                return name;
            }
        };
        // Check for non digit characters and give specialized exception for this.
        if (!DIGITS.matcher(tmp).matches()) {
            final OXException e = OXJSONExceptionCodes.CONTAINS_NON_DIGITS.create(tmp, name);
            e.addProblematic(parsing);
            throw e;
        }
        try {
            return Long.valueOf(tmp);
        } catch (final NumberFormatException e1) {
            // Check if it parses into a BigInteger.
            try {
                new BigInteger(tmp);
                final OXException e = OXJSONExceptionCodes.TOO_BIG_NUMBER.create(e1, name);
                e.addProblematic(parsing);
                throw e;
            } catch (final NumberFormatException e2) {
                final OXException e = OXJSONExceptionCodes.NUMBER_PARSING.create(e1, tmp, name);
                e.addProblematic(parsing);
                throw e;
            }
        }
    }

    public static Integer parseInteger(JSONObject json, final String name) throws OXException {
        final String tmp;
        try {
            tmp = json.getString(name);
        } catch (JSONException e) {
            return null;
        }
        if (com.openexchange.java.Strings.isEmpty(tmp) || json.isNull(name) || "null".equalsIgnoreCase(tmp)) {
            return null;
        }
        final Parsing parsing = new Parsing() {
            @Override
            public String getAttribute() {
                return name;
            }
        };
        // Check for non digit characters and give specialized exception for this.
        if (!DIGITS.matcher(tmp).matches()) {
            final OXException e = OXJSONExceptionCodes.CONTAINS_NON_DIGITS.create(tmp, name);
            e.addProblematic(parsing);
            throw e;
        }
        try {
            return Integer.valueOf(tmp);
        } catch (NumberFormatException e) {
            // Check if it parses into a BigInteger.
            try {
                new BigInteger(tmp);
                final OXException exc = OXJSONExceptionCodes.TOO_BIG_NUMBER.create(e, name);
                exc.addProblematic(parsing);
                throw exc;
            } catch (final NumberFormatException e2) {
                final OXException exc = OXJSONExceptionCodes.NUMBER_PARSING.create(e, tmp, name);
                exc.addProblematic(parsing);
                throw e;
            }
        }
    }

    public static Date parseTime(final JSONObject jsonObj, final String name, final TimeZone timeZone) {
        final Date d = parseDate(jsonObj, name);
        if (d == null) {
            return null;
        }

        final int offset = timeZone.getOffset(d.getTime());
        d.setTime(d.getTime() - offset);
        return d;
    }

    private int getOffSet(final TimeZone timeZone, final Date d) {
        int offset = timeZone.getOffset(d.getTime());
        final Date test = new Date(d.getTime() - offset);
        final int clientOffset = timeZone.getOffset(test.getTime());
        if (clientOffset != offset) {
            // UI offset addition triggered DST, use not DST
            offset = clientOffset;
        }
        return offset;
    }

    public static Date parseDate(final JSONObject jsonObj, final String name) {
        if (!jsonObj.has(name)) {
            return null;
        }

        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            return null;
        }
        return new Date(Long.parseLong(tmp));
    }

    public static String checkString(final JSONObject jsonObj, final String name) throws OXException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        if (tmp.length() == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        return tmp;
    }

    public static int checkInt(final JSONObject json, final String name) throws OXException, OXException {
        final String tmp = checkString(json, name);
        if (tmp == null || tmp.length() == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException e) {
            throw OXJSONExceptionCodes.NUMBER_PARSING.create(e, tmp, name);
        }
    }

    public static boolean checkBoolean(final JSONObject jsonObj, final String name) throws JSONException, OXException {
        final String tmp = jsonObj.getString(name);
        if (tmp == null || tmp.length() == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        return Boolean.parseBoolean(tmp);
    }

    public static float checkFloat(final JSONObject jsonObj, final String name) throws JSONException, OXException, OXException {
        final String tmp = jsonObj.getString(name);
        if (tmp == null || tmp.length() == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }

        try {
            return Float.parseFloat(tmp);
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    public static Date checkDate(final JSONObject jsonObj, final String name) throws OXException, OXException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }

        try {
            return new Date(Long.parseLong(tmp));
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    public static Date checkTime(final JSONObject jsonObj, final String name, final TimeZone timeZone) throws OXException, OXException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        try {
            final Date d = new Date(Long.parseLong(tmp));
            final int offset = timeZone.getOffset(d.getTime());
            d.setTime(d.getTime()-offset);
            return d;
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    public static UUID checkUUID(final JSONObject jsonObj, final String name) throws OXException, OXException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        try {
            return UUID.fromString(tmp);
        } catch (final IllegalArgumentException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    public static JSONObject checkJSONObject(final JSONObject jsonObj, final String name) throws OXException {
        final JSONObject tmp = jsonObj.optJSONObject(name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        return tmp;
    }

    public static JSONArray checkJSONArray(final JSONObject jsonObj, final String name) throws OXException {
        final JSONArray tmp = jsonObj.optJSONArray(name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }
        return tmp;
    }

    public static int[] parseJSONIntArray(final JSONObject jsonObj, final String name) throws JSONException, OXException {
        if (!jsonObj.has(name)) {
            return null;
        }

        final JSONArray tmp = jsonObj.getJSONArray(name);
        if (tmp == null) {
            return null;
        }

        try {
            final int i[] = new int[tmp.length()];
            for (int a = 0; a < tmp.length(); a++) {
                i[a] = tmp.getInt(a);
            }

            return i;
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    /**
     * Parses optional array field out of specified JSON object
     *
     * @param jsonObj
     *            The JSON object to parse
     * @param name
     *            The optional array field's name
     * @return The optional array field's value as an array of {@link String} or
     *         <code>null</code> if there's no such field
     * @throws JSONException
     *             If a JSON error occurs
     */
    public static String[] parseJSONStringArray(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.hasAndNotNull(name)) {
            return null;
        }
        final JSONArray tmp = jsonObj.getJSONArray(name);
        final String s[] = new String[tmp.length()];
        for (int a = 0; a < tmp.length(); a++) {
            s[a] = tmp.getString(a);
        }
        return s;
    }

    public static UUID parseUUID(final JSONObject jsonObj, final String name) throws OXException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            return null;
        }
        try {
            return UUID.fromString(tmp);
        } catch (final IllegalArgumentException exc) {
            return null;
        }
    }

    public static Date[] parseJSONDateArray(final JSONObject jsonObj, final String name) throws JSONException, OXException {
        if (!jsonObj.has(name)) {
            return null;
        }

        final JSONArray tmp = jsonObj.getJSONArray(name);
        if (tmp == null) {
            return null;
        }

        try {
            final Date d[] = new Date[tmp.length()];
            for (int a = 0; a < tmp.length(); a++) {
                d[a] = new Date(tmp.getLong(a));
            }

            return d;
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    public static int[] checkJSONIntArray(final JSONObject jsonObj, final String name) throws JSONException, OXException, OXException {
        final int[] i = parseJSONIntArray(jsonObj, name);
        if (i == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( name);
        }

        return i;
    }

    /**
     * @return the timeZone
     */
    protected TimeZone getTimeZone() {
        return timeZone;
    }
}
