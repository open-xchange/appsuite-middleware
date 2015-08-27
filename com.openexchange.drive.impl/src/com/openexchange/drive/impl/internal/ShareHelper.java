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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveShareInfo;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.CreatedShare;
import com.openexchange.share.PersonalizedShareTarget;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.Entities.PermissionType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ShareHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareHelper {

    private final SyncSession session;

    /**
     * Initializes a new {@link ShareHelper}.
     *
     * @param session The sync session
     */
    public ShareHelper(SyncSession session) {
        super();
        this.session = session;
    }

    /**
     * Adds a share to a single target for a specific recipient.
     *
     * @param target The share target to add
     * @param recipient The recipient for the share
     * @param meta Additional metadata to store along with the created share(s), or <code>null</code> if not needed
     * @return The created share
     */
    public DriveShareInfo addShare(DriveShareTarget target, ShareRecipient recipient, Map<String, Object> meta) throws OXException {
        /*
         * map drive target to plain share target & add the share
         */
        ShareTarget shareTarget = getShareTarget(target);
        CreatedShare createdShare = getShareService().addShare(session.getServerSession(), shareTarget, recipient, meta);
        /*
         * convert & return appropriate drive share
         */
        return new DefaultDriveShareInfo(createdShare.getShareInfo(), target);
    }
    /**
     * Gets all shares for a specific target.
     *
     * @param target The target to get the shares for
     * @return The shares, or an empty list if there are none
     */
    public List<DriveShareInfo> getShares(DriveShareTarget target) throws OXException {
        /*
         * map drive target to plain share target & lookup shares
         */
        ShareTarget shareTarget = getShareTarget(target);
        ServerSession serverSession = session.getServerSession();
        List<ShareInfo> shareInfos = getShareService().getShares(serverSession, getShareModule(), shareTarget.getFolder(), shareTarget.getItem());
        /*
         * convert & return appropriate drive share
         */
        List<DriveShareInfo> driveShareInfos = new ArrayList<DriveShareInfo>(shareInfos.size());
        ModuleSupport moduleSupport = DriveServiceLookup.getService(ModuleSupport.class);
        for (ShareInfo shareInfo : shareInfos) {
            PersonalizedShareTarget personalizedTarget = moduleSupport.personalizeTarget(shareTarget, serverSession.getContextId(), serverSession.getUserId());
            DriveShareTarget driveTarget = new DriveShareTarget(shareTarget, personalizedTarget.getPath(), target.getName(), target.getChecksum());
            driveShareInfos.add(new DefaultDriveShareInfo(shareInfo, driveTarget));
        }
        return driveShareInfos;
    }

    /**
     * Gets the plain share target to a drive share target, throwing appropriate exceptions in case the target can't be looked up or it's
     * checksum doesn't match.
     *
     * @param driveTarget The drive share target
     * @return The share target
     */
    public ShareTarget getShareTarget(DriveShareTarget driveTarget) throws OXException {
        if (driveTarget.isFolder()) {
            String folderID = session.getStorage().getFolderID(driveTarget.getDrivePath());
            DirectoryChecksum directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folderID)).get(0);
            if (false == driveTarget.getChecksum().equals(directoryChecksum.getChecksum())) {
                throw DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.create(driveTarget.getDrivePath(), driveTarget.getChecksum());
            }
            return new ShareTarget(DriveConstants.FILES_MODULE, folderID);
        } else {
            File file = session.getStorage().getFileByName(driveTarget.getDrivePath(), driveTarget.getName());
            if (null == file) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create(driveTarget.getName(), driveTarget.getDrivePath());
            }
            if (false == ChecksumProvider.matches(session, file, driveTarget.getChecksum())) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(driveTarget.getName(), driveTarget.getChecksum(), driveTarget.getDrivePath());
            }
            return new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
        }
    }

    /**
     * Gets the fixed share module identifier for the "files" module.
     *
     * @return The share module identifier
     */
    public String getShareModule() {
        return DriveServiceLookup.getService(ModuleSupport.class).getShareModule(DriveConstants.FILES_MODULE);
    }

    /**
     * Determines which permission entities were added during an update of a file, i.e. the permissions of those entities that are present
     * in the updated file, but were not in the original file.
     *
     * @param originalFile The original file
     * @param updatedFile The updated file
     * @return The entities collection representing the added permissions
     */
    public static Entities getAddedPermissions(File originalFile, File updatedFile) {
        Entities entities = new Entities();
        List<FileStorageObjectPermission> originalPermissions = originalFile.getObjectPermissions();
        if (null == originalPermissions || 0 == originalPermissions.size()) {
            if (null != updatedFile.getObjectPermissions()) {
                for (FileStorageObjectPermission permission : updatedFile.getObjectPermissions()) {
                    addObjectPermissionEntity(entities, permission);
                }
            }
            return entities;
        }
        List<FileStorageObjectPermission> updatedPermissions = updatedFile.getObjectPermissions();
        if (null == updatedPermissions || 0 == updatedPermissions.size()) {
            return entities;
        }
        for (FileStorageObjectPermission updatedPermission : updatedPermissions) {
            if (false == containsObjectPermissionEntity(originalPermissions, updatedPermission.getEntity())) {
                addObjectPermissionEntity(entities, updatedPermission);
            }
        }
        return entities;
    }

    /**
     * Determines which permission entities were added during an update of a file, i.e. the permissions of those entities that are present
     * in the updated file, but were not in the original file.
     *
     * @param originalFolder The original file
     * @param updatedFolder The updated file
     * @return The entities collection representing the added permissions
     */
    public static Entities getAddedPermissions(FileStorageFolder originalFolder, FileStorageFolder updatedFolder) {
        Entities entities = new Entities();
        List<FileStoragePermission> originalPermissions = originalFolder.getPermissions();
        if (null == originalPermissions || 0 == originalPermissions.size()) {
            if (null != updatedFolder.getPermissions()) {
                for (FileStoragePermission permission : updatedFolder.getPermissions()) {
                    addFolderPermissionEntity(entities, permission);
                }
            }
            return entities;
        }
        List<FileStoragePermission> updatedPermissions = updatedFolder.getPermissions();
        if (null == updatedPermissions || 0 == updatedPermissions.size()) {
            return entities;
        }
        for (FileStoragePermission updatedPermission : updatedPermissions) {
            if (false == containsFolderPermissionEntity(originalPermissions, updatedPermission.getEntity())) {
                addFolderPermissionEntity(entities, updatedPermission);
            }
        }
        return entities;
    }

    private static boolean containsObjectPermissionEntity(List<FileStorageObjectPermission> objectPermissions, int entity) {
        if (null != objectPermissions && 0 < objectPermissions.size()) {
            for (FileStorageObjectPermission objectPermission : objectPermissions) {
                if (objectPermission.getEntity() == entity) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsFolderPermissionEntity(List<FileStoragePermission> permissions, int entity) {
        if (null != permissions && 0 < permissions.size()) {
            for (FileStoragePermission permission : permissions) {
                if (permission.getEntity() == entity) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addObjectPermissionEntity(Entities entities, FileStorageObjectPermission permission) {
        if (permission.isGroup()) {
            entities.addGroup(permission.getEntity(), PermissionType.OBJECT, permission.getPermissions());
        } else {
            entities.addUser(permission.getEntity(), PermissionType.OBJECT, permission.getPermissions());
        }
    }

    private static void addFolderPermissionEntity(Entities entities, FileStoragePermission permission) {
        int bits = Permissions.createPermissionBits(permission.getFolderPermission(), permission.getReadPermission(),
            permission.getWritePermission(), permission.getDeletePermission(), permission.isAdmin());
        if (permission.isGroup()) {
            entities.addGroup(permission.getEntity(), PermissionType.FOLDER, bits);
        } else {
            entities.addUser(permission.getEntity(), PermissionType.FOLDER, bits);
        }
    }

    /**
     * Send out share notifications for permission entities added to a share target
     *
     * @param target The share target
     * @param transport The notification transport
     * @param message The notification message, or <code>null</code> if not set
     * @param entities The entities to notify
     * @return A list of warnings occurred during notification, or an empty list if there were none
     */
    public List<OXException> sendNotifications(ShareTarget target, Transport transport, String message, Entities entities) {
        if (null == entities || 0 == entities.size()) {
            return Collections.emptyList();
        }
        ShareNotificationService notificationService = DriveServiceLookup.getService(ShareNotificationService.class);
        if (null == notificationService) {
            return Collections.singletonList(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("ShareNotificationService was absent"));
        }
        return notificationService.sendShareCreatedNotifications(transport, entities, message, target, session.getServerSession(), session.getHostData());
    }

    private static ShareService getShareService() {
        return DriveServiceLookup.getService(ShareService.class);
    }

}
