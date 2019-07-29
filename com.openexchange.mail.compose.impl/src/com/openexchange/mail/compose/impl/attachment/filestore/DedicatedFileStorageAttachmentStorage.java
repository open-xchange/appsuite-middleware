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

package com.openexchange.mail.compose.impl.attachment.filestore;

import java.net.URI;
import java.util.Optional;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
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
        // Acquire config view for session-associated user
        ConfigViewFactory viewFactory = services.getServiceSafe(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        // Check if a dedicated file storage is configured
        return ConfigViews.getDefinedIntPropertyFrom("com.openexchange.mail.compose.fileStorageId", 0, view) > 0;
    }

    @Override
    protected FileStorageReference getFileStorage(Optional<Integer> dedicatedFileStorageId, Session session) throws OXException {
        // Determine file storage identifier
        int fileStorageId;
        if (dedicatedFileStorageId.isPresent()) {
            fileStorageId = dedicatedFileStorageId.get().intValue();
        } else {
            fileStorageId = getFileStorageId(session.getUserId(), session.getContextId(), services);
        }

        // Check determined file storage identifier
        if (fileStorageId <= 0) {
            throw OXException.general("Missing or invalid setting for \"com.openexchange.mail.compose.fileStorageId\" property");
        }

        // Use dedicated file storage with prefix; e.g. "1337_mailcompose_store"
        Pair<FileStorage, URI> fsAndUri = getFileStorage(fileStorageId, session.getContextId());
        return new FileStorageReference(fsAndUri.getFirst(), fileStorageId, fsAndUri.getSecond());
    }

    /**
     * Gets the dedicated file storage for given identifier.
     *
     * @param fileStorageId The file storage identifier
     * @param contextId The context identifier
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    public static Pair<FileStorage, URI> getFileStorage(int fileStorageId, int contextId) throws OXException {
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

}
