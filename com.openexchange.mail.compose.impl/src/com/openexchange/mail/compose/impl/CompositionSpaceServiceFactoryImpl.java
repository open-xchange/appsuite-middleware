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

package com.openexchange.mail.compose.impl;

import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.security.CompositionSpaceKeyStorageServiceImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceServiceFactoryImpl} - The composition space service factory implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CompositionSpaceServiceFactoryImpl implements CompositionSpaceServiceFactory {

    private final ServiceLookup services;
    private final CompositionSpaceStorageService storageService;
    private final AttachmentStorageService attachmentStorageService;
    private final CompositionSpaceKeyStorageServiceImpl keyStorageService;

    /**
     * Initializes a new {@link CompositionSpaceServiceFactoryImpl}.
     *
     * @param storageService The storage service
     * @param attachmentStorageService The attachment storage service
     * @param keyStorageService The key storage service
     * @param services The service look-up
     */
    public CompositionSpaceServiceFactoryImpl(CompositionSpaceStorageService storageService, AttachmentStorageService attachmentStorageService, CompositionSpaceKeyStorageServiceImpl keyStorageService, ServiceLookup services) {
        super();
        this.keyStorageService = keyStorageService;
        if (null == storageService) {
            throw new IllegalArgumentException("Storage service must not be null");
        }
        if (null == attachmentStorageService) {
            throw new IllegalArgumentException("Attachment storage service must not be null");
        }
        if (null == services) {
            throw new IllegalArgumentException("Service registry must not be null");
        }
        this.storageService = storageService;
        this.attachmentStorageService = attachmentStorageService;
        this.services = services;
    }

    @Override
    public CompositionSpaceService createServiceFor(Session session) throws OXException {
        CompositionSpaceServiceImpl serviceImpl = new CompositionSpaceServiceImpl(session, storageService, attachmentStorageService, services);
        return new CryptoCompositionSpaceService(session, serviceImpl, keyStorageService, services);
    }

}
