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

package com.openexchange.groupware.imap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.imap.IMAPProperties.BoolCapVal;
import com.openexchange.groupware.imap.User2IMAP.User2IMAPException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.sessiond.SessionObject;

/**
 * IMAPPropertiesFactory
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class IMAPPropertiesFactory {

	public static enum IMAPCredSrc {
		SESSION("session"), USER_IMAPLOGIN("user.imapLogin"), OTHER("other");

		private final String str;

		private IMAPCredSrc(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public static enum IMAPLoginType {
		GLOBAL("global"), USER("user"), ANONYMOUS("anonymous");

		private final String str;

		private IMAPLoginType(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	private static Properties props;

	private static boolean propsLoaded;

	private static Properties javaMailProps;

	private static final Lock PROP_LOCK = new ReentrantLock();

	private static final String PROPERTYNAME_IMAP = "IMAPPROPERTIES";

	private static final String PROPERTYNAME_JAVAMAIL = "JAVAMAILPROPERTIES";

	private static String PROP_FILE;

	private static final String PROP_LOGINTYPE = "imapLoginType";

	private static final String PROP_MASTERPW = "imapMasterPassword";

	private static final String PROP_CREDSRC = "imapCredSrc";

	public static final String PROP_IMAPSERVER = "imapServer";

	private static final String PROP_SMTPSERVER = "smtpServer";

	private static final String PROP_SMTPLOCALHOST = "smtpLocalhost";

	private static final String PROP_IMAPS_ENABLED = "imaps";

	private static final String PROP_IMAPS_PORT = "imapsPort";

	private static final String PROP_SMTPS_ENABLED = "smtps";

	private static final String PROP_SMTPS_PORT = "smtpsPort";

	private static final String PROP_IMAPSORT = "imapSort";

	private static final String PROP_IMAPSEARCH = "imapSearch";

	private static final String PROP_IMAP_MESSAGE_FETCH_LIMIT = "imapMessageFetchLimit";

	private static final String PROP_IMAP_ATTACHMENT_DISPLAY_SIZE_LIMIT = "imapAttachmentDisplaySizeLimit";

	private static final String PROP_IMAP_QUOTE_LINE_COLORS = "imapQuoteLineColors";

	private static final String PROP_IMAP_SUPPORTS_ACL = "imapSupportsACL";

	private static final String PROP_IMAP_AUTH_ENC = "imapAuthEnc";

	private static final String PROP_IMAP_PART_MODIFIER = "partModifierImpl";

	private static final String PROP_IMAP_SMTP_AUTH = "smtpAuthentication";

	private static final String PROP_IMAP_TIMEOUT = "imapTimeout";

	private static final String PROP_IMAP_CONNECTION_TIMEOUT = "imapConnectionTimeout";

	private static final String PROP_IMAP_USER_FLAGS_ENABLED = "userFlagsEnabled";

	private static final String PROP_MAX_IMAP_CON_IDLE_TIME = "maxIMAPConnectionIdleTime";

	private static final String PROP_MAX_NUM_OF_IMAP_CONS = "imapMaxNumOfConnections";

	private static final String PROP_ALLOW_NESTED_DEFAULT_FOLDERS = "allowNestedDefaultFolderOnAltNamespace";

	private static final String PROP_MIME_CHARSET = "mail.mime.charset";

	private static final String PROP_IGNORE_SUBSCRIPTION = "ignoreSubscription";

	private static final String PROP_SET_SMTP_ENVELOPE_FROM = "setSMTPEnvelopeFrom";

	private static final String PROP_SPAM_ENABLED = "spamEnabled";

	private static final String PROP_WATCHER_ENABLED = "watcherEnabled";

	private static final String PROP_WATCHER_TIME = "watcherTime";

	private static final String PROP_WATCHER_FREQUENCY = "watcherFrequency";

	private static final String PROP_WATCHER_SHALL_CLOSE = "watcherShallClose";

	private static final String SPELL_CHECK_CONFIG_FILE = SystemConfig.getProperty("SPELLCHECKCFG");

	private static final Log LOG = LogFactory.getLog(IMAPPropertiesFactory.class);

	// ##################################################
	private static final String TEST_PW = "oxTEST";

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

	private IMAPPropertiesFactory() {
		super();
	}

	private static String getRandomTestLogin() {
		final int num = 1 + Math.abs(RANDOM.nextInt()) % 10000;
		return TEST_LOGIN_MAP.get(Integer.valueOf(num));
	}

	private static void checkImapPropFile() throws IMAPException {
		/*
		 * Load properties in a thread-safe manner
		 */
		if (!propsLoaded) {
			PROP_LOCK.lock();
			try {
				if (PROP_FILE == null && (PROP_FILE = SystemConfig.getProperty(PROPERTYNAME_IMAP)) == null) {
					throw new IMAPException(new StringBuilder(50).append("Property \"").append(PROPERTYNAME_IMAP)
							.append("\" not defined in system.properties").toString());
				}
				if (props == null) {
					loadProps();
					propsLoaded = true;
				}
			} finally {
				PROP_LOCK.unlock();
			}
		}
	}

	private static final void loadProps() throws IMAPException {
		props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(PROP_FILE));
			props.load(fis);
			fis.close();
			fis = null;
			if (SystemConfig.getProperty(PROPERTYNAME_JAVAMAIL) != null) {
				fis = new FileInputStream(new File(SystemConfig.getProperty(PROPERTYNAME_JAVAMAIL)));
				javaMailProps = new Properties();
				javaMailProps.load(fis);
				if (javaMailProps.size() == 0) {
					javaMailProps = null;
				}
			}
		} catch (final FileNotFoundException e) {
			props = null;
			throw new IMAPException(new StringBuilder(300).append("IMAP properties not found at location: ").append(
					PROP_FILE).toString(), e);
		} catch (final IOException e) {
			props = null;
			throw new IMAPException(new StringBuilder(300).append(
					"I/O error while reading IMAP properties from file \"").append(PROP_FILE).append("\": ").append(
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
				fis = null;
			}
		}
	}
	
	public static final Properties getProperties() throws IMAPException {
		checkImapPropFile();
		return (Properties) props.clone();
	}

	private static final String STR_PROPERTY = "Property ";

	private static final String STR_NOTSETIN = " NOT set in ";

	public static final IMAPProperties getImapProperties(final SessionObject sessionObj) throws IMAPException {
		checkImapPropFile();
		/*
		 * Load global IMAP properties if not done, yet
		 */
		loadGlobalImapProperties(false);
		/*
		 * Fetch user object and create its IMAP properties
		 */
		final User userObj = sessionObj.getUserObject();
		final IMAPProperties imapProps = new IMAPProperties(sessionObj.getUserObject().getId());
		if (IMAPLoginType.GLOBAL.equals(IMAPProperties.getImapLoginTypeInternal())) {
			String imapServer = props.getProperty(PROP_IMAPSERVER);
			if (imapServer == null) {
				throw new IMAPException(new StringBuilder().append(STR_PROPERTY).append(PROP_IMAPSERVER).append(
						STR_NOTSETIN).append(PROP_FILE).toString());
			}
			int imapPort = 143;
			int pos = imapServer.indexOf(':');
			if (pos > -1) {
				imapPort = Integer.parseInt(imapServer.substring(pos + 1));
				imapServer = imapServer.substring(0, pos);
			}
			final String masterPw = props.getProperty(PROP_MASTERPW);
			if (masterPw == null) {
				throw new IMAPException(new StringBuilder().append(STR_PROPERTY).append(PROP_MASTERPW).append(
						STR_NOTSETIN).append(PROP_FILE).toString());
			}
			String smtpServer = props.getProperty(PROP_SMTPSERVER);
			if (smtpServer == null) {
				throw new IMAPException(new StringBuilder().append(STR_PROPERTY).append(PROP_SMTPSERVER).append(
						STR_NOTSETIN).append(PROP_FILE).toString());
			}
			int smtpPort = 25;
			pos = smtpServer.indexOf(':');
			if (pos > -1) {
				smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
				smtpServer = smtpServer.substring(0, pos);
			}
			imapProps.setImapPassword(masterPw);
			imapProps.setImapServer(imapServer);
			imapProps.setImapPort(imapPort);
			imapProps.setSmtpServer(smtpServer);
			imapProps.setSmtpPort(smtpPort);
			/*
			 * Set login to user's email address
			 */
			imapProps.setImapLogin(userObj.getMail());
		} else if (IMAPLoginType.USER.equals(IMAPProperties.getImapLoginTypeInternal())) {
			String imapServer = userObj.getImapServer();
			int imapPort = 143;
			int pos = imapServer.indexOf(':');
			if (pos > -1) {
				imapPort = Integer.parseInt(imapServer.substring(pos + 1));
				imapServer = imapServer.substring(0, pos);
			}
			String smtpServer = userObj.getSmtpServer();
			int smtpPort = 25;
			pos = smtpServer.indexOf(':');
			if (pos > -1) {
				smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
				smtpServer = smtpServer.substring(0, pos);
			}
			imapProps.setImapServer(imapServer);
			imapProps.setImapPort(imapPort);
			imapProps.setSmtpServer(smtpServer);
			imapProps.setSmtpPort(smtpPort);
			try {
				if (IMAPProperties.getImapCredSrcInternal() == null
						|| IMAPCredSrc.SESSION.equals(IMAPProperties.getImapCredSrcInternal())) {
					imapProps.setImapPassword(sessionObj.getPassword());
					imapProps.setImapLogin(User2IMAP.getInstance(userObj).getLocalIMAPLogin(sessionObj, false));
				} else if (IMAPCredSrc.OTHER.equals(IMAPProperties.getImapCredSrcInternal())) {
					imapProps.setImapPassword(TEST_PW);
					imapProps.setImapLogin(getRandomTestLogin());
				} else if (IMAPCredSrc.USER_IMAPLOGIN.equals(IMAPProperties.getImapCredSrcInternal())) {
					imapProps.setImapPassword(sessionObj.getPassword());
					imapProps.setImapLogin(User2IMAP.getInstance(userObj).getLocalIMAPLogin(sessionObj, true));
				}
			} catch (final User2IMAPException e) {
				throw new IMAPException(e);
			}
		} else if (IMAPLoginType.ANONYMOUS.equals(IMAPProperties.getImapLoginTypeInternal())) {
			String imapServer = userObj.getImapServer();
			int imapPort = 143;
			int pos = imapServer.indexOf(':');
			if (pos > -1) {
				imapPort = Integer.parseInt(imapServer.substring(pos + 1));
				imapServer = imapServer.substring(0, pos);
			}
			String smtpServer = userObj.getSmtpServer();
			int smtpPort = 25;
			pos = smtpServer.indexOf(':');
			if (pos > -1) {
				smtpPort = Integer.parseInt(smtpServer.substring(pos + 1));
				smtpServer = smtpServer.substring(0, pos);
			}
			imapProps.setImapLogin(IMAPLoginType.ANONYMOUS.toString());
			imapProps.setImapPassword("");
			imapProps.setImapServer(imapServer);
			imapProps.setImapPort(imapPort);
			imapProps.setSmtpServer(smtpServer);
			imapProps.setSmtpPort(smtpPort);
		}
		return imapProps;
	}

	/**
	 * Loads global IMAP properties
	 */
	public static void loadGlobalImapProperties() throws IMAPException {
		loadGlobalImapProperties(true);
	}

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	private static final String STR_IMAP = "IMAP";

	private static final Lock GLOBAL_PROP_LOCK = new ReentrantLock();

	/**
	 * Loads global IMAP properties. <code>checkPropFile</code> determines
	 * whether an availability check for IMAP property file is done or not.
	 */
	private static void loadGlobalImapProperties(final boolean checkPropFile) throws IMAPException {
		if (!IMAPProperties.isGlobalPropertiesLoaded()) {
			GLOBAL_PROP_LOCK.lock();
			try {
				if (IMAPProperties.isGlobalPropertiesLoaded()) {
					return;
				}
				if (checkPropFile) {
					checkImapPropFile();
				}
				final StringBuilder logBuilder = new StringBuilder();
				logBuilder.append("\nLoading global IMAP properties...\n");
				IMAPProperties.setImapSort(STR_IMAP.equalsIgnoreCase(props.getProperty(PROP_IMAPSORT)));
				logBuilder.append("\tIMAP-Sort: ").append(IMAPProperties.isImapSortInternal()).append('\n');

				IMAPProperties.setImapSearch(STR_IMAP.equalsIgnoreCase(props.getProperty(PROP_IMAPSEARCH)));
				logBuilder.append("\tIMAP-Search: ").append(IMAPProperties.isImapSearchInternal()).append('\n');

				final String propSmtpLocalhost = props.getProperty(PROP_SMTPLOCALHOST);
				IMAPProperties.setSmtpLocalhost(propSmtpLocalhost == null || propSmtpLocalhost.length() == 0
						|| "null".equalsIgnoreCase(propSmtpLocalhost) ? null : propSmtpLocalhost);
				logBuilder.append("\tSMTP Localhost: ").append(IMAPProperties.getSmtpLocalhostInternal()).append('\n');

				final String loginType = props.getProperty(PROP_LOGINTYPE);
				if (loginType == null) {
					throw new IMAPException(new StringBuilder().append(STR_PROPERTY).append(PROP_LOGINTYPE).append(
							STR_NOTSETIN).append(PROP_FILE).toString());
				}
				if (IMAPLoginType.GLOBAL.toString().equalsIgnoreCase(loginType)) {
					IMAPProperties.setImapLoginType(IMAPLoginType.GLOBAL);
				} else if (IMAPLoginType.USER.toString().equalsIgnoreCase(loginType)) {
					IMAPProperties.setImapLoginType(IMAPLoginType.USER);
				} else if (IMAPLoginType.ANONYMOUS.toString().equalsIgnoreCase(loginType)) {
					IMAPProperties.setImapLoginType(IMAPLoginType.ANONYMOUS);
				} else {
					throw new IMAPException("Unknown value in property " + PROP_LOGINTYPE + " set in " + PROP_FILE
							+ ": " + loginType);
				}
				logBuilder.append("\tIMAP Login Type: ").append(IMAPProperties.getImapLoginTypeInternal().toString())
						.append('\n');

				final String credSrc = props.getProperty(PROP_CREDSRC);
				if (credSrc == null || credSrc.equalsIgnoreCase(IMAPCredSrc.SESSION.toString())) {
					IMAPProperties.setImapCredSrc(IMAPCredSrc.SESSION);
				} else if (credSrc.equalsIgnoreCase(IMAPCredSrc.OTHER.toString())) {
					IMAPProperties.setImapCredSrc(IMAPCredSrc.OTHER);
				} else if (credSrc.equalsIgnoreCase(IMAPCredSrc.USER_IMAPLOGIN.toString())) {
					IMAPProperties.setImapCredSrc(IMAPCredSrc.USER_IMAPLOGIN);
				} else {
					throw new IMAPException("Unknown value in property " + PROP_CREDSRC + " set in " + PROP_FILE + ": "
							+ credSrc);
				}
				logBuilder.append("\tIMAP Credentials Source: ").append(
						IMAPProperties.getImapCredSrcInternal().toString()).append('\n');

				IMAPProperties.setMessageFetchLimit(Integer.parseInt(props.getProperty(PROP_IMAP_MESSAGE_FETCH_LIMIT,
						"1000")));
				logBuilder.append("\tMessage Fetch Limit: ").append(IMAPProperties.getMessageFetchLimitInternal())
						.append('\n');

				IMAPProperties.setAttachmentDisplaySizeLimit(Integer.parseInt(props.getProperty(
						PROP_IMAP_ATTACHMENT_DISPLAY_SIZE_LIMIT, "8192")));
				logBuilder.append("\tAttachment Display Size Limit: ").append(
						IMAPProperties.getAttachmentDisplaySizeLimitInternal()).append('\n');

				IMAPProperties
						.setSmtpAuth(STR_TRUE.equalsIgnoreCase(props.getProperty(PROP_IMAP_SMTP_AUTH, STR_FALSE)));
				logBuilder.append("\tSMTP Authentication: ").append(IMAPProperties.isSmtpAuthInternal()).append('\n');

				IMAPProperties.setImapsEnabled(STR_TRUE.equalsIgnoreCase(props.getProperty(PROP_IMAPS_ENABLED,
						STR_FALSE)));
				logBuilder.append("\tIMAP/S enabled: ").append(IMAPProperties.isImapsEnabledInternal()).append('\n');

				IMAPProperties.setImapsPort(Integer.parseInt(props.getProperty(PROP_IMAPS_PORT, "993")));
				logBuilder.append("\tIMAP/S port: ").append(IMAPProperties.getImapsPortInternal()).append('\n');

				IMAPProperties.setSmtpsEnabled(STR_TRUE.equalsIgnoreCase(props.getProperty(PROP_SMTPS_ENABLED,
						STR_FALSE)));
				logBuilder.append("\tSMTP/S enabled: ").append(IMAPProperties.isSmtpsEnabledInternal()).append('\n');

				IMAPProperties.setSmtpsPort(Integer.parseInt(props.getProperty(PROP_SMTPS_PORT, "465")));
				logBuilder.append("\tSMTP/S port: ").append(IMAPProperties.getSmtpsPortInternal()).append('\n');

				IMAPProperties.setSupportsACLs(BoolCapVal.parseBoolCapVal(props.getProperty(PROP_IMAP_SUPPORTS_ACL,
						STR_FALSE)));
				logBuilder.append("\tSupport ACLs: ").append(IMAPProperties.isSupportsACLsInternal()).append('\n');

				IMAPProperties.setImapTimeout(Integer.parseInt(props.getProperty(PROP_IMAP_TIMEOUT, "0")));
				logBuilder.append("\tIMAP Timeout: ").append(IMAPProperties.getImapTimeoutInternal()).append('\n');

				IMAPProperties.setImapConnectionTimeout(Integer.parseInt(props.getProperty(
						PROP_IMAP_CONNECTION_TIMEOUT, "0")));
				logBuilder.append("\tIMAP Connection Timeout: ").append(
						IMAPProperties.getImapConnectionTimeoutInternal()).append('\n');

				IMAPProperties.setUserFlagsEnabled(STR_TRUE.equalsIgnoreCase(props.getProperty(
						PROP_IMAP_USER_FLAGS_ENABLED, STR_FALSE)));
				logBuilder.append("\tUser Flags Enabled: ").append(IMAPProperties.isUserFlagsEnabledInternal()).append(
						'\n');

				IMAPProperties.setMaxIMAPConnectionIdleTime(Integer.parseInt(props.getProperty(
						PROP_MAX_IMAP_CON_IDLE_TIME, "60000")));
				logBuilder.append("\tMax IMAP Connection Idle Time: ").append(
						IMAPProperties.getMaxIMAPConnectionIdleTimeInternal()).append('\n');

				IMAPProperties.setMaxNumOfIMAPConnections(Integer.parseInt(props.getProperty(PROP_MAX_NUM_OF_IMAP_CONS,
						"0")));
				logBuilder.append("\tMax Number Of IMAP Connections: ").append(
						IMAPProperties.getMaxNumOfIMAPConnectionsInternal()).append('\n');

				IMAPProperties.setAllowNestedDefaultFolderOnAltNamespace(STR_TRUE.equalsIgnoreCase(props.getProperty(
						PROP_ALLOW_NESTED_DEFAULT_FOLDERS, STR_FALSE)));
				logBuilder.append("\tAllow Nested Default Folders on AltNamespace: ").append(
						IMAPProperties.isAllowNestedDefaultFolderOnAltNamespaceInternal()).append('\n');

				IMAPProperties.setDefaultMimeCharset(props.getProperty(PROP_MIME_CHARSET, "UTF-8"));
				logBuilder.append("\tDefault MIME Charset: ").append(IMAPProperties.getDefaultMimeCharsetInternal())
						.append('\n');

				IMAPProperties.setIgnoreSubscription(Boolean.parseBoolean(props.getProperty(PROP_IGNORE_SUBSCRIPTION,
						STR_FALSE)));
				logBuilder.append("\tIgnore Folder Subscription: ").append(
						IMAPProperties.isIgnoreSubscriptionInternal()).append('\n');

				IMAPProperties.setSMTPEnvelopeFrom(Boolean.parseBoolean(props.getProperty(PROP_SET_SMTP_ENVELOPE_FROM,
						STR_FALSE)));
				logBuilder.append("\tSet SMTP ENVELOPE-FROM: ").append(IMAPProperties.isSMTPEnvelopeFromInternal())
						.append('\n');

				IMAPProperties.setSpamEnabled(Boolean.parseBoolean(props.getProperty(PROP_SPAM_ENABLED, STR_FALSE)));
				logBuilder.append("\tSpam Enabled: ").append(IMAPProperties.isSpamEnabledInternal()).append('\n');

				IMAPProperties.setImapAuthEnc(props.getProperty(PROP_IMAP_AUTH_ENC, "UTF-8"));
				logBuilder.append("\tAuthentication Encoding: ").append(IMAPProperties.getImapAuthEncInternal())
						.append('\n');

				IMAPProperties.setWatcherEnabled(STR_TRUE.equalsIgnoreCase(props.getProperty(PROP_WATCHER_ENABLED,
						STR_FALSE).trim()));
				logBuilder.append("\tWatcher Enabled: ").append(IMAPProperties.isWatcherEnabledInternal()).append('\n');

				IMAPProperties.setWatcherTime(Integer.parseInt(props.getProperty(PROP_WATCHER_TIME, "60000").trim()));
				logBuilder.append("\tWatcher Time: ").append(IMAPProperties.getWatcherTimeInternal()).append('\n');

				IMAPProperties.setWatcherFrequency(Integer.parseInt(props.getProperty(PROP_WATCHER_FREQUENCY, "10000")
						.trim()));
				logBuilder.append("\tWatcher Frequency: ").append(IMAPProperties.getWatcherFrequencyInternal()).append(
						'\n');

				IMAPProperties.setWatcherShallClose(STR_TRUE.equalsIgnoreCase(props.getProperty(
						PROP_WATCHER_SHALL_CLOSE, STR_FALSE).trim()));
				logBuilder.append("\tWatcher Shall Close: ").append(IMAPProperties.isWatcherShallCloseInternal())
						.append('\n');

				IMAPProperties.setJavaMailProperties(javaMailProps);
				logBuilder.append("\tJavaMail Properties loaded: ").append(
						IMAPProperties.getJavaMailPropertiesInternal() != null).append('\n');

				try {
					IMAPProperties.setPartModifierImpl(PartModifier.getImpl(props.getProperty(PROP_IMAP_PART_MODIFIER,
							"com.openexchange.groupware.imap.DummyPartModifier")));
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
					IMAPProperties.setPartModifierImpl(new DummyPartModifier());
				}
				logBuilder.append("\tPartModifier Implementation: ").append(
						IMAPProperties.getPartModifierImplInternal().getClass().getName()).append('\n');

				final String quoteColors = props.getProperty(PROP_IMAP_QUOTE_LINE_COLORS, "#666666");
				logBuilder.append("\tHTML Quote Colors: ").append(quoteColors).append('\n');
				final String[] colors = quoteColors.split(" *, *");
				IMAPProperties.setQuoteLineColors(colors);
				/*
				 * Load & parse spell check config
				 */
				if (SPELL_CHECK_CONFIG_FILE == null) {
					throw new IMAPException("Property \"SPELLCHECKCFG\" is not defined in system.properties");
				}
				try {
					IMAPProperties.setSpellCheckConfig(new SpellCheckConfigParser()
							.parseSpellCheckConfig(SPELL_CHECK_CONFIG_FILE));
					logBuilder.append("\tSpellCheck Config File: ").append(SPELL_CHECK_CONFIG_FILE).append('\n');
				} catch (final Exception e) {
					LOG.error("SpellCheck config file \"" + SPELL_CHECK_CONFIG_FILE
							+ "\" could not be properly loaded & parsed:\n" + e.getMessage(), e);
				}
				/*
				 * Switch flag
				 */
				IMAPProperties.setGlobalPropertiesLoaded(true);
				logBuilder.append("Global IMAP properties successfully loaded!");
				if (LOG.isInfoEnabled()) {
					LOG.info(logBuilder.toString());
				}
			} finally {
				GLOBAL_PROP_LOCK.unlock();
			}
		}
	}

}
