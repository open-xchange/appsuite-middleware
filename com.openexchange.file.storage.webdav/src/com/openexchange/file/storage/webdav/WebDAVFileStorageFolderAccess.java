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

package com.openexchange.file.storage.webdav;

import static com.openexchange.file.storage.webdav.WebDAVFileStorageResourceUtil.checkFolderId;
import static com.openexchange.file.storage.webdav.WebDAVFileStorageResourceUtil.getHref;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link WebDAVFileStorageFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> - Exceptions
 */
public final class WebDAVFileStorageFolderAccess extends AbstractWebDAVAccess implements FileStorageFolderAccess {

    private final String rootUri;

    /**
     * Initializes a new {@link WebDAVFileStorageFolderAccess}.
     */
    public WebDAVFileStorageFolderAccess(final HttpClient client, final FileStorageAccount account, final Session session) {
        super(client, account, session);
        rootUri = checkFolderId(((String) account.getConfiguration().get(WebDAVConstants.WEBDAV_URL)).trim());
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid, true);
            final DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                client.executeMethod(propFindMethod);
                /*
                 * Check if request was successfully executed
                 */
                propFindMethod.checkSuccess();
                /*
                 * Get MultiStatus response
                 */
                final MultiStatus multiStatus = propFindMethod.getResponseBodyAsMultiStatus();
                /*
                 * Find MultiStatus for specified folder URI
                 */
                final URI tmp = new URI(fid, true);
                for (final MultiStatusResponse multiStatusResponse : multiStatus.getResponses()) {
                    /*
                     * Get the DAV property set for 200 (OK) status
                     */
                    final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                    final String href = getHref(multiStatusResponse.getHref(), propertySet);
                    tmp.setEscapedPath(href);
                    if (uri.equals(tmp)) {
                        /*
                         * Check for collection
                         */
                        if (!href.endsWith(SLASH)) {
                            /*
                             * Not a directory
                             */
                            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(WebDAVConstants.ID, fid);
                        }
                        /*
                         * Found directory
                         */
                        return true;
                    }
                }
                return false;
            } finally {
                closeHttpMethod(propFindMethod);
            }
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid, true);
            final WebDAVFileStorageFolder ret = new WebDAVFileStorageFolder(fid, rootUri, session.getUserId());
            final DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                client.executeMethod(propFindMethod);
                /*
                 * Check if request was successfully executed
                 */
                propFindMethod.checkSuccess();
                /*
                 * Get MultiStatus response
                 */
                final MultiStatus multiStatus = propFindMethod.getResponseBodyAsMultiStatus();
                /*
                 * Find MultiStatus for specified folder URI
                 */
                final URI tmp = new URI(fid, true);
                boolean hasSubdir = false;
                int fileCount = 0;
                for (final MultiStatusResponse multiStatusResponse : multiStatus.getResponses()) {
                    /*
                     * Get the DAV property set for 200 (OK) status
                     */
                    final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                    final String href = getHref(multiStatusResponse.getHref(), propertySet);
                    tmp.setEscapedPath(href);
                    if (uri.equals(tmp)) {
                        if (null == propertySet || propertySet.isEmpty()) {
                            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                                fid,
                                account.getId(),
                                WebDAVConstants.ID,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        /*
                         * Check for collection
                         */
                        if (!href.endsWith(SLASH)) {
                            /*
                             * Not a collection
                             */
                            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(WebDAVConstants.ID, fid);
                        }
                        /*
                         * Parse properties
                         */
                        ret.parseDavPropertySet(propertySet);
                    } else {
                        if (href.endsWith(SLASH)) {
                            /*
                             * Sub-Directory found
                             */
                            hasSubdir = true;
                        } else {
                            /*
                             * File
                             */
                            fileCount++;
                        }
                    }
                }
                ret.setFileCount(fileCount);
                ret.setSubfolders(hasSubdir);
                ret.setSubscribedSubfolders(hasSubdir);
            } finally {
                closeHttpMethod(propFindMethod);
            }
            /*
             * Perform OPTIONS to retrieve folder capabilities
             */
            final DavMethod optionsMethod = new OptionsMethod(fid);
            try {
                client.executeMethod(optionsMethod);
                optionsMethod.checkSuccess();
                final Header header = optionsMethod.getResponseHeader("Allow");
                ret.parseAllowHeader(null == header ? null : header.getValue());
            } finally {
                closeHttpMethod(optionsMethod);
            }
            /*
             * Return
             */
            return ret;
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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
    public FileStorageFolder[] getSubfolders(final String parentId, final boolean all) throws OXException {
        try {
            /*
             * Check
             */
            final String pid = checkFolderId(parentId, rootUri);
            final URI uri = new URI(pid, true);
            final List<String> subDirs;
            final DavMethod propFindMethod = new PropFindMethod(pid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                client.executeMethod(propFindMethod);
                /*
                 * Check if request was successfully executed
                 */
                propFindMethod.checkSuccess();
                /*
                 * Get MultiStatus response
                 */
                final MultiStatus multiStatus = propFindMethod.getResponseBodyAsMultiStatus();
                /*
                 * Find MultiStatus for specified folder URI
                 */
                final URI tmp = new URI(pid, true);
                final MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();
                subDirs = new ArrayList<String>(multiStatusResponses.length);
                for (final MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                    /*
                     * Get the DAV property set for 200 (OK) status
                     */
                    final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                    final String href = getHref(multiStatusResponse.getHref(), propertySet);
                    tmp.setEscapedPath(href);
                    if (uri.equals(tmp)) {
                        if (null == propertySet || propertySet.isEmpty()) {
                            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                                pid,
                                account.getId(),
                                WebDAVConstants.ID,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        /*
                         * Check for collection
                         */
                        if (!href.endsWith(SLASH)) {
                            /*
                             * Not a collection
                             */
                            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(WebDAVConstants.ID, pid);
                        }
                    } else {
                        if (href.endsWith(SLASH)) {
                            /*
                             * Sub-Directory found
                             */
                            subDirs.add(tmp.toString());
                        }
                    }
                }
            } finally {
                closeHttpMethod(propFindMethod);
            }
            /*
             * Request folders
             */
            if (subDirs.isEmpty()) {
                return new FileStorageFolder[0];
            }
            final int size = subDirs.size();
            final FileStorageFolder[] ret = new FileStorageFolder[size];
            for (int i = 0; i < size; i++) {
                ret[i] = getFolder(subDirs.get(i));
            }
            /*
             * Return
             */
            return ret;
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(rootUri);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        try {
            final URI uri = new URI(checkFolderId(toCreate.getParentId(), rootUri), true);
            final String prevPath = uri.getPath();
            uri.setPath(new StringBuilder(prevPath).append(prevPath.endsWith(SLASH) ? "" : SLASH).append(toCreate.getName()).append('/').toString());
            /*
             * Perform MkCol
             */
            final String folderUri = uri.toString();
            final DavMethod method = new MkColMethod(folderUri);
            try {
                client.executeMethod(method);
                method.checkSuccess();
                return folderUri;
            } finally {
                closeHttpMethod(method);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String updateFolder(final String folderId, final FileStorageFolder toUpdate) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUri);
            if (rootUri.equalsIgnoreCase(fid)) {
                throw FileStorageExceptionCodes.UPDATE_DENIED.create(WebDAVConstants.ID, fid);
            }
            /*
             * WebDAV does neither support permissions nor subscriptions
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
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId, String newName) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUri);
            if (rootUri.equalsIgnoreCase(fid)) {
                throw FileStorageExceptionCodes.UPDATE_DENIED.create(WebDAVConstants.ID, fid);
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
                uri.setPath(new StringBuilder(path).append('/').append(null != newName ? newName : name).toString());
                newUri = checkFolderId(uri.toString());
            }
            /*
             * Perform MOVE
             */
            final MoveMethod method = new MoveMethod(fid, newUri, true);
            try {
                client.executeMethod(method);
                method.checkSuccess();
                return newUri;
            } finally {
                closeHttpMethod(method);
            }
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUri);
            if (rootUri.equalsIgnoreCase(fid)) {
                throw FileStorageExceptionCodes.UPDATE_DENIED.create(WebDAVConstants.ID, fid);
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
             * Perform MOVE
             */
            final MoveMethod method = new MoveMethod(fid, newUri, true);
            try {
                client.executeMethod(method);
                method.checkSuccess();
                return newUri;
            } finally {
                closeHttpMethod(method);
            }
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
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
            final String fid = checkFolderId(folderId, rootUri);
            if (rootUri.equalsIgnoreCase(fid)) {
                throw FileStorageExceptionCodes.DELETE_DENIED.create(WebDAVConstants.ID, fid);
            }
            /*
             * Perform DELETE
             */
            final DavMethod method = new DeleteMethod(fid);
            try {
                client.executeMethod(method);
                method.checkSuccess();
                return fid;
            } finally {
                closeHttpMethod(method);
            }
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
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
        clearFolder0(folderId, false);
    }

    private void clearFolder0(final String folderId, final boolean deleteRoot) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid, true);
            final List<String> subDirs;
            final List<String> files;
            final DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                client.executeMethod(propFindMethod);
                /*
                 * Check if request was successfully executed
                 */
                propFindMethod.checkSuccess();
                /*
                 * Get MultiStatus response
                 */
                final MultiStatus multiStatus = propFindMethod.getResponseBodyAsMultiStatus();
                /*
                 * Find MultiStatus for specified folder URI
                 */
                final URI tmp = new URI(fid, true);
                final MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();
                subDirs = new ArrayList<String>(multiStatusResponses.length);
                files = new ArrayList<String>(multiStatusResponses.length);
                for (final MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                    /*
                     * Get the DAV property set for 200 (OK) status
                     */
                    final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                    final String href = getHref(multiStatusResponse.getHref(), propertySet);
                    tmp.setEscapedPath(href);
                    if (uri.equals(tmp)) {
                        if (null == propertySet || propertySet.isEmpty()) {
                            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                                fid,
                                account.getId(),
                                WebDAVConstants.ID,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        /*
                         * Check for collection
                         */
                        if (!href.endsWith(SLASH)) {
                            /*
                             * Not a collection
                             */
                            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(WebDAVConstants.ID, fid);
                        }
                    } else {
                        /*
                         * Check for collection
                         */
                        if (href.endsWith(SLASH)) {
                            /*
                             * Sub-Directory found
                             */
                            subDirs.add(tmp.toString());
                        } else {
                            /*
                             * File
                             */
                            files.add(tmp.toString());
                        }
                    }
                }
            } finally {
                closeHttpMethod(propFindMethod);
            }
            /*
             * Process
             */
            if (!subDirs.isEmpty()) {
                for (final String subDirURI : subDirs) {
                    clearFolder0(subDirURI, true);
                }
            }
            if (!files.isEmpty()) {
                for (final String fileURI : files) {
                    /*
                     * Perform DELETE
                     */
                    final DavMethod method = new DeleteMethod(fileURI);
                    try {
                        client.executeMethod(method);
                        method.checkSuccess();
                    } finally {
                        closeHttpMethod(method);
                    }
                }
            }
            if (deleteRoot) {
                try {
                    /*
                     * Perform DELETE
                     */
                    final DavMethod method = new DeleteMethod(fid);
                    try {
                        client.executeMethod(method);
                        method.checkSuccess();
                    } finally {
                        closeHttpMethod(method);
                    }
                } catch (final HttpException e) {
                    throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
                } catch (final IOException e) {
                    throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                } catch (final Exception e) {
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        } catch (final OXException e) {
            throw e;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        final List<FileStorageFolder> list = new ArrayList<FileStorageFolder>();
        final String fid = checkFolderId(folderId, rootUri);
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
