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

package com.openexchange.ajax.parser;

import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.groupware.AbstractOXException.Parsing;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.OXJSONException.Code;

/**
 * DataParser
 * TODO make protected fields private.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public abstract class DataParser {

    protected boolean parseAll;

    protected TimeZone timeZone;

    protected DataParser() {
        this(false, null);
    }

    protected DataParser(final TimeZone timeZone) {
        this(false, timeZone);
    }

    protected DataParser(boolean parseAll, TimeZone timeZone) {
        super();
        this.parseAll = parseAll;
        this.timeZone = timeZone;
    }

    protected void parseElementDataObject(final DataObject dataobject, final JSONObject jsonobject) throws JSONException, OXJSONException {
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
     * @throws JSONException
     *             If a JSON error occurs
     */
    public static String parseString(final JSONObject jsonObj, final String name) throws JSONException {
        String retval = null;
        if (jsonObj.hasAndNotNull(name)) {
            final String test = jsonObj.getString(name);
            if (0 != test.length()) {
                retval = test;
            }
        }
        return retval;
    }

    public static int parseInt(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException {
        if (!jsonObj.has(name)) {
            return 0;
        }

        final String tmp = jsonObj.getString(name);
        if (tmp == null || jsonObj.isNull(name) || tmp.length() == 0) {
            return 0;
        }

        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException exc) {
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    public static boolean parseBoolean(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return false;
        }

        return jsonObj.getBoolean(name);
    }

    public static float parseFloat(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException {
        if (!jsonObj.has(name)) {
            return 0;
        }

        final String tmp = jsonObj.getString(name);
        if (tmp == null || jsonObj.isNull(name) || tmp.length() == 0) {
            return 0;
        }

        try {
            return Float.parseFloat(tmp);
        } catch (final NumberFormatException exc) {
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    private static final Pattern DIGITS = Pattern.compile("^\\-?\\d+$");

    public static long parseLong(final JSONObject jsonObj, final String name)
        throws OXJSONException {
        final String tmp;
        try {
            tmp = jsonObj.getString(name);
        } catch (final JSONException e) {
            return 0;
        }
        if (tmp == null || jsonObj.isNull(name) || tmp.length() == 0) {
            return 0;
        }
        final Parsing parsing = new Parsing() {
            public String getAttribute() {
                return name;
            }
        };
        // Check for non digit characters and give specialized exception for this.
        if (!DIGITS.matcher(tmp).matches()) {
            final OXJSONException e = new OXJSONException(
                Code.CONTAINS_NON_DIGITS, tmp, name);
            e.addProblematic(parsing);
            throw e;
        }
        try {
            return Long.parseLong(tmp);
        } catch (final NumberFormatException e1) {
            // Check if it parses into a BigInteger.
            try {
                new BigInteger(tmp);
                final OXJSONException e = new OXJSONException(
                    Code.TOO_BIG_NUMBER, e1, name);
                e.addProblematic(parsing);
                throw e;
            } catch (final NumberFormatException e2) {
                final OXJSONException e = new OXJSONException(
                    Code.NUMBER_PARSING, e1, tmp, name);
                e.addProblematic(parsing);
                throw e;
            }
        }
    }

    public static Date parseTime(final JSONObject jsonObj, final String name, final TimeZone timeZone) throws JSONException {
        final Date d = parseDate(jsonObj, name);
        if (d == null) {
            return null;
        }

        final int offset = timeZone.getOffset(d.getTime());
        d.setTime(d.getTime()-offset);
        return d;
    }

    public static Date parseDate(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return null;
        }

        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            return null;
        }
        return new Date(Long.parseLong(tmp));
    }

    public static String checkString(final JSONObject jsonObj, final String name) throws JSONException, AjaxException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }
        if (tmp.length() == 0) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }
        return tmp;
    }

    public static int checkInt(final JSONObject jsonObj, final String name) throws OXJSONException, JSONException, AjaxException {
        final String tmp = checkString(jsonObj, name);
        if (tmp == null || tmp.length() == 0) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }

        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException exc) {
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    public static boolean checkBoolean(final JSONObject jsonObj, final String name) throws JSONException, AjaxException {
        final String tmp = jsonObj.getString(name);
        if (tmp == null || tmp.length() == 0) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }
        return Boolean.parseBoolean(tmp);
    }

    public static float checkFloat(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException, AjaxException {
        final String tmp = jsonObj.getString(name);
        if (tmp == null || tmp.length() == 0) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }

        try {
            return Float.parseFloat(tmp);
        } catch (final NumberFormatException exc) {
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    public static Date checkDate(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException, AjaxException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }

        try {
            return new Date(Long.parseLong(tmp));
        } catch (final NumberFormatException exc) {
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    public static Date checkTime(final JSONObject jsonObj, final String name, final TimeZone timeZone) throws JSONException, OXJSONException, AjaxException {
        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }
        try {
            final Date d = new Date(Long.parseLong(tmp));
            final int offset = timeZone.getOffset(d.getTime());
            d.setTime(d.getTime()-offset);
            return d;
        } catch (final NumberFormatException exc) {
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    public static JSONObject checkJSONObject(final JSONObject jsonObj, final String name) throws AjaxException {
        final JSONObject tmp = jsonObj.optJSONObject(name);
        if (tmp == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }
        return tmp;
    }

    public static JSONArray checkJSONArray(final JSONObject jsonObj, final String name) throws AjaxException {
        final JSONArray tmp = jsonObj.optJSONArray(name);
        if (tmp == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
        }
        return tmp;
    }

    public static int[] parseJSONIntArray(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException {
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
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
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

    public static Date[] parseJSONDateArray(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException {
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
            throw new OXJSONException(Code.INVALID_VALUE, exc, name, tmp);
        }
    }

    public static int[] checkJSONIntArray(final JSONObject jsonObj, final String name) throws JSONException, OXJSONException, AjaxException {
        final int[] i = parseJSONIntArray(jsonObj, name);
        if (i == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, name);
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
