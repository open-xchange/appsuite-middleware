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

package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.ajax.user.UserResolver;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;

/**
 * These tests use the recurrence_position field to access change exceptions.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForCreatingChangeExceptions extends ManagedAppointmentTest {

    private Changes generateDefaultChangeException() {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("31/12/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("31/12/2008 2:00", utc));
        return changes;
    }

    public TestsForCreatingChangeExceptions(String name) {
        super(name);
    }

    public void testShouldAllowMovingAnExceptionBehindEndOfSeries() throws OXException {
        Appointment app = generateMonthlyAppointment(); // starts last year in January
        app.setOccurrence(3); // this should end last year in March

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2); // this should be the appointment in February
        changes.put(Appointment.START_DATE, D("1/5/2008 1:00"));
        changes.put(Appointment.END_DATE, D("1/5/2008 2:00"));

        positiveAssertionOnChangeException.check(app, changes, new Expectations(changes));
    }

    public void testShouldAllowMovingTheFirstAppointmentTo2359TheSameDay() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 1);
        changes.put(Appointment.START_DATE, D("1/1/2008 23:00", utc));
        changes.put(Appointment.END_DATE, D("1/1/2008 23:59", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentBeforeTheFirst() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentTo2359TheDayBefore() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("1/1/2008 23:00", utc));
        changes.put(Appointment.END_DATE, D("1/1/2008 23:59", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentTo2359OnTheSameDay() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("2/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("2/1/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentToTheSamePlaceAsTheThirdOne() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("3/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldNotMixUpWholeDayChangeExceptionAndNormalSeries() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);
        app.setFullTime(true);

        Changes changes = generateDefaultChangeException();
        changes.put(Appointment.FULL_TIME, false);

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries();
        assertTrue("Series should stay full time even if exception does not", series.getFullTime());
    }

    public void testShouldKeepChangeInConfirmationLimitedToException() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment exception = positiveAssertionOnChangeException.getChangeException();
        Appointment series = positiveAssertionOnChangeException.getSeries();
        calendarManager.confirm(exception, Appointment.TENTATIVE, "Changing change exception only");

        exception = calendarManager.get(exception);
        series = calendarManager.get(series);

        int actualConfirmationForException = exception.getUsers()[0].getConfirm();
        int actualConfirmationForSeries = series.getUsers()[0].getConfirm();
        assertEquals("Should change confirmation status for exception", Appointment.TENTATIVE, actualConfirmationForException);
        assertTrue("Should not change confirmation status for series", Appointment.TENTATIVE != actualConfirmationForSeries);
    }

    public void testShouldKeepChangeToFulltimeLimitedToException() throws Exception {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();
        changes.put(Appointment.FULL_TIME, false);

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
        app = positiveAssertionOnChangeException.getSeries(); // update app with object ID, folder ID and lastModified

        Appointment exception = positiveAssertionOnChangeException.getChangeException();
        changes = new Changes();
        changes.put(Appointment.FULL_TIME, true);

        positiveAssertionOnUpdate.check(exception, changes, new Expectations(changes));

        Appointment actual = calendarManager.get(app);
        assertFalse("Making an exception a whole-day-appointment should not make the series that, too", actual.getFullTime());
    }

    public void testShouldKeepChangeInResourcesLimitedToException() throws Exception {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries();
        Appointment exception = positiveAssertionOnChangeException.getChangeException();

        changes = new Changes();

        Resource res = resourceManager.search("*").get(0);

        ResourceParticipant resParticipant = new ResourceParticipant(res);
        Participant[] participants = new ResourceParticipant[]{resParticipant };
        changes.put(Appointment.PARTICIPANTS, participants );

        positiveAssertionOnUpdate.check(exception, changes, new Expectations()); //yepp doing this only to perform update, not to compare fields at all

        exception = calendarManager.get(exception); //update exception from server
        series = calendarManager.get(series); //update series from server
        assertTrue("Should contain the resource in the change exception" , java.util.Arrays.asList(exception.getParticipants()).contains(resParticipant));
        assertFalse("Should not contain the resource in the whole series" , java.util.Arrays.asList(series.getParticipants()).contains(resParticipant));
    }

    @SuppressWarnings("deprecation")
    public void testShouldKeepChangeInParticipantsLimitedToException() throws Exception {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries();
        Appointment exception = positiveAssertionOnChangeException.getChangeException();

        changes = new Changes();

        UserResolver resolver = new UserResolver(getClient());
        User[] resolveUser = resolver.resolveUser(getAJAXProperty("seconduser") + "*");
        assertTrue("Precondition: Cannot start without having another user ready", resolveUser.length > 0);
        UserParticipant userParticipant = new UserParticipant(resolveUser[0].getId());
        Participant[] participants = new UserParticipant[]{userParticipant };
        changes.put(Appointment.PARTICIPANTS, participants );

        positiveAssertionOnUpdate.check(exception, changes, new Expectations());

        exception = calendarManager.get(exception); //update exception from server
        series = calendarManager.get(series); //update series from server
        assertTrue("Should contain the participant in the change exception" , java.util.Arrays.asList(exception.getParticipants()).contains(userParticipant));
        assertFalse("Should not contain the participant in the whole series" , java.util.Arrays.asList(series.getParticipants()).contains(userParticipant));
     }

    public void testShouldFailIfTryingToCreateADeleteExceptionOnTopOfAChangeException() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        int recurrencePos = 2;
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, recurrencePos);
        changes.put(Appointment.START_DATE, D("3/1/2008 0:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 24:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries().clone();

        calendarManager.createDeleteException(folder.getObjectID(), series.getObjectID(), recurrencePos);
        assertTrue(
            "Should get exception when trying to get create delete exception on top of change exception",
            calendarManager.hasLastException());

        OXException expected1 = new OXException(11);
        OXException expected2 = OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create();
        OXException actual = (OXException) calendarManager.getLastException();
        assertTrue("Expecting " + expected1 + " or " + expected2 + ", but got " + actual, expected1.similarTo(actual) || expected2.similarTo(actual));
    }

    public void testShouldFailChangeExceptionIfCreatingOneOnADeleteException() {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        calendarManager.insert(app);

        int recurrencePos = 2;
        calendarManager.createDeleteException(app, recurrencePos);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, recurrencePos);
        changes.put(Appointment.START_DATE, D("3/1/2008 0:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 24:00", utc));

        negativeAssertionOnChangeException.check(app, changes, OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION.create());
    }

    public void testShouldSilentlyIgnoreNumberOfAttachmentsOnExceptionCreation() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        calendarManager.insert(app);

        Appointment changeEx = calendarManager.createIdentifyingCopy(app);
        changeEx.setNumberOfAttachments(23);
        changeEx.setRecurrencePosition(2);
        changeEx.setTitle("Bla");
        changeEx.setRecurrenceType(CalendarObject.NO_RECURRENCE);
        calendarManager.update(changeEx);


        Appointment loaded = calendarManager.get(changeEx);

        assertEquals(0, loaded.getNumberOfAttachments());
    }

}
