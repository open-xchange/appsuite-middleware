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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.MailException;
import com.openexchange.mail.partmodifier.DummyPartModifier;
import com.openexchange.mail.partmodifier.PartModifier;
import com.openexchange.mail.spellcheck.SpellCheckConfig;
import com.openexchange.mail.spellcheck.SpellCheckConfigParser;
import com.openexchange.sessiond.SessionObject;

/**
 * {@link MailConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailConfig {

	private static final String STR_FALSE = "false";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailConfig.class);

	public static enum BoolCapVal {

		/**
		 * TRUE
		 */
		TRUE("true"),
		/**
		 * FALSE
		 */
		FALSE(STR_FALSE),
		/**
		 * AUTO
		 */
		AUTO("auto");

		private final String str;

		private BoolCapVal(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}

		/**
		 * Parses given capability value. If given value equals ignore-case to
		 * string <code>true</code>, constant {@link #TRUE} will be returned.
		 * Else if given value equals ignore-case to string <code>auto</code>,
		 * constant {@link #AUTO} will be returned. Otherwise {@link #FALSE}
		 * will be returned.
		 * 
		 * @param capVal -
		 *            the string value to parse
		 * @return an instance of <code>BoolCapVal</code>: either
		 *         {@link #TRUE}, {@link #FALSE}, or {@link #AUTO}
		 */
		public final static BoolCapVal parseBoolCapVal(final String capVal) {
			if (TRUE.str.equalsIgnoreCase(capVal)) {
				return TRUE;
			} else if (AUTO.str.equalsIgnoreCase(capVal)) {
				return AUTO;
			}
			return FALSE;
		}
	}

	public static enum CredSrc {
		SESSION("session"), USER_IMAPLOGIN("user.imapLogin"), OTHER("other");

		private final String str;

		private CredSrc(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public static enum LoginType {
		GLOBAL("global"), USER("user"), ANONYMOUS("anonymous");

		private final String str;

		private LoginType(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * Constructor
	 */
	protected MailConfig() {
		super();
	}

	protected static final String PROPERTYNAME = "MailProperties";

	private static final String PROPERTYNAME_JAVAMAIL = "JavaMailProperties";

	/**
	 * The lock that sould be obtained first beforee loading any properties
	 */
	protected static final Lock PROP_LOCK = new ReentrantLock();

	private static final String SPELL_CHECK_CONFIG_FILE = SystemConfig.getProperty("SPELLCHECKCFG");

	private static final Lock GLOBAL_PROP_LOCK = new ReentrantLock();

	private static String mailPropFile;

	private static Properties mailProperties;

	private static boolean mailPropsLoaded;

	private static boolean globalMailPropsLoaded;

	/*
	 * Fields for gloabel properties
	 */
	private static LoginType loginType;

	private static CredSrc credSrc;

	private static String mailServer;

	private static String transportServer;

	private static String masterPassword;

	private static int mailFetchLimit;

	private static int attachDisplaySize;

	private static boolean userFlagsEnabled;

	private static boolean allowNestedDefaultFolderOnAltNamespace;

	private static String defaultMimeCharset;

	private static boolean ignoreSubscription;

	private static boolean spamEnabled;

	private static char defaultSeparator;

	private static int maxNumOfConnections;

	private static String[] quoteLineColors;

	private static SpellCheckConfig spellCheckConfig;

	private static Properties javaMailProperties;

	private static boolean watcherEnabled;

	private static int watcherTime;

	private static int watcherFrequency;

	private static boolean watcherShallClose;

	/*
	 * User-specific fields
	 */
	private String login;

	private String password;

	private AbstractOXException error;

	/**
	 * Checks if the mail properties are loaded. The mail properties are loaded
	 * if not done, yet.
	 * 
	 * @throws MailConfigException
	 *             If mail properties are not defined or cannot be read from
	 *             file
	 */
	protected static final void checkMailPropFile() throws MailConfigException {
		/*
		 * Load mail properties in a thread-safe manner
		 */
		if (!mailPropsLoaded) {
			PROP_LOCK.lock();
			try {
				if (mailPropFile == null && (mailPropFile = SystemConfig.getProperty(PROPERTYNAME)) == null) {
					throw new MailConfigException(new StringBuilder(50).append("Property \"").append(PROPERTYNAME)
							.append("\" not defined in system.properties").toString());
				}
				if (mailProperties == null) {
					loadMailProps();
					mailPropsLoaded = true;
				}
			} finally {
				PROP_LOCK.unlock();
			}
		}
	}

	private static final void loadMailProps() throws MailConfigException {
		mailProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(mailPropFile));
			mailProperties.load(fis);
			fis.close();
			fis = null;
			if (SystemConfig.getProperty(PROPERTYNAME_JAVAMAIL) != null) {
				fis = new FileInputStream(new File(SystemConfig.getProperty(PROPERTYNAME_JAVAMAIL)));
				javaMailProperties = new Properties();
				javaMailProperties.load(fis);
				if (javaMailProperties.size() == 0) {
					javaMailProperties = null;
				}
				fis.close();
				fis = null;
			}
		} catch (final FileNotFoundException e) {
			mailProperties = null;
			throw new MailConfigException(new StringBuilder(256).append("Mail properties not found at location: ")
					.append(mailPropFile).toString(), e);
		} catch (final IOException e) {
			mailProperties = null;
			throw new MailConfigException(new StringBuilder(256).append(
					"I/O error while reading mail properties from file \"").append(mailPropFile).append("\": ").append(
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
	 * Gets a copy of mail properties
	 * 
	 * @return A copy of mail properties
	 * @throws MailConfigException
	 *             If mail properties could not be checked
	 */
	protected static final Properties getProperties() throws MailConfigException {
		checkMailPropFile();
		return (Properties) mailProperties.clone();
	}

	/**
	 * Loads global mail properties
	 * 
	 * @throws MailConfigException
	 *             If gloabal mail properties cannot be loaded
	 */
	public static final void loadGlobalMailProperties() throws MailConfigException {
		loadGlobalMailProperties(true);
	}

	/**
	 * Fills login and password in specified instance of {@link MailConfig}
	 * 
	 * @param mailConfig
	 *            The mail config
	 * @param session
	 *            The session providing needed user data
	 * @throws MailConfigException
	 */
	protected static final void fillLoginAndPassword(final MailConfig mailConfig, final SessionObject session)
			throws MailConfigException {
		checkMailPropFile();
		/*
		 * Load global mail properties if not done, yet
		 */
		loadGlobalMailProperties(false);
		/*
		 * Fetch user object and create its mail properties
		 */
		final User user = session.getUserObject();
		if (LoginType.GLOBAL.equals(getLoginType())) {
			final String masterPw = MailConfig.getProperties().getProperty("masterPassword");
			if (masterPw == null) {
				throw new MailConfigException(new StringBuilder().append("Property \"").append("masterPassword")
						.append("\" not set in ").append(MailConfig.PROPERTYNAME).toString());
			}
			mailConfig.login = user.getMail();
			mailConfig.password = masterPw;
		} else if (LoginType.USER.equals(getLoginType())) {
			if (getCredSrc() == null || CredSrc.SESSION.equals(getCredSrc())) {
				mailConfig.login = getLocalMailLogin(session, false);
				mailConfig.password = session.getPassword();
			} else if (CredSrc.OTHER.equals(getCredSrc())) {
				mailConfig.password = TEST_PW;
				mailConfig.login = getRandomTestLogin();
			} else if (CredSrc.USER_IMAPLOGIN.equals(getCredSrc())) {
				mailConfig.password = session.getPassword();
				mailConfig.login = getLocalMailLogin(session, true);
			}
		} else if (LoginType.ANONYMOUS.equals(getLoginType())) {
			mailConfig.login = LoginType.ANONYMOUS.toString();
			mailConfig.password = "";
		}
	}

	/**
	 * Determines login for session-associated user. If <code>lookUp</code> is
	 * <code>true</code>, this routine tries to fetch the mail login from
	 * {@link User#getImapLogin()} and falls back to session-supplied user login
	 * info. Otherwise session-supplied user login info is directly taken as
	 * return value.
	 * 
	 * @param session -
	 *            the user's session
	 * @param lookUp -
	 *            determines whether to look up {@link User#getImapLogin()} or
	 *            not
	 * @return The session-associated user's login
	 */
	private static final String getLocalMailLogin(final SessionObject session, final boolean lookUp) {
		String login = lookUp ? session.getUserObject().getImapLogin() : null;
		if (login == null || login.length() == 0) {
			login = session.getUserlogin() != null && session.getUserlogin().length() > 0 ? session.getUserlogin()
					: session.getUsername();
		}
		return login;
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
	protected static final void loadGlobalMailProperties(final boolean checkPropFile) throws MailConfigException {
		if (!globalMailPropsLoaded) {
			GLOBAL_PROP_LOCK.lock();
			try {
				if (globalMailPropsLoaded) {
					return;
				}
				if (checkPropFile) {
					checkMailPropFile();
				}
				final StringBuilder logBuilder = new StringBuilder(1024);
				logBuilder.append("\nLoading global mail properties...\n");

				{
					final String loginTypeStr = mailProperties.getProperty("loginType");
					if (loginTypeStr == null) {
						throw new MailConfigException(new StringBuilder(128).append(
								"Property \"loginType\" not set in ").append(mailPropFile).toString());
					}
					if (LoginType.GLOBAL.toString().equalsIgnoreCase(loginTypeStr)) {
						loginType = LoginType.GLOBAL;
					} else if (LoginType.USER.toString().equalsIgnoreCase(loginTypeStr)) {
						loginType = LoginType.USER;
					} else if (LoginType.ANONYMOUS.toString().equalsIgnoreCase(loginTypeStr)) {
						loginType = LoginType.ANONYMOUS;
					} else {
						throw new MailConfigException(new StringBuilder(256).append(
								"Unknown value in property \"loginType\" set in ").append(mailPropFile).append(": ")
								.append(loginTypeStr).toString());
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
						throw new MailConfigException(new StringBuilder(256).append(
								"Unknown value in property \"credSrc\" set in ").append(mailPropFile).append(": ")
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
						logBuilder.append("\tMail Fetch Limit: Non parseable value \"").append(mailFetchLimitStr)
								.append("\". Setting to fallback: ").append(mailFetchLimit).append('\n');
					}
				}

				{
					final String attachDisplaySizeStr = mailProperties
							.getProperty("attachmentDisplaySizeLimit", "8192").trim();
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
					final String defaultMimeCharsetStr = mailProperties.getProperty("mail.mime.charset", "UTF-8")
							.trim();
					/*
					 * Check validity
					 */
					try {
						Charset.forName(defaultMimeCharsetStr);
						defaultMimeCharset = defaultMimeCharsetStr;
						logBuilder.append("\tDefault MIME Charset: ").append(defaultMimeCharset).append('\n');
					} catch (final Throwable t) {
						defaultMimeCharset = "UTF-8";
						logBuilder.append("\tDefault MIME Charset: Unsupported charset \"").append(
								defaultMimeCharsetStr).append("\". Setting to fallback: ").append(defaultMimeCharset)
								.append('\n');
					}
					/*
					 * Add to system properties, too
					 */
					System.getProperties().setProperty("mail.mime.charset", defaultMimeCharset);
					logBuilder.append("\tDefault MIME Charset: ").append(defaultMimeCharset).append('\n');
				}

				{
					final String ignoreSubsStr = mailProperties.getProperty("ignoreSubscription", STR_FALSE).trim();
					ignoreSubscription = Boolean.parseBoolean(ignoreSubsStr);
					logBuilder.append("\tIgnore Folder Subscription: ").append(ignoreSubscription).append('\n');
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
								(int) defaultSep).append("). Setting to fallback: ").append(defaultSeparator).append(
								'\n');
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
						logBuilder.append("\tPartModifier Implementation: Unknown class \"").append(partModifierStr)
								.append("\". Setting to fallback: ").append(DummyPartModifier.class.getName()).append(
										'\n');
					}
				}

				{
					final String quoteColors = mailProperties.getProperty("quoteLineColors", "#666666").trim();
					if (Pattern.matches("((#[0-9a-fA-F&&[^,]]{6})(?:\r?\n|\\z|\\s*,\\s*))+", quoteColors)) {
						quoteLineColors = quoteColors.split("\\s*,\\s*");
						logBuilder.append("\tHTML Quote Colors: ").append(quoteColors).append('\n');
					} else {
						quoteLineColors = new String[] { "#666666" };
						logBuilder.append("\tHTML Quote Colors: Invalid sequence of colors \"").append(quoteColors)
								.append("\". Setting to fallback: #666666").append('\n');
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

				/*
				 * Load & parse spell check config
				 */
				if (SPELL_CHECK_CONFIG_FILE == null) {
					throw new MailConfigException("Property \"SPELLCHECKCFG\" is not defined in system.properties");
				}
				try {
					spellCheckConfig = new SpellCheckConfigParser().parseSpellCheckConfig(SPELL_CHECK_CONFIG_FILE);
					logBuilder.append("\tSpellCheck Config File: ").append(SPELL_CHECK_CONFIG_FILE).append('\n');
				} catch (final Exception e) {
					LOG.error("SpellCheck config file \"" + SPELL_CHECK_CONFIG_FILE
							+ "\" could not be properly loaded & parsed:\n" + e.getMessage(), e);
				}

				logBuilder.append("\tJavaMail Properties loaded: ").append(javaMailProperties != null).append('\n');

				/*
				 * Switch flag
				 */
				globalMailPropsLoaded = true;
				logBuilder.append("Global mail properties successfully loaded!");
				if (LOG.isInfoEnabled()) {
					LOG.info(logBuilder.toString());
				}
			} finally {
				GLOBAL_PROP_LOCK.unlock();
			}
		}
	}

	/**
	 * @return <code>true</code> if global mail properties have already been
	 *         loaded; otherwise <code>false</code>
	 */
	public static boolean isGlobalMailPropsLoaded() {
		return globalMailPropsLoaded;
	}

	/**
	 * Gets the loginType
	 * 
	 * @return the loginType
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final LoginType getLoginType() throws MailConfigException {
		loadGlobalMailProperties();
		return loginType;
	}

	/**
	 * Gets the allowNestedDefaultFolderOnAltNamespace
	 * 
	 * @return the allowNestedDefaultFolderOnAltNamespace
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final boolean isAllowNestedDefaultFolderOnAltNamespace() throws MailConfigException {
		loadGlobalMailProperties();
		return allowNestedDefaultFolderOnAltNamespace;
	}

	/**
	 * Gets the attachDisplaySize
	 * 
	 * @return the attachDisplaySize
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final int getAttachDisplaySize() throws MailConfigException {
		loadGlobalMailProperties();
		return attachDisplaySize;
	}

	/**
	 * Gets the credSrc
	 * 
	 * @return the credSrc
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final CredSrc getCredSrc() throws MailConfigException {
		loadGlobalMailProperties();
		return credSrc;
	}

	/**
	 * Gets the mailServer
	 * 
	 * @return the mailServer
	 * @throws MailConfigException
	 */
	public static final String getMailServer() throws MailConfigException {
		loadGlobalMailProperties();
		return mailServer;
	}

	/**
	 * Gets the transportServer
	 * 
	 * @return the transportServer
	 * @throws MailConfigException
	 */
	public static final String getTransportServer() throws MailConfigException {
		loadGlobalMailProperties();
		return transportServer;
	}

	/**
	 * Gets the masterPassword
	 * 
	 * @return the masterPassword
	 * @throws MailConfigException
	 */
	public static final String getMasterPassword() throws MailConfigException {
		loadGlobalMailProperties();
		return masterPassword;
	}

	/**
	 * Gets the defaultMimeCharset
	 * 
	 * @return the defaultMimeCharset
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final String getDefaultMimeCharset() throws MailConfigException {
		loadGlobalMailProperties();
		return defaultMimeCharset;
	}

	/**
	 * Gets the defaultSeparator
	 * 
	 * @return the defaultSeparator
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final char getDefaultSeparator() throws MailConfigException {
		loadGlobalMailProperties();
		return defaultSeparator;
	}

	/**
	 * Gets the maxNumOfConnections
	 * 
	 * @return the maxNumOfConnections
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final int getMaxNumOfConnections() throws MailConfigException {
		loadGlobalMailProperties();
		return maxNumOfConnections;
	}

	/**
	 * Gets the ignoreSubscription
	 * 
	 * @return the ignoreSubscription
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final boolean isIgnoreSubscription() throws MailConfigException {
		loadGlobalMailProperties();
		return ignoreSubscription;
	}

	/**
	 * Gets the mailFetchLimit
	 * 
	 * @return the mailFetchLimit
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final int getMailFetchLimit() throws MailConfigException {
		loadGlobalMailProperties();
		return mailFetchLimit;
	}

	/**
	 * Gets the partModifier
	 * 
	 * @return the partModifier
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final PartModifier getPartModifier() throws MailConfigException {
		loadGlobalMailProperties();
		return PartModifier.getInstance();
	}

	/**
	 * Gets the quoteLineColors
	 * 
	 * @return the quoteLineColors
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final String[] getQuoteLineColors() throws MailConfigException {
		loadGlobalMailProperties();
		return quoteLineColors;
	}

	/**
	 * Gets the spamEnabled
	 * 
	 * @return the spamEnabled
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final boolean isSpamEnabled() throws MailConfigException {
		loadGlobalMailProperties();
		return spamEnabled;
	}

	/**
	 * Gets the spellCheckConfig
	 * 
	 * @return the spellCheckConfig
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final SpellCheckConfig getSpellCheckConfig() throws MailConfigException {
		loadGlobalMailProperties();
		return spellCheckConfig;
	}

	/**
	 * Gets the userFlagsEnabled
	 * 
	 * @return the userFlagsEnabled
	 * @throws MailConfigException
	 *             If global mail properties cannot be loaded
	 */
	public static final boolean isUserFlagsEnabled() throws MailConfigException {
		loadGlobalMailProperties();
		return userFlagsEnabled;
	}

	/**
	 * Gets the javaMailProperties
	 * 
	 * @return the javaMailProperties
	 * @throws MailConfigException
	 */
	public static Properties getJavaMailProperties() throws MailConfigException {
		loadGlobalMailProperties();
		return javaMailProperties;
	}

	/**
	 * Gets the watcherEnabled
	 * 
	 * @return the watcherEnabled
	 * @throws MailConfigException
	 */
	public static boolean isWatcherEnabled() throws MailConfigException {
		loadGlobalMailProperties();
		return watcherEnabled;
	}

	/**
	 * Gets the watcherFrequency
	 * 
	 * @return the watcherFrequency
	 * @throws MailConfigException
	 */
	public static int getWatcherFrequency() throws MailConfigException {
		loadGlobalMailProperties();
		return watcherFrequency;
	}

	/**
	 * Gets the watcherShallClose
	 * 
	 * @return the watcherShallClose
	 * @throws MailConfigException
	 */
	public static boolean isWatcherShallClose() throws MailConfigException {
		loadGlobalMailProperties();
		return watcherShallClose;
	}

	/**
	 * Gets the watcherTime
	 * 
	 * @return the watcherTime
	 * @throws MailConfigException
	 */
	public static int getWatcherTime() throws MailConfigException {
		loadGlobalMailProperties();
		return watcherTime;
	}

	/**
	 * Gets the login
	 * 
	 * @return the login
	 */
	public final String getLogin() {
		return login;
	}

	/**
	 * Gets the password
	 * 
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * @return The host name or IP address of the server
	 */
	public abstract String getServer();

	/**
	 * @return The port of the server obtained via {@link #getServer()}
	 */
	public abstract int getPort();
	
	/**
	 * @return Gets the encoded capabilities
	 */
	public abstract int getCapabilities();

	/**
	 * Gets occured error
	 * 
	 * @return the error (if any) or <code>null</code>
	 */
	public AbstractOXException getError() {
		return error;
	}

	/*
	 * TEST TEST TEST TEST TEST
	 */

	protected static final String TEST_PW = "oxTEST";

	private static Map<Integer, String> TEST_LOGIN_MAP;

	static {
		final StringBuilder sb = new StringBuilder();
		TEST_LOGIN_MAP = new HashMap<Integer, String>();
		for (int i = 1; i <= 10000; i++) {
			TEST_LOGIN_MAP.put(Integer.valueOf(i), sb.append("ox-test-").append(i).append("@nms112.de").toString());
			sb.setLength(0);
		}
	}

	private static final Random RANDOM = new Random();

	protected static String getRandomTestLogin() {
		final int num = 1 + Math.abs(RANDOM.nextInt()) % 10000;
		return TEST_LOGIN_MAP.get(Integer.valueOf(num));
	}
}
