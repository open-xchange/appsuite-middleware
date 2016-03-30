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

package com.openexchange.file.storage.json.actions.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.Entities.PermissionType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;

/**
 * {@link AbstractWriteAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractWriteAction extends AbstractFileAction {

    @Override
    protected void before(final AJAXInfostoreRequest req) throws OXException {
        super.before(req);
        req.getFileAccess().startTransaction();
    }

    @Override
    protected void success(final AJAXInfostoreRequest req, final AJAXRequestResult result) throws OXException {
        req.getFileAccess().commit();
    }

    @Override
    protected void failure(final AJAXInfostoreRequest req, final Throwable throwable) throws OXException {
        req.getFileAccess().rollback();
    }

    @Override
    protected void after(final AJAXInfostoreRequest req) {
        IDBasedFileAccess fileAccess = req.optFileAccess();
        if (null != fileAccess) {
            try {
                fileAccess.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
        IDBasedFolderAccess folderAccess = req.optFolderAccess();
        if (null != folderAccess) {
            try {
                folderAccess.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
        super.after(req);
    }

    /**
     * Gets a file's name in a safe way for use in error messages, without throwing exceptions.
     *
     * @param file The file metadata to get the filename for
     * @param id The file identifier, or <code>null</code> if not available
     * @param fileAccess A reference to the file access
     * @return The filename
     */
    protected static String getFilenameSave(File file, FileID id, IDBasedFileAccess fileAccess) {
        String name = file.getFileName();
        if (null != name) {
            return name;
        }
        name = file.getTitle();
        if (null != name) {
            return name;
        }
        if (null != id && null != fileAccess) {
            try {
                File metadata = fileAccess.getFileMetadata(id.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
                if (null != metadata) {
                    name = metadata.getFileName();
                    if (null == name) {
                        name = metadata.getTitle();
                    }
                }
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(UpdateAction.class).debug("Error getting name for file {}: {}", id, e.getMessage(), e);
            }
        }
        return name;
    }

    /**
     * Send out share notifications for added permission entities. Those entities are calculated based on
     * the passed file objects.
     *
     * @param transport The notification transport
     * @param message The notification message; may be <code>null</code>
     * @param original The file before any permission changes took place; may be <code>null</code> in case of a newly created file
     * @param modified The file after any permission changes took place
     * @param session The session
     * @param hostData The host data
     * @return A list of warnings to be included in the API response
     */
    protected List<OXException> sendNotifications(Transport transport, String message, File original, File modified, Session session, HostData hostData) {
        if (hostData == null) {
            return Collections.singletonList(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("HostData was not available"));
        }

        List<FileStorageObjectPermission> oldPermissions = original == null ? null : original.getObjectPermissions();
        if (oldPermissions == null) {
            oldPermissions = Collections.<FileStorageObjectPermission>emptyList();
        }
        List<FileStorageObjectPermission> newPermissions = modified.getObjectPermissions();
        if (newPermissions == null) {
            newPermissions = Collections.<FileStorageObjectPermission>emptyList();
        }
        List<FileStorageObjectPermission> addedPermissions = new ArrayList<>(newPermissions.size());
        for (FileStorageObjectPermission permission : newPermissions) {
            boolean isNew = true;
            for (FileStorageObjectPermission existing : oldPermissions) {
                if (existing.getEntity() == permission.getEntity() && existing.isGroup() == permission.isGroup()) {
                    isNew = false;
                    break;
                }
            }

            if (isNew) {
                addedPermissions.add(permission);
            }
        }

        if (addedPermissions.isEmpty()) {
            return Collections.emptyList();
        }
        ShareNotificationService notificationService = Services.getShareNotificationService();
        if (notificationService == null) {
            return Collections.singletonList(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("ShareNotificationService was absent"));
        }

        Entities entities = new Entities();
        for (FileStorageObjectPermission permission : addedPermissions) {
            if (permission.isGroup()) {
                entities.addGroup(permission.getEntity(), PermissionType.OBJECT, permission.getPermissions());
            } else {
                entities.addUser(permission.getEntity(), PermissionType.OBJECT, permission.getPermissions());
            }
        }

        return notificationService.sendShareCreatedNotifications(
            transport,
            entities,
            message,
            new ShareTargetPath(8, modified.getFolderId(), modified.getId()),
            session,
            hostData);

    }

}
