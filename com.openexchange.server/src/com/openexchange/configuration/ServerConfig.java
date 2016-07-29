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

package com.openexchange.configuration;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.File;
import java.util.Properties;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.calendar.CalendarConfig;
import com.openexchange.groupware.infostore.InfostoreConfig;
import com.openexchange.groupware.notify.NotificationConfig;
import com.openexchange.java.Strings;

/**
 * This class handles the configuration parameters read from the configuration property file server.properties.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ServerConfig implements Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServerConfig.class);

    /**
     * Singleton object.
     */
    private static final ServerConfig SINGLETON = new ServerConfig();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ServerConfig getInstance() {
        return SINGLETON;
    }

    /**
     * Name of the properties file.
     */
    private static final String FILENAME = "server.properties";

    // ------------------------------------------------------------------------------ //

    private final Properties props;
    private String uploadDirectory = "/tmp/";
    private int maxFileUploadSize = 10000;
    private int maxUploadIdleTimeMillis = 300000;
    private boolean prefetchEnabled;
    private String defaultEncoding;
    private int jmxPort;
    private String jmxBindAddress;
    private Boolean checkIP;
    private String ipMaskV4;
    private String ipMaskV6;
    private final ClientWhitelist clientWhitelist;
    private String uiWebPath;
    private int cookieTTL;
    private boolean cookieHttpOnly;
    private int maxBodySize;
    private int defaultMaxConcurrentAJAXRequests;

    private ServerConfig() {
        super();
        props = new Properties();
        clientWhitelist = new ClientWhitelist();
    }

    @Override
    public void reloadConfiguration(final ConfigurationService configService) {
        final Properties newProps = configService.getFile(FILENAME);
        this.props.clear();
        if (null == newProps) {
            LOG.info("Configuration file {} is missing. Using defaults.", FILENAME);
        } else {
            this.props.putAll(newProps);
            LOG.info("Read configuration file {}.", FILENAME);
        }
        reinit();

        try {
            final AttachmentConfig attachmentConfig = AttachmentConfig.getInstance();
            attachmentConfig.stop();
            attachmentConfig.start();
        } catch (final Exception e) {
            LOG.warn("Could not reload attachment configuration.", e);
        }

        try {
            final CalendarConfig calendarConfig = CalendarConfig.getInstance();
            calendarConfig.stop();
            calendarConfig.start();
        } catch (final Exception e) {
            LOG.warn("Could not reload calendar configuration.", e);
        }

        try {
            final InfostoreConfig infostoreConfig = InfostoreConfig.getInstance();
            infostoreConfig.stop();
            infostoreConfig.start();
        } catch (final Exception e) {
            LOG.warn("Could not reload infostore configuration.", e);
        }

        try {
            final NotificationConfig notificationConfig = NotificationConfig.getInstance();
            notificationConfig.stop();
            notificationConfig.start();
        } catch (final Exception e) {
            LOG.warn("Could not reload infostore configuration.", e);
        }

        LoginWriter.invalidateRandomTokenEnabled();
    }

    public void initialize(final ConfigurationService confService) {
        final Properties newProps = confService.getFile(FILENAME);
        if (null == newProps) {
            LOG.info("Configuration file {} is missing. Using defaults.", FILENAME);
        } else {
            this.props.clear();
            this.props.putAll(newProps);
            LOG.info("Read configuration file {}.", FILENAME);
        }
        reinit();
    }

    public void shutdown() {
        props.clear();
        reinit();
    }

    private void reinit() {
        // UPLOAD_DIRECTORY
        uploadDirectory = getPropertyInternal(Property.UploadDirectory);
        if (!uploadDirectory.endsWith("/")) {
            uploadDirectory += "/";
        }
        uploadDirectory += ".OX/";
        try {
            if (new File(uploadDirectory).mkdir()) {
                Runtime.getRuntime().exec("chmod 700 " + uploadDirectory);
                Runtime.getRuntime().exec("chown open-xchange:open-xchange " + uploadDirectory);
                LOG.info("Temporary upload directory created");
            }
        } catch (final Exception e) {
            LOG.error("Temporary upload directory could NOT be properly created", e);
        }
        // MAX_FILE_UPLOAD_SIZE
        try {
            maxFileUploadSize = Integer.parseInt(getPropertyInternal(Property.MaxFileUploadSize));
        } catch (final NumberFormatException e) {
            maxFileUploadSize = 10000;
        }
        // MAX_UPLOAD_IDLE_TIME_MILLIS
        try {
            maxUploadIdleTimeMillis = Integer.parseInt(getPropertyInternal(Property.MaxUploadIdleTimeMillis));
        } catch (final NumberFormatException e) {
            maxUploadIdleTimeMillis = 300000;
        }
        // PrefetchEnabled
        prefetchEnabled = Boolean.parseBoolean(getPropertyInternal(Property.PrefetchEnabled));
        // Default encoding
        defaultEncoding = getPropertyInternal(Property.DefaultEncoding);
        // JMX port
        jmxPort = Integer.parseInt(getPropertyInternal(Property.JMX_PORT));
        // JMX bind address
        jmxBindAddress = getPropertyInternal(Property.JMX_BIND_ADDRESS);
        // Check IP
        checkIP = Boolean.valueOf(getPropertyInternal(Property.IP_CHECK));
        ipMaskV4 = getPropertyInternal(Property.IP_MASK_V4);
        ipMaskV6 = getPropertyInternal(Property.IP_MASK_V6);
        // IP check whitelist
        clientWhitelist.clear();
        clientWhitelist.add(getPropertyInternal(Property.IP_CHECK_WHITELIST));
        // UI web path
        uiWebPath = getPropertyInternal(Property.UI_WEB_PATH);
        cookieTTL = (int) ConfigTools.parseTimespan(getPropertyInternal(Property.COOKIE_TTL));
        cookieHttpOnly = Boolean.parseBoolean(getPropertyInternal(Property.COOKIE_HTTP_ONLY));
        // The max. body size
        maxBodySize = Integer.parseInt(getPropertyInternal(Property.MAX_BODY_SIZE));
        // Default max. concurrent AJAX requests
        defaultMaxConcurrentAJAXRequests = Integer.parseInt(getPropertyInternal(Property.DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS));
    }

    private String getPropertyInternal(final Property property) {
        return props.getProperty(property.getPropertyName(), property.getDefaultValue());
    }

    /**
     * Returns the value of the property with the specified key. This method returns <code>null</code> if the property is not found.
     *
     * @param key the property key.
     * @return the value of the property or <code>null</code> if the property is not found.
     */
    private static String getProperty(final String key) {
        return SINGLETON.props.getProperty(key);
    }

    /**
     * @param property
     *            wanted property.
     * @return the value of the property.
     */
    @SuppressWarnings("unchecked")
    public static <V> V getPropertyObject(final Property property) {
        try {
            final Object value;
            switch (property) {
            case UploadDirectory:
                value = SINGLETON.uploadDirectory;
                break;
            case MaxFileUploadSize:
                value = Integer.valueOf(SINGLETON.maxFileUploadSize);
                break;
            case MaxUploadIdleTimeMillis:
                value = Integer.valueOf(SINGLETON.maxUploadIdleTimeMillis);
                break;
            case PrefetchEnabled:
                value = Boolean.valueOf(SINGLETON.prefetchEnabled);
                break;
            case DefaultEncoding:
                value = SINGLETON.defaultEncoding;
                break;
            case JMX_PORT:
                value = Integer.valueOf(SINGLETON.jmxPort);
                break;
            case JMX_BIND_ADDRESS:
                value = SINGLETON.jmxBindAddress;
                break;
            case UI_WEB_PATH:
                value = SINGLETON.uiWebPath;
                break;
            case COOKIE_TTL:
                value = Integer.valueOf(SINGLETON.cookieTTL);
                break;
            case COOKIE_HTTP_ONLY:
                value = Boolean.valueOf(SINGLETON.cookieHttpOnly);
                break;
            case IP_CHECK:
                value = SINGLETON.checkIP;
                break;
            case IP_MASK_V4:
                value = SINGLETON.ipMaskV4;
                break;
            case IP_MASK_V6:
                value = SINGLETON.ipMaskV6;
                break;
            case IP_CHECK_WHITELIST:
                value = SINGLETON.clientWhitelist;
                break;
            case MAX_BODY_SIZE:
                value = Integer.valueOf(SINGLETON.maxBodySize);
                break;
            case DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS:
                value = Integer.valueOf(SINGLETON.defaultMaxConcurrentAJAXRequests);
                break;
            default:
                value = getProperty(property.getPropertyName());
            }
            return (V) value;
        } catch (final ClassCastException e) {
            LOG.debug("", e);
            return null;
        }
    }

    /**
     * @param property wanted property.
     * @return the value of the property.
     */
    public static String getProperty(final Property property) {
        final String value;
        switch (property) {
        case UploadDirectory:
            value = SINGLETON.uploadDirectory;
            break;
        case MaxFileUploadSize:
            value = Integer.toString(SINGLETON.maxFileUploadSize);
            break;
        case MaxUploadIdleTimeMillis:
            value = Integer.toString(SINGLETON.maxUploadIdleTimeMillis);
            break;
        case PrefetchEnabled:
            value = String.valueOf(SINGLETON.prefetchEnabled);
            break;
        case DefaultEncoding:
            value = SINGLETON.defaultEncoding;
            break;
        case JMX_PORT:
            value = Integer.toString(SINGLETON.jmxPort);
            break;
        case JMX_BIND_ADDRESS:
            value = SINGLETON.jmxBindAddress;
            break;
        case UI_WEB_PATH:
            value = SINGLETON.uiWebPath;
            break;
        case COOKIE_TTL:
            value = Integer.toString(SINGLETON.cookieTTL);
            break;
        case COOKIE_HTTP_ONLY:
            value = String.valueOf(SINGLETON.cookieHttpOnly);
            break;
        case IP_CHECK:
            value = SINGLETON.checkIP.toString();
            break;
        case IP_MASK_V4:
            value = SINGLETON.ipMaskV4.toString();
            break;
        case IP_MASK_V6:
            value = SINGLETON.ipMaskV6.toString();
            break;
        case MAX_BODY_SIZE:
            value = Integer.toString(SINGLETON.maxBodySize);
            break;
        case DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS:
            value = Integer.toString(SINGLETON.defaultMaxConcurrentAJAXRequests);
            break;
        default:
            value = getProperty(property.getPropertyName());
        }
        return value;
    }

    /**
     * Returns <code>true</code> if and only if the property named by the argument exists and is equal to the string <code>"true"</code>.
     * The test of this string is case insensitive.
     * <p>
     * If there is no property with the specified name, or if the specified name is empty or null, then <code>false</code> is returned.
     *
     * @param property the property.
     * @return the <code>boolean</code> value of the property.
     */
    public static boolean getBoolean(final Property property) {
        final boolean value;
        if (Property.PrefetchEnabled == property) {
            value = SINGLETON.prefetchEnabled;
        } else if (Property.COOKIE_HTTP_ONLY == property) {
            value = SINGLETON.cookieHttpOnly;
        } else {
            value = Boolean.parseBoolean(SINGLETON.props.getProperty(property.getPropertyName()));
        }
        return value;
    }

    public static Integer getInteger(final Property property) throws OXException {
        final Integer value;
        switch (property) {
        case MaxFileUploadSize:
            value = I(SINGLETON.maxFileUploadSize);
            break;
        case MaxUploadIdleTimeMillis:
            value = I(SINGLETON.maxUploadIdleTimeMillis);
            break;
        case JMX_PORT:
            value = I(SINGLETON.jmxPort);
            break;
        case COOKIE_TTL:
            value = I(SINGLETON.cookieTTL);
            break;
        case MAX_BODY_SIZE:
            value = I(SINGLETON.maxBodySize);
            break;
        case DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS:
            value = I(SINGLETON.defaultMaxConcurrentAJAXRequests);
            break;
        default:
            try {
                final String prop = getProperty(property.getPropertyName());
                if (prop == null) {
                    throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property.getPropertyName());
                }
                value = Integer.valueOf(getProperty(property.getPropertyName()));
            } catch (final NumberFormatException e) {
                throw ConfigurationExceptionCodes.PROPERTY_NOT_AN_INTEGER.create(property.getPropertyName());
            }
        }
        return value;
    }

    /**
     * @param property wanted property.
     * @return the value of the property.
     * @throws OXException If property is missing or its type is not an integer
     */
    public static int getInt(final Property property) throws OXException {
        final int value;
        switch (property) {
        case MaxFileUploadSize:
            value = SINGLETON.maxFileUploadSize;
            break;
        case MaxUploadIdleTimeMillis:
            value = SINGLETON.maxUploadIdleTimeMillis;
            break;
        case JMX_PORT:
            value = SINGLETON.jmxPort;
            break;
        case COOKIE_TTL:
            value = SINGLETON.cookieTTL;
            break;
        case MAX_BODY_SIZE:
            value = SINGLETON.maxBodySize;
            break;
        case DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS:
            value = SINGLETON.defaultMaxConcurrentAJAXRequests;
            break;
        default:
            try {
                final String prop = getProperty(property.getPropertyName());
                if (Strings.isEmpty(prop)) {
                    throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property.getPropertyName());
                }
                value = Integer.parseInt(prop.trim());
            } catch (final NumberFormatException e) {
                throw ConfigurationExceptionCodes.PROPERTY_NOT_AN_INTEGER.create(e, property.getPropertyName());
            }
        }
        return value;
    }

    public static Long getLong(final Property property) throws OXException {
        final Long value;
        switch (property) {
        case MaxFileUploadSize:
            value = L(SINGLETON.maxFileUploadSize);
            break;
        case MaxUploadIdleTimeMillis:
            value = L(SINGLETON.maxUploadIdleTimeMillis);
            break;
        case JMX_PORT:
            value = L(SINGLETON.jmxPort);
            break;
        case COOKIE_TTL:
            value = L(SINGLETON.cookieTTL);
            break;
        case MAX_BODY_SIZE:
            value = L(SINGLETON.maxBodySize);
            break;
        case DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS:
            value = L(SINGLETON.defaultMaxConcurrentAJAXRequests);
            break;
        default:
            try {
                final String prop = getProperty(property.getPropertyName());
                if (prop == null) {
                    throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property.getPropertyName());
                }
                value = Long.valueOf(getProperty(property.getPropertyName()));
            } catch (final NumberFormatException e) {
                throw ConfigurationExceptionCodes.PROPERTY_NOT_AN_INTEGER.create(property.getPropertyName());
            }
        }
        return value;
    }

    public static enum Property {
        /**
         * Upload directory.
         */
        UploadDirectory("UPLOAD_DIRECTORY", "/tmp/"),
        /**
         * Max upload file size.
         */
        @Deprecated
        MaxFileUploadSize("MAX_UPLOAD_FILE_SIZE", "10000"),
        /**
         * Enable/Disable SearchIterator's ResultSet prefetch.
         */
        PrefetchEnabled("PrefetchEnabled", Boolean.FALSE.toString()),
        /**
         * Default encoding.
         */
        DefaultEncoding("DefaultEncoding", "UTF-8"),
        /**
         * The maximum size of accepted uploads. May be overridden in specialized module configs and user settings.
         */
        MAX_UPLOAD_SIZE("MAX_UPLOAD_SIZE", "104857600"),
        /**
         * JMXPort
         */
        JMX_PORT("JMXPort", "9999"),
        /**
         * JMXBindAddress
         */
        JMX_BIND_ADDRESS("JMXBindAddress", "localhost"),
        /**
         * Max idle time for uploaded files in milliseconds
         */
        MaxUploadIdleTimeMillis("MAX_UPLOAD_IDLE_TIME_MILLIS", "300000"),
        /**
         * Number of characters a search pattern must contain to prevent slow search queries and big responses in large contexts.
         */
        MINIMUM_SEARCH_CHARACTERS("com.openexchange.MinimumSearchCharacters", "0"),
        /**
         * On session validation of every request the client IP address is compared with the client IP address used for the login request.
         * If this connfiguration parameter is set to <code>true</code> and the client IP addresses do not match the request will be denied.
         * Setting this parameter to <code>false</code> will only log the different client IP addresses with debug level.
         */
        IP_CHECK("com.openexchange.IPCheck", Boolean.TRUE.toString()),
        /**
        * Subnet mask for accepting IP-ranges.
        * Using CIDR-Notation for v4 and v6 or dotted decimal only for v4.
        * Examples:
        * com.openexchange.IPMaskV4=255.255.255.0
        * com.openexchange.IPMaskV4=/24
        * com.openexchange.IPMaskV6=/60
        */
        IP_MASK_V4("com.openexchange.IPMaskV4", ""),
        IP_MASK_V6("com.openexchange.IPMaskV6", ""),
        /**
         * The comma-separated list of client patterns that do bypass IP check
         */
        IP_CHECK_WHITELIST("com.openexchange.IPCheckWhitelist", ""),
        /**
         * Configures the path on the web server where the UI is located. This path is used to generate links directly into the UI. The
         * default conforms to the path where the UI is installed by the standard packages on the web server.
         */
        UI_WEB_PATH("com.openexchange.UIWebPath", "/appsuite/"),
        /**
         * The cookie time-to-live
         */
        COOKIE_TTL("com.openexchange.cookie.ttl", "1W"),
        /**
         * The Cookie HttpOnly flag
         */
        COOKIE_HTTP_ONLY("com.openexchange.cookie.httpOnly", Boolean.TRUE.toString()),
        /**
         * The fields used to calculate the hash value which is part of Cookie name.
         * <p>
         * This option only has effect if "com.openexchange.cookie.hash" option is set to "calculate".
         */
        COOKIE_HASH_FIELDS("com.openexchange.cookie.hash.fields", ""),
        /**
         * The method how to generate the hash value which is part of Cookie name
         */
        COOKIE_HASH("com.openexchange.cookie.hash", "calculate"),
        /**
         * Whether to force secure flag for Cookies
         */
        COOKIE_FORCE_HTTPS("com.openexchange.forceHTTPS", Boolean.FALSE.toString()),
        /**
         * Whether to force HTTPS protocol.
         */
        FORCE_HTTPS("com.openexchange.forceHTTPS", Boolean.FALSE.toString()),
        /**
         * The max. allowed size of a HTTP request
         *
         * @deprecated Use "com.openexchange.servlet.maxBodySize" instead
         */
        @Deprecated
        MAX_BODY_SIZE("MAX_BODY_SIZE", "104857600"),
        /**
         * The default value for max. concurrent AJAX requests.
         */
        DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS("com.openexchange.defaultMaxConcurrentAJAXRequests", "100"),

        ;

        private final String propertyName;

        private final String defaultValue;

        private Property(final String propertyName, final String defaultValue) {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForFiles("server.properties");
    }
}
