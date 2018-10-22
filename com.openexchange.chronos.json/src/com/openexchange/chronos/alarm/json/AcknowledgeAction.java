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

package com.openexchange.chronos.alarm.json;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_PUSH_TOKEN;
import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
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

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAMETER_PUSH_TOKEN);

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
        Date now = new Date();
        Integer alarmId = (Integer) parseAlarmParameter(requestData, AlarmParameters.PARAMETER_ALARM_ID, true);
        EventID eventID = parseIdParameter(requestData);
        Event event = calendarAccess.getEvent(eventID);
        List<Alarm> alarms = event.getAlarms();
        if (null == alarms) {
            return new AJAXRequestResult();
        }
        for (Alarm alarm : alarms) {
            if (alarm.getId() == alarmId.intValue()) {
                alarm.setAcknowledged(now);
                break;
            }
        }

        CalendarResult updateAlarms = calendarAccess.updateAlarms(eventID, alarms, event.getTimestamp());
        return new AJAXRequestResult(updateAlarms, CalendarResultConverter.INPUT_FORMAT);
    }

}
