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

package com.openexchange.ajax.helper;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;

/**
 * ParamContainer
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ParamContainer {

    /**
     * Split pattern for CSV.
     */
    static final Pattern SPLIT = Pattern.compile(" *, *");

    private static final class MapParamContainer extends ParamContainer {

        private final Map<String, String> map;

        public MapParamContainer(Map<String, String> map) {
            super();
            this.map = map;
        }

        @Override
        public Set<String> getParameterNames() {
            return map.keySet();
        }

        @Override
        public Date checkDateParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return new Date(Long.parseLong(tmp));
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public int[] checkIntArrayParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            final String[] sa = SPLIT.split(tmp, 0);
            final int intArray[] = new int[sa.length];
            for (int a = 0; a < sa.length; a++) {
                try {
                    intArray[a] = Integer.parseInt(sa[a]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
                }
            }
            return intArray;
        }

        @Override
        public int checkIntParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public long checkLongParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return Long.parseLong(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public String checkStringParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            return tmp;
        }

        @Override
        public Date getDateParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                return null;
            }
            try {
                return new Date(Long.parseLong(tmp));
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public String getHeader(final String hdrName) {
            return null;
        }

        @Override
        public int[] getIntArrayParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                return null;
            }
            final String[] sa = SPLIT.split(tmp, 0);
            final int intArray[] = new int[sa.length];
            for (int a = 0; a < sa.length; a++) {
                try {
                    intArray[a] = Integer.parseInt(sa[a]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
                }
            }
            return intArray;
        }

        @Override
        public int getIntParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                return NOT_FOUND;
            }
            try {
                return Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public long getLongParam(final String paramName) throws OXException {
            final String tmp = map.get(paramName);
            if (tmp == null) {
                return NOT_FOUND;
            }
            try {
                return Long.parseLong(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public String getStringParam(final String paramName) {
            return map.get(paramName);
        }

        @Override
        public HttpServletResponse getHttpServletResponse() {
            return null;
        }

    }

    private static final class HttpParamContainer extends ParamContainer {

        private final HttpServletRequest req;
        private final HttpServletResponse resp;

        /**
         * @param req
         * @param resp
         */
        public HttpParamContainer(HttpServletRequest req, HttpServletResponse resp) {
            this.req = req;
            this.resp = resp;
        }

        @Override
        public Set<String> getParameterNames() {
            final Set<String> ret = new HashSet<String>();
            for (final Enumeration<?> enumeration = req.getParameterNames(); enumeration.hasMoreElements();) {
                ret.add((String) enumeration.nextElement());
            }
            return ret;
        }

        @Override
        public Date checkDateParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return new Date(Long.parseLong(tmp));
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public int[] checkIntArrayParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            final String[] sa = SPLIT.split(tmp, 0);
            final int intArray[] = new int[sa.length];
            for (int a = 0; a < sa.length; a++) {
                try {
                    intArray[a] = Integer.parseInt(sa[a]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
                }
            }
            return intArray;
        }

        @Override
        public long checkLongParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return Long.parseLong(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public int checkIntParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public String checkStringParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            return tmp;
        }

        @Override
        public Date getDateParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                return null;
            }
            try {
                return new Date(Long.parseLong(tmp));
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public String getHeader(final String hdrName) {
            return req.getHeader(hdrName);
        }

        @Override
        public int[] getIntArrayParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                return null;
            }
            final String[] sa = SPLIT.split(tmp, 0);
            final int intArray[] = new int[sa.length];
            for (int a = 0; a < sa.length; a++) {
                try {
                    intArray[a] = Integer.parseInt(sa[a]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
                }
            }
            return intArray;
        }

        @Override
        public int getIntParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                return NOT_FOUND;
            }
            try {
                return Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public long getLongParam(final String paramName) throws OXException {
            final String tmp = req.getParameter(paramName);
            if (tmp == null) {
                return NOT_FOUND;
            }
            try {
                return Long.parseLong(tmp);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, tmp, paramName);
            }
        }

        @Override
        public String getStringParam(final String paramName) {
            return req.getParameter(paramName);
        }

        @Override
        public HttpServletResponse getHttpServletResponse() {
            return resp;
        }
    }

    private static final class JSONParamContainer extends ParamContainer {

        private final JSONObject jo;

        /**
         * @param jo
         */
        public JSONParamContainer(JSONObject jo) {
            super();
            this.jo = jo;
        }

        @Override
        public Set<String> getParameterNames() {
            return jo.keySet();
        }

        @Override
        public Date checkDateParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return new Date(jo.getLong(paramName));
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public int[] checkIntArrayParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            String[] tmp;
            try {
                tmp = SPLIT.split(jo.getString(paramName), 0);
            } catch (JSONException e1) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e1, jo.opt(paramName), paramName);
            }
            final int[] intArray = new int[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                try {
                    intArray[i] = Integer.parseInt(tmp[i]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
                }
            }
            return intArray;
        }

        @Override
        public int checkIntParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return jo.getInt(paramName);
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public long checkLongParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return jo.getLong(paramName);
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public String checkStringParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                throw ParamContainerExceptionCode.MISSING_PARAMETER.create(paramName);
            }
            try {
                return jo.getString(paramName);
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public Date getDateParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                return null;
            }
            try {
                return new Date(jo.getLong(paramName));
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public String getHeader(final String hdrName) {
            return null;
        }

        @Override
        public int[] getIntArrayParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                return null;
            }
            String[] tmp;
            try {
                tmp = SPLIT.split(jo.getString(paramName), 0);
            } catch (JSONException e1) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e1, jo.opt(paramName), paramName);
            }
            final int[] intArray = new int[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                try {
                    intArray[i] = Integer.parseInt(tmp[i]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
                }
            }
            return intArray;
        }

        @Override
        public int getIntParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                return NOT_FOUND;
            }
            try {
                return jo.getInt(paramName);
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public long getLongParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                return NOT_FOUND;
            }
            try {
                return jo.getLong(paramName);
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public String getStringParam(final String paramName) throws OXException {
            if (!jo.has(paramName) || jo.isNull(paramName)) {
                return null;
            }
            try {
                return jo.getString(paramName);
            } catch (JSONException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, jo.opt(paramName), paramName);
            }
        }

        @Override
        public HttpServletResponse getHttpServletResponse() {
            return null;
        }
    }

    private static final class RequestDataParamContainer extends ParamContainer {

        private final AJAXRequestData requestData;

        /**
         * @param requestData
         */
        public RequestDataParamContainer(AJAXRequestData requestData) {
            super();
            this.requestData = requestData;
        }

        @Override
        public Set<String> getParameterNames() {
            return requestData.getParameters().keySet();
        }

        @Override
        public Date checkDateParam(final String paramName) throws OXException {
            String value = requestData.checkParameter(paramName);

            try {
                return new Date(Long.parseLong(value));
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
            }
        }

        @Override
        public int[] checkIntArrayParam(final String paramName) throws OXException {
            String value = requestData.checkParameter(paramName);

            String[] tmp = SPLIT.split(value, 0);
            final int[] intArray = new int[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                try {
                    intArray[i] = Integer.parseInt(tmp[i]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
                }
            }
            return intArray;
        }

        @Override
        public int checkIntParam(final String paramName) throws OXException {
            String value = requestData.checkParameter(paramName);

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
            }
        }

        @Override
        public long checkLongParam(final String paramName) throws OXException {
            String value = requestData.checkParameter(paramName);

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
            }
        }

        @Override
        public String checkStringParam(final String paramName) throws OXException {
            return requestData.checkParameter(paramName);
        }

        @Override
        public Date getDateParam(final String paramName) throws OXException {
            String value = requestData.getParameter(paramName);
            if (null == value) {
                return null;
            }

            try {
                return new Date(Long.parseLong(value));
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
            }
        }

        @Override
        public String getHeader(final String hdrName) {
            return null;
        }

        @Override
        public int[] getIntArrayParam(final String paramName) throws OXException {
            String value = requestData.getParameter(paramName);
            if (null == value) {
                return null;
            }

            String[] tmp = SPLIT.split(value, 0);
            final int[] intArray = new int[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                try {
                    intArray[i] = Integer.parseInt(tmp[i]);
                } catch (NumberFormatException e) {
                    throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
                }
            }
            return intArray;
        }

        @Override
        public int getIntParam(final String paramName) throws OXException {
            String value = requestData.getParameter(paramName);
            if (null == value) {
                return NOT_FOUND;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
            }
        }

        @Override
        public long getLongParam(final String paramName) throws OXException {
            String value = requestData.getParameter(paramName);
            if (null == value) {
                return NOT_FOUND;
            }

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw ParamContainerExceptionCode.BAD_PARAM_VALUE.create(e, value, paramName);
            }
        }

        @Override
        public String getStringParam(final String paramName) throws OXException {
            return requestData.getParameter(paramName);
        }

        @Override
        public HttpServletResponse getHttpServletResponse() {
            return requestData.optHttpServletResponse();
        }
    }

    public static final int NOT_FOUND = -9999;

    public static ParamContainer getInstance(final HttpServletRequest req, final HttpServletResponse resp) {
        return new HttpParamContainer(req, resp);
    }

    public static ParamContainer getInstance(final JSONObject jo) {
        return new JSONParamContainer(jo);
    }

    public static ParamContainer getInstance(final AJAXRequestData requestData) {
        return new RequestDataParamContainer(requestData);
    }

    public static ParamContainer getInstance(final Map<String, String> map) {
        return new MapParamContainer(map);
    }

    /**
     * Gets a parameter as String
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>String</code> or <code>null</code> if not found
     * @throws OXException If parameter could not be found
     */
    public abstract String getStringParam(String paramName) throws OXException;

    /**
     * Gets the parameter names.
     *
     * @return The parameter names
     */
    public abstract Set<String> getParameterNames();

    /**
     * Requires a parameter as <code>String</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>String</code>
     * @throws OXException If parameter could not be found
     */
    public abstract String checkStringParam(String paramName) throws OXException;

    /**
     * Gets a parameter as <code>int</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>int</code> or constant <code>NOT_FOUND</code> if not found
     * @throws OXException If parameter could not be found
     */
    public abstract int getIntParam(String paramName) throws OXException;

    /**
     * Requires a parameter as <code>int</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>int</code>
     * @throws OXException If parameter could not be found
     */
    public abstract int checkIntParam(String paramName) throws OXException;

    /**
     * Gets a parameter as <code>long</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>long</code> or constant <code>NOT_FOUND</code> if not found
     * @throws OXException If parameter could not be found
     */
    public abstract long getLongParam(String paramName) throws OXException;

    /**
     * Requires a parameter as <code>long</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>long</code>
     * @throws OXException If parameter could not be found
     */
    public abstract long checkLongParam(String paramName) throws OXException;

    /**
     * Gets a parameter as an array of <code>int</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as an array of <code>int</code> or <code>null</code> if not found
     * @throws OXException If parameter could not be found
     */
    public abstract int[] getIntArrayParam(String paramName) throws OXException;

    /**
     * Requires a parameter as an array of <code>int</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as an array of <code>int</code>
     * @throws OXException If parameter could not be found
     */
    public abstract int[] checkIntArrayParam(String paramName) throws OXException;

    /**
     * Gets a parameter as a <code>java.util.Date</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as an array of <code>java.util.Date</code> or <code>null</code> if not found
     * @throws OXException If parameter could not be found
     */
    public abstract Date getDateParam(String paramName) throws OXException;

    /**
     * Requires a parameter as a <code>java.util.Date</code>
     *
     * @param paramName - the parameter name
     * @return parameter value as <code>java.util.Date</code>
     * @throws OXException If parameter could not be found
     */
    public abstract Date checkDateParam(String paramName) throws OXException;

    /**
     * Gets a header
     *
     * @param hdrName - the header name
     * @return the header as <code>String</code> or <code>null</code> if not found
     */
    public abstract String getHeader(String hdrName);

    /**
     * Gets the <code>javax.servlet.http.HttpServletResponse</code> instance
     *
     * @return the <code>javax.servlet.http.HttpServletResponse</code> instance if present; otherwise <code>null</code>
     */
    public abstract HttpServletResponse getHttpServletResponse();

}
