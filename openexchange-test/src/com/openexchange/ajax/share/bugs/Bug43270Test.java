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

package com.openexchange.ajax.share.bugs;

import java.rmi.Naming;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;

/**
 * {@link Bug43270Test}
 *
 * cannot get an anoymous link for calendar and addressbook
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class Bug43270Test extends ShareTest {

    private AJAXClient client2;
    private Map<Integer, FolderObject> foldersToDelete;

    /**
     * Initializes a new {@link Bug43270Test}.
     *
     * @param name The test name
     */
    public Bug43270Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        foldersToDelete = new HashMap<Integer, FolderObject>();
        client2 = new AJAXClient(User.User2);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != client2) {
                if (null != foldersToDelete && 0 < foldersToDelete.size()) {
                    deleteFoldersSilently(client2, foldersToDelete);
                }
                client2.logout();
                prepareUser(true, null, null);
            }
        } finally {
            super.tearDown();
        }
    }

    public void testGetFolderLinkWithShareLinks() throws Exception {
        getFolderLink(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    public void testGetFolderLinkWithShareLinksAndInviteGuests() throws Exception {
        getFolderLink(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testGetFolderLinkWithShareLinksAndInviteGuestsAndReadCreateSharedFolders() throws Exception {
        getFolderLink(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testDontGetFolderLink() throws Exception {
        getFolderLink(false, Boolean.FALSE, Boolean.FALSE, "SHR-0018");
    }

    public void testDontGetFolderLinkWithInviteGuests() throws Exception {
        getFolderLink(false, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    public void testDontGetFolderLinkWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        getFolderLink(true, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    public void testGetFileLinkWithShareLinks() throws Exception {
        getFileLink(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    public void testGetFileLinkWithShareLinksAndInviteGuests() throws Exception {
        getFileLink(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testGetFileLinkWithShareLinksAndInviteGuestsAndReadCreateSharedFiles() throws Exception {
        getFileLink(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testDontGetFileLink() throws Exception {
        getFileLink(false, Boolean.FALSE, Boolean.FALSE, "SHR-0018");
    }

    public void testDontGetFileLinkWithInviteGuests() throws Exception {
        getFileLink(false, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    public void testDontGetFileLinkWithInviteGuestsAndReadCreateSharedFiles() throws Exception {
        getFileLink(true, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    public void testInviteGuestToFolderWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        inviteGuestToFolder(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testInviteGuestToFolderWithInviteGuestsAndShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteGuestToFolder(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testDontInviteGuestToFolder() throws Exception {
        inviteGuestToFolder(false, Boolean.FALSE, Boolean.FALSE, "SHR-0019");
    }

    public void testInviteGuestToFolderWithInviteGuests() throws Exception {
        inviteGuestToFolder(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testDontInviteGuestToFolderWithShareLinks() throws Exception {
        inviteGuestToFolder(false, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    public void testInviteGuestToFolderWithInviteGuestsAndShareLinks() throws Exception {
        inviteGuestToFolder(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testDontInviteGuestToFolderWithShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteGuestToFolder(true, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    public void testInviteGuestToFileWithInviteGuestsAndReadCreateSharedFiles() throws Exception {
        inviteGuestToFile(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testInviteGuestToFileWithInviteGuestsAndShareLinksAndReadCreateSharedFiles() throws Exception {
        inviteGuestToFile(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testDontInviteGuestToFile() throws Exception {
        inviteGuestToFile(false, Boolean.FALSE, Boolean.FALSE, "SHR-0019");
    }

    public void testInviteGuestToFileWithInviteGuests() throws Exception {
        inviteGuestToFile(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testDontInviteGuestToFileWithShareLinks() throws Exception {
        inviteGuestToFile(false, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    public void testInviteGuestToFileWithInviteGuestsAndShareLinks() throws Exception {
        inviteGuestToFile(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testDontInviteGuestToFileWithShareLinksAndReadCreateSharedFiles() throws Exception {
        inviteGuestToFile(true, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    public void testInviteUserToFolderWithReadCreateSharedFolders() throws Exception {
        inviteUserToFolder(true, Boolean.FALSE, Boolean.FALSE, null);
    }

    public void testInviteUserToFolderWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        inviteUserToFolder(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testInviteUserToFolderWithInviteGuestsAndShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteUserToFolder(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testInviteUserToFolder() throws Exception {
        inviteUserToFolder(false, Boolean.FALSE, Boolean.FALSE, null);
    }

    public void testInviteUserToFolderWithInviteGuests() throws Exception {
        inviteUserToFolder(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testInviteUserToFolderWithShareLinks() throws Exception {
        inviteUserToFolder(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    public void testInviteUserToFolderWithInviteGuestsAndShareLinks() throws Exception {
        inviteUserToFolder(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testInviteUserToFileWithReadCreateSharedFolders() throws Exception {
        inviteUserToFile(true, Boolean.FALSE, Boolean.FALSE, null);
    }

    public void testInviteUserToFileWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        inviteUserToFile(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testInviteUserToFileWithInviteGuestsAndShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteUserToFile(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    public void testInviteUserToFile() throws Exception {
        inviteUserToFile(false, Boolean.FALSE, Boolean.FALSE, null);
    }

    public void testInviteUserToFileWithInviteGuests() throws Exception {
        inviteUserToFile(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    public void testInviteUserToFileWithShareLinks() throws Exception {
        inviteUserToFile(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    public void testInviteUserToFileWithInviteGuestsAndShareLinks() throws Exception {
        inviteUserToFile(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    private void setReadCreateSharedFolders(boolean readCreateSharedFolders) throws Exception {
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface userInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client2.getValues().getUserId());
        com.openexchange.admin.rmi.dataobjects.Context context = new com.openexchange.admin.rmi.dataobjects.Context(client2.getValues().getContextId());
        UserModuleAccess moduleAccess = userInterface.getModuleAccess(context, user, credentials);
        moduleAccess.setReadCreateSharedFolders(readCreateSharedFolders);
        userInterface.changeModuleAccess(context, user, moduleAccess, credentials);
	}

    private void setInviteGuests(Boolean inviteGuests) throws Exception {
        changeCapability("invite_guests", inviteGuests);
    }

    private void setShareLinks(Boolean shareLinks) throws Exception {
        changeCapability("share_links", shareLinks);
    }

    private void changeCapability(String capability, Boolean value) throws Exception {
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface userInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client2.getValues().getUserId());
        com.openexchange.admin.rmi.dataobjects.Context context = new com.openexchange.admin.rmi.dataobjects.Context(client2.getValues().getContextId());
        if (null == value) {
            userInterface.changeCapabilities(context, user, Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.singleton(capability), credentials);
        } else if (Boolean.TRUE.equals(value)) {
            userInterface.changeCapabilities(context, user, Collections.singleton(capability), Collections.<String>emptySet(), Collections.<String>emptySet(), credentials);
        } else {
            userInterface.changeCapabilities(context, user, Collections.<String>emptySet(), Collections.singleton(capability), Collections.<String>emptySet(), credentials);
        }
    }

    private void prepareUser(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests) throws Exception {
        setShareLinks(shareLinks);
        setInviteGuests(inviteGuests);
        setReadCreateSharedFolders(readCreateSharedFolders);
    }

    private void getFolderLink(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests, String expectedError) throws Exception {
        prepareUser(readCreateSharedFolders, shareLinks, inviteGuests);
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        GetLinkRequest getLinkRequest = new GetLinkRequest(new ShareTarget(FolderObject.INFOSTORE, String.valueOf(folder.getObjectID())));
        getLinkRequest.setFailOnError(false);
        executeAndCheck(getLinkRequest, expectedError);
    }

    private void getFileLink(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests, String expectedError) throws Exception {
        prepareUser(readCreateSharedFolders, shareLinks, inviteGuests);
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        File file = insertFile(client2, folder.getObjectID(), randomUID());
        GetLinkRequest getLinkRequest = new GetLinkRequest(new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId()));
        getLinkRequest.setFailOnError(false);
        executeAndCheck(getLinkRequest, expectedError);
    }

    private void inviteGuestToFolder(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests, String expectedError) throws Exception {
        prepareUser(readCreateSharedFolders, shareLinks, inviteGuests);
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        folder.getPermissions().add(createNamedAuthorPermission(randomUID() + "@example.com", randomUID()));
        UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_NEW, folder);
        updateRequest.setFailOnError(false);
        executeAndCheck(updateRequest, expectedError);
    }

    private void inviteUserToFolder(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests, String expectedError) throws Exception {
        prepareUser(readCreateSharedFolders, shareLinks, inviteGuests);
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        OCLPermission permission = new OCLPermission(getClient().getValues().getUserId(), folder.getObjectID());
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS);
        folder.getPermissions().add(permission);
        UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_NEW, folder);
        updateRequest.setFailOnError(false);
        executeAndCheck(updateRequest, expectedError);
    }

    private void inviteGuestToFile(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests, String expectedError) throws Exception {
        prepareUser(readCreateSharedFolders, shareLinks, inviteGuests);
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        File file = insertFile(client2, folder.getObjectID(), randomUID());
        FileStorageObjectPermission guestPermission = asObjectPermission(createNamedAuthorPermission(randomUID() + "@example.com", randomUID()));
        file.setObjectPermissions(Collections.singletonList(guestPermission));
        UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(file, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
        updateRequest.setFailOnError(false);
        executeAndCheck(updateRequest, expectedError);
    }

    private void inviteUserToFile(boolean readCreateSharedFolders, Boolean shareLinks, Boolean inviteGuests, String expectedError) throws Exception {
        prepareUser(readCreateSharedFolders, shareLinks, inviteGuests);
        FolderObject folder = insertPrivateFolder(client2, EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(client2, FolderObject.INFOSTORE));
        foldersToDelete.put(Integer.valueOf(folder.getObjectID()), folder);
        File file = insertFile(client2, folder.getObjectID(), randomUID());
        FileStorageObjectPermission permission = new DefaultFileStorageObjectPermission(getClient().getValues().getUserId(), false, FileStorageObjectPermission.WRITE);
        file.setObjectPermissions(Collections.singletonList(permission));
        UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(file, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
        updateRequest.setFailOnError(false);
        executeAndCheck(updateRequest, expectedError);
    }

    private void executeAndCheck(AJAXRequest<?> request, String expectedError) throws Exception {
        AbstractAJAXResponse response = client2.execute(request);
        if (null == expectedError) {
            /*
             * expect no errors
             */
            assertFalse(response.getErrorMessage(), response.hasError());
        } else {
            /*
             * expect to fail with error code
             */
            assertTrue("No error in response", response.hasError());
            assertEquals("Unexpected error code", expectedError, response.getException().getErrorCode());
        }
    }

}
