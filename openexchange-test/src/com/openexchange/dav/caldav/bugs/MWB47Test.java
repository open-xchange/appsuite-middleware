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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.google.common.io.BaseEncoding;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Charsets;

/**
 * {@link MWB47Test}
 *
 * Task Sync via CalDAV not working properly between Apple products and OX
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class MWB47Test extends CalDAVTest {

    @Override
    protected String encodeFolderID(String folderID) {
        return BaseEncoding.base64Url().omitPadding().encode(folderID.getBytes(Charsets.US_ASCII));
    }

    @Test
    public void testSyncCreation() throws Exception {
        String folderID = String.valueOf(getClient().getValues().getPrivateTaskFolder());
        /*
         * fetch sync token prior creation
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken(folderID));
        /*
         * create task on server
         */
        String uid = randomUID();
        String summary = "MWB47Test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Task task = generateTask(start, end, uid, summary);
        task = create(folderID, task);
        /*
         * check that the sync token has changed
         */
        assertNotEquals("Sync-Token not changed", syncToken.getToken(), fetchSyncToken(folderID));
        /*
         * check that the task is included in sync report
         */
        Map<String, String> eTags = syncCollection(syncToken, folderID).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<String> hrefs = new ArrayList<String>();
        for (String href : eTags.keySet()) {
            hrefs.add(getBaseUri() + href);
        }
        List<ICalResource> calendarData = calendarMultiget(folderID, hrefs);
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VTODO in iCal found", iCalResource.getVTodo());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVTodo().getSummary());
    }

    @Test
    public void testSyncUpdate() throws Exception {
        String folderID = String.valueOf(getClient().getValues().getPrivateTaskFolder());
        /*
         * create task on server
         */
        String uid = randomUID();
        String summary = "MWB47Test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Task task = generateTask(start, end, uid, summary);
        task = create(folderID, task);
        /*
         * fetch sync token prior update
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken(folderID));
        /*
         * update task on server
         */
        String updatedSummary = task.getTitle() + "_updated";
        task.setTitle(updatedSummary);
        task = update(task);
        /*
         * check that the sync token has changed
         */
        assertNotEquals("Sync-Token not changed", syncToken.getToken(), fetchSyncToken(folderID));
        /*
         * check that the task is included in sync report
         */
        Map<String, String> eTags = syncCollection(syncToken, folderID).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<String> hrefs = new ArrayList<String>();
        for (String href : eTags.keySet()) {
            hrefs.add(getBaseUri() + href);
        }
        List<ICalResource> calendarData = calendarMultiget(folderID, hrefs);
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VTODO in iCal found", iCalResource.getVTodo());
        assertEquals("SUMMARY wrong", updatedSummary, iCalResource.getVTodo().getSummary());
    }

    @Test
    public void testSyncDeletion() throws Exception {
        String folderID = String.valueOf(getClient().getValues().getPrivateTaskFolder());
        /*
         * create task on server
         */
        String uid = randomUID();
        String summary = "MWB47Test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Task task = generateTask(start, end, uid, summary);
        task = create(folderID, task);
        /*
         * fetch sync token prior delete
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken(folderID));
        /*
         * delete task on server
         */
        delete(task);
        /*
         * check that the sync token has changed
         */
        assertNotEquals("Sync-Token not changed", syncToken.getToken(), fetchSyncToken(folderID));
        /*
         * check that the task is included in sync report
         */
        List<String> hrefsStatusNotFound = syncCollection(syncToken, folderID).getHrefsStatusNotFound();
        assertTrue("no resource deletions reported on sync collection", 0 < hrefsStatusNotFound.size());
        boolean found = false;
        for (String href : hrefsStatusNotFound) {
            if (null != href && href.contains(uid)) {
                found = true;
                break;
            }
        }
        assertTrue("task not reported as deleted", found);
    }

}
