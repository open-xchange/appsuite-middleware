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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.config.util.ChangePropertiesRequest;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.share.ShareTarget;

/**
 * {@link QuotaTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class QuotaTest extends Abstract2UserShareTest {

    private Map<Integer, FolderObject> foldersToDelete;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        foldersToDelete = new HashMap<Integer, FolderObject>();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("com.openexchange.quota.invite_guests", "0");
        properties.put("com.openexchange.quota.share_links", "0");
        ChangePropertiesRequest changePropertiesRequest = new ChangePropertiesRequest(properties, "user", null);
        client2.execute(changePropertiesRequest);
    }

    @Test
    public void testShareLinkButQuotaLimitReached() throws Exception {
        /*
         * try and create more links than allowed
         */
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        GetLinkRequest getLinkRequest = new GetLinkRequest(new ShareTarget(FolderObject.INFOSTORE, String.valueOf(folder.getObjectID())));
        getLinkRequest.setFailOnError(false);
        GetLinkResponse getLinkResponse = client2.execute(getLinkRequest);
        if (getLinkResponse.hasError()) {
            /*
             * one or more share links existed before, expect appropriate exception
             */
            OXException e = getLinkResponse.getException();
            assertTrue("Unexpected exception: " + e, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(e));
        } else {
            /*
             * no errors in first getLink request - a second link will exceed the quota for sure
             */
            folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
            foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
            getLinkRequest = new GetLinkRequest(new ShareTarget(FolderObject.INFOSTORE, String.valueOf(folder.getObjectID())));
            getLinkRequest.setFailOnError(false);
            getLinkResponse = client2.execute(getLinkRequest);
            assertTrue("No errors in response", getLinkResponse.hasError());
            OXException e = getLinkResponse.getException();
            assertTrue("Unexpected exception: " + e, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(e));
        }
    }
}
