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

package com.openexchange.ajax.login;

import java.util.Date;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.InitProperty;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.share.GuestInfo;

/**
 * {@link ShareLoginConfiguration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareLoginConfiguration {

    public enum ShareLoginProperty implements InitProperty {

        /**
         * The client name to use for automatically logged-in guest sessions
         */
        CLIENT_NAME("com.openexchange.share.clientName", "open-xchange-appsuite"),

        /**
         * The client version to use for automatically logged-in guest sessions
         */
        CLIENT_VERSION("com.openexchange.share.clientVersion", "Share"),

        /**
         * The TTL for the client cookies written when accessing a share, or <code>null</code> to fall back to the default login
         * configuration
         */
        COOKIE_TTL("com.openexchange.share.cookieTTL", "-1"),

        /**
         * <code>true</code> if guest sessions should be transient, <code>false</code>, otherwise
         */
        TRANSIENT_SESSIONS("com.openexchange.share.transientSessions", "true"),

        ;

        private final String propertyName;
        private final String defaultValue;

        private ShareLoginProperty(String propertyName, String defaultValue) {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

    }

    private String shareClientName;
    private String shareClientVersion;
    private Integer shareCookieTTL;
    private boolean shareTransientSessions;

    /**
     * Initializes a new {@link ShareLoginConfiguration}.
     *
     * @param shareClientName The client name to use for automatically logged-in guest sessions
     * @param shareClientVersion The client version to use for automatically logged-in guest sessions
     * @param shareCookieTTL The TTL for the client cookies written when accessing a share, or <code>null</code> to fall back to the
     *                       default login configuration
     * @param shareTransientSessions <code>true</code> if guest sessions should be transient, <code>false</code>, otherwise
     */
    public ShareLoginConfiguration(String shareClientName, String shareClientVersion, Integer shareCookieTTL, boolean shareTransientSessions) {
        super();
        reinitialise(shareClientName, shareClientVersion, shareCookieTTL, shareTransientSessions);
    }

    /**
     * Initializes a new {@link ShareLoginConfiguration}.
     *
     * @param configService A reference to the configuration service for initialization
     * @throws OXException
     */
    public ShareLoginConfiguration(ConfigurationService configService) throws OXException {
        super();
        reinitialise(configService);
    }

    /**
     * Gets a custom login configuration suitable for the share, where cookie TTLs are adjusted as needed.
     *
     * @param guest The guest info to determine the session cookies expiry
     * @return The login configuration
     * @throws OXException
     */
    public LoginConfiguration getLoginConfig(GuestInfo guest) throws OXException {
        return adjustCookieTTL(getLoginConfig(), guest);
    }

    /**
     * Gets the default login configuration used when logging in through the share servlet.
     *
     * @return The login configuration
     * @throws OXException
     */
    public LoginConfiguration getLoginConfig() throws OXException {
        /*
         * construct custom login config based on default template, overridden with share-specific values
         */
        LoginConfiguration defaultConfig = LoginServlet.getLoginConfiguration();
        if (null == defaultConfig) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("no default login configuration available");
        }
        return getLoginConfig(defaultConfig);
    }

    /**
     * Gets the default login configuration used when logging in through the share servlet.
     *
     * @param defaultConfig The default login configuration (as available via {@link LoginServlet#getLoginConfiguration()})
     * @return The share login configuration
     */
    public LoginConfiguration getLoginConfig(LoginConfiguration defaultConfig) {
        /*
         * construct custom login config based on default template, overridden with share-specific values
         */
        return new LoginConfiguration(
            defaultConfig.getUiWebPath(), // com.openexchange.UIWebPath
            defaultConfig.getHashSource(), // com.openexchange.cookie.hash
            defaultConfig.getHttpAuthAutoLogin(),
            null != shareClientName ? shareClientName : defaultConfig.getDefaultClient(),
            null != shareClientVersion ? shareClientVersion : defaultConfig.getClientVersion(),
            defaultConfig.getErrorPageTemplate(), // com.openexchange.ajax.login.errorPageTemplate
            null != shareCookieTTL ? shareCookieTTL.intValue() : defaultConfig.getCookieExpiry(),
            defaultConfig.isCookieForceHTTPS(),
            defaultConfig.isInsecure(),
            defaultConfig.isRedirectIPChangeAllowed(),
            defaultConfig.isDisableTrimLogin(),
            defaultConfig.isFormLoginWithoutAuthId(),
            defaultConfig.isRandomTokenEnabled(),
            defaultConfig.isCheckPunyCodeLoginString()
        );
    }

    /**
     * Gets the transientShareSessions
     *
     * @return The transientShareSessions
     */
    public boolean isTransientShareSessions() {
        return shareTransientSessions;
    }

    /**
     * (Re-)initializes the configuration.
     *
     * @param shareClientName The client name to use for automatically logged-in guest sessions
     * @param shareClientVersion The client version to use for automatically logged-in guest sessions
     * @param shareCookieTTL The TTL for the client cookies written when accessing a share, or <code>null</code> to fall back to the
     *                       default login configuration
     * @param shareTransientSessions <code>true</code> if guest sessions should be transient, <code>false</code>, otherwise
     */
    private void reinitialise(String shareClientName, String shareClientVersion, Integer shareCookieTTL, boolean shareTransientSessions) {
        this.shareClientName = shareClientName;
        this.shareClientVersion = shareClientVersion;
        this.shareCookieTTL = shareCookieTTL;
        this.shareTransientSessions = shareTransientSessions;
    }

    /**
     * (Re-)initializes the configuration.
     *
     * @param configService A reference to the configuration service
     * @throws OXException
     */
    private void reinitialise(ConfigurationService configService) throws OXException {
        /*
         * get share-specific login config overrides from configuration service
         */
        String shareClientName = configService.getProperty(ShareLoginProperty.CLIENT_NAME.getPropertyName(), ShareLoginProperty.CLIENT_NAME.getDefaultValue());
        String  shareClientVersion = configService.getProperty(ShareLoginProperty.CLIENT_VERSION.getPropertyName(), ShareLoginProperty.CLIENT_VERSION.getDefaultValue());
        String shareCookieTTLValue = configService.getProperty(ShareLoginProperty.COOKIE_TTL.getPropertyName());
        Integer shareCookieTTL = Strings.isEmpty(shareCookieTTLValue) ? null : Integer.valueOf(ConfigTools.parseTimespanSecs(shareCookieTTLValue));
        boolean shareTransientSessions = configService.getBoolProperty(ShareLoginProperty.TRANSIENT_SESSIONS.getPropertyName(), Boolean.parseBoolean(ShareLoginProperty.TRANSIENT_SESSIONS.getDefaultValue()));
        reinitialise(shareClientName, shareClientVersion, shareCookieTTL, shareTransientSessions);
    }

    /**
     * Adjusts the configured cookie TTL of the login configuration if the share is about to expire soon. In case the default cookie TTL
     * is overridden, a new login configuration instance is created holding the adjusted cookie TTL, otherwise the supplied login config
     * is returned as-is.
     *
     * @param loginConfig The login configuration
     * @param share The share to check
     * @return The possibly adjusted login configuration
     * @throws OXException
     */
    private static LoginConfiguration adjustCookieTTL(LoginConfiguration loginConfig, GuestInfo guest) {
        /*
         * determine maximum expiry of all contained share targets (if all targets are decorated with an expiry date)
         */
        Date effectiveExpiry = guest.getExpiryDate();
        if (null != effectiveExpiry) {
            int shareExpiry = (int) ((effectiveExpiry.getTime() - System.currentTimeMillis()) / 1000);
            if (0 <= shareExpiry && loginConfig.getCookieExpiry() > shareExpiry) {
                /*
                 * all share targets are about to expire earlier than default TTL, adjust login configuration
                 */
                return new LoginConfiguration(
                    loginConfig.getUiWebPath(),
                    loginConfig.getHashSource(),
                    loginConfig.getHttpAuthAutoLogin(),
                    loginConfig.getDefaultClient(),
                    loginConfig.getClientVersion(),
                    loginConfig.getErrorPageTemplate(),
                    shareExpiry,
                    loginConfig.isCookieForceHTTPS(),
                    loginConfig.isInsecure(),
                    loginConfig.isRedirectIPChangeAllowed(),
                    loginConfig.isDisableTrimLogin(),
                    loginConfig.isFormLoginWithoutAuthId(),
                    loginConfig.isRandomTokenEnabled(),
                    loginConfig.isCheckPunyCodeLoginString()
                );
            }
        }
        /*
         * use supplied login config as default fallback
         */
        return loginConfig;
    }

}
