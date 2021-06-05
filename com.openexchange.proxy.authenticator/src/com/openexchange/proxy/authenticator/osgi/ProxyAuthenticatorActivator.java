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

package com.openexchange.proxy.authenticator.osgi;

import java.lang.reflect.Field;
import java.net.Authenticator;
import org.slf4j.Logger;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.proxy.authenticator.DefaultPasswordAuthenticationProvider;
import com.openexchange.proxy.authenticator.PasswordAuthenticationProvider;
import com.openexchange.proxy.authenticator.impl.ProxyAuthenticator;

/**
 * {@link ProxyAuthenticatorActivator} sets the default proxy authenticator and prevent any further changes with the help of a {@link SecurityManager}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ProxyAuthenticatorActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ProxyAuthenticatorActivator.class);
    private Authenticator previousAuthenticator = null;

    /**
     * Initializes a new {@link ProxyAuthenticatorActivator}.
     */
    public ProxyAuthenticatorActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {};
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());

        // Set new default Authenticator
        ProxyAuthenticator authenticator = new ProxyAuthenticator(context);
        track(PasswordAuthenticationProvider.class, authenticator);
        Authenticator.setDefault(authenticator);

        createDefaultAuthenticationproviders();

        LOG.info("Bundle {} successfully started.", context.getBundle().getSymbolicName());

    }

    private void createDefaultAuthenticationproviders() {
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
        if (validHttpProxySettings || validHttpsProxySettings) {

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
                this.previousAuthenticator = previousAuthenticator;
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
    protected synchronized void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProxyAuthenticatorActivator.class);
        logger.info("Stopping bundle {}", this.context.getBundle().getSymbolicName());

        // Restore previous default Authenticator
        Authenticator previousAuthenticator = this.previousAuthenticator;
        if (null != previousAuthenticator) {
            this.previousAuthenticator = null;
            Authenticator.setDefault(previousAuthenticator);
        }

        super.stopBundle();
        logger.info("Bundle {} successfully stopped", context.getBundle().getSymbolicName());
    }


}
