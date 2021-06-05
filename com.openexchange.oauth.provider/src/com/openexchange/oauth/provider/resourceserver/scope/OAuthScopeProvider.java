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

package com.openexchange.oauth.provider.resourceserver.scope;

import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;


/**
 * Provides information and validation methods for an OAuth scope token. Such a token can
 * be seen as a protection domain that includes a dedicated set of accessible modules and
 * actions. For every token that is defined on an action to prevent unauthorized access
 * a {@link OAuthScopeProvider} must be registered as OSGi service. For your convenience,
 * {@link AbstractScopeProvider} allows you to specify token and description and implement
 * only the {@link #canBeGranted(CapabilitySet)} method.
 *
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 * @see Scope
 * @see AbstractScopeProvider
 */
public interface OAuthScopeProvider {

    /**
     * Gets the scope token. Must be unique within the whole application Allowed characters are
     * %x21 / %x23-5B / %x5D-7E.
     *
     * @return The token
     */
    String getToken();

    /**
     * A localizable string that describes the impact of granting the denoted scope
     * to an external application. The string is shown to the user requesting OAuth
     * access.
     *
     * Example:
     * Application 'example' requires the following permissions:
     * - Read your contacts
     * - Create / modify appointments
     *
     * @return The description
     */
    String getDescription();

    /**
     * Checks whether the denoted scope can be granted for the passed session users
     * capabilities.
     *
     * @param capabilities The capabilities to check
     * @return <code>true</code> if the scope can be granted, <code>false</code> if not.
     * @throws OXException If an error occurs during the permission check.
     */
    boolean canBeGranted(CapabilitySet capabilities);

}
