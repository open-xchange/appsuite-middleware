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

package com.openexchange.tasks.json.actions;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link TaskAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */

@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.READ)
public abstract class TaskAction implements AJAXActionService {

    protected static final String MODULE = TaskActionFactory.MODULE;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAction.class);

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
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        if (!session.getUserPermissionBits().hasTask()) {
            throw OXException.noPermissionForModule("task");
        }
        try {
            final TaskRequest taskRequest = new TaskRequest(requestData, session);
            final String sTimeZone = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            if (null != sTimeZone) {
                taskRequest.setTimeZone(getTimeZone(sTimeZone));
            }
            return perform(taskRequest);
        } catch (JSONException e) {
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
            if (Task.START_TIME == col) {
                tmp.add(CalendarObject.START_DATE);
            } else if (Task.END_TIME == col) {
                tmp.add(CalendarObject.END_DATE);
            } else if (col != DataObject.LAST_MODIFIED_UTC) {
                tmp.add(col);
            }
        }
        return tmp.toArray();
    }

    /**
     * Gets the column identifier to use for sorting the results if defined. If a virtual identifier is passed from the client, it is
     * implicitly mapped to the corresponding real column identifier.
     *
     * @param request The task request to get the order by information
     * @return The column identifier to use for sorting the results, or {@link TaskRequest#NOT_FOUND} if not set
     */
    protected int getOrderBy(TaskRequest request) throws OXException {
        int sort = request.optInt(AJAXServlet.PARAMETER_SORT);
        if (TaskRequest.NOT_FOUND != sort) {
            if (Task.START_TIME == sort) {
                return Task.START_DATE;
            } else if (Task.END_TIME == sort) {
                return Task.END_DATE;
            }
        }
        return sort;
    }

    /**
     * Gets the result filled with JSON <code>NULL</code>.
     *
     * @return The result with JSON <code>NULL</code>.
     */
    protected static AJAXRequestResult getJSONNullResult() {
        return RESULT_JSON_NULL;
    }

    protected void convertExternalToInternalUsersIfPossible(final CalendarObject appointmentObj, final Context ctx) {
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
                } catch (OXException e) {
                    //log.error(e); // ...and continue doing this for the remaining users
                }
            }
        }

        appointmentObj.setParticipants(participants);
    }

    /**
     * Increments the object use count
     *
     * @param session The {@link Session}
     * @param task The {@link Task}
     * @throws OXException if the object count cannot be incremented
     */
    void countObjectUse(Session session, Task task) throws OXException {
        if (null == task) {
            return;
        }
        ObjectUseCountService service = services.getService(ObjectUseCountService.class);
        PrincipalUseCountService principalUseCountService = services.getService(PrincipalUseCountService.class);
        if (null == service && principalUseCountService == null) {
            LOGGER.debug("The 'ObjectUseCountService' and the 'PrincipalUseCountService' are both unavailable at the moment");
            return;
        }
        if (!task.containsParticipants()) {
            return;
        }
        for (Participant p : task.getParticipants()) {
            switch (p.getType()) {
                case Participant.USER:
                    if (null == service) {
                        continue;
                    }
                    if (p.getIdentifier() != session.getUserId()) {
                        IncrementArguments arguments = new IncrementArguments.Builder(p.getIdentifier()).build();
                        service.incrementObjectUseCount(session, arguments);
                    }
                    break;
                case Participant.EXTERNAL_USER:
                    if (null == service) {
                        continue;
                    }
                    IncrementArguments arguments = new IncrementArguments.Builder(p.getEmailAddress()).build();
                    service.incrementObjectUseCount(session, arguments);
                    break;
                case Participant.GROUP:
                case Participant.RESOURCE:
                    if (null == principalUseCountService) {
                        continue;
                    }
                    principalUseCountService.increment(session, p.getIdentifier());
                    break;
                default:
                    LOGGER.debug("Skipping participant type '{}'", I(p.getType()));
                    break;
            }
        }
    }
}
