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

package com.openexchange.mail.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.openexchange.config.Configuration;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig.CredSrc;
import com.openexchange.mail.config.MailConfig.LoginType;
import com.openexchange.mail.mime.spam.DefaultSpamHandler;
import com.openexchange.mail.osgi.services.ConfigurationService;
import com.openexchange.mail.partmodifier.DummyPartModifier;
import com.openexchange.mail.partmodifier.PartModifier;

/**
 * {@link MailProperties} - Global mail properties read from configuration file
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailProperties {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailProperties.class);

	private static final String STR_FALSE = "false";

	private static final String STR_TRUE = "true";

	private static final MailProperties instance = new MailProperties();

	/**
	 * Gets the singleton instance of {@link MailProperties}
	 * 
	 * @return The singleton instance of {@link MailProperties}
	 */
	static MailProperties getInstance() {
		return instance;
	}

	private final AtomicBoolean loaded;

	/*
	 * Fields for global properties
	 */
	private LoginType loginType;

	private CredSrc credSrc;

	private String mailServer;

	private String transportServer;

	private String masterPassword;

	private int mailFetchLimit;

	private int attachDisplaySize;

	private boolean userFlagsEnabled;

	private boolean allowNestedDefaultFolderOnAltNamespace;

	private String defaultMimeCharset;

	private boolean ignoreSubscription;

	private boolean spamEnabled;

	private char defaultSeparator;

	private int maxNumOfConnections;

	private String[] quoteLineColors;

	private Properties javaMailProperties;

	private boolean watcherEnabled;

	private int watcherTime;

	private int watcherFrequency;

	private boolean watcherShallClose;

	private String spamHandlerClass;

	private boolean supportSubscription;

	/**
	 * Initializes a new {@link MailProperties}
	 */
	private MailProperties() {
		super();
		loaded = new AtomicBoolean();
	}

	/**
	 * Exclusively loads the global mail properties
	 * 
	 * @throws MailConfigException
	 *             If loading of global mail properties fails
	 */
	void loadProperties() throws MailConfigException {
		if (!loaded.get()) {
			synchronized (loaded) {
				if (!loaded.get()) {
					loadProperties0();
					loaded.set(true);
				}
			}
		}
	}

	/**
	 * Exclusively resets the global mail properties
	 */
	void resetProperties() {
		if (loaded.get()) {
			synchronized (loaded) {
				if (loaded.get()) {
					resetFields();
					loaded.set(false);
				}
			}
		}
	}

	private void resetFields() {
		loginType = null;
		credSrc = null;
		mailServer = null;
		transportServer = null;
		masterPassword = null;
		mailFetchLimit = 0;
		attachDisplaySize = 0;
		userFlagsEnabled = false;
		allowNestedDefaultFolderOnAltNamespace = false;
		defaultMimeCharset = null;
		ignoreSubscription = false;
		spamEnabled = false;
		defaultSeparator = '\0';
		maxNumOfConnections = 0;
		quoteLineColors = null;
		javaMailProperties = null;
		watcherEnabled = false;
		watcherTime = 0;
		watcherFrequency = 0;
		watcherShallClose = false;
		spamHandlerClass = null;
		supportSubscription = false;
	}

	private void loadProperties0() throws MailConfigException {
		final StringBuilder logBuilder = new StringBuilder(1024);
		logBuilder.append("\nLoading global mail properties...\n");

		final Configuration configuration = ConfigurationService.getInstance().getService();
		try {

			{
				final String loginTypeStr = configuration.getProperty("com.openexchange.mail.loginType");
				if (loginTypeStr == null) {
					throw new MailConfigException("Property \"loginType\" not set");
				}
				if (LoginType.GLOBAL.toString().equalsIgnoreCase(loginTypeStr)) {
					loginType = LoginType.GLOBAL;
				} else if (LoginType.USER.toString().equalsIgnoreCase(loginTypeStr)) {
					loginType = LoginType.USER;
				} else if (LoginType.ANONYMOUS.toString().equalsIgnoreCase(loginTypeStr)) {
					loginType = LoginType.ANONYMOUS;
				} else {
					throw new MailConfigException(new StringBuilder(256).append(
							"Unknown value in property \"loginType\": ").append(loginTypeStr).toString());
				}
				logBuilder.append("\tLogin Type: ").append(loginType.toString()).append('\n');
			}

			{
				final String credSrcStr = configuration.getProperty("com.openexchange.mail.credSrc");
				if (credSrcStr == null || credSrcStr.equalsIgnoreCase(CredSrc.SESSION.toString())) {
					credSrc = CredSrc.SESSION;
				} else if (credSrcStr.equalsIgnoreCase(CredSrc.OTHER.toString())) {
					credSrc = CredSrc.OTHER;
				} else if (credSrcStr.equalsIgnoreCase(CredSrc.USER_IMAPLOGIN.toString())) {
					credSrc = CredSrc.USER_IMAPLOGIN;
				} else {
					throw new MailConfigException(new StringBuilder(256).append(
							"Unknown value in property \"credSrc\": ").append(credSrcStr).toString());
				}
				logBuilder.append("\tCredentials Source: ").append(credSrc.toString()).append('\n');
			}

			{
				mailServer = configuration.getProperty("com.openexchange.mail.mailServer");
				if (mailServer != null) {
					mailServer = mailServer.trim();
				}
			}

			{
				transportServer = configuration.getProperty("com.openexchange.mail.transportServer");
				if (transportServer != null) {
					transportServer = transportServer.trim();
				}
			}

			{
				masterPassword = configuration.getProperty("com.openexchange.mail.masterPassword");
				if (masterPassword != null) {
					masterPassword = masterPassword.trim();
				}
			}

			{
				final String mailFetchLimitStr = configuration.getProperty("com.openexchange.mail.mailFetchLimit",
						"1000").trim();
				try {
					mailFetchLimit = Integer.parseInt(mailFetchLimitStr);
					logBuilder.append("\tMail Fetch Limit: ").append(mailFetchLimit).append('\n');
				} catch (final NumberFormatException e) {
					mailFetchLimit = 1000;
					logBuilder.append("\tMail Fetch Limit: Non parseable value \"").append(mailFetchLimitStr).append(
							"\". Setting to fallback: ").append(mailFetchLimit).append('\n');
				}
			}

			{
				final String attachDisplaySizeStr = configuration.getProperty(
						"com.openexchange.mail.attachmentDisplaySizeLimit", "8192").trim();
				try {
					attachDisplaySize = Integer.parseInt(attachDisplaySizeStr);
					logBuilder.append("\tAttachment Display Size Limit: ").append(attachDisplaySize).append('\n');
				} catch (final NumberFormatException e) {
					attachDisplaySize = 8192;
					logBuilder.append("\tAttachment Display Size Limit: Non parseable value \"").append(
							attachDisplaySizeStr).append("\". Setting to fallback: ").append(attachDisplaySize).append(
							'\n');
				}
			}

			{
				final String userFlagsStr = configuration.getProperty("com.openexchange.mail.userFlagsEnabled",
						STR_FALSE).trim();
				userFlagsEnabled = Boolean.parseBoolean(userFlagsStr);
				logBuilder.append("\tUser Flags Enabled: ").append(userFlagsEnabled).append('\n');
			}

			{
				final String allowNestedStr = configuration.getProperty(
						"com.openexchange.mail.allowNestedDefaultFolderOnAltNamespace", STR_FALSE).trim();
				allowNestedDefaultFolderOnAltNamespace = Boolean.parseBoolean(allowNestedStr);
				logBuilder.append("\tAllow Nested Default Folders on AltNamespace: ").append(
						allowNestedDefaultFolderOnAltNamespace).append('\n');
			}

			{
				final String defaultMimeCharsetStr = configuration.getProperty("mail.mime.charset", "UTF-8").trim();
				/*
				 * Check validity
				 */
				try {
					Charset.forName(defaultMimeCharsetStr);
					defaultMimeCharset = defaultMimeCharsetStr;
					logBuilder.append("\tDefault MIME Charset: ").append(defaultMimeCharset).append('\n');
				} catch (final Throwable t) {
					defaultMimeCharset = "UTF-8";
					logBuilder.append("\tDefault MIME Charset: Unsupported charset \"").append(defaultMimeCharsetStr)
							.append("\". Setting to fallback: ").append(defaultMimeCharset).append('\n');
				}
				/*
				 * Add to system properties, too
				 */
				System.getProperties().setProperty("mail.mime.charset", defaultMimeCharset);
			}

			{
				final String ignoreSubsStr = configuration.getProperty("com.openexchange.mail.ignoreSubscription",
						STR_FALSE).trim();
				ignoreSubscription = Boolean.parseBoolean(ignoreSubsStr);
				logBuilder.append("\tIgnore Folder Subscription: ").append(ignoreSubscription).append('\n');
			}

			{
				final String supSubsStr = configuration.getProperty("com.openexchange.mail.supportSubscription",
						STR_TRUE).trim();
				supportSubscription = Boolean.parseBoolean(supSubsStr);
				logBuilder.append("\tSupport Subscription: ").append(supportSubscription).append('\n');
			}

			{
				final String spamEnabledStr = configuration.getProperty("com.openexchange.mail.spamEnabled", STR_FALSE)
						.trim();
				spamEnabled = Boolean.parseBoolean(spamEnabledStr);
				logBuilder.append("\tSpam Enabled: ").append(spamEnabled).append('\n');
			}

			{
				final char defaultSep = configuration.getProperty("com.openexchange.mail.defaultSeparator", "/").trim()
						.charAt(0);
				if (defaultSep <= 32) {
					defaultSeparator = '/';
					logBuilder.append("\tDefault Separator: Invalid separator (decimal ascii value=").append(
							(int) defaultSep).append("). Setting to fallback: ").append(defaultSeparator).append('\n');
				} else {
					defaultSeparator = defaultSep;
					logBuilder.append("\tDefault Separator: ").append(defaultSeparator).append('\n');
				}
			}

			{
				final String maxNum = configuration.getProperty("com.openexchange.mail.maxNumOfConnections", "0")
						.trim();
				try {
					maxNumOfConnections = Integer.parseInt(maxNum);
					logBuilder.append("\tMax Number of Connections: ").append(maxNumOfConnections).append('\n');
				} catch (final NumberFormatException e) {
					maxNumOfConnections = 0;
					logBuilder.append("\tMax Number of Connections: Invalid value \"").append(maxNum).append(
							"\". Setting to fallback: ").append(maxNumOfConnections).append('\n');
				}
			}

			{
				final String partModifierStr = configuration.getProperty("com.openexchange.mail.partModifierImpl",
						DummyPartModifier.class.getName()).trim();
				try {
					PartModifier.init(partModifierStr);
					logBuilder.append("\tPartModifier Implementation: ").append(
							PartModifier.getInstance().getClass().getName()).append('\n');
				} catch (final MailException e) {
					try {
						PartModifier.init(DummyPartModifier.class.getName());
					} catch (final MailException e1) {
						/*
						 * Cannot occur
						 */
						LOG.error(e.getLocalizedMessage(), e);
					}
					logBuilder.append("\tPartModifier Implementation: Unknown class \"").append(partModifierStr)
							.append("\". Setting to fallback: ").append(DummyPartModifier.class.getName()).append('\n');
				}
			}

			{
				final String quoteColors = configuration
						.getProperty("com.openexchange.mail.quoteLineColors", "#666666").trim();
				if (Pattern.matches("((#[0-9a-fA-F&&[^,]]{6})(?:\r?\n|\\z|\\s*,\\s*))+", quoteColors)) {
					quoteLineColors = quoteColors.split("\\s*,\\s*");
					logBuilder.append("\tHTML Quote Colors: ").append(quoteColors).append('\n');
				} else {
					quoteLineColors = new String[] { "#666666" };
					logBuilder.append("\tHTML Quote Colors: Invalid sequence of colors \"").append(quoteColors).append(
							"\". Setting to fallback: #666666").append('\n');
				}
			}

			{
				final String watcherEnabledStr = configuration.getProperty("com.openexchange.mail.watcherEnabled",
						STR_FALSE);
				watcherEnabled = Boolean.parseBoolean(watcherEnabledStr);
				logBuilder.append("\tWatcher Enabled: ").append(watcherEnabled).append('\n');
			}

			{
				final String watcherTimeStr = configuration.getProperty("com.openexchange.mail.watcherTime", "60000");
				try {
					watcherTime = Integer.parseInt(watcherTimeStr);
					logBuilder.append("\tWatcher Time: ").append(watcherTime).append('\n');
				} catch (final NumberFormatException e) {
					watcherTime = 60000;
					logBuilder.append("\tWatcher Time: Invalid value \"").append(watcherTimeStr).append(
							"\". Setting to fallback: ").append(watcherTime).append('\n');
				}
			}

			{
				final String watcherFeqStr = configuration.getProperty("com.openexchange.mail.watcherFrequency",
						"10000");
				try {
					watcherFrequency = Integer.parseInt(watcherFeqStr);
					logBuilder.append("\tWatcher Frequency: ").append(watcherFrequency).append('\n');
				} catch (final NumberFormatException e) {
					watcherFrequency = 10000;
					logBuilder.append("\tWatcher Frequency: Invalid value \"").append(watcherFeqStr).append(
							"\". Setting to fallback: ").append(watcherFrequency).append('\n');
				}
			}

			{
				final String watcherShallCloseStr = configuration.getProperty(
						"com.openexchange.mail.watcherShallClose", STR_FALSE);
				watcherShallClose = Boolean.parseBoolean(watcherShallCloseStr);
				logBuilder.append("\tWatcher Shall Close: ").append(watcherShallClose).append('\n');
			}

			{
				spamHandlerClass = configuration.getProperty("com.openexchange.mail.spamHandlerClass",
						DefaultSpamHandler.class.getName()).trim();
				logBuilder.append("\tSpam Handler Class: ").append(spamHandlerClass).append('\n');
			}

			{
				String javaMailPropertiesStr = configuration.getProperty("com.openexchange.mail.JavaMailProperties");
				if (null != javaMailPropertiesStr) {
					javaMailPropertiesStr = javaMailPropertiesStr.trim();
					if (javaMailPropertiesStr.indexOf("@oxgroupwaresysconfdir@") != -1) {
						final String configPath = configuration.getProperty("CONFIGPATH");
						if (null == configPath) {
							throw new MailConfigException("Missing property \"CONFIGPATH\" in system.properties");
						}
						javaMailPropertiesStr = javaMailPropertiesStr.replaceFirst("@oxgroupwaresysconfdir@",
								configPath);
					}
					javaMailProperties = readPropertiesFromFile(javaMailPropertiesStr);
					if (javaMailProperties.size() == 0) {
						javaMailProperties = null;
					}
				}
				logBuilder.append("\tJavaMail Properties loaded: ").append(javaMailProperties != null).append('\n');
			}
		} finally {
			ConfigurationService.getInstance().ungetService(configuration);
		}

		logBuilder.append("Global mail properties successfully loaded!");
		if (LOG.isInfoEnabled()) {
			LOG.info(logBuilder.toString());
		}
	}

	protected Properties readPropertiesFromFile(final String propFile) throws MailConfigException {
		final Properties properties = new Properties();
		FileInputStream fis = null;
		try {
			properties.load((fis = new FileInputStream(new File(propFile))));
			return properties;
		} catch (final FileNotFoundException e) {
			throw new MailConfigException(new StringBuilder(256).append("Properties not found at location: ").append(
					propFile).toString(), e);
		} catch (final IOException e) {
			throw new MailConfigException(new StringBuilder(256).append(
					"I/O error while reading properties from file \"").append(propFile).append("\": ").append(
					e.getMessage()).toString(), e);
		} finally {
			if (null != fis) {
				try {
					fis.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				fis = null;
			}
		}
	}

	/**
	 * Gets the allowNestedDefaultFolderOnAltNamespace
	 * 
	 * @return the allowNestedDefaultFolderOnAltNamespace
	 */
	boolean isAllowNestedDefaultFolderOnAltNamespace() {
		return allowNestedDefaultFolderOnAltNamespace;
	}

	/**
	 * Gets the attachDisplaySize
	 * 
	 * @return the attachDisplaySize
	 */
	int getAttachDisplaySize() {
		return attachDisplaySize;
	}

	/**
	 * Gets the credSrc
	 * 
	 * @return the credSrc
	 */
	CredSrc getCredSrc() {
		return credSrc;
	}

	/**
	 * Gets the defaultMimeCharset
	 * 
	 * @return the defaultMimeCharset
	 */
	String getDefaultMimeCharset() {
		return defaultMimeCharset;
	}

	/**
	 * Gets the defaultSeparator
	 * 
	 * @return the defaultSeparator
	 */
	char getDefaultSeparator() {
		return defaultSeparator;
	}

	/**
	 * Gets the ignoreSubscription
	 * 
	 * @return the ignoreSubscription
	 */
	boolean isIgnoreSubscription() {
		return ignoreSubscription;
	}

	/**
	 * Gets the supportSubscription
	 * 
	 * @return the supportSubscription
	 */
	boolean isSupportSubscription() {
		return supportSubscription;
	}

	/**
	 * Gets the javaMailProperties
	 * 
	 * @return the javaMailProperties
	 */
	Properties getJavaMailProperties() {
		return javaMailProperties;
	}

	/**
	 * Gets the loginType
	 * 
	 * @return the loginType
	 */
	LoginType getLoginType() {
		return loginType;
	}

	/**
	 * Gets the mailFetchLimit
	 * 
	 * @return the mailFetchLimit
	 */
	int getMailFetchLimit() {
		return mailFetchLimit;
	}

	/**
	 * Gets the mailServer
	 * 
	 * @return the mailServer
	 */
	String getMailServer() {
		return mailServer;
	}

	/**
	 * Gets the masterPassword
	 * 
	 * @return the masterPassword
	 */
	String getMasterPassword() {
		return masterPassword;
	}

	/**
	 * Gets the maxNumOfConnections
	 * 
	 * @return the maxNumOfConnections
	 */
	int getMaxNumOfConnections() {
		return maxNumOfConnections;
	}

	/**
	 * Gets the quoteLineColors
	 * 
	 * @return the quoteLineColors
	 */
	String[] getQuoteLineColors() {
		return quoteLineColors;
	}

	/**
	 * Gets the spamEnabled
	 * 
	 * @return the spamEnabled
	 */
	boolean isSpamEnabled() {
		return spamEnabled;
	}

	/**
	 * Gets the transportServer
	 * 
	 * @return the transportServer
	 */
	String getTransportServer() {
		return transportServer;
	}

	/**
	 * Gets the userFlagsEnabled
	 * 
	 * @return the userFlagsEnabled
	 */
	boolean isUserFlagsEnabled() {
		return userFlagsEnabled;
	}

	/**
	 * Gets the watcherEnabled
	 * 
	 * @return the watcherEnabled
	 */
	boolean isWatcherEnabled() {
		return watcherEnabled;
	}

	/**
	 * Gets the watcherFrequency
	 * 
	 * @return the watcherFrequency
	 */
	int getWatcherFrequency() {
		return watcherFrequency;
	}

	/**
	 * Gets the watcherShallClose
	 * 
	 * @return the watcherShallClose
	 */
	boolean isWatcherShallClose() {
		return watcherShallClose;
	}

	/**
	 * Gets the watcherTime
	 * 
	 * @return the watcherTime
	 */
	int getWatcherTime() {
		return watcherTime;
	}

	/**
	 * Gets the spamHandlerClass
	 * 
	 * @return the spamHandlerClass
	 */
	String getSpamHandlerClass() {
		return spamHandlerClass;
	}
}
