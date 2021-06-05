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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link Bug43270Test}
 *
 * cannot get an anoymous link for calendar and addressbook
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class Bug43270Test extends Abstract2UserShareTest {

    private Map<Integer, FolderObject> foldersToDelete;

    /**
     * Initializes a new {@link Bug43270Test}.
     *
     * @param name The test name
     */
    public Bug43270Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        foldersToDelete = new HashMap<Integer, FolderObject>();
    }

    @Test
    @TryAgain
    public void testGetFolderLinkWithShareLinks() throws Exception {
        getFolderLink(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testGetFolderLinkWithShareLinksAndInviteGuests() throws Exception {
        getFolderLink(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testGetFolderLinkWithShareLinksAndInviteGuestsAndReadCreateSharedFolders() throws Exception {
        getFolderLink(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontGetFolderLink() throws Exception {
        getFolderLink(false, Boolean.FALSE, Boolean.FALSE, "SHR-0018");
    }

    @Test
    @TryAgain
    public void testDontGetFolderLinkWithInviteGuests() throws Exception {
        getFolderLink(false, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    @Test
    @TryAgain
    public void testDontGetFolderLinkWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        getFolderLink(true, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    @Test
    @TryAgain
    public void testGetFileLinkWithShareLinks() throws Exception {
        getFileLink(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testGetFileLinkWithShareLinksAndInviteGuests() throws Exception {
        getFileLink(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testGetFileLinkWithShareLinksAndInviteGuestsAndReadCreateSharedFiles() throws Exception {
        getFileLink(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontGetFileLink() throws Exception {
        getFileLink(false, Boolean.FALSE, Boolean.FALSE, "SHR-0018");
    }

    @Test
    @TryAgain
    public void testDontGetFileLinkWithInviteGuests() throws Exception {
        getFileLink(false, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    @Test
    @TryAgain
    public void testDontGetFileLinkWithInviteGuestsAndReadCreateSharedFiles() throws Exception {
        getFileLink(true, Boolean.FALSE, Boolean.TRUE, "SHR-0018");
    }

    @Test
    @TryAgain
    public void testInviteGuestToFolderWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        inviteGuestToFolder(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteGuestToFolderWithInviteGuestsAndShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteGuestToFolder(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontInviteGuestToFolder() throws Exception {
        inviteGuestToFolder(false, Boolean.FALSE, Boolean.FALSE, "SHR-0019");
    }

    @Test
    @TryAgain
    public void testInviteGuestToFolderWithInviteGuests() throws Exception {
        inviteGuestToFolder(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontInviteGuestToFolderWithShareLinks() throws Exception {
        inviteGuestToFolder(false, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    @Test
    @TryAgain
    public void testInviteGuestToFolderWithInviteGuestsAndShareLinks() throws Exception {
        inviteGuestToFolder(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontInviteGuestToFolderWithShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteGuestToFolder(true, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    @Test
    @TryAgain
    public void testInviteGuestToFileWithInviteGuestsAndReadCreateSharedFiles() throws Exception {
        inviteGuestToFile(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteGuestToFileWithInviteGuestsAndShareLinksAndReadCreateSharedFiles() throws Exception {
        inviteGuestToFile(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontInviteGuestToFile() throws Exception {
        inviteGuestToFile(false, Boolean.FALSE, Boolean.FALSE, "SHR-0019");
    }

    @Test
    @TryAgain
    public void testInviteGuestToFileWithInviteGuests() throws Exception {
        inviteGuestToFile(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontInviteGuestToFileWithShareLinks() throws Exception {
        inviteGuestToFile(false, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    @Test
    @TryAgain
    public void testInviteGuestToFileWithInviteGuestsAndShareLinks() throws Exception {
        inviteGuestToFile(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testDontInviteGuestToFileWithShareLinksAndReadCreateSharedFiles() throws Exception {
        inviteGuestToFile(true, Boolean.TRUE, Boolean.FALSE, "SHR-0019");
    }

    @Test
    @TryAgain
    public void testInviteUserToFolderWithReadCreateSharedFolders() throws Exception {
        inviteUserToFolder(true, Boolean.FALSE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFolderWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        inviteUserToFolder(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFolderWithInviteGuestsAndShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteUserToFolder(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFolder() throws Exception {
        inviteUserToFolder(false, Boolean.FALSE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFolderWithInviteGuests() throws Exception {
        inviteUserToFolder(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFolderWithShareLinks() throws Exception {
        inviteUserToFolder(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFolderWithInviteGuestsAndShareLinks() throws Exception {
        inviteUserToFolder(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFileWithReadCreateSharedFolders() throws Exception {
        inviteUserToFile(true, Boolean.FALSE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFileWithInviteGuestsAndReadCreateSharedFolders() throws Exception {
        inviteUserToFile(true, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFileWithInviteGuestsAndShareLinksAndReadCreateSharedFolders() throws Exception {
        inviteUserToFile(true, Boolean.TRUE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFile() throws Exception {
        inviteUserToFile(false, Boolean.FALSE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFileWithInviteGuests() throws Exception {
        inviteUserToFile(false, Boolean.FALSE, Boolean.TRUE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFileWithShareLinks() throws Exception {
        inviteUserToFile(false, Boolean.TRUE, Boolean.FALSE, null);
    }

    @Test
    @TryAgain
    public void testInviteUserToFileWithInviteGuestsAndShareLinks() throws Exception {
        inviteUserToFile(false, Boolean.TRUE, Boolean.TRUE, null);
    }

    private void setReadCreateSharedFolders(boolean readCreateSharedFolders) throws Exception {
        Credentials credentials = new Credentials(admin.getUser(), admin.getPassword());
        OXUserInterface userInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + OXUserInterface.RMI_NAME);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client2.getValues().getUserId());
        com.openexchange.admin.rmi.dataobjects.Context context = new com.openexchange.admin.rmi.dataobjects.Context(I(client2.getValues().getContextId()));
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
        Credentials credentials = new Credentials(admin.getUser(), admin.getPassword());
        OXUserInterface userInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + OXUserInterface.RMI_NAME);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client2.getValues().getUserId());
        com.openexchange.admin.rmi.dataobjects.Context context = new com.openexchange.admin.rmi.dataobjects.Context(I(client2.getValues().getContextId()));
        if (null == value) {
            userInterface.changeCapabilities(context, user, Collections.<String> emptySet(), Collections.<String> emptySet(), Collections.singleton(capability), credentials);
        } else if (Boolean.TRUE.equals(value)) {
            userInterface.changeCapabilities(context, user, Collections.singleton(capability), Collections.<String> emptySet(), Collections.<String> emptySet(), credentials);
        } else {
            userInterface.changeCapabilities(context, user, Collections.<String> emptySet(), Collections.singleton(capability), Collections.<String> emptySet(), credentials);
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
        folder.getPermissions().add(createNamedAuthorPermission());
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
        FileStorageObjectPermission guestPermission = asObjectPermission(createNamedAuthorPermission());
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
