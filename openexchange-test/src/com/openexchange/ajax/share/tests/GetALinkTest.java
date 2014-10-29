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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.DeleteRequest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.UpdateLinkRequest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;


/**
 * {@link GetALinkTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GetALinkTest extends ShareTest {

    private InfostoreTestManager itm;
    private FolderObject infostore;
    private DefaultFile file;
    private String token;
    private GuestClient guestClient;
    private String password;
    private String url;

    /**
     * Initializes a new {@link GetALinkTest}.
     * @param name
     */
    public GetALinkTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);

        UserValues values = client.getValues();
        infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());

        long now = System.currentTimeMillis();
        FolderObject parent = infostore;
        file = new DefaultFile();
        file.setFolderId(String.valueOf(parent.getObjectID()));
        file.setTitle("GetALinkTest_" + now);
        file.setDescription(file.getTitle());
        itm.newAction(file);

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(infostore.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(Collections.singletonList(target));
        getLinkRequest.setBits(createAnonymousGuestPermission().getPermissionBits());
        password = UUIDs.getUnformattedString(UUID.randomUUID());
        getLinkRequest.setPassword(password);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
        token = getLinkResponse.getToken();
        url = getLinkResponse.getUrl();
        guestClient = resolveShare(url, null, password);
    }

    @Override
    public void tearDown() throws Exception {
        if (guestClient != null) {
            ParsedShare share = discoverShare(client, infostore.getObjectID(), guestClient.getValues().getUserId());
            if (share != null) {
                DeleteRequest deleteRequest = new DeleteRequest(share, System.currentTimeMillis(), false);
                client.execute(deleteRequest);
            }
        }

        if (itm != null) {
            itm.cleanUp();
        }


        super.tearDown();
    }

    public void testCreateAndUpdateForAFolder() throws Exception {
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        guestClient.checkFolderAccessible(Integer.toString(infostore.getObjectID()), expectedPermission);
        File reloaded = (File) guestClient.getItem(infostore, file.getId(), false);
        assertNotNull(reloaded);

        OCLGuestPermission allPermission = createAnonymousGuestPermission();
        allPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        String newPassword = UUIDs.getUnformattedString(UUID.randomUUID());
        Date newExpiry = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(token, System.currentTimeMillis());
        updateLinkRequest.setBits(allPermission.getPermissionBits());
        updateLinkRequest.setExpiry(newExpiry.getTime());
        updateLinkRequest.setPassword(newPassword);
        client.execute(updateLinkRequest);

        GuestClient newClient = resolveShare(url, null, newPassword);
        newClient.checkFolderAccessible(Integer.toString(infostore.getObjectID()), allPermission);
        ParsedShare share = discoverShare(client, infostore.getObjectID(), newClient.getValues().getUserId());
        assertNotNull(share);
        assertEquals(newExpiry, share.getTarget().getExpiryDate());
    }

}
