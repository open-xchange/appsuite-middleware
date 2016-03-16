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

package com.openexchange.subscribe.google;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link GoogleSubscribeCalendarTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.6.1
 */
public class GoogleSubscribeCalendarTest extends AbstractGoogleSubscribeTest {

    /**
     * Initializes a new {@link GoogleSubscribeCalendarTest}.
     * 
     * @param name
     */
    public GoogleSubscribeCalendarTest(String name) {
        super(name);
    }

    public void testSingleAppointment() throws OXException, IOException, JSONException {
        final String title = "Single appointment | 29 Jan 2014";
        Appointment appointment = fetchAppointment(getDateTime(29, 1, 2014, 13, 30), getDateTime(29, 1, 2014, 15, 30), title, true);
        assertNotNull("Appointment: '" + title + "' not found", appointment);
        assertFieldNotNull("user id", 1, appointment.getUid());
        assertNotNullAndEquals("location", "Olpe, Deutschland", appointment.getLocation());
        assertNotNullAndEquals("note", "Single appointment | 29 Jan 2014\n\nSome text...", appointment.getNote());
        assertNotNullAndEquals("start date", getDateTime(29, 1, 2014, 13, 30, 00, TimeZone.getTimeZone("UTC")), appointment.getStartDate());
        assertNotNullAndEquals("timezone", "America/Santiago", appointment.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(29, 1, 2014, 15, 30, 00, TimeZone.getTimeZone("UTC")), appointment.getEndDate());
        assertNotNullAndEquals("created by", client.getValues().getUserId(), appointment.getCreatedBy());
        assertNotNullAndEquals("alarm", 0, appointment.getAlarm());
        assertEquals("This appointment has no confirmation, but the mapping exist", 0, appointment.getConfirmations().length);
        assertNull("This appointment has no participants, but the mapping exist", appointment.getParticipants());
        assertNotNullAndEquals("recurrence type", CalendarObject.NO_RECURRENCE, appointment.getRecurrenceType());
        assertNotNullAndEquals("fulltime", false, appointment.getFullTime());
    }

    public void testAllDayAppointment() throws OXException, IOException, JSONException {
        final String title = "All day appointment | 30 Jan 2014";
        Appointment appointment = fetchAppointment(
            getDateTime(28, 1, 2014, 0, 0, 0, TimeZone.getTimeZone("UTC")),
            getDateTime(29, 1, 2014, 0, 0, 0, TimeZone.getTimeZone("UTC")),
            title, false);

        assertNotNull("Appointment: '" + title + "' not found", appointment);
        assertFieldNotNull("user id", 1, appointment.getUid());
        assertNotNullAndEquals("location", "Bremen, Deutschland", appointment.getLocation());
        assertNotNullAndEquals("note", "All day appointment | 30 Jan 2014", appointment.getNote());
        assertNotNullAndEquals("start date", getDateTime(28, 1, 2014, 0, 0, 0, TimeZone.getTimeZone("UTC")), appointment.getStartDate());
        assertEquals("timezone", client.getValues().getTimeZone().getID(), appointment.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(29, 1, 2014, 0, 0, 0, TimeZone.getTimeZone("UTC")), appointment.getEndDate());
        assertNotNullAndEquals("created by", client.getValues().getUserId(), appointment.getCreatedBy());
        assertNotNullAndEquals("alarm", 0, appointment.getAlarm());
        assertEquals("This appointment has no confirmation, but the mapping exist", 0, appointment.getConfirmations().length);
        assertNull("This appointment has no participants, but the mapping exist", appointment.getParticipants());
        assertNotNullAndEquals("recurrence type", CalendarObject.NO_RECURRENCE, appointment.getRecurrenceType());
        assertNotNullAndEquals("fulltime", true, appointment.getFullTime());
    }
    
