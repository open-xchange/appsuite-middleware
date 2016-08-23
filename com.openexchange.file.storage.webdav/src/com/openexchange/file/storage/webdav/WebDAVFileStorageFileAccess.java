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
import static com.openexchange.file.storage.webdav.WebDAVFileStorageResourceUtil.parseIntProperty;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.CopyMethod;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.LockMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.client.methods.UnLockMethod;
import org.apache.jackrabbit.webdav.header.CodedUrlHeader;
import org.apache.jackrabbit.webdav.header.IfHeader;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.transaction.TransactionConstants;
import org.apache.jackrabbit.webdav.transaction.TransactionInfo;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAdvancedSearchFileAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.search.FieldCollectorVisitor;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.webdav.workarounds.LiberalUnLockMethod;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tx.TransactionException;

/**
 * {@link WebDAVFileStorageFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> - Exceptions
 */
public final class WebDAVFileStorageFileAccess extends AbstractWebDAVAccess implements FileStorageLockedFileAccess, FileStorageAdvancedSearchFileAccess {

    private static final class LockTokenKey {

        private final String folderId;

        private final String id;

        public LockTokenKey(final String folderId, final String id) {
            super();
            this.folderId = folderId;
            this.id = id;
        }

        public String getFolderId() {
            return folderId;
        }

        public String getId() {
            return id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LockTokenKey)) {
                return false;
            }
            final LockTokenKey other = (LockTokenKey) obj;
            if (folderId == null) {
                if (other.folderId != null) {
                    return false;
                }
            } else if (!folderId.equals(other.folderId)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

    }

    private final Map<LockTokenKey, String> lockTokenMap;

    private final String rootUri;

    private final WebDAVFileStorageAccountAccess accountAccess;

    private volatile String transactionToken;

    /**
     * Initializes a new {@link WebDAVFileStorageFileAccess}.
     */
    public WebDAVFileStorageFileAccess(final HttpClient client, final FileStorageAccount account, final WebDAVFileStorageAccountAccess accountAccess, final Session session) {
        super(client, account, session);
        rootUri = checkFolderId(((String) account.getConfiguration().get(WebDAVConstants.WEBDAV_URL)).trim());
        this.accountAccess = accountAccess;
        lockTokenMap = new ConcurrentHashMap<LockTokenKey, String>();
    }

    /**
     * Gets the root URI.
     *
     * @return The root URI
     */
    public String getRootUri() {
        return rootUri;
    }

    /**
     * Clean up.
     */
    public void cleanUp() {
        /*
         * Remove all pending locks
         */
        for (final Entry<LockTokenKey, String> entry : lockTokenMap.entrySet()) {
            try {
                unlock0(entry.getKey(), entry.getValue());
            } catch (final OXException e) {
                LoggerFactory.getLogger(WebDAVFileStorageFileAccess.class).error("", e);
            }
        }
    }

    private void initMethod(final String folderId, final String id, final DavMethod davMethod) {
        initMethod(folderId, id, davMethod, !isLockMethod(davMethod));
    }

    private void initMethod(final String folderId, final String id, final DavMethod davMethod, final boolean addIfHeader) {
        if (addIfHeader) {
            // http://blog.ropardo.ro/getting-the-lock-token-for-an-item-in-jackrabbit/
            // http://www.koders.com/java/fid411ED72BF4F6D245D33A7E6A4F48127AF190657A.aspx?s=mdef:getUserId
            final String thisTransactionToken = transactionToken;
            if (null != thisTransactionToken) {
                CodedUrlHeader codedUrl = new CodedUrlHeader("Transaction", thisTransactionToken);
                davMethod.setRequestHeader(codedUrl);
                codedUrl = new CodedUrlHeader(TransactionConstants.HEADER_TRANSACTIONID, thisTransactionToken);
                davMethod.setRequestHeader(codedUrl);
            }
            if (null != folderId && null != id) {
                // Lock token from former exclusive lock for a certain item
                final String lockToken = lockTokenMap.get(new LockTokenKey(folderId, id));
                if (null != lockToken) {
                    final IfHeader ifH = new IfHeader(new String[] { lockToken });
                    davMethod.setRequestHeader(ifH.getHeaderName(), ifH.getHeaderValue());
                }
            }
        }
    }

    private static boolean isLockMethod(final DavMethod method) {
        final int code = DavMethods.getMethodCode(method.getName());
        return DavMethods.DAV_LOCK == code || DavMethods.DAV_UNLOCK == code;
    }

