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

package com.openexchange.google.subscribe;

import static com.openexchange.google.subscribe.utility.AssertField.assertFieldIsNull;
import static com.openexchange.google.subscribe.utility.AssertField.assertFieldNotNull;
import static com.openexchange.google.subscribe.utility.AssertField.assertNotNullAndEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import com.openexchange.exception.OXException;
import com.openexchange.google.subscribe.mocks.MockAppointmentSqlFactoryService;
import com.openexchange.google.subscribe.mocks.MockServiceLookup;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.user.SimUserService;
import com.openexchange.user.UserService;

/**
 * {@link GoogleCalendarTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
@PrepareForTest({
    com.openexchange.subscribe.google.osgi.Services.class, AppointmentSqlFactoryService.class,
    com.openexchange.api2.AppointmentSQLInterface.class })
public class GoogleCalendarTest extends AbstractGoogleTest {

    public void testCalendar() throws Exception {
        try {
            subscribeCalendar();

            List<CalendarDataObject> calendarObjects = new LinkedList<CalendarDataObject>();

            // appointments for testing
            final String singleAppointment = "Single appointment | 29 Jan 2014";
            boolean successSingleAppointment = false;

            final String allDayAppointment = "All day appointment | 30 Jan 2014";
            boolean successAllDayAppointment = false;

            final String recurrenceDailyAppointment = "Daily recurrence appointment | 27 Jan 2014 - 14 March 2014";
            boolean successRecurrenceDailyAppointment = false;

            final String recurrenceMonthlyAppointment = "Every third month recurrence appointment | 15 March 2014 - Never ending";
            boolean successMonthlyRecurrenceAppointment = false;

            final String recurrenceYearlyAppointment = "Yearly recurrence appointment | 14 March 2014 - 14 March 2016";
            boolean successYearlyRecurrenceAppointment = false;

            final String recurrenceAppointmentWithExceptions = "Every two days recurrence appointment with exception | 14 March 2014 - Never ending";
            boolean successRecurrenceAppointmentWithExceptions = false;

            for (CalendarDataObject co : calendarObjects) {
                if (co.getTitle() != null) {
                    if (co.getTitle().equals(singleAppointment)) {
                        testSingleAppointment(co);
                        successSingleAppointment = true;
                    } else if (co.getTitle().equals(allDayAppointment)) {
                        testAllDayAppointment(co);
                        successAllDayAppointment = true;
                    } else if (co.getTitle().equals(recurrenceDailyAppointment)) {
                        testDailyRecurrenceAppointment(co);
                        successRecurrenceDailyAppointment = true;
                    } else if (co.getTitle().equals(recurrenceMonthlyAppointment)) {
                        testMonthlyRecurrenceAppointment(co);
                        successMonthlyRecurrenceAppointment = true;
                    } else if (co.getTitle().equals(recurrenceYearlyAppointment)) {
                        testYearlyRecurrenceAppointment(co);
                        successYearlyRecurrenceAppointment = true;
                    } else if (co.getTitle().equals(recurrenceAppointmentWithExceptions)) {
                        testAppointmentWithExceptions(co);
                        successRecurrenceAppointmentWithExceptions = true;
                    }
                }

                // if all appointments were succeeded break out
                if (successSingleAppointment && successAllDayAppointment && successRecurrenceDailyAppointment && successMonthlyRecurrenceAppointment && successYearlyRecurrenceAppointment && successRecurrenceAppointmentWithExceptions) {
                    return;
                }
            }

            assertTrue("Appointment: " + singleAppointment + " not found", successSingleAppointment);
            assertTrue("Appointment: " + successAllDayAppointment + " not found", successAllDayAppointment);
            assertTrue("Appointment: " + recurrenceDailyAppointment + " not found", successRecurrenceDailyAppointment);
            assertTrue("Appointment: " + recurrenceMonthlyAppointment + " not found", successMonthlyRecurrenceAppointment);
            assertTrue("Appointment: " + recurrenceYearlyAppointment + " not found", successYearlyRecurrenceAppointment);
        } catch (OXException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    private void testSingleAppointment(CalendarDataObject co) {
        assertNotNullAndEquals("context", 1, co.getContext().getContextId());
        assertFieldNotNull("user id", 1, co.getUid());
        assertNotNullAndEquals("location", "Olpe, Deutschland", co.getLocation());
        assertNotNullAndEquals("note", "Single appointment | 29 Jan 2014\n\nSome text...", co.getNote());
        assertNotNullAndEquals("start date", getDateTime(29, 1, 2014, 13, 30), co.getStartDate());
        assertNotNullAndEquals("timezone", "America/Santiago", co.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(29, 1, 2014, 15, 30), co.getEndDate());
        assertNotNullAndEquals("creation date", getDateTime(8, 8, 2014, 15, 32, 33), co.getCreationDate());
        assertNotNullAndEquals("created by", 1, co.getCreatedBy());
        assertNotNullAndEquals("alarm", 45, co.getAlarm());
        assertNull("This appointment has no confirmation, but the mapping exist", co.getConfirmations());
        assertNull("This appointment has no participants, but the mapping exist", co.getParticipants());
        assertNotNullAndEquals("recurrence type", CalendarObject.NO_RECURRENCE, co.getRecurrenceType());
        assertNotNullAndEquals("fulltime", false, co.getFullTime());
    }

    private void testAllDayAppointment(CalendarDataObject co) {
        assertNotNullAndEquals("context", 1, co.getContext().getContextId());
        assertFieldNotNull("user id", 1, co.getUid());
        assertNotNullAndEquals("location", "Bremen, Deutschland", co.getLocation());
        assertNotNullAndEquals("note", "All day appointment | 30 Jan 2014", co.getNote());
        assertNotNullAndEquals("start date", getDateTime(28, 1, 2014, 0, 0, 0, TimeZone.getTimeZone("UTC")), co.getStartDate());
        assertFieldIsNull("timezone", co.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(29, 1, 2014, 0, 0, 0, TimeZone.getTimeZone("UTC")), co.getEndDate());
        assertNotNullAndEquals("creation date", getDateTime(8, 8, 2014, 15, 34, 07), co.getCreationDate());
        assertNotNullAndEquals("created by", 1, co.getCreatedBy());
        assertNotNullAndEquals("alarm", 0, co.getAlarm());
        assertNull("This appointment has no confirmation, but the mapping exist", co.getConfirmations());
        assertNull("This appointment has no participants, but the mapping exist", co.getParticipants());
        assertNotNullAndEquals("recurrence type", CalendarObject.NO_RECURRENCE, co.getRecurrenceType());
        assertNotNullAndEquals("fulltime", true, co.getFullTime());
    }

    private void testDailyRecurrenceAppointment(CalendarDataObject co) {
        assertNotNullAndEquals("context", 1, co.getContext().getContextId());
        assertFieldNotNull("user id", 1, co.getUid());
        assertNotNullAndEquals("location", "Köln", co.getLocation());
        assertNotNullAndEquals("note", "Daily recurrence appointment | 27 Jan 2014 - 14 March 2014", co.getNote());
        assertNotNullAndEquals("start date", getDateTime(27, 1, 2014, 15, 30), co.getStartDate());
        assertNotNullAndEquals("timezone", "Europe/Berlin", co.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(27, 1, 2014, 17, 30), co.getEndDate());
        assertNotNullAndEquals("creation date", getDateTime(8, 8, 2014, 15, 38, 29), co.getCreationDate());
        assertNotNullAndEquals("created by", 1, co.getCreatedBy());
        assertNotNullAndEquals("alarm", 30, co.getAlarm());
        assertNull("This appointment has no confirmation, but the mapping exist", co.getConfirmations());
        assertNull("This appointment has no participants, but the mapping exist", co.getParticipants());
        assertNotNullAndEquals("fulltime", false, co.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.WEEKLY, co.getRecurrenceType());
        assertNotNullAndEquals("days", 62, co.getDays());
        assertNotNullAndEquals("interval", 1, co.getInterval());
        assertNotNullAndEquals("occurrence", 35, co.getOccurrence());
    }

    private void testMonthlyRecurrenceAppointment(CalendarDataObject co) {
        assertNotNullAndEquals("context", 1, co.getContext().getContextId());
        assertFieldNotNull("user id", 1, co.getUid());
        assertFieldIsNull("location", co.getLocation());
        assertNotNullAndEquals("note", "Every third month recurrence appointment | 15 March 2014 - Never ending", co.getNote());
        assertNotNullAndEquals("start date", getDateTime(15, 3, 2014, 19, 00), co.getStartDate());
        assertFieldIsNull("timezone", co.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(15, 3, 2014, 21, 30), co.getEndDate());
        assertNotNullAndEquals("creation date", getDateTime(8, 8, 2014, 14, 11, 43), co.getCreationDate());
        assertNotNullAndEquals("created by", 1, co.getCreatedBy());
        assertNotNullAndEquals("alarm", 10, co.getAlarm());
        assertNotNullAndEquals("fulltime", false, co.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.MONTHLY, co.getRecurrenceType());
        assertNotNullAndEquals("day in month", 15, co.getDayInMonth());
        assertNotNullAndEquals("interval", 3, co.getInterval());
        assertFieldIsNull("occurrence", co.getOccurrence());
        assertFieldIsNull("days", co.getDays());

        assertNull("This appointment has no confirmation, but the mapping exist", co.getConfirmations());
        assertNull("This appointment has no participants, but the mapping exist", co.getParticipants());
    }

    private void testYearlyRecurrenceAppointment(CalendarDataObject co) {
        assertNotNullAndEquals("context", 1, co.getContext().getContextId());
        assertFieldNotNull("user id", 1, co.getUid());
        assertFieldIsNull("location", co.getLocation());
        assertNotNullAndEquals("note", "Yearly recurrence appointment | 14 March 2014 - 14 March 2016", co.getNote());
        assertNotNullAndEquals("start date", getDateTime(14, 3, 2014, 19, 00), co.getStartDate());
        assertFieldIsNull("timezone", co.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(14, 3, 2014, 20, 30), co.getEndDate());
        assertNotNullAndEquals("creation date", getDateTime(8, 8, 2014, 14, 59, 06), co.getCreationDate());
        assertNotNullAndEquals("created by", 1, co.getCreatedBy());
        assertNotNullAndEquals("alarm", 0, co.getAlarm());
        assertNotNullAndEquals("fulltime", false, co.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.YEARLY, co.getRecurrenceType());
        assertFieldIsNull("days", co.getDays());
        assertNotNullAndEquals("interval", 1, co.getInterval());
        assertNotNullAndEquals("occurrence", 2, co.getOccurrence());
        assertNotNullAndEquals("day in month", 14, co.getDayInMonth());
        assertNotNullAndEquals("month", 3, co.getMonth());

        assertNull("This appointment has no confirmation, but the mapping exist", co.getConfirmations());
        assertNull("This appointment has no participants, but the mapping exist", co.getParticipants());
    }

    private void testAppointmentWithExceptions(CalendarDataObject co) {
        assertNotNullAndEquals("context", 1, co.getContext().getContextId());
        assertFieldNotNull("user id", 1, co.getUid());
        assertNotNullAndEquals("location", "Hannover, Deutschland", co.getLocation());
        assertNotNullAndEquals(
            "note",
            "Every two days recurrence appointment with exception | 14 March 2014 - Never ending\nDonnerstag 20.03. ChangeException\nMittwoch 26.03. DeleteException",
            co.getNote());
        assertNotNullAndEquals("start date", getDateTime(14, 3, 2014, 12, 00), co.getStartDate());
        assertNotNullAndEquals("timezone", "Europe/Berlin", co.getTimezone());
        assertNotNullAndEquals("end date", getDateTime(14, 3, 2014, 14, 00), co.getEndDate());
        assertNotNullAndEquals("creation date", getDateTime(8, 8, 2014, 15, 38, 29), co.getCreationDate());
        assertNotNullAndEquals("created by", 1, co.getCreatedBy());
        assertFieldIsNull("alarm", co.getAlarm());
        assertNotNullAndEquals("fulltime", false, co.getFullTime());

        assertNotNullAndEquals("recurrence type", CalendarObject.DAILY, co.getRecurrenceType());
        assertFieldIsNull("days", co.getDays());
        assertNotNullAndEquals("interval", 2, co.getInterval());
        assertFieldIsNull("occurrence", co.getOccurrence());

        // List of participants
        List<Part> participants = new LinkedList<Part>();
        participants.add(new Part("Ewald Bartkowiak", "ewald.bartkowiak@googlemail.com", Participant.EXTERNAL_USER, ConfirmStatus.ACCEPT));
        participants.add(new Part("Dimitri Bronkowitsch", "dimitri.bronkowitsch@googlemail.com", Participant.USER, ConfirmStatus.NONE));
        participants.add(new Part("jan.finsel@premium", "jan.finsel@premium", Participant.USER, ConfirmStatus.NONE));

        assertNotNull(co.getConfirmations());
        for (ConfirmableParticipant cp : co.getConfirmations()) {
            int countParts = 0;
            for (Part p : participants) {
                if (cp.getEmailAddress().equals(p.getEmailAddress())) {
                    assertNotNullAndEquals("particiant display name", p.getDisplayName(), cp.getDisplayName());
                    assertFieldNotNull("participant status", p.getConfirmStatus(), cp.getStatus());
                    assertNotNullAndEquals("particiant status id", p.getConfirmStatus().getId(), cp.getStatus().getId());
                    assertNotNullAndEquals("participant type", p.getParticipantType(), cp.getType());
                    ++countParts;
                }
            }
            assertTrue("Should have found three participants but only got: " + countParts, countParts == 3);
        }
    }

    /**
     * Gets date / time with the default time zone Europe / Berlin.
     */
    private Date getDateTime(int day, int month, int year, int hour, int minute) {
        return getDateTime(day, month, year, hour, minute, 0, TimeZone.getTimeZone("Europe/Berlin"));
    }

    /**
     * Gets date / time with precisions seconds with the default time zone Europe / Berlin.
     */
    private Date getDateTime(int day, int month, int year, int hour, int minute, int seconds) {
        return getDateTime(day, month, year, hour, minute, seconds, TimeZone.getTimeZone("Europe/Berlin"));
    }

    private Date getDateTime(int day, int month, int year, int hour, int minute, int seconds, TimeZone timezone) {
        Calendar cal = Calendar.getInstance(timezone);
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    protected void prepareAdditionalMocks(MockServiceLookup sl) throws Exception {
        SimUserService simUser = new SimUserService();
        PowerMockito.mockStatic(com.openexchange.subscribe.google.osgi.Services.class);
        PowerMockito.doReturn(simUser).when(
            com.openexchange.subscribe.google.osgi.Services.class,
            "getService",
            Matchers.any(UserService.class));
        AppointmentSqlFactoryService asfs = new MockAppointmentSqlFactoryService();
        sl.setAppointmentSQLServiceMock(asfs);
    }

    private class Part {

        private String displayName;

        private String emailAddress;

        private int participantType;

        private ConfirmStatus confirmStatus;

        public Part(String displayName, String emailAddress, int participantType, ConfirmStatus confirmStatus) {
            super();
            this.displayName = displayName;
            this.emailAddress = emailAddress;
            this.participantType = participantType;
            this.confirmStatus = confirmStatus;
        }

        public String getDisplayName() {
            return displayName;
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
