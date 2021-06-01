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

package com.openexchange.chronos.alarm.json;

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.json.action.ChronosAction;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdateAlarmsAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@RestrictedAction(module = ChronosAction.MODULE, type = RestrictedAction.Type.WRITE)
public class UpdateAlarmsAction extends ChronosAction {

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_PUSH_TOKEN, PARAM_EXPAND, PARAM_SCHEDULING, PARAM_RANGE_START, PARAM_RANGE_END, PARAM_FIELDS);

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    /**
     * Initializes a new {@link UpdateAlarmsAction}.
     *
     * @param services A service lookup reference
     */
    protected UpdateAlarmsAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        long clientTimestamp = parseClientTimestamp(requestData);
        JSONArray jsonArray = requestData.getData(JSONArray.class);
        if (null == jsonArray) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        EventID eventID = parseIdParameter(requestData);
        List<Alarm> alarms = parseAlarms(jsonArray, getTimeZone(requestData));
        CalendarResult calendarResult = calendarAccess.updateAlarms(eventID, alarms, clientTimestamp);
        return new AJAXRequestResult(calendarResult, new Date(calendarResult.getTimestamp()), CalendarResultConverter.INPUT_FORMAT);
    }

}
