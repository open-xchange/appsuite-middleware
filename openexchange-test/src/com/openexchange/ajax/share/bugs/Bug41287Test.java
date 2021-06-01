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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;

/**
 * {@link Bug41287Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class Bug41287Test extends ShareTest {

    private FolderObject parent;
    private FolderObject subfolder;
    private ShareTarget target;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        parent = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
        subfolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, parent.getObjectID());
        remember(subfolder);
        remember(parent);
        target = new ShareTarget(FolderObject.INFOSTORE, String.valueOf(subfolder.getObjectID()));
        GetLinkRequest req = new GetLinkRequest(target);
        getClient().execute(req);
    }

    @Test
    public void testBug41287() throws Exception {
        ShareTarget t = new ShareTarget(FolderObject.INFOSTORE, String.valueOf(parent.getObjectID()));
        GetLinkRequest req = new GetLinkRequest(t);
        GetLinkResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        String url = resp.getShareLink().getShareURL();
        GuestClient guestClient = resolveShare(url);
        OCLGuestPermission perm = createAnonymousGuestPermission();
        perm.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFolderAccessible(String.valueOf(parent.getObjectID()), perm);
    }

}
