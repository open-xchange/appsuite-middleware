/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.compose.impl.attachment.filestore;

import java.net.URI;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link ContextAssociatedFileStorageAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ContextAssociatedFileStorageAttachmentStorage extends FileStorageAttachmentStorage {

    /**
     * Initializes a new {@link ContextAssociatedFileStorageAttachmentStorage}.
     */
    public ContextAssociatedFileStorageAttachmentStorage(ServiceLookup services) {
        super(services);
    }

    @Override
    public AttachmentStorageType getStorageType() {
        return KnownAttachmentStorageType.CONTEXT_ASSOCIATED_FILE_STORAGE;
    }

    @Override
    public boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException {
        return capabilities.contains("filestore");
    }

    @Override
    protected FileStorageAndId getFileStorage(Session session) throws OXException {
        Pair<FileStorage, URI> fsAndUri = getContextAssociatedFileStorage(session.getContextId());
        return new FileStorageAndId(fsAndUri.getFirst(), fsAndUri.getSecond());
    }

    /**
     * Gets the context-associated file storage for given context identifier.
     *
     * @param contextId The context identifier
     * @return The context-associated file storage
     * @throws OXException If context-associated file storage cannot be returned
     */
    public static Pair<FileStorage, URI> getContextAssociatedFileStorage(int contextId) throws OXException {
        FileStorageService storageService = FileStorages.getFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(FileStorageService.class);
        }

        QuotaFileStorageService quotaStorageService = FileStorages.getQuotaFileStorageService();
        if (null == quotaStorageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }

        // Grab quota-aware file storage to determine fully qualifying URI
        URI uri = quotaStorageService.getQuotaFileStorage(contextId, Info.general()).getUri();
        return new Pair<>(storageService.getFileStorage(uri), uri);
    }

    /**
     * Returns the {@link FileStorage} assigned to the given context
     *
     * @param contextId The context identifier
     * @return The file storage or <code>null</code>
     */
    public static Pair<FileStorage, URI> optFileStorage(int contextId) {
        try {
            return getContextAssociatedFileStorage(contextId);
        } catch (Exception e) {
            return null;
        }
    }

}
