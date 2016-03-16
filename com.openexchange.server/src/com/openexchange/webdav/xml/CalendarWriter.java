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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.jdom2.Element;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.webdav.WebdavExceptionCode;
import com.openexchange.webdav.xml.fields.CalendarFields;

/**
 * WebDAV/XML writer for common appointment and task attributes.
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public abstract class CalendarWriter extends CommonWriter {

    private static final String CONFIRM_ATTRIBUTE = "confirm";

    /**
     * Constructor for subclassing.
     */
    protected CalendarWriter() {
        super();
    }

    protected void writeCalendarElements(final CalendarObject calendarobject, final Element e_prop) throws OXException, SearchIteratorException, UnsupportedEncodingException {
        addElement(CalendarFields.TITLE, calendarobject.getTitle(), e_prop);
        addElement(CalendarFields.NOTE, calendarobject.getNote(), e_prop);

        addRecurrenceElements(calendarobject, e_prop);
        addElementParticipants(calendarobject, e_prop);
        writeCommonElements(calendarobject, e_prop);
    }

    public void addRecurrenceElements(final CalendarObject calendarobject, final Element e_prop) throws OXException {
        if (calendarobject.containsRecurrenceID()) {
            addElement(CalendarFields.RECURRENCE_ID, calendarobject.getRecurrenceID(), e_prop);
        }

        if (calendarobject.containsRecurrencePosition()) {
            addElement(CalendarFields.RECURRENCE_POSITION, calendarobject.getRecurrencePosition(), e_prop);
        }

        final int recurrenceType = calendarobject.getRecurrenceType();

        switch (recurrenceType) {
            case CalendarObject.NONE:
                break;
            case CalendarObject.DAILY:
                addElement(CalendarFields.RECURRENCE_TYPE, "daily", e_prop);
                break;
            case CalendarObject.WEEKLY:
                addElement(CalendarFields.RECURRENCE_TYPE, "weekly", e_prop);
                addElement(CalendarFields.DAYS, calendarobject.getDays(), e_prop);
                break;
            case CalendarObject.MONTHLY:
                addElement(CalendarFields.RECURRENCE_TYPE, "monthly", e_prop);
                addElement(CalendarFields.DAY_IN_MONTH, calendarobject.getDayInMonth(), e_prop);

                if (calendarobject.containsDays()) {
                    addElement(CalendarFields.DAYS, calendarobject.getDays(), e_prop);
                }

                break;
            case CalendarObject.YEARLY:
                addElement(CalendarFields.RECURRENCE_TYPE, "yearly", e_prop);
                addElement(CalendarFields.DAY_IN_MONTH, calendarobject.getDayInMonth(), e_prop);

                if (calendarobject.containsDays()) {
                    addElement(CalendarFields.DAYS, calendarobject.getDays(), e_prop);
                }

                addElement(CalendarFields.MONTH, calendarobject.getMonth(), e_prop);

                break;
            default:
                throw WebdavExceptionCode.IO_ERROR.create("invalid recurrence type: " + recurrenceType);
        }

        if (calendarobject.containsInterval()) {
            addElement(CalendarFields.INTERVAL, calendarobject.getInterval(), e_prop);
        }

        if (calendarobject.containsUntil()) {
            addElement(CalendarFields.UNTIL, calendarobject.getUntil(), e_prop);
        }

        if (calendarobject.containsOccurrence()) {
            addElement(CalendarFields.OCCURRENCES, calendarobject.getOccurrence(), e_prop);
        }
    }

    public void addElementParticipants(final CalendarObject calendarobject, final Element e_prop) throws OXException {
        boolean hasParticipants = false;

        final Element e_participants = new Element(CalendarFields.PARTICIPANTS, XmlServlet.NS);

        final Participant participant[] = calendarobject.getParticipants();
        final UserParticipant[] userparticipant = calendarobject.getUsers();

        boolean hasUserParticipants = false;

        if (participant != null) {
            hasParticipants = true;

            if (userparticipant != null) {
                Arrays.sort(userparticipant);
                hasUserParticipants = true;
            }

            for (int a = 0; a < participant.length; a++) {
                final Element eParticipant;
                final int type = participant[a].getType();

                boolean external = false;

                switch (type) {
                    case Participant.USER:
                        eParticipant = new Element("user", XmlServlet.NS);
                        eParticipant.addContent(Integer.toString(participant[a].getIdentifier()));

                        if (hasUserParticipants) {
                            final int userPos = Arrays.binarySearch(userparticipant, participant[a]);

                            if (userPos >= 0) {
                                if (userparticipant[userPos].getConfirm() == CalendarObject.NONE) {
                                    eParticipant.setAttribute(CONFIRM_ATTRIBUTE, "none", XmlServlet.NS);
                                } else if (userparticipant[userPos].getConfirm() == CalendarObject.ACCEPT) {
                                    eParticipant.setAttribute(CONFIRM_ATTRIBUTE, "accept", XmlServlet.NS);
                                } else if (userparticipant[userPos].getConfirm() == CalendarObject.DECLINE) {
                                    eParticipant.setAttribute(CONFIRM_ATTRIBUTE, "decline", XmlServlet.NS);
                                } else if (userparticipant[userPos].getConfirm() == CalendarObject.TENTATIVE) {
                                    eParticipant.setAttribute(CONFIRM_ATTRIBUTE, "tentative", XmlServlet.NS);
                                } else {
                                    throw WebdavExceptionCode.IO_ERROR.create("invalid value in confirm: " + userparticipant[a].getConfirm());
                                }
                            }
                        } else {
                            eParticipant.setAttribute(CONFIRM_ATTRIBUTE, "none", XmlServlet.NS);
                        }

                        break;
                    case Participant.GROUP:
                        eParticipant = new Element("group", XmlServlet.NS);
                        eParticipant.addContent(Integer.toString(participant[a].getIdentifier()));
                        break;
                    case Participant.RESOURCE:
                        eParticipant = new Element("resource", XmlServlet.NS);
                        eParticipant.addContent(Integer.toString(participant[a].getIdentifier()));
                        break;
                    case Participant.EXTERNAL_USER:
                        eParticipant = new Element("user", XmlServlet.NS);
                        eParticipant.addContent(Integer.toString(participant[a].getIdentifier()));
                        if (participant[a].getDisplayName() != null) {
                            eParticipant.setAttribute("displayname", participant[a].getDisplayName(), XmlServlet.NS);
                        } else {
                            eParticipant.setAttribute("displayname", "", XmlServlet.NS);
                        }

                        if (participant[a].getEmailAddress() != null) {
                            eParticipant.setAttribute("mail", participant[a].getEmailAddress(), XmlServlet.NS);
                        } else {
                            eParticipant.setAttribute("mail", "", XmlServlet.NS);
                        }
                        external = true;
                        break;
                    case Participant.EXTERNAL_GROUP:
                        eParticipant = new Element("group", XmlServlet.NS);
                        eParticipant.addContent(Integer.toString(participant[a].getIdentifier()));
                        if (participant[a].getDisplayName() != null) {
                            eParticipant.setAttribute("displayname", participant[a].getDisplayName(), XmlServlet.NS);
                        } else {
                            eParticipant.setAttribute("displayname", "", XmlServlet.NS);
                        }

                        if (participant[a].getEmailAddress() != null) {
                            eParticipant.setAttribute("mail", participant[a].getEmailAddress(), XmlServlet.NS);
                        } else {
                            eParticipant.setAttribute("mail", "", XmlServlet.NS);
                        }
                        external = true;
                        break;
                    default:
                        throw WebdavExceptionCode.IO_ERROR.create("invalid type in participant: " + type);

                }

                eParticipant.setAttribute("external", String.valueOf(external), XmlServlet.NS);
                e_participants.addContent(eParticipant);
            }
        }

        if (hasParticipants) {
            e_prop.addContent(e_participants);
        }
    }
}
