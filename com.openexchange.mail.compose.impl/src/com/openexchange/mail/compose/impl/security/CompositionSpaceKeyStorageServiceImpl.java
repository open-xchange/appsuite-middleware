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

package com.openexchange.mail.compose.impl.security;

import org.osgi.framework.BundleContext;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorage;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link CompositionSpaceKeyStorageServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CompositionSpaceKeyStorageServiceImpl extends RankingAwareNearRegistryServiceTracker<CompositionSpaceKeyStorage> implements CompositionSpaceKeyStorageService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositionSpaceKeyStorageServiceImpl}.
     */
    public CompositionSpaceKeyStorageServiceImpl(ServiceLookup services, BundleContext context) {
        super(context, CompositionSpaceKeyStorage.class);
        this.services = services;
    }

    @Override
    public CompositionSpaceKeyStorage getKeyStorageFor(Session session) throws OXException {
        CapabilitySet capabilities = getCapabilitySet(session);
        for (CompositionSpaceKeyStorage keyStorage : this) {
            if (keyStorage.isApplicableFor(capabilities, session)) {
                return keyStorage;
            }
        }
        throw CompositionSpaceErrorCode.NO_KEY_STORAGE.create();
    }

    private CapabilitySet getCapabilitySet(Session session) throws OXException {
        CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        return capabilityService.getCapabilities(session);
    }

}
