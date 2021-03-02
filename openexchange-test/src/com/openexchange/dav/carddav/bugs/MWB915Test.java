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
