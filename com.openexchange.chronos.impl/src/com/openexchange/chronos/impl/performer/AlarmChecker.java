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

package com.openexchange.chronos.impl.performer;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AlarmChecker}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AlarmChecker {

    ServiceLookup services;

    /**
     * Initializes a new {@link AlarmChecker}.
     */
    public AlarmChecker(ServiceLookup lookup) {
        this.services = lookup;
    }

    /**
     * Checks the given alarms if they contain mail alarms which doesn't contain all necessary fields and fills them up if possible.
     *
     * @param session The user session
     * @param alarms The alarms of the event
     * @throws OXException
     */
    public void checkAlarmList(Session session, List<Alarm> alarms) throws OXException {
        if(alarms!=null) {
            for (Alarm alarm : alarms) {
                if (alarm.containsAction() && AlarmAction.EMAIL.equals(alarm.getAction())) {
                    fillAlarm(session, alarm);
                }
            }
        }
    }

    /**
     * Fills all necessary fields of the alarm if possible
     *
     * @param session The user session
     * @param alarm The mail alarm
     * @throws OXException
     */
    private void fillAlarm(Session session, Alarm alarm) throws OXException {
        fillAttendees(session, alarm);
        if (!alarm.containsSummary()) {
            alarm.setSummary("Reminder");
        }
        if (!alarm.containsDescription()) {
            alarm.setDescription("Reminder");
        }
    }

    /**
     * Fills the attendees of the given mail alarms with all necessary fields if possible
     *
     * @param session The user session
     * @param alarm A mail alarm
     * @throws OXException
     */
    private void fillAttendees(Session session, Alarm alarm) throws OXException {
        // add current user as the only attendee
        EntityResolver entityResolver = optEntityResolver(session.getContextId());
        if (entityResolver != null) {
            Attendee attendee = new Attendee();
            attendee.setEntity(session.getUserId());
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
            entityResolver.applyEntityData(attendee);
            alarm.setAttendees(Collections.singletonList(attendee));
        }
    }

    /**
     * Optionally gets an entity resolver for the context.
     *
     * @param contextId The context id
     * @return The entity resolver, or <code>null</code> if not available
     */
    protected EntityResolver optEntityResolver(int contextId) throws OXException {
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        return null != calendarUtilities ? calendarUtilities.getEntityResolver(contextId) : null;
    }

}
