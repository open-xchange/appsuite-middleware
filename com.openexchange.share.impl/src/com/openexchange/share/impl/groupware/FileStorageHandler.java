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

package com.openexchange.share.impl.groupware;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ShareTargetDiff;
import com.openexchange.share.recipient.InternalRecipient;


/**
 * {@link FileStorageHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileStorageHandler extends AbstractModuleHandler {

    /**
     * Initializes a new {@link FileStorageHandler}.
     */
    public FileStorageHandler(ServiceLookup services) {
        super(services);
    }

    @Override
    public int getModule() {
        return Module.INFOSTORE.getFolderConstant();
    }

    @Override
    protected String getItemTitle(String folder, String item, Session session) throws OXException {
        FileID fileID = new FileID(item);
        if (fileID.getFolderId() == null) {
            fileID.setFolderId(folder);
        }
        File file = getFileAccess(session).getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
        String title = file.getTitle();
        if (title == null) {
            title = file.getFileName();
        }

        return title;
    }

    @Override
    public void updateObjects(ShareTargetDiff targetDiff, List<InternalRecipient> finalRecipients, Session session, Connection writeCon) throws OXException {
        IDBasedFileAccess fileAccess = getFileAccess(session);
        try {
            fileAccess.startTransaction();
            for (ShareTarget target : targetDiff.getAdded()) {
                FileID fileID = new FileID(target.getItem());
                if (fileID.getFolderId() == null) {
                    fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                }

                File file = new DefaultFile(fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION));
                mergePermissions(file, finalRecipients);
                fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
            }

            for (ShareTarget target : targetDiff.getModified()) {
                FileID fileID = new FileID(target.getItem());
                if (fileID.getFolderId() == null) {
                    fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                }

                File file = new DefaultFile(fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION));
                mergePermissions(file, finalRecipients);
                fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
            }

            for (ShareTarget target : targetDiff.getRemoved()) {
                FileID fileID = new FileID(target.getItem());
                if (fileID.getFolderId() == null) {
                    fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                }

                File file = new DefaultFile(fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION));
                removePermissions(file, finalRecipients);
                fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
            }

            fileAccess.commit();
        } catch (OXException e) {
            fileAccess.rollback();
            throw e;
        } finally {
            fileAccess.finish();
        }

    }

    private void removePermissions(File file, List<InternalRecipient> finalRecipients) {
        List<FileStorageObjectPermission> origPermissions = file.getObjectPermissions();
        if (origPermissions == null || origPermissions.isEmpty()) {
            return;
        }

        List<FileStorageObjectPermission> newPermissions = new ArrayList<FileStorageObjectPermission>(origPermissions.size());
        Map<Integer, FileStorageObjectPermission> permissionsByEntity = new HashMap<Integer, FileStorageObjectPermission>();
        for (FileStorageObjectPermission permission : origPermissions) {
            permissionsByEntity.put(permission.getEntity(), permission);
        }

        for (InternalRecipient recipient : finalRecipients) {
            permissionsByEntity.remove(recipient.getEntity());
        }

        for (FileStorageObjectPermission permission : permissionsByEntity.values()) {
            newPermissions.add(permission);
        }

        file.setObjectPermissions(newPermissions);
    }

    private void mergePermissions(File file, List<InternalRecipient> finalRecipients) {
        List<FileStorageObjectPermission> origPermissions = file.getObjectPermissions();
        if (origPermissions == null) {
            origPermissions = Collections.emptyList();
        }

        List<FileStorageObjectPermission> newPermissions = new ArrayList<FileStorageObjectPermission>(origPermissions.size() + finalRecipients.size());
        if (origPermissions.isEmpty()) {
            for (InternalRecipient recipient : finalRecipients) {
                newPermissions.add(new DefaultFileStorageObjectPermission(recipient.getEntity(), recipient.isGroup(), getObjectPermissionBits(recipient.getBits())));
            }
        } else {
            Map<Integer, FileStorageObjectPermission> permissionsByEntity = new HashMap<Integer, FileStorageObjectPermission>();
            for (FileStorageObjectPermission permission : origPermissions) {
                permissionsByEntity.put(permission.getEntity(), permission);
            }

            for (InternalRecipient recipient : finalRecipients) {
                permissionsByEntity.remove(recipient.getEntity());
                newPermissions.add(new DefaultFileStorageObjectPermission(recipient.getEntity(), recipient.isGroup(), recipient.getBits()));
            }

            for (FileStorageObjectPermission permission : permissionsByEntity.values()) {
                newPermissions.add(permission);
            }
        }

        file.setObjectPermissions(newPermissions);
    }

    private IDBasedFileAccess getFileAccess(Session session) throws OXException {
        IDBasedFileAccessFactory factory = getService(IDBasedFileAccessFactory.class);
        return factory.createAccess(session);
    }

}
