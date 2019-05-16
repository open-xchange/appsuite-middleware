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

package com.openexchange.mail.compose.impl.attachment;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.SeekingDataProvider;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link FileStorageAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStorageAttachmentStorage extends AbstractAttachmentStorage {

    /**
     * Initializes a new {@link FileStorageAttachmentStorage}.
     */
    public FileStorageAttachmentStorage(ServiceLookup services) {
        super(services);
    }

    @Override
    public AttachmentStorageType getStorageType() {
        return AttachmentStorageType.FILE_STORAGE;
    }

    @Override
    public List<String> neededCapabilities() {
        return Collections.singletonList("filestore");
    }

    @Override
    protected DataProvider getDataProviderFor(String storageIdentifier, Session session) throws OXException {
        return new FileStorageDataProvider(session, storageIdentifier);
    }

    @Override
    protected String saveData(InputStream input, long size, Session session) throws OXException {
        QuotaFileStorage fileStorage = getFileStorage(session);
        return fileStorage.saveNewFile(input, size);
    }

    @Override
    protected boolean deleteData(String storageIdentifier, Session session) throws OXException {
        QuotaFileStorage fileStorage = getFileStorage(session);
        return fileStorage.deleteFile(storageIdentifier);
    }

    /**
     * Returns the {@link FileStorage} assigned to session-associated context
     *
     * @param session The session providing context identifier
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    protected static QuotaFileStorage getFileStorage(Session session) throws OXException {
        return getFileStorage(session.getContextId());
    }

    /**
     * Returns the {@link FileStorage} assigned to the given context
     *
     * @param contextId The context identifier
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    protected static QuotaFileStorage getFileStorage(int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        return storageService.getQuotaFileStorage(contextId, Info.general());
    }

    /**
     * Returns the {@link FileStorage} assigned to the given context
     *
     * @param contextId The context identifier
     * @return The file storage or <code>null</code>
     */
    public static QuotaFileStorage optFileStorage(int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            return null;
        }
        return storageService.getQuotaFileStorage(contextId, Info.general());
    }

    private static class FileStorageDataProvider implements SeekingDataProvider {

        private final Session session;
        private final String storageIdentifier;

        /**
         * Initializes a new {@link DataProviderImplementation}.
         */
        FileStorageDataProvider(Session session, String storageIdentifier) {
            super();
            this.session = session;
            this.storageIdentifier = storageIdentifier;
        }

        @Override
        public InputStream getData() throws OXException {
            QuotaFileStorage fileStorage = getFileStorage(session);
            return fileStorage.getFile(storageIdentifier);
        }

        @Override
        public InputStream getData(long offset, long length) throws OXException {
            QuotaFileStorage fileStorage = getFileStorage(session);
            return fileStorage.getFile(storageIdentifier, offset, length);
        }
    }

}
