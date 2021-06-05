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

package com.openexchange.ajax.appointment.recurrence;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
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
import com.openexchange.resource.Resource;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.user.User;

/**
 * These tests use the recurrence_position field to access change exceptions.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForCreatingChangeExceptions extends ManagedAppointmentTest {

    private Changes generateDefaultChangeException() {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(2));
        changes.put(Appointment.START_DATE, D("31/12/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("31/12/2008 2:00", utc));
        return changes;
    }

    public TestsForCreatingChangeExceptions() {
        super();
    }

    @Test
    public void testShouldAllowMovingAnExceptionBehindEndOfSeries() throws OXException {
        Appointment app = generateMonthlyAppointment(); // starts last year in January
        app.setOccurrence(3); // this should end last year in March

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(2)); // this should be the appointment in February
        changes.put(Appointment.START_DATE, D("1/5/2008 1:00"));
        changes.put(Appointment.END_DATE, D("1/5/2008 2:00"));

        positiveAssertionOnChangeException.check(app, changes, new Expectations(changes));
    }

    @Test
    public void testShouldAllowMovingTheFirstAppointmentTo2359TheSameDay() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(1));
        changes.put(Appointment.START_DATE, D("1/1/2008 23:00", utc));
        changes.put(Appointment.END_DATE, D("1/1/2008 23:59", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    @Test
    public void testShouldAllowMovingTheSecondAppointmentBeforeTheFirst() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    @Test
    public void testShouldAllowMovingTheSecondAppointmentTo2359TheDayBefore() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(2));
        changes.put(Appointment.START_DATE, D("1/1/2008 23:00", utc));
        changes.put(Appointment.END_DATE, D("1/1/2008 23:59", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    @Test
    public void testShouldAllowMovingTheSecondAppointmentTo2359OnTheSameDay() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(2));
        changes.put(Appointment.START_DATE, D("2/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("2/1/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    @Test
    public void testShouldAllowMovingTheSecondAppointmentToTheSamePlaceAsTheThirdOne() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(2));
        changes.put(Appointment.START_DATE, D("3/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    @Test
    public void testShouldNotMixUpWholeDayChangeExceptionAndNormalSeries() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);
        app.setFullTime(true);

        Changes changes = generateDefaultChangeException();
        changes.put(Appointment.FULL_TIME, Boolean.FALSE);

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries();
        assertTrue("Series should stay full time even if exception does not", series.getFullTime());
    }

    @Test
    public void testShouldKeepChangeInConfirmationLimitedToException() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment exception = positiveAssertionOnChangeException.getChangeException();
        Appointment series = positiveAssertionOnChangeException.getSeries();
        catm.confirm(exception, Appointment.TENTATIVE, "Changing change exception only");

        exception = catm.get(exception);
        series = catm.get(series);

        int actualConfirmationForException = exception.getUsers()[0].getConfirm();
        int actualConfirmationForSeries = series.getUsers()[0].getConfirm();
        assertEquals("Should change confirmation status for exception", Appointment.TENTATIVE, actualConfirmationForException);
        assertTrue("Should not change confirmation status for series", Appointment.TENTATIVE != actualConfirmationForSeries);
    }

    @Test
    public void testShouldKeepChangeToFulltimeLimitedToException() throws Exception {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();
        changes.put(Appointment.FULL_TIME, Boolean.FALSE);

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
        app = positiveAssertionOnChangeException.getSeries(); // update app with object ID, folder ID and lastModified

        Appointment exception = positiveAssertionOnChangeException.getChangeException();
        changes = new Changes();
        changes.put(Appointment.FULL_TIME, Boolean.FALSE);

        positiveAssertionOnUpdate.check(exception, changes, new Expectations(changes));

        Appointment actual = catm.get(app);
        assertFalse("Making an exception a whole-day-appointment should not make the series that, too", actual.getFullTime());
    }

    @Test
    public void testShouldKeepChangeInResourcesLimitedToException() throws Exception {
        this.testContext.acquireResource();
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = generateDefaultChangeException();

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries();
        Appointment exception = positiveAssertionOnChangeException.getChangeException();

        changes = new Changes();

        List<Resource> resources = resTm.search("*");
        Assert.assertFalse("Missing resources", resources.isEmpty());
        Resource res = resources.get(0);

        ResourceParticipant resParticipant = new ResourceParticipant(res);
        Participant[] participants = new ResourceParticipant[] { resParticipant };
        changes.put(Appointment.PARTICIPANTS, participants);

        positiveAssertionOnUpdate.check(exception, changes, new Expectations()); //yepp doing this only to perform update, not to compare fields at all

        exception = catm.get(exception); //update exception from server
        series = catm.get(series); //update series from server
        assertTrue("Should contain the resource in the change exception", java.util.Arrays.asList(exception.getParticipants()).contains(resParticipant));
        assertFalse("Should not contain the resource in the whole series", java.util.Arrays.asList(series.getParticipants()).contains(resParticipant));
    }

    @Test
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
        User[] resolveUser = resolver.resolveUser(testUser2.getUser() + "*");
        assertTrue("Precondition: Cannot start without having another user ready", resolveUser.length > 0);
        UserParticipant userParticipant = new UserParticipant(resolveUser[0].getId());
        Participant[] participants = new UserParticipant[] { userParticipant };
        changes.put(Appointment.PARTICIPANTS, participants);

        positiveAssertionOnUpdate.check(exception, changes, new Expectations());

        exception = catm.get(exception); //update exception from server
        series = catm.get(series); //update series from server
        assertTrue("Should contain the participant in the change exception", java.util.Arrays.asList(exception.getParticipants()).contains(userParticipant));
        assertFalse("Should not contain the participant in the whole series", java.util.Arrays.asList(series.getParticipants()).contains(userParticipant));
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testShouldFailIfTryingToCreateADeleteExceptionOnTopOfAChangeException() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Integer recurrencePos = I(2);
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, recurrencePos);
        changes.put(Appointment.START_DATE, D("3/1/2008 0:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 24:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);

        Appointment series = positiveAssertionOnChangeException.getSeries().clone();

        catm.createDeleteException(folder.getObjectID(), series.getObjectID(), recurrencePos.intValue());
        assertTrue("Should get exception when trying to get create delete exception on top of change exception", catm.hasLastException());

        OXException expected1 = new OXException(11);
        OXException expected2 = OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create();
        OXException actual = (OXException) catm.getLastException();
        assertTrue("Expecting " + expected1 + " or " + expected2 + ", but got " + actual, expected1.similarTo(actual) || expected2.similarTo(actual));
    }

    @Test
    public void testShouldFailChangeExceptionIfCreatingOneOnADeleteException() {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        catm.insert(app);

        Integer recurrencePos = I(2);
        catm.createDeleteException(app, recurrencePos.intValue());

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, recurrencePos);
        changes.put(Appointment.START_DATE, D("3/1/2008 0:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 24:00", utc));

        negativeAssertionOnChangeException.check(app, changes, OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION.create());
    }

    @Test
    public void testShouldSilentlyIgnoreNumberOfAttachmentsOnExceptionCreation() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);
        catm.insert(app);

        Appointment changeEx = catm.createIdentifyingCopy(app);
        changeEx.setNumberOfAttachments(23);
        changeEx.setRecurrencePosition(2);
        changeEx.setTitle("Bla");
        changeEx.setRecurrenceType(CalendarObject.NO_RECURRENCE);
        catm.update(changeEx);

        Appointment loaded = catm.get(changeEx);
        assertEquals(0, loaded.getNumberOfAttachments());
    }

}
