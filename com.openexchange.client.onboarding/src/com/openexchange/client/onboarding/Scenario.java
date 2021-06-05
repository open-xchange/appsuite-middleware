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

import java.util.List;
import com.openexchange.session.Session;

/**
 * {@link Scenario} - An on-boarding scenario.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface Scenario extends Entity {

    /**
     * Gets the associated type for this scenario.
     *
     * @return The type
     */
    OnboardingType getType();

    /**
     * Gets the link
     *
     * @return The link
     */
    Link getLink();

    /**
     * Gets the associated on-boarding providers.
     *
     * @param session The session to use
     * @return The provider list
     */
    List<OnboardingProvider> getProviders(Session session);

    /**
     * Gets the associated on-boarding providers.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The provider list
     */
    List<OnboardingProvider> getProviders(int userId, int contextId);

    /**
     * Gets the alternative scenarios.
     *
     * @param session The session to use
     * @return The alternative scenarios
     */
    List<Scenario> getAlternatives(Session session);

    /**
     * Gets the optional capabilities associated with this scenario
     *
     * @param session The session to use
     * @return The optional capabilities (may be <code>null</code> or an empty list)
     */
    List<String> getCapabilities(Session session);

    /**
     * Gets the optional capabilities associated with this scenario
     *
     *  @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional capabilities (may be <code>null</code> or an empty list)
     */
    List<String> getCapabilities(int userId, int contextId);

}
