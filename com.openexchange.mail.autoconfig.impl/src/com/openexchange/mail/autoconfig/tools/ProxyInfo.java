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

package com.openexchange.mail.autoconfig.tools;

import java.net.URI;

/**
 * {@link ProxyInfo}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public final class ProxyInfo {

    final URI proxyUrl;
    final String proxyLogin;
    final String proxyPassword;

    public ProxyInfo(URI proxyUrl, String proxyLogin, String proxyPassword) {
        super();
        this.proxyUrl = proxyUrl;
        this.proxyLogin = proxyLogin;
        this.proxyPassword = proxyPassword;
    }

    @Override
    public String toString() {
        return proxyUrl.toString();
    }

    /**
     * Gets the proxyLogin
     *
     * @return The proxyLogin
     */
    public String getProxyLogin() {
        return proxyLogin;
    }

    /**
     * Gets the proxyPassword
     *
     * @return The proxyPassword
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Gets the proxyUrl
     *
     * @return The proxyUrl
     */
    public URI getProxyUrl() {
        return proxyUrl;
    }

}
