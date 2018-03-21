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

package com.openexchange.chronos.provider.ical.properties;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.chronos.provider.ical.osgi.Services;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;

/**
 * {@link ICalCalendarProviderProperties}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public enum ICalCalendarProviderProperties implements Property {

    /**
     * Defines the default timeout interval for contacting the external resource after an error occurred.
     */
    retryAfterErrorInterval(3600L, ICalCalendarProviderProperties.PREFIX), // one hour
    /**
     * Defines the default refresh interval of the calendar feeds
     */
    refreshInterval(10080L, ICalCalendarProviderProperties.PREFIX), // one week
    /**
     * Defines the maximum possible number of connections used to access the calendar feed
     */
    maxConnections(1000, ICalCalendarProviderProperties.PREFIX),
    /**
     * Defines the maximum possible number of connections per host used to access the calendar feed
     */
    maxConnectionsPerRoute(100, ICalCalendarProviderProperties.PREFIX),
    /**
     * Defines the connection timeout
     */
    connectionTimeout(5000, ICalCalendarProviderProperties.PREFIX),
    /**
     * Defines the timeout on waiting to read data
     */
    socketReadTimeout(30000, ICalCalendarProviderProperties.PREFIX),
    /**
     * Defines hosts that are blacklisted which means that they are not allowed to be accessed
     */
    blacklistedHosts("127.0.0.1-127.255.255.255,localhost", ICalCalendarProviderProperties.PREFIX),
    /**
     * Defines schemes that are allowed to access a feed. All given schemes have to support port 80 or 443
     */
    schemes("http, https, webcal", ICalCalendarProviderProperties.PREFIX),
    /**
     * Defines the maximum size of an ICal file that will be allowed for processing. Feeds that exceed this limit cannot be subscribed
     */
    maxFileSize("5MB", ICalCalendarProviderProperties.PREFIX),

    ;

    private static final String EMPTY = "";
    private static final String PREFIX = "com.openexchange.calendar.ical.";
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initializes a new {@link ICalCalendarProviderProperties}.
     */
    private ICalCalendarProviderProperties() {
        this(EMPTY);
    }

    /**
     * Initializes a new {@link ICalCalendarProviderProperties}.
     *
     * @param defaultValue The default value of the property
     */
    private ICalCalendarProviderProperties(Object defaultValue) {
        this(defaultValue, PREFIX);
    }

    /**
     * Initializes a new {@link ICalCalendarProviderProperties}.
     *
     * @param defaultValue The default value of the property
     * @param optional Whether the property is optional
     */
    private ICalCalendarProviderProperties(Object defaultValue, String fqn) {
        this.defaultValue = defaultValue;
        this.fqn = fqn;
    }

    /**
     * Returns the fully qualified name of the property
     *
     * @return the fully qualified name of the property
     */
    @Override
    public String getFQPropertyName() {
        return fqn + name();
    }

    /**
     * Returns the default value of this property
     *
     * @return the default value of this property
     */
    @Override
    public <T extends Object> T getDefaultValue(Class<T> cls) {
        if (defaultValue.getClass().isAssignableFrom(cls)) {
            return cls.cast(defaultValue);
        }
        throw new IllegalArgumentException("The object cannot be converted to the specified type '" + cls.getCanonicalName() + "'");
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    private static volatile Long configuredAllowedFeedSize;

    public static long allowedFeedSize() {
        Long tmp = configuredAllowedFeedSize;
        if (null == tmp) {
            synchronized (ICalCalendarProviderProperties.class) {
                tmp = configuredAllowedFeedSize;
                if (null == tmp) {
                    LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
                    String prop = service.getProperty(maxFileSize);
                    if (Strings.isNotEmpty(prop)) {
                        prop = prop.trim();
                    }
                    try {
                        tmp = new Long(ConfigTools.parseBytes(prop));
                    } catch (NumberFormatException e) {
                        org.slf4j.LoggerFactory.getLogger(ICalCalendarProviderProperties.class).warn("Unable to parse value {} for property 'com.openexchange.chronos.provider.ical.maxFileSize'. Will use default 5MB.", e);
                        tmp = new Long(ConfigTools.parseBytes("5MB"));
                    }
                    configuredAllowedFeedSize = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile HostList configuredBlacklistedHosts;

    private static HostList blacklistedHosts() {
        HostList tmp = configuredBlacklistedHosts;
        if (null == tmp) {
            synchronized (ICalCalendarProviderProperties.class) {
                tmp = configuredBlacklistedHosts;
                if (null == tmp) {
                    LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
                    String prop = service.getProperty(blacklistedHosts);
                    if (Strings.isNotEmpty(prop)) {
                        prop = prop.trim();
                    }
                    tmp = HostList.valueOf(prop);
                    configuredBlacklistedHosts = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Checks if specified host name is black-listed.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if black-listed; otherwise <code>false</code>
     */
    public static boolean isBlacklisted(String hostName) {
        if (Strings.isEmpty(hostName)) {
            return false;
        }
        return blacklistedHosts().contains(hostName);
    }

    /**
     * Checks if specified host name and port are denied to connect against.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param scheme The url scheme; might be something like 'http', 'https', ...
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @param port The port number
     * @return <code>true</code> if denied; otherwise <code>false</code>
     */
    private static boolean isDenied(String scheme, String hostName, int port) {
        return isBlacklisted(hostName) || !isAllowedScheme(scheme);
    }

    public static boolean isDenied(URI uri) {
        return isDenied(uri.getScheme(), uri.getHost(), uri.getPort());
    }

    private static volatile Set<String> configuredSchemes;

    public static Set<String> supportedSchemes() {
        Set<String> tmp = configuredSchemes;
        if (null == tmp) {
            synchronized (ICalCalendarProviderProperties.class) {
                tmp = configuredSchemes;
                if (null == tmp) {
                    LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
                    String prop = service.getProperty(schemes);
                    tmp = toSet(prop);
                    configuredSchemes = tmp;
                }
            }
        }
        return tmp;
    }

    private static Set<String> toSet(String concatenatedSchemes) {
        if (Strings.isEmpty(concatenatedSchemes)) {
            return Collections.emptySet();
        }
        String[] schemes = Strings.splitByComma(concatenatedSchemes);
        if (schemes == null || schemes.length == 0) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(schemes));
    }

    private static boolean isAllowedScheme(String scheme) {
        Set<String> supportedSchemes = supportedSchemes();
        return supportedSchemes.isEmpty() ? true : supportedSchemes.contains(scheme);
    }

    protected static void reset() {
        configuredSchemes = null;
        configuredBlacklistedHosts = null;
        configuredAllowedFeedSize = null;
    }
}
