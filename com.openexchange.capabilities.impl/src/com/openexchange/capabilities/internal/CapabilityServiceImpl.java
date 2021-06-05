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

package com.openexchange.capabilities.internal;

import java.util.List;
import java.util.Map;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.osgi.CapabilityCheckerRegistry;
import com.openexchange.capabilities.osgi.PermissionAvailabilityServiceRegistry;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;

/**
 * Implementation of the {@link AbstractCapabilityService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 */
public final class CapabilityServiceImpl extends AbstractCapabilityService {

    /**
     * Provides the {@link CapabilityChecker}
     */
    private final CapabilityCheckerRegistry capCheckers;

    /**
     * Initializes a new {@link CapabilityServiceImpl}.
     *
     * @param services - {@link ServiceLookup} to search for services
     * @param capCheckers - {@link CapabilityCheckerRegistry} to provide the {@link CapabilityChecker}
     * @param registry - {@link NearRegistryServiceTracker} with the {@link PermissionAvailabilityService} to check if JSON bundles are available
     */
    public CapabilityServiceImpl(ServiceLookup services, CapabilityCheckerRegistry capCheckers, PermissionAvailabilityServiceRegistry registry) {
        super(services, registry);
        this.capCheckers = capCheckers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, List<CapabilityChecker>> getCheckers() {
        return capCheckers.getCheckers();
    }
}
