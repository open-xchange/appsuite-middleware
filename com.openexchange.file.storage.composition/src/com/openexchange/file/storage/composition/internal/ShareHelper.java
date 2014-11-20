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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.file.storage.composition.internal.FileStorageTools.supports;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FileStorageCapability;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.session.Session;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tx.ConnectionHolder;

/**
 * {@link ShareHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareHelper {

    /** com.openexchange.groupware.container.FolderObject.INFOSTORE */
    private static final int MODULE_FILE_STORAGE = 8;

    /**
     * Pre-processes the supplied document to extract added or removed guest object permissions required for sharing support. Guest object
     * permissions that are considered as "new", i.e. guest object permissions from the document metadata that are not yet resolved to a
     * guest user entity, are removed implicitly from the document in order to re-add them afterwards (usually by calling
     * {@link ShareHelper#applyGuestPermissions}).
     *
     * @param fileAccess The file access hosting the document
     * @param document The document being saved
     * @param modifiedColumns The modified fields as supplied by the client, or <code>null</code> if not set
     * @return The compared object permissions yielding new and removed guest object permissions
     */
    public static ComparedObjectPermissions processGuestPermissions(FileStorageFileAccess fileAccess, File document, List<Field> modifiedColumns) throws OXException {
        if (supports(fileAccess, FileStorageCapability.OBJECT_PERMISSIONS)) {
            ComparedObjectPermissions comparedPermissions;
            if (FileStorageFileAccess.NEW == document.getId()) {
                comparedPermissions = new ComparedObjectPermissions(null, document, modifiedColumns);
            } else {
                File oldDocument = fileAccess.getFileMetadata(document.getFolderId(), document.getId(), FileStorageFileAccess.CURRENT_VERSION);
                comparedPermissions = new ComparedObjectPermissions(oldDocument, document, modifiedColumns);
            }
            if (comparedPermissions.hasAddedGuestPermissions()) {
                document.getObjectPermissions().removeAll(comparedPermissions.getAddedGuestPermissions());
            }
            return comparedPermissions;
        }
        return ComparedObjectPermissions.EMPTY;
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

    /**
     * Removes all shares referencing any of the supplied document identifiers, usually after the corresponding files are deleted in the
     * storage.
     *
     * @param session The session
     * @param access The file access hosting the documents
     * @param ids The identifiers of the files to remove the shares for
     * @throws OXException
     */
    public static void removeShares(Session session, FileStorageFileAccess fileAccess, List<IDTuple> ids) throws OXException {
        if (null != ids && 0 < ids.size() && supports(fileAccess, FileStorageCapability.OBJECT_PERMISSIONS)) {
            /*
             * prepare share targets for removal
             */
            String serviceId = fileAccess.getAccountAccess().getService().getId();
            String accountId = fileAccess.getAccountAccess().getAccountId();
            List<ShareTarget> shareTargets = new ArrayList<ShareTarget>(ids.size());
            for (IDTuple tuple : ids) {
                String folderID = new FolderID(serviceId, accountId, tuple.getFolder()).toUniqueID();
                String fileID = new FileID(serviceId, accountId, tuple.getFolder(), tuple.getId()).toUniqueID();
                shareTargets.add(new ShareTarget(MODULE_FILE_STORAGE, folderID, fileID));
            }
            /*
             * remove all shares targeting the documents
             */
            Connection connection = ConnectionHolder.CONNECTION.get();
            try {
                session.setParameter(Connection.class.getName(), connection);
                Services.getService(ShareService.class).deleteTargets(session, shareTargets, null);
            } finally {
                session.setParameter(Connection.class.getName(), null);
            }
        }
    }

    /**
     * Removes all shares referencing any of the supplied document identifiers, usually after the corresponding files are deleted in the
     * storage.
     *
     * @param session The session
     * @param access The file access hosting the documents
     * @param folderID The identifiers of the folder to remove the shares for
     * @throws OXException
     */
    public static void removeShares(Session session, FileStorageFileAccess fileAccess, String folderID) throws OXException {
        if (null != folderID && supports(fileAccess, FileStorageCapability.OBJECT_PERMISSIONS)) {
            /*
             * prepare share target for removal
             */
            String serviceId = fileAccess.getAccountAccess().getService().getId();
            String accountId = fileAccess.getAccountAccess().getAccountId();
            String uniqueID = new FolderID(serviceId, accountId, folderID).toUniqueID();
            ShareTarget shareTarget = new ShareTarget(MODULE_FILE_STORAGE, uniqueID);
            /*
             * remove all shares targeting the documents in the folder
             */
            Connection connection = ConnectionHolder.CONNECTION.get();
            try {
                session.setParameter(Connection.class.getName(), connection);
                Services.getService(ShareService.class).deleteTargets(session, Collections.singletonList(shareTarget), true);
            } finally {
                session.setParameter(Connection.class.getName(), null);
            }
        }
    }

    private static List<FileStorageObjectPermission> handleGuestPermissions(Session session, FileStorageFileAccess fileAccess, File document, ComparedObjectPermissions comparedPermissions) throws OXException {
        List<FileStorageObjectPermission> updatedPermissions = null;
        if (null != comparedPermissions) {
            if (comparedPermissions.hasAddedGuestPermissions()) {
                updatedPermissions = ShareHelper.handleNewGuestPermissions(session, fileAccess, document, comparedPermissions.getAddedGuestPermissions());
            }
            if (comparedPermissions.hasRemovedGuestPermissions()) {
                ShareHelper.handleRemovedObjectPermissions(session, fileAccess, document, comparedPermissions.getRemovedGuestPermissions());
            }
        }
        return updatedPermissions;
    }

    /**
     * Removes shares referencing a document to reflect the removal of object permissions previously assigned to the document.
     *
     * @param session The session
     * @param fileAccess The file access
     * @param document The document where the permissions are removed
     * @param removedPermissions The permissions that are removed
     * @return <code>true</code> if shares were removed, <code>false</code>, otherwise
     * @throws OXException
     */
    private static boolean handleRemovedObjectPermissions(Session session, FileStorageFileAccess fileAccess, File document, List<FileStorageObjectPermission> removedPermissions) throws OXException {
        /*
         * extract affected user entities
         */
        List<Integer> affectedUserIDs = getAffectedUserIDs(removedPermissions);
        if (0 == affectedUserIDs.size()) {
            return false;
        }
        /*
         * prepare share target representing the document
         */
        String service = fileAccess.getAccountAccess().getService().getId();
        String account = fileAccess.getAccountAccess().getAccountId();
        String folderID = new FolderID(service, account, document.getFolderId()).toUniqueID();
        String fileID = new FileID(service, account, document.getFolderId(), document.getId()).toUniqueID();
        ShareTarget shareTarget = new ShareTarget(8, folderID, fileID);
        /*
         * remove shares targeting the document for all affected users
         */
        Connection connection = ConnectionHolder.CONNECTION.get();
        try {
            session.setParameter(Connection.class.getName(), connection);
            Services.getService(ShareService.class).deleteTargets(session, Collections.singletonList(shareTarget), affectedUserIDs);
            return true;
        } finally {
            session.setParameter(Connection.class.getName(), null);
        }
    }

    private static List<FileStorageObjectPermission> handleNewGuestPermissions(Session session, FileStorageFileAccess access, File document, List<FileStorageGuestObjectPermission> guestPermissions) throws OXException {
        Connection connection = ConnectionHolder.CONNECTION.get();
        try {
            session.setParameter(Connection.class.getName(), connection);
            if (guestPermissions != null && !guestPermissions.isEmpty()) {
                List<ShareRecipient> shareRecipients = new ArrayList<ShareRecipient>(guestPermissions.size());
                for (FileStorageGuestObjectPermission guestPermission : guestPermissions) {
                    shareRecipients.add(guestPermission.getRecipient());
                }

                List<FileStorageObjectPermission> allPermissions = new ArrayList<FileStorageObjectPermission>(shareRecipients.size());
                ShareService shareService = Services.getService(ShareService.class);
                int owner = document.getCreatedBy();
                if (0 >= owner) {
                    owner = access.getFileMetadata(
                        document.getFolderId(), document.getId(), FileStorageFileAccess.CURRENT_VERSION).getCreatedBy();
                }
                String service = access.getAccountAccess().getService().getId();
                String account = access.getAccountAccess().getAccountId();
                String folderID = new FolderID(service, account, document.getFolderId()).toUniqueID();
                String fileID = new FileID(service, account, document.getFolderId(), document.getId()).toUniqueID();
                ShareTarget shareTarget = new ShareTarget(8, folderID, fileID);
                shareTarget.setOwnedBy(owner);
                List<ShareInfo> shares = shareService.addTarget(session, shareTarget, shareRecipients);
                for (int i = 0; i < guestPermissions.size(); i++) {
                    FileStorageGuestObjectPermission guestPermission = guestPermissions.get(0);
                    ShareInfo share = shares.get(i);
                    allPermissions.add(new DefaultFileStorageObjectPermission(share.getGuest().getGuestID(), false, guestPermission.getPermissions()));
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

}
