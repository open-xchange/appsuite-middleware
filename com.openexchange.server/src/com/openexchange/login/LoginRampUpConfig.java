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
 *    trademarks of the OX Software GmbH. group of companies.
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
