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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cmis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link CMISFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CMISFolderAccess extends AbstractCMISAccess implements FileStorageFolderAccess {

    /**
     * Initializes a new {@link CMISFolderAccess}.
     */
    public CMISFolderAccess(final String rootUrl, final org.apache.chemistry.opencmis.client.api.Session cmisSession, final FileStorageAccount account, final Session session) {
        super(rootUrl, cmisSession, account, session);
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId)) {
            return true;
        }
        try {
            /*
             * Check
             */
            final CmisObject object = cmisSession.getObject(cmisSession.createObjectId(folderId));
            if (null == object) {
                return false;
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType())) {
                return false;
            }
            return true;
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        try {
            /*
             * Check
             */
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            return convertFolder(folderObjectId, (Folder) object);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private FileStorageFolder convertFolder(final ObjectId folderObjectId, final Folder folder) throws OXException {
        /*
         * Check sub resources
         */
        final ItemIterable<CmisObject> children = folder.getChildren();
        boolean hasSubdir = false;
        int fileCount = 0;
        for (final CmisObject sub : children) {
            if (ObjectType.FOLDER_BASETYPE_ID.equals(sub.getType())) {
                hasSubdir = true;
            } else if (ObjectType.DOCUMENT_BASETYPE_ID.equals(sub.getType())) {
                fileCount++;
            }
        }
        cmisSession.getAcl(folderObjectId, true);
        //cmisSession.get
        /*
         * Convert to a folder
         */
        final CMISFolder cmisFolder = new CMISFolder(session.getUserId());
        cmisFolder.parseSmbFolder(folder);
        cmisFolder.setFileCount(fileCount);
        cmisFolder.setSubfolders(hasSubdir);
        cmisFolder.setSubscribedSubfolders(hasSubdir);
        /*
         * TODO: Set capabilities
         */
        return cmisFolder;
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentId, final boolean all) throws OXException {
        try {
            /*
             * Check
             */
            final ObjectId folderObjectId;
            final CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(parentId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(parentId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(parentId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(parentId);
            }
            final Folder folder = (Folder) object;
            /*
             * Check sub resources
             */
            final ItemIterable<CmisObject> children = folder.getChildren();
            final List<FileStorageFolder> subs = new LinkedList<FileStorageFolder>();
            for (final CmisObject sub : children) {
                if (ObjectType.FOLDER_BASETYPE_ID.equals(sub.getType())) {
                    subs.add(convertFolder(cmisSession.createObjectId(sub.getId()), (Folder) sub));
                }
            }
            /*
             * Return
             */
            return subs.toArray(new FileStorageFolder[subs.size()]);
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(rootUrl);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        try {
            final String parentId = toCreate.getParentId();
            final String pid = checkFolderId(parentId, rootUrl);
            final SmbFile smbFolder = new SmbFile(pid, auth);
            if (!exists(smbFolder)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(parentId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(parentId);
            }
            /*
             * Create folder
             */
            final String fid = pid + '/' + toCreate.getName() + '/';
            final SmbFile newDir = new SmbFile(fid, auth);
            newDir.mkdir();
            return fid;
        } catch (final OXException e) {
            throw e;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String updateFolder(final String folderId, final FileStorageFolder toUpdate) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUrl);
            if (rootUrl.equalsIgnoreCase(fid)) {
                throw CIFSExceptionCodes.UPDATE_DENIED.create(fid);
            }
            /*
             * CIFS/SMB does neither support permissions nor subscriptions
             */
            return fid;
        } catch (final OXException e) {
            throw e;
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUrl);
            if (rootUrl.equalsIgnoreCase(fid)) {
                throw CIFSExceptionCodes.UPDATE_DENIED.create(fid);
            }
            /*
             * New URI
             */
            final String newUri;
            {
                URI uri = new URI(fid, true);
                String path = uri.getPath();
                if (path.endsWith(SLASH)) {
                    path = path.substring(0, path.length() - 1);
                }
                final int pos = path.lastIndexOf('/');
                final String name = pos >= 0 ? path.substring(pos) : path;

                uri = new URI(newParentId, true);
                path = uri.getPath();
                if (path.endsWith(SLASH)) {
                    path = path.substring(0, path.length() - 1);
                }
                uri.setPath(new StringBuilder(path).append('/').append(name).toString());
                newUri = checkFolderId(uri.toString());
            }
            /*
             * Check validity
             */
            final SmbFile copyMe = new SmbFile(fid, auth);
            if (!exists(copyMe)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!copyMe.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final SmbFile dest = new SmbFile(newUri, auth);
            /*
             * Perform COPY
             */
            copyMe.copyTo(dest);
            /*
             * Now delete
             */
            copyMe.delete();
            /*
             * Return URL
             */
            return newUri;
        } catch (final OXException e) {
            throw e;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUrl);
            if (rootUrl.equalsIgnoreCase(fid)) {
                throw CIFSExceptionCodes.UPDATE_DENIED.create(fid);
            }
            /*
             * New URI
             */
            final String newUri;
            {
                final URI uri = new URI(fid, true);
                String path = uri.getPath();
                if (path.endsWith(SLASH)) {
                    path = path.substring(0, path.length() - 1);
                }
                final int pos = path.lastIndexOf('/');
                uri.setPath(pos > 0 ? new StringBuilder(path.substring(0, pos)).append('/').append(newName).toString() : newName);
                newUri = checkFolderId(uri.toString());
            }
            /*
             * Check validity
             */
            final SmbFile renameMe = new SmbFile(fid, auth);
            if (!exists(renameMe)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!renameMe.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final SmbFile dest = new SmbFile(newUri, auth);
            /*
             * Perform COPY
             */
            renameMe.copyTo(dest);
            /*
             * Now delete
             */
            renameMe.delete();
            /*
             * Return URL
             */
            return newUri;
        } catch (final OXException e) {
            throw e;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        return deleteFolder(folderId, true);
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUrl);
            if (rootUrl.equalsIgnoreCase(fid)) {
                throw CIFSExceptionCodes.DELETE_DENIED.create(fid);
            }
            /*
             * Check validity
             */
            final SmbFile deleteMe = new SmbFile(fid, auth);
            if (!exists(deleteMe)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!deleteMe.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * Delete
             */
            deleteMe.delete();
            /*
             * Return
             */
            return fid;
        } catch (final OXException e) {
            throw e;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        clearFolder(folderId, true);
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        clearFolder0(folderId);
    }

    private void clearFolder0(final String folderId) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUrl);
            /*
             * Check validity
             */
            final SmbFile smbFolder = new SmbFile(fid, auth);
            if (!exists(smbFolder)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * Get sub-files
             */
            final SmbFile[] listFiles = smbFolder.listFiles();
            for (final SmbFile sub : listFiles) {
                sub.delete();
            }
        } catch (final OXException e) {
            throw e;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        final List<FileStorageFolder> list = new ArrayList<FileStorageFolder>();
        final String fid = checkFolderId(folderId, rootUrl);
        FileStorageFolder f = getFolder(fid);
        do {
            list.add(f);
            f = getFolder(f.getParentId());
        } while (!FileStorageFolder.ROOT_FULLNAME.equals(f.getParentId()));

        return list.toArray(new FileStorageFolder[list.size()]);
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Quota.getUnlimitedQuota(Quota.Type.STORAGE);
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Quota.getUnlimitedQuota(Quota.Type.FILE);
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] ret = new Quota[types.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Quota.getUnlimitedQuota(types[i]);
        }
        return ret;
    }

}