    @Override
    public void startTransaction() throws OXException {
        if (null != transactionToken) {
            /*
             * Transaction already started
             */
            return;
        }
        try {
            final URI uri = new URI(rootUri, true);
            /*
             * Create proper LockInfo
             */
            // final LockInfo lockInfo = new LockInfo(DavConstants.INFINITE_TIMEOUT);
            // lockInfo.setType(Type.create(TransactionConstants.XML_TRANSACTION, DavConstants.NAMESPACE)); // or
            // lockInfo.setType(TransactionConstants.TRANSACTION); with different namespace "dcr" instead of "d"
            // lockInfo.setScope(Scope.create(TransactionConstants.XML_GLOBAL, DavConstants.NAMESPACE)); // or
            // lockInfo.setScope(TransactionConstants.GLOBAL); with different namespace "dcr" instead of "d"
            // final LockMethod lockMethod = new LockMethod(uri.toString(), lockInfo);

            final LockMethod lockMethod =
                new LockMethod(
                    uri.toString(),
                    TransactionConstants.LOCAL,
                    TransactionConstants.TRANSACTION,
                    null,
                    DavConstants.INFINITE_TIMEOUT,
                    true);
            try {
                client.executeMethod(lockMethod);
                /*
                 * Check if request was successfully executed
                 */
                lockMethod.checkSuccess();
                /*
                 * Obtain & remember lock token
                 */
                final String lockToken = lockMethod.getLockToken();
                transactionToken = lockToken;
            } finally {
                closeHttpMethod(lockMethod);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        }
    }

    @Override
    public void commit() throws OXException {
        if (null == transactionToken) {
            /*
             * Transaction not started
             */
            return;
        }
        try {
            final URI uri = new URI(rootUri, true);
            final UnLockMethod method = new LiberalUnLockMethod(uri.toString(), transactionToken);
            try {
                method.setRequestBody(new TransactionInfo(true)); // COMMIT
                client.executeMethod(method);
                /*
                 * Check if request was successfully executed
                 */
                method.checkSuccess();
                transactionToken = null;
            } finally {
                closeHttpMethod(method);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        }
    }

    @Override
    public void rollback() throws OXException {
        if (null == transactionToken) {
            /*
             * Transaction not started
             */
            return;
        }
        try {
            final URI uri = new URI(rootUri, true);
            final UnLockMethod method = new LiberalUnLockMethod(uri.toString(), transactionToken);
            try {
                method.setRequestBody(new TransactionInfo(false)); // ROLL-BACK
                client.executeMethod(method);
                /*
                 * Check if request was successfully executed
                 */
                method.checkSuccess();
                transactionToken = null;
            } finally {
                closeHttpMethod(method);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        }
    }

    @Override
    public void finish() throws TransactionException {
        /*
         * Nope
         */
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nothing to do

    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nothing to do

    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nope
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFile, final List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(WebDAVConstants.ID);
        }
        try {
            final String fid = checkFolderId(source.getFolder(), rootUri);
            final String id = source.getId();
            final String dfid = checkFolderId(destFolder, rootUri);
            final IDTuple ret = new IDTuple();
            ret.setFolder(dfid);
            /*
             * Perform COPY
             */
            final CopyMethod copyMethod = new CopyMethod(new URI(fid + id, true).toString(), new URI(dfid + id, true).toString(), false);
            try {
                initMethod(fid, id, copyMethod);
                client.executeMethod(copyMethod);
                copyMethod.checkSuccess();
                ret.setId(id);
            } finally {
                closeHttpMethod(copyMethod);
            }
            if (null != update) {
                /*
                 * Apply update to copied file
                 */
                update.setFolderId(dfid);
                update.setId(id);
                saveDocument(update, newFile, DISTANT_FUTURE, modifiedFields);
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
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        try {
            final String fid = checkFolderId(source.getFolder(), rootUri);
            final String id = source.getId();
            final String dfid = checkFolderId(destFolder, rootUri);
            String did = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : id;
            /*
             * Perform MOVE
             */
            final MoveMethod moveMethod = new MoveMethod(new URI(fid + id, true).toString(), new URI(dfid + did, true).toString(), false);
            try {
                initMethod(fid, id, moveMethod);
                client.executeMethod(moveMethod);
                moveMethod.checkSuccess();
            } finally {
                closeHttpMethod(moveMethod);
            }
            /*
             * Return
             */
            return new IDTuple(dfid, did);
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
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid + id, true);
            final DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                initMethod(fid, id, propFindMethod);
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
                        if (href.endsWith(SLASH)) {
                            /*
                             * Not a file
                             */
                            throw FileStorageExceptionCodes.NOT_A_FILE.create(WebDAVConstants.ID, id);
                        }
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
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        if (version != CURRENT_VERSION) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(WebDAVConstants.ID);
        }
        try {
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid + id, true);
            final DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                initMethod(fid, id, propFindMethod);
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
                         * Check for file
                         */
                        if (href.endsWith(SLASH)) {
                            /*
                             * Not a file
                             */
                            throw FileStorageExceptionCodes.NOT_A_FILE.create(WebDAVConstants.ID, id);
                        }
                        /*
                         * Found file
                         */
                        return new WebDAVFileStorageFile(fid, id, session.getUserId(), rootUri).parseDavPropertySet(propertySet);
                    }
                }
                /*
                 * File not found
                 */
                throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, fid);
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
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        return saveFileMetadata0(file, null);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        return saveFileMetadata0(file, modifiedFields);
    }

