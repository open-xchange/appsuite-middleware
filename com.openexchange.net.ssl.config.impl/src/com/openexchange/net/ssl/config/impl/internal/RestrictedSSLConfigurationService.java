/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.net.ssl.config.impl.internal;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.TrustLevel;

/**
 * The {@link RestrictedSSLConfigurationService} provides user specific configuration with regards to SSL
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class RestrictedSSLConfigurationService implements SSLConfigurationService {

    private final TrustLevel trustLevel;
    private final AtomicReference<RestrictedConfig> configReference;
    private final boolean hostnameVerificationEnabled;
    private final boolean defaultTruststoreEnabled;
    private final boolean customTruststoreEnabled;
    private final String customTruststoreLocation;
    private final String customTruststorePassword;

    /**
     * Initializes a new {@link RestrictedSSLConfigurationService}.
     *
     * @param trustLevel The trust level
     * @param configService The service to use
     */
    public RestrictedSSLConfigurationService(TrustLevel trustLevel, ConfigurationService configService) {
        super();
        this.trustLevel = trustLevel;
        this.configReference = new AtomicReference<RestrictedConfig>(SSLProperties.newConfig(configService));

        // The immutable configuration properties
        this.hostnameVerificationEnabled = configService.getBoolProperty(SSLProperties.HOSTNAME_VERIFICATION_ENABLED.getName(), SSLProperties.HOSTNAME_VERIFICATION_ENABLED.getDefaultBoolean());
        this.defaultTruststoreEnabled = configService.getBoolProperty(SSLProperties.DEFAULT_TRUSTSTORE_ENABLED.getName(), SSLProperties.DEFAULT_TRUSTSTORE_ENABLED.getDefaultBoolean());
        this.customTruststoreEnabled = configService.getBoolProperty(SSLProperties.CUSTOM_TRUSTSTORE_ENABLED.getName(), SSLProperties.CUSTOM_TRUSTSTORE_ENABLED.getDefaultBoolean());
        this.customTruststoreLocation = configService.getProperty(SSLProperties.CUSTOM_TRUSTSTORE_LOCATION.getName(), SSLProperties.CUSTOM_TRUSTSTORE_LOCATION.getDefault());
        this.customTruststorePassword = configService.getProperty(SSLProperties.CUSTOM_TRUSTSTORE_PASSWORD.getName(), SSLProperties.CUSTOM_TRUSTSTORE_PASSWORD.getDefault());
    }

    /**
     * Reloads the configuration
     */
    public void reload(ConfigurationService configService) {
        RestrictedConfig existing;
        RestrictedConfig newConfig;
        do {
            existing = configReference.get();
            newConfig = SSLProperties.newConfig(configService);
        } while (!configReference.compareAndSet(existing, newConfig));
    }

    @Override
    public boolean isWhitelisted(String hostName) {
        RestrictedConfig config = configReference.get();
        return config.isWhitelisted(hostName);
    }

    @Override
    public TrustLevel getTrustLevel() {
        return trustLevel;
    }

    @Override
    public String[] getSupportedProtocols() {
        RestrictedConfig config = configReference.get();
        return config.getProtocols();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        RestrictedConfig config = configReference.get();
        return config.getCiphers();
    }

    @Override
    public boolean isVerifyHostname() {
        return hostnameVerificationEnabled;
    }

    @Override
    public boolean isDefaultTruststoreEnabled() {
        return defaultTruststoreEnabled;
    }

    @Override
    public boolean isCustomTruststoreEnabled() {
        return customTruststoreEnabled;
    }

    @Override
    public String getCustomTruststoreLocation() {
        return customTruststoreLocation;
    }

    @Override
    public String getCustomTruststorePassword() {
        return customTruststorePassword;
    }
}
