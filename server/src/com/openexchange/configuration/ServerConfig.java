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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationException.Code;

/**
 * This class handles the configuration parameters read from the configuration property file server.properties.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ServerConfig {

    private static final Log LOG = LogFactory.getLog(ServerConfig.class);

    /**
     * Singleton object.
     */
    private static final ServerConfig SINGLETON = new ServerConfig();

    /**
     * Name of the properties file.
     */
    private static final String FILENAME = "server.properties";

    private final Properties props = new Properties();

    private String uploadDirectory = "/tmp/";

    private int maxFileUploadSize = 10000;

    private int maxUploadIdleTimeMillis = 300000;

    private boolean prefetchEnabled;

    private String defaultEncoding;

    private int jmxPort;

    private String jmxBindAddress;

    private Boolean checkIP;

    /**
     * Prevent instantiation
     */
    private ServerConfig() {
        super();
    }

    public static ServerConfig getInstance() {
        return SINGLETON;
    }

    public void initialize(ConfigurationService confService) {
        final Properties props = confService.getFile(FILENAME);
        if (null == props) {
            LOG.info("Configuration file " + FILENAME + " is missing. Using defaults.");
        } else {
            this.props.clear();
            this.props.putAll(props);
            LOG.info("Read configuration file " + FILENAME + ".");
        }
        reinit();
    }

    public void shutdown() {
        props.clear();
        reinit();
    }

    private void reinit() {
        // UPLOAD_DIRECTORY
        uploadDirectory = props.getProperty(Property.UploadDirectory.propertyName, "/tmp/");
        if (!uploadDirectory.endsWith("/")) {
            uploadDirectory += "/";
        }
        uploadDirectory += ".OX/";
        try {
            if (new File(uploadDirectory).mkdir()) {
                Runtime.getRuntime().exec("chmod 700 " + uploadDirectory);
                Runtime.getRuntime().exec("chown open-xchange:open-xchange " + uploadDirectory);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Temporary upload directory created");
                }
            }
        } catch (final Exception e) {
            LOG.error("Temporary upload directory could NOT be properly created");
        }
        // MAX_FILE_UPLOAD_SIZE
        try {
            maxFileUploadSize = Integer.parseInt(getProperty(Property.MaxFileUploadSize.propertyName));
        } catch (final NumberFormatException e) {
            maxFileUploadSize = 10000;
        }
        // MAX_UPLOAD_IDLE_TIME_MILLIS
        try {
            maxUploadIdleTimeMillis = Integer.parseInt(getProperty(Property.MaxUploadIdleTimeMillis.propertyName));
        } catch (final NumberFormatException e) {
            maxUploadIdleTimeMillis = 300000;
        }
        // PrefetchEnabled
        prefetchEnabled = Boolean.parseBoolean(props.getProperty(Property.PrefetchEnabled.propertyName, Boolean.FALSE.toString()));
        // Default encoding
        defaultEncoding = props.getProperty(Property.DefaultEncoding.propertyName, "UTF-8");
        // JMX port
        jmxPort = Integer.parseInt(props.getProperty(Property.JMX_PORT.propertyName, "9999"));
        // JMX bind address
        jmxBindAddress = props.getProperty(Property.JMX_BIND_ADDRESS.propertyName, "localhost");
        checkIP = Boolean.valueOf(props.getProperty(Property.IP_CHECK.getPropertyName(), Boolean.TRUE.toString()));
    }

    /**
     * Returns the value of the property with the specified key. This method
     * returns <code>null</code> if the property is not found.
     * 
     * @param key
     *            the property key.
     * @return the value of the property or <code>null</code> if the property
     *         is not found.
     */
    private static String getProperty(final String key) {
        return SINGLETON.props.getProperty(key);
    }

    /**
     * @param property
     *            wanted property.
     * @return the value of the property.
     */
    public static String getProperty(final Property property) {
        final String value;
        switch (property) {
        case UploadDirectory:
            value = SINGLETON.uploadDirectory;
            break;
        case MaxFileUploadSize:
            value = String.valueOf(SINGLETON.maxFileUploadSize);
            break;
        case MaxUploadIdleTimeMillis:
            value = String.valueOf(SINGLETON.maxUploadIdleTimeMillis);
            break;
        case PrefetchEnabled:
            value = String.valueOf(SINGLETON.prefetchEnabled);
            break;
        case DefaultEncoding:
            value = SINGLETON.defaultEncoding;
            break;
        case JMX_PORT:
            value = String.valueOf(SINGLETON.jmxPort);
            break;
        case JMX_BIND_ADDRESS:
            value = SINGLETON.jmxBindAddress;
            break;
        default:
            value = getProperty(property.propertyName);
        }
        return value;
    }

    /**
     * Returns <code>true</code> if and only if the property named by the
     * argument exists and is equal to the string <code>"true"</code>. The
     * test of this string is case insensitive.
     * <p>
     * If there is no property with the specified name, or if the specified name
     * is empty or null, then <code>false</code> is returned.
     * 
     * @param property
     *            the property.
     * @return the <code>boolean</code> value of the property.
     */
    public static boolean getBoolean(final Property property) {
        final boolean value;
        if (Property.PrefetchEnabled == property) {
            value = SINGLETON.prefetchEnabled;
        } else {
            value = Boolean.parseBoolean(SINGLETON.props.getProperty(property.propertyName));
        }
        return value;
    }

    public static Integer getInteger(Property property) throws ConfigurationException {
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
        default:
            try {
                String prop = getProperty(property.propertyName);
                if (prop == null) {
                    throw new ConfigurationException(Code.PROPERTY_MISSING, property.propertyName);
                }
                value = Integer.valueOf(getProperty(property.propertyName));
            } catch (NumberFormatException e) {
                throw new ConfigurationException(Code.PROPERTY_NOT_AN_INTEGER, property.propertyName);
            }
        }
        return value;
    }

    /**
     * @param property
     *            wanted property.
     * @return the value of the property.
     * @throws ConfigurationException
     *             If property is missing or its type is not an integer
     */
    public static int getInt(final Property property) throws ConfigurationException {
        final int value;
        if (Property.MaxFileUploadSize == property) {
            value = SINGLETON.maxFileUploadSize;
        } else if (Property.MaxUploadIdleTimeMillis == property) {
            value = SINGLETON.maxUploadIdleTimeMillis;
        } else if (Property.JMX_PORT == property) {
            value = SINGLETON.jmxPort;
        } else {
            try {
                final String prop = getProperty(property.propertyName);
                if (prop == null) {
                    throw new ConfigurationException(ConfigurationException.Code.PROPERTY_MISSING,
                            property.propertyName);
                }
                value = Integer.parseInt(getProperty(property.propertyName));
            } catch (final NumberFormatException e) {
                throw new ConfigurationException(ConfigurationException.Code.PROPERTY_NOT_AN_INTEGER,
                        property.propertyName);
            }
        }
        return value;
    }

    public Boolean isCheckIP() {
        return checkIP;
    }

    /**
     * Enumeration of all properties in the server.properties file.
     */
    public static enum Property {
        /**
         * Upload directory.
         */
        UploadDirectory("UPLOAD_DIRECTORY"),
        /**
         * Max upload file size.
         */
        @Deprecated
        MaxFileUploadSize("MAX_UPLOAD_FILE_SIZE"),
        /**
         * Enable/Disable SearchIterator's ResultSet prefetch.
         */
        PrefetchEnabled("PrefetchEnabled"),
        /**
         * Implementation of the file storage.
         */
        FileStorageImpl("FileStorageImpl"),
        /**
         * Default encoding.
         */
        DefaultEncoding("DefaultEncoding"),
        /**
         * The maximum size of accepted uploads. Max be overridden in
         * specialized module configs and user settings.
         */
        MAX_UPLOAD_SIZE("MAX_UPLOAD_SIZE"),
        /**
         * JMXPort
         */
        JMX_PORT("JMXPort"),
        /**
         * JMXBindAddress
         */
        JMX_BIND_ADDRESS("JMXBindAddress"),
        /**
         * Max idle time for uploaded files in milliseconds
         */
        MaxUploadIdleTimeMillis("MAX_UPLOAD_IDLE_TIME_MILLIS"),
        /**
         * Number of characters a search pattern must contain to prevent slow
         * search queries and big responses in large contexts.
         */
        MINIMUM_SEARCH_CHARACTERS("com.openexchange.MinimumSearchCharacters"),
        /**
         * On session validation of every request the client IP address is compared with the client IP address used for the login request.
         * If this connfiguration parameter is set to <code>true</code> and the client IP addresses do not match the request will be denied.
         * Setting this parameter to <code>false</code> will only log the different client IP addresses with debug level.
         */
        IP_CHECK("com.openexchange.IPCheck");

        /**
         * Name of the property in the server.properties file.
         */
        private String propertyName;

        /**
         * Default constructor.
         * 
         * @param propertyName
         *            Name of the property in the server.properties file.
         */
        private Property(final String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }
}
