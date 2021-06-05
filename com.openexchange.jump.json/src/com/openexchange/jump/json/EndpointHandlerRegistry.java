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

package com.openexchange.jump.json;

import java.util.List;
import java.util.Set;
import com.openexchange.jump.EndpointHandler;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;


/**
 * {@link EndpointHandlerRegistry} - A registry for en-point handlers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class EndpointHandlerRegistry {

    private final RankingAwareNearRegistryServiceTracker<EndpointHandler> registry;

    /**
     * Initializes a new {@link EndpointHandlerRegistry}.
     */
    public EndpointHandlerRegistry(final RankingAwareNearRegistryServiceTracker<EndpointHandler> registry) {
        super();
        this.registry = registry;
    }

    /**
     * Gets the rank-wise sorted list of known end-point handlers.
     *
     * @return The rank-wise sorted list
     */
    public List<EndpointHandler> getHandlers() {
        return registry.getServiceList();
    }

    /**
     * Checks if this registry has a handler for given system name.
     *
     * @param systemName The system name
     * @return <code>true</code> if such a handler is present; otherwise <code>false</code>
     */
    public boolean hasHandlerFor(final String systemName) {
        for (final EndpointHandler endpointHandler : getHandlers()) {
            final Set<String> namesOfInterest = endpointHandler.systemNamesOfInterest();
            if (namesOfInterest.contains(systemName)) {
                return true;
            }
        }
        return false;
    }

}
