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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.calendar.json;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.calendar.json.actions.AppointmentAction;
import com.openexchange.exception.OXException;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AppointmentAJAXRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AppointmentAJAXRequest {

    private final ServerSession session;

    private final AJAXRequestData request;

    private TimeZone timeZone;

    /**
     * Initializes a new {@link AppointmentAJAXRequest}.
     *
     * @param session The session
     * @param request The request
     */
    public AppointmentAJAXRequest(final AJAXRequestData request, final ServerSession session) {
        super();
        this.request = request;
        this.session = session;
        timeZone = TimeZoneUtils.getTimeZone(session.getUser().getTimeZone());
    }

    /**
     * Requires <code>int</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>int</code>
     * @throws OXException If parameter is missing or not a number
     */
    public int checkInt(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        try {
            return Integer.parseInt(parameter.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    /**
     * Gets the data object.
     *
     * @return The data object or <code>null</code> if no data object available
     */
    public <V> V getData() {
        return (V) request.getData();
    }

    /**
     * Sets the time zone.
     *
     * @param timeZone The time zone
     */
    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Gets the time zone (initially set to session's user one).
     *
     * @return The time zone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Date getDate(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            return null;
        }
        try {
            return new Date(Long.parseLong(parameter.trim()));
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    public Date optDate(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            return null;
        }
        try {
            return new Date(Long.parseLong(parameter.trim()));
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    public Date checkDate(final String name) throws OXException {
        final Date date = optDate(name);
        if (null == date) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return date;
    }

    public String checkParameter(final String name) throws OXException {
        return request.checkParameter(name);
    }

    public String getParameter(final String name) {
        return request.getParameter(name);
    }

    /**
     * Constant for not-found number.
     */
    public static final int NOT_FOUND = -9999;

    /**
     * Gets optional <code>int</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>int</code> or {@link #NOT_FOUND} (<code>-9999</code>)
     * @throws OXException If parameter is an invalid number value
     */
    public int optInt(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            return NOT_FOUND;
        }
        try {
            return Integer.parseInt(parameter.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    /**
     * Split pattern for CSV.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Checks for presence of comma-separated <code>int</code> list.
     *
     * @param name The parameter name
     * @return The <code>int</code> array
     * @throws OXException If an error occurs
     */
    public int[] checkIntArray(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        if (name.equals(AJAXServlet.PARAMETER_COLUMNS)) {
            if (parameter.equals("all")) {
                return AppointmentAction.COLUMNS_ALL_ALIAS;
            }
            if (parameter.equals("list")) {
                return AppointmentAction.COLUMNS_LIST_ALIAS;
            }
        }
        final String[] sa = SPLIT.split(parameter, 0);
        final int[] ret = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            try {
                ret[i] = Integer.parseInt(sa[i].trim());
            } catch (final NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
            }
        }
        return ret;
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     *
     * @param name The parameter name
     * @return The <code>String</code> array
     * @throws OXException If parameter is absdent
     */
    public String[] checkStringArray(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     *
     * @param name The parameter name
     * @return The <code>String</code> array
     */
    public String[] optStringArray(final String name) {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            return null;
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Gets the request.
     *
     * @return The request
     */
    public AJAXRequestData getRequest() {
        return request;
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Creates a new <code>java.util.Date</code> instance with this request's time zone offset subtracted from specified UTC time.
     *
     * @param utcTime The UTC time
     * @return A new <code>java.util.Date</code> with time zone offset applied
     */
    public Date applyTimeZone2Date(final long utcTime) {
        return new Date(utcTime - timeZone.getOffset(utcTime));
    }

    /**
     * Parses specified <code>boolean</code> parameter
     *
     * @param name The parameter name
     * @return <code>true</code> if the string argument is not <code>null</code> and is equal, ignoring case, to the string
     *         <code>"true"</code>; otherwise <code>false</code>
     */
    public boolean parseBoolean(final String name) {
        return Boolean.parseBoolean(getParameter(name));
    }

    /**
     * Checks specified <code>boolean</code> parameter
     *
     * @param name The parameter name
     * @return <code>true</code> if the string argument is equal, ignoring case, to the string <code>"true"</code>; otherwise
     *         <code>false</code>
     * @throws OXException If parameter is absent
     */
    public boolean checkBoolean(final String name) throws OXException {
        final String value = getParameter(name);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return Boolean.parseBoolean(value);
    }

    public Date checkTime(final String name, final TimeZone timeZone) throws OXException, OXException {
        final String tmp = getParameter(name);
        if (tmp == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        try {
            final Date d = new Date(Long.parseLong(tmp));
            final int offset = timeZone.getOffset(d.getTime());
            d.setTime(d.getTime() - offset);
            return d;
        } catch (final NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    public int getFolderId() throws OXException {
        final int optInt = optInt(AJAXServlet.PARAMETER_FOLDERID);
        if (optInt == NOT_FOUND) {
            return 0;
        }
        return optInt;
    }

}
