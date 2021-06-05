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
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Charsets;
import com.openexchange.test.common.groupware.calendar.TimeTools;

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
