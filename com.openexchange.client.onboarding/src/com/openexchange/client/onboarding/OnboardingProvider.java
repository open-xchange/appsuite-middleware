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

package com.openexchange.client.onboarding;

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link OnboardingProvider} - Represents an on-boarding provider suitable for configuring/integrating a client for communicating with the
 * Open-Xchange Middleware.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
@Service
public interface OnboardingProvider {

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets this provider's description
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the supported devices.
     *
     * @return The supported devices
     */
    Set<Device> getSupportedDevices();

    /**
     * Gets the on-boarding types, which are supported by this provider
     *
     * @return The supported on-boarding types
     */
    Set<OnboardingType> getSupportedTypes();

    /**
     * Executes specified on-boarding scenario according to given action.
     *
     * @param request The on-boarding request
     * @param previousResult The optional previous result or <code>null</code>
     * @param session The session
     * @return The execution result
     * @throws OXException If execution fails
     */
    Result execute(OnboardingRequest request, Result previousResult, Session session) throws OXException;

    /**
     * Checks if this provider is enabled for session-associated user.
     *
     * @param session The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If availability cannot be checked
     */
    AvailabilityResult isAvailable(Session session) throws OXException;

    /**
     * Checks if this provider is enabled for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If availability cannot be checked
     */
    AvailabilityResult isAvailable(int userId, int contextId) throws OXException;

}
