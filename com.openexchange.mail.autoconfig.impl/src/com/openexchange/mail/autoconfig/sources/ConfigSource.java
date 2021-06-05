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

package com.openexchange.mail.autoconfig.sources;

import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;

/**
 * {@link ConfigSource} - Generates an {@code Autoconfig} instance.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface ConfigSource {

    /**
     * Generates an {@code Autoconfig} instance for given arguments.
     *
     * @param emailLocalPart The local part of the Email address; <code>"<b>someone</b>@somewhere.org"</code>
     * @param emailDomain The domain part of the Email address; <code>"someone@<b>somewhere.org</b>"</code>
     * @param password The associated password
     * @param userId The identifier of the associated user
     * @param contextId The identifier of the associated context
     * @return An {@code Autoconfig} instance or <code>null</code> if generation fails.
     * @throws OXException If operation fails for any reason
     */
    Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId) throws OXException;

    /**
     * Generates an {@code Autoconfig} instance for given arguments.
     *
     * @param emailLocalPart The local part of the Email address; <code>"<b>someone</b>@somewhere.org"</code>
     * @param emailDomain The domain part of the Email address; <code>"someone@<b>somewhere.org</b>"</code>
     * @param password The associated password
     * @param userId The identifier of the associated user
     * @param contextId The identifier of the associated context
     * @param forceSecure <code>true</code> if a secure connection should be enforced; otherwise <code>false</code> to also allow plain ones
     * @return An {@code Autoconfig} instance or <code>null</code> if generation fails.
     * @throws OXException If operation fails for any reason
     */
    DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException;

}
