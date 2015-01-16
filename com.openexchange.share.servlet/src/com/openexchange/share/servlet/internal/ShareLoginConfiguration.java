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
import java.util.List;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareTarget;

/**
 * {@link ShareLoginConfiguration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareLoginConfiguration {

    private Boolean shareAutoLogin;
    private String shareClientName;
    private String shareClientVersion;
    private Integer shareCookieTTL;
    private boolean shareTransientSessions;
    private byte[] cookieHashSalt;

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
     * @throws OXException
     */
    public LoginConfiguration getLoginConfig(GuestShare share) throws OXException {
        return adjustCookieTTL(getLoginConfig(), share);
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
            defaultConfig.isRandomTokenEnabled()
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
     * Gets the cookie hash salt as configured via <code>com.openexchange.cookie.hash.salt</code>.
     *
     * @return The cookie hash salt as byte array
     */
    public byte[] getCookieHashSalt() {
        return cookieHashSalt;
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
        String shareAutoLogin = configService.getProperty("com.openexchange.share.autoLogin");
        if (false == Strings.isEmpty(shareAutoLogin)) {
            this.shareAutoLogin = Boolean.valueOf(shareAutoLogin);
        }
        this.shareClientName = configService.getProperty("com.openexchange.share.clientName", "open-xchange-appsuite");
        this.shareClientVersion = configService.getProperty("com.openexchange.share.clientVersion", "Share");
        String shareCookieTTL = configService.getProperty("com.openexchange.share.cookieTTL");
        if (false == Strings.isEmpty(shareCookieTTL)) {
            this.shareCookieTTL = Integer.valueOf(ConfigTools.parseTimespanSecs(shareCookieTTL));
        }
        this.shareTransientSessions = configService.getBoolProperty("com.openexchange.share.transientSessions", true);
        this.cookieHashSalt = configService.getProperty("com.openexchange.cookie.hash.salt", "replaceMe1234567890").getBytes();
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
    private static LoginConfiguration adjustCookieTTL(LoginConfiguration loginConfig, GuestShare share) {
        /*
         * determine maximum expiry of all contained share targets (if all targets are decorated with an expiry date)
         */
        Date effectiveExpiry = getEffectiveExpiryDate(share.getTargets());
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
     * Gets the maximum expiry date of all supplied shared targets in case all of them have an expiry date defined.
     *
     * @param targets The targets to get the maximum expiry for
     * @return The maxium expiry date, or <code>null</code> if at least one of the targets has no expiry date set
     */
    private static Date getEffectiveExpiryDate(List<ShareTarget> targets) {
        Date effectiveExpiry = null;
        if (null != targets && 0 < targets.size()) {
            for (ShareTarget target : targets) {
                Date targetExpiry = target.getExpiryDate();
                if (null == targetExpiry) {
                    return null;
                }
                if (null == effectiveExpiry || targetExpiry.after(effectiveExpiry)) {
                    effectiveExpiry = targetExpiry;
                }
            }
        }
        return effectiveExpiry;
    }

}
