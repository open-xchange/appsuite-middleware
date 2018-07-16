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

package com.openexchange.chronos.json.converter;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link AlarmChecker}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AlarmChecker {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmChecker.class);

    ServiceLookup services;

    /**
     * Initializes a new {@link AlarmChecker}.
     */
    public AlarmChecker(ServiceLookup lookup) {
        this.services = lookup;
    }

    /**
     * Checks the given event if it contains mail alarms which doesn't contain all necessary fields and fills them up if possible.
     *
     * @param session The user session
     * @param event The event to check
     * @throws OXException
     */
    public void checkEvent(Session session, Event event) throws OXException {
        checkAlarmList(session, event, event.getAlarms());
    }

    /**
     * Checks the given alarms if they contain mail alarms which doesn't contain all necessary fields and fills them up if possible.
     *
     * @param session The user session
     * @param event An optional event the alarms belong to
     * @param alarms The alarms of the event
     * @throws OXException
     */
    public void checkAlarmList(Session session, Event event, List<Alarm> alarms) throws OXException {
        for (Alarm alarm : alarms) {
            if (alarm.containsAction() && AlarmAction.EMAIL.equals(alarm.getAction())) {
                fillAlarm(session, event, alarm);
            }
        }
    }

    /**
     * Fills all necessary fields of the alarm if possible
     *
     * @param session The user session
     * @param event An optional event the alarm belongs to
     * @param alarm The mail alarm
     * @throws OXException
     */
    private void fillAlarm(Session session, Event event, Alarm alarm) throws OXException {
        fillAttendees(session, event, alarm);
        if (!alarm.containsSummary()) {
            alarm.setSummary(""); // TODO proper summary
        }
        if (!alarm.containsDescription()) {
            alarm.setDescription(""); // TODO proper description
        }
    }

    /**
     * Fills the attendees of the given mail alarms with all necessary fields if possible
     *
     * @param session The user session
     * @param event An optional event the alarm belongs to
     * @param alarm A mail alarm
     * @throws OXException
     */
    private void fillAttendees(Session session, Event event, Alarm alarm) throws OXException {
        if (!alarm.containsAttendees() && alarm.getAttendees() == null) {
            // add user as Attendee
            Attendee attendee = new Attendee();
            CalendarUser calendarUser = event == null ? null : event.getCalendarUser();

            if (calendarUser != null && calendarUser.getEMail() != null) {
                setValues(attendee, calendarUser.getEMail(), calendarUser.getEntity());
            } else {
                UserService userService = getUserService();
                if (userService == null) {
                    return;
                }
                User user = userService.getUser(session.getUserId(), session.getContextId());
                setValues(attendee, user.getMail(), session.getUserId());
            }
            alarm.setAttendees(Collections.singletonList(attendee));
        } else {
            for (Attendee att : alarm.getAttendees()) {
                fillAttendee(session, att);
            }
        }
    }

    /**
     * Gets the {@link UserService}
     *
     * @return The {@link UserService} or null if not available
     */
    private UserService getUserService() {
        UserService userService = services.getOptionalService(UserService.class);
        if (userService == null) {
            LOG.info("Can't fill up mail alarm attendess. The user user service is missing.");
        }
        return userService;
    }

    /**
     * Creates a mailto uri from a mail address
     *
     * @param mail The mail address
     * @return The uri
     */
    private String getUri(String mail) {
        return "mailto:" + mail;
    }

    /**
     * Sets the necessary fields for an email alarm attendee.
     *
     * @param att The Attendee to the set the values for
     * @param mail The mail address
     * @param entity The entity
     */
    private void setValues(Attendee att, String mail, int entity) {
        att.setEntity(entity);
        att.setEMail(mail);
        att.setUri(getUri(mail));
    }

    /**
     * Fills the given attendee with all necessary fields if possible
     *
     * @param session The user session
     * @param attendee The Attendee
     */
    private void fillAttendee(Session session, Attendee attendee) {
        if (attendee.containsUri()) {
            String eMailAddress = CalendarUtils.extractEMailAddress(attendee.getUri());
            if (!attendee.containsEMail() || attendee.getEMail() == null) {
                attendee.setEMail(eMailAddress);
            }
            return;
        }

        if (!attendee.containsEMail() || attendee.getEMail() == null) {
            if (attendee.containsEntity() && attendee.containsCuType() && attendee.getCuType().equals(CalendarUserType.INDIVIDUAL)) {
                User user;
                try {
                    UserService userService = getUserService();
                    if (userService == null) {
                        return;
                    }
                    user = userService.getUser(attendee.getEntity(), session.getContextId());
                    if (user != null) {
                        setValues(attendee, user.getMail(), session.getUserId());
                    }
                } catch (OXException e) {
                    // nothing can be done
                }
            }
        }
    }

}