    private IDTuple saveFileMetadata0(final File file, final List<Field> modifiedFields) throws OXException {
        try {
            final String folderId = checkFolderId(file.getFolderId(), rootUri);
            final String id;
            {
                final String fid = file.getId();
                if (null == fid) {
                    final String name = file.getFileName();
                    if (null == name) {
                        throw FileStorageExceptionCodes.MISSING_FILE_NAME.create();
                    }
                    id = name;
                    file.setId(id);
                } else {
                    id = fid;
                }
            }
            final URI uri = new URI(folderId + id, true);
            /*
             * Convert file to DAV representation
             */
            final WebDAVFileStorageDavRepr davRepr = new WebDAVFileStorageDavRepr(file, modifiedFields);
            /*
             * Check existence
             */
            if (!exists(folderId, id, CURRENT_VERSION)) {
                lock(folderId, id, FileStorageFileAccess.DISTANT_FUTURE);
                try {
                    /*
                     * Perform PUT first to create the resource
                     */
                    final PutMethod putMethod = new PutMethod(uri.toString());
                    try {
                        initMethod(folderId, id, putMethod);
                        client.executeMethod(putMethod);
                        /*
                         * Check if request was successfully executed
                         */
                        putMethod.checkSuccess();
                    } finally {
                        closeHttpMethod(putMethod);
                    }
                } finally {
                    unlock(folderId, id);
                }
            }
            /*
             * Perform PropPatch
             */
            final DavMethod davMethod = new PropPatchMethod(uri.toString(), davRepr.getSetProperties(), davRepr.getRemoveProperties());
            try {
                initMethod(folderId, id, davMethod);
                client.executeMethod(davMethod);
                /*
                 * Check if request was successfully executed
                 */
                davMethod.checkSuccess();
            } finally {
                closeHttpMethod(davMethod);
            }
            return new IDTuple(folderId, id);
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            if (e instanceof OXException) {
                throw (OXException) e;
            }
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        final String fid = checkFolderId(folderId, rootUri);
        final URI uri;
        try {
            uri = new URI(fid + id, true);
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        try {
            long size = -1;

            DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                initMethod(fid, id, propFindMethod);
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
                URI tmp = new URI(fid, true);
                MultiStatusResponse[] responses = multiStatus.getResponses();
                boolean found = false;
                for (int i = 0; i < responses.length && !found; i++) {
                    final MultiStatusResponse multiStatusResponse = responses[i];
                    /*
                     * Get the DAV property set for 200 (OK) status
                     */
                    final DavPropertySet propertySet = multiStatusResponse.getProperties(HttpServletResponse.SC_OK);
                    final String href = getHref(multiStatusResponse.getHref(), propertySet);
                    tmp.setEscapedPath(href);
                    if (uri.equals(tmp)) {
                        /*
                         * Check for file
                         */
                        if (href.endsWith(SLASH)) {
                            /*
                             * Not a file
                             */
                            throw FileStorageExceptionCodes.NOT_A_FILE.create(WebDAVConstants.ID, id);
                        }
                        /*
                         * Found file
                         */
                        size = parseIntProperty(DavConstants.PROPERTY_GETCONTENTLENGTH, propertySet);
                        found = true;
                    }
                }
            } finally {
                closeHttpMethod(propFindMethod);
                propFindMethod = null;
            }

            GetMethod getMethod = new GetMethod(uri.toString());
            try {
                // initMethod(folderId, id, getMethod);
                client.executeMethod(getMethod);
                /*
                 * Check if request was successfully executed
                 */
                if (HttpServletResponse.SC_NOT_FOUND == getMethod.getStatusCode()) {
                    throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(fid, id);
                }
                if (HttpServletResponse.SC_OK != getMethod.getStatusCode()) {
                    throw FileStorageExceptionCodes.PROTOCOL_ERROR.create("HTTP", getMethod.getStatusText());
                }
                /*
                 * Return as stream
                 */
                HttpMethodInputStream stream = new HttpMethodInputStream(getMethod);
                getMethod = null;
                return new SizeKnowingInputStream(stream, size);
            } finally {
                closeHttpMethod(getMethod);
            }
        } catch (HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        return saveDocument0(file, data, null);
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        return saveDocument0(file, data, modifiedFields);
    }

    private IDTuple saveDocument0(final File file, final InputStream data, final List<Field> modifiedColumns) throws OXException {
        try {
            /*
             * Save metadata
             */
            IDTuple result = saveFileMetadata0(file, modifiedColumns);
            /*
             * Save content
             */
            if (null != data) {
                try {
                    final String folderId = checkFolderId(file.getFolderId(), rootUri);
                    final String id = file.getId();
                    final PutMethod putMethod = new PutMethod(new URI(folderId + id, true).toString());
                    putMethod.setRequestEntity(new InputStreamRequestEntity(data));
                    try {
                        initMethod(folderId, id, putMethod);
                        client.executeMethod(putMethod);
                        /*
                         * Check if request was successfully executed
                         */
                        putMethod.checkSuccess();
                    } finally {
                        closeHttpMethod(putMethod);
                    }
                } finally {
                    /*
                     * Close given stream
                     */
                    Streams.close(data);
                }
            }
            return result;
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
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        accountAccess.getFolderAccess().clearFolder(checkFolderId(folderId, rootUri));
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber, boolean hardDelete) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (final IDTuple idTuple : ids) {
                final String folderId = checkFolderId(idTuple.getFolder(), rootUri);
                final String id = idTuple.getId();
                final URI uri = new URI(folderId + id, true);
                final DeleteMethod deleteMethod = new DeleteMethod(uri.toString());
                try {
                    initMethod(folderId, id, deleteMethod);
                    client.executeMethod(deleteMethod);
                    /*
                     * Check if request was successfully executed
                     */
                    final int statusCode = deleteMethod.getStatusCode();
                    if (HttpServletResponse.SC_NOT_FOUND == statusCode) {
                        /*
                         * No-op for us...
                         */
                    } else if (HttpServletResponse.SC_OK != statusCode && HttpServletResponse.SC_NO_CONTENT != statusCode) {
                        ret.add(idTuple);
                    }
                } finally {
                    closeHttpMethod(deleteMethod);
                }
            }
            return ret;
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void unlock(final String folderId, final String id) throws OXException {
        final String fid = checkFolderId(folderId, rootUri);
        final LockTokenKey lockTokenKey = new LockTokenKey(fid, id);
        final String lockToken = lockTokenMap.get(lockTokenKey);
        unlock0(lockTokenKey, lockToken);
        lockTokenMap.remove(lockTokenKey);
    }

    private void unlock0(final LockTokenKey lockTokenKey, final String lockToken) throws OXException {
        try {
            final String folderId = lockTokenKey.getFolderId();
            final String id = lockTokenKey.getId();
            final URI uri = new URI(folderId + id, true);
            final UnLockMethod unlockMethod = new LiberalUnLockMethod(uri.toString(), lockToken);
            try {
                initMethod(folderId, id, unlockMethod);
                client.executeMethod(unlockMethod);
                /*
                 * Check if request was successfully executed
                 */
                unlockMethod.checkSuccess();
            } finally {
                closeHttpMethod(unlockMethod);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        }
    }

    @Override
    public void lock(final String folderId, final String id, final long diff) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid + id, true);
            final LockMethod lockMethod =
                new LockMethod(
                    uri.toString(),
                    Scope.EXCLUSIVE,
                    Type.WRITE,
                    null /* accountAccess.getUser() */,
                    DavConstants.INFINITE_TIMEOUT,
                    true);
            try {
                initMethod(fid, id, lockMethod);
                client.executeMethod(lockMethod);
                /*
                 * Check if request was successfully executed
                 */
                lockMethod.checkSuccess();
                /*
                 * Obtain & remember lock token
                 */
                final String lockToken = lockMethod.getLockToken();
                lockTokenMap.put(new LockTokenKey(fid, id), lockToken);
            } finally {
                closeHttpMethod(lockMethod);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        }
    }

    @Override
    public void touch(final String folderId, final String id) throws OXException {
        /*
         * Update last-modified time stamp through a PropPatch on a dummy property
         */
        try {
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid + id, true);
            /*
             * Perform PropPatch
             */
            final DavPropertySet setProperties = new DavPropertySet();
            setProperties.add(new DefaultDavProperty<String>(DavPropertyName.create("dummy", WebDAVConstants.OX_NAMESPACE), Long.toString(System.currentTimeMillis())));
            final DavMethod davMethod = new PropPatchMethod(uri.toString(), setProperties, new DavPropertyNameSet());
            try {
                initMethod(folderId, id, davMethod);
                client.executeMethod(davMethod);
                /*
                 * Check if request was successfully executed
                 */
                davMethod.checkSuccess();
            } finally {
                closeHttpMethod(davMethod);
            }
        } catch (final HttpException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final DavException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            if (e instanceof OXException) {
                throw (OXException) e;
            }
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        return new FileTimedResult(getFileList(folderId, null));
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return new FileTimedResult(getFileList(folderId, fields));
    }

    /**
     * Gets the file listing for specified folder
     *
     * @param folderId The folder identifier
     * @param fields The fields to fill
     * @return The file listing
     * @throws OXException If listing fails
     */
    public List<File> getFileList(final String folderId, final List<Field> fields) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUri);
            final URI uri = new URI(fid, true);
            final List<File> files;
            final DavMethod propFindMethod = new PropFindMethod(fid, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                initMethod(fid, null, propFindMethod);
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
                files = new ArrayList<File>(multiStatusResponses.length);
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
                         * Check for file
                         */
                        if (!href.endsWith(SLASH)) {
                            /*
                             * File
                             */
                            files.add(new WebDAVFileStorageFile(fid, extractFileName(href), session.getUserId(), rootUri).parseDavPropertySet(propertySet, fields));
                        }
                    }
                }
            } finally {
                closeHttpMethod(propFindMethod);
            }
            return files;
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
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final List<File> files = getFileList(folderId, fields);
        /*
         * Sort list
         */
        Collections.sort(files, order.comparatorBy(sort));
        /*
         * Return sorted result
         */
        return new FileTimedResult(files);
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        final List<File> list = new ArrayList<File>(ids.size());
        for (final IDTuple idTuple : ids) {
            list.add(getFileMetadata(idTuple.getFolder(), idTuple.getId(), CURRENT_VERSION));
        }
        return new FileTimedResult(list);
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final Field sort, final SortDirection order, final boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(List<String> folderIds, final SearchTerm<?> searchTerm, final List<Field> fields, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final List<File> results;
        {
            final FieldCollectorVisitor fieldCollector = new FieldCollectorVisitor(Field.ID, Field.FOLDER_ID);
            searchTerm.visit(fieldCollector);

            final List<Field> fieldz = new ArrayList<Field>(fields);
            fieldz.addAll(fieldCollector.getFields());

            final WebDAVSearchVisitor visitor = new WebDAVSearchVisitor(fieldz, this);
            searchTerm.visit(visitor);
            results = visitor.getResults();
        }
        return getSortedRangeFrom(results, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String folderId, boolean includeSubfolders, final SearchTerm<?> searchTerm, final List<Field> fields, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final List<File> results;
        {
            final FieldCollectorVisitor fieldCollector = new FieldCollectorVisitor(Field.ID, Field.FOLDER_ID);
            searchTerm.visit(fieldCollector);

            final List<Field> fieldz = new ArrayList<Field>(fields);
            fieldz.addAll(fieldCollector.getFields());

            final WebDAVSearchVisitor visitor = new WebDAVSearchVisitor(fieldz, this);
            searchTerm.visit(visitor);
            results = visitor.getResults();
        }
        return getSortedRangeFrom(results, sort, order, start, end);
    }

    /**
     * Recursively searches using given term.
     *
     * @param term The term
     * @param folderId The folder identifier to start with
     * @param fields The field to fill
     * @param results The result
     * @throws OXException If search fails
     */
    public void recursiveSearchFile(final SearchTerm<?> term, final String folderId, final List<Field> fields, final List<File> results) throws OXException {
        try {
            /*
             * Check
             */
            final URI uri = new URI(folderId, true);
            final DavMethod propFindMethod = new PropFindMethod(folderId, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                initMethod(folderId, null, propFindMethod);
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
                                folderId,
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
                            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(WebDAVConstants.ID, folderId);
                        }
                    } else {
                        /*
                         * Check for collection
                         */
                        if (href.endsWith(SLASH)) {
                            /*
                             * A directory
                             */
                            recursiveSearchFile(term, tmp.toString(), fields, results);
                        } else {
                            /*
                             * File
                             */
                            final WebDAVFileStorageFile davFile = new WebDAVFileStorageFile(folderId, extractFileName(href), session.getUserId(), rootUri).parseDavPropertySet(propertySet, fields);
                            if (term.matches(davFile)) {
                                results.add(davFile);
                            }
                        }
                    }
                }
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
            if (HttpServletResponse.SC_NOT_FOUND == e.getErrorCode()) {
                throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    folderId,
                    account.getId(),
                    WebDAVConstants.ID,
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        final List<File> results;
        if (ALL_FOLDERS == folderId) {
            /*
             * Recursively search files in directories
             */
            results = new ArrayList<File>();
            recursiveSearchFile(pattern, rootUri, fields, results);
        } else if (includeSubfolders) {
            results = new ArrayList<File>();
            recursiveSearchFile(pattern, folderId, fields, results);
        } else {
            /*
             * Get files from folder
             */
            results = getFileList(folderId, fields);
            /*
             * Filter by search pattern
             */
            for (final Iterator<File> iterator = results.iterator(); iterator.hasNext();) {
                final File file = iterator.next();
                if (!file.matches(pattern)) {
                    iterator.remove();
                }
            }
        }
        return getSortedRangeFrom(results, sort, order, start, end);
    }

    private SearchIterator<File> getSortedRangeFrom(final List<File> results, final Field sort, final SortDirection order, final int start, final int end) {
        /*
         * Empty?
         */
        if (results.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * Sort
         */
        Collections.sort(results, order.comparatorBy(sort));
        /*
         * Consider start/end index
         */
        if (start != NOT_SET && end != NOT_SET && end > start) {
            final int fromIndex = start;
            int toIndex = end;
            if ((fromIndex) > results.size()) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return SearchIteratorAdapter.emptyIterator();
            }
            /*
             * Reset end index if out of range
             */
            if (toIndex >= results.size()) {
                toIndex = results.size();
            }
            /*
             * Return
             */
            final List<File> subList = results.subList(fromIndex, toIndex);
            return new SearchIteratorAdapter<File>(subList.iterator(), subList.size());
        }
        /*
         * Return sorted result
         */
        return new SearchIteratorAdapter<File>(results.iterator(), results.size());
    }

    private void recursiveSearchFile(final String pattern, final String folderId, final List<Field> fields, final List<File> results) throws OXException {
        try {
            /*
             * Check
             */
            final URI uri = new URI(folderId, true);
            final DavMethod propFindMethod = new PropFindMethod(folderId, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            try {
                initMethod(folderId, null, propFindMethod);
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
                                folderId,
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
                            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(WebDAVConstants.ID, folderId);
                        }
                    } else {
                        /*
                         * Check for collection
                         */
                        if (href.endsWith(SLASH)) {
                            /*
                             * A directory
                             */
                            recursiveSearchFile(pattern, tmp.toString(), fields, results);
                        } else {
                            /*
                             * File
                             */
                            final WebDAVFileStorageFile davFile = new WebDAVFileStorageFile(folderId, extractFileName(href), session.getUserId(), rootUri).parseDavPropertySet(propertySet, fields);
                            if (davFile.matches(pattern)) {
                                results.add(davFile);
                            }
                        }
                    }
                }
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
            if (HttpServletResponse.SC_NOT_FOUND == e.getErrorCode()) {
                throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    folderId,
                    account.getId(),
                    WebDAVConstants.ID,
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "DAV", e.getMessage());
        } catch (final Exception e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    private static String extractFileName(final String href) {
        final int pos = href.lastIndexOf('/');
        return pos > 0 ? href.substring(pos + 1) : href;
    }

}
