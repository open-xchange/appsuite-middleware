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
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link DedicatedFileStorageAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class DedicatedFileStorageAttachmentStorage extends FileStorageAttachmentStorage {

    /**
     * Initializes a new {@link DedicatedFileStorageAttachmentStorage}.
     */
    public DedicatedFileStorageAttachmentStorage(ServiceLookup services) {
        super(services);
    }

    @Override
    public AttachmentStorageType getStorageType() {
        return KnownAttachmentStorageType.DEDICATED_FILE_STORAGE;
    }

    @Override
    public boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException {
        // Check if a dedicated file storage is configured
        return getFileStorageId(session.getUserId(), session.getContextId(), services) > 0;
    }

    @Override
    protected FileStorageAndId getFileStorage(Session session) throws OXException {
        // Determine file storage identifier
        int fileStorageId = getFileStorageId(session.getUserId(), session.getContextId(), services);

        // Check determined file storage identifier
        if (fileStorageId <= 0) {
            throw OXException.general("Missing or invalid setting for \"com.openexchange.mail.compose.fileStorageId\" property");
        }

        // Use dedicated file storage with prefix; e.g. "1337_mailcompose_store"
        Pair<FileStorage, URI> fsAndUri = getDedicatedFileStorage(fileStorageId, session.getContextId());
        return new FileStorageAndId(fsAndUri.getFirst(), fileStorageId, fsAndUri.getSecond());
    }

    /**
     * Gets the session-associated configured identifier of the dedicated file storage (if any).
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param services The service look-up
     * @return The identifier of the dedicated file storage or <code>0</code> (zero) if there is none
     * @throws OXException If file storage identifier cannot be returned
     */
    public static int getFileStorageId(int userId, int contextId, ServiceLookup services) throws OXException {
        // Acquire config view for session-associated user
        ConfigViewFactory viewFactory = services.getServiceSafe(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);

        // Check if a dedicated file storage is configured
        return ConfigViews.getDefinedIntPropertyFrom("com.openexchange.mail.compose.fileStorageId", 0, view);
    }

    /**
     * Gets the dedicated file storage for given identifier.
     *
     * @param fileStorageId The file storage identifier
     * @param contextId The context identifier
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    public static Pair<FileStorage, URI> getDedicatedFileStorage(int fileStorageId, int contextId) throws OXException {
        // Acquire needed service
        FileStorageService storageService = FileStorages.getFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(FileStorageService.class);
        }

        FileStorageInfoService infoService = FileStorages.getFileStorageInfoService();
        if (null == infoService) {
            throw ServiceExceptionCode.absentService(FileStorageInfoService.class);
        }

        // Determine base URI and scheme
        URI baseUri = infoService.getFileStorageInfo(fileStorageId).getUri();
        String scheme = baseUri.getScheme();
        if (scheme == null) {
            scheme = "file";
        }

        // Prefer a static prefix in case of "file"-schemed file storage
        String prefix;
        if ("file".equals(scheme)) {
            prefix = "mailcompose_store";
        } else {
            prefix = new StringBuilder(32).append(contextId).append("_mailcompose_store").toString();
        }

        URI uri = FileStorages.getFullyQualifyingUriForPrefix(prefix, baseUri);
        return new Pair<>(storageService.getFileStorage(uri), uri);
    }

    /**
     * Extracts the context identifier from given path prefix.
     *
     * @param prefix The path prefix; e.g. "4321_mailcompose_store"
     * @return The extracted context identifier or <code>-1</code>
     */
    public static int extractContextIdFrom(String prefix) {
        if (Strings.isEmpty(prefix)) {
            return -1;
        }

        String toExtractFrom = prefix.trim();
        if (!toExtractFrom.endsWith("_mailcompose_store")) {
            return -1;
        }

        int length = toExtractFrom.length();
        StringBuilder ctxChars = null;
        int i = 0;
        {
            boolean keepOn = true;
            while (keepOn && i < length) {
                char ch = toExtractFrom.charAt(i);
                int digit = Strings.digitForChar(ch);
                if (digit >= 0) {
                    if (ctxChars == null) {
                        ctxChars = new StringBuilder();
                    }
                    ctxChars.append(ch);
                    i++;
                } else {
                    keepOn = false;
                }
            }
            if (ctxChars == null || keepOn) {
                return -1;
            }
        }

        if (toExtractFrom.charAt(i) != '_') {
            return -1;
        }

        try {
            return Integer.parseInt(ctxChars.toString(), 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
