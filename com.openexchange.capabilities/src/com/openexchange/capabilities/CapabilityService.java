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

package com.openexchange.capabilities;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link CapabilityService} - A capability service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface CapabilityService {

    /**
     * Gets the capabilities associated with given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param alignPermissions Whether permission-bound capabilities shall be removed from the resulting set if the services
     *            which define/require those are unavailable (e.g. a user has the <code>editpassword</code> permission
     *            set, but no PasswordChangeService is available).
     * @param allowCache <code>true</code> (default) to allow fetching pre-calculated capabilities from cache; otherwise <code>false</code>
     * @return The capabilities
     * @throws OXException If capabilities cannot be determined
     */
    CapabilitySet getCapabilities(int userId, int contextId, boolean alignPermissions, boolean allowCache) throws OXException;

    /**
     * Gets the capabilities associated with given session. Cached capability sets are always preferred.
     *
     * @param session The session
     * @param alignPermissions Whether permission-bound capabilities shall be removed from the resulting set if the services
     *            which define/require those are unavailable (e.g. a user has the <code>editpassword</code> permission
     *            set, but no PasswordChangeService is available).
     * @return The capabilities
     * @throws OXException If capabilities cannot be determined
     */
    CapabilitySet getCapabilities(Session session, boolean alignPermissions) throws OXException;

    /**
     * Gets the capabilities associated with given user. Behaves the same as {@link #getCapabilities(int, int, boolean, boolean)}
     * called with <code>alignPermissions = false</code> and <code>allowCache = true</code>.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The capabilities
     * @throws OXException If capabilities cannot be determined
     */
    CapabilitySet getCapabilities(int userId, int contextId) throws OXException;

    /**
     * Gets the capabilities associated with given session. Cached capability sets are always preferred. Permission capabilities
     * will not be aligned.
     *
     * @param session The session
     * @return The capabilities
     * @throws OXException If capabilities cannot be determined
     */
    CapabilitySet getCapabilities(Session session) throws OXException;

    /**
     * Declares specified capability.
     *
     * @param capability The capability to declare
     * @return <code>true</code> if capability has not been declared before; otherwise <code>false</code> if already declared (no-op)
     */
    boolean declareCapability(String capability);

    /**
     * Un-Declares specified capability.
     *
     * @param capability The capability to undeclare
     * @return <code>true</code> if capability has been undeclared before; otherwise <code>false</code> if no such capability was available
     */
    boolean undeclareCapability(String capability);

    /**
     * Gets the user configuration and its source based on the given search pattern.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param searchPattern A pattern to filter
     * @return List with {@link ConfigurationProperty}s that match the parameters
     * @throws OXException If capabilities cannot be determined
     */
    List<ConfigurationProperty> getConfigurationSource(int userId, int contextId, String searchPattern) throws OXException;

    /**
     * Gets the capabilities tree showing which capability comes from which source
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The capabilities tree
     * @throws OXException If capabilities tree cannot be returned
     */
    Map<String, Map<String, Set<String>>> getCapabilitiesSource(int userId, int contextId) throws OXException;
}
