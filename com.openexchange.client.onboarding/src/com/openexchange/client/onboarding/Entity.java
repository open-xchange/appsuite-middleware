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

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link Entity} - An on-boarding entity.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface Entity {

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Checks if this entity is enabled.
     *
     * @param session The session to use
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If enabled flag cannot be returned
     */
    boolean isEnabled(Session session) throws OXException;

    /**
     * Checks if this entity is enabled.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If enabled flag cannot be returned
     */
    boolean isEnabled(int userId, int contextId) throws OXException;

    /**
     * Gets the display name appropriate for the specified user and context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The display name
     * @throws OXException If display name cannot be returned
     */
    String getDisplayName(int userId, int contextId) throws OXException;

    /**
     * Gets the display name appropriate for the specified session
     *
     * @param session The session to use
     * @return The display name
     * @throws OXException If display name cannot be returned
     */
    String getDisplayName(Session session) throws OXException;

    /**
     * Gets the icon associated with this on-boarding entity.
     *
     * @param session The session to use
     * @return The icon
     * @throws OXException If icon cannot be returned
     */
    Icon getIcon(Session session) throws OXException;

    /**
     * Gets the optional description for this entity
     *
     * @param session The session to use
     * @return The description or <code>null</code>
     * @throws OXException If description cannot be returned
     */
    String getDescription(Session session) throws OXException;

}
