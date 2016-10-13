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

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * Bug 15645
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UsmFailureDuringRecurrenceTest extends ManagedAppointmentTest {

    private Appointment app;

    public UsmFailureDuringRecurrenceTest(String name) {
        super(name);
    }


  @Override
    protected void setUp() throws Exception {
        super.setUp();
        app = generateYearlyAppointment();
        calendarManager.setTimezone(TimeZone.getTimeZone("UTC"));
    }


    //I think the message should be more like "Bullshit, you cannot make a change exception a series"
    public void testFailWhenTryingToMakeAChangeExceptionASeries() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 1);
        changes.put(Appointment.RECURRENCE_TYPE,3);
        changes.put(Appointment.DAY_IN_MONTH, 23);
        changes.put(Appointment.MONTH, 3);
        changes.put(Appointment.UNTIL,D("31/12/2025 00:00"));
        failTest(changes, "Incomplete recurring information: missing interval");
    }

  //I think it should be an exception like "Bullshit, you cannot make a change exception a series"
    public void testShouldFailWhenTryingToMakeAChangeExceptionASeriesButDoesNot() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 1);
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAY_IN_MONTH, 23);
        changes.put(Appointment.MONTH, 3);
        changes.put(Appointment.UNTIL,D("31/12/2025 00:00"));
        changes.put(Appointment.INTERVAL, 1);

        Expectations expectationsForSeries = new Expectations();
        expectationsForSeries.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);

        Expectations expectationsForException= new Expectations();
        expectationsForException.put(Appointment.RECURRENCE_POSITION, 1);
        expectationsForException.put(Appointment.RECURRENCE_TYPE, 0);

        succeedTest(changes, expectationsForSeries , expectationsForException);
    }

    public void testFailOnAChangeExceptionWithoutInterval() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 1);
        changes.put(Appointment.START_DATE,D("31/12/2025 00:00"));

        Expectations expectationsForException = new Expectations(changes);
        expectationsForException.put(Appointment.RECURRENCE_TYPE, 1);
        expectationsForException.put(Appointment.DAY_IN_MONTH, null);
        expectationsForException.put(Appointment.MONTH, null);
        expectationsForException.put(Appointment.UNTIL,null);
        expectationsForException.put(Appointment.INTERVAL, null);

        failTest(changes, "Incomplete recurring information: missing interval.");
    }

    public void testShouldAllowToCreateAChangeException() throws Exception {
        Date myDate = D("31.12.2025 00:00");
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 1);
        changes.put(Appointment.START_DATE, myDate);
        // FIXME: should fail with APP-0020 Categories=USER_INPUT Message='End date is before start date.'
        //        or, a "valid" END_DATE would be supplied here
        changes.put(Appointment.INTERVAL, 1);

        Expectations expectationsForException = new Expectations();
        expectationsForException.put(Appointment.RECURRENCE_POSITION, 1);
        //expectationsForException.put(Appointment.RECURRENCE_DATE_POSITION, app.getStartDate()); //WTF? Offset problem?
        //expectationsForException.put(Appointment.START_DATE, myDate);
        expectationsForException.put(Appointment.RECURRENCE_TYPE, 0);
        expectationsForException.put(Appointment.UNTIL,null);
        expectationsForException.put(Appointment.INTERVAL, null);
        expectationsForException.put(Appointment.MONTH, null);
        expectationsForException.put(Appointment.DAY_IN_MONTH, null);

        succeedTest(changes, null, expectationsForException);
    }

    public void testShouldFailWhenTryingToDeleteExceptionOnNormalAppointment() throws Exception {
        app = new Appointment();
        app.setParentFolderID(folder.getObjectID());
        app.setStartDate( D("31.12.2025 00:00") );
        app.setEndDate( D("31.12.2025 01:00") );

        calendarManager.insert(app);
        app.setRecurrencePosition(1);
        calendarManager.delete(app, false);
        assertTrue("Should fail", calendarManager.hasLastException());
        /* won't go further because exception is not wrapped nicely,
         * so this is just a boring JSON exception on the client side.
         */
    }


    private void succeedTest(Changes changes, Expectations expectationsForSeries, Expectations expectationsForException) throws OXException {
        calendarManager.insert(app);
        assertFalse("Creation was expected to work", calendarManager.hasLastException());

        Appointment update = new Appointment();
        update.setParentFolderID( app.getParentFolderID() );
        update.setObjectID( app.getObjectID() );
        update.setLastModified(app.getLastModified());
        changes.update(update);
        calendarManager.update(update);

        if(update.containsRecurrencePosition()) {
            assertFalse("Appointment and change exception should have different IDs", app.getObjectID() == update.getObjectID() );
        }

        assertFalse("Update was expected to work", calendarManager.hasLastException());

        if(expectationsForSeries != null){
            Appointment actualSeries = calendarManager.get(app);
            assertFalse("Getting the series was expected to work", calendarManager.hasLastException());
            expectationsForSeries.verify("[series]", actualSeries);
        }

        if(expectationsForException != null){
            Appointment actualChangeException = calendarManager.get(update);
            assertFalse("Getting the update was expected to work", calendarManager.hasLastException());
            expectationsForException.verify("[change exception]", actualChangeException);
        }
    }

    private void failTest(Changes changes, String errorCode){
        calendarManager.insert(app);

        Appointment update = new Appointment();
        update.setParentFolderID( app.getParentFolderID() );
        update.setObjectID( app.getObjectID() );
        update.setLastModified(app.getLastModified());
        changes.update(update);
        calendarManager.update(update);

        assertTrue("Was expected to fail", calendarManager.hasLastException());
        Exception exception = calendarManager.getLastException();
        assertTrue("Expected message was "+errorCode+", but got: " + exception.getMessage(), exception.getMessage().contains(errorCode));
    }

}
