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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.servlet.internal;

import java.util.Date;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.share.Share;

/**
 * {@link ShareLoginConfiguration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class ShareLoginConfiguration {

    private LoginConfiguration loginConfiguration;
    private boolean transientShareSessions;

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
     * @param share The share to get the login configuration for
     * @return The login configuration
     */
    public LoginConfiguration getLoginConfig(Share share) {
        return adjustCookieTTL(loginConfiguration, share);
    }

    /**
     * Gets the transientShareSessions
     *
     * @return The transientShareSessions
     */
    public boolean isTransientShareSessions() {
        return transientShareSessions;
    }

    /**
     * (Re-)initializes the configuration.
     *
     * @param configService A reference to the configuration service
     * @throws OXException
     */
    void reinitialise(ConfigurationService configService) throws OXException {
        this.loginConfiguration = init(LoginServlet.getLoginConfiguration(), configService);
        this.transientShareSessions = configService.getBoolProperty("com.openexchange.share.transientSessions", true);
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
    private static LoginConfiguration adjustCookieTTL(LoginConfiguration loginConfig, Share share) {
        Date expires = share.getExpiryDate();
        if (null != expires) {
            int shareExpiry = (int) ((expires.getTime() - System.currentTimeMillis()) / 1000);
            if (0 <= shareExpiry && loginConfig.getCookieExpiry() > shareExpiry) {
                /*
                 * share is about to expire earlier than default TTL, adjust login configuration
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
                    loginConfig.isRandomTokenEnabled()
                );
            }
        }
        /*
         * use supplied login config as default fallback
         */
        return loginConfig;
    }

    /**
     * Initializes the custom share login configuration using the supplied default config as template.
     *
     * @param defaultConfig The default login configuration
     * @param configService A reference to the config service
     * @return The custom share login configuration
     * @throws OXException
     */
    private static LoginConfiguration init(LoginConfiguration defaultConfig, ConfigurationService configService) throws OXException {
        /*
         * configure overrides
         */
        boolean sessiondAutoLogin = configService.getBoolProperty("com.openexchange.share.autoLogin", defaultConfig.isSessiondAutoLogin());
        String defaultClient = configService.getProperty("com.openexchange.share.clientName", "open-xchange-appsuite");
        String clientVersion = configService.getProperty("com.openexchange.share.clientVersion", "Share");
        String cookieTTL = configService.getProperty("com.openexchange.share.cookieTTL");
        int cookieExpiry = Strings.isEmpty(cookieTTL) ? defaultConfig.getCookieExpiry() : ConfigTools.parseTimespanSecs(cookieTTL);
        /*
         * construct custom login config from template
         */
        return new LoginConfiguration(
            defaultConfig.getUiWebPath(), // com.openexchange.UIWebPath
            sessiondAutoLogin,
            defaultConfig.getHashSource(), // com.openexchange.cookie.hash
            defaultConfig.getHttpAuthAutoLogin(),
            defaultClient,
            clientVersion,
            defaultConfig.getErrorPageTemplate(), // com.openexchange.ajax.login.errorPageTemplate
            cookieExpiry,
            defaultConfig.isCookieForceHTTPS(),
            defaultConfig.isInsecure(),
            defaultConfig.isIpCheck(),
            defaultConfig.getIpCheckWhitelist(),
            defaultConfig.isRedirectIPChangeAllowed(),
            defaultConfig.getRanges(),
            defaultConfig.isDisableTrimLogin(),
            defaultConfig.isFormLoginWithoutAuthId(),
            defaultConfig.isRandomTokenEnabled()
        );
    }

}
