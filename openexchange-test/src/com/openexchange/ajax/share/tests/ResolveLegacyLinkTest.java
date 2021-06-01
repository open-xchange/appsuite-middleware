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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;

/**
 * {@link ResolveLegacyLinkTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ResolveLegacyLinkTest extends ShareTest {

    /**
     * Initializes a new {@link ResolveLegacyLinkTest}.
     *
     * @param name
     */
    public ResolveLegacyLinkTest() {
        super();
    }

    @Test
    public void testOpeningALegacyLinkWorks() throws Exception {
        OCLGuestPermission guestPermission = createNamedGuestPermission(false);
        int module = randomModule();
        EnumAPI api = randomFolderAPI();
        FolderObject folder = insertSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), guestPermission);

        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);

        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        URI uri = new URI(discoverShareURL(guestPermission.getApiClient(), guest));
        String path = uri.getPath();
        Matcher m = Pattern.compile("(.*)/share/([a-z0-9]+/)").matcher(path);
        assertTrue(m.find());
        String servletPrefix = m.group(1);
        String baseToken = m.group(2);
        String legacyTarget = String.format("%08x", Integer.valueOf(new ShareTarget(folder.getModule(), Integer.toString(folder.getObjectID())).hashCode()));
        String legacyUrl = uri.getScheme() + "://" + uri.getHost() + servletPrefix + "/share/" + baseToken + legacyTarget;
        GuestClient guestClient = resolveShare(legacyUrl);
        ResolveShareResponse shareResolveResponse = guestClient.getShareResolveResponse();
        assertEquals(folder.getModule(), Module.getModuleInteger(shareResolveResponse.getModule()));
        assertEquals(Integer.toString(folder.getObjectID()), shareResolveResponse.getFolder());
        assertNull(shareResolveResponse.getItem());
    }

}
