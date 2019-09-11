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
