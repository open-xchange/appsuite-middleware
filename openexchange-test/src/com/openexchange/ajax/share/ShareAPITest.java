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

package com.openexchange.ajax.share;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FileShare;
import com.openexchange.ajax.share.actions.FileSharesRequest;
import com.openexchange.ajax.share.actions.FolderShare;
import com.openexchange.ajax.share.actions.FolderSharesRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * 
 * This class contains methods from {@link ShareTest}.
 * The super class is {@link AbstractAPIClientSession} to use generated API for inheriting tests.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v7.10.5
 */
public abstract class ShareAPITest extends AbstractAPIClientSession {

    /**
     * Inserts and remembers a new private folder.
     *
     * @param api The folder tree to use
     * @param module The module identifier
     * @param parent The ID of the parent folder
     * @param name The folder's name
     * @return The inserted folder
     * @throws Exception
     */
    private FolderObject insertPrivateFolder(EnumAPI api, int module, int parent, String name) throws Exception {
        FolderObject createdFolder = insertPrivateFolder(getClient(), api, module, parent, name);
        assertNotNull(createdFolder);
        assertEquals("Folder name wrong", name, createdFolder.getFolderName());
        return createdFolder;
    }

    private static FolderObject insertPrivateFolder(AJAXClient client, EnumAPI api, int module, int parent, String name) throws Exception {
        FolderObject privateFolder = Create.createPrivateFolder(name, module, client.getValues().getUserId());
        privateFolder.setParentFolderID(parent);
        return insertFolder(client, api, privateFolder);
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
        return insertPrivateFolder(api, module, parent, randomUID());
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
        return getFolder(api, objectID, getClient());
    }
    
    /**
     * Gets a folder by ID with the given getClient().
     *
     * @param api The folder API to use
     * @param objectID The ID of the folder to get
     * @return The folder
     * @throws Exception
     */
    protected static FolderObject getFolder(EnumAPI api, int objectID, AJAXClient client) throws Exception {
        GetResponse getResponse = client.execute(new GetRequest(api, objectID));
        FolderObject folder = getResponse.getFolder();
        folder.setLastModified(getResponse.getTimestamp());
        return folder;
    }

    private static FolderObject insertFolder(AJAXClient client, EnumAPI api, FolderObject folder) throws Exception {
        InsertRequest insertRequest = new InsertRequest(api, folder, client.getValues().getTimeZone());
        insertRequest.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse = client.execute(insertRequest);
        insertResponse.fillObject(folder);
        return getFolder(api, folder.getObjectID(), client);
    }

    /**
     * Gets all folder shares of a specific module.
     *
     * @param client The ajax client to use
     * @param api The folder tree to use
     * @param module The module identifier
     * @return The folder shares
     */
    private static List<FolderShare> getFolderShares(AJAXClient client, EnumAPI api, int module) throws Exception {
        return client.execute(new FolderSharesRequest(api, Module.getModuleString(module, -1))).getShares(client.getValues().getTimeZone());
    }

    /**
     * Gets all folder shares of a specific module.
     *
     * @param client The ajax client to use
     * @param api The folder tree to use
     * @param module The module identifier
     * @return The folder shares
     */
    private static List<FileShare> getFileShares(AJAXClient client) throws Exception {
        return client.execute(new FileSharesRequest()).getShares(client.getValues().getTimeZone());
    }

    /**
     * Discovers a specific guest permission entity amongst all available shares of the current user, based on the folder- and guest identifiers.
     *
     * @param api The folder tree to use
     * @param module The module identifier
     * @param folderID The folder ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The guest permission entity, or <code>null</code> if not found
     */
    protected ExtendedPermissionEntity discoverGuestEntity(EnumAPI api, int module, int folderID, int guest) throws Exception {
        return discoverGuestEntity(getClient(), api, module, folderID, guest);
    }

    /**
     * Discovers a specific guest permission entity amongst all available shares of the current user, based on the folder- and guest identifiers.
     *
     * @param client The ajax client to use
     * @param folderID The folder ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The share, or <code>null</code> if not found
     */
    private static ExtendedPermissionEntity discoverGuestEntity(AJAXClient client, EnumAPI api, int module, int folderID, int guest) throws Exception {
        List<FolderShare> shares = getFolderShares(client, api, module);
        for (FolderShare share : shares) {
            if (share.getObjectID() == folderID) {
                return discoverGuestEntity(share.getExtendedPermissions(), guest);
            }
        }
        return null;
    }

