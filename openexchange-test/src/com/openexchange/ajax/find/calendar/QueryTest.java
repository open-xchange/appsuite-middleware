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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.find.calendar;

import static com.openexchange.find.calendar.CalendarFacetType.DESCRIPTION;
import static com.openexchange.find.calendar.CalendarFacetType.LOCATION;
import static com.openexchange.find.calendar.CalendarFacetType.PARTICIPANT;
import static com.openexchange.find.calendar.CalendarFacetType.RECURRING_TYPE;
import static com.openexchange.find.calendar.CalendarFacetType.RELATIVE_DATE;
import static com.openexchange.find.calendar.CalendarFacetType.STATUS;
import static com.openexchange.find.calendar.CalendarFacetType.SUBJECT;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.find.Module;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link QueryTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class QueryTest extends CalendarFindTest {

    /**
     * Initializes a new {@link QueryTest}.
     *
     * @param name The test name
     */
    public QueryTest(String name) {
        super(name);
    }

    public void testFilterChaining() throws Exception {
        Appointment appointment = randomAppointment();
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(createActiveFieldFacet(SUBJECT, "subject", randomSubstring(appointment.getTitle())));
        facets.add(createActiveFieldFacet(LOCATION, "location", randomSubstring(appointment.getLocation())));
        facets.add(createActiveFieldFacet(DESCRIPTION, "description", randomSubstring(appointment.getNote())));
        facets.add(createActiveFacet(RELATIVE_DATE, "coming", "date", "coming"));
        facets.add(createActiveFacet(STATUS, "accepted", "status", "accepted"));
        facets.add(createActiveFacet(RECURRING_TYPE, "single", "type", "single"));
        facets.add(createActiveFacet(PARTICIPANT, String.valueOf(client.getValues().getUserId()), "users", String.valueOf(client.getValues().getUserId())));
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(facets);
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));
    }

    public void testFilterSubject() throws Exception {
        Appointment appointment = randomAppointment();
        appointment.setTitle(randomUID());
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(Collections.singletonList(createActiveFieldFacet(SUBJECT, "subject", randomSubstring(appointment.getTitle()))));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));
    }

    public void testEmptyFilter() throws Exception {
        Appointment appointment = randomAppointment();
        appointment.setTitle(randomUID());
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(Collections.singletonList(createActiveFieldFacet(SUBJECT, "subject", "")));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));
    }

    public void testFilterLocation() throws Exception {
        Appointment appointment = randomAppointment();
        appointment.setLocation(randomUID());
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(Collections.singletonList(createActiveFieldFacet(LOCATION, "location", randomSubstring(appointment.getLocation()))));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "location", appointment.getLocation()));
    }

    public void testFilterDescription() throws Exception {
        Appointment appointment = randomAppointment();
        appointment.setNote(randomUID());
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(Collections.singletonList(createActiveFieldFacet(DESCRIPTION, "description", randomSubstring(appointment.getNote()))));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "note", appointment.getNote()));
    }

    public void testFilterRelativeDate() throws Exception {
        Appointment comingAppointment = randomAppointment();
        comingAppointment.setStartDate(TimeTools.D("tomorrow at noon"));
        comingAppointment.setEndDate(TimeTools.D("tomorrow at noon"));
        comingAppointment = manager.insert(comingAppointment);
        Appointment pastAppointment = randomAppointment();
        pastAppointment.setStartDate(TimeTools.D("yesterday at noon"));
        pastAppointment.setEndDate(TimeTools.D("yesterday at noon"));
        pastAppointment = manager.insert(pastAppointment);

        List<PropDocument> comingDocuments = query(Collections.singletonList(createActiveFacet(RELATIVE_DATE, "coming", "date", "coming")));
        assertTrue("no appointments found", 0 < comingDocuments.size());
        assertNotNull("coming appointment not found", findByProperty(comingDocuments, "title", comingAppointment.getTitle()));
        assertNull("past appointment found", findByProperty(comingDocuments, "title", pastAppointment.getTitle()));

        List<PropDocument> pastDocuments = query(Collections.singletonList(createActiveFacet(RELATIVE_DATE, "past", "date", "past")));
        assertTrue("no appointments found", 0 < pastDocuments.size());
        assertNotNull("past appointment not found", findByProperty(pastDocuments, "title", pastAppointment.getTitle()));
        assertNull("coming appointment found", findByProperty(pastDocuments, "title", comingAppointment.getTitle()));
    }

    public void testFilterStatus() throws Exception {
        Appointment acceptedAppointment = randomAppointment();
        acceptedAppointment = manager.insert(acceptedAppointment);
        manager.confirm(acceptedAppointment, Appointment.ACCEPT, "accept");
        Appointment declinedAppointment = randomAppointment();
        declinedAppointment = manager.insert(declinedAppointment);
        manager.confirm(declinedAppointment, Appointment.DECLINE, "decline");
        Appointment tentativeAppointment = randomAppointment();
        tentativeAppointment = manager.insert(tentativeAppointment);
        manager.confirm(tentativeAppointment, Appointment.TENTATIVE, "tentative");
        Appointment noneAppointment = randomAppointment();
        noneAppointment = manager.insert(noneAppointment);
        manager.confirm(noneAppointment, Appointment.NONE, "none");

        List<PropDocument> acceptDocuments = query(Collections.singletonList(createActiveFacet(STATUS, "accepted", "status", "accepted")));
        assertTrue("no appointments found", 0 < acceptDocuments.size());
        assertNotNull("accepted appointment not found", findByProperty(acceptDocuments, "title", acceptedAppointment.getTitle()));
        assertNull("declined appointment found", findByProperty(acceptDocuments, "title", declinedAppointment.getTitle()));
        assertNull("tentative appointment found", findByProperty(acceptDocuments, "title", tentativeAppointment.getTitle()));
        assertNull("no status appointment found", findByProperty(acceptDocuments, "title", noneAppointment.getTitle()));

        List<PropDocument> declineDocuments = query(Collections.singletonList(createActiveFacet(STATUS, "declined", "status", "declined")));
        assertTrue("no appointments found", 0 < declineDocuments.size());
        assertNull("accepted appointment found", findByProperty(declineDocuments, "title", acceptedAppointment.getTitle()));
        assertNotNull("declined appointment not found", findByProperty(declineDocuments, "title", declinedAppointment.getTitle()));
        assertNull("tentative appointment found", findByProperty(declineDocuments, "title", tentativeAppointment.getTitle()));
        assertNull("no status appointment found", findByProperty(declineDocuments, "title", noneAppointment.getTitle()));

        List<PropDocument> tentativeDocuments = query(Collections.singletonList(createActiveFacet(STATUS, "tentative", "status", "tentative")));
        assertTrue("no appointments found", 0 < declineDocuments.size());
        assertNull("accepted appointment found", findByProperty(tentativeDocuments, "title", acceptedAppointment.getTitle()));
        assertNull("declined appointment found", findByProperty(tentativeDocuments, "title", declinedAppointment.getTitle()));
        assertNotNull("tentative appointment not found", findByProperty(tentativeDocuments, "title", tentativeAppointment.getTitle()));
        assertNull("no status appointment found", findByProperty(tentativeDocuments, "title", noneAppointment.getTitle()));

        List<PropDocument> noStatusDocuments = query(Collections.singletonList(createActiveFacet(STATUS, "none", "status", "none")));
        assertTrue("no appointments found", 0 < declineDocuments.size());
        assertNull("accepted appointment found", findByProperty(noStatusDocuments, "title", acceptedAppointment.getTitle()));
        assertNull("declined appointment found", findByProperty(noStatusDocuments, "title", declinedAppointment.getTitle()));
        assertNull("tentative appointment found", findByProperty(noStatusDocuments, "title", tentativeAppointment.getTitle()));
        assertNotNull("no status appointment not found", findByProperty(noStatusDocuments, "title", noneAppointment.getTitle()));
    }

    public void testFilterRecurringType() throws Exception {
        Appointment appointment = manager.insert(randomAppointment());
        Appointment recurringAppointment = randomAppointment();
        recurringAppointment.setRecurrenceType(Appointment.DAILY);
        recurringAppointment.setInterval(1);
        recurringAppointment = manager.insert(recurringAppointment);

        List<PropDocument> singleDocuments = query(Collections.singletonList(createActiveFacet(RECURRING_TYPE, "single", "type", "single")));
        assertTrue("no appointments found", 0 < singleDocuments.size());
        assertNotNull("single appointment not found", findByProperty(singleDocuments, "title", appointment.getTitle()));
        assertNull("recurring appointment found", findByProperty(singleDocuments, "title", recurringAppointment.getTitle()));

        List<PropDocument> recurringDocuments = query(Collections.singletonList(createActiveFacet(RECURRING_TYPE, "series", "type", "series")));
        assertTrue("no appointments found", 0 < recurringDocuments.size());
        assertNull("single appointment found", findByProperty(recurringDocuments, "title", appointment.getTitle()));
        assertNotNull("recurring appointment not found", findByProperty(recurringDocuments, "title", recurringAppointment.getTitle()));
    }

    public void testFilterParticipants() throws Exception {
        ExternalUserParticipant participant = new ExternalUserParticipant(randomUID() + "example.com");
        Appointment appointment = randomAppointment();
        appointment.addParticipant(participant);
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(Collections.singletonList(createActiveFacet(PARTICIPANT, participant.getEmailAddress(), "participants", participant.getEmailAddress())));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));
    }

    public void testFilterUsers() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        int userId = client2.getValues().getUserId();
        client2.logout();
        UserParticipant userParticipant = new UserParticipant(userId);
        Appointment appointment = randomAppointment();
        appointment.addParticipant(userParticipant);
        appointment = manager.insert(appointment);
        List<PropDocument> documents = query(Collections.singletonList(createActiveFacet(PARTICIPANT, String.valueOf(userParticipant.getIdentifier()), "users", String.valueOf(userParticipant.getIdentifier()))));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));
    }

    public void testCorrectTimeZone() throws Exception {
        /*
         * We search for an appointment and request the dates in a different
         * time zone than the one used when the appointment was created. Afterwards
         * we expect the dates in the response object to match the requested time zone.
         */
        TimeZone userTimeZone = client.getValues().getTimeZone();
        TimeZone responseTimeZone = TimeZone.getTimeZone("America/New York");
        if (responseTimeZone.getRawOffset() == userTimeZone.getRawOffset()) { // we need different time zones for creation and query response
            responseTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        }

        Calendar originStartDate = TimeTools.createCalendar(userTimeZone);
        Calendar originEndDate = (Calendar) originStartDate.clone();
        originEndDate.add(Calendar.HOUR_OF_DAY, 1);
        Calendar expectedStartDate = TimeTools.createCalendar(responseTimeZone);
        Appointment appointment = randomAppointment();
        appointment.setStartDate(originStartDate.getTime());
        appointment.setEndDate(originEndDate.getTime());
        appointment.setTitle(randomUID());
        appointment = manager.insert(appointment);

        List<PropDocument> documents = query(Collections.singletonList(createActiveFieldFacet(SUBJECT, "subject", randomSubstring(appointment.getTitle()))),Collections.singletonMap("timezone", responseTimeZone.getID()));
        assertTrue("no appointments found", 0 < documents.size());
        PropDocument document = findByProperty(documents, "title", appointment.getTitle());
        assertNotNull("appointment not found", document);

        Date d = new Date(Long.parseLong(document.getProps().get("start_date").toString()));
        Calendar responseStartDate = Calendar.getInstance(responseTimeZone);
        responseStartDate.setTime(d);
        assertEquals(expectedStartDate, responseStartDate);
    }

    public void testTokenizedQuery() throws Exception {
        Appointment appointment = randomAppointment();
        String t1 = randomUID();
        String t2 = randomUID();
        String t3 = randomUID();
        appointment.setTitle(t1 + " " + t2 + " " + t3);
        appointment = manager.insert(appointment);

        SimpleFacet globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.CALENDAR, t1 + " " + t3));
        List<PropDocument> documents = query(Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.CALENDAR, "\"" + t1 + " " + t2 + "\""));
        documents = query(Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("no appointments found", 0 < documents.size());
        assertNotNull("appointment not found", findByProperty(documents, "title", appointment.getTitle()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.CALENDAR, "\"" + t1 + " " + t3 + "\""));
        documents = query(Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("appointments found", 0 == documents.size());
    }

}
