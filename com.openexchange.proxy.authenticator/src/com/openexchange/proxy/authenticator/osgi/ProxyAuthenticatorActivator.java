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

package com.openexchange.proxy.authenticator.osgi;

import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.NetPermission;
import java.security.Permission;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.proxy.authenticator.DefaultPasswordAuthenticationProvider;
import com.openexchange.proxy.authenticator.PasswordAuthenticationProvider;
import com.openexchange.proxy.authenticator.impl.ProxyAuthenticator;
import com.openexchange.proxy.authenticator.impl.ReportClientUtil;

/**
 * {@link ProxyAuthenticatorActivator} sets the default proxy authenticator and prevent any further changes with the help of a {@link SecurityManager}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ProxyAuthenticatorActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ProxyAuthenticatorActivator.class);

    /**
     * Initializes a new {@link ProxyAuthenticatorActivator}.
     */
    public ProxyAuthenticatorActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {

        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());

        // Set new default Authenticator
        ProxyAuthenticator authenticator = new ProxyAuthenticator(context);
        track(PasswordAuthenticationProvider.class, authenticator);
        Authenticator.setDefault(authenticator);

        setSecurityManager();
        createDefaultAuthenticationproviders();

        LOG.info("Bundle {} successfully started.", context.getBundle().getSymbolicName());

    }

    private void createDefaultAuthenticationproviders() {
        // Load report client configuration which is going to be used as fallback values
        ConfigurationService service = getService(ConfigurationService.class);
        PasswordAuthenticationProvider reportProvider = null;
        if(service == null) {
            LOG.error("ConfigurationService not available. Unable to properly init proxy configuration for report clients.");
        } else {
            reportProvider = ReportClientUtil.getProvider(service);
        }

        // Loads all relevant JRE system properties and setups an Authenticator in case user-name and password are set.
        String httpHost = System.getProperty("http.proxyHost");
        int httpPort = Tools.getUnsignedInteger(System.getProperty("http.proxyPort", "80").trim());
        String httpUser = System.getProperty("http.proxyUser");
        String httpPassword = System.getProperty("http.proxyPassword");

        String httpsHost = System.getProperty("https.proxyHost");
        int httpsPort = Tools.getUnsignedInteger(System.getProperty("https.proxyPort", "443").trim());
        String httpsUser = System.getProperty("https.proxyUser");
        String httpsPassword = System.getProperty("https.proxyPassword");

        boolean validHttpProxySettings = validProxySettings(httpHost, httpPort, httpUser, httpPassword);
        boolean validHttpsProxySettings = validProxySettings(httpsHost, httpsPort, httpsUser, httpsPassword);
        if (validHttpProxySettings || validHttpsProxySettings || reportProvider != null) {

            checkForOldAuthenticator();

            // Password authentication providers for HTTP/HTTPS
            // HTTP
            if (validHttpProxySettings) {
                registerService(PasswordAuthenticationProvider.class, new DefaultPasswordAuthenticationProvider("http", httpHost, httpPort, httpUser, httpPassword));
            }
            // HTTPS
            if (validHttpsProxySettings) {
                registerService(PasswordAuthenticationProvider.class, new DefaultPasswordAuthenticationProvider("https", httpsHost, httpsPort, httpsUser, httpsPassword));
            }

            // Register reportclient provider as fallback to ensure compatibility with old config
            if (reportProvider != null) {
                registerService(PasswordAuthenticationProvider.class, reportProvider);
            }
        }
    }

    /**
     * Checks if a proxy authenticator has already been set and prints a warning.
     *
     */
    private void checkForOldAuthenticator() {
        try {
            Field theAuthenticatorField = Authenticator.class.getDeclaredField("theAuthenticator");
            theAuthenticatorField.setAccessible(true);
            Authenticator previousAuthenticator = (Authenticator) theAuthenticatorField.get(null);
            if (previousAuthenticator != null) {
                LOG.warn("There is already a proxy authenticator defined with the name {}. This authenticator will be overwritten.", previousAuthenticator.getClass().getName());
            }
        } catch (NoSuchFieldException e) {
            LOG.warn("Failed to look-up \"theAuthenticator\" field in class {}. Incompatible JRE?", Authenticator.class.getName());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOG.warn("Failed to access \"theAuthenticator\" field in class {}. Incompatible JRE?", Authenticator.class.getName());
        }
    }

    /**
     * Checks if the given proxy configuration is valid
     *
     * @param host The host
     * @param port The port
     * @param user The proxy user
     * @param password the proxy password
     * @return true if it is valid, false otherwise
     */
    private boolean validProxySettings(String host, int port, String user, String password) {
        return Strings.isNotEmpty(host) && port > 0 && Strings.isNotEmpty(user) && Strings.isNotEmpty(password);
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProxyAuthenticatorActivator.class);
        logger.info("Stopping bundle {}", this.context.getBundle().getSymbolicName());

        // Restore previous default Authenticator
        removeSecurityManager();
        Authenticator.setDefault(null);

        logger.info("Bundle {} successfully stopped", context.getBundle().getSymbolicName());
    }

    SecurityManager oldSecurityManager = null;

    /**
     * Sets a java {@link SecurityManager} which prevents that the authenticator will be overwritten.
     */
    private void setSecurityManager() {
        oldSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {

            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof NetPermission) {
                    if (perm.getName().equals("setDefaultAuthenticator")) {
                        throw new SecurityException("Setting the default authenticator twice is not allowed.");
                    }
                }
                if (oldSecurityManager != null) {
                    oldSecurityManager.checkPermission(perm);
                }
            }
        });
    }

    /**
     * Sets the java {@link SecurityManager} back to its previous value.
     */
    private void removeSecurityManager() {
        System.setSecurityManager(oldSecurityManager);
        oldSecurityManager = null;
    }

}
