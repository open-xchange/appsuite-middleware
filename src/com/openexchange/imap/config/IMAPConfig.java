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

package com.openexchange.imap.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.ldap.User;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPConfig extends MailConfig {

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPConfig.class);

	private static final String PROPERTYNAME = "IMAPProperties";

	private static final Lock GLOBAL_PROP_LOCK = new ReentrantLock();

	private static String imapPropFile;

	private static Properties imapProperties;

	private static boolean imapPropsLoaded;

	private static boolean globalImapPropsLoaded;

	/*
	 * Fields for global properties
	 */
	private static boolean imapSort;

	private static boolean imapSearch;

	private static boolean fastFetch;

	private static boolean imapsEnabled;

	private static int imapsPort;

	private static BoolCapVal supportsACLs;

	private static int imapTimeout;

	private static int imapConnectionTimeout;

	private static int imapConnectionIdleTime;

	private static String spamHandlerClass;

	private static String imapAuthEnc;

	private static String user2AclImpl;

	private static Map<String, Boolean> newACLExtMap = new ConcurrentHashMap<String, Boolean>();

	/*
	 * User-specific fields
	 */
	private String imapServer;

	private int imapPort;

	private final AtomicBoolean capabilitiesLoaded = new AtomicBoolean();

	private IMAPCapabilities imapCapabilities;

	/**
	 * Default constructor
	 */
	private IMAPConfig() {
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
	public static IMAPConfig getImapConfig(final SessionObject session) throws MailConfigException {
		final IMAPConfig imapConf = new IMAPConfig();
		fillLoginAndPassword(imapConf, session);
		/*
		 * Check IMAP prop file
		 */
		checkImapPropFile();
		/*
		 * Load global IMAP properties if not done, yet
		 */
		loadGlobalImapProperties(false);
		/*
		 * Fetch user object and create its IMAP properties
		 */
		final User user = session.getUserObject();
		if (LoginType.GLOBAL.equals(getLoginType())) {
			String imapServer = MailConfig.getMailServer();
			if (imapServer == null) {
				throw new MailConfigException(new StringBuilder(128).append("Property \"").append("mailServer").append(
						"\" not set in mail properties").toString());
			}
			int imapPort = 143;
			{
				final int pos = imapServer.indexOf(':');
				if (pos > -1) {
					imapPort = Integer.parseInt(imapServer.substring(pos + 1));
					imapServer = imapServer.substring(0, pos);
				}
			}
			final String masterPw = MailConfig.getMasterPassword();
			if (masterPw == null) {
				throw new MailConfigException(new StringBuilder().append("Property \"").append("masterPassword")
						.append("\" not set in mail properties").toString());
			}
			imapConf.imapServer = imapServer;
			imapConf.imapPort = imapPort;
		} else if (LoginType.USER.equals(getLoginType())) {
			String imapServer = user.getImapServer();
			int imapPort = 143;
			{
				final int pos = imapServer.indexOf(':');
				if (pos > -1) {
					imapPort = Integer.parseInt(imapServer.substring(pos + 1));
					imapServer = imapServer.substring(0, pos);
				}
			}
			imapConf.imapServer = imapServer;
			imapConf.imapPort = imapPort;
		} else if (LoginType.ANONYMOUS.equals(getLoginType())) {
			String imapServer = user.getImapServer();
			int imapPort = 143;
			{
				final int pos = imapServer.indexOf(':');
				if (pos > -1) {
					imapPort = Integer.parseInt(imapServer.substring(pos + 1));
					imapServer = imapServer.substring(0, pos);
				}
			}
			imapConf.imapServer = imapServer;
			imapConf.imapPort = imapPort;
		}
		return imapConf;
	}

	/**
	 * Checks if the IMAP properties are loaded. The IMAP properties are loaded
	 * if not done, yet.
	 * 
	 * @throws MailConfigException
	 *             If IMAP properties are not defined or cannot be read from
	 *             file
	 */
	private static void checkImapPropFile() throws MailConfigException {
		checkMailPropFile();
		/*
		 * Load mail properties in a thread-safe manner
		 */
		if (!imapPropsLoaded) {
			PROP_LOCK.lock();
			try {
				if (imapPropFile == null && (imapPropFile = SystemConfig.getProperty(PROPERTYNAME)) == null) {
					throw new MailConfigException(new StringBuilder(50).append("Property \"").append(PROPERTYNAME)
							.append("\" not defined in system.properties").toString());
				}
				if (imapProperties == null) {
					loadImapProps();
					imapPropsLoaded = true;
				}
			} finally {
				PROP_LOCK.unlock();
			}
		}
	}

	private static void loadImapProps() throws MailConfigException {
		imapProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(imapPropFile));
			imapProperties.load(fis);
			fis.close();
			fis = null;
		} catch (final FileNotFoundException e) {
			imapProperties = null;
			throw new MailConfigException(new StringBuilder(256).append("IMAP properties not found at location: ")
					.append(imapPropFile).toString(), e);
		} catch (final IOException e) {
			imapProperties = null;
			throw new MailConfigException(new StringBuilder(256).append(
					"I/O error while reading IMAP properties from file \"").append(imapPropFile).append("\": ").append(
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
	 * Gets a copy of IMAP properties
	 * 
	 * @return A copy of IMAP properties
	 * @throws MailConfigException
	 *             If IMAP properties could not be checked
	 */
	public static Properties getImapProperties() throws MailConfigException {
		checkImapPropFile();
		final Properties retval = new Properties();
		retval.putAll(getProperties());
		retval.putAll((Properties) imapProperties.clone());
		return retval;
	}

	/**
	 * Loads global mail properties
	 * 
	 * @throws MailConfigException
	 *             If gloabal mail properties cannot be loaded
	 */
	public static void loadGlobalImapProperties() throws MailConfigException {
		loadGlobalImapProperties(true);
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
	private static void loadGlobalImapProperties(final boolean checkPropFile) throws MailConfigException {
		loadGlobalMailProperties(checkPropFile);
		if (!globalImapPropsLoaded) {
			GLOBAL_PROP_LOCK.lock();
			try {
				if (globalImapPropsLoaded) {
					return;
				}
				if (checkPropFile) {
					checkImapPropFile();
				}
				final StringBuilder logBuilder = new StringBuilder(1024);
				logBuilder.append("\nLoading global IMAP properties...\n");

				{
					final String imapSortStr = imapProperties.getProperty("imapSort", "application").trim();
					imapSort = "imap".equalsIgnoreCase(imapSortStr);
					logBuilder.append("\tIMAP-Sort: ").append(imapSort).append('\n');
				}

				{
					final String imapSearchStr = imapProperties.getProperty("imapSearch", "imap").trim();
					imapSearch = "imap".equalsIgnoreCase(imapSearchStr);
					logBuilder.append("\tIMAP-Search: ").append(imapSearch).append('\n');
				}

				{
					final String fastFetchStr = imapProperties.getProperty("imapFastFetch", STR_TRUE).trim();
					fastFetch = Boolean.parseBoolean(fastFetchStr);
					logBuilder.append("\tFast Fetch Enabled: ").append(fastFetch).append('\n');
				}

				{
					final String imapSecStr = imapProperties.getProperty("imaps", STR_FALSE).trim();
					imapsEnabled = Boolean.parseBoolean(imapSecStr);
					logBuilder.append("\tIMAP/S enabled: ").append(imapsEnabled).append('\n');
				}

				{
					final String imapSecPortStr = imapProperties.getProperty("imapsPort", "993").trim();
					try {
						imapsPort = Integer.parseInt(imapSecPortStr);
						logBuilder.append("\tIMAP/S port: ").append(imapsPort).append('\n');
					} catch (final NumberFormatException e) {
						imapsPort = 993;
						logBuilder.append("\tIMAP/S port: Invalid value \"").append(imapSecPortStr).append(
								"\". Setting to fallback: ").append(imapsPort).append('\n');
					}
				}

				{
					final String supportsACLsStr = imapProperties.getProperty("imapSupportsACL", STR_FALSE).trim();
					supportsACLs = BoolCapVal.parseBoolCapVal(supportsACLsStr);
					logBuilder.append("\tSupport ACLs: ").append(supportsACLs).append('\n');
				}

				{
					final String imapTimeoutStr = imapProperties.getProperty("imapTimeout", "0").trim();
					try {
						imapTimeout = Integer.parseInt(imapTimeoutStr);
						logBuilder.append("\tIMAP Timeout: ").append(imapTimeout).append('\n');
					} catch (final NumberFormatException e) {
						imapTimeout = 0;
						logBuilder.append("\tIMAP Timeout: Invalid value \"").append(imapTimeoutStr).append(
								"\". Setting to fallback: ").append(imapTimeout).append('\n');
					}
				}

				{
					final String imapConTimeoutStr = imapProperties.getProperty("imapConnectionTimeout", "0").trim();
					try {
						imapConnectionTimeout = Integer.parseInt(imapConTimeoutStr);
						logBuilder.append("\tIMAP Connection Timeout: ").append(imapConnectionTimeout).append('\n');
					} catch (final NumberFormatException e) {
						imapConnectionTimeout = 0;
						logBuilder.append("\tIMAP Connection Timeout: Invalid value \"").append(imapConTimeoutStr)
								.append("\". Setting to fallback: ").append(imapConnectionTimeout).append('\n');
					}
				}

				{
					final String maxConIdleTime = imapProperties.getProperty("maxIMAPConnectionIdleTime", "60000")
							.trim();
					try {
						imapConnectionIdleTime = Integer.parseInt(maxConIdleTime);
						logBuilder.append("\tMax IMAP Connection Idle Time: ").append(imapConnectionIdleTime).append(
								'\n');
					} catch (final NumberFormatException e) {
						imapConnectionIdleTime = 60000;
						logBuilder.append("\tMax IMAP Connection Idle Time: Invalid value \"").append(maxConIdleTime)
								.append("\". Setting to fallback: ").append(imapConnectionIdleTime).append('\n');
					}
				}

				{
					spamHandlerClass = imapProperties.getProperty("spamHandlerClass",
							"com.openexchange.mail.imap.spam.SpamAssassinSpamHandler").trim();
					logBuilder.append("\tSpam Handler Class: ").append(spamHandlerClass).append('\n');
				}

				{
					final String imapAuthEncStr = imapProperties.getProperty("imapAuthEnc", "UTF-8").trim();
					if (Charset.isSupported(imapAuthEncStr)) {
						imapAuthEnc = imapAuthEncStr;
						logBuilder.append("\tAuthentication Encoding: ").append(imapAuthEnc).append('\n');
					} else {
						imapAuthEnc = "UTF-8";
						logBuilder.append("\tAuthentication Encoding: Unsupported charset \"").append(imapAuthEncStr)
								.append("\". Setting to fallback: ").append(imapAuthEnc).append('\n');
					}
				}

				{
					user2AclImpl = imapProperties.getProperty("User2ACLImpl");
					if (null != user2AclImpl) {
						user2AclImpl.trim();
					}
				}

				/*
				 * Switch flag
				 */
				globalImapPropsLoaded = true;
				logBuilder.append("Global IMAP properties successfully loaded!");
				if (LOG.isInfoEnabled()) {
					LOG.info(logBuilder.toString());
				}
			} finally {
				GLOBAL_PROP_LOCK.unlock();
			}
		}
	}

	/**
	 * @return <code>true</code> if global IMAP properties have already been
	 *         loaded; otherwise <code>false</code>
	 */
	public static boolean isGlobalImapPropsLoaded() {
		return globalImapPropsLoaded;
	}

	/**
	 * Gets the fastFetch
	 * 
	 * @return the fastFetch
	 * @throws MailConfigException
	 */
	public static boolean isFastFetch() throws MailConfigException {
		loadGlobalImapProperties();
		return fastFetch;
	}

	/**
	 * Gets the imapAuthEnc
	 * 
	 * @return the imapAuthEnc
	 * @throws MailConfigException
	 */
	public static String getImapAuthEnc() throws MailConfigException {
		loadGlobalImapProperties();
		return imapAuthEnc;
	}

	/**
	 * Gets the imapConnectionIdleTime
	 * 
	 * @return the imapConnectionIdleTime
	 * @throws MailConfigException
	 */
	public static int getImapConnectionIdleTime() throws MailConfigException {
		loadGlobalImapProperties();
		return imapConnectionIdleTime;
	}

	/**
	 * Gets the imapConnectionTimeout
	 * 
	 * @return the imapConnectionTimeout
	 * @throws MailConfigException
	 */
	public static int getImapConnectionTimeout() throws MailConfigException {
		loadGlobalImapProperties();
		return imapConnectionTimeout;
	}

	/**
	 * Gets the imapSearch
	 * 
	 * @return the imapSearch
	 * @throws MailConfigException
	 */
	public boolean isImapSearch() throws MailConfigException {
		loadGlobalImapProperties();
		if (capabilitiesLoaded.get()) {
			return (imapSearch && (imapCapabilities.hasIMAP4rev1() || imapCapabilities.hasIMAP4()));
		}
		return imapSearch;
	}

	/**
	 * Gets the imapsEnabled
	 * 
	 * @return the imapsEnabled
	 * @throws MailConfigException
	 */
	public static boolean isImapsEnabled() throws MailConfigException {
		loadGlobalImapProperties();
		return imapsEnabled;
	}

	/**
	 * Gets the imapSort
	 * 
	 * @return the imapSort
	 * @throws MailConfigException
	 */
	public boolean isImapSort() throws MailConfigException {
		loadGlobalImapProperties();
		if (capabilitiesLoaded.get()) {
			return (imapSort && imapCapabilities.hasSort());
		}
		return imapSort;
	}

	/**
	 * Gets the imapsPort
	 * 
	 * @return the imapsPort
	 * @throws MailConfigException
	 */
	public static int getImapsPort() throws MailConfigException {
		loadGlobalImapProperties();
		return imapsPort;
	}

	/**
	 * Gets the imapTimeout
	 * 
	 * @return the imapTimeout
	 * @throws MailConfigException
	 */
	public static int getImapTimeout() throws MailConfigException {
		loadGlobalImapProperties();
		return imapTimeout;
	}

	/**
	 * Gets the spamHandlerClass
	 * 
	 * @return the spamHandlerClass
	 * @throws MailConfigException
	 */
	public static String getSpamHandlerClass() throws MailConfigException {
		loadGlobalImapProperties();
		return spamHandlerClass;
	}

	/**
	 * Gets the global supportsACLs
	 * 
	 * @return the global supportsACLs
	 * @throws MailConfigException
	 */
	public static boolean isSupportsACLsConfig() throws MailConfigException {
		loadGlobalImapProperties();
		return BoolCapVal.TRUE.equals(supportsACLs) ? true : false;
	}

	/**
	 * Gets the user2acl implementation's canonical class name
	 * 
	 * @return The user2acl implementation's canonical class name
	 * @throws MailConfigException
	 */
	public static String getUser2AclImpl() throws MailConfigException {
		loadGlobalImapProperties();
		return user2AclImpl;
	}

	/**
	 * Checks if given IMAP server implements newer ACL extension conforming to
	 * RFC 4314
	 * 
	 * @param imapServer
	 *            The IMAP server's host name or IP address
	 * @return <code>true</code> if newer ACL extension is supported by IMAP
	 *         server; otherwise <code>false</code>
	 */
	public static boolean hasNewACLExt(final String imapServer) {
		return newACLExtMap.containsKey(imapServer) ? newACLExtMap.get(imapServer).booleanValue() : false;
	}

	/**
	 * Remembers if given IMAP server supports newer ACL extension conforming to
	 * RFC 4314
	 * 
	 * @param imapServer
	 *            The IMAP server's host name or IP address
	 * @param newACLExt
	 *            Whether newer ACL extension is supported or not
	 */
	public static void setNewACLExt(final String imapServer, final boolean newACLExt) {
		newACLExtMap.put(imapServer, Boolean.valueOf(newACLExt));
	}

	/**
	 * Requests if the IMAP capabilities are loaded
	 * 
	 * @return <code>true</code> if IMAP capabilities are loaded; otherwise
	 *         <code>false</code>
	 */
	public boolean isCapabilitiesLoaded() {
		return capabilitiesLoaded.get();
	}

	/**
	 * Gets the IMAP capabilities
	 * 
	 * @return The IMAP capabilities
	 */
	public IMAPCapabilities getImapCapabilities() {
		return imapCapabilities;
	}

	private final transient Lock lockCaps = new ReentrantLock();

	/**
	 * Initializes IMAP server's capabilities if not done, yet
	 * 
	 * @param imapStore
	 *            The IMAP store from which to fetch the capabilities
	 * @throws MailConfigException
	 *             If IMAP capabilities cannot be initialized
	 */
	public void initializeCapabilities(final IMAPStore imapStore) throws MailConfigException {
		if (!capabilitiesLoaded.get()) {
			lockCaps.lock();
			try {
				if (capabilitiesLoaded.get()) {
					return;
				}
				final IMAPCapabilities imapCaps = new IMAPCapabilities();
				imapCaps.setACL(imapStore.hasCapability(IMAPCapabilities.CAP_ACL));
				imapCaps.setThreadReferences(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_REFERENCES));
				imapCaps.setThreadOrderedSubject(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_ORDEREDSUBJECT));
				imapCaps.setQuota(imapStore.hasCapability(IMAPCapabilities.CAP_QUOTA));
				imapCaps.setSort(imapStore.hasCapability(IMAPCapabilities.CAP_SORT));
				imapCaps.setIMAP4(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4));
				imapCaps.setIMAP4rev1(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4_REV1));
				imapCaps.setUIDPlus(imapStore.hasCapability(IMAPCapabilities.CAP_UIDPLUS));
				try {
					imapCaps.setHasSubscription(!IMAPConfig.isIgnoreSubscription());
				} catch (final MailConfigException e) {
					LOG.error(e.getMessage(), e);
					imapCaps.setHasSubscription(false);
				}
				imapCapabilities = imapCaps;
				capabilitiesLoaded.set(true);
			} catch (final MessagingException e) {
				throw new MailConfigException(e);
			} finally {
				lockCaps.unlock();
			}
		}
	}

	@Override
	public int getCapabilities() {
		return capabilitiesLoaded.get() ? imapCapabilities.getCapabilities() : 0;
	}
	
	/**
	 * Gets the imapPort
	 * 
	 * @return the imapPort
	 */
	@Override
	public int getPort() {
		return imapPort;
	}

	/**
	 * Gets the imapServer
	 * 
	 * @return the imapServer
	 */
	@Override
	public String getServer() {
		return imapServer;
	}

	/**
	 * Gets the supportsACLs
	 * 
	 * @return the supportsACLs
	 * @throws MailConfigException
	 */
	public boolean isSupportsACLs() throws MailConfigException {
		loadGlobalImapProperties();
		if (capabilitiesLoaded.get() && BoolCapVal.AUTO.equals(supportsACLs)) {
			return imapCapabilities.hasACL();
		}
		return BoolCapVal.TRUE.equals(supportsACLs) ? true : false;
	}
}
