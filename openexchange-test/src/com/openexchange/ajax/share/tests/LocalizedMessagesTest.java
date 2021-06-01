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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.RedeemRequest;
import com.openexchange.ajax.share.actions.RedeemResponse;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link LocalizedMessagesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class LocalizedMessagesTest extends ShareTest {

    @Test
    public void testSharedFolder() throws Exception {
        /*
         * create folder and generate a link with password for it
         */
        OCLGuestPermission guestPermission = createAnonymousGuestPermission("secret");
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        guestPermission.setEntity(matchingPermission.getEntity());
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, folder.getModule(), folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient(), guestPermission.getApiClient());
        guestClient.checkShareModuleAvailable();
        /*
         * get & check token from resolve response
         */
        ResolveShareResponse resolveResponse = guestClient.getShareResolveResponse();
        String token = resolveResponse.getToken();
        assertNotNull(token);
        /*
         * re-redeeem token with different languages
         */
        RedeemResponse redeemResponse = guestClient.execute(new RedeemRequest(token, "de_DE", true));
        assertTrue("Wrong localization. Expected 'freigegeben' but was: '" + redeemResponse.getMessage() + "'", null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("freigegeben"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "fr_FR", true));
        assertTrue("Wrong localization. Expected 'partag\u00e9' but was: '" + redeemResponse.getMessage() + "'", null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("partag\u00e9"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "en_US", true));
        assertTrue("Wrong localization. Expected 'shared' but was: '" + redeemResponse.getMessage() + "'", null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("shared"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "qx_YT", true));
        assertTrue("Wrong localization. Expected 'shared' but was: '" + redeemResponse.getMessage() + "'", null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("shared"));
    }

    @Test
    public void testUnknownShare() throws Exception {
        /*
         * create folder and generate a link for it
         */
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        guestPermission.setEntity(matchingPermission.getEntity());
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, folder.getModule(), folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to an invalid share (based on a valid share url)
         */
        String shareURL = guest.getShareURL();
        int index = shareURL.indexOf("/share/");
        String replacement = "/share/" + UUIDs.getUnformattedStringFromRandom();
        assertTrue(0 < index && shareURL.length() > replacement.length() + index);
        shareURL = shareURL.substring(0, index) + replacement + shareURL.substring(index + replacement.length());
        GuestClient guestClient = new GuestClient(shareURL, guestPermission.getRecipient());
        /*
         * get & check token from resolve response
         */
        ResolveShareResponse resolveResponse = guestClient.getShareResolveResponse();
        String token = resolveResponse.getToken();
        assertNotNull(token);
        /*
         * re-redeeem token with different languages
         */
        RedeemResponse redeemResponse = guestClient.execute(new RedeemRequest(token, "de_DE", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("nicht vorhanden"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "fr_FR", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("n'existe"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "en_US", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("not exist"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "qx_YT", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("not exist"));
    }

}
