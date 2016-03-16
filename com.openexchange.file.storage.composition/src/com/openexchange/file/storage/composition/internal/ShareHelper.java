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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.file.storage.composition.internal.FileStorageTools.supports;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.Autoboxing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tx.ConnectionHolder;

/**
 * {@link ShareHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareHelper {

    /**
     * Pre-processes the supplied document to extract added, modified or removed guest object permissions required for sharing support. Guest object
     * permissions that are considered as "new", i.e. guest object permissions from the document metadata that are not yet resolved to a
     * guest user entity, are removed implicitly from the document in order to re-add them afterwards (usually by calling
     * {@link ShareHelper#applyGuestPermissions}). Additionally some validity checks are performed to fail fast in case of invalid requests.
     *
     * @param session The session
     * @param fileAccess The file access hosting the document
     * @param document The document being saved
     * @param modifiedColumns The modified fields as supplied by the client, or <code>null</code> if not set
     * @return The compared object permissions yielding new and removed guest object permissions
     */
    public static ComparedObjectPermissions processGuestPermissions(Session session, FileStorageFileAccess fileAccess, File document, List<Field> modifiedColumns) throws OXException {
        if ((null == modifiedColumns || modifiedColumns.contains(Field.OBJECT_PERMISSIONS))) {
            ComparedObjectPermissions comparedPermissions;
            if (FileStorageFileAccess.NEW == document.getId()) {
                comparedPermissions = new ComparedObjectPermissions(session, null, document);
            } else {
                File oldDocument = fileAccess.getFileMetadata(document.getFolderId(), document.getId(), FileStorageFileAccess.CURRENT_VERSION);
                comparedPermissions = new ComparedObjectPermissions(session, oldDocument, document);
            }
            /*
             * check for general support if changes should be applied
             */
            if (comparedPermissions.hasChanges() && false == supports(fileAccess, FileStorageCapability.OBJECT_PERMISSIONS)) {
                throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(
                    fileAccess.getAccountAccess().getService().getDisplayName(), document.getFolderId(), session.getContextId());
            }
            /*
             * Remove new guests from the document and check them in terms of permission bits
             */
            if (comparedPermissions.hasNewGuests()) {
                List<FileStorageGuestObjectPermission> newGuestPermissions = comparedPermissions.getNewGuestPermissions();
                document.getObjectPermissions().removeAll(newGuestPermissions);
                FileStorageGuestObjectPermission newAnonymousPermission = null;
                for (FileStorageGuestObjectPermission p : newGuestPermissions) {
                    if (isInvalidGuestPermission(p)) {
                        throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(p.getPermissions(), p.getEntity(), document.getId());
                    }
                    if (RecipientType.ANONYMOUS.equals(p.getRecipient().getType())) {
                        if (null == newAnonymousPermission) {
                            newAnonymousPermission = p;
                        } else {
                            throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(p.getPermissions(), p.getEntity(), document.getId());
                        }
                    }
                }
                /*
                 * check for an already existing anonymous permission if a new one should be added
                 */
                if (null != newAnonymousPermission && containsOriginalAnonymousPermission(comparedPermissions)) {
                    throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(
                        newAnonymousPermission.getPermissions(), newAnonymousPermission.getEntity(), document.getId());
                }
            }
            /*
             * Check permission bits of added and modified guests that already exist as users.
             * Especially existing anonymous guests must not be added as permission entities.
             */
             if (comparedPermissions.hasAddedGuests()) {
                 FileStorageObjectPermission addedAnonymousPermission = null;
                 for (Integer guest : comparedPermissions.getAddedGuests()) {
                     FileStorageObjectPermission p = comparedPermissions.getAddedGuestPermission(guest);
                     GuestInfo guestInfo = comparedPermissions.getGuestInfo(guest);
                     if (isInvalidGuestPermission(p, guestInfo) || (isAnonymous(guestInfo) && isNotEqualsTarget(document, fileAccess.getAccountAccess(), guestInfo.getLinkTarget()))) {
                         throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(p.getPermissions(), p.getEntity(), document.getId());
                     }
                     if (isAnonymous(guestInfo)) {
                         if (null == addedAnonymousPermission) {
                             addedAnonymousPermission = p;
                         } else {
                             throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(p.getPermissions(), p.getEntity(), document.getId());
                         }
                     }
                 }
                 /*
                  * check for an already existing anonymous permission if another one should be added
                  */
                 if (null != addedAnonymousPermission && containsOriginalAnonymousPermission(comparedPermissions)) {
                     throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(
                         addedAnonymousPermission.getPermissions(), addedAnonymousPermission.getEntity(), document.getId());
                 }
             }
             if (comparedPermissions.hasModifiedGuests()) {
                 for (Integer guest : comparedPermissions.getModifiedGuests()) {
                     FileStorageObjectPermission p = comparedPermissions.getModifiedGuestPermission(guest);
                     if (isInvalidGuestPermission(p, comparedPermissions.getGuestInfo(guest))) {
                         throw FileStorageExceptionCodes.INVALID_OBJECT_PERMISSIONS.create(p.getPermissions(), p.getEntity(), document.getId());
                     }
                 }
             }

            return comparedPermissions;
        }
        return new ComparedObjectPermissions(session, (File)null, (File)null);
    }

    public static List<FileStorageObjectPermission> collectAddedObjectPermissions(ComparedObjectPermissions comparedPermissions, Session session) throws OXException {
        Collection<FileStorageObjectPermission> newPermissions = comparedPermissions.getNewPermissions();
        if (newPermissions == null || newPermissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<FileStorageObjectPermission> addedPermissions = new ArrayList<>(newPermissions.size());
        List<Integer> modifiedGuests = comparedPermissions.getModifiedGuests();
        for (FileStorageObjectPermission p : newPermissions) {
            // gather all new user entities except the one executing this operation
            if (!p.isGroup() && p.getEntity() != session.getUserId() && !modifiedGuests.contains(p.getEntity())) {
                addedPermissions.add(p);
            }
        }

        for (FileStorageObjectPermission p : comparedPermissions.getAddedGroupPermissions()) {
            addedPermissions.add(p);
        }

        return addedPermissions;
    }

    /**
     * Applies any added or removed guest object permissions for a document, based on the previously extracted object permission
     * comparison (via {@link ShareHelper#processGuestPermissions}). This includes removing shares for removed object permissions, adding
     * shares for new guest permissions, as well as writing back resolved object permissions containing the guest user entities to the
     * document.
     *
     * @param session The session
     * @param access The file access hosting the document
     * @param document The saved document
     * @param comparedPermissions The previously extracted object permission comparison
     * @return The ID tuple referencing the document
     * @throws OXException
     */
    public static IDTuple applyGuestPermissions(Session session, FileStorageFileAccess fileAccess, File document, ComparedObjectPermissions comparedPermissions) throws OXException {
        List<FileStorageObjectPermission> updatedPermissions = handleGuestPermissions(session, fileAccess, document, comparedPermissions);
        if (null != updatedPermissions) {
            document.setObjectPermissions(updatedPermissions);
            return fileAccess.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
        } else {
            return new IDTuple(document.getFolderId(), document.getId());
        }
    }

    private static List<FileStorageObjectPermission> handleGuestPermissions(Session session, FileStorageFileAccess fileAccess, File document, ComparedObjectPermissions comparedPermissions) throws OXException {
        List<FileStorageObjectPermission> updatedPermissions = null;
        if (null != comparedPermissions) {
            if (comparedPermissions.hasNewGuests()) {
                updatedPermissions = ShareHelper.handleNewGuestPermissions(session, fileAccess, document, comparedPermissions);
            }
            if (comparedPermissions.hasRemovedGuests()) {
                /*
                 * extract affected guest entities & schedule cleanup tasks
                 */
                List<Integer> affectedUserIDs = getAffectedUserIDs(comparedPermissions.getRemovedGuestPermissions());
                if (0 < affectedUserIDs.size()) {
                    Services.getService(ShareService.class).scheduleGuestCleanup(session.getContextId(), Autoboxing.I2i(affectedUserIDs));
                }
            }
        }
        return updatedPermissions;
    }

    private static List<FileStorageObjectPermission> handleNewGuestPermissions(Session session, FileStorageFileAccess access, File document, ComparedObjectPermissions comparedPermissions) throws OXException {
        Connection connection = ConnectionHolder.CONNECTION.get();
        session.setParameter(Connection.class.getName(), connection);
        try {
            if (comparedPermissions.hasNewGuests()) {
                List<FileStorageGuestObjectPermission> newGuestPermissions = comparedPermissions.getNewGuestPermissions();
                List<ShareRecipient> shareRecipients = new ArrayList<ShareRecipient>(newGuestPermissions.size());
                for (FileStorageGuestObjectPermission guestPermission : newGuestPermissions) {
                    shareRecipients.add(guestPermission.getRecipient());
                }

                List<FileStorageObjectPermission> allPermissions = new ArrayList<FileStorageObjectPermission>(shareRecipients.size());
                ShareService shareService = Services.getService(ShareService.class);
                if (null == shareService) {
                    throw ServiceExceptionCode.absentService(ShareService.class);
                }
                String service = access.getAccountAccess().getService().getId();
                String account = access.getAccountAccess().getAccountId();
                String folderID = new FolderID(service, account, document.getFolderId()).toUniqueID();
                String fileID = new FileID(service, account, document.getFolderId(), document.getId()).toUniqueID();
                ShareTarget shareTarget = new ShareTarget(8, folderID, fileID);
                CreatedShares shares = shareService.addTarget(session, shareTarget, shareRecipients);
                for (FileStorageGuestObjectPermission permission : newGuestPermissions) {
                    ShareInfo share = shares.getShare(permission.getRecipient());
                    GuestInfo guestInfo = share.getGuest();
                    allPermissions.add(new DefaultFileStorageObjectPermission(guestInfo.getGuestID(), false, permission.getPermissions()));
                    comparedPermissions.rememberGuestInfo(guestInfo);
                }
                List<FileStorageObjectPermission> objectPermissions = document.getObjectPermissions();
                if (objectPermissions != null) {
                    for (FileStorageObjectPermission objectPermission : objectPermissions) {
                        allPermissions.add(objectPermission);
                    }
                }

                return allPermissions;
            }
        } finally {
            session.setParameter(Connection.class.getName(), null);
        }

        return null;
    }

    private static List<Integer> getAffectedUserIDs(List<FileStorageObjectPermission> permissions) {
        if (null == permissions || 0 == permissions.size()) {
            return Collections.emptyList();
        }
        List<Integer> affectedUserIDs = new ArrayList<Integer>(permissions.size());
        for (FileStorageObjectPermission removedPermission : permissions) {
            if (false == removedPermission.isGroup()) {
                affectedUserIDs.add(Integer.valueOf(removedPermission.getEntity()));
            }
        }
        return affectedUserIDs;
    }

    private static boolean isAnonymous(GuestInfo guestInfo) {
        return guestInfo.getRecipientType() == RecipientType.ANONYMOUS;
    }

    private static boolean isNotEqualsTarget(File document, FileStorageAccountAccess accountAccess, ShareTarget target) {
        FileID fileId;
        FolderID folderId;
        if (document instanceof UserizedFile) {
            UserizedFile uFile = (UserizedFile) document;
            folderId = new FolderID(uFile.getOriginalFolderId());
            fileId = new FileID(uFile.getOriginalId());
        } else {
            folderId = new FolderID(document.getFolderId());
            fileId = new FileID(document.getId());
        }

        String service = accountAccess.getService().getId();
        String account = accountAccess.getAccountId();
        folderId.setService(service);
        folderId.setAccountId(account);
        fileId.setService(service);
        fileId.setAccountId(account);
        fileId.setFolderId(folderId.getFolderId());
        return !(new ShareTarget(8, folderId.toUniqueID(), fileId.toUniqueID()).equals(target));
    }

    private static boolean isInvalidGuestPermission(FileStorageGuestObjectPermission p) {
        return p.getRecipient().getType() == RecipientType.ANONYMOUS && (p.canWrite() || p.canDelete());
    }

    private static boolean isInvalidGuestPermission(FileStorageObjectPermission p, GuestInfo guestInfo) {
        if (guestInfo.getRecipientType() == RecipientType.ANONYMOUS) {
            return (p.canWrite() || p.canDelete());
        }

        return false;
    }

    /**
     * Gets a value indicating whether the original permissions in the supplied compared permissions instance already contain an
     * "anonymous" entity one or not.
     *
     * @param comparedPermissions The compared permissions to check
     * @return <code>true</code> if there's an "anonymous" entity in the original permissions, <code>false</code>, otherwise
     */
    private static boolean containsOriginalAnonymousPermission(ComparedObjectPermissions comparedPermissions) throws OXException {
        Collection<FileStorageObjectPermission> originalPermissions = comparedPermissions.getOriginalPermissions();
        if (null != originalPermissions && 0 < originalPermissions.size()) {
            for (FileStorageObjectPermission originalPermission : originalPermissions) {
                if (false == originalPermission.isGroup()) {
                    GuestInfo guestInfo = comparedPermissions.getGuestInfo(originalPermission.getEntity());
                    if (null != guestInfo && isAnonymous(guestInfo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
