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

package com.openexchange.file.storage.owncloud.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.owncloud.OwnCloudEntityResolver;
import com.openexchange.file.storage.owncloud.OwnCloudFileAccess;
import com.openexchange.file.storage.owncloud.rest.OCShares.OCShare;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.file.storage.owncloud.rest.OwnCloudRestClient;
import com.openexchange.webdav.client.WebDAVResource;

/**
 * {@link OwnCloudPermissionsUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OwnCloudPermissionsUtil {

    /**
     * Loads the permissions for the given {@link WebDAVResource} if necessary
     *
     * @param res The {@link WebDAVResource}
     * @param rest The {@link OwnCloudRestClient}
     * @return A list of Permissions
     * @throws OXException in case of errors
     */
    public static List<FileStorageObjectPermission> getPermissions(String path, WebDAVResource res, OwnCloudRestClient rest, OwnCloudEntityResolver resolver) throws OXException{
        if (containsShares(res)) {
            List<OCShare> shares = rest.getShares(path).getShares();
            List<FileStorageObjectPermission> result = new ArrayList<>(shares.size());
            for(OCShare ocShare: shares) {
                boolean isGroup = false;
                switch(ocShare.getShare_type()) {
                    case 0:
                        // single user share
                        break;
                    case 1:
                        // group share
                        isGroup = true;
                        break;
                    default:
                    case 3:
                        // public link
                        DefaultFileStorageGuestObjectPermission guestPerm = new DefaultFileStorageGuestObjectPermission();
                        guestPerm.setGroup(false);
                        int perm = OCPermissionToOXPermission(ocShare.getPermission());
                        guestPerm.setPermissions(perm);
                        guestPerm.setRecipient(new AnonymousRecipient(ocPermissionToOXPermissionBit(ocShare.getPermission()), null, null));
                        result.add(guestPerm);
                        continue;
                }

                result.add(new DefaultFileStorageObjectPermission(resolver.ocEntity2OXEntity(ocShare.getShare_with(), isGroup),
                                                                  isGroup,
                                                                  OCPermissionToOXPermission(ocShare.getPermission())));
            }
            return result;
        }

        return Collections.emptyList();
    }

    /**
     * Transforms the oc permission to an ox permission bit
     *
     * @param ocperm The oc permission
     * @return The ox permission bit
     */
    private static int ocPermissionToOXPermissionBit(int ocperm) {
        if ((ocperm & (1L << 1)) != 0) {
            return Permissions.createPermissionBits(Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.WRITE_ALL_OBJECTS, Permission.DELETE_ALL_OBJECTS, false);
        }

        if ((ocperm & (1L << 0)) != 0) {
            return Permissions.createPermissionBits(Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false);
        }

        return Permissions.createPermissionBits(Permission.READ_FOLDER, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false);
    }

    /**
     * Transforms a owncloud permission to an ox permission
     *
     * @param ocperm The owncloud permission
     * @return The ox permission
     */
    private static int OCPermissionToOXPermission(int ocperm) {
        if ((ocperm & (1L << 1)) != 0) {
            return FileStorageObjectPermission.DELETE;
        }

        if ((ocperm & (1L << 0)) != 0) {
            return FileStorageObjectPermission.READ;
        }

        return FileStorageObjectPermission.NONE;
    }

    /**
     * Checks whether the {@link WebDAVResource} has shares
     *
     * @param res The {@link WebDAVResource} to check
     * @return <code>true</code> if the {@link WebDAVResource} has shares, <code>false</code> otherwise
     */
    private static boolean containsShares(WebDAVResource res) {
        Element shares = res.getProperty(OwnCloudFileAccess.OC_SHARE_TYPES);
        return shares == null ? false : shares.getChildNodes().getLength() != 0;
    }

}
