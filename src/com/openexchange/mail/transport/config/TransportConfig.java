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

package com.openexchange.mail.transport.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;

/**
 * {@link TransportConfig} - The transport configuration
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class TransportConfig extends MailConfig {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(TransportConfig.class);

	private static final String PROPERTYNAME = "TransportProperties";

	private static final Lock GLOBAL_PROP_LOCK = new ReentrantLock();

	private static String transportPropFile;

	private static Properties transportProperties;

	private static boolean transportPropsLoaded;

	private static boolean globalTransportPropsLoaded;

	/*
	 * 
	 */
	private static int referencedPartLimit;

	/**
	 * Default constructor
	 */
	protected TransportConfig() {
		super();
	}

	/**
	 * Checks if the transport properties are loaded. The SMTP properties are
	 * loaded if not done, yet.
	 * 
	 * @throws MailConfigException
	 *             If transport properties are not defined or cannot be read
	 *             from file
	 */
	public static void checkTransportPropFile() throws MailConfigException {
		checkMailPropFile();
		/*
		 * Load mail properties in a thread-safe manner
		 */
		if (!transportPropsLoaded) {
			PROP_LOCK.lock();
			try {
				if (transportPropFile == null && (transportPropFile = SystemConfig.getProperty(PROPERTYNAME)) == null) {
					throw new MailConfigException(new StringBuilder(50).append("Property \"").append(PROPERTYNAME)
							.append("\" not defined in system.properties").toString());
				}
				if (transportProperties == null) {
					loadTransportProps();
					transportPropsLoaded = true;
				}
			} finally {
				PROP_LOCK.unlock();
			}
		}
	}

	private static void loadTransportProps() throws MailConfigException {
		transportProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(transportPropFile));
			transportProperties.load(fis);
			fis.close();
			fis = null;
		} catch (final FileNotFoundException e) {
			transportProperties = null;
			throw new MailConfigException(new StringBuilder(256).append("SMTP properties not found at location: ")
					.append(transportPropFile).toString(), e);
		} catch (final IOException e) {
			transportProperties = null;
			throw new MailConfigException(new StringBuilder(256).append(
					"I/O error while reading SMTP properties from file \"").append(transportPropFile).append("\": ")
					.append(e.getMessage()).toString(), e);
		} finally {
			/*
			 * Close FileInputStream
			 */
			if (fis != null) {
				try {
					fis.close();
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Gets a copy of transport properties
	 * 
	 * @return A copy of SMTP properties
	 * @throws MailConfigException
	 *             If SMTP properties could not be checked
	 */
	public static Properties getTransportProperties() throws MailConfigException {
		checkTransportPropFile();
		final Properties retval = new Properties();
		retval.putAll(getProperties());
		retval.putAll((Properties) transportProperties.clone());
		return retval;
	}

	/**
	 * Loads global mail properties
	 * 
	 * @throws MailConfigException
	 *             If gloabal transport properties cannot be loaded
	 */
	public static void loadGlobalTransportProperties() throws MailConfigException {
		loadGlobalTransportProperties(true);
	}

	/**
	 * Loads global transport properties
	 * 
	 * @param checkPropFile
	 *            <code>true</code> to check for transport properties file;
	 *            otherwise <code>false</code>
	 * @throws MailConfigException
	 *             If gloabal transport properties cannot be loaded
	 */
	protected static void loadGlobalTransportProperties(final boolean checkPropFile) throws MailConfigException {
		loadGlobalMailProperties(checkPropFile);
		if (!globalTransportPropsLoaded) {
			GLOBAL_PROP_LOCK.lock();
			try {
				if (globalTransportPropsLoaded) {
					return;
				}
				if (checkPropFile) {
					checkTransportPropFile();
				}
				final StringBuilder logBuilder = new StringBuilder(1024);
				logBuilder.append("\nLoading global transport properties...\n");

				{
					final String referencedPartLimitStr = transportProperties.getProperty("referencedPartLimit",
							"1048576").trim();
					try {
						referencedPartLimit = Integer.parseInt(referencedPartLimitStr);
						logBuilder.append("\tReferenced Part Limit: ").append(referencedPartLimit).append('\n');
					} catch (final NumberFormatException e) {
						referencedPartLimit = 1048576;
						logBuilder.append("\tReferenced Part Limit: Invalid value \"").append(referencedPartLimitStr)
								.append("\". Setting to fallback ").append(referencedPartLimit).append('\n');

					}
				}

				/*
				 * Switch flag
				 */
				globalTransportPropsLoaded = true;
				logBuilder.append("Global transport properties successfully loaded!");
				if (LOG.isInfoEnabled()) {
					LOG.info(logBuilder.toString());
				}
			} finally {
				GLOBAL_PROP_LOCK.unlock();
			}
		}
	}

	/**
	 * @return <code>true</code> if global transport properties have already
	 *         been loaded; otherwise <code>false</code>
	 */
	public static boolean isGlobalTransportPropsLoaded() {
		return globalTransportPropsLoaded;
	}

	/**
	 * Gets the referencedPartLimit
	 * 
	 * @return The referencedPartLimit
	 * @throws MailConfigException
	 */
	public static int getReferencedPartLimit() throws MailConfigException {
		loadGlobalTransportProperties();
		return referencedPartLimit;
	}

}
