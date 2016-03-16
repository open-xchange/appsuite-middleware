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
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug24682Test}
 *
 * Change excpetions in shared folders sometimes can't be loaded
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug24682Test extends CalDAVTest {

    private CalendarTestManager[] managers;

    CalendarTestManager userA, userB, userC, userD;

    @Before
    public void setUp() throws Exception {
        /*
         * setup managers for other users
         */
        managers = new CalendarTestManager[3];
        managers[0] = new CalendarTestManager(new AJAXClient(User.User2));
        managers[1] = new CalendarTestManager(new AJAXClient(User.User3));
        managers[2] = new CalendarTestManager(new AJAXClient(User.User4));
        for (CalendarTestManager manager : managers) {
            manager.setFailOnError(true);
        }
        userA = getManager();
        userB = managers[0];
        userC = managers[1];
        userD = managers[2];
        /*
         * As user C, share your calendar to users B and A
         */
        FolderTools.shareFolder(userC.getClient(), EnumAPI.OX_NEW, userC.getPrivateFolder(),
            userA.getClient().getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        FolderTools.shareFolder(userC.getClient(), EnumAPI.OX_NEW, userC.getPrivateFolder(),
            userB.getClient().getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
    }

    @After
    public void tearDown() throws Exception {
        /*
         * cleanup
         */
        if (null != managers) {
            for (CalendarTestManager manager : managers) {
                if (null != manager) {
                    manager.cleanUp();
                }
            }
        }
        /*
         * unshare user C's calendar
         */
        if (null != userC && null != userC.getClient()) {
            if (null != userA && null != userA.getClient()) {
                FolderTools.unshareFolder(userC.getClient(), EnumAPI.OX_NEW, userC.getPrivateFolder(),
                    userA.getClient().getValues().getUserId());
            }
            if (null != userB && null != userB.getClient()) {
                FolderTools.unshareFolder(userC.getClient(), EnumAPI.OX_NEW, userC.getPrivateFolder(),
                    userB.getClient().getValues().getUserId());
            }
        }
        /*
         * close managers
         */
        if (null != managers) {
            for (CalendarTestManager manager : managers) {
                if (null != manager && null != manager.getClient()) {
                    manager.getClient().logout();
                }
            }
        }
    }

    @Test
    public void testGetChangeExceptionsInSharedFolder() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken(String.valueOf(userC.getPrivateFolder())));
        /*
         * As user B, create a recurring appointment in user C's calendar, inviting users D, E and F
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("Last week at 2pm", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(userC.getPrivateFolder());
        appointment.setUid(randomUID());
        appointment.setTitle(getClass().getName());
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.addParticipant(new UserParticipant(userD.getClient().getValues().getUserId()));
        appointment.setDays(2 ^ (calendar.get(Calendar.DAY_OF_WEEK) - 1));
        appointment = userB.insert(appointment);
        /*
         * As user A, synchronize via iCal
         */
        Map<String, String> eTags = super.syncCollection(syncToken, String.valueOf(userC.getPrivateFolder())).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(String.valueOf(userC.getPrivateFolder()), eTags.keySet());
        ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", appointment.getTitle(), iCalResource.getVEvent().getSummary());
        /*
         * As user B, create a change excpetion of the appoointment series created in step 2
         */
        Appointment exception = userB.createIdentifyingCopy(appointment);
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        exception.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        exception.setEndDate(calendar.getTime());
        exception.setTitle(appointment.getTitle() + "_edit");
        exception.setRecurrencePosition(2);
        userB.update(exception);
        /*
         * As user A, synchronize via iCal
         */
        eTags = super.syncCollection(syncToken, String.valueOf(userC.getPrivateFolder())).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(String.valueOf(userC.getPrivateFolder()), eTags.keySet());
        iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Component vEventException = null;
        for (Component vEvent : iCalResource.getVEvents()) {
            Date recurrenceID = vEvent.getRecurrenceID();
            if (null != recurrenceID) {
                // exception
                assertEquals("SUMMARY wrong", exception.getTitle(), vEvent.getSummary());
                vEventException = vEvent;
            } else {
                // master
                assertEquals("SUMMARY wrong", appointment.getTitle(), vEvent.getSummary());
            }
        }
        assertNotNull("No exception found on client", vEventException);
    }

}
