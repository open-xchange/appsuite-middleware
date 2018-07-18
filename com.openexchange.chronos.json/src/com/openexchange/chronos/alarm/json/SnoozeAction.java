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

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.json.converter.mapper.AlarmMapper;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link SnoozeAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class SnoozeAction extends AbstractChronosAlarmAction {

    private static final Set<String> REQUIRED_PARAMETERS = unmodifiableSet(AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_FOLDERID);

    /**
     * Initializes a new {@link SnoozeAction}.
     *
     * @param services
     */
    protected SnoozeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        Date now = new Date();
        int alarmId = ((Integer) parseAlarmParameter(requestData, AlarmParameters.PARAMETER_ALARM_ID, true)).intValue();
        Long snooze = (Long) parseAlarmParameter(requestData, AlarmParameters.PARAMETER_SNOOZE_DURATION, true);
        if (snooze.longValue() <= 0) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AlarmParameters.PARAMETER_SNOOZE_DURATION, "The snooze time must be greater than 0");
        }

        EventID eventID = parseIdParameter(requestData);
        Event event = calendarAccess.getEvent(eventID);
        if(event.containsAlarms() == false || event.getAlarms() == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AlarmParameters.PARAMETER_ALARM_ID, "The event doesn't contain a alarm with this id.");
        }
        List<Alarm> alarms = new ArrayList<Alarm>(event.getAlarms());

        Alarm oldAlarmToSnooze = null;
        for (Iterator<Alarm> it = alarms.iterator(); null == oldAlarmToSnooze && it.hasNext();) {
            Alarm alarm = it.next();
            if (alarm.getId() == alarmId) {
                oldAlarmToSnooze = alarm;
                oldAlarmToSnooze.setAcknowledged(now);
            }
        }
        if (oldAlarmToSnooze == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AlarmParameters.PARAMETER_ALARM_ID, "Unable to find an alarm with id " + alarmId);
        }

        Alarm newSnoozeAlarm = AlarmMapper.getInstance().copy(oldAlarmToSnooze, null, (AlarmField[]) null);
        Trigger trigger = new Trigger(new Date(now.getTime() + snooze.intValue()));
        newSnoozeAlarm.setTrigger(trigger);
        String uid = oldAlarmToSnooze.getUid();
        if (Strings.isEmpty(uid)) {
            uid = UUID.randomUUID().toString().toUpperCase();
            oldAlarmToSnooze.setUid(uid);
        }
        newSnoozeAlarm.setRelatedTo(new RelatedTo("SNOOZE", uid));
        newSnoozeAlarm.removeUid();
        newSnoozeAlarm.removeId();
        newSnoozeAlarm.removeAcknowledged();

        alarms.add(newSnoozeAlarm);

        // Remove old snooze in case it was snoozed again
        if (oldAlarmToSnooze.getRelatedTo() != null && (oldAlarmToSnooze.getRelatedTo().getRelType() == null || oldAlarmToSnooze.getRelatedTo().getRelType().equals("SNOOZE"))) {
            newSnoozeAlarm.setRelatedTo(oldAlarmToSnooze.getRelatedTo());
            alarms.remove(oldAlarmToSnooze);
        }

        CalendarResult updateAlarms = calendarAccess.updateAlarms(eventID, alarms, event.getTimestamp());
        return new AJAXRequestResult(updateAlarms, CalendarResultConverter.INPUT_FORMAT);
    }

}
