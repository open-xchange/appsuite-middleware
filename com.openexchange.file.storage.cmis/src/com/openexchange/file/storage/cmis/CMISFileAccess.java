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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.codec.binary.Base64InputStream;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionException;
import com.openexchange.user.UserService;

/**
 * {@link CMISFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CMISFileAccess extends AbstractCMISAccess implements FileStorageFileAccess {

    private final CMISAccountAccess accountAccess;

    /**
     * Initializes a new {@link CMISFileAccess}.
     */
    public CMISFileAccess(final String rootUrl, final org.apache.chemistry.opencmis.client.api.Session cmisSession, final FileStorageAccount account, final Session session, final CMISAccountAccess accountAccess) {
        super(rootUrl, cmisSession, account, session);
        this.accountAccess = accountAccess;
    }

    @Override
    public void startTransaction() throws TransactionException {
        // CMIS does not support transactions
    }

    @Override
    public void commit() throws TransactionException {
        // CMIS does not support transactions
    }

    @Override
    public void rollback() throws TransactionException {
        // CMIS does not support transactions
    }

    @Override
    public void finish() throws TransactionException {
        // CMIS does not support transactions
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // CMIS does not support transactions
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // CMIS does not support transactions
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // CMIS does not support transactions
    }

    @Override
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        try {
            /*
             * Check
             */
            if (!checkFolder(folderId)) {
                return false;
            }
            final ObjectId oid = cmisSession.createObjectId(id);
            final CmisObject cdocument = cmisSession.getObject(oid);
            if (null == cdocument) {
                return false;
            }
            if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(cdocument.getType().getId())) {
                return false;
            }
            final Document document = (Document) cdocument;
            document.getProperty(PropertyIds.OBJECT_ID);
            return true;
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private boolean checkFolder(final String folderId) {
        final ObjectId folderObjectId;
        final CmisObject object;
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
            object = cmisSession.getRootFolder();
            folderObjectId = cmisSession.createObjectId(object.getId());
        } else {
            folderObjectId = cmisSession.createObjectId(folderId);
            object = cmisSession.getObject(folderObjectId);
        }
        if (null == object) {
            return false;
        }
        if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
            return false;
        }
        return true;
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        if (version != CURRENT_VERSION) {
            throw CMISExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }
        try {
            /*
             * Check
             */
            if (!checkFolder(folderId)) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            final ObjectId oid = cmisSession.createObjectId(id);
            final CmisObject cdocument = cmisSession.getObject(oid);
            if (null == cdocument) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(cdocument.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FILE.create(folderId);
            }
            final Document document = (Document) cdocument;
            /*
             * Start conversion
             */
            return new CMISFile(folderId, id, session.getUserId()).parseSmbFile(document);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        createCmisFile(file, null, null);
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        createCmisFile(file, modifiedFields, null);
    }

    private Document createCmisFile(final File file, final List<Field> modifiedFields, final ContentStreamImpl contentStream) throws OXException {
        try {
            final Set<Field> set =
                null == modifiedFields || modifiedFields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(modifiedFields);
            /*
             * Check
             */
            final String folderId = file.getFolderId();
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder parent = (Folder) object;
            /*
             * Create or update
             */
            final String id = file.getId();
            final ObjectId documentId = null == id ? null : cmisSession.createObjectId(id);
            final Document document;
            {
                if (null == documentId) {
                    document = null;
                } else {
                    object = cmisSession.getObject(documentId);
                    if (null == object) {
                        throw CMISExceptionCodes.NOT_FOUND.create(id);
                    }
                    if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(object.getType().getId())) {
                        throw CMISExceptionCodes.NOT_A_FILE.create(id);
                    }
                    document = (Document) object;
                }
            }
            String name = file.getFileName();
            if (isEmpty(name)) {
                name = file.getTitle();
                if (null == name) {
                    throw CMISExceptionCodes.MISSING_FILE_NAME.create();
                }
            }
            /*
             * Properties
             */
            final Map<String, Object> properties = new HashMap<String, Object>(4);
            properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID);
            properties.put(PropertyIds.NAME, name);
            ContentStreamImpl csi = contentStream;
            if (null == csi) {
                final String description = file.getDescription();
                if (!isEmpty(description)) {
                    csi = new ContentStreamImpl(name, "text/plain; charset=\"UTF-8\"", description);
                    properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, csi.getFileName());
                    properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, csi.getMimeType());
                }
            } else {
                properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, csi.getFileName());
                properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, csi.getMimeType());
            }
            //properties.put(PropertyIds.PATH, parent.getPath());
            /*
             * Perform create or update
             */
            if (null != document) {
                document.updateProperties(properties, true);
                if (null != csi) {
                    final InputStream stream = csi.getStream();
                    if (null != stream) {
                        // Returning data as Base64 is needed for MS Sharepoint
                        csi.setStream(new Base64InputStream(stream, true));
                    }
                    document.setContentStream(csi, true, true);
                }
                // Reload & return document
                return (Document) cmisSession.getObject(documentId);
            }
            return parent.createDocument(properties, csi, VersioningState.NONE);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple copy(final IDTuple source, final String destFolder, final File update, final InputStream newFil, final List<Field> modifiedFields) throws OXException {
        InputStream stream = null;
        try {
            CmisObject object;
            /*
             * Source folder
             */
            final ObjectId sourceObjectId;
            if (FileStorageFolder.ROOT_FULLNAME.equals(source.getFolder())) {
                object = cmisSession.getRootFolder();
                sourceObjectId = cmisSession.createObjectId(object.getId());
            } else {
                sourceObjectId = cmisSession.createObjectId(source.getFolder());
                object = cmisSession.getObject(sourceObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(source.getFolder());
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(source.getFolder());
            }
            /*
             * Destination folder
             */
            final ObjectId destObjectId;
            if (FileStorageFolder.ROOT_FULLNAME.equals(source.getFolder()) || rootUrl.equals(source.getFolder())) {
                object = cmisSession.getRootFolder();
                destObjectId = cmisSession.createObjectId(object.getId());
            } else {
                destObjectId = cmisSession.createObjectId(source.getFolder());
                object = cmisSession.getObject(destObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(source.getFolder());
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(source.getFolder());
            }
            final Folder destinationFolder = (Folder) object;
            /*
             * Document
             */
            final ObjectId documentId = cmisSession.createObjectId(source.getId());
            final Document document = (Document) cmisSession.getObject(documentId);
            /*
             * Copy document
             */
            final String name = document.getName();
            /*
             * Properties
             */
            final List<Property<?>> list = document.getProperties();
            final Map<String, Object> properties = new HashMap<String, Object>(list.size());
            for (final Property<?> property : list) {
                properties.put(property.getId(), property.getValue());
            }
            /*
             * Content
             */
            final ContentStream contentStream = document.getContentStream();
            stream = contentStream.getStream();
            final ContentStream newContentStream =
                new ContentStreamImpl(name, contentStream.getBigLength(), contentStream.getMimeType(), stream);
            /*
             * Create a major version
             */
            final Document newDoc = destinationFolder.createDocument(properties, newContentStream, VersioningState.MAJOR);
            /*
             * Return
             */
            return new IDTuple(destFolder, newDoc.getId());
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(stream);
        }
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        try {
            CmisObject object;
            /*
             * Source folder
             */
            final ObjectId sourceObjectId;
            if (FileStorageFolder.ROOT_FULLNAME.equals(source.getFolder())) {
                object = cmisSession.getRootFolder();
                sourceObjectId = cmisSession.createObjectId(object.getId());
            } else {
                sourceObjectId = cmisSession.createObjectId(source.getFolder());
                object = cmisSession.getObject(sourceObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(source.getFolder());
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(source.getFolder());
            }
            /*
             * Destination folder
             */
            final ObjectId destObjectId;
            if (FileStorageFolder.ROOT_FULLNAME.equals(source.getFolder()) || rootUrl.equals(source.getFolder())) {
                object = cmisSession.getRootFolder();
                destObjectId = cmisSession.createObjectId(object.getId());
            } else {
                destObjectId = cmisSession.createObjectId(source.getFolder());
                object = cmisSession.getObject(destObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(source.getFolder());
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(source.getFolder());
            }
            final Folder destinationFolder = (Folder) object;
            /*
             * Document
             */
            final ObjectId documentId = cmisSession.createObjectId(source.getId());
            final Document document = (Document) cmisSession.getObject(documentId);
            /*
             * Move document
             */
            FileableCmisObject movedDoc = document.move(cmisSession.createObjectId(source.getFolder()), destinationFolder);
            //TODO: rename if needed?
            /*
             * Return
             */
            return new IDTuple(destFolder, movedDoc.getId());
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        try {
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * Check document
             */
            final ObjectId documentId = cmisSession.createObjectId(id);
            object = cmisSession.getObject(documentId);
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(id);
            }
            if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FILE.create(id);
            }
            final Document document = (Document) object;
            object = null;
            /*
             * Get SMB file's input stream
             */
            return document.getContentStream().getStream();
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        saveDocument0(file, data, null);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        saveDocument0(file, data, modifiedFields);
    }

    private void saveDocument0(final File file, final InputStream data, final List<Field> modifiedFields) throws OXException {
        try {
            /*
             * Save document
             */
            final ContentStreamImpl contentStream = new ContentStreamImpl(file.getFileName(), null, file.getFileMIMEType(), data);
            createCmisFile(file, modifiedFields, contentStream);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(data);
        }
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        try {
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            object = null;
            /*
             * List documents
             */
            final List<ObjectId> docIds = new LinkedList<ObjectId>();
            for (final CmisObject cmisObject : folder.getChildren()) {
                if (ObjectType.DOCUMENT_BASETYPE_ID.equals(cmisObject.getType().getId())) {
                    docIds.add(cmisSession.createObjectId(cmisObject.getId()));
                }
            }
            for (final ObjectId documentId : docIds) {
                ((Document) cmisSession.getObject(documentId)).deleteAllVersions();
            }
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<FileStorageFileAccess.IDTuple>();
            for (final IDTuple id : ids) {
                final String folderId = id.getFolder();
                final ObjectId folderObjectId;
                CmisObject object;
                if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                    object = cmisSession.getRootFolder();
                    folderObjectId = cmisSession.createObjectId(object.getId());
                } else {
                    folderObjectId = cmisSession.createObjectId(folderId);
                    object = cmisSession.getObject(folderObjectId);
                }
                if (null == object) {
                    continue;
                }
                if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                    throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
                }
                /*
                 * Check document
                 */
                final String did = id.getId();
                final ObjectId documentId = cmisSession.createObjectId(did);
                object = cmisSession.getObject(documentId);
                if (null == object) {
                    continue;
                }
                if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(object.getType().getId())) {
                    throw CMISExceptionCodes.NOT_A_FILE.create(did);
                }
                final Document document = (Document) object;
                document.deleteAllVersions();
            }
            /*
             * Return
             */
            return ret;
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] removeVersion(final String folderId, final String id, final String[] versions) throws OXException {
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw CMISExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        try {
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * Check document
             */
            final ObjectId documentId = cmisSession.createObjectId(id);
            object = cmisSession.getObject(documentId);
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(id);
            }
            if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FILE.create(id);
            }
            final Document document = (Document) object;
            document.deleteAllVersions();
            /*
             * Return empty array
             */
            return new String[0];
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void unlock(final String folderId, final String id) throws OXException {
        // Nothing to do

    }

    @Override
    public void lock(final String folderId, final String id, final long diff) throws OXException {
        // Nothing to do

    }

    @Override
    public void touch(final String folderId, final String id) throws OXException {
        try {
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * Check document
             */
            final ObjectId documentId = cmisSession.createObjectId(id);
            object = cmisSession.getObject(documentId);
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(id);
            }
            if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FILE.create(id);
            }
            final Document document = (Document) object;
            /*
             * Update
             */
            final Map<String, Object> properties = new HashMap<String, Object>(1);
            final Calendar calendar;
            if (session instanceof ServerSession) {
                final User user = ((ServerSession) session).getUser();
                calendar = Calendar.getInstance(TimeZone.getTimeZone(user.getTimeZone()), user.getLocale());
            } else {
                final UserService userService = CMISServices.getService(UserService.class);
                final ContextService contextService = CMISServices.getService(ContextService.class);
                if (null == userService || null == contextService) {
                    calendar = Calendar.getInstance();
                } else {
                    final User user = userService.getUser(session.getUserId(), contextService.getContext(session.getContextId()));
                    calendar = Calendar.getInstance(TimeZone.getTimeZone(user.getTimeZone()), user.getLocale());
                }
            }
            calendar.setTimeInMillis(System.currentTimeMillis());
            properties.put(PropertyIds.LAST_MODIFICATION_DATE, calendar);
            document.updateProperties(properties);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
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

    private List<File> getFileList(final String folderId, final List<Field> fields) throws OXException {
        try {
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            object = null;
            /*
             * List its sub-resources
             */
            final List<File> files = new LinkedList<File>();
            for (final CmisObject child : folder.getChildren()) {
                if (ObjectType.DOCUMENT_BASETYPE_ID.equals(child.getType().getId())) {
                    final Document document = (Document) child;
                    files.add(new CMISFile(folderId, document.getId(), session.getUserId()).parseSmbFile(document));
                }
            }
            /*
             * Return list
             */
            return files;
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
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
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        return new FileTimedResult(Collections.singletonList(getFileMetadata(folderId, id, CURRENT_VERSION)));
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields) throws OXException {
        return new FileTimedResult(Collections.singletonList(getFileMetadata(folderId, id, CURRENT_VERSION)));
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        return new FileTimedResult(Collections.singletonList(getFileMetadata(folderId, id, CURRENT_VERSION)));
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        try {
            /*
             * Iterate identifiers
             */
            final List<File> files = new ArrayList<File>(ids.size());
            for (final IDTuple id : ids) {
                final String folderId = id.getFolder();
                final ObjectId folderObjectId;
                CmisObject object;
                if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                    object = cmisSession.getRootFolder();
                    folderObjectId = cmisSession.createObjectId(object.getId());
                } else {
                    folderObjectId = cmisSession.createObjectId(folderId);
                    object = cmisSession.getObject(folderObjectId);
                }
                if (null == object) {
                    throw CMISExceptionCodes.NOT_FOUND.create(folderId);
                }
                if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                    throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
                }
                /*
                 * Check document
                 */
                final ObjectId documentId = cmisSession.createObjectId(id.getId());
                object = cmisSession.getObject(documentId);
                if (null == object) {
                    throw CMISExceptionCodes.NOT_FOUND.create(id.getId());
                }
                if (!ObjectType.DOCUMENT_BASETYPE_ID.equals(object.getType().getId())) {
                    throw CMISExceptionCodes.NOT_A_FILE.create(id.getId());
                }
                final Document document = (Document) object;
                object = null;
                files.add(new CMISFile(folderId, document.getId(), session.getUserId()).parseSmbFile(document));
            }
            /*
             * Return
             */
            return new FileTimedResult(files);
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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

    private static final String ALL = "*";

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final String pat = isEmpty(pattern) ? ALL : pattern;
        final List<File> results;
        if (ALL_FOLDERS == folderId) {
            /*
             * Recursively search files in directories
             */
            results = new LinkedList<File>();
            recursiveSearchFile(pat, rootUrl, fields, results);
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
                if (!file.matches(pat)) {
                    iterator.remove();
                }
            }
        }
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
            final ObjectId folderObjectId;
            CmisObject object;
            if (FileStorageFolder.ROOT_FULLNAME.equals(folderId) || rootUrl.equals(folderId)) {
                object = cmisSession.getRootFolder();
                folderObjectId = cmisSession.createObjectId(object.getId());
            } else {
                folderObjectId = cmisSession.createObjectId(folderId);
                object = cmisSession.getObject(folderObjectId);
            }
            if (null == object) {
                throw CMISExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!ObjectType.FOLDER_BASETYPE_ID.equals(object.getType().getId())) {
                throw CMISExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final Folder folder = (Folder) object;
            object = null;
            /*
             * List its sub-resources
             */
            for (final CmisObject child : folder.getChildren()) {
                final String typeId = child.getType().getId();
                if (ObjectType.DOCUMENT_BASETYPE_ID.equals(typeId)) {
                    final Document document = (Document) child;
                    final CMISFile file = new CMISFile(folderId, document.getId(), session.getUserId()).parseSmbFile(document);
                    if (file.matches(pattern)) {
                        results.add(file);
                    }
                } else if (ObjectType.FOLDER_BASETYPE_ID.equals(typeId)) {
                    recursiveSearchFile(pattern, child.getId(), fields, results);
                }
            }
        } catch (final CmisBaseException e) {
            throw handleCmisException(e);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
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
