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

package com.openexchange.ajax;

import java.util.Date;
import java.util.Map;
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
            } catch (NumberFormatException exc) {
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
            } catch (NumberFormatException exc) {
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
     * @param httpRequest The HTTP request
     * @return An appropriate JSON object
     * @throws JSONException If a JSON error occurs
     */
    public static JSONObject convertParameter2JSONObject(final HttpServletRequest httpRequest) throws JSONException {
        @SuppressWarnings("unchecked")
        Map<String, String[]> parameters = httpRequest.getParameterMap();
        JSONObject jsonObj = new JSONObject(parameters.size());
        for (Map.Entry<String,String[]> entry : parameters.entrySet()) {
            String[] values = entry.getValue();
            if (null != values && values.length > 0) {
                // Grab first value
                String value = values[0];

                // Check parameter name
                String name = entry.getKey();
                if (AJAXServlet.PARAMETER_COLUMNS.equals(name) && value.indexOf(' ', 0) >= 0) {
                    jsonObj.put(name, SPLIT.matcher(value).replaceAll(","));
                } else {
                    jsonObj.put(name, value);
                }
            }
        }
        return jsonObj;
    }

}
