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

package com.openexchange.smtp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.sessiond.SessionObject;

/**
 * {@link SMTPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPConfig extends MailConfig {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SMTPConfig.class);

	private static final String PROPERTYNAME = "SMTPProperties";

	private static final Lock GLOBAL_PROP_LOCK = new ReentrantLock();

	private static String smtpPropFile;

	private static Properties smtpProperties;

	private static boolean smtpPropsLoaded;

	private static boolean globalSmtpPropsLoaded;

	/*
	 * Static fields for global properties
	 */
	private static String smtpLocalhost;

	private static boolean smtpsEnabled;

	private static int smtpsPort;

	private static boolean smtpAuth;

	private static boolean smtpEnvelopeFrom;

	private static String smtpAuthEnc;

	private static int smtpTimeout;

	private static int smtpConnectionTimeout;

	private static int smtpReferencedPartLimit;

	/*
	 * User-specific fields
	 */
	private String smtpServer;

	private int smtpPort;

	/**
	 * Constructor
	 */
	private SMTPConfig() {
		super();
	}

	/**
	 * Gets the user-specific IMAP configuration
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @return The user-specific IMAP configuration
	 * @throws MailConfigException
	 *             If user-specific IMAP configuration cannot be determined
	 */
	public static SMTPConfig getSmtpConfig(final SessionObject session) throws MailConfigException {
		final SMTPConfig smtpConf = new SMTPConfig();
		fillLoginAndPassword(smtpConf, session);
		/*
		 * Check IMAP prop file
		 */
		checkSmtpPropFile();
		/*
		 * Load global IMAP properties if not done, yet
		 */
		loadGlobalSmtpProperties(false);
		/*
		 * Fetch user object and create its IMAP properties
		 */
		final User user = session.getUserObject();
		if (LoginType.GLOBAL.equals(getLoginType())) {
			String smtpServer = MailConfig.getTransportServer();
			if (smtpServer == null) {
				throw new MailConfigException(new StringBuilder(128).append("Property \"").append("transportServer")
						.append("\" not set in mail properties").toString());
			}
			int smtpPort = 25;
			{
				final int pos = smtpServer.indexOf(':');
				if (pos > -1) {
					smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
					smtpServer = smtpServer.substring(0, pos);
				}
			}
			final String masterPw = MailConfig.getMasterPassword();
			if (masterPw == null) {
				throw new MailConfigException(new StringBuilder().append("Property \"").append("masterPassword")
						.append("\" not set in mail properties").toString());
			}
			smtpConf.smtpServer = smtpServer;
			smtpConf.smtpPort = smtpPort;
		} else if (LoginType.USER.equals(getLoginType())) {
			String smtpServer = user.getSmtpServer();
			int smtpPort = 25;
			{
				final int pos = smtpServer.indexOf(':');
				if (pos > -1) {
					smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
					smtpServer = smtpServer.substring(0, pos);
				}
			}
			smtpConf.smtpServer = smtpServer;
			smtpConf.smtpPort = smtpPort;
		} else if (LoginType.ANONYMOUS.equals(getLoginType())) {
			String smtpServer = user.getSmtpServer();
			int smtpPort = 25;
			{
				final int pos = smtpServer.indexOf(':');
				if (pos > -1) {
					smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
					smtpServer = smtpServer.substring(0, pos);
				}
			}
			smtpConf.smtpServer = smtpServer;
			smtpConf.smtpPort = smtpPort;
		}
		return smtpConf;
	}

	/**
	 * Checks if the SMTP properties are loaded. The SMTP properties are loaded
	 * if not done, yet.
	 * 
	 * @throws MailConfigException
	 *             If SMTP properties are not defined or cannot be read from
	 *             file
	 */
	public static void checkSmtpPropFile() throws MailConfigException {
		checkMailPropFile();
		/*
		 * Load mail properties in a thread-safe manner
		 */
		if (!smtpPropsLoaded) {
			PROP_LOCK.lock();
			try {
				if (smtpPropFile == null && (smtpPropFile = SystemConfig.getProperty(PROPERTYNAME)) == null) {
					throw new MailConfigException(new StringBuilder(50).append("Property \"").append(PROPERTYNAME)
							.append("\" not defined in system.properties").toString());
				}
				if (smtpProperties == null) {
					loadSmtpProps();
					smtpPropsLoaded = true;
				}
			} finally {
				PROP_LOCK.unlock();
			}
		}
	}

	private static void loadSmtpProps() throws MailConfigException {
		smtpProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(smtpPropFile));
			smtpProperties.load(fis);
			fis.close();
			fis = null;
		} catch (final FileNotFoundException e) {
			smtpProperties = null;
			throw new MailConfigException(new StringBuilder(256).append("SMTP properties not found at location: ")
					.append(smtpPropFile).toString(), e);
		} catch (final IOException e) {
			smtpProperties = null;
			throw new MailConfigException(new StringBuilder(256).append(
					"I/O error while reading SMTP properties from file \"").append(smtpPropFile).append("\": ").append(
					e.getMessage()).toString(), e);
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
	 * Gets a copy of SMTP properties
	 * 
	 * @return A copy of SMTP properties
	 * @throws MailConfigException
	 *             If SMTP properties could not be checked
	 */
	public static Properties getSmtpProperties() throws MailConfigException {
		checkSmtpPropFile();
		final Properties retval = new Properties();
		retval.putAll(getProperties());
		retval.putAll((Properties) smtpProperties.clone());
		return retval;
	}

	/**
	 * Loads global mail properties
	 * 
	 * @throws MailConfigException
	 *             If gloabal mail properties cannot be loaded
	 */
	public static void loadGlobalSmtpProperties() throws MailConfigException {
		loadGlobalSmtpProperties(true);
	}

	/**
	 * Loads global mail properties
	 * 
	 * @param checkPropFile
	 *            <code>true</code> to check for mail properties file;
	 *            otherwise <code>false</code>
	 * @throws MailConfigException
	 *             If gloabal mail properties cannot be loaded
	 */
	private static void loadGlobalSmtpProperties(final boolean checkPropFile) throws MailConfigException {
		loadGlobalMailProperties(checkPropFile);
		if (!globalSmtpPropsLoaded) {
			GLOBAL_PROP_LOCK.lock();
			try {
				if (globalSmtpPropsLoaded) {
					return;
				}
				if (checkPropFile) {
					checkSmtpPropFile();
				}
				final StringBuilder logBuilder = new StringBuilder(1024);
				logBuilder.append("\nLoading global SMTP properties...\n");

				{
					final String smtpLocalhostStr = smtpProperties.getProperty("smtpLocalhost").trim();
					smtpLocalhost = smtpLocalhostStr == null || smtpLocalhostStr.length() == 0
							|| "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
					logBuilder.append("\tSMTP Localhost: ").append(smtpLocalhost).append('\n');
				}

				{
					final String smtpsEnStr = smtpProperties.getProperty("smtps", "false").trim();
					smtpsEnabled = Boolean.parseBoolean(smtpsEnStr);
					logBuilder.append("\tSMTP/S enabled: ").append(smtpsEnabled).append('\n');
				}

				{
					final String smtpsPortStr = smtpProperties.getProperty("smtpsPort", "465").trim();
					try {
						smtpsPort = Integer.parseInt(smtpsPortStr);
						logBuilder.append("\tSMTP/S port: ").append(smtpsPort).append('\n');
					} catch (final NumberFormatException e) {
						smtpsPort = 465;
						logBuilder.append("\tSMTP/S port: Invalid value \"").append(smtpsPortStr).append(
								"\". Setting to fallback ").append(smtpsPort).append('\n');
					}
				}

				{
					final String smtpAuthStr = smtpProperties.getProperty("smtpAuthentication", "false").trim();
					smtpAuth = Boolean.parseBoolean(smtpAuthStr);
					logBuilder.append("\tSMTP Authentication: ").append(smtpAuth).append('\n');
				}

				{
					final String smtpEnvFromStr = smtpProperties.getProperty("setSMTPEnvelopeFrom", "false").trim();
					smtpEnvelopeFrom = Boolean.parseBoolean(smtpEnvFromStr);
					logBuilder.append("\tSet SMTP ENVELOPE-FROM: ").append(smtpEnvelopeFrom).append('\n');
				}

				{
					final String smtpAuthEncStr = smtpProperties.getProperty("smtpAuthEnc", "UTF-8").trim();
					if (Charset.isSupported(smtpAuthEncStr)) {
						smtpAuthEnc = smtpAuthEncStr;
						logBuilder.append("\tSMTP Auth Encoding: ").append(smtpAuthEnc).append('\n');
					} else {
						smtpAuthEnc = "UTF-8";
						logBuilder.append("\tSMTP Auth Encoding: Unsupported charset \"").append(smtpAuthEncStr)
								.append("\". Setting to fallback ").append(smtpEnvelopeFrom).append('\n');
					}
				}

				{
					final String smtpTimeoutStr = smtpProperties.getProperty("smtpTimeout", "5000").trim();
					try {
						smtpTimeout = Integer.parseInt(smtpTimeoutStr);
						logBuilder.append("\tSMTP Timeout: ").append(smtpTimeout).append('\n');
					} catch (final NumberFormatException e) {
						smtpTimeout = 5000;
						logBuilder.append("\tSMTP Timeout: Invalid value \"").append(smtpTimeoutStr).append(
								"\". Setting to fallback ").append(smtpTimeout).append('\n');

					}
				}

				{
					final String smtpConTimeoutStr = smtpProperties.getProperty("smtpConnectionTimeout", "10000")
							.trim();
					try {
						smtpConnectionTimeout = Integer.parseInt(smtpConTimeoutStr);
						logBuilder.append("\tSMTP Timeout: ").append(smtpConnectionTimeout).append('\n');
					} catch (final NumberFormatException e) {
						smtpConnectionTimeout = 10000;
						logBuilder.append("\tSMTP Timeout: Invalid value \"").append(smtpConTimeoutStr).append(
								"\". Setting to fallback ").append(smtpConnectionTimeout).append('\n');

					}
				}

				{
					final String smtpReferencedPartLimitStr = smtpProperties.getProperty("smtpReferencedPartLimit",
							"1048576").trim();
					try {
						smtpReferencedPartLimit = Integer.parseInt(smtpReferencedPartLimitStr);
						logBuilder.append("\tSMTP Referenced Part Limit: ").append(smtpReferencedPartLimit)
								.append('\n');
					} catch (final NumberFormatException e) {
						smtpReferencedPartLimit = 1048576;
						logBuilder.append("\tSMTP Referenced Part Limit: Invalid value \"").append(
								smtpReferencedPartLimitStr).append("\". Setting to fallback ").append(
								smtpReferencedPartLimit).append('\n');

					}
				}

				/*
				 * Switch flag
				 */
				globalSmtpPropsLoaded = true;
				logBuilder.append("Global SMTP properties successfully loaded!");
				if (LOG.isInfoEnabled()) {
					LOG.info(logBuilder.toString());
				}
			} finally {
				GLOBAL_PROP_LOCK.unlock();
			}
		}
	}

	/**
	 * @return <code>true</code> if global SMTP properties have already been
	 *         loaded; otherwise <code>false</code>
	 */
	public static boolean isGlobalSmtpPropsLoaded() {
		return globalSmtpPropsLoaded;
	}

	/**
	 * Gets the smtpAuth
	 * 
	 * @return the smtpAuth
	 * @throws MailConfigException
	 */
	public static boolean isSmtpAuth() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpAuth;
	}

	/**
	 * Gets the smtpAuthEnc
	 * 
	 * @return the smtpAuthEnc
	 * @throws MailConfigException
	 */
	public static String getSmtpAuthEnc() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpAuthEnc;
	}

	/**
	 * Gets the smtpEnvelopeFrom
	 * 
	 * @return the smtpEnvelopeFrom
	 * @throws MailConfigException
	 */
	public static boolean isSmtpEnvelopeFrom() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpEnvelopeFrom;
	}

	/**
	 * Gets the smtpLocalhost
	 * 
	 * @return the smtpLocalhost
	 * @throws MailConfigException
	 */
	public static String getSmtpLocalhost() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpLocalhost;
	}

	/**
	 * Gets the smtpsEnabled
	 * 
	 * @return the smtpsEnabled
	 * @throws MailConfigException
	 */
	public static boolean isSmtpsEnabled() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpsEnabled;
	}

	/**
	 * Gets the smtpsPort
	 * 
	 * @return the smtpsPort
	 * @throws MailConfigException
	 */
	public static int getSmtpsPort() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpsPort;
	}

	/**
	 * Gets the smtpConnectionTimeout
	 * 
	 * @return the smtpConnectionTimeout
	 * @throws MailConfigException
	 */
	public static int getSmtpConnectionTimeout() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpConnectionTimeout;
	}

	/**
	 * Gets the smtpTimeout
	 * 
	 * @return the smtpTimeout
	 * @throws MailConfigException
	 */
	public static int getSmtpTimeout() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpTimeout;
	}

	/**
	 * Gets the smtpReferencedPartLimit
	 * 
	 * @return The smtpReferencedPartLimit
	 * @throws MailConfigException
	 */
	public static int getSmtpReferencedPartLimit() throws MailConfigException {
		loadGlobalSmtpProperties();
		return smtpReferencedPartLimit;
	}

	/**
	 * Gets the smtpPort
	 * 
	 * @return the smtpPort
	 */
	@Override
	public int getPort() {
		return smtpPort;
	}

	/**
	 * Gets the smtpServer
	 * 
	 * @return the smtpServer
	 */
	@Override
	public String getServer() {
		return smtpServer;
	}
}
