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

package com.openexchange.net.ssl.config.impl.internal;

import java.util.List;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.TrustLevel;

/**
 * {@link TrustAllSSLConfigurationService} - The SSL configuration service in case trust-all is configured.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class TrustAllSSLConfigurationService implements SSLConfigurationService {

    private static final TrustAllSSLConfigurationService INSTANCE = new TrustAllSSLConfigurationService();

    /**
     * Gets the singleton instance.
     *
     * @return The instance
     */
    public static TrustAllSSLConfigurationService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link TrustAllSSLConfigurationService}.
     */
    private TrustAllSSLConfigurationService() {
        super();
    }

    @Override
    public boolean isWhitelisted(String hostName) {
        return true;
    }

    @Override
    public TrustLevel getTrustLevel() {
        return TrustLevel.TRUST_ALL;
    }

    @Override
    public String[] getSupportedProtocols() {
        List<String> protocols = SSLProperties.getSupportedProtocols();
        return protocols.toArray(new String[protocols.size()]);
    }

    @Override
    public String[] getSupportedCipherSuites() {
        List<String> cipherSuites = SSLProperties.getSupportedCipherSuites();
        return cipherSuites.toArray(new String[cipherSuites.size()]);
    }

    @Override
    public boolean isVerifyHostname() {
        return false;
    }

    @Override
    public boolean isDefaultTruststoreEnabled() {
        return true;
    }

    @Override
    public boolean isCustomTruststoreEnabled() {
        return false;
    }

    @Override
    public String getCustomTruststoreLocation() {
        return null;
    }

    @Override
    public String getCustomTruststorePassword() {
        return null;
    }
}
