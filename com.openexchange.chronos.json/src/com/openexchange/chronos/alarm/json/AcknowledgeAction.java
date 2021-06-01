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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.json.converter.mapper.AlarmMapper;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AcknowledgeAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AcknowledgeAction extends AbstractChronosAlarmAction {

    private static final Set<String> REQUIRED_PARAMETERS = unmodifiableSet(AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_FOLDERID);

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_PUSH_TOKEN);

    /**
     * Initializes a new {@link AcknowledgeAction}.
     * 
     * @param services
     */
    protected AcknowledgeAction(ServiceLookup services) {
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
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        Integer alarmId = (Integer) parseAlarmParameter(requestData, AlarmParameters.PARAMETER_ALARM_ID, true);
        EventID eventID = parseIdParameter(requestData);
        Event event;
        EventField[] originalFields = calendarAccess.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        try {
            EventField[] fields = new EventField[] { EventField.ALARMS, EventField.TIMESTAMP };
            calendarAccess.set(CalendarParameters.PARAMETER_FIELDS, fields);
            event = calendarAccess.getEvent(eventID);
        } finally {
            calendarAccess.set(CalendarParameters.PARAMETER_FIELDS, originalFields);
        }
        List<Alarm> alarms = event.getAlarms();
        if (null == alarms) {
            return new AJAXRequestResult();
        }
        List<Alarm> updatedAlarms = new ArrayList<Alarm>(alarms.size());
        for (Alarm alarm : alarms) {
            if (alarm.getId() == alarmId.intValue()) {
                alarm = AlarmMapper.getInstance().copy(alarm, null, (AlarmField[]) null);
                alarm.setAcknowledged(new Date());
            }
            updatedAlarms.add(alarm);
        }

        CalendarResult updateAlarms = calendarAccess.updateAlarms(eventID, updatedAlarms, event.getTimestamp());
        return new AJAXRequestResult(updateAlarms, CalendarResultConverter.INPUT_FORMAT);
    }

}
