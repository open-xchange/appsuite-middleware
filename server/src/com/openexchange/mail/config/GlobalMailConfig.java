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
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig.CredSrc;
import com.openexchange.mail.config.MailConfig.LoginType;
import com.openexchange.mail.partmodifier.DummyPartModifier;
import com.openexchange.mail.partmodifier.PartModifier;
import com.openexchange.mail.spellcheck.SpellCheckConfig;
import com.openexchange.mail.spellcheck.SpellCheckConfigParser;

/**
 * {@link GlobalMailConfig} - Global mail properties
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class GlobalMailConfig {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(GlobalMailConfig.class);

	private static final String STR_FALSE = "false";
	
	private static final String STR_TRUE = "true";

	private static GlobalMailConfig mailInstance;
	
	private final AtomicBoolean loaded = new AtomicBoolean();

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

	private SpellCheckConfig spellCheckConfig;

	private Properties javaMailProperties;

	private boolean watcherEnabled;

	private int watcherTime;

	private int watcherFrequency;

	private boolean watcherShallClose;
	
	private String spamHandlerClass;
	
	private boolean supportSubscription;

	/**
	 * Initializes a new global mail config
	 */
	protected GlobalMailConfig() {
		super();
	}

	protected static final Class<?>[] CONSTRUCTOR_ARGS = new Class[0];

	static final void initializeGlobalMailConfig(final Class<? extends GlobalMailConfig> clazz) throws MailException {
		try {
			mailInstance = clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(new Object[0]);
			mailInstance.loadConfig();
		} catch (final IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}

	/**
	 * Getter for singleton instance of {@link GlobalMailConfig}
	 * 
	 * @return The singleton instance of {@link GlobalMailConfig}
	 */
	public static GlobalMailConfig getInstance() {
		return mailInstance;
	}

	private final void loadGlobalMailConfig() throws MailConfigException {
		final Properties mailProperties;
		{
			String propFile = SystemConfig.getProperty("MailProperties");
			if (propFile == null || (propFile = propFile.trim()).length() == 0) {
				throw new MailConfigException("Property \"MailProperties\" not defined in system.properties");
			}
			mailProperties = readPropertiesFromFile(propFile);
		}
		final StringBuilder logBuilder = new StringBuilder(1024);
		logBuilder.append("\nLoading global mail properties...\n");

		{
			final String loginTypeStr = mailProperties.getProperty("loginType");
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
				throw new MailConfigException(new StringBuilder(256)
						.append("Unknown value in property \"loginType\": ").append(loginTypeStr).toString());
			}
			logBuilder.append("\tLogin Type: ").append(loginType.toString()).append('\n');
		}

		{
			final String credSrcStr = mailProperties.getProperty("credSrc");
			if (credSrcStr == null || credSrcStr.equalsIgnoreCase(CredSrc.SESSION.toString())) {
				credSrc = CredSrc.SESSION;
			} else if (credSrcStr.equalsIgnoreCase(CredSrc.OTHER.toString())) {
				credSrc = CredSrc.OTHER;
			} else if (credSrcStr.equalsIgnoreCase(CredSrc.USER_IMAPLOGIN.toString())) {
				credSrc = CredSrc.USER_IMAPLOGIN;
			} else {
				throw new MailConfigException(new StringBuilder(256).append("Unknown value in property \"credSrc\": ")
						.append(credSrcStr).toString());
			}
			logBuilder.append("\tCredentials Source: ").append(credSrc.toString()).append('\n');
		}

		{
			mailServer = mailProperties.getProperty("mailServer");
			if (mailServer != null) {
				mailServer = mailServer.trim();
			}
		}

		{
			transportServer = mailProperties.getProperty("transportServer");
			if (transportServer != null) {
				transportServer = transportServer.trim();
			}
		}

		{
			masterPassword = mailProperties.getProperty("masterPassword");
			if (masterPassword != null) {
				masterPassword = masterPassword.trim();
			}
		}

		{
			final String mailFetchLimitStr = mailProperties.getProperty("mailFetchLimit", "1000").trim();
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
			final String attachDisplaySizeStr = mailProperties.getProperty("attachmentDisplaySizeLimit", "8192").trim();
			try {
				attachDisplaySize = Integer.parseInt(attachDisplaySizeStr);
				logBuilder.append("\tAttachment Display Size Limit: ").append(attachDisplaySize).append('\n');
			} catch (final NumberFormatException e) {
				attachDisplaySize = 8192;
				logBuilder.append("\tAttachment Display Size Limit: Non parseable value \"").append(
						attachDisplaySizeStr).append("\". Setting to fallback: ").append(attachDisplaySize)
						.append('\n');
			}
		}

		{
			final String userFlagsStr = mailProperties.getProperty("userFlagsEnabled", STR_FALSE).trim();
			userFlagsEnabled = Boolean.parseBoolean(userFlagsStr);
			logBuilder.append("\tUser Flags Enabled: ").append(userFlagsEnabled).append('\n');
		}

		{
			final String allowNestedStr = mailProperties.getProperty("allowNestedDefaultFolderOnAltNamespace",
					STR_FALSE).trim();
			allowNestedDefaultFolderOnAltNamespace = Boolean.parseBoolean(allowNestedStr);
			logBuilder.append("\tAllow Nested Default Folders on AltNamespace: ").append(
					allowNestedDefaultFolderOnAltNamespace).append('\n');
		}

		{
			final String defaultMimeCharsetStr = mailProperties.getProperty("mail.mime.charset", "UTF-8").trim();
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
			final String ignoreSubsStr = mailProperties.getProperty("ignoreSubscription", STR_FALSE).trim();
			ignoreSubscription = Boolean.parseBoolean(ignoreSubsStr);
			logBuilder.append("\tIgnore Folder Subscription: ").append(ignoreSubscription).append('\n');
		}

		{
			final String supSubsStr = mailProperties.getProperty("supportSubscription", STR_TRUE).trim();
			supportSubscription = Boolean.parseBoolean(supSubsStr);
			logBuilder.append("\tSupport Subscription: ").append(supportSubscription).append('\n');
		}

		{
			final String spamEnabledStr = mailProperties.getProperty("spamEnabled", STR_FALSE).trim();
			spamEnabled = Boolean.parseBoolean(spamEnabledStr);
			logBuilder.append("\tSpam Enabled: ").append(spamEnabled).append('\n');
		}

		{
			final char defaultSep = mailProperties.getProperty("defaultSeparator", "/").trim().charAt(0);
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
			final String maxNum = mailProperties.getProperty("maxNumOfConnections", "0").trim();
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
			final String partModifierStr = mailProperties.getProperty("partModifierImpl",
					"com.openexchange.imap.DummyPartModifier").trim();
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
				logBuilder.append("\tPartModifier Implementation: Unknown class \"").append(partModifierStr).append(
						"\". Setting to fallback: ").append(DummyPartModifier.class.getName()).append('\n');
			}
		}

		{
			final String quoteColors = mailProperties.getProperty("quoteLineColors", "#666666").trim();
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
			final String watcherEnabledStr = mailProperties.getProperty("watcherEnabled", STR_FALSE);
			watcherEnabled = Boolean.parseBoolean(watcherEnabledStr);
			logBuilder.append("\tWatcher Enabled: ").append(watcherEnabled).append('\n');
		}

		{
			final String watcherTimeStr = mailProperties.getProperty("watcherTime", "60000");
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
			final String watcherFeqStr = mailProperties.getProperty("watcherFrequency", "10000");
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
			final String watcherShallCloseStr = mailProperties.getProperty("watcherShallClose", STR_FALSE);
			watcherShallClose = Boolean.parseBoolean(watcherShallCloseStr);
			logBuilder.append("\tWatcher Shall Close: ").append(watcherShallClose).append('\n');
		}

		{
			spamHandlerClass = mailProperties.getProperty("spamHandlerClass",
					"com.openexchange.mail.mime.spam.DefaultSpamHandler").trim();
			logBuilder.append("\tSpam Handler Class: ").append(spamHandlerClass).append('\n');
		}

		/*
		 * Load & parse spell check config
		 */
		{
			String spellCheckConfigStr = SystemConfig.getProperty("SPELLCHECKCFG");
			if (spellCheckConfigStr == null) {
				throw new MailConfigException("Property \"SPELLCHECKCFG\" is not defined in system.properties");
			}
			spellCheckConfigStr = spellCheckConfigStr.trim();
			try {
				spellCheckConfig = new SpellCheckConfigParser().parseSpellCheckConfig(spellCheckConfigStr);
				logBuilder.append("\tSpellCheck Config File: ").append(spellCheckConfigStr).append('\n');
			} catch (final Exception e) {
				LOG.error("SpellCheck config file \"" + spellCheckConfigStr
						+ "\" could not be properly loaded & parsed:\n" + e.getMessage(), e);
			}
		}

		{
			String javaMailPropertiesStr = SystemConfig.getProperty("JavaMailProperties");
			if (null != javaMailPropertiesStr) {
				javaMailPropertiesStr = javaMailPropertiesStr.trim();
				javaMailProperties = readPropertiesFromFile(javaMailPropertiesStr);
				if (javaMailProperties.size() == 0) {
					javaMailProperties = null;
				}
			}
			logBuilder.append("\tJavaMail Properties loaded: ").append(javaMailProperties != null).append('\n');
		}

		logBuilder.append("Global mail properties successfully loaded!");
		if (LOG.isInfoEnabled()) {
			LOG.info(logBuilder.toString());
		}
	}

	protected final Properties readPropertiesFromFile(final String propFile) throws MailConfigException {
		final Properties mailProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(propFile));
			mailProperties.load(fis);
			return mailProperties;
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

	protected final void loadConfig() throws MailConfigException {
		if (loaded.get()) {
			return;
		}
		loadGlobalMailConfig();
		loadGlobalSubConfig();
		loaded.set(true);
	}

	protected abstract void loadGlobalSubConfig() throws MailConfigException;

	/**
	 * Gets the allowNestedDefaultFolderOnAltNamespace
	 * 
	 * @return the allowNestedDefaultFolderOnAltNamespace
	 */
	final boolean isAllowNestedDefaultFolderOnAltNamespace() {
		return allowNestedDefaultFolderOnAltNamespace;
	}

	/**
	 * Gets the attachDisplaySize
	 * 
	 * @return the attachDisplaySize
	 */
	final int getAttachDisplaySize() {
		return attachDisplaySize;
	}

	/**
	 * Gets the credSrc
	 * 
	 * @return the credSrc
	 */
	final CredSrc getCredSrc() {
		return credSrc;
	}

	/**
	 * Gets the defaultMimeCharset
	 * 
	 * @return the defaultMimeCharset
	 */
	final String getDefaultMimeCharset() {
		return defaultMimeCharset;
	}

	/**
	 * Gets the defaultSeparator
	 * 
	 * @return the defaultSeparator
	 */
	final char getDefaultSeparator() {
		return defaultSeparator;
	}

	/**
	 * Gets the ignoreSubscription
	 * 
	 * @return the ignoreSubscription
	 */
	final boolean isIgnoreSubscription() {
		return ignoreSubscription;
	}

	/**
	 * Gets the supportSubscription
	 * 
	 * @return the supportSubscription
	 */
	final boolean isSupportSubscription() {
		return supportSubscription;
	}

	/**
	 * Gets the javaMailProperties
	 * 
	 * @return the javaMailProperties
	 */
	final Properties getJavaMailProperties() {
		return javaMailProperties;
	}

	/**
	 * Gets the loginType
	 * 
	 * @return the loginType
	 */
	final LoginType getLoginType() {
		return loginType;
	}

	/**
	 * Gets the mailFetchLimit
	 * 
	 * @return the mailFetchLimit
	 */
	final int getMailFetchLimit() {
		return mailFetchLimit;
	}

	/**
	 * Gets the mailServer
	 * 
	 * @return the mailServer
	 */
	final String getMailServer() {
		return mailServer;
	}

	/**
	 * Gets the masterPassword
	 * 
	 * @return the masterPassword
	 */
	final String getMasterPassword() {
		return masterPassword;
	}

	/**
	 * Gets the maxNumOfConnections
	 * 
	 * @return the maxNumOfConnections
	 */
	final int getMaxNumOfConnections() {
		return maxNumOfConnections;
	}

	/**
	 * Gets the quoteLineColors
	 * 
	 * @return the quoteLineColors
	 */
	final String[] getQuoteLineColors() {
		return quoteLineColors;
	}

	/**
	 * Gets the spamEnabled
	 * 
	 * @return the spamEnabled
	 */
	final boolean isSpamEnabled() {
		return spamEnabled;
	}

	/**
	 * Gets the spellCheckConfig
	 * 
	 * @return the spellCheckConfig
	 */
	final SpellCheckConfig getSpellCheckConfig() {
		return spellCheckConfig;
	}

	/**
	 * Gets the transportServer
	 * 
	 * @return the transportServer
	 */
	final String getTransportServer() {
		return transportServer;
	}

	/**
	 * Gets the userFlagsEnabled
	 * 
	 * @return the userFlagsEnabled
	 */
	final boolean isUserFlagsEnabled() {
		return userFlagsEnabled;
	}

	/**
	 * Gets the watcherEnabled
	 * 
	 * @return the watcherEnabled
	 */
	final boolean isWatcherEnabled() {
		return watcherEnabled;
	}

	/**
	 * Gets the watcherFrequency
	 * 
	 * @return the watcherFrequency
	 */
	final int getWatcherFrequency() {
		return watcherFrequency;
	}

	/**
	 * Gets the watcherShallClose
	 * 
	 * @return the watcherShallClose
	 */
	final boolean isWatcherShallClose() {
		return watcherShallClose;
	}

	/**
	 * Gets the watcherTime
	 * 
	 * @return the watcherTime
	 */
	final int getWatcherTime() {
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
