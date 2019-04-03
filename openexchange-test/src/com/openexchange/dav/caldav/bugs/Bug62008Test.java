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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug62008Test} - Calendar: Appointment gets deleted w/o action by organizer / Sync with caldav iOS
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug62008Test extends CalDAVTest {

    private CalendarTestManager catm2;

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_12_0;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm2 = new CalendarTestManager(getClient2());
        catm2.setFailOnError(true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != catm2) {
                catm2.cleanUp();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testRestoreDeclinedExceptionAsOrganizer() throws Exception {
        /*
         * create appointment series on server
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("last week at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug62008Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(20);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(getClient2().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.setParentFolderID(catm.getPrivateFolder());
        catm.insert(appointment);
        Date clientLastModified = catm.getLastModification();
        /*
         * create change exception on server & decline it
         */
        catm.confirm(appointment.getParentFolderID(), appointment.getObjectID(), clientLastModified, Appointment.DECLINE, "keine zeit", 4);
        clientLastModified = catm.getLastModification();
        /*
         * verify participation status in exception as user a
         */
        Appointment exception = catm.get(appointment.getParentFolderID(), appointment.getObjectID(), 4);
        assertNotNull(exception);
        assertNotNull(exception.getUsers());
        for (UserParticipant participant : exception.getUsers()) {
            if (getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * verify participation status in exception as user b
         */
        Appointment exception2 = catm2.get(catm2.getPrivateFolder(), appointment.getObjectID(), 4);
        assertNotNull(exception2);
        assertNotNull(exception2.getUsers());
        for (UserParticipant participant : exception2.getUsers()) {
            if (getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * get & check appointment via caldav as user a
         */
        ICalResource iCalResource = get(appointment.getUid());
        assertNotNull("Event not found via CalDAV", iCalResource);
        assertEquals("Unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        Property attendeeProperty = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found", attendeeProperty);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendeeProperty.getAttribute("PARTSTAT"));
        attendeeProperty = iCalResource.getVEvents().get(1).getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in exception", attendeeProperty);
        assertEquals("PARTSTAT wrong in exception", "DECLINED", attendeeProperty.getAttribute("PARTSTAT"));
        /*
         * (try and) delete exception as user a
         */
        Component exceptionComponent = iCalResource.getVEvents().get(1);
        Property recurrenceIdProperty = exceptionComponent.getProperty("RECURRENCE-ID");
        iCalResource.getVCalendar().getComponents().remove(exceptionComponent);
        iCalResource.getVEvent().setProperty("EXDATE", recurrenceIdProperty.getValue(), recurrenceIdProperty.getAttributes());
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify participation status in exception as user a
         */
        exception = catm.get(appointment.getParentFolderID(), appointment.getObjectID(), 4);
        assertNotNull(exception);
        assertNotNull(exception.getUsers());
        for (UserParticipant participant : exception.getUsers()) {
            if (getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * verify participation status in exception as user b
         */
        exception2 = catm2.get(catm2.getPrivateFolder(), appointment.getObjectID(), 4);
        assertNotNull(exception2);
        assertNotNull(exception2.getUsers());
        for (UserParticipant participant : exception2.getUsers()) {
            if (getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * get & check appointment via caldav as user a
         */
        iCalResource = get(appointment.getUid());
        assertNotNull("Event not found via CalDAV", iCalResource);
        assertEquals("Unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        attendeeProperty = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found", attendeeProperty);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendeeProperty.getAttribute("PARTSTAT"));
        attendeeProperty = iCalResource.getVEvents().get(1).getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in exception", attendeeProperty);
        assertEquals("PARTSTAT wrong in exception", "DECLINED", attendeeProperty.getAttribute("PARTSTAT"));
    }

    @Test
    public void testRestoreDeclinedExceptionAsAttendee() throws Exception {
        /*
         * create appointment series on server as user b
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("last week at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug62008Test");
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(20);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(getClient2().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.setParentFolderID(catm2.getPrivateFolder());
        catm2.insert(appointment);
        Date clientLastModified = catm2.getLastModification();
        /*
         * create change exception on server & decline it as user a
         */
        catm.confirm(catm.getPrivateFolder(), appointment.getObjectID(), clientLastModified, Appointment.DECLINE, "keine zeit", 4);
        clientLastModified = catm.getLastModification();
        /*
         * verify participation status in exception as user b
         */
        Appointment exception = catm2.get(appointment.getParentFolderID(), appointment.getObjectID(), 4);
        assertNotNull(exception);
        assertNotNull(exception.getUsers());
        for (UserParticipant participant : exception.getUsers()) {
            if (catm.getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * verify participation status in exception as user a
         */
        Appointment exception2 = catm.get(catm.getPrivateFolder(), appointment.getObjectID(), 4);
        assertNotNull(exception2);
        assertNotNull(exception2.getUsers());
        for (UserParticipant participant : exception2.getUsers()) {
            if (catm.getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * get & check appointment via caldav as user a
         */
        ICalResource iCalResource = get(appointment.getUid());
        assertNotNull("Event not found via CalDAV", iCalResource);
        assertEquals("Unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        Property attendeeProperty = iCalResource.getVEvent().getAttendee(catm.getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found", attendeeProperty);
        assertEquals("PARTSTAT wrong", "NEEDS-ACTION", attendeeProperty.getAttribute("PARTSTAT"));
        attendeeProperty = iCalResource.getVEvents().get(1).getAttendee(catm.getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in exception", attendeeProperty);
        assertEquals("PARTSTAT wrong in exception", "DECLINED", attendeeProperty.getAttribute("PARTSTAT"));
        /*
         * (try and) delete exception as user a
         */
        Component exceptionComponent = iCalResource.getVEvents().get(1);
        Property recurrenceIdProperty = exceptionComponent.getProperty("RECURRENCE-ID");
        iCalResource.getVCalendar().getComponents().remove(exceptionComponent);
        iCalResource.getVEvent().setProperty("EXDATE", recurrenceIdProperty.getValue(), recurrenceIdProperty.getAttributes());
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify participation status in exception as user b
         */
        exception = catm2.get(appointment.getParentFolderID(), appointment.getObjectID(), 4);
        assertNotNull(exception);
        assertNotNull(exception.getUsers());
        for (UserParticipant participant : exception.getUsers()) {
            if (catm.getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * verify participation status in exception as user a
         */
        exception2 = catm.get(catm.getPrivateFolder(), appointment.getObjectID(), 4);
        assertNotNull(exception2);
        assertNotNull(exception2.getUsers());
        for (UserParticipant participant : exception2.getUsers()) {
            if (catm.getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
        /*
         * get & check appointment via caldav as user a
         */
        iCalResource = get(appointment.getUid());
        assertNotNull("Event not found via CalDAV", iCalResource);
        assertEquals("Unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        attendeeProperty = iCalResource.getVEvent().getAttendee(catm.getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found", attendeeProperty);
        assertEquals("PARTSTAT wrong", "NEEDS-ACTION", attendeeProperty.getAttribute("PARTSTAT"));
        attendeeProperty = iCalResource.getVEvents().get(1).getAttendee(catm.getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in exception", attendeeProperty);
        assertEquals("PARTSTAT wrong in exception", "DECLINED", attendeeProperty.getAttribute("PARTSTAT"));
    }

}
