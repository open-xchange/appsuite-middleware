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

package com.openexchange.chronos.common;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
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
        if(alarms!=null) {
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
        if(alarms!=null) {
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
