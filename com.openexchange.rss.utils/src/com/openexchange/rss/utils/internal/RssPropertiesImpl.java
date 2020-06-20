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

package com.openexchange.rss.utils.internal;

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.openexchange.rss.utils.RssProperties;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RssPropertiesImpl} - Provides access to commonly used configuration properties for RSS communication.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class RssPropertiesImpl implements RssProperties {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RssProperties.class); // Yes, use interface type here
    }

    private final AtomicReference<HostList> blacklistedHosts;
    private final AtomicReference<Set<Integer>> allowedPorts;
    private final AtomicReference<Set<String>> allowedSchemes;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link RssPropertiesImpl}.
     *
     * @param services The service look-up
     */
    public RssPropertiesImpl(ServiceLookup services) {
        super();
        this.services = services;
        blacklistedHosts = new AtomicReference<HostList>(null);
        allowedPorts = new AtomicReference<Set<Integer>>(null);
        allowedSchemes = new AtomicReference<Set<String>>(null);
    }

    // ----------------------------------- Black-listed hosts -------------------------------------------------------------------------------

    /**
     * Gets the black-listed hosts.
     *
     * @return The black-listed hosts
     */
    private HostList blacklistedHosts() {
        HostList tmp = blacklistedHosts.get();
        if (null == tmp) {
            synchronized (RssPropertiesImpl.class) {
                tmp = blacklistedHosts.get();
                if (null == tmp) {
                    ConfigurationService service = services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        LoggerHolder.LOG.info("ConfigurationService not yet available. Use default value for '{}'.", PROP_HOST_BLACKLIST);
                        return HostList.valueOf(DEFAULT_HOST_BLACKLIST);
                    }
                    String prop = service.getProperty(PROP_HOST_BLACKLIST, DEFAULT_HOST_BLACKLIST);
                    if (Strings.isNotEmpty(prop)) {
                        prop = prop.trim();
                    }
                    tmp = HostList.valueOf(prop);
                    blacklistedHosts.set(tmp);
                }
            }
        }
        return tmp;
    }

    @Override
    public boolean isBlacklisted(String hostName) {
        return Strings.isEmpty(hostName) ? false : blacklistedHosts().contains(hostName.trim());
    }

    // ---------------------------------------- Allowed ports ------------------------------------------------------------------------------

    /**
     * Gets the allowed ports
     *
     * @return A {@link Set} of allowed ports
     */
    private Set<Integer> allowedPorts() {
        Set<Integer> tmp = allowedPorts.get();
        if (null == tmp) {
            synchronized (RssPropertiesImpl.class) {
                tmp = allowedPorts.get();
                if (null == tmp) {
                    ConfigurationService service = services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        LoggerHolder.LOG.info("ConfigurationService not yet available. Use default value for '{}'.", PROP_PORT_WHITELIST);
                        return toIntSet(DEFAULT_PORT_WHITELIST);
                    }
                    String prop = service.getProperty(PROP_PORT_WHITELIST, DEFAULT_PORT_WHITELIST);
                    if (Strings.isNotEmpty(prop)) {
                        prop = prop.trim();
                    }
                    if (Strings.isEmpty(prop)) {
                        tmp = Collections.<Integer> emptySet();
                    } else {
                        tmp = toIntSet(prop);
                    }
                    allowedPorts.set(tmp);
                }
            }
        }
        return tmp;
    }

    /**
     * Parses an comma separated list of port numbers to a set.
     *
     * @param concatenatedPorts The comma-separated list of port numbers
     * @return The set consisting of port numbers
     */
    private static Set<Integer> toIntSet(String concatenatedPorts) {
        if (Strings.isEmpty(concatenatedPorts)) {
            return Collections.emptySet();
        }

        String[] tokens = Strings.splitByComma(concatenatedPorts);
        if (tokens == null || tokens.length == 0) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<Integer> tmp = ImmutableSet.builderWithExpectedSize(tokens.length);
        for (String token : tokens) {
            if (Strings.isNotEmpty(token)) {
                try {
                    int port = Integer.parseInt(token.trim());
                    if (port > 0 && port <= 65535) {
                        tmp.add(I(port));
                    } else {
                        LoggerHolder.LOG.debug("Given value for property '{}' appears to hold a port number, which is outside of possible port range (0, 65535): {}.", PROP_PORT_WHITELIST, token);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                    LoggerHolder.LOG.debug("Given value for property '{}' appears to hold an invalid port number: {}.", PROP_PORT_WHITELIST, token, e);
                }
            }
        }
        return tmp.build();
    }

    @Override
    public boolean isAllowed(int port) {
        if (port < 0) {
            // Not set; always allow
            return true;
        }

        if (port > 65535) {
            // Invalid port
            return false;
        }

        Set<Integer> lAllowedPorts = allowedPorts();
        return lAllowedPorts.isEmpty() ? true : lAllowedPorts.contains(Integer.valueOf(port));
    }

    // ------------------------------------------ Allowed schemes --------------------------------------------------------------------------

    /**
     * Gets the {@link Set} of supported schemes
     *
     * @return The {@link Set} of schemes
     */
    private Set<String> supportedSchemes() {
        Set<String> tmp = allowedSchemes.get();
        if (null == tmp) {
            synchronized (RssPropertiesImpl.class) {
                tmp = allowedSchemes.get();
                if (null == tmp) {
                    ConfigurationService service = services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        LoggerHolder.LOG.info("ConfigurationService not yet available. Use default value for '{}'.", PROP_SCHEMES_WHITELIST);
                        return toSet(DEFAULT_SCHEMES_WHITELIST);
                    }
                    String prop = service.getProperty(PROP_SCHEMES_WHITELIST, DEFAULT_SCHEMES_WHITELIST);
                    tmp = toSet(prop);
                    allowedSchemes.set(tmp);
                }
            }
        }
        return tmp;
    }

    /**
     * Parses the given comma separated list of schemes into a set.
     *
     * @param concatenatedSchemes The comma-separated list of schemes
     * @return The set of schemes
     */
    private static Set<String> toSet(String concatenatedSchemes) {
        if (Strings.isEmpty(concatenatedSchemes)) {
            return Collections.emptySet();
        }

        String[] schemes = Strings.splitByComma(concatenatedSchemes);
        if (schemes == null || schemes.length == 0) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<String> tmp = ImmutableSet.builderWithExpectedSize(schemes.length);
        for (String scheme : schemes) {
            if (Strings.isNotEmpty(scheme)) {
                tmp.add(scheme.trim());
            }
        }
        return tmp.build();
    }

    @Override
    public boolean isAllowedScheme(String scheme) {
        Set<String> supportedSchemes = supportedSchemes();
        return supportedSchemes.isEmpty() ? true : supportedSchemes.contains(scheme);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean isDenied(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
            int port = uri.getPort();
            if (port < 0) {
                String scheme = Strings.asciiLowerCase(uri.getScheme());
                if (Strings.isEmpty(scheme)) {
                    // Assume HTTP as default
                    port = 80;
                } else {
                    scheme = scheme.trim();
                    if ("http".equals(scheme)) {
                        port = 80;
                    } else if ("https".equals(scheme)) {
                        port = 443;
                    }  else {
                        port = 80;
                    }
                }
            }
            return !isAllowed(port) || isBlacklisted(uri.getHost()) || !isAllowedScheme(uri.getScheme()) || !isValid(uri);
        } catch (URISyntaxException e) {
            LoggerHolder.LOG.debug("Given feed URL \"{}\" appears not to be valid.", uriString, e);
            return true;
        }
    }

    /**
     * Checks whether the given {@link URI} is valid or not
     *
     * @param uri The {@link URI} to check
     * @return <code>true</code> if the URI is valid, <code>false</code> otherwise
     */
    private static boolean isValid(URI uri) {
        try {
            InetAddress inetAddress = InetAddress.getByName(uri.getHost());
            if (inetAddress.isAnyLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
                LoggerHolder.LOG.debug("Given feed URL \"{}\" with destination IP {} appears not to be valid.", uri.toString(), inetAddress.getHostAddress());
                return false;
            }
        } catch (UnknownHostException e) {
            LoggerHolder.LOG.debug("Given feed URL \"{}\" appears not to be valid.", uri.toString(), e);
            return false;
        }
        return true;
    }
}
