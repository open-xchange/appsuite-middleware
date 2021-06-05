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

package com.openexchange.drive.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveShareLink;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.LinkUpdate;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.Entities.PermissionType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;

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
     * Gets or creates an "anonymous" share link with read-only permissions for a specific target.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Permissions are checked based on the the session's user being able to update the referenced share target or not, throwing an
     * appropriate exception if the permissions are not sufficient</li>
     * </ul>
     *
     * @param target The share target from the session users point of view
     * @return The share link
     */
    public DriveShareLink getLink(DriveShareTarget target) throws OXException {
        ShareTarget shareTarget = getShareTarget(target);
        ShareLink shareLink = getShareService().getLink(session.getServerSession(), shareTarget);
        if (shareLink.isNew() && target.isFolder()) {
            /*
             * invalidate storage cache & retrieve updated directory checksum for response
             */
            session.getStorage().invalidateCache();
            String folderID = session.getStorage().getFolderID(target.getDrivePath());
            DirectoryChecksum directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folderID)).get(0);
            target = new DriveShareTarget(new ShareTarget(DriveConstants.FILES_MODULE, folderID), target.getDrivePath(), directoryChecksum.getChecksum());
        }
        return new DefaultDriveShareLink(shareLink, target);
    }

    /**
     * Gets an existing "anonymous" share link with read-only permissions for a specific target if one exists.
     *
     * @param target The share target from the session users point of view
     * @return The share link, or <code>null</code> if there is none
     */
    public DriveShareLink optLink(DriveShareTarget target) throws OXException {
        ShareTarget shareTarget = getShareTarget(target);
        ShareLink shareLink = getShareService().optLink(session.getServerSession(), shareTarget);
        DriveShareTarget resolvedTarget = new DriveShareTarget(shareTarget, target.getDrivePath(), target.getName(), target.getChecksum());
        return null != shareLink ? new DefaultDriveShareLink(shareLink, resolvedTarget) : null;
    }

    /**
     * Updates the certain properties of a specific "anonymous" share link.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Permissions are checked based on the the session's user being able to update the referenced share target or not, throwing an
     * appropriate exception if the permissions are not sufficient</li>
     * </ul>
     *
     * @param target The share target from the session users point of view
     * @param linkUpdate The link update holding the updated properties
     * @return The share link
     */
    public DriveShareLink updateLink(DriveShareTarget driveTarget, LinkUpdate linkUpdate) throws OXException {
        ShareLink shareLink;
        if (driveTarget.isFolder()) {
            FileStorageFolder folder = session.getStorage().getFolder(driveTarget.getDrivePath());
            DirectoryChecksum directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folder.getId())).get(0);
            if (false == driveTarget.getChecksum().equals(directoryChecksum.getChecksum())) {
                throw DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.create(driveTarget.getDrivePath(), driveTarget.getChecksum());
            }
            ShareTarget shareTarget = new ShareTarget(DriveConstants.FILES_MODULE, folder.getId());
            shareLink = getShareService().updateLink(session.getServerSession(), shareTarget, linkUpdate, folder.getLastModifiedDate());
            /*
             * invalidate storage cache & retrieve updated directory checksum for response
             */
            session.getStorage().invalidateCache();
            directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folder.getId())).get(0);
            driveTarget = new DriveShareTarget(new ShareTarget(DriveConstants.FILES_MODULE, folder.getId()), driveTarget.getDrivePath(), directoryChecksum.getChecksum());
        } else {
            File file = session.getStorage().getFileByName(driveTarget.getDrivePath(), driveTarget.getName());
            if (null == file) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create(driveTarget.getName(), driveTarget.getDrivePath());
            }
            if (false == ChecksumProvider.matches(session, file, driveTarget.getChecksum())) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(driveTarget.getName(), driveTarget.getChecksum(), driveTarget.getDrivePath());
            }
            ShareTarget shareTarget = new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
            shareLink = getShareService().updateLink(session.getServerSession(), shareTarget, linkUpdate, new Date(file.getSequenceNumber()));
        }
        return new DefaultDriveShareLink(shareLink, driveTarget);
    }

    /**
     * Deletes an existing "anonymous" share link.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated guest permission entities from the referenced share targets are removed implicitly, so there's no need to take care
     * of those for the caller</li>
     * <li>Since the referenced share targets are updated accordingly, depending permissions checks are performed, especially
     * regarding the session's user being able to update the referenced share targets or not, throwing an appropriate exception if the
     * permissions are not sufficient</li>
     * </ul>
     *
     * @param target The share to delete from the session users point of view
     * @return The (possibly) updated share target
     */
    public DriveShareTarget deleteLink(DriveShareTarget driveTarget) throws OXException {
        if (driveTarget.isFolder()) {
            FileStorageFolder folder = session.getStorage().getFolder(driveTarget.getDrivePath());
            DirectoryChecksum directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folder.getId())).get(0);
            if (false == driveTarget.getChecksum().equals(directoryChecksum.getChecksum())) {
                throw DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.create(driveTarget.getDrivePath(), driveTarget.getChecksum());
            }
            ShareTarget shareTarget = new ShareTarget(DriveConstants.FILES_MODULE, folder.getId());
            getShareService().deleteLink(session.getServerSession(), shareTarget, folder.getLastModifiedDate());
            session.getStorage().invalidateCache();
            directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folder.getId())).get(0);
            return new DriveShareTarget(new ShareTarget(DriveConstants.FILES_MODULE, folder.getId()), driveTarget.getDrivePath(), directoryChecksum.getChecksum());
        }
        File file = session.getStorage().getFileByName(driveTarget.getDrivePath(), driveTarget.getName());
        if (null == file) {
            throw DriveExceptionCodes.FILE_NOT_FOUND.create(driveTarget.getName(), driveTarget.getDrivePath());
        }
        if (false == ChecksumProvider.matches(session, file, driveTarget.getChecksum())) {
            throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(driveTarget.getName(), driveTarget.getChecksum(), driveTarget.getDrivePath());
        }
        ShareTarget shareTarget = new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
        getShareService().deleteLink(session.getServerSession(), shareTarget, new Date(file.getSequenceNumber()));
        return driveTarget;
    }

    /**
     * Sends notifications about one or more existing shares to specific recipients, identified by their permission entity.
     *
     * @param target The share to notify about
     * @param transport The type of {@link Transport} to use when sending notifications
     * @param message The (optional) additional message for the notification. Can be <code>null</code>.
     * @param entityIDs The entity identifiers to notify
     * @return Any exceptions occurred during notification, or an empty list if all was fine
     */
    public List<OXException> notifyEntities(DriveShareTarget driveTarget, Transport transport, String message, int[] entityIDs) throws OXException {
        if (null == entityIDs || 0 == entityIDs.length) {
            return Collections.emptyList();
        }
        Entities entities;
        ShareTarget target;
        if (driveTarget.isFolder()) {
            FileStorageFolder folder = session.getStorage().getFolder(driveTarget.getDrivePath());
            DirectoryChecksum directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folder.getId())).get(0);
            if (false == driveTarget.getChecksum().equals(directoryChecksum.getChecksum())) {
                throw DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.create(driveTarget.getDrivePath(), driveTarget.getChecksum());
            }
            target = new ShareTarget(DriveConstants.FILES_MODULE, folder.getId());
            entities = filterFolderEntities(entityIDs, folder.getPermissions());
        } else {
            List<Field> fields = new ArrayList<Field>(DriveConstants.FILE_FIELDS);
            fields.add(Field.OBJECT_PERMISSIONS);
            File file = session.getStorage().getFileByName(driveTarget.getDrivePath(), driveTarget.getName(), fields, false);
            if (null == file) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create(driveTarget.getName(), driveTarget.getDrivePath());
            }
            if (false == ChecksumProvider.matches(session, file, driveTarget.getChecksum())) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(driveTarget.getName(), driveTarget.getChecksum(), driveTarget.getDrivePath());
            }
            target = new ShareTarget(DriveConstants.FILES_MODULE, file.getFolderId(), file.getId());
            entities = filterFileEntities(entityIDs, file.getObjectPermissions());
        }
        ShareNotificationService notificationService = DriveServiceLookup.getService(ShareNotificationService.class);
        if (null == notificationService) {
            return Collections.singletonList(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("ShareNotificationService was absent"));
        }
        ShareTargetPath targetPath = new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
        return notificationService.sendShareCreatedNotifications(transport, entities, message, targetPath, session.getServerSession(), session.getHostData());
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
        ShareTargetPath targetPath = new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
        return notificationService.sendShareCreatedNotifications(transport, entities, message, targetPath, session.getServerSession(), session.getHostData());
    }

    private static ShareService getShareService() {
        return DriveServiceLookup.getService(ShareService.class);
    }

    private static Entities filterFileEntities(int[] entityIDs, List<FileStorageObjectPermission> permissions) throws OXException {
        Entities entities = new Entities();
        for (int entityID : entityIDs) {
            FileStorageObjectPermission matchingPermission = null;
            if (null != permissions) {
                for (FileStorageObjectPermission permission : permissions) {
                    if (permission.getEntity() == entityID) {
                        matchingPermission = permission;
                        break;
                    }
                }
            }
            if (null == matchingPermission) {
                throw OXException.notFound(Integer.toString(entityID));
            }
            if (matchingPermission.isGroup()) {
                entities.addGroup(matchingPermission.getEntity(), PermissionType.OBJECT, matchingPermission.getPermissions());
            } else {
                entities.addUser(matchingPermission.getEntity(), PermissionType.OBJECT, matchingPermission.getPermissions());
            }
        }
        return entities;
    }

    private static Entities filterFolderEntities(int[] entityIDs, List<FileStoragePermission> permissions) throws OXException {
        Entities entities = new Entities();
        for (int entityID : entityIDs) {
            FileStoragePermission matchingPermission = null;
            if (null != permissions) {
                for (FileStoragePermission permission : permissions) {
                    if (permission.getEntity() == entityID) {
                        matchingPermission = permission;
                        break;
                    }
                }
            }
            if (null == matchingPermission) {
                throw OXException.notFound(Integer.toString(entityID));
            }
            int permissionBits = Permissions.createPermissionBits(matchingPermission.getFolderPermission(), matchingPermission.getReadPermission(),
                matchingPermission.getWritePermission(), matchingPermission.getDeletePermission(), matchingPermission.isAdmin());
            if (matchingPermission.isGroup()) {
                entities.addGroup(matchingPermission.getEntity(), PermissionType.FOLDER, permissionBits);
            } else {
                entities.addUser(matchingPermission.getEntity(), PermissionType.FOLDER, permissionBits);
            }
        }
        return entities;
    }

}
