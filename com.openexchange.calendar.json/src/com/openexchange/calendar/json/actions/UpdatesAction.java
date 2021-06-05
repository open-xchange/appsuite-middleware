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

package com.openexchange.calendar.json.actions;

import java.util.Date;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = AppointmentAction.MODULE, type = RestrictedAction.Type.READ)
public final class UpdatesAction extends AppointmentAction {

    private static final Set<String> REQUIRED_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(AJAXServlet.PARAMETER_COLUMNS);

    private static final Set<String> OPTIONAL_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_START, AJAXServlet.PARAMETER_END, AJAXServlet.PARAMETER_RECURRENCE_MASTER, AJAXServlet.PARAMETER_TIMEZONE, AJAXServlet.PARAMETER_SORT, AJAXServlet.PARAMETER_ORDER);

    /**
     * Initializes a new {@link UpdatesAction}.
     *
     * @param services A service lookup reference
     */
    public UpdatesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        if (false == session.contains(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES)) {
            session.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.TRUE);
        }
        Date since = request.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        String folderId = request.getParameter(AJAXServlet.PARAMETER_FOLDERID);
        UpdatesResult result;
        if (null != folderId) {
            result = session.getCalendarService().getUpdatedEventsInFolder(session, folderId, since.getTime());
        } else {
            if (false == session.contains(CalendarParameters.PARAMETER_RANGE_START)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_START);
            }
            if (false == session.contains(CalendarParameters.PARAMETER_RANGE_END)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_END);
            }
            result = session.getCalendarService().getUpdatedEventsOfUser(session, since.getTime());
        }
        AJAXRequestResult deltaResult = getAppointmentDeltaResultWithTimestamp(getEventConverter(session), result.getNewAndModifiedEvents(), result.getDeletedEvents());
        if (null == deltaResult.getTimestamp() || deltaResult.getTimestamp().before(since)) {
            deltaResult.setTimestamp(since);
        }
        return deltaResult;
    }

}