    public void testDailyRecurrenceAppointment() throws OXException, IOException, JSONException {
        final String title = "Daily recurrence appointment | 27 Jan 2014 - 14 March 2014";
        Appointment appointment = fetchAppointment(getDateTime(27, 1, 2014, 15, 30), getDateTime(27, 1, 2014, 17, 30), title, true);

        assertNotNull("Appointment: '" + title + "' not found", appointment);
        assertFieldNotNull("user id", 1, appointment.getUid());
        assertNotNullAndEquals("location", "K\u00F6ln", appointment.getLocation());
        assertNotNullAndEquals("note", "Daily recurrence appointment | 27 Jan 2014 - 14 March 2014", appointment.getNote());
        assertNotNullAndEquals("start date", getDateTime(27, 1, 2014, 15, 30, 00, TimeZone.getTimeZone("UTC")), appointment.getStartDate());
        assertNotNullAndEquals("timezone", "Europe/Berlin", appointment.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(27, 1, 2014, 17, 30, 00, TimeZone.getTimeZone("UTC")), appointment.getEndDate());
        assertNotNullAndEquals("created by", client.getValues().getUserId(), appointment.getCreatedBy());
        assertNotNullAndEquals("alarm", 0, appointment.getAlarm());
        assertEquals("This appointment has no confirmation, but the mapping exist", 0, appointment.getConfirmations().length);
        assertNull("This appointment has no participants, but the mapping exist", appointment.getParticipants());
        assertNotNullAndEquals("fulltime", false, appointment.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.WEEKLY, appointment.getRecurrenceType());
        assertNotNullAndEquals("days", 62, appointment.getDays());
        assertNotNullAndEquals("interval", 1, appointment.getInterval());
        assertNotNullAndEquals("occurrence", 35, appointment.getOccurrence());
    }

    public void testMonthlyRecurrenceAppointment() throws OXException, IOException, JSONException {
        final String title = "Every third month recurrence appointment | 15 March 2014 - Never ending";
        Appointment appointment = fetchAppointment(getDateTime(15, 3, 2014, 19, 00), getDateTime(15, 3, 2014, 21, 30), title, true);

        assertNotNull("Appointment: '" + title + "' not found", appointment);
        assertFieldNotNull("user id", 1, appointment.getUid());
        assertFieldIsNull("location", appointment.getLocation());
        assertNotNullAndEquals("note", "Every third month recurrence appointment | 15 March 2014 - Never ending", appointment.getNote());
        assertNotNullAndEquals("start date", getDateTime(15, 3, 2014, 19, 00, 00, TimeZone.getTimeZone("UTC")), appointment.getStartDate());
        assertEquals("timezone", client.getValues().getTimeZone().getID(), appointment.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(15, 3, 2014, 21, 30, 00, TimeZone.getTimeZone("UTC")), appointment.getEndDate());
        assertNotNullAndEquals("created by", client.getValues().getUserId(), appointment.getCreatedBy());
        assertNotNullAndEquals("alarm", 10, appointment.getAlarm());
        assertNotNullAndEquals("fulltime", false, appointment.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.MONTHLY, appointment.getRecurrenceType());
        assertNotNullAndEquals("day in month", 15, appointment.getDayInMonth());
        assertNotNullAndEquals("interval", 3, appointment.getInterval());
        assertEquals("occurrence", 0, appointment.getOccurrence());
        assertEquals("days", 0, appointment.getDays());

        assertEquals("This appointment has no confirmation, but the mapping exist", 0, appointment.getConfirmations().length);
        assertNull("This appointment has no participants, but the mapping exist", appointment.getParticipants());
    }
    
    public void testYearlyRecurrenceAppointment() throws OXException, IOException, JSONException {
        final String title = "Yearly recurrence appointment | 14 March 2014 - 14 March 2024";
        Appointment appointment = fetchAppointment(getDateTime(14, 3, 2014, 19, 00), getDateTime(14, 3, 2014, 20, 30), title, true);

        assertNotNull("Appointment: '" + title + "' not found", appointment);
        assertFieldNotNull("user id", 1, appointment.getUid());
        assertFieldIsNull("location", appointment.getLocation());
        assertNotNullAndEquals("note", "Yearly recurrence appointment | 14 March 2014 - 14 March 2024", appointment.getNote());
        assertNotNullAndEquals("start date", getDateTime(14, 3, 2014, 19, 00, 00, TimeZone.getTimeZone("UTC")), appointment.getStartDate());
        assertEquals("timezone", client.getValues().getTimeZone().getID(), appointment.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(14, 3, 2014, 20, 30, 00, TimeZone.getTimeZone("UTC")), appointment.getEndDate());
        assertNotNullAndEquals("created by", client.getValues().getUserId(), appointment.getCreatedBy());
        assertNotNullAndEquals("alarm", 10, appointment.getAlarm());
        assertNotNullAndEquals("fulltime", false, appointment.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.YEARLY, appointment.getRecurrenceType());
        assertEquals("days", 0, appointment.getDays());
        assertNotNullAndEquals("interval", 1, appointment.getInterval());
        assertNotNullAndEquals("occurrence", 10, appointment.getOccurrence());
        assertNotNullAndEquals("day in month", 14, appointment.getDayInMonth());
        assertNotNullAndEquals("month", 2, appointment.getMonth());

        assertEquals("This appointment has no confirmation, but the mapping exist", 0, appointment.getConfirmations().length);
        assertNull("This appointment has no participants, but the mapping exist", appointment.getParticipants());
    }

