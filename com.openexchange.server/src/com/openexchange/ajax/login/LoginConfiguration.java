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

import java.util.Set;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;

/**
 * Object to store the configuration parameters for the different login process mechanisms.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class LoginConfiguration {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoginConfiguration.class);

    private final String uiWebPath;
    private final boolean sessiondAutoLogin;
    private final CookieHashSource hashSource;
    private final String httpAuthAutoLogin;
    private final String defaultClient;
    private final String clientVersion;
    private final String errorPageTemplate;
    private final int cookieExpiry;
    private final boolean insecure;
    private final boolean cookieForceHTTPS;
    private final boolean redirectIPChangeAllowed;
    private final boolean disableTrimLogin;
    private final boolean formLoginWithoutAuthId;
    private final boolean isRandomTokenEnabled;
    private final boolean checkPunyCodeLoginString;

    public LoginConfiguration(String uiWebPath, boolean sessiondAutoLogin, CookieHashSource hashSource, String httpAuthAutoLogin, String defaultClient, String clientVersion, String errorPageTemplate, int cookieExpiry, boolean cookieForceHTTPS, boolean insecure, boolean redirectIPChangeAllowed, boolean disableTrimLogin, boolean formLoginWithoutAuthId, boolean isRandomTokenEnabled, boolean checkPunyCodeLoginString) {
        super();
        this.uiWebPath = uiWebPath;
        this.sessiondAutoLogin = sessiondAutoLogin;
        this.hashSource = hashSource;
        this.httpAuthAutoLogin = httpAuthAutoLogin;
        this.defaultClient = defaultClient;
        this.clientVersion = clientVersion;
        this.errorPageTemplate = errorPageTemplate;
        this.cookieExpiry = cookieExpiry;
        this.cookieForceHTTPS = cookieForceHTTPS;
        this.insecure = insecure;
        this.redirectIPChangeAllowed = redirectIPChangeAllowed;
        this.disableTrimLogin = disableTrimLogin;
        this.formLoginWithoutAuthId = formLoginWithoutAuthId;
        this.isRandomTokenEnabled = isRandomTokenEnabled;
        this.checkPunyCodeLoginString = checkPunyCodeLoginString;
    }

    public String getUiWebPath() {
        return uiWebPath;
    }

    /**
     * Checks if auto-login is enabled.
     * <p>
     * This routine tries to determine the host name associated with calling thread in order to check reliably using {@link #isSessiondAutoLogin(String)} method.
     * If host name cannot be determined, global auto-login property is considered.
     *
     * @return <code>true</code> if auto-login is enabled; otherwise <code>false</code>
     */
    public boolean isSessiondAutoLogin() {
        String hostname = LogProperties.getHostName();
        if (hostname != null) {
            return isSessiondAutoLogin(hostname);
        }
        return sessiondAutoLogin;
    }

    private static final Capability CAPABILITY_AUTOLOGIN = new Capability("autologin");

    /**
     * Checks if auto-login is enabled for given host name
     *
     * @param hostName The host name to check for
     * @return <code>true</code> if auto-login is enabled; otherwise <code>false</code>
     */
    public boolean isSessiondAutoLogin(String hostName) {
        return isSessiondAutoLogin(hostName, null);
    }

    /**
     * Checks if auto-login is enabled for given host name
     *
     * @param hostName The host name to check for
     * @param optSession The optional session to use
     * @return <code>true</code> if auto-login is enabled; otherwise <code>false</code>
     */
    public boolean isSessiondAutoLogin(String hostName, Session optSession) {
        try {
            if (null != hostName) {
                LogProperties.put(LogProperties.Name.HOSTNAME, hostName);
            }

            CapabilityService capabilityService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
            if (null == capabilityService) {
                LOGGER.warn("Missing capability service. Unable to reliably check auto-login capability for host {}. Using default '{}' as fall-back for auto-login (as configured through \"{}\" property", hostName, sessiondAutoLogin, ConfigurationProperty.SESSIOND_AUTOLOGIN.getPropertyName());
                return sessiondAutoLogin;
            }

            Set<Capability> capabilities;
            {
                Session ses = null == optSession ? ThreadLocalSessionHolder.getInstance().getSessionObject() : optSession;
                if (null == ses) {
                    capabilities = capabilityService.getCapabilities(getUserId(), getContextId(), true, true).asSet();
                } else {
                    capabilities = capabilityService.getCapabilities(ses, true).asSet();
                }
            }

            if (null != capabilities) {
                return capabilities.contains(CAPABILITY_AUTOLOGIN);
            }

            LOGGER.warn("Retrieved server configuration for host {} misses the capabilities set. Using default '{}' as fall-back (as configured through \"{}\" property", hostName, sessiondAutoLogin, ConfigurationProperty.SESSIOND_AUTOLOGIN.getPropertyName());
        } catch (OXException e) {
            // fallback to default
            LOGGER.warn("Failed to retrieve server configuration for host {}. Using default '{}' as fall-back (as configured through \"{}\" property", hostName, sessiondAutoLogin, ConfigurationProperty.SESSIOND_AUTOLOGIN.getPropertyName(), e);
        }
        return sessiondAutoLogin;
    }

    public CookieHashSource getHashSource() {
        return hashSource;
    }

    public String getHttpAuthAutoLogin() {
        return httpAuthAutoLogin;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getErrorPageTemplate() {
        return errorPageTemplate;
    }

    public int getCookieExpiry() {
        return cookieExpiry;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public boolean isCookieForceHTTPS() {
        return cookieForceHTTPS;
    }

    public boolean isRedirectIPChangeAllowed() {
        return redirectIPChangeAllowed;
    }

    public boolean isDisableTrimLogin() {
        return disableTrimLogin;
    }

    public boolean isFormLoginWithoutAuthId() {
        return formLoginWithoutAuthId;
    }

    public boolean isRandomTokenEnabled() {
        return isRandomTokenEnabled;
    }

    public boolean isCheckPunyCodeLoginString() {
        return checkPunyCodeLoginString;
    }


    // ----------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Retrieves the context identifier from the {@link LogProperties}
     *
     * @return the context identifier from the LogProperties
     */
    private int getContextId() {
        return getLogPropertyValue(LogProperties.Name.SESSION_CONTEXT_ID);
    }

    /**
     * Retrieves the user identifier from the {@link LogProperties}
     *
     * @return the user identifier from the {@link LogProperties}
     */
    private int getUserId() {
        return getLogPropertyValue(LogProperties.Name.SESSION_USER_ID);
    }

    /**
     * Retrieves value of the specified property from the {@link LogProperties}
     *
     * @param name The log property's name
     * @return the property's value
     */
    private int getLogPropertyValue(LogProperties.Name name) {
        return Tools.getUnsignedInteger(LogProperties.get(name));
    }

}