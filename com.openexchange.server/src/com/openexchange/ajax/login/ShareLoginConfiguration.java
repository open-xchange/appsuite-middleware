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
         * <code>true</code> if auto-login for shares is enabled, <code>false</code> if not, <code>null</code> to fall back to the
         * default login configuration
         */
        AUTO_LOGIN("com.openexchange.share.autoLogin", null),

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

    private Boolean shareAutoLogin;
    private String shareClientName;
    private String shareClientVersion;
    private Integer shareCookieTTL;
    private boolean shareTransientSessions;

    /**
     * Initializes a new {@link ShareLoginConfiguration}.
     *
     * @param shareAutoLogin <code>true</code> if auto-login for shares is enabled, <code>false</code> if not, <code>null</code> to fall
     *                       back to the default login configuration
     * @param shareClientName The client name to use for automatically logged-in guest sessions
     * @param shareClientVersion The client version to use for automatically logged-in guest sessions
     * @param shareCookieTTL The TTL for the client cookies written when accessing a share, or <code>null</code> to fall back to the
     *                       default login configuration
     * @param shareTransientSessions <code>true</code> if guest sessions should be transient, <code>false</code>, otherwise
     */
    public ShareLoginConfiguration(Boolean shareAutoLogin, String shareClientName, String shareClientVersion, Integer shareCookieTTL, boolean shareTransientSessions) {
        super();
        reinitialise(shareAutoLogin, shareClientName, shareClientVersion, shareCookieTTL, shareTransientSessions);
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
            null != shareAutoLogin ? shareAutoLogin.booleanValue() : defaultConfig.isSessiondAutoLogin(),
            defaultConfig.getHashSource(), // com.openexchange.cookie.hash
            defaultConfig.getHttpAuthAutoLogin(),
            null != shareClientName ? shareClientName : defaultConfig.getDefaultClient(),
            null != shareClientVersion ? shareClientVersion : defaultConfig.getClientVersion(),
            defaultConfig.getErrorPageTemplate(), // com.openexchange.ajax.login.errorPageTemplate
            null != shareCookieTTL ? shareCookieTTL.intValue() : defaultConfig.getCookieExpiry(),
            defaultConfig.isCookieForceHTTPS(),
            defaultConfig.isInsecure(),
            defaultConfig.isIpCheck(),
            defaultConfig.getIpCheckWhitelist(),
            defaultConfig.isRedirectIPChangeAllowed(),
            defaultConfig.getRanges(),
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
     * @param shareAutoLogin <code>true</code> if auto-login for shares is enabled, <code>false</code> if not, <code>null</code> to fall
     *                       back to the default login configuration
     * @param shareClientName The client name to use for automatically logged-in guest sessions
     * @param shareClientVersion The client version to use for automatically logged-in guest sessions
     * @param shareCookieTTL The TTL for the client cookies written when accessing a share, or <code>null</code> to fall back to the
     *                       default login configuration
     * @param shareTransientSessions <code>true</code> if guest sessions should be transient, <code>false</code>, otherwise
     */
    private void reinitialise(Boolean shareAutoLogin, String shareClientName, String shareClientVersion, Integer shareCookieTTL, boolean shareTransientSessions) {
        this.shareAutoLogin = shareAutoLogin;
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
        String shareAutoLoginValue = configService.getProperty(ShareLoginProperty.AUTO_LOGIN.getPropertyName());
        Boolean shareAutoLogin = Strings.isEmpty(shareAutoLoginValue) ? null : Boolean.valueOf(shareAutoLoginValue);
        String shareClientName = configService.getProperty(ShareLoginProperty.CLIENT_NAME.getPropertyName(), ShareLoginProperty.CLIENT_NAME.getDefaultValue());
        String  shareClientVersion = configService.getProperty(ShareLoginProperty.CLIENT_VERSION.getPropertyName(), ShareLoginProperty.CLIENT_VERSION.getDefaultValue());
        String shareCookieTTLValue = configService.getProperty(ShareLoginProperty.COOKIE_TTL.getPropertyName());
        Integer shareCookieTTL = Strings.isEmpty(shareCookieTTLValue) ? null : Integer.valueOf(ConfigTools.parseTimespanSecs(shareCookieTTLValue));
        boolean shareTransientSessions = configService.getBoolProperty(ShareLoginProperty.TRANSIENT_SESSIONS.getPropertyName(), Boolean.valueOf(ShareLoginProperty.TRANSIENT_SESSIONS.getDefaultValue()));
        reinitialise(shareAutoLogin, shareClientName, shareClientVersion, shareCookieTTL, shareTransientSessions);
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
                    loginConfig.isSessiondAutoLogin(),
                    loginConfig.getHashSource(),
                    loginConfig.getHttpAuthAutoLogin(),
                    loginConfig.getDefaultClient(),
                    loginConfig.getClientVersion(),
                    loginConfig.getErrorPageTemplate(),
                    shareExpiry,
                    loginConfig.isCookieForceHTTPS(),
                    loginConfig.isInsecure(),
                    loginConfig.isIpCheck(),
                    loginConfig.getIpCheckWhitelist(),
                    loginConfig.isRedirectIPChangeAllowed(),
                    loginConfig.getRanges(),
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
