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

package com.openexchange.user.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link Utility} - Utility class for user JSON interface bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    private static final ConcurrentMap<String, Future<TimeZone>> ZONE_CACHE = new ConcurrentHashMap<String, Future<TimeZone>>();

    private static final Pattern PAT = Pattern.compile(" *, *");

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    public static TimeZone getTimeZone(final String ID) {
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
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utility.class);
            LOG.error("", e);
        }
        return TimeZone.getTimeZone(ID);
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone identifier
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final String timeZone) {
        return (date + getTimeZone(timeZone).getOffset(date));
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

    /**
     * Checks if specified required field is contained in given fields and appends it if necessary.
     *
     * @param fields The fields to check
     * @param requiredField The required field
     * @return The fields with required field (possibly appended)
     */
    public static int[] checkForRequiredField(final int[] fields, final int requiredField) {
        for (final int field : fields) {
            if (requiredField == field) {
                /*
                 * Found
                 */
                return fields;
            }
        }
        /*
         * Append required field
         */
        final int[] checkedCols = new int[fields.length + 1];
        System.arraycopy(fields, 0, checkedCols, 0, fields.length);
        checkedCols[fields.length] = requiredField;
        return checkedCols;
    }

    /**
     * Parses specified optional parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>; a zero length array is returned if parameter is missing
     */
    public static int[] parseOptionalIntArrayParameter(final String parameterName, final AJAXRequestData request) {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return new int[0];
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = Integer.parseInt(sa[i]);
        }
        return columns;
    }

    /**
     * Split a comma-separated string.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Gets the attribute parameters.
     *
     * @param expectedParameterNames The expected parameter names
     * @param request The request
     * @return The attribute parameters
     * @throws OXException If parsing attribute parameters fails
     */
    public static Map<String, List<String>> getAttributeParameters(final Set<String> expectedParameterNames, final AJAXRequestData request) throws OXException {
        final Iterator<Entry<String, String>> nonMatchingParameters = request.getNonMatchingParameters(expectedParameterNames);
        if (!nonMatchingParameters.hasNext()) {
            return Collections.emptyMap();
        }
        final Map<String, List<String>> attributeParameters = new LinkedHashMap<String, List<String>>();
        do {
            final Entry<String, String> entry = nonMatchingParameters.next();
            final String key = entry.getKey();
            List<String> list = attributeParameters.get(key);
            if (null == list) {
                list = new ArrayList<String>(4);
                attributeParameters.put(key, list);
            }
            final String value = entry.getValue();
            final int pos = value.indexOf('*');
            if (pos < 0) {
                final String[] strings = SPLIT.split(value, 0);
                for (final String string : strings) {
                    list.add(string);
                }
            } else {
                if (value.length() > 1) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( key, value);
                }
                list.add(UserContact.ALL_ATTRIBUTES);
            }
        } while (nonMatchingParameters.hasNext());
        return attributeParameters;
    }

}
