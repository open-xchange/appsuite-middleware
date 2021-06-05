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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.RedeemRequest;
import com.openexchange.ajax.share.actions.RedeemResponse;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.ajax.share.actions.ShareLink;
import com.openexchange.ajax.share.actions.UpdateLinkRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.ShareTarget;

/**
 * {@link LoginScreenTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class LoginScreenTest extends ShareTest {

    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), getClient().getValues().getPrivateInfostoreFolder());
    }

    @Test
    public void testLinkWithPassword() throws Exception {
        /*
         * Create link and set password
         */
        ShareTarget target = new ShareTarget(folder.getModule(), Integer.toString(folder.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target);
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        ShareLink shareLink = getLinkResponse.getShareLink();
        assertTrue(shareLink.isNew());
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(target, getLinkResponse.getTimestamp().getTime());
        String newPW = UUIDs.getUnformattedStringFromRandom();
        updateLinkRequest.setPassword(newPW);
        getClient().execute(updateLinkRequest);
        /*
         * Login and check params
         */
        GuestClient guestClient = resolveShare(shareLink.getShareURL(), null, newPW);
        guestClient.checkSessionAlive(false);
        ResolveShareResponse resolveResponse = guestClient.getShareResolveResponse();
        String token = resolveResponse.getToken();
        assertNotNull(token);
        RedeemRequest req = new RedeemRequest(token);
        RedeemResponse resp = guestClient.execute(req);
        assertEquals("anonymous_password", resp.getLoginType());
        assertEquals("INFO", resp.getMessageType());
        assertNotNull(resp.getMessage());
    }

}
