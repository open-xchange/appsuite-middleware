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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.w3c.dom.Element;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageException;
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
 */
public final class WebDAVFileStorageFolderAccess extends AbstractWebDAVAccess implements FileStorageFolderAccess {

    private final String rootUri;

    /**
     * Initializes a new {@link WebDAVFileStorageFolderAccess}.
     */
    public WebDAVFileStorageFolderAccess(final HttpClient client, final FileStorageAccount account, final Session session) {
        super(client, account, session);
        rootUri = (String) account.getConfiguration().get(WebDAVConstants.WEBDAV_URL);
    }

    public boolean exists(final String folderId) throws FileStorageException {
        try {
            /*
             * Check
             */
            final URI uri = new URI(folderId, true);
            final DavMethod propFindMethod = new PropFindMethod(folderId, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
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
                final URI tmp = new URI(folderId, true);
                for (final MultiStatusResponse multiStatusResponse : multiStatus.getResponses()) {
                    tmp.setEscapedPath(multiStatusResponse.getHref());
                    if (uri.equals(tmp)) {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null != propertySet && !propertySet.isEmpty()) {
                            /*
                             * Check for collection
                             */
                            @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                                (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                            final Element resourceType = davProperty.getValue();
                            if (null == resourceType || !"collection".equalsIgnoreCase(resourceType.getLocalName())) {
                                /*
                                 * Not a directory
                                 */
                                throw WebDAVFileStorageExceptionCodes.NOT_A_FOLDER.create(folderId);
                            }
                            /*
                             * Found directory
                             */
                            return true;
                        }
                    }
                }
                return false;
            } finally {
                propFindMethod.releaseConnection();
            }
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw WebDAVFileStorageExceptionCodes.DAV_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public FileStorageFolder getFolder(final String folderId) throws FileStorageException {
        try {
            /*
             * Check
             */
            final URI uri = new URI(folderId, true);
            final WebDAVFileStorageFolder ret = new WebDAVFileStorageFolder(folderId, rootUri);
            final DavMethod propFindMethod = new PropFindMethod(folderId, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
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
                final URI tmp = new URI(folderId, true);
                boolean hasSubdir = false;
                int fileCount = 0;
                for (final MultiStatusResponse multiStatusResponse : multiStatus.getResponses()) {
                    tmp.setEscapedPath(multiStatusResponse.getHref());
                    if (uri.equals(tmp)) {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null == propertySet || propertySet.isEmpty()) {
                            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                                folderId,
                                account.getId(),
                                WebDAVConstants.ID,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        /*
                         * Check for collection
                         */
                        @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                            (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                        final Element resourceType = davProperty.getValue();
                        if (null == resourceType || !"collection".equalsIgnoreCase(resourceType.getLocalName())) {
                            /*
                             * Not a collection
                             */
                            throw WebDAVFileStorageExceptionCodes.NOT_A_FOLDER.create(folderId);
                        }
                        /*
                         * Parse properties
                         */
                        ret.parseDavPropertySet(propertySet);
                    } else {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null != propertySet && !propertySet.isEmpty()) {
                            /*
                             * Check for collection
                             */
                            @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                                (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                            final Element resourceType = davProperty.getValue();
                            if (null != resourceType && "collection".equalsIgnoreCase(resourceType.getLocalName())) {
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
                }
                ret.setFileCount(fileCount);
                ret.setSubfolders(hasSubdir);
                ret.setSubscribedSubfolders(hasSubdir);
            } finally {
                propFindMethod.releaseConnection();
            }
            /*
             * Perform OPTIONS to retrieve folder capabilities
             */
            final DavMethod optionsMethod = new OptionsMethod(folderId);
            try {
                client.executeMethod(optionsMethod);
                optionsMethod.checkSuccess();
                final Header header = optionsMethod.getResponseHeader("Allow");
                ret.parseAllowHeader(null == header ? null : header.getValue());
            } finally {
                optionsMethod.releaseConnection();
            }
            /*
             * Return
             */
            return ret;
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw WebDAVFileStorageExceptionCodes.DAV_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws FileStorageException {
        try {
            /*
             * Check
             */
            final URI uri = new URI(parentIdentifier, true);
            final List<String> subDirs;
            final DavMethod propFindMethod = new PropFindMethod(parentIdentifier, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
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
                final URI tmp = new URI(parentIdentifier, true);
                final MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();
                subDirs = new ArrayList<String>(multiStatusResponses.length);
                for (final MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                    tmp.setEscapedPath(multiStatusResponse.getHref());
                    if (uri.equals(tmp)) {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null == propertySet || propertySet.isEmpty()) {
                            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                                parentIdentifier,
                                account.getId(),
                                WebDAVConstants.ID,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        /*
                         * Check for collection
                         */
                        @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                            (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                        final Element resourceType = davProperty.getValue();
                        if (null == resourceType || !"collection".equalsIgnoreCase(resourceType.getLocalName())) {
                            /*
                             * Not a collection
                             */
                            throw WebDAVFileStorageExceptionCodes.NOT_A_FOLDER.create(parentIdentifier);
                        }
                    } else {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null != propertySet && !propertySet.isEmpty()) {
                            /*
                             * Check for collection
                             */
                            @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                                (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                            final Element resourceType = davProperty.getValue();
                            if (null != resourceType && "collection".equalsIgnoreCase(resourceType.getLocalName())) {
                                /*
                                 * Sub-Directory found
                                 */
                                subDirs.add(tmp.toString());
                            }
                        }
                    }
                }
            } finally {
                propFindMethod.releaseConnection();
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
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw WebDAVFileStorageExceptionCodes.DAV_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public FileStorageFolder getRootFolder() throws FileStorageException {
        return getFolder(rootUri);
    }

    public String createFolder(final FileStorageFolder toCreate) throws FileStorageException {
        try {
            final URI uri = new URI(toCreate.getParentId(), true);
            final String prevPath = uri.getPath();
            uri.setPath(new StringBuilder(prevPath).append('/').append(toCreate.getName()).toString());
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
                method.releaseConnection();
            }
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public String updateFolder(final String identifier, final FileStorageFolder toUpdate) throws FileStorageException {
        // TODO PropPatch
        return null;
    }

    public String moveFolder(final String folderId, final String newParentId) throws FileStorageException {
        // TODO Move
        return null;
    }

    public String renameFolder(final String folderId, final String newName) throws FileStorageException {
        // TODO PropPatch auf displayname oder Move
        return null;
    }

    public String deleteFolder(final String folderId) throws FileStorageException {
        return deleteFolder(folderId, true);
    }

    public String deleteFolder(final String folderId, final boolean hardDelete) throws FileStorageException {
        try {
            if (rootUri.equalsIgnoreCase(folderId)) {
                throw WebDAVFileStorageExceptionCodes.DELETE_DENIED.create(folderId);
            }
            /*
             * Perform DELETE
             */
            final DavMethod method = new DeleteMethod(folderId);
            try {
                client.executeMethod(method);
                method.checkSuccess();
                return folderId;
            } finally {
                method.releaseConnection();
            }
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public void clearFolder(final String folderId) throws FileStorageException {
        clearFolder(folderId, true);
    }

    public void clearFolder(final String folderId, final boolean hardDelete) throws FileStorageException {
        clearFolder0(folderId, false);
    }

    private void clearFolder0(final String folderId, final boolean deleteRoot) throws FileStorageException {
        try {
            /*
             * Check
             */
            final URI uri = new URI(folderId, true);
            final List<String> subDirs;
            final List<String> files;
            final DavMethod propFindMethod = new PropFindMethod(folderId, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
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
                final URI tmp = new URI(folderId, true);
                final MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();
                subDirs = new ArrayList<String>(multiStatusResponses.length);
                files = new ArrayList<String>(multiStatusResponses.length);
                for (final MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                    tmp.setEscapedPath(multiStatusResponse.getHref());
                    if (uri.equals(tmp)) {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null == propertySet || propertySet.isEmpty()) {
                            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                                folderId,
                                account.getId(),
                                WebDAVConstants.ID,
                                Integer.valueOf(session.getUserId()),
                                Integer.valueOf(session.getContextId()));
                        }
                        /*
                         * Check for collection
                         */
                        @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                            (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                        final Element resourceType = davProperty.getValue();
                        if (null == resourceType || !"collection".equalsIgnoreCase(resourceType.getLocalName())) {
                            /*
                             * Not a collection
                             */
                            throw WebDAVFileStorageExceptionCodes.NOT_A_FOLDER.create(folderId);
                        }
                    } else {
                        /*
                         * Get for 200 (OK) status
                         */
                        final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                        if (null != propertySet && !propertySet.isEmpty()) {
                            /*
                             * Check for collection
                             */
                            @SuppressWarnings("unchecked") final DavProperty<Element> davProperty =
                                (DavProperty<Element>) propertySet.get(DavConstants.PROPERTY_RESOURCETYPE);
                            final Element resourceType = davProperty.getValue();
                            if (null != resourceType && "collection".equalsIgnoreCase(resourceType.getLocalName())) {
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
                }
            } finally {
                propFindMethod.releaseConnection();
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
                    try {
                        /*
                         * Perform DELETE
                         */
                        final DavMethod method = new DeleteMethod(fileURI);
                        try {
                            client.executeMethod(method);
                            method.checkSuccess();
                        } finally {
                            method.releaseConnection();
                        }
                    } catch (final HttpException e) {
                        throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
                    } catch (final IOException e) {
                        throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                    } catch (final Exception e) {
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                    
                }
            }
            if (deleteRoot) {
                try {
                    /*
                     * Perform DELETE
                     */
                    final DavMethod method = new DeleteMethod(folderId);
                    try {
                        client.executeMethod(method);
                        method.checkSuccess();
                    } finally {
                        method.releaseConnection();
                    }
                } catch (final HttpException e) {
                    throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
                } catch (final IOException e) {
                    throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                } catch (final Exception e) {
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw WebDAVFileStorageExceptionCodes.DAV_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quota getStorageQuota(final String folderId) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quota getFileQuota(final String folderId) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quota[] getQuotas(final String folder, final Type[] types) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    private DavPropertySet getPropertySetFor(final String folderId) throws FileStorageException {
        try {
            final DavMethod propFindMethod = new PropFindMethod(folderId, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                client.executeMethod(propFindMethod);
                /*
                 * Check if request was successfully executed
                 */
                propFindMethod.checkSuccess();
                /*
                 * Get multistatus response
                 */
                final MultiStatus multiStatus = propFindMethod.getResponseBodyAsMultiStatus();
                final MultiStatusResponse multiStatusResponse = multiStatus.getResponses()[0];
                /*
                 * Get for 200 (OK) status
                 */
                final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                if (null == propertySet || propertySet.isEmpty()) {
                    throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                        folderId,
                        account.getId(),
                        WebDAVConstants.ID,
                        session.getUserId(),
                        session.getContextId());
                }
                /*
                 * Check for collection
                 */
                return propertySet;
            } finally {
                propFindMethod.releaseConnection();
            }
        } catch (final HttpException e) {
            throw WebDAVFileStorageExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw WebDAVFileStorageExceptionCodes.DAV_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
