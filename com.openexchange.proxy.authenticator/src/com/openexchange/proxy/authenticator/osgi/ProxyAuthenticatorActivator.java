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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;

/**
 * {@link ProxyAuthenticatorActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ProxyAuthenticatorActivator implements BundleActivator {

    private static interface PasswordAuthenticationProvider {

        PasswordAuthentication getPasswordAuthentication(String requestingHost, int requestingPort);
    }

    // ------------------------------------------------------------------------------------------------

    private Authenticator previousAuthenticator;

    /**
     * Initializes a new {@link ProxyAuthenticatorActivator}.
     */
    public ProxyAuthenticatorActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProxyAuthenticatorActivator.class);
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());

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
            // Remember previous default Authenticator (if any)
            final Authenticator previousAuthenticator;
            final Method getPasswordAuthenticationMethod;
            {
                Authenticator a = null;
                Method m = null;
                try {
                    Field theAuthenticatorField = Authenticator.class.getDeclaredField("theAuthenticator");
                    theAuthenticatorField.setAccessible(true);
                    a = (Authenticator) theAuthenticatorField.get(null);
                    this.previousAuthenticator = a;

                    if (a != null) {
                        m = Authenticator.class.getDeclaredMethod("getPasswordAuthentication", new Class[0]);
                        m.setAccessible(true);
                    }
                } catch (NoSuchFieldException e) {
                    logger.warn("Failed to look-up \"theAuthenticator\" field in class {}. Incompatible JRE?", Authenticator.class.getName());
                } catch (NoSuchMethodException e) {
                    logger.warn("Failed to look-up \"getPasswordAuthentication\" method in class {}. Incompatible JRE?", Authenticator.class.getName());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    logger.warn("Failed to access \"theAuthenticator\" field in class {}. Incompatible JRE?", Authenticator.class.getName());
                }
                previousAuthenticator = a;
                getPasswordAuthenticationMethod = m;
            }

            // Password authentication provides for HTTP/HTTPS
            final Map<String, PasswordAuthenticationProvider> providers;
            {
                ImmutableMap.Builder<String, PasswordAuthenticationProvider> builder = ImmutableMap.builder();
                // HTTP
                if (validHttpProxySettings) {
                    builder.put("http", new PasswordAuthenticationProvider() {

                        @Override
                        public PasswordAuthentication getPasswordAuthentication(String requestingHost, int requestingPort) {
                            if (httpPort == requestingPort && httpHost.equalsIgnoreCase(requestingHost)) {
                                // Seems to be OK.
                                return new PasswordAuthentication(httpUser, httpPassword.toCharArray());
                            }

                            // Does not match configured host/port for HTTP proxy
                            return null;
                        }
                    });
                }
                // HTTPS
                if (validHttpsProxySettings) {
                    builder.put("https", new PasswordAuthenticationProvider() {

                        @Override
                        public PasswordAuthentication getPasswordAuthentication(String requestingHost, int requestingPort) {
                            if (httpsPort == requestingPort && httpsHost.equalsIgnoreCase(requestingHost)) {
                                // Seems to be OK.
                                return new PasswordAuthentication(httpsUser, httpsPassword.toCharArray());
                            }

                            // Does not match configured host/port for HTTPS proxy
                            return null;
                        }
                    });
                }
                providers = builder.build();
            }

            // Set new default Authenticator
            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestorType() == RequestorType.PROXY) {
                        // Entity-requesting authentication is a HTTP proxy server
                        String protocol = Strings.asciiLowerCase(getRequestingProtocol());
                        if (null != protocol) {
                            // Optionally followed by "/version", where version is a version number.
                            int pos = protocol.lastIndexOf('/');
                            PasswordAuthenticationProvider provider = providers.get(pos < 0 ? protocol : protocol.substring(0, pos));
                            PasswordAuthentication passwordAuthentication;
                            if (null != provider && (passwordAuthentication = provider.getPasswordAuthentication(getRequestingHost(), getRequestingPort())) != null) {
                                // Match...
                                return passwordAuthentication;
                            }
                        }
                    }

                    // Either invoke previous authenticator (if any) or return super implementation
                    if (null == previousAuthenticator || null == getPasswordAuthenticationMethod) {
                        return super.getPasswordAuthentication();
                    }

                    // Delegate to previous default authenticator
                    try {
                        Object result = getPasswordAuthenticationMethod.invoke(previousAuthenticator, new Object[0]);
                        return null == result ? null : (PasswordAuthentication) result;
                    } catch (IllegalAccessException e) {
                        // Should not occur since 'setAccessible(true)' has been invoked
                        return null;
                    } catch (IllegalArgumentException e) {
                        // Should not occur
                        return null;
                    } catch (InvocationTargetException e) {
                        Throwable t = e.getCause();
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        } else if (t instanceof Error) {
                            throw (Error) t;
                        } else {
                            throw new IllegalStateException("Not unchecked", t);
                        }
                    }
                }
            });
            if (validHttpProxySettings) {
                if (validHttpsProxySettings) {
                    logger.info("Bundle {} successfully started. Injected authenticator for HTTP and HTTPS proxy server", context.getBundle().getSymbolicName());
                }
                logger.info("Bundle {} successfully started. Injected authenticator for HTTP proxy server", context.getBundle().getSymbolicName());
            } else {
                logger.info("Bundle {} successfully started. Injected authenticator for HTTPS proxy server", context.getBundle().getSymbolicName());
            }
        } else {
            logger.info("Bundle {} successfully started. Using no authenticator for HTTP/HTTPS proxy server", context.getBundle().getSymbolicName());
        }
    }

    private boolean validProxySettings(String httpHost, int httpPort, String httpUser, String httpPassword) {
        return Strings.isNotEmpty(httpHost) && httpPort > 0 && Strings.isNotEmpty(httpUser) && Strings.isNotEmpty(httpPassword);
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProxyAuthenticatorActivator.class);
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());

        // Restore previous default Authenticator
        Authenticator.setDefault(previousAuthenticator);
        previousAuthenticator= null;

        logger.info("Bundle {} successfully stopped", context.getBundle().getSymbolicName());
    }

}
