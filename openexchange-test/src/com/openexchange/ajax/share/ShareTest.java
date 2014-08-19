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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Assert;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link ShareTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ShareTest extends AbstractAJAXSession {

    protected static final OCLGuestPermission[] TESTED_PERMISSIONS = new OCLGuestPermission[] {
        createNamedAuthorPermission("otto@example.com", "Otto Example", "secret", AuthenticationMode.DIGEST),
        createNamedGuestPermission("horst@example.com", "Horst Example", "secret", AuthenticationMode.BASIC),
        createAnonymousAuthorPermission(),
        createAnonymousGuestPermission()
    };

    protected static final EnumAPI[] TESTED_FOLDER_APIS = new EnumAPI[] { EnumAPI.OX_OLD, EnumAPI.OX_NEW, EnumAPI.OUTLOOK };

    protected static final int[] TESTED_MODULES = new int[] {
        FolderObject.CONTACT, FolderObject.INFOSTORE, FolderObject.TASK, FolderObject.CALENDAR
    };

    protected static final Random random = new Random();

    private Map<Integer, FolderObject> foldersToDelete;

    /**
     * Initializes a new {@link ShareTest}.
     *
     * @param name The test name
     */
    protected ShareTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        foldersToDelete = new HashMap<Integer, FolderObject>();
    }

    /**
     * Inserts and remembers a new shared folder containing the supplied guest permissions.
     *
     * @param api The folder tree to use
     * @param module The module identifier
     * @param parent The ID of the parent folder
     * @param guestPermission The guest permission to add
     * @return The inserted folder
     * @throws Exception
     */
    protected FolderObject insertSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        return insertSharedFolder(api, module, parent, randomUID(), guestPermission);
    }

    /**
     * Inserts and remembers a new shared folder containing the supplied guest permissions.
     *
     * @param api The folder tree to use
     * @param module The module identifier
     * @param parent The ID of the parent folder
     * @param name The folders name
     * @param guestPermission The guest permission to add
     * @return The inserted folder
     * @throws Exception
     */
    protected FolderObject insertSharedFolder(EnumAPI api, int module, int parent, String name, OCLGuestPermission guestPermission) throws Exception {
        FolderObject sharedFolder = Create.createPrivateFolder(name, module, client.getValues().getUserId(), guestPermission);
        sharedFolder.setParentFolderID(parent);
        return insertFolder(api, sharedFolder);
    }

    /**
     * Inserts and remembers a new private folder.
     *
     * @param api The folder tree to use
     * @param module The module identifier
     * @param parent The ID of the parent folder
     * @return The inserted folder
     * @throws Exception
     */
    protected FolderObject insertPrivateFolder(EnumAPI api, int module, int parent) throws Exception {
        FolderObject privateFolder = Create.createPrivateFolder(randomUID(), module, client.getValues().getUserId());
        privateFolder.setParentFolderID(parent);
        return insertFolder(api, privateFolder);
    }

    /**
     * Updates and remembers a folder.
     *
     * @param api The folder tree to use
     * @param folder The folder to udpate
     * @return The udpated folder
     * @throws Exception
     */
    protected FolderObject updateFolder(EnumAPI api, FolderObject folder) throws Exception {
        InsertResponse insertResponse = client.execute(new UpdateRequest(api, folder));
        insertResponse.fillObject(folder);
        remember(folder);
        FolderObject updatedFolder = getFolder(api, folder.getObjectID());
        assertNotNull(updatedFolder);
        assertEquals("Folder name wrong", folder.getFolderName(), updatedFolder.getFolderName());
        return updatedFolder;
    }

    /**
     * Gets a folder by ID.
     *
     * @param api The folder API to use
     * @param objectID The ID of the folder to get
     * @return The folder
     * @throws Exception
     */
    protected FolderObject getFolder(EnumAPI api, int objectID) throws Exception {
        GetResponse getResponse = client.execute(new GetRequest(api, objectID));
        FolderObject folder = getResponse.getFolder();
        folder.setLastModified(getResponse.getTimestamp());
        return getResponse.getFolder();
    }

    protected FolderObject insertFolder(EnumAPI api, FolderObject folder) throws Exception {
        InsertResponse insertResponse = client.execute(new InsertRequest(api, folder));
        insertResponse.fillObject(folder);
        remember(folder);
        FolderObject createdFolder = getFolder(api, folder.getObjectID());
        assertNotNull(createdFolder);
        assertEquals("Folder name wrong", folder.getFolderName(), createdFolder.getFolderName());
        return createdFolder;
    }

    /**
     * Remembers a folder for cleanup.
     *
     * @param folder The folder to remember
     */
    protected void remember(FolderObject folder) {
        if (null != folder) {
            foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        }
    }

    /**
     * Discovers a specific share amongst all available shares of the current user, based on the folder- and guest identifiers.
     *
     * @param folderID The folder ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The share, or <code>null</code> if not found
     */
    protected ParsedShare discoverShare(int folderID, int guest) throws OXException, IOException, JSONException {
        return discoverShare(client.execute(new AllRequest()).getParsedShares(), folderID, guest);
    }

    /**
     * Discovers a specific share amongst the supplied shares, based on the folder- and guest identifiers.
     *
     * @param shares The shares to search
     * @param folderID The folder ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The share, or <code>null</code> if not found
     */
    protected ParsedShare discoverShare(List<ParsedShare> shares, int folderID, int guest) throws OXException, IOException, JSONException {
        String folder = String.valueOf(folderID);
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
            client.execute(new DeleteRequest(
                EnumAPI.OX_NEW, false, foldersToDelete.values().toArray(new FolderObject[foldersToDelete.size()])));
        }
        super.tearDown();
    }

    /**
     * Resolves the supplied share, i.e. accesses the share link and authenticates using the share's credentials.
     *
     * @param share The share
     * @return An authenticate guest client being able to access the share
     */
    protected GuestClient resolveShare(ParsedShare share) throws Exception {
        return new GuestClient(share);
    }

    /**
     * Checks the supplied OCL permissions against the expected guest permissions.
     *
     * @param expected The expected permissions
     * @param actual The actual permissions
     */
    protected static void checkPermissions(OCLGuestPermission expected, OCLPermission actual) {
        assertEquals("Permission wrong", expected.getDeletePermission(), actual.getDeletePermission());
        assertEquals("Permission wrong", expected.getFolderPermission(), actual.getFolderPermission());
        assertEquals("Permission wrong", expected.getReadPermission(), actual.getReadPermission());
        assertEquals("Permission wrong", expected.getWritePermission(), actual.getWritePermission());
    }

    /**
     * Checks the supplied share against the expected guest permissions.
     *
     * @param expected The expected permissions
     * @param actual The actual share
     */
    protected static void checkShare(OCLGuestPermission expected, ParsedShare actual) {
        assertNotNull("No share", actual);
        assertEquals("Authentication mode wrong", expected.getAuthenticationMode(), actual.getAuthentication());
        assertEquals("Expiry date wrong", expected.getExpires(), actual.getExpires());
        if (AuthenticationMode.ANONYMOUS != expected.getAuthenticationMode()) {
            assertEquals("E-Mail address wrong", expected.getEmailAddress(), actual.getGuestMailAddress());
//TODO            assertEquals("Display name wrong", guestPermission.getDisplayName(), share.getGuestDisplayName());
            assertEquals("Password wrong", expected.getPassword(), actual.getGuestPassword());
        }
    }

    protected static OCLGuestPermission createNamedGuestPermission(String emailAddress, String displayName, String password, AuthenticationMode authenticationMode) {
        OCLGuestPermission guestPermission = createNamedPermission(emailAddress, displayName, password, authenticationMode);
        guestPermission.setAllPermission(
            OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        return guestPermission;
    }

    protected static OCLGuestPermission createNamedAuthorPermission(String emailAddress, String displayName, String password, AuthenticationMode authenticationMode) {
        OCLGuestPermission guestPermission = createNamedPermission(emailAddress, displayName, password, authenticationMode);
        guestPermission.setAllPermission(
            OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        return guestPermission;
    }

    protected static OCLGuestPermission createNamedPermission(String emailAddress, String displayName, String password, AuthenticationMode authenticationMode) {
        OCLGuestPermission guestPermission = new OCLGuestPermission();
        guestPermission.setEmailAddress(emailAddress);
        guestPermission.setDisplayName(displayName);
        guestPermission.setPassword(password);
        guestPermission.setAuthenticationMode(authenticationMode);
        guestPermission.setGroupPermission(false);
        guestPermission.setFolderAdmin(false);
        return guestPermission;
    }

    protected static OCLGuestPermission createAnonymousGuestPermission() {
        OCLGuestPermission guestPermission = createAnonymousPermission();
        guestPermission.setAllPermission(
            OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        return guestPermission;
    }

    protected static OCLGuestPermission createAnonymousAuthorPermission() {
        OCLGuestPermission guestPermission = createAnonymousPermission();
        guestPermission.setAllPermission(
            OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        return guestPermission;
    }

    protected static OCLGuestPermission createAnonymousPermission() {
        OCLGuestPermission guestPermission = new OCLGuestPermission();
        guestPermission.setAuthenticationMode(AuthenticationMode.ANONYMOUS);
        guestPermission.setGroupPermission(false);
        guestPermission.setFolderAdmin(false);
        return guestPermission;
    }

    protected int getDefaultFolder(int module) throws Exception {
        switch (module) {
        case FolderObject.CONTACT:
            return client.getValues().getPrivateContactFolder();
        case FolderObject.CALENDAR:
            return client.getValues().getPrivateAppointmentFolder();
        case FolderObject.INFOSTORE:
            return client.getValues().getPrivateInfostoreFolder();
        case FolderObject.TASK:
            return client.getValues().getPrivateTaskFolder();
        default:
            Assert.fail("No default folder for moduel: " + module);
            return 0;
        }
    }

    protected static String randomUID() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    protected static int randomModule() {
        return TESTED_MODULES[random.nextInt(TESTED_MODULES.length)];
    }

    protected static EnumAPI randomFolderAPI() {
        return TESTED_FOLDER_APIS[random.nextInt(TESTED_FOLDER_APIS.length)];
    }

    protected static OCLGuestPermission randomGuestPermission() {
        return TESTED_PERMISSIONS[random.nextInt(TESTED_PERMISSIONS.length)];
    }

}
