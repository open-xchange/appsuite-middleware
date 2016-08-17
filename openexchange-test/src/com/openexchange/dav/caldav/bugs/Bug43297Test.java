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
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug43297Test}
 *
 * iOS reminds to appointments of shared but deactivated CalDAV calendars
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug43297Test extends CalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_9_1;
    }

    private CalendarTestManager manager2;
    private FolderObject subfolder;
    private String sharedFolderID;

    @Before
    public void setUp() throws Exception {
        manager2 = new CalendarTestManager(new AJAXClient(User.User2));
        manager2.setFailOnError(true);
        FolderObject calendarFolder = manager2.getClient().execute(
            new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, manager2.getPrivateFolder())).getFolder();
        String subFolderName = "testfolder_" + randomUID();
        FolderObject folder = new FolderObject();
        folder.setFolderName(subFolderName);
        folder.setParentFolderID(calendarFolder.getObjectID());
        folder.setModule(calendarFolder.getModule());
        folder.setType(calendarFolder.getType());
        OCLPermission perm = new OCLPermission();
        perm.setEntity(getClient().getValues().getUserId());
        perm.setGroupPermission(false);
        perm.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        List<OCLPermission> permissions = calendarFolder.getPermissions();
        permissions.add(perm);
        folder.setPermissions(calendarFolder.getPermissions());
        InsertResponse response = manager2.getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, folder));
        folder.setObjectID(response.getId());
        folder.setLastModified(response.getTimestamp());
        subfolder = folder;
        sharedFolderID = String.valueOf(folder.getObjectID());
    }

    @After
    public void tearDown() throws Exception {
        if (null != this.manager2) {
            if (null != subfolder) {
                manager2.getClient().execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, subfolder));
            }
            this.manager2.cleanUp();
            if (null != manager2.getClient()) {
                manager2.getClient().logout();
            }
        }
    }

    @Test
    public void testDefaultAlarmInSharedFolder() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken(sharedFolderID));
        /*
         * create an appointment as user B in user B's personal calendar
         */
        String uid = randomUID();
        String summary = "test alarm";
        String location = "achtung";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setAlarm(15);
        appointment.setAlarmFlag(true);
        appointment.setParentFolderID(Integer.parseInt(sharedFolderID));
        appointment.setIgnoreConflicts(true);
        appointment = manager2.insert(appointment);
        /*
         * synchronize user B's shared calendar as user A
         */
        Map<String, String> eTags = syncCollection(syncToken, sharedFolderID).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        Component vAlarm = iCalResource.getVEvent().getVAlarm();
        assertNotNull("No VALARM found", vAlarm);
        assertEquals("Unexpected ACTION in alarm", "NONE", vAlarm.getPropertyValue("ACTION"));
        assertEquals("TRUE", vAlarm.getPropertyValue("X-APPLE-DEFAULT-ALARM"));
    }

}
