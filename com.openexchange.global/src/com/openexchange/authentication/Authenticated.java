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

package com.openexchange.authentication;

/**
 * This data must be available to the application after a user has been authenticated. It is used to assign the according context and user
 * information.
 * <p>
 * If you want to influence the session, the {@link Authenticated} instance may also implement {@link SessionEnhancement}.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @see SessionEnhancement
 */
public interface Authenticated {

    /**
     * The default context info/login mapping: {@code defaultcontext}
     */
    static final String DEFAULT_CONTEXT_INFO = "defaultcontext";

    /**
     * Gets the context information used to look-up the associated context.
     *
     * @return The context information
     */
    String getContextInfo();

    /**
     * Gets the user information used to look-up the associated user.
     *
     * @return The user information
     */
    String getUserInfo();

}
