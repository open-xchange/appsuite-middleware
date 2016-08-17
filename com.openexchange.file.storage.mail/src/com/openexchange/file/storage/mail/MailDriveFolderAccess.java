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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.file.storage.mail;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link MailDriveFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFolderAccess extends AbstractMailDriveResourceAccess implements FileStorageFolderAccess {

    private final int userId;

    /**
     * Initializes a new {@link MailDriveFolderAccess}.
     *
     * @param fullNameCollection The collection of valid full names
     * @param session The associated session
     * @throws OXException If initialization fails
     */
    public MailDriveFolderAccess(FullNameCollection fullNameCollection, Session session) throws OXException {
        super(fullNameCollection, session);
        userId = session.getUserId();
    }

    protected MailDriveFolder parsedFolder(FullName fullName) throws OXException {
        return new MailDriveFolder(userId).parseFullName(fullName, session);
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        FullName fullName = optFolderId(folderId);
        return null != fullName;
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        final FullName fullName = checkFolderId(folderId);
        return parsedFolder(fullName);
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        FullName fullName = checkFolderId(parentIdentifier);

        if (fullName.isNotDefaultFolder()) {
            return new FileStorageFolder[0];
        }

        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>(4);
        for (FullName fn : fullNameCollection) {
            if (!FullName.Type.ALL.equals(fn.getType())) {
                folders.add(parsedFolder(fn));
            }
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return parsedFolder(fullNameCollection.getFullNameFor(FullName.Type.ALL));
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId, final String newName) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public String deleteFolder(final String folderId, boolean hardDelete) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public void clearFolder(final String folderId, boolean hardDelete) throws OXException {
        // Read only...
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(MailDriveConstants.ID);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        FullName fullName = checkFolderId(folderId);

        if (fullName.isNotDefaultFolder()) {
            return new FileStorageFolder[] { parsedFolder(fullName) };
        }

        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>(3);
        folders.add(parsedFolder(fullName));
        folders.add(parsedFolder(fullNameCollection.getFullNameFor(FullName.Type.ALL)));
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return Type.STORAGE.getUnlimited();
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        if (null == types) {
            return null;
        }
        Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case FILE:
                    quotas[i] = getFileQuota(folder);
                    break;
                case STORAGE:
                    quotas[i] = getStorageQuota(folder);
                    break;
                default:
                    throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Quota " + types[i]);
            }
        }
        return quotas;
    }

}
