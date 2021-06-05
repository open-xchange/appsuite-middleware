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

package com.openexchange.mail.autoconfig.sources.staticsource;

import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.sources.ConfigSource;

/**
 * {@link AbstractStaticConfigSource} - An abstract class for static auto-config sources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public abstract class AbstractStaticConfigSource implements ConfigSource {

    /**
     * Initializes a new {@link AbstractStaticConfigSource}.
     */
    protected AbstractStaticConfigSource() {
        super();
    }

    @Override
    public final Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, true);
    }

    @Override
    public final DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        if (accept(emailDomain)) {
            return getStaticAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, forceSecure);
        }
        return null;
    }

    /**
     * Checks if specified domain is supported; e.g. <code>"yahoo.com"</code>.
     *
     * @param emailDomain The domain to check
     * @return <code>true</code> if accepted; otherwise <code>false</code>
     */
    protected abstract boolean accept(String emailDomain);

    /**
     * Gets the static auto-config.
     *
     * @param emailLocalPart The local part
     * @param emailDomain The domain part
     * @param password The password
     * @param user The identifier of the associated user
     * @param context The identifier of the context
     * @param forceSecure <code>true</code> if a secure connection should be enforced; otherwise <code>false</code> to also allow plain ones
     * @return The auto-config or <code>null</code>
     * @throws OXException If an error occurs
     */
    protected abstract DefaultAutoconfig getStaticAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException;

}
