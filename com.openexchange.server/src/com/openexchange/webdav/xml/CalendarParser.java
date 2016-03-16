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

package com.openexchange.webdav.xml;

import org.xmlpull.v1.XmlPullParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.WebdavExceptionCode;
import com.openexchange.webdav.xml.fields.CalendarFields;

/**
 * CalendarParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class CalendarParser extends CommonParser {

    protected void parseElementCalendar(final CalendarObject calendarobject, final XmlPullParser parser) throws Exception {
        if (isTag(parser, CalendarFields.RECURRENCE_ID)) {
            calendarobject.setRecurrenceID(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.RECURRENCE_POSITION)) {
            calendarobject.setRecurrencePosition(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.RECURRENCE_DATE_POSITION)) {
            calendarobject.setRecurrenceDatePosition(getValueAsDate(parser));
            return ;
        } else if (isTag(parser, CalendarFields.MONTH)) {
            calendarobject.setMonth(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.DAY_IN_MONTH)) {
            calendarobject.setDayInMonth(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.DAYS)) {
            calendarobject.setDays(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.INTERVAL)) {
            calendarobject.setInterval(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.UNTIL)) {
            calendarobject.setUntil(getValueAsDate(parser));
            return ;
        } else if (isTag(parser, CalendarFields.OCCURRENCES)) {
            calendarobject.setOccurrence(getValueAsInt(parser));
            return ;
        } else if (isTag(parser, CalendarFields.START_DATE)) {
            calendarobject.setStartDate(getValueAsDate(parser));
            return ;
        } else if (isTag(parser, CalendarFields.END_DATE)) {
            calendarobject.setEndDate(getValueAsDate(parser));
            return ;
        } else if (isTag(parser, CalendarFields.RECURRENCE_TYPE)) {
            calendarobject.setRecurrenceType(parseRecurrenceType(getValue(parser)));
            return ;
        } else if (isTag(parser, CalendarFields.NOTIFICATION)) {
            calendarobject.setNotification(getValueAsBoolean(parser));
            return ;
        } else if (isTag(parser, CalendarFields.TITLE)) {
            calendarobject.setTitle(getValue(parser));
            return ;
        } else if (isTag(parser, CalendarFields.NOTE)) {
            calendarobject.setNote(getValue(parser));
            return ;
        } else if (isTag(parser, CalendarFields.CONFIRM)) {
            final String s = getValue(parser);
            if ("accept".equals(s)) {
                calendarobject.setConfirm(CalendarObject.ACCEPT);
            } else if ("decline".equals(s)) {
                calendarobject.setConfirm(CalendarObject.DECLINE);
            } else if ("tentative".equals(s)) {
                calendarobject.setConfirm(CalendarObject.TENTATIVE);
            } else if ("none".equals(s)) {
                calendarobject.setConfirm(CalendarObject.NONE);
            } else {
                throw WebdavExceptionCode.IO_ERROR.create("invalid value in confirm tag");
            }
            return ;
        } else if (isTag(parser, CalendarFields.CONFIRM_MESSAGE)) {
            calendarobject.setConfirmMessage(getValue(parser));
            return;
        } else if (isTag(parser, CalendarFields.PARTICIPANTS)) {
            parseElementParticipants(calendarobject, parser);
        } else {
            parseElementCommon(calendarobject, parser);
        }
    }

    protected int parseRecurrenceType(final String value) throws Exception {
        if ("none".equals(value)) {
            return CalendarObject.NONE;
        } else if ("daily".equals(value)) {
            return CalendarObject.DAILY;
        } else if ("weekly".equals(value)) {
            return CalendarObject.WEEKLY;
        } else if ("monthly".equals(value)) {
            return CalendarObject.MONTHLY;
        } else if ("yearly".equals(value)) {
            return CalendarObject.YEARLY;
        } else {
            throw WebdavExceptionCode.IO_ERROR.create("unknown value in " + CalendarFields.RECURRENCE_TYPE + ": " + value);
        }
    }

    protected void parseElementParticipants(final CalendarObject calendarObj, final XmlPullParser parser) throws OXException {
        final Participants participants = new Participants();

        try {
            boolean isParticipant = true;

            while (isParticipant) {
                parser.nextTag();

                if (isEnd(parser)) {
                    throw WebdavExceptionCode.IO_ERROR.create("invalid xml in participant!");
                }

                if (parser.getName().equals(CalendarFields.PARTICIPANTS) && parser.getEventType() == XmlPullParser.END_TAG) {
                    isParticipant = false;
                    break;
                }

                if (isTag(parser, "user")) {
                    parseElementUser(parser, participants);
                } else if (isTag(parser, "group")) {
                    parseElementGroup(parser, participants);
                } else if (isTag(parser, "resource")) {
                    parseElementResource(parser, participants);
                } else {
                    throw WebdavExceptionCode.IO_ERROR.create("unknown xml tag in permissions!");
                }
            }

            calendarObj.setParticipants(participants.getList());
            calendarObj.setUsers(participants.getUsers());
        } catch (final Exception exc) {
            throw new OXException(exc);
        }
    }

    private void parseElementUser(final XmlPullParser parser, final Participants participants) throws Exception {
        Participant p = null;
        final String confirm = parser.getAttributeValue(XmlServlet.NAMESPACE, "confirm");
        final String external = parser.getAttributeValue(XmlServlet.NAMESPACE, "external");

        boolean isExternal = false;

        if (external != null && external.equals("true")) {
            isExternal = true;
        }

        if (isExternal) {
            final String mail = parser.getAttributeValue(XmlServlet.NAMESPACE, "mail");
            final String displayName = parser.getAttributeValue(XmlServlet.NAMESPACE, "displayname");

            p = new ExternalUserParticipant(mail);
            p.setDisplayName(displayName);

            // set parse token to next value
            getValue(parser);
        } else {
            final int userId = getValueAsInt(parser);
            final UserParticipant userparticipant = new UserParticipant(userId);

            if (confirm != null) {
                if ("accept".equals(confirm)) {
                    userparticipant.setConfirm(CalendarObject.ACCEPT);
                } else if ("decline".equals(confirm)) {
                    userparticipant.setConfirm(CalendarObject.DECLINE);
                } else if ("tentative".equals(confirm)) {
                    userparticipant.setConfirm(CalendarObject.TENTATIVE);
                } else if ("none".equals(confirm)) {
                    userparticipant.setConfirm(CalendarObject.NONE);
                } else {
                    throw WebdavExceptionCode.IO_ERROR.create("unknown value in confirm attribute: " + confirm);
                }
            }

            participants.add(userparticipant);
            p = userparticipant;
        }
        participants.add(p);
    }

    private void parseElementGroup(final XmlPullParser parser, final Participants participants) throws Exception {
        Participant p = null;

        final String external = parser.getAttributeValue(XmlServlet.NAMESPACE, "external");
        final int id = getValueAsInt(parser);

        boolean isExternal = false;

        if (external != null && external.equals("true")) {
            isExternal = true;
        }
        if (isExternal) {
            final String displayName = parser.getAttributeValue(XmlServlet.NAMESPACE, "displayname");
            final String mail = parser.getAttributeValue(XmlServlet.NAMESPACE, "mail");
            p = new ExternalGroupParticipant(mail);
            p.setDisplayName(displayName);
        } else {
            p = new GroupParticipant(id);
        }
        participants.add(p);
    }

    private void parseElementResource(final XmlPullParser parser, final Participants participants) throws Exception {
        final Participant p = new ResourceParticipant(getValueAsInt(parser));
        participants.add(p);
    }
}
