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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.dav.reports.SyncCollectionReportMethod;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug23078Test}
 *
 * Contacts don't get removed on OSX when removing their contacts folder
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug23078Test extends CardDAVTest {

    public Bug23078Test() {
        super();
    }

    @Test
    public void testReportItemsFromDeletedFolder() throws Exception {
        /*
         * create subfolder on server
         */
        String folderName = "testfolder_" + randomUID();
        FolderObject subFolder = super.createFolder(folderName);
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "doktor";
        String lastName = "horst";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setUid(uid);
        super.rememberForCleanUp(super.create(contact, subFolder.getObjectID()));
        /*
         * verify contact and folder on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getGivenName());
        assertEquals("N wrong", lastName, contactCard.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getFN());
        /*
         * delete folder (and contents) on server
         */
        super.deleteFolder(subFolder);
        /*
         * verify deletion on client (assuming that the aggregated collection path has changed)
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        SyncCollectionReportInfo reportInfo = new SyncCollectionReportInfo(syncToken.getToken(), props);
        SyncCollectionReportMethod report = null;
        try {
            report = new SyncCollectionReportMethod(getBaseUri() + buildCollectionHref(getDefaultCollectionName(true)), reportInfo);
            getWebDAVClient().doReport(report, StatusCodes.SC_FORBIDDEN);
        } finally {
            release(report);
        }
        /*
         * verify deletion on client
         */
        eTags = getAllETags();
        for (String href : eTags.keySet()) {
            if (null != href && href.contains(uid)) {
                fail("contact still found when listing etags");
            }
        }
    }

}
