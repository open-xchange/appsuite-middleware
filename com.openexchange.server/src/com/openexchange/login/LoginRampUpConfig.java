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

package com.openexchange.login;

import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.login.DefaultAppSuiteLoginRampUp.RampUpKey;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link LoginRampUpConfig} - The login ramp-up configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class LoginRampUpConfig {

    private static final Cache<String, LoginRampUpConfig> CACHE_CONFIGS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterAccess(30, TimeUnit.MINUTES).build();

    /**
     * Clears the config cache.
     */
    public static void invalidateCache() {
        CACHE_CONFIGS.invalidateAll();
    }

    private static final LoginRampUpConfig DEFAULT_CONFIG = configBuilder().build();
    private static final String DEFAULT_KEY = "default";

    /**
     * Gets the config to use.
     *
     * @param infix The infix to use when looking up config settings
     * @return The config to use
     */
    public static LoginRampUpConfig getConfig(String infix) {
        String key = null == infix ? DEFAULT_KEY : infix;
        LoginRampUpConfig loginRampUpConfig = CACHE_CONFIGS.getIfPresent(key);
        if (null == loginRampUpConfig) {
            ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            if (null == configService) {
                return DEFAULT_CONFIG;
            }
            loginRampUpConfig = doGetConfig(infix, configService);
            CACHE_CONFIGS.put(key, loginRampUpConfig);
        }
        return loginRampUpConfig;
    }

    private static LoginRampUpConfig doGetConfig(String infix, ConfigurationService configService) {
        ConfigBuilder configBuilder = configBuilder();

        StringBuilder propNameBuilder = new StringBuilder("com.openexchange.ajax.login.rampup.disabled"); // Initialize with prefix
        configBuilder.serverConfigDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.SERVER_CONFIG.key), false));
        configBuilder.accountsDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.ACCOUNTS.key), false));
        configBuilder.folderDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.FOLDER.key), false));
        configBuilder.folderlistDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.FOLDER_LIST.key), false));
        configBuilder.jslobsDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.JSLOBS.key), false));
        configBuilder.oauthDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.OAUTH.key), false));
        configBuilder.userDisabled(configService.getBoolProperty(buildPropertyName(propNameBuilder, infix, RampUpKey.USER.key), false));
        configBuilder.debugThresholdMillis(configService.getIntProperty("com.openexchange.ajax.login.rampup.debugThresholdMillis", 0));

        return configBuilder.build();
    }

    private static String buildPropertyName(StringBuilder propNameBuilder, String optInfix, String suffix) {
        int len = propNameBuilder.length();
        if (null != optInfix) {
            propNameBuilder.append('.').append(optInfix);
        }
        String propName = propNameBuilder.append('.').append(suffix).toString();
        propNameBuilder.setLength(len);
        return propName;
    }

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static ConfigBuilder configBuilder() {
        return new ConfigBuilder();
    }

    /** The config builder */
    public static class ConfigBuilder {

        private boolean serverConfigDisabled;
        private boolean jslobsDisabled;
        private boolean oauthDisabled;
        private boolean folderDisabled;
        private boolean folderlistDisabled;
        private boolean userDisabled;
        private boolean accountsDisabled;
        private int debugThresholdMillis;

        ConfigBuilder() {
            super();
            debugThresholdMillis = 0;
        }

        ConfigBuilder debugThresholdMillis(int debugThresholdMillis) {
            this.debugThresholdMillis = debugThresholdMillis;
            return this;
        }

        ConfigBuilder serverConfigDisabled(boolean serverConfigDisabled) {
            this.serverConfigDisabled = serverConfigDisabled;
            return this;
        }

        ConfigBuilder jslobsDisabled(boolean jslobsDisabled) {
            this.jslobsDisabled = jslobsDisabled;
            return this;
        }

        ConfigBuilder oauthDisabled(boolean oauthDisabled) {
            this.oauthDisabled = oauthDisabled;
            return this;
        }

        ConfigBuilder folderDisabled(boolean folderDisabled) {
            this.folderDisabled = folderDisabled;
            return this;
        }

        ConfigBuilder folderlistDisabled(boolean folderlistDisabled) {
            this.folderlistDisabled = folderlistDisabled;
            return this;
        }

        ConfigBuilder userDisabled(boolean userDisabled) {
            this.userDisabled = userDisabled;
            return this;
        }

        ConfigBuilder accountsDisabled(boolean accountsDisabled) {
            this.accountsDisabled = accountsDisabled;
            return this;
        }

        LoginRampUpConfig build() {
            return new LoginRampUpConfig(serverConfigDisabled, jslobsDisabled, oauthDisabled, folderDisabled, folderlistDisabled, userDisabled, accountsDisabled, debugThresholdMillis);
        }
    }

    public final boolean serverConfigDisabled;
    public final boolean jslobsDisabled;
    public final boolean oauthDisabled;
    public final boolean folderDisabled;
    public final boolean folderlistDisabled;
    public final boolean userDisabled;
    public final boolean accountsDisabled;
    public final int debugThresholdMillis;

    LoginRampUpConfig(boolean serverConfigDisabled, boolean jslobsDisabled, boolean oauthDisabled, boolean folderDisabled, boolean folderlistDisabled, boolean userDisabled, boolean accountsDisabled, int debugThresholdMillis) {
        super();
        this.serverConfigDisabled = serverConfigDisabled;
        this.jslobsDisabled = jslobsDisabled;
        this.oauthDisabled = oauthDisabled;
        this.folderDisabled = folderDisabled;
        this.folderlistDisabled = folderlistDisabled;
        this.userDisabled = userDisabled;
        this.accountsDisabled = accountsDisabled;
        this.debugThresholdMillis = debugThresholdMillis;
    }

}