    public void testAppointmentWithExceptions() throws OXException, IOException, JSONException {
        final String title = "Every two days recurrence appointment with exception | 14 March 2014 - Never ending";
        Appointment appointment = fetchAppointment(getDateTime(14, 3, 2014, 12, 00), getDateTime(14, 3, 2014, 14, 00), title, true);

        assertNotNull("Appointment: '" + title + "' not found", appointment);
        assertFieldNotNull("user id", 1, appointment.getUid());
        assertNotNullAndEquals("location", "Hannover, Deutschland", appointment.getLocation());
        assertNotNullAndEquals(
            "note",
            "Every two days recurrence appointment with exception | 14 March 2014 - Never ending\nDonnerstag 20.03. ChangeException\nMittwoch 26.03. DeleteException",
            appointment.getNote());
        assertNotNullAndEquals("start date", getDateTime(14, 3, 2014, 12, 00, 00, TimeZone.getTimeZone("UTC")), appointment.getStartDate());
        assertNotNullAndEquals("timezone", "Europe/Berlin", appointment.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(14, 3, 2014, 14, 00, 00, TimeZone.getTimeZone("UTC")), appointment.getEndDate());
        assertNotNullAndEquals("created by", client.getValues().getUserId(), appointment.getCreatedBy());
        assertNotNullAndEquals("alarm", 30, appointment.getAlarm());
        assertNotNullAndEquals("fulltime", false, appointment.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.DAILY, appointment.getRecurrenceType());
        assertEquals("days", 0, appointment.getDays());
        assertNotNullAndEquals("interval", 2, appointment.getInterval());
        assertEquals("occurrence", 0, appointment.getOccurrence());

        // List of participants
        Map<String, Part> participants = new HashMap<String, Part>();
        participants.put("ewaldbartkowiak@gmail.com", new Part("ewaldbartkowiak@gmail.com", Participant.EXTERNAL_USER, ConfirmStatus.ACCEPT));
        participants.put("dimitribronkowitsch@googlemail.com", new Part("dimitribronkowitsch@googlemail.com", Participant.EXTERNAL_USER, ConfirmStatus.NONE));
        
        int externals = 0;
        assertNotNull(appointment.getConfirmations());
        for (ConfirmableParticipant cp : appointment.getConfirmations()) {
            Part p = participants.get(cp.getEmailAddress());
            if (p != null && p.getEmailAddress().equals(cp.getEmailAddress())) {
                assertNotNull("No participant found with email address " + cp.getEmailAddress(), p);
                assertNotNullAndEquals("particiant email address", p.getEmailAddress(), cp.getEmailAddress());
                assertFieldNotNull("participant status", p.getConfirmStatus(), cp.getStatus());
                assertNotNullAndEquals("particiant status id", p.getConfirmStatus().getId(), cp.getStatus().getId());
                assertNotNullAndEquals("participant type", p.getParticipantType(), cp.getType());
                externals++;
            }
        }
        assertEquals("External participants are not equal", 2, externals);
    }
    
    private Appointment fetchAppointment(final Date startDate, final Date endDate, final String title, final boolean reccurence) {
        final int folderId = getCalendarTestFolderID();
        final Appointment[] appointments = getCalendarManager().all(folderId, startDate, endDate, Appointment.ALL_COLUMNS, reccurence);
        for (Appointment a : appointments) {
            if (a.getTitle().equals(title)) {
                return a;
            }
        }
        return null;
    }

    private class Part {

        private String emailAddress;

        private int participantType;

        private ConfirmStatus confirmStatus;

        public Part(String emailAddress, int participantType, ConfirmStatus confirmStatus) {
            super();
            this.emailAddress = emailAddress;
            this.participantType = participantType;
            this.confirmStatus = confirmStatus;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public int getParticipantType() {
            return participantType;
        }

        public ConfirmStatus getConfirmStatus() {
            return confirmStatus;
        }
    }
}
