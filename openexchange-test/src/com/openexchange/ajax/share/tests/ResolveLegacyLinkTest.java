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

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * @param name
     */
    public ResolveLegacyLinkTest(String name) {
        super(name);
    }

    public void testOpeningALegacyLinkWorks() throws Exception {
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", "Test Guest");
        int module = randomModule();
        EnumAPI api = randomFolderAPI();
        FolderObject folder = insertSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), guestPermission);

        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);

        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        URI uri = new URI(discoverShareURL(guest));
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