    /**
     * Discovers a specific guest permission entity amongst all available shares of the current user, based on the file- and guest identifiers.
     *
     * @param item The item ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The guest permission entity, or <code>null</code> if not found
     */
    protected ExtendedPermissionEntity discoverGuestEntity(String item, int guest) throws Exception {
        return discoverGuestEntity(getClient(), item, guest);
    }

    /**
     * Discovers a specific guest permission entity amongst all available shares of the current user, based on the file- and guest identifiers.
     *
     * @param client The ajax client to use
     * @param item The item ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The share, or <code>null</code> if not found
     */
    private static ExtendedPermissionEntity discoverGuestEntity(AJAXClient client, String item, int guest) throws Exception {
        List<FileShare> shares = getFileShares(client);
        for (FileShare share : shares) {
            if (share.getId().equals(item)) {
                return discoverGuestEntity(share.getExtendedPermissions(), guest);
            }
        }
        return null;
    }

    /**
     * Discovers a specific share amongst all available shares of the current user, based on the folder- and guest identifiers.
     *
     * @param client The ajax client to use
     * @param folderID The folder ID to discover the share for
     * @param guest The ID of the guest associated to the share
     * @return The share, or <code>null</code> if not found
     */
    private static ExtendedPermissionEntity discoverGuestEntity(List<ExtendedPermissionEntity> entities, int guest) {
        if (null != entities) {
            for (ExtendedPermissionEntity entity : entities) {
                if (entity.getEntity() == guest) {
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * Resolves the supplied share url, i.e. accesses the share link and authenticates using the supplied credentials.
     *
     * @param url The share URL
     * @param username The username, or <code>null</code> if not needed
     * @param password The password, or <code>null</code> if not needed
     * @return An authenticated guest client being able to access the share
     */
    protected GuestClient resolveShare(String url, String username, String password) throws Exception {
        return new GuestClient(url, username, password);
    }

    private static OCLGuestPermission createAnonymousGuestPermission(String password) {
        OCLGuestPermission guestPermission = createAnonymousPermission(password);
        guestPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        guestPermission.getRecipient().setBits(guestPermission.getPermissionBits());
        return guestPermission;
    }

    protected static OCLGuestPermission createAnonymousGuestPermission() {
        return createAnonymousGuestPermission(null);
    }

    private static OCLGuestPermission createAnonymousPermission(String password) {
        AnonymousRecipient recipient = new AnonymousRecipient();
        recipient.setPassword(password);
        OCLGuestPermission guestPermission = new OCLGuestPermission(recipient);
        AnonymousRecipient anonymousRecipient = new AnonymousRecipient();
        anonymousRecipient.setPassword(password);
        guestPermission.setRecipient(anonymousRecipient);
        guestPermission.setGroupPermission(false);
        guestPermission.setFolderAdmin(false);
        guestPermission.getRecipient().setBits(guestPermission.getPermissionBits());
        return guestPermission;
    }

    private static String randomUID() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    /**
     * Gets the username to use for login from the supplied share recipient.
     *
     * @param recipient The recipient
     * @return The username
     */
    public static String getUsername(ShareRecipient recipient) {
        switch (recipient.getType()) {
            case ANONYMOUS:
                return recipient.getType().toString().toLowerCase();
            case GUEST:
                return ((GuestRecipient) recipient).getEmailAddress();
            default:
                Assert.fail("Unknown recipient: " + recipient.getType());
                return null;
        }
    }

    /**
     * Gets the password to use for login from the supplied share recipient.
     *
     * @param recipient The recipient
     * @return The password, or <code>null</code> if not needed
     */
    public static String getPassword(ShareRecipient recipient) {
        switch (recipient.getType()) {
            case ANONYMOUS:
                return ((AnonymousRecipient) recipient).getPassword();
            case GUEST:
                return ((GuestRecipient) recipient).getPassword();
            default:
                Assert.fail("Unknown recipient: " + recipient.getType());
                return null;
        }
    }

}
