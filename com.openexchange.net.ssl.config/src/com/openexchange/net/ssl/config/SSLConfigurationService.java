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

package com.openexchange.net.ssl.config;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link SSLConfigurationService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
@SingletonService
public interface SSLConfigurationService {

    /**
     * Returns if the provided hostname is whitelisted and therefore considered as 'trustable'
     *
     * @param hostName A hostname to check
     * @return <code>true</code> if the given hostname is whitelisted; otherwise <code>false</code>
     */
    boolean isWhitelisted(String hostName);

    /**
     * Returns the {@link TrustLevel} configured for the server.
     *
     * @return {@link TrustLevel} enum of the configured level
     */
    TrustLevel getTrustLevel();

    /**
     * Returns the protocols that will be considered by the server for SSL handshaking with external systems.
     *
     * @return An Array with supported protocols.
     */
    String[] getSupportedProtocols();

    /**
     * Returns the cipher suites that will be considered by the server for SSL handshaking with external systems.
     *
     * @return An Array with supported cipher suites.
     */
    String[] getSupportedCipherSuites();

    /**
     * Returns if the server is configured to check hostnames while SSL handshaking.
     *
     * @return <code>true</code> if the hostnames should be checked; otherwise <code>false</code>
     */
    boolean isVerifyHostname();

    /**
     * Returns if the default truststore provided with the JVM should be used.
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return <code>true</code> if the default truststore is used; otherwise <code>false</code>
     */
    boolean isDefaultTruststoreEnabled();

    /**
     * Returns if the custom truststore defined by the administrator should be used.
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return <code>true</code> if the custom truststore is used; otherwise <code>false</code>
     * @see #getCustomTruststoreLocation()
     * @see #getCustomTruststorePassword()
     */
    boolean isCustomTruststoreEnabled();

    /**
     * Returns the location of the custom truststore consisting of the path and file name (e. g. /opt/open-xchange/customTrustStore.jks).
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return the location of the custom truststore
     */
    String getCustomTruststoreLocation();

    /**
     * Returns the password of the custom truststore to get access.
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return the password to access the custom truststore
     */
    String getCustomTruststorePassword();

}
