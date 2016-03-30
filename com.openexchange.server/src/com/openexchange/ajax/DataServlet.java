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

package com.openexchange.ajax;

import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * TODO Remove this class from inheritance tree because all its methods are
 * static.
 */
public abstract class DataServlet extends PermissionServlet {

    private static final long serialVersionUID = 5088332994969906626L;

    private static final String _invalidParameter = "invalid parameter: ";

    private static final String _missingField = "missing field: ";

    public static final int maxEntries = 50000;

    public static int parseIntParameter(final HttpServletRequest httpServletRequest, final String name) throws OXException {
        if (containsParameter(httpServletRequest, name)) {
            final String parameter = httpServletRequest.getParameter(name);
            try {
                return Integer.parseInt(parameter);
            } catch (final NumberFormatException exc) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
            }
        }
        return 0;
    }

    public static Date parseDateParameter(final HttpServletRequest httpServletRequest, final String name) throws OXException {
        if (containsParameter(httpServletRequest, name)) {
            final String parameter = httpServletRequest.getParameter(name);
            try {
                return new Date(Long.parseLong(httpServletRequest.getParameter(name)));
            } catch (final NumberFormatException exc) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
            }
        }
        return null;
    }

    public static String parseStringParameter(final HttpServletRequest httpServletRequest, final String name) {
        return httpServletRequest.getParameter(name);
    }

    public static String parseMandatoryStringParameter(final HttpServletRequest httpServletRequest, final String name) throws OXException {
        if (!containsParameter(httpServletRequest, name)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return parseStringParameter(httpServletRequest, name);
    }

    public static int parseMandatoryIntParameter(final HttpServletRequest httpServletRequest, final String name) throws OXException {
        if (!containsParameter(httpServletRequest, name)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return parseIntParameter(httpServletRequest, name);
    }

    public static int[] parsIntParameterArray(final HttpServletRequest httpServletRequest, final String name) {
        if (containsParameter(httpServletRequest, name)) {
            final String[] s = httpServletRequest.getParameterValues(name);

            final int[] i = new int[s.length];

            for (int a = 0; a < i.length; a++) {
                i[a] = Integer.parseInt(s[a]);
            }

            return i;
        }
        return null;
    }

    public static String[] parseStringParameterArray(final HttpServletRequest httpServletRequest, final String name) {
        if (containsParameter(httpServletRequest, name)) {
            final String[] s = httpServletRequest.getParameterValues(name);
            return s;
        }
        return null;
    }

    public static int[] parseMandatoryIntParameterArray(final HttpServletRequest httpServletRequest, final String name) throws OXException {
        if (!containsParameter(httpServletRequest, name)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        final String[] s = httpServletRequest.getParameterValues(name);

        final int[] i = new int[s.length];

        for (int a = 0; a < i.length; a++) {
            i[a] = Integer.parseInt(s[a]);
        }

        return i;
    }

    public static Date parseMandatoryDateParameter(final HttpServletRequest httpServletRequest, final String name) throws OXException {
        if (!containsParameter(httpServletRequest, name)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return parseDateParameter(httpServletRequest, name);
    }

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Generates an appropriate JSON object from given request's parameters.
     *
     * @param httpServletRequest The HTTP servlet request
     * @return An appropriate JSON object
     * @throws JSONException If a JSON error occurs
     */
    public static JSONObject convertParameter2JSONObject(final HttpServletRequest httpServletRequest) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        // final StringBuilder sb = new StringBuilder();
        for (final Enumeration<?> e = httpServletRequest.getParameterNames(); e.hasMoreElements();) {
            final String name = e.nextElement().toString();
            final String value = httpServletRequest.getParameter(name);
            if (AJAXServlet.PARAMETER_COLUMNS.equals(name)) {
                jsonObj.put(name, SPLIT.matcher(value).replaceAll(","));
                /*-
                 * Previous code:
                 *
                final String[] sa = SPLIT.split(value, 0);
                final int len = sa.length;
                if (len > 0) {
                    sb.setLength(0);
                    sb.append(sa[0]);
                    for (int a = 1; a < len; a++) {
                        sb.append(',').append(sa[a]);
                    }
                    jsonObj.put(name, sb.toString());
                } else {
                    jsonObj.put(name, "");
                }
                */
            } else {
                jsonObj.put(name, value);
            }
        }
        return jsonObj;
    }

}
