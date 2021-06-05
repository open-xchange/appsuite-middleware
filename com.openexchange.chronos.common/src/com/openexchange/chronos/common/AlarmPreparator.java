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

package com.openexchange.chronos.common;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AlarmPreparator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AlarmPreparator {


    private static final AlarmPreparator INSTANCE = new AlarmPreparator();

    public static AlarmPreparator getInstance() {
        return INSTANCE;
    }


    /**
     * Initializes a new {@link AlarmPreparator}.
     */
    private AlarmPreparator() {
        super();
    }

    /**
     * Prepares all eMail alarms
     *
     * @param session The calendar session
     * @param alarms The alarms of the event
     * @throws OXException
     */
    public void prepareEMailAlarms(CalendarSession session, List<Alarm> alarms) throws OXException {
        if (alarms!=null) {
            for (Alarm alarm : alarms) {
                if (alarm.containsAction() && AlarmAction.EMAIL.equals(alarm.getAction())) {
                    prepareAlarm(session.getEntityResolver(), session.getUserId(), alarm);
                }
            }
        }
    }

    /**
     * Prepares all eMail alarms
     *
     * @param session The calendar session
     * @param alarms The alarms of the event
     * @throws OXException
     */
    public void prepareEMailAlarms(Session session, CalendarUtilities calUtil, List<Alarm> alarms) throws OXException {
        if (alarms!=null) {
            for (Alarm alarm : alarms) {
                if (alarm.containsAction() && AlarmAction.EMAIL.equals(alarm.getAction())) {
                    prepareAlarm(calUtil == null ? null : calUtil.getEntityResolver(session.getContextId()), session.getUserId(), alarm);
                }
            }
        }
    }

    /**
     * Prepares a single eMail alarm
     *
     * @param session The calendar session
     * @param alarm The mail alarm
     * @throws OXException
     */
    private void prepareAlarm(EntityResolver resolver, int userId, Alarm alarm) throws OXException {
        prepareAttendees(resolver, userId, alarm);
        if (!alarm.containsSummary()) {
            alarm.setSummary("Reminder");
        }
        if (!alarm.containsDescription()) {
            alarm.setDescription("Reminder");
        }
        /*
         * ensure the alarm-agent is set to "server", too
         * see also https://tools.ietf.org/html/draft-daboo-valarm-extensions-04#section-7
         */
        AlarmUtils.addExtendedProperty(alarm, new ExtendedProperty("ALARM-AGENT", "SERVER"), true);
    }

    /**
     * Prepares the attendee list of an eMail alarm by setting it to only the current user
     *
     * @param session The calendar session
     * @param alarm A mail alarm
     * @throws OXException
     */
    private void prepareAttendees(EntityResolver entityResolver, int userId, Alarm alarm) throws OXException {
        // add current user as the only attendee
        if (entityResolver != null) {
            Attendee attendee = new Attendee();
            attendee.setEntity(userId);
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
            entityResolver.applyEntityData(attendee);
            alarm.setAttendees(Collections.singletonList(attendee));
        }
    }
}
