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

package com.openexchange.tasks.json.actions;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link TaskAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */

public abstract class TaskAction implements AJAXActionService {

    public static final int[] COLUMNS_ALL_ALIAS = new int[] { 20, 1, 2, 5, 4 };

    public static final int[] COLUMNS_LIST_ALIAS = new int[] { 20, 1, 5, 2, 4, 209, 301, 101, 200, 309, 201, 202, 102 };

    private static final AJAXRequestResult RESULT_JSON_NULL = new AJAXRequestResult(JSONObject.NULL, "json");

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractTaskAction}.
     */
    protected TaskAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> is absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        if (!session.getUserConfiguration().hasTask()) {
            throw OXException.noPermissionForModule("task");
        }
        try {
            final TaskRequest taskRequest = new TaskRequest(requestData, session);
            final String sTimeZone = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            if (null != sTimeZone) {
                taskRequest.setTimeZone(getTimeZone(sTimeZone));
            }
            return perform(taskRequest);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs specified task request.
     *
     * @param req The task request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(TaskRequest req) throws OXException, JSONException;

    protected int[] removeVirtualColumns(final int[] columns) {
        final TIntList tmp = new TIntArrayList(columns.length);
        for (final int col : columns) {
            if (col != DataObject.LAST_MODIFIED_UTC) {
                tmp.add(col);
            }
        }
        return tmp.toArray();
    }

    /**
     * Gets the result filled with JSON <code>NULL</code>.
     *
     * @return The result with JSON <code>NULL</code>.
     */
    protected static AJAXRequestResult getJSONNullResult() {
        return RESULT_JSON_NULL;
    }

    protected static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    protected void convertExternalToInternalUsersIfPossible(final CalendarObject appointmentObj, final Context ctx, final Log log) {
        final Participant[] participants = appointmentObj.getParticipants();
        if (participants == null) {
            return;
        }

        final UserService us = getService(UserService.class);

        for (int pos = 0; pos < participants.length; pos++) {
            final Participant part = participants[pos];
            if (part.getType() == Participant.EXTERNAL_USER) {
                User foundUser;
                try {
                    foundUser = us.searchUser(part.getEmailAddress(), ctx);
                    if (foundUser == null) {
                        continue;
                    }
                    participants[pos] = new UserParticipant(foundUser.getId());
                } catch (final OXException e) {
                    //log.error(e); // ...and continue doing this for the remaining users
                }
            }
        }

        appointmentObj.setParticipants(participants);
    }

}
