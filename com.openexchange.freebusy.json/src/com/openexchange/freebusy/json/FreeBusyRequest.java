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

package com.openexchange.freebusy.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FreeBusyRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyRequest {

    private static final String PARAMETER_FROM = "from";
    private static final String PARAMETER_UNTIL = "until";
    private static final String PARAMETER_PARTICIPANT = "participant";
    private static final String PARAMETER_MERGED = "merged";
    private static final String PARAMETER_DATA = "data";

    private final AJAXRequestData request;
    private final ServerSession session;

    /**
     * Initializes a new {@link FreeBusyRequest}.
     *
     * @param request The AJAX request data
     * @param session The session
     * @throws OXException
     */
    public FreeBusyRequest(AJAXRequestData request, ServerSession session) throws OXException {
        super();
        this.request = request;
        this.session = session;
    }

    /**
     * Gets the 'from' parameter from the request.
     *
     * @return The 'from' date
     * @throws OXException
     */
    public Date getFrom() throws OXException {
        return getDate(PARAMETER_FROM);
    }

    /**
     * Gets the 'until' parameter from the request.
     *
     * @return The 'until' date
     * @throws OXException
     */
    public Date getUntil() throws OXException {
        return getDate(PARAMETER_UNTIL);
    }

    public String getParticipant() throws OXException {
        String value = request.getParameter(PARAMETER_PARTICIPANT);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_PARTICIPANT);
        } else {
            return value;
        }
    }

    public boolean isMerged() throws OXException {
        String value = request.getParameter(PARAMETER_MERGED);
        return null != value ? Boolean.parseBoolean(value) : false;
    }

    public List<String> getParticipants() throws OXException {
        JSONArray jsonArray = (JSONArray)request.getData();
        if (null == jsonArray) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_DATA);
        }
        List<String> participants = new ArrayList<String>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            String participant = null;
            try {
                participant = jsonArray.getString(i);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAMETER_DATA, jsonArray);
            }
            if (null == participant) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAMETER_DATA, jsonArray);
            }
            participants.add(participant);
        }
        return participants;
    }

    public ServerSession getSession() {
        return session;
    }

    public TimeZone getTimeZone() {
        String timeZone = request.getParameter("timezone");
        return TimeZoneUtils.getTimeZone(null != timeZone ? timeZone : session.getUser().getTimeZone());
    }

    private Date getDate(String parameterName) throws OXException {
        String value = request.getParameter(parameterName);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameterName);
        }
        try {
            return new Date(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, value);
        }
    }

}
