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

package com.openexchange.imap;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.imap.spellcheck.SpellCheckConfig;
import com.openexchange.mail.imap.IMAPPropertyNames;

/**
 * {@link IMAPProperties} - Container for IMAP properties
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class IMAPProperties {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPProperties.class);

	private final int user;

	private String imapLogin;

	private String imapPassword;

	private String imapServer;

	private String smtpServer;

	private int imapPort = -1;

	private int smtpPort = -1;

	private AbstractOXException error;

	private static boolean globalPropertiesLoaded;

	private static boolean capabilitiesLoaded;

	private static IMAPCapabilities imapCapabilities;

	private static SpellCheckConfig spellCheckConfig;

	private static boolean imapSort;

	private static boolean imapSearch;

	private static String smtpLocalhost;

	private static int messageFetchLimit = 1000;

	private static int attachmentDisplaySizeLimit = 8192;

	private static String[] quoteLineColors = new String[] { "#666666" };

	private static BoolCapVal supportsACLs;

	private static PartModifier partModifierImpl;

	private static boolean smtpAuth;

	private static int imapTimeout;

	private static int imapConnectionTimeout;

	private static boolean userFlagsEnabled = true;

	private static long maxIMAPConnectionIdleTime = Long.MIN_VALUE;

	private static boolean allowNestedDefaultFolderOnAltNamespace;

	private static boolean imapsEnabled;

	private static int imapsPort;

	private static boolean smtpsEnabled;

	private static int smtpsPort;

	private static int maxNumOfIMAPConnections;

	private static String defaultMimeCharset;

	private static String imapAuthEnc;

	private static boolean ignoreSubscription;

	private static boolean smtpEnvelopeFrom;

	private static Properties javaMailProperties;

	private static boolean spamEnabled;

	private static boolean watcherEnabled;

	private static boolean watcherShallClose;

	private static int watcherFrequency;

	private static int watcherTime;

	private static IMAPPropertiesFactory.IMAPCredSrc imapCredSrc;

	private static IMAPPropertiesFactory.IMAPLoginType imapLoginType;

	private static Properties JAVAMAIL_PROPS;

	private static final Lock LOCK_JAVAMAIL_PROPS = new ReentrantLock();

	private static boolean javamailPropsInitialized;

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	public static enum BoolCapVal {

		/**
		 * TRUE
		 */
		TRUE("true"),
		/**
		 * FALSE
		 */
		FALSE("false"),
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

	/**
	 * Creates a <b>cloned</b> version of default IMAP properties
	 * 
	 * @return a cloned version of default IMAP properties
	 * @throws IMAPPropertyException
	 *             If gloabl IMAP proeprties could not be initialized
	 */
	public static Properties getDefaultJavaMailProperties() throws IMAPPropertyException {
		if (!javamailPropsInitialized) {
			LOCK_JAVAMAIL_PROPS.lock();
			try {
				if (null == JAVAMAIL_PROPS) {
					initializeIMAPProperties();
					javamailPropsInitialized = true;
				}
			} finally {
				LOCK_JAVAMAIL_PROPS.unlock();
			}
		}
		return (Properties) JAVAMAIL_PROPS.clone();
	}

	private static final String CLASS_TRUSTALLSSLSOCKETFACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

	/**
	 * This method can only be exclusively accessed
	 * 
	 * @throws IMAPPropertyException
	 */
	private static void initializeIMAPProperties() throws IMAPPropertyException {
		/*
		 * Define imap properties
		 */
		JAVAMAIL_PROPS = ((Properties) (System.getProperties().clone()));
		/*
		 * Set some global JavaMail properties
		 */
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, STR_TRUE);
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_ALLOWREADONLYSELECT, STR_TRUE);
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, STR_TRUE);
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, STR_FALSE);
		/*
		 * A connected IMAPStore maintains a pool of IMAP protocol objects for
		 * use in communicating with the IMAP server. The IMAPStore will create
		 * the initial AUTHENTICATED connection and seed the pool with this
		 * connection. As folders are opened and new IMAP protocol objects are
		 * needed, the IMAPStore will provide them from the connection pool, or
		 * create them if none are available. When a folder is closed, its IMAP
		 * protocol object is returned to the connection pool if the pool is not
		 * over capacity.
		 */
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLSIZE, "1");
		/*
		 * A mechanism is provided for timing out idle connection pool IMAP
		 * protocol objects. Timed out connections are closed and removed
		 * (pruned) from the connection pool.
		 */
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT, "1000"); // 1
		// sec
		/*
		 * Fill global IMAP Properties only once and switch flag
		 */
		if (!IMAPProperties.isGlobalPropertiesLoaded()) {
			IMAPPropertiesFactory.loadGlobalImapProperties();
		}
		/*
		 * Initialize properties
		 */
		try {
			JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_MIME_CHARSET, IMAPProperties.getDefaultMimeCharset());
		} catch (final IMAPPropertyException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		/*
		 * Following properties define if IMAPS and/or SMTPS should be enabled
		 */
		try {
			if (IMAPProperties.isImapsEnabled()) {
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS,
						CLASS_TRUSTALLSSLSOCKETFACTORY);
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_PORT, String.valueOf(IMAPProperties
						.getImapsPort()));
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK, STR_FALSE);
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_SMTP_STARTTLS_ENABLE, STR_TRUE);
			}
		} catch (final IMAPPropertyException e) {
			LOG.error(e.getMessage(), e);
		}
		try {
			if (IMAPProperties.isSmtpsEnabled()) {
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_SMTP_SOCKET_FACTORY_CLASS,
						CLASS_TRUSTALLSSLSOCKETFACTORY);
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf(IMAPProperties
						.getSmtpsPort()));
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_SMTP_SOCKET_FACTORY_FALLBACK, STR_FALSE);
				JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_SMTP_STARTTLS_ENABLE, STR_TRUE);
			}
		} catch (final IMAPPropertyException e) {
			LOG.error(e.getMessage(), e);
		}
		if (IMAPProperties.getSmtpLocalhost() != null) {
			JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_SMTPLOCALHOST, IMAPProperties.getSmtpLocalhost());
		}
		try {
			if (IMAPProperties.getJavaMailProperties() != null) {
				/*
				 * Overwrite current JavaMail-Specific properties with the ones
				 * defined in javamail.properties
				 */
				JAVAMAIL_PROPS.putAll(IMAPProperties.getJavaMailProperties());
			}
		} catch (final IMAPPropertyException e) {
			LOG.error(e.getMessage(), e);
		}
		if (IMAPProperties.getImapTimeout() > 0) {
			JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_TIMEOUT, Integer.valueOf(IMAPProperties
					.getImapTimeout()));
		}
		if (IMAPProperties.getImapConnectionTimeout() > 0) {
			JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_IMAP_CONNECTIONTIMEOUT, Integer.valueOf(IMAPProperties
					.getImapConnectionTimeout()));
		}
		JAVAMAIL_PROPS.put(IMAPPropertyNames.PROP_MAIL_SMTP_AUTH, IMAPProperties.isSmtpAuth() ? STR_TRUE : STR_FALSE);
	}

	public IMAPProperties(final int user) {
		super();
		this.user = user;
	}

	public String getImapLogin() {
		return imapLogin;
	}

	public void setImapLogin(final String imapLogin) {
		this.imapLogin = imapLogin;
	}

	public String getImapPassword() {
		return imapPassword;
	}

	public void setImapPassword(final String imapPassword) {
		this.imapPassword = imapPassword;
	}

	public int getImapPort() {
		return imapPort;
	}

	public void setImapPort(final int imapPort) {
		this.imapPort = imapPort;
	}

	public String getImapServer() {
		return imapServer;
	}

	public void setImapServer(final String imapServer) {
		this.imapServer = imapServer;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(final String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public static String getSmtpLocalhost() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return smtpLocalhost;
	}

	static String getSmtpLocalhostInternal() {
		return smtpLocalhost;
	}

	public static void setSmtpLocalhost(final String smtpLocalhost) {
		IMAPProperties.smtpLocalhost = smtpLocalhost;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(final int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public int getUser() {
		return user;
	}

	public AbstractOXException getError() {
		return error;
	}

	public boolean hasError() {
		return (null != error);
	}

	public void setError(final AbstractOXException error) {
		this.error = error;
	}

	public static boolean isCapabilitiesLoaded() {
		return capabilitiesLoaded;
	}

	public static void setCapabilitiesLoaded(final boolean capabilitiesLoaded) {
		IMAPProperties.capabilitiesLoaded = capabilitiesLoaded;
	}

	public static IMAPCapabilities getImapCapabilities() {
		return imapCapabilities;
	}

	public static void setImapCapabilities(final IMAPCapabilities imapCapabilities) {
		IMAPProperties.imapCapabilities = imapCapabilities;
	}

	public static boolean isImapSort() throws IMAPPropertyException {
		checkGlobalImapProperties();
		if (capabilitiesLoaded) {
			return (imapSort && imapCapabilities.hasSort());
		}
		return imapSort;
	}

	static boolean isImapSortInternal() {
		return imapSort;
	}

	public static void setImapSort(final boolean imapSort) {
		IMAPProperties.imapSort = imapSort;
	}

	public static boolean isImapSearch() throws IMAPPropertyException {
		checkGlobalImapProperties();
		if (capabilitiesLoaded) {
			return (imapSearch && (imapCapabilities.hasIMAP4rev1() || imapCapabilities.hasIMAP4()));
		}
		return imapSearch;
	}

	static boolean isImapSearchInternal() {
		return imapSearch;
	}

	public static void setImapSearch(final boolean imapSearch) {
		IMAPProperties.imapSearch = imapSearch;
	}

	public static int getMessageFetchLimit() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return messageFetchLimit;
	}

	static int getMessageFetchLimitInternal() {
		return messageFetchLimit;
	}

	public static void setMessageFetchLimit(final int messageFetchLimit) {
		IMAPProperties.messageFetchLimit = messageFetchLimit;
	}

	public static int getAttachmentDisplaySizeLimit() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return attachmentDisplaySizeLimit;
	}

	static int getAttachmentDisplaySizeLimitInternal() {
		return attachmentDisplaySizeLimit;
	}

	public static void setAttachmentDisplaySizeLimit(final int attachmentDisplaySizeLimit) {
		IMAPProperties.attachmentDisplaySizeLimit = attachmentDisplaySizeLimit;
	}

	public static String[] getQuoteLineColors() throws IMAPPropertyException {
		checkGlobalImapProperties();
		final String[] retval = new String[quoteLineColors.length];
		System.arraycopy(quoteLineColors, 0, retval, 0, retval.length);
		return retval;
	}

	public static void setQuoteLineColors(final String[] quoteLineColors) {
		IMAPProperties.quoteLineColors = new String[quoteLineColors.length];
		System.arraycopy(quoteLineColors, 0, IMAPProperties.quoteLineColors, 0, quoteLineColors.length);
	}

	public static boolean isSupportsACLs() throws IMAPPropertyException {
		checkGlobalImapProperties();
		if (capabilitiesLoaded && BoolCapVal.AUTO.equals(supportsACLs)) {
			return imapCapabilities.hasACL();
		}
		return BoolCapVal.TRUE.equals(supportsACLs) ? true : false;
	}

	static BoolCapVal isSupportsACLsInternal() {
		return supportsACLs;
	}

	static void setSupportsACLs(final BoolCapVal supportsACLs) {
		IMAPProperties.supportsACLs = supportsACLs;
	}

	public static SpellCheckConfig getSpellCheckConfig() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return spellCheckConfig;
	}

	public static void setSpellCheckConfig(final SpellCheckConfig spellCheckConfig) {
		IMAPProperties.spellCheckConfig = spellCheckConfig;
	}

	public static boolean isGlobalPropertiesLoaded() {
		return globalPropertiesLoaded;
	}

	static void setGlobalPropertiesLoaded(final boolean globalPropertiesLoaded) {
		IMAPProperties.globalPropertiesLoaded = globalPropertiesLoaded;
	}

	public static PartModifier getPartModifierImpl() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return partModifierImpl;
	}

	static PartModifier getPartModifierImplInternal() {
		return partModifierImpl;
	}

	public static void setPartModifierImpl(final PartModifier partModifierImpl) {
		IMAPProperties.partModifierImpl = partModifierImpl;
	}

	public static boolean isSmtpAuth() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return smtpAuth;
	}

	static boolean isSmtpAuthInternal() {
		return smtpAuth;
	}

	public static void setSmtpAuth(final boolean smtpAuth) {
		IMAPProperties.smtpAuth = smtpAuth;
	}

	public static int getImapConnectionTimeout() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapConnectionTimeout;
	}

	static int getImapConnectionTimeoutInternal() {
		return imapConnectionTimeout;
	}

	public static void setImapConnectionTimeout(final int imapConnectionTimeout) {
		IMAPProperties.imapConnectionTimeout = imapConnectionTimeout;
	}

	public static int getImapTimeout() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapTimeout;
	}

	static int getImapTimeoutInternal() {
		return imapTimeout;
	}

	public static void setImapTimeout(final int imapConnectionTimeout) {
		IMAPProperties.imapTimeout = imapConnectionTimeout;
	}

	public static boolean isUserFlagsEnabled() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return userFlagsEnabled;
	}

	static boolean isUserFlagsEnabledInternal() {
		return userFlagsEnabled;
	}

	public static void setUserFlagsEnabled(final boolean userFlagsEnabled) {
		IMAPProperties.userFlagsEnabled = userFlagsEnabled;
	}

	public static long getMaxIMAPConnectionIdleTime() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return maxIMAPConnectionIdleTime;
	}

	static long getMaxIMAPConnectionIdleTimeInternal() {
		return maxIMAPConnectionIdleTime;
	}

	public static void setMaxIMAPConnectionIdleTime(final long maxIMAPConnectionIdleTime) {
		IMAPProperties.maxIMAPConnectionIdleTime = maxIMAPConnectionIdleTime;
	}

	public static int getMaxNumOfIMAPConnections() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return maxNumOfIMAPConnections;
	}

	static int getMaxNumOfIMAPConnectionsInternal() {
		return maxNumOfIMAPConnections;
	}

	public static void setMaxNumOfIMAPConnections(final int maxNumOfIMAPConnections) {
		IMAPProperties.maxNumOfIMAPConnections = maxNumOfIMAPConnections;
	}

	public static String getImapAuthEnc() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapAuthEnc;
	}

	static String getImapAuthEncInternal() {
		return imapAuthEnc;
	}

	public static void setImapAuthEnc(final String imapAuthEnc) {
		IMAPProperties.imapAuthEnc = imapAuthEnc;
	}

	public static String getDefaultMimeCharset() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return defaultMimeCharset;
	}

	static String getDefaultMimeCharsetInternal() {
		return defaultMimeCharset;
	}

	private static final String PROP_MIME_CS = "mail.mime.charset";

	public static void setDefaultMimeCharset(final String defaultMimeCharset) {
		IMAPProperties.defaultMimeCharset = defaultMimeCharset;
		/*
		 * Add to system properties, too
		 */
		System.getProperties().setProperty(PROP_MIME_CS,
				defaultMimeCharset == null ? System.getProperty("file.encoding", "8859_1") : defaultMimeCharset);
	}

	public static boolean isImapsEnabled() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapsEnabled;
	}

	static boolean isImapsEnabledInternal() {
		return imapsEnabled;
	}

	public static void setImapsEnabled(final boolean imapsEnables) {
		IMAPProperties.imapsEnabled = imapsEnables;
	}

	public static int getImapsPort() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapsPort;
	}

	static int getImapsPortInternal() {
		return imapsPort;
	}

	public static void setImapsPort(final int imapsPort) {
		IMAPProperties.imapsPort = imapsPort;
	}

	public static boolean isSmtpsEnabled() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return smtpsEnabled;
	}

	static boolean isSmtpsEnabledInternal() {
		return smtpsEnabled;
	}

	public static void setSmtpsEnabled(final boolean smtpsEnabled) {
		IMAPProperties.smtpsEnabled = smtpsEnabled;
	}

	public static int getSmtpsPort() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return smtpsPort;
	}

	static int getSmtpsPortInternal() {
		return smtpsPort;
	}

	public static void setSmtpsPort(final int smtpsPort) {
		IMAPProperties.smtpsPort = smtpsPort;
	}

	public static boolean isAllowNestedDefaultFolderOnAltNamespace() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return allowNestedDefaultFolderOnAltNamespace;
	}

	static boolean isAllowNestedDefaultFolderOnAltNamespaceInternal() {
		return allowNestedDefaultFolderOnAltNamespace;
	}

	public static void setAllowNestedDefaultFolderOnAltNamespace(final boolean allowNestedDefaultFolderOnAltNamespace) {
		IMAPProperties.allowNestedDefaultFolderOnAltNamespace = allowNestedDefaultFolderOnAltNamespace;
	}

	public static boolean isIgnoreSubscription() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return ignoreSubscription;
	}

	static boolean isIgnoreSubscriptionInternal() {
		return ignoreSubscription;
	}

	public static void setIgnoreSubscription(final boolean ignoreSubscription) {
		IMAPProperties.ignoreSubscription = ignoreSubscription;
	}

	public static boolean isSMTPEnvelopeFrom() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return smtpEnvelopeFrom;
	}

	static boolean isSMTPEnvelopeFromInternal() {
		return smtpEnvelopeFrom;
	}

	public static void setSMTPEnvelopeFrom(final boolean setSMTPEnvelopeFrom) {
		IMAPProperties.smtpEnvelopeFrom = setSMTPEnvelopeFrom;
	}

	public static boolean isSpamEnabled() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return spamEnabled;
	}

	static boolean isSpamEnabledInternal() {
		return spamEnabled;
	}

	public static void setSpamEnabled(final boolean spamEnabled) {
		IMAPProperties.spamEnabled = spamEnabled;
	}

	public static boolean isWatcherEnabled() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return watcherEnabled;
	}

	static boolean isWatcherEnabledInternal() {
		return watcherEnabled;
	}

	public static void setWatcherEnabled(final boolean watcherEnabled) {
		IMAPProperties.watcherEnabled = watcherEnabled;
	}

	public static boolean isWatcherShallClose() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return watcherShallClose;
	}

	static boolean isWatcherShallCloseInternal() {
		return watcherShallClose;
	}

	public static void setWatcherShallClose(final boolean watcherShallClose) {
		IMAPProperties.watcherShallClose = watcherShallClose;
	}

	public static int getWatcherTime() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return watcherTime;
	}

	static int getWatcherTimeInternal() {
		return watcherTime;
	}

	public static void setWatcherTime(final int watcherTime) {
		IMAPProperties.watcherTime = watcherTime;
	}

	public static int getWatcherFrequency() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return watcherFrequency;
	}

	static int getWatcherFrequencyInternal() {
		return watcherFrequency;
	}

	public static void setWatcherFrequency(final int watcherFrequency) {
		IMAPProperties.watcherFrequency = watcherFrequency;
	}

	public static IMAPPropertiesFactory.IMAPCredSrc getImapCredSrc() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapCredSrc;
	}

	static IMAPPropertiesFactory.IMAPCredSrc getImapCredSrcInternal() {
		return imapCredSrc;
	}

	public static void setImapCredSrc(final IMAPPropertiesFactory.IMAPCredSrc imapCredSrc) {
		IMAPProperties.imapCredSrc = imapCredSrc;
	}

	public static IMAPPropertiesFactory.IMAPLoginType getImapLoginType() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return imapLoginType;
	}

	static IMAPPropertiesFactory.IMAPLoginType getImapLoginTypeInternal() {
		return imapLoginType;
	}

	public static void setImapLoginType(final IMAPPropertiesFactory.IMAPLoginType imapLoginType) {
		IMAPProperties.imapLoginType = imapLoginType;
	}

	public static Properties getJavaMailProperties() throws IMAPPropertyException {
		checkGlobalImapProperties();
		return javaMailProperties;
	}

	static Properties getJavaMailPropertiesInternal() {
		return javaMailProperties;
	}

	public static void setJavaMailProperties(final Properties javaMailProperties) {
		IMAPProperties.javaMailProperties = javaMailProperties;
	}

	private static final void checkGlobalImapProperties() throws IMAPPropertyException {
		if (!globalPropertiesLoaded) {
			IMAPPropertiesFactory.loadGlobalImapProperties();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final String delim = " | ";
		final StringBuilder sb = new StringBuilder(300);
		sb.append("imapLogin=").append(imapLogin).append(delim).append("imapPassword=").append(imapPassword).append(
				delim);
		sb.append("imapServer=").append(imapServer).append(delim).append("imapPort=").append(imapPort).append(delim);
		sb.append("imapSort=").append(imapSort).append(delim).append("imapSearch=").append(imapSearch).append(delim);
		sb.append("messageFetchLimit=").append(messageFetchLimit).append(delim).append("attachmentDisplaySizeLimit=")
				.append(attachmentDisplaySizeLimit).append(delim);
		sb.append("quoteLineColors=").append(Arrays.toString(quoteLineColors)).append(delim).append("supportsACLs=")
				.append(supportsACLs).append(delim);
		sb.append("smtpAuth=").append(smtpAuth).append(delim).append("imapConnectionTimeout=").append(
				imapConnectionTimeout);
		return sb.toString();
	}

}
