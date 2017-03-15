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
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.PermissionTools;

/**
 * {@link Bug52255Test}
 *
 * "Private" appointment details readable via CalDAV "GET" request
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug52255Test extends CalDAVTest {

    private CalendarTestManager manager2;
    private FolderObject sharedFolder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * as user b, create subfolder shared to user a
         */
        manager2 = new CalendarTestManager(getClient2());
        manager2.setFailOnError(true);
        sharedFolder = new FolderObject();
        sharedFolder.setModule(FolderObject.CALENDAR);
        sharedFolder.setParentFolderID(manager2.getPrivateFolder());
        sharedFolder.setPermissions(
            PermissionTools.P(Integer.valueOf(getClient2().getValues().getUserId()),
            PermissionTools.ADMIN, Integer.valueOf(getClient().getValues().getUserId()), "vr")
        );
        sharedFolder.setFolderName(randomUID());
        com.openexchange.ajax.folder.actions.InsertRequest request = new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, sharedFolder);
        com.openexchange.ajax.folder.actions.InsertResponse response = manager2.getClient().execute(request);
        response.fillObject(sharedFolder);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != manager2) {
                if (null != sharedFolder) {
                    manager2.getClient().execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, sharedFolder));
                }
                manager2.cleanUp();
                manager2.getClient().logout();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testGetPrivateInSharedFolder() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("tomorrow at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Date startTime = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        Date endTime = calendar.getTime();
        /*
         * fetch sync token for later synchronization
         */
        String sharedFolderID = String.valueOf(sharedFolder.getObjectID());
        SyncToken syncToken = new SyncToken(fetchSyncToken(sharedFolderID));
        /*
         * create private appointment in shared folder for user b on server
         */
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Geheim");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(startTime);
        appointment.setEndDate(endTime);
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setPrivateFlag(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client as user a
         */
        ICalResource iCalResource = get(sharedFolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        String classification = iCalResource.getVEvent().getPropertyValue("CLASS");
        assertTrue("CLASS wrong", "PRIVATE".equals(classification) || "CONFIDENTIAL".equals(classification));
        assertNotEquals("SUMMARY is readable", appointment.getTitle(), iCalResource.getVEvent().getSummary());
        /*
         * verify appointment on client as user a via sync-collection report
         */
        Map<String, String> eTags = syncCollection(syncToken, sharedFolderID).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        classification = iCalResource.getVEvent().getPropertyValue("CLASS");
        assertTrue("CLASS wrong", "PRIVATE".equals(classification) || "CONFIDENTIAL".equals(classification));
        assertNotEquals("SUMMARY is readable", appointment.getTitle(), iCalResource.getVEvent().getSummary());
    }

}
