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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.dav.reports.SyncCollectionReportMethod;
import com.openexchange.groupware.container.Contact;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderDataUsedForSync;

/**
 * {@link MWB915Test}
 *
 * CardDAV: subscribe / unsubscribe CardDAV folders has no effect on macOS address book
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB915Test extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.MACOS_11_1;
    }

    @Test
    public void testResubscribeSubfolder() throws Exception {
        /*
         * create subfolder on server
         */
        FolderData subfolder = createSubfolder(String.valueOf(getDefaultFolderID()), randomUID());
        /*
         * fetch sync token and uri to aggregated collection for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        String initialCollectionHref = buildCollectionHref(getDefaultCollectionName());
        /*
         * create contact in subfolder on server
         */
        String uid = randomUID();
        String firstName = "test";
        String lastName = "otto";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setUid(uid);
        rememberForCleanUp(create(contact, Integer.parseInt(subfolder.getId())));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getGivenName());
        assertEquals("N wrong", lastName, contactCard.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getFN());
        /*
         * de-select folder from sync
         */
        updateFolder(subfolder.getId(), new FolderData().permissions(null).usedForSync(new FolderDataUsedForSync().value("false")));
        /*
         * check that the old collection path is no longer reachable
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        SyncCollectionReportInfo reportInfo = new SyncCollectionReportInfo(syncToken.getToken(), props);
        SyncCollectionReportMethod report = null;
        try {
            report = new SyncCollectionReportMethod(getBaseUri() + initialCollectionHref, reportInfo);
            getWebDAVClient().doReport(report, StatusCodes.SC_NOT_FOUND);
        } finally {
            release(report);
        }
        /*
         * re-discover new default collection path
         */
        String intermediateCollectionHref = buildCollectionHref(getDefaultCollectionName(true));
        assertNotEquals("collection path unchanged", initialCollectionHref, intermediateCollectionHref);
        /*
         * verify removal of contained contact on client at new path
         */
        eTags = getAllETags();
        for (String href : eTags.keySet()) {
            if (null != href && href.contains(uid)) {
                fail("contact still found when listing etags");
            }
        }
        /*
         * issue an additional, explicit delete request on the old contact resource uri
         */
        DeleteMethod delete = null;
        try {
            delete = new DeleteMethod(getBaseUri() + contactCard.getHref());
            assertEquals("response code wrong", StatusCodes.SC_NOT_FOUND, getWebDAVClient().executeMethod(delete));
        } finally {
            release(delete);
        }
        /*
         * re-select the folder for sync
         */
        updateFolder(subfolder.getId(), new FolderData().permissions(null).usedForSync(new FolderDataUsedForSync().value("true")));
        /*
         * check that the intermediate collection path is no longer reachable
         */
        try {
            report = new SyncCollectionReportMethod(getBaseUri() + intermediateCollectionHref, reportInfo);
            getWebDAVClient().doReport(report, StatusCodes.SC_NOT_FOUND);
        } finally {
            release(report);
        }
        /*
         * re-discover new default collection path
         */
        String nextCollectionHref = buildCollectionHref(getDefaultCollectionName(true));
        assertNotEquals("collection path unchanged", intermediateCollectionHref, nextCollectionHref);
        /*
         * re-sync contacts under new path & verify that contact appeared again
         */
        contactCard = assertContains(uid, getAllVCards());
        assertEquals("N wrong", firstName, contactCard.getGivenName());
        assertEquals("N wrong", lastName, contactCard.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getFN());
    }

}
