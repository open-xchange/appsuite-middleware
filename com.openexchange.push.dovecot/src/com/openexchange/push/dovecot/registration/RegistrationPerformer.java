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

package com.openexchange.push.dovecot.registration;

import com.openexchange.exception.OXException;

/**
 * {@link RegistrationPerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface RegistrationPerformer {

    /**
     * Gets the ranking
     *
     * @return The ranking
     */
    int getRanking();

    /**
     * Initializes registration for this listener.
     *
     * @param context The context to use
     * @return A reason string in case registration failed; otherwise <code>null</code> on success
     * @throws OXException If registration fails unexpectedly
     */
    RegistrationResult initateRegistration(RegistrationContext context) throws OXException;

    /**
     * Unregisters this listeners.
     *
     * @param context The context to use
     * @throws OXException If unregistration fails
     */
    void unregister(RegistrationContext context) throws OXException;
}
