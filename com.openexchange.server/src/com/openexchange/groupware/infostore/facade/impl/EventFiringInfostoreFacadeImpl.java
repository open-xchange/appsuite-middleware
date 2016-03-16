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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.EventFiringInfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.java.Streams;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link EventFiringInfostoreFacadeImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EventFiringInfostoreFacadeImpl extends InfostoreFacadeImpl implements EventFiringInfostoreFacade {

    private static final String SERVICE_ID = "com.openexchange.infostore";
    private static final String ACCOUNT_ID = "infostore";

    /**
     * Initializes a new {@link EventFiringInfostoreFacadeImpl}.
     */
    public EventFiringInfostoreFacadeImpl() {
        super();
    }

    /**
     * Initializes a new {@link EventFiringInfostoreFacadeImpl}.
     *
     * @param provider The db provider to use
     */
    public EventFiringInfostoreFacadeImpl(DBProvider provider) {
        super(provider);
    }

    @Override
    public InputStream getDocument(int id, int version, long offset, long length, ServerSession session) throws OXException {
        DocumentMetadata dm = load(id, version, session.getContext());
        EffectiveInfostorePermission infoPerm = security.getInfostorePermission(session, dm);
        if (false == infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }

        com.openexchange.filestore.FileStorage fs = getFileStorage(infoPerm.getFolderOwner(), session.getContextId());
        InputStream document;
        if (dm.getFilestoreLocation() == null) {
            document = Streams.newByteArrayInputStream(new byte[0]);
        } else if (0 == offset && -1 == length) {
            document = fs.getFile(dm.getFilestoreLocation());
        } else {
            document = fs.getFile(dm.getFilestoreLocation(), offset, length);
        }

        fireEvent(FileStorageEventHelper.buildAccessEvent(session, SERVICE_ID, ACCOUNT_ID, getFolderID(dm), getFileID(dm), dm.getFileName()));
        return document;
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession session) throws OXException {
        boolean wasCreation = InfostoreFacade.NEW == document.getId();
        IDTuple result = super.saveDocument(document, data, sequenceNumber, session);
        if (wasCreation) {
            fireEvent(FileStorageEventHelper.buildCreateEvent(
                session, SERVICE_ID, ACCOUNT_ID, getFolderID(document), getFileID(document), document.getFileName()));
        } else {
            /*
             * leads to
             *
             *   saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns,
             *       boolean ignoreVersion, long offset, ServerSession session)
             *
             * being called from super class
             */
        }
        return result;
    }

    @Override
    protected IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, long offset, ServerSession session) throws OXException {
        boolean wasCreation = InfostoreFacade.NEW == document.getId();
        IDTuple result = super.saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion, offset, session);
        if (wasCreation) {
            /*
             * leads to
             *
             *   saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession session)
             *
             * being called from super class
             */
        } else {
            fireEvent(FileStorageEventHelper.buildUpdateEvent(
                session, SERVICE_ID, ACCOUNT_ID, getFolderID(document), getFileID(document), document.getFileName()));
        }
        return result;
    }

    @Override
    protected void removeDocuments(List<DocumentMetadata> allDocuments, List<DocumentMetadata> allVersions, long date, ServerSession sessionObj, List<DocumentMetadata> rejected) throws OXException {
        super.removeDocuments(allDocuments, allVersions, date, sessionObj, rejected);
        if (null != allDocuments && 0 < allDocuments.size()) {
            for (DocumentMetadata document : allDocuments) {
                if (null != rejected && rejected.contains(document)) {
                    continue;
                }
                fireEvent(FileStorageEventHelper.buildDeleteEvent(
                    sessionObj, SERVICE_ID, ACCOUNT_ID, getFolderID(document), getFileID(document), document.getFileName(), null));
            }
        }
    }

    @Override
    protected List<DocumentMetadata> moveDocuments(ServerSession session, List<DocumentMetadata> documents, long destinationFolderID,
        long sequenceNumber, boolean adjustFilenamesAsNeeded) throws OXException {
        List<DocumentMetadata> rejectedDocuments = super.moveDocuments(
            session, documents, destinationFolderID, sequenceNumber, adjustFilenamesAsNeeded);
        if (null != documents && 0 < documents.size()) {
            for (DocumentMetadata document : documents) {
                if (null != rejectedDocuments && rejectedDocuments.contains(document)) {
                    continue;
                }
                fireEvent(FileStorageEventHelper.buildUpdateEvent(session, SERVICE_ID, ACCOUNT_ID,
                    new FolderID(SERVICE_ID, ACCOUNT_ID, String.valueOf(destinationFolderID)).toUniqueID(),
                    getFileID(document), document.getFileName()));
            }
        }
        return rejectedDocuments;
    }

    @Override
    public int[] removeVersion(int id, int[] versionIds, ServerSession session) throws OXException {
        if (null == versionIds || 0 == versionIds.length) {
            return super.removeVersion(id, versionIds, session);
        }
        DocumentMetadata document = load(id, InfostoreFacade.CURRENT_VERSION, session.getContext());
        int[] notRemoved = super.removeVersion(id, versionIds, session);
        Set<String> deletedVersions = new HashSet<String>(versionIds.length);
        for (int versionID : versionIds) {
            if (null != notRemoved && Arrays.contains(notRemoved, versionID)) {
                continue;
            }
            deletedVersions.add(String.valueOf(versionID));
        }
        /*
         * fire event if needed
         */
        if (0 < deletedVersions.size()) {
            fireEvent(FileStorageEventHelper.buildDeleteEvent(
                session, SERVICE_ID, ACCOUNT_ID, getFolderID(document), getFileID(document), document.getFileName(), deletedVersions));
        }
        return notRemoved;
    }

    private static void fireEvent(Event event) {
        EventAdmin eventAdmin = ServerServiceRegistry.getServize(EventAdmin.class);
        if (null != eventAdmin) {
            eventAdmin.postEvent(event);
        }
    }

    private static String getFileID(DocumentMetadata document) {
        return new FileID(SERVICE_ID, ACCOUNT_ID, String.valueOf(document.getFolderId()), String.valueOf(document.getId())).toUniqueID();
    }

    private static String getFolderID(DocumentMetadata document) {
        return new FolderID(SERVICE_ID, ACCOUNT_ID, String.valueOf(document.getFolderId())).toUniqueID();
    }

}
