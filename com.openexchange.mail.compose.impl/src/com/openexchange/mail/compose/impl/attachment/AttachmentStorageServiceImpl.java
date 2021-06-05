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

package com.openexchange.mail.compose.impl.attachment;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.NonCryptoCompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.attachment.security.CryptoAttachmentStorage;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link AttachmentStorageServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentStorageServiceImpl extends RankingAwareNearRegistryServiceTracker<AttachmentStorage> implements AttachmentStorageService {

    private final ServiceLookup services;
    private final CompositionSpaceKeyStorageService keyStorageService;
    private final AtomicReference<CompositionSpaceStorageService> compositionSpaceStorage;

    /**
     * Initializes a new {@link AttachmentStorageServiceImpl}.
     */
    public AttachmentStorageServiceImpl(CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services, BundleContext context) {
        super(context, AttachmentStorage.class);
        this.services = services;
        this.keyStorageService = keyStorageService;
        compositionSpaceStorage = new AtomicReference<>();
    }

    /**
     * Sets the composition space storage instance.
     *
     * @param compositionSpaceStorage The composition space storage instance
     */
    public void setCompositionSpaceStorageService(CompositionSpaceStorageService compositionSpaceStorage) {
        if (!(compositionSpaceStorage instanceof NonCryptoCompositionSpaceStorageService)) {
            throw new IllegalArgumentException("Composition space storage instance must not be crypto-aware");
        }

        this.compositionSpaceStorage.set(compositionSpaceStorage);
    }

    @Override
    public AttachmentStorage getAttachmentStorageFor(Session session) throws OXException {
        // Obtain user's capabilities
        CapabilitySet capabilities = getCapabilitySet(session);

        // Find suitable attachment storage
        for (AttachmentStorage attachmentStorage : this) {
            if (attachmentStorage.isApplicableFor(capabilities, session)) {
                return new CryptoAttachmentStorage(attachmentStorage, compositionSpaceStorage.get(), keyStorageService, services);
            }
        }
        throw CompositionSpaceErrorCode.NO_ATTACHMENT_STORAGE.create();
    }

    private CapabilitySet getCapabilitySet(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        return capabilityService.getCapabilities(session);
    }

    @Override
    public AttachmentStorage getAttachmentStorageByType(AttachmentStorageType storageType) throws OXException {
        // Find matching attachment storage
        for (AttachmentStorage attachmentStorage : this) {
            if (attachmentStorage.getStorageType().getType() == storageType.getType()) {
                return new CryptoAttachmentStorage(attachmentStorage, compositionSpaceStorage.get(), keyStorageService, services);
            }
        }
        throw CompositionSpaceErrorCode.NO_ATTACHMENT_STORAGE.create();
    }

}
