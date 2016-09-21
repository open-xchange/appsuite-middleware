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

import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug44109Test}
 *
 * Some event occurrences not displayed in iOS calendar
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class Bug44109Test extends CalDAVTest {

    private CalendarTestManager manager2;

    @Before
    public void setUp() throws Exception {
        manager2 = new CalendarTestManager(new AJAXClient(User.User2));
        manager2.setFailOnError(true);
    }

    @After
    public void tearDown() throws Exception {
        if (null != this.manager2) {
            this.manager2.cleanUp();
            if (null != manager2.getClient()) {
                manager2.getClient().logout();
            }
        }
    }

    @Test
	public void testAddToSeries() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(fetchSyncToken());
		/*
		 * create appointment series on server as user b
		 */
		String uid = randomUID();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(TimeTools.D("last week at noon", TimeZone.getTimeZone("Europe/Berlin")));
	    Appointment appointment = new Appointment();
	    appointment.setUid(uid);
	    appointment.setTitle("Bug44109Test");
	    appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
	    appointment.setStartDate(calendar.getTime());
	    calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParentFolderID(manager2.getPrivateFolder());
        manager2.insert(appointment);
		Date clientLastModified = manager2.getLastModification();
        /*
         * create two change exceptions on server as user b, and invite user a there
         */
        Appointment exception1 = new Appointment();
        exception1.setTitle("Bug44109Test_exception1");
        exception1.setObjectID(appointment.getObjectID());
        exception1.setRecurrencePosition(3);
        exception1.setLastModified(clientLastModified);
        exception1.setParentFolderID(appointment.getParentFolderID());
        exception1.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        exception1.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        manager2.update(exception1);
        clientLastModified = manager2.getLastModification();
        Appointment exception2 = new Appointment();
        exception2.setTitle("Bug44109Test_exception2");
        exception2.setObjectID(appointment.getObjectID());
        exception2.setRecurrencePosition(4);
        exception2.setLastModified(clientLastModified);
        exception2.setParentFolderID(appointment.getParentFolderID());
        exception2.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        exception2.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        manager2.update(exception2);
        clientLastModified = manager2.getLastModification();
        /*
         * verify appointment exceptions on client as user a
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertEquals("unexpected number of VEVENTs", 2, iCalResource.getVEvents().size());
        Component vEventException1 = null;
        Component vEventException2 = null;
        for (Component vEventComponent : iCalResource.getVEvents()) {
            if (uid.equals(vEventComponent.getUID()) && exception1.getTitle().equals(vEventComponent.getSummary())) {
                vEventException1 = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && exception2.getTitle().equals(vEventComponent.getSummary())) {
                vEventException2 = vEventComponent;
            }
        }
        assertNotNull("No VEVENT for first occurrence in iCal found", vEventException1);
        assertNotNull("No VEVENT for second occurrence in iCal found", vEventException2);
        /*
         * add user a to the recurrence master event
         */
        appointment = manager2.get(appointment);
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        manager2.update(appointment);
        /*
         * verify recurrence on client as user a
         */
        eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertEquals("unexpected number of VEVENTs", 3, iCalResource.getVEvents().size());
        Component vEventSeries = null;
        vEventException1 = null;
        vEventException2 = null;
        for (Component vEventComponent : iCalResource.getVEvents()) {
            if (uid.equals(vEventComponent.getUID()) && appointment.getTitle().equals(vEventComponent.getSummary())) {
                vEventSeries = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && exception1.getTitle().equals(vEventComponent.getSummary())) {
                vEventException1 = vEventComponent;
            }
            if (uid.equals(vEventComponent.getUID()) && exception2.getTitle().equals(vEventComponent.getSummary())) {
                vEventException2 = vEventComponent;
            }
        }
        assertNotNull("No VEVENT for recurrence master in iCal found", vEventSeries);
        assertNotNull("No VEVENT for first occurrence in iCal found", vEventException1);
        assertNotNull("No VEVENT for second occurrence in iCal found", vEventException2);
	}

}


