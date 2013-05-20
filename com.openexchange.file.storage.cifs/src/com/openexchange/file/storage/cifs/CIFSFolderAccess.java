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

package com.openexchange.file.storage.cifs;

import static com.openexchange.file.storage.cifs.Utils.checkFolderId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import org.apache.commons.httpclient.URI;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.cifs.cache.SmbFileMapManagement;
import com.openexchange.session.Session;

/**
 * {@link CIFSFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSFolderAccess extends AbstractCIFSAccess implements FileStorageFolderAccess {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CIFSFolderAccess.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Constant string to indicate that home directory has not been found.
     */
    private static final String NOT_FOUND = "__NONE";

    private final String login;

    private volatile String homeDirPath;

    /**
     * Initializes a new {@link CIFSFolderAccess}.
     */
    public CIFSFolderAccess(final String rootUrl, final NtlmPasswordAuthentication auth, final FileStorageAccount account, final Session session, final WarningsAware warningsAware) {
        super(rootUrl, auth, account, session, warningsAware);
        login = (String) account.getConfiguration().get(CIFSConstants.CIFS_LOGIN);
        homeDirPath = (String) account.getConfiguration().get(CIFSConstants.CIFS_HOME_DIR);
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final SmbFile smbFolder = getSmbFile(fid);
            if (!exists(smbFolder)) {
                return false;
            }
            if (!smbFolder.isDirectory()) {
                /*
                 * Not a directory
                 */
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            return true;
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
            final String fid = checkFolderId(folderId, rootUrl);
            final SmbFile smbFolder = getSmbFile(fid);
            if (!exists(smbFolder)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * Convert to FileStorageFolder
             */
            return toFileStorageFolder(folderId, smbFolder);
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final class CountingSmbFileFilter implements SmbFileFilter {

        private int fileCount;
        private boolean hasSubdir;

        protected CountingSmbFileFilter() {
            super();
            fileCount = 0;
            hasSubdir = false;
        }

        @Override
        public boolean accept(SmbFile file) throws SmbException {
            if (file.isDirectory()) {
                hasSubdir = true;
            } else if (file.isFile()) {
                fileCount++;
            }
            return false;
        }

        public boolean hasSubdir() {
            return hasSubdir;
        }

        public int getFileCount() {
            return fileCount;
        }
    }

    private CIFSFolder toFileStorageFolder(final String folderId, final SmbFile smbFolder) throws SmbException, OXException {
        /*
         * Check sub resources
         */
        final CountingSmbFileFilter filter = new CountingSmbFileFilter();
        try {
            if (smbFolder.canRead()) {
                smbFolder.listFiles(filter);
            }
        } catch (final SmbException e) {
            if (!indicatesNotReadable(e)) {
                throw e;
            }
        }
        /*
         * Convert to a folder
         */
        final CIFSFolder cifsFolder = new CIFSFolder(session.getUserId(), rootUrl);
        cifsFolder.parseSmbFolder(smbFolder);
        cifsFolder.setFileCount(filter.getFileCount());
        cifsFolder.setSubfolders(filter.hasSubdir());
        cifsFolder.setSubscribedSubfolders(filter.hasSubdir);
        /*
         * Home dir or public folder?
         */
        {
            final String homeDirectory = getHomeDirectory();
            if (null != homeDirectory) {
                if (homeDirectory.equals(smbFolder.getPath())) {
                    cifsFolder.setType(FileStorageFolderType.HOME_DIRECTORY);
                } else if (FileStorageFolder.ROOT_FULLNAME.equals(cifsFolder.getParentId())) {
                    cifsFolder.setType(FileStorageFolderType.PUBLIC_FOLDER);
                }
            }
        }
        /*
         * TODO: Set capabilities
         */
        return cifsFolder;
    }

    private String getHomeDirectory() {
        /*
         * Check
         */
        String homeDirPath = this.homeDirPath;
        if (null == homeDirPath) {
            synchronized (this) {
                homeDirPath = this.homeDirPath;
                if (null == homeDirPath) {
                    /*
                     * Try to determine home directory by user name
                     */
                    final long st = DEBUG ? System.currentTimeMillis() : 0L;
                    try {
                        final String folderId = rootUrl;
                        final String fid = checkFolderId(folderId, rootUrl);
                        final SmbFile smbFolder = getSmbFile(fid);
                        if (!exists(smbFolder)) {
                            throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
                        }
                        if (!smbFolder.isDirectory()) {
                            throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
                        }
                        /*
                         * Check sub resources
                         */
                        final SmbFile homeDir = recursiveSearch(smbFolder, login + '/');
                        if (null == homeDir) {
                            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
                        }
                        homeDirPath = homeDir.getPath();
                    } catch (final OXException e) {
                        homeDirPath = NOT_FOUND;
                    } catch (final SmbException e) {
                        homeDirPath = NOT_FOUND;
                    } catch (final IOException e) {
                        homeDirPath = NOT_FOUND;
                    } catch (final RuntimeException e) {
                        homeDirPath = NOT_FOUND;
                    }
                    if (DEBUG) {
                        final long dur = System.currentTimeMillis() - st;
                        LOG.debug("CIFSFolderAccess.getHomeDirectory() took " + dur + "msec.");
                    }
                    this.homeDirPath = homeDirPath;
                }
            }
        }
        return NOT_FOUND.equals(homeDirPath) ? null : homeDirPath;
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        try {
            /*
             * Check
             */
            final String homeDirPath = this.homeDirPath;
            if (NOT_FOUND.equals(homeDirPath)) {
                throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
            }
            final SmbFile homeDir;
            if (null == homeDirPath) {
                /*
                 * Try to determine home directory by user name
                 */
                final String folderId = rootUrl;
                final String fid = checkFolderId(folderId, rootUrl);
                final SmbFile smbFolder = getSmbFile(fid);
                if (!exists(smbFolder)) {
                    throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
                }
                if (!smbFolder.isDirectory()) {
                    throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
                }
                /*
                 * Check sub resources
                 */
                homeDir = recursiveSearch(smbFolder, login + '/');
                if (null == homeDir) {
                    throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
                }
                this.homeDirPath = homeDir.getPath();
            } else {
                /*
                 * Get by configured path
                 */
                final String folderId = homeDirPath;
                final String fid = checkFolderId(folderId, rootUrl);
                homeDir = getSmbFile(fid);
                if (!exists(homeDir)) {
                    throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
                }
                if (!homeDir.isDirectory()) {
                    throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
                }
            }
            /*
             * Convert to FileStorageFolder
             */
            return toFileStorageFolder(homeDir.getPath(), homeDir).setType(FileStorageFolderType.HOME_DIRECTORY);
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final SmbFileFilter DICTIONARY_FILTER = new SmbFileFilter() {

        @Override
        public boolean accept(SmbFile file) throws SmbException {
            return file.isDirectory();
        }
    };

    private SmbFile recursiveSearch(final SmbFile smbFolder, final String appendix) throws SmbException {
        /*
         * Check sub resources
         */
        SmbFile[] subFiles;
        try {
            subFiles = smbFolder.canRead() ? smbFolder.listFiles(DICTIONARY_FILTER) : new SmbFile[0];
        } catch (final SmbException e) {
            if (!indicatesNotReadable(e)) {
                throw e;
            }
            subFiles = new SmbFile[0];
        }
        SmbFile homeDir = null;
        for (int i = 0; null == homeDir && i < subFiles.length; i++) {
            final SmbFile sub = subFiles[i];
            if (sub.isDirectory()) {
                final String path = sub.getPath();
                if (path.endsWith(appendix)) {
                    homeDir = sub;
                } else {
                    final SmbFile tmp = recursiveSearch(sub, appendix);
                    if (null != tmp) {
                        homeDir = sub;
                    }
                }
            }
        }
        return homeDir;
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        try {
            /*
             * All shares except home directory
             */
            final String homeDirectory = getHomeDirectory();
            if (null == homeDirectory) {
                /*
                 * No public folders without a home directory
                 */
                return new FileStorageFolder[0];
            }
            /*
             * Return root folders
             */
            final FileStorageFolder[] subfolders = getSubfolders(rootUrl, false);
            for (FileStorageFolder folder : subfolders) {
                ((CIFSFolder) folder).setType(FileStorageFolderType.PUBLIC_FOLDER);
            }
            return subfolders;
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentId, final boolean all) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(parentId, rootUrl);
            final SmbFile smbFolder = getSmbFile(fid);
            if (!exists(smbFolder)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(parentId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(parentId);
            }
            /*
             * Check sub resources
             */
            SmbFile[] subFiles;
            try {
                if (DEBUG) {
                    final long st = System.currentTimeMillis();
                    subFiles = smbFolder.canRead() ? smbFolder.listFiles(DICTIONARY_FILTER) : new SmbFile[0];
                    final long dur = System.currentTimeMillis() - st;
                    LOG.debug("CIFSFolderAccess.getSubfolders() - SmbFile.listFiles() took " + dur + "msec.");
                } else {
                    subFiles = smbFolder.canRead() ? smbFolder.listFiles(DICTIONARY_FILTER) : new SmbFile[0];
                }
            } catch (final SmbException e) {
                if (!indicatesNotReadable(e)) {
                    throw e;
                }
                subFiles = new SmbFile[0];
            }
            /*
             * All shares except home directory
             */
            final List<FileStorageFolder> list = new ArrayList<FileStorageFolder>(subFiles.length);
            final String homeDirPath = getHomeDirectory();
            for (final SmbFile sub : subFiles) {
                if (sub.isDirectory()) {
                    final String path = sub.getPath();
                    if ((null == homeDirPath || !homeDirPath.equals(path)) && !isHidden(sub)) {
                        try {
                            list.add(getFolder(path));
                        } catch (final OXException e) {
                            if (!CIFSExceptionCodes.NOT_FOUND.equals(e)) {
                                throw e;
                            }
                        }
                    }
                }
            }
            /*
             * Return
             */
            return list.toArray(new FileStorageFolder[list.size()]);
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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
            final SmbFile smbFolder = getSmbFile(pid);
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
            final SmbFile newDir = getSmbFile(fid);
            newDir.mkdir();
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            return fid;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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
        } catch (final RuntimeException e) {
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
                URI uri = new URI(fid, false);
                String path = uri.getPath();
                if (path.endsWith(SLASH)) {
                    path = path.substring(0, path.length() - 1);
                }
                final int pos = path.lastIndexOf('/');
                final String name = pos >= 0 ? path.substring(pos) : path;

                uri = new URI(newParentId, false);
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
            final SmbFile copyMe = getSmbFile(fid);
            if (!exists(copyMe)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!copyMe.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final SmbFile dest = getSmbFile(newUri);
            /*
             * Perform COPY
             */
            copyMe.copyTo(dest);
            /*
             * Now delete
             */
            copyMe.delete();
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            /*
             * Return URL
             */
            return newUri;
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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
                final URI uri = new URI(fid, false);
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
            final SmbFile renameMe = getSmbFile(fid);
            if (!exists(renameMe)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!renameMe.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final SmbFile dest = getSmbFile(newUri);
            /*
             * Perform COPY
             */
            renameMe.copyTo(dest);
            /*
             * Now delete
             */
            renameMe.delete();
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            /*
             * Return URL
             */
            return newUri;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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
            final SmbFile deleteMe = getSmbFile(fid);
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
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            /*
             * Return
             */
            return fid;
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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
            final SmbFile smbFolder = getSmbFile(fid);
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
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
        } catch (final SmbAuthException e) {
            throw FileStorageExceptionCodes.LOGIN_FAILED.create(e, login, rootUrl, CIFSConstants.ID);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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

    private static boolean isHidden(final SmbFile smbFolder) {
        if (null == smbFolder) {
            return true;
        }
        final String name = smbFolder.getName();
        return isEmpty(name) || '$' == name.charAt(0);
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
