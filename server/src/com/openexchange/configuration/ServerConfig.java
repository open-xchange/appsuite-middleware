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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigurationException.Code;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * This class handles the configuration parameters read from the configuration
 * property file server.properties.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ServerConfig extends AbstractConfig {

	private static final Log LOG = LogFactory.getLog(ServerConfig.class);

	/**
	 * Name of the property in the system.properties configuration file that
	 * value contains the filename of the server.properties file.
	 */
	private static final SystemConfig.Property KEY = SystemConfig.Property.SERVER_CONFIG;

	/**
	 * Singleton object.
	 */
	private static final ServerConfig SINGLETON = new ServerConfig();

	private String uploadDirectory = "/tmp/";

	private int maxFileUplaodSize = 10000;

	private int maxUploadIdleTimeMillis = 300000;

	private boolean prefetchEnabled;

	private String defaultEncoding;

	private int jmxPort;

	private String jmxBindAddress;

	/**
	 * Prevent instantiation
	 */
	private ServerConfig() {
		super();
		try {
			super.loadPropertiesInternal();
		} catch (final ConfigurationException e) {
			throw new RuntimeException(e);
		}
		reinit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPropertyFileName() throws ConfigurationException {
		final String filename = SystemConfig.getProperty(KEY);
		if (null == filename) {
			throw new ConfigurationException(Code.PROPERTY_MISSING, KEY.getPropertyName());
		}
		return filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.conf.AbstractConfig#reinit()
	 */
	protected void reinit() {
		/*
		 * UPLOAD_DIRECTORY
		 */
		uploadDirectory = getPropertyInternal(Property.UploadDirectory.propertyName);
		if (uploadDirectory == null) {
			uploadDirectory = "/tmp/";
		}
		if (!uploadDirectory.endsWith("/")) {
			uploadDirectory += "/";
		}
		uploadDirectory += ".OX/";
		try {
			if (new File(uploadDirectory).mkdir()) {
				Runtime.getRuntime().exec("chmod 700 " + uploadDirectory);
				if (LOG.isInfoEnabled()) {
					LOG.info("Temporary upload directory created");
				}
			}
		} catch (final Exception e) {
			LOG.error("Temporary upload directory could NOT be properly created");
		}
		/*
		 * MAX_FILE_UPLOAD_SIZE
		 */
		try {
			maxFileUplaodSize = Integer.parseInt(getPropertyInternal(Property.MaxFileUploadSize.propertyName));
		} catch (final Throwable t) {
			maxFileUplaodSize = 10000;
		}
		/*
		 * MAX_UPLOAD_IDLE_TIME_MILLIS
		 */
		try {
			maxUploadIdleTimeMillis = Integer
					.parseInt(getPropertyInternal(Property.MaxUploadIdleTimeMillis.propertyName));
		} catch (final Throwable t) {
			maxUploadIdleTimeMillis = 300000;
		}
		/*
		 * PrefetchEnabled
		 */
		prefetchEnabled = getBooleanInternal(Property.PrefetchEnabled.propertyName, false);
		/*
		 * Default encoding
		 */
		defaultEncoding = getPropertyInternal(Property.DefaultEncoding.propertyName, "UTF-8");
		/*
		 * JMX port
		 */
		jmxPort = Integer.parseInt(getPropertyInternal(Property.JMX_PORT.propertyName, "9999"));
		/*
		 * JMX bind address
		 */
		jmxBindAddress = getPropertyInternal(Property.JMX_BIND_ADDRESS.propertyName, "localhost");
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
		return SINGLETON.getPropertyInternal(key);
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
			value = String.valueOf(SINGLETON.maxFileUplaodSize);
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
			value = getBoolean(property.propertyName);
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
	 * @param key
	 *            the property name.
	 * @return the <code>boolean</code> value of the property.
	 */
	private static boolean getBoolean(final String key) {
		return SINGLETON.getBooleanInternal(key);
	}

	/**
	 * @param property
	 *            wanted property.
	 * @return the value of the property.
	 * @throws ConfigurationException
	 *             If property is missing or its type is not an integer
	 */
	public static int getInteger(final Property property) throws ConfigurationException {
		final int value;
		if (Property.MaxFileUploadSize == property) {
			value = SINGLETON.maxFileUplaodSize;
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
		 * MonitorJMXPort
		 */
		JMX_PORT("MonitorJMXPort"),
		/**
		 * MonitorJMXBindAddress
		 */
		JMX_BIND_ADDRESS("MonitorJMXBindAddress"),
		/**
		 * Max idle time for uploaded files in milliseconds
		 */
		MaxUploadIdleTimeMillis("MAX_UPLOAD_IDLE_TIME_MILLIS");

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
	}
}
