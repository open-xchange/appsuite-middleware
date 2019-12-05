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
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
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
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("freigegeben"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "fr_FR", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("partag\u00e9"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "en_US", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("shared"));
        redeemResponse = guestClient.execute(new RedeemRequest(token, "qx_YT", true));
        assertTrue(null != redeemResponse.getMessage() && redeemResponse.getMessage().contains("shared"));
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
