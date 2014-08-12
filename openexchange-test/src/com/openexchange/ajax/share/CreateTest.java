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

package com.openexchange.ajax.share;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.AllResponse;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link CreateTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateTest extends AbstractAJAXSession {

    private final List<FolderObject> foldersToDelete = new ArrayList<FolderObject>();

    /**
     * Initializes a new {@link CreateTest}.
     *
     * @param name The test name
     */
    public CreateTest(String name) {
        super(name);
    }

    public void testCreateSharedContactFolder() throws Exception {
        /*
         * create folder shared to guest user
         */
        OCLGuestPermission guestPermission = new OCLGuestPermission();
        guestPermission.setEmailAddress("otto@example.com");
        guestPermission.setDisplayName("Otto Example");
        guestPermission.setPassword("secret");
        guestPermission.setAuthenticationMode(AuthenticationMode.DIGEST);
        guestPermission.setGroupPermission(false);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(
            OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        FolderObject sharedFolder = Create.createPrivateFolder(
            UUIDs.getUnformattedString(UUID.randomUUID()), FolderObject.CONTACT, client.getValues().getUserId(), guestPermission);
        sharedFolder.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        InsertResponse insertResponse = client.execute(new InsertRequest(EnumAPI.OUTLOOK, sharedFolder));
        insertResponse.fillObject(sharedFolder);
        foldersToDelete.add(sharedFolder);
        /*
         * check permissions
         */
        GetResponse getResponse = client.execute(new GetRequest(EnumAPI.OX_NEW, sharedFolder.getObjectID()));
        FolderObject createdFolder = getResponse.getFolder();
        assertNotNull(createdFolder);
        assertEquals(sharedFolder.getFolderName(), sharedFolder.getFolderName());
        assertEquals(2, createdFolder.getPermissions().size());
        int guest = -1;
        for (OCLPermission permission : createdFolder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                guest = permission.getEntity();
                break;
            }
        }
        assertTrue(guest != -1);
        /*
         * discover share
         */
        ParsedShare share = discoverShare(createdFolder.getObjectID(), guest);
        assertNotNull(share);
        assertEquals(guestPermission.getEmailAddress(), share.getGuestMailAddress());
        ResolveShareResponse resolvedShare = new GuestClient().resolve(share);
    }

    protected ParsedShare discoverShare(int folderID, int guest) throws OXException, IOException, JSONException {
        String folder = String.valueOf(folderID);
        AllResponse allResponse = client.execute(new AllRequest());
        List<ParsedShare> shares = allResponse.getParsedShares();
        for (ParsedShare share : shares) {
            if (folder.equals(share.getFolder()) && guest == share.getGuest()) {
                return share;
            }
        }
        return null;
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client && null != foldersToDelete && 0 < foldersToDelete.size()) {
            client.execute(new DeleteRequest(EnumAPI.OX_NEW, false, foldersToDelete.toArray(new FolderObject[foldersToDelete.size()])));
        }
        super.tearDown();
    }

}
