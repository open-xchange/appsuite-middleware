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

package com.openexchange.net.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * The provider of the {@link SSLSocketFactory} based on the configuration made by the administrator.
 *
 * @since v7.8.3
 */
@SingletonService
public interface SSLSocketFactoryProvider {

    /**
     * Returns the configured {@link SSLSocketFactory}. This method is invoked by by reflection.
     * <p>
     * Do not use the underlying {@link SSLSocketFactory} directly as this will bypass the server configuration.
     *
     * @return {@link TrustedSSLSocketFactory} or {@link TrustAllSSLSocketFactory} based on the configuration
     */
    SSLSocketFactory getDefault();

    /**
     * Returns the originating {@link SSLContext}, that is used to create the {@link SSLSocketFactory} according to configuration.
     *
     * @return The {@link SSLContext} instance
     */
    SSLContext getOriginatingDefaultContext();
}
