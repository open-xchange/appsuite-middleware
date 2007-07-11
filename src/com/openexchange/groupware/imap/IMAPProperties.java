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

import java.util.Arrays;
import java.util.Properties;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;

/**
 * IMAPProperties
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class IMAPProperties {

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

	public static String getSmtpLocalhost() throws IMAPException {
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

	public static boolean isImapSort() throws IMAPException {
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

	public static boolean isImapSearch() throws IMAPException {
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

	public static int getMessageFetchLimit() throws IMAPException {
		checkGlobalImapProperties();
		return messageFetchLimit;
	}

	static int getMessageFetchLimitInternal() {
		return messageFetchLimit;
	}

	public static void setMessageFetchLimit(final int messageFetchLimit) {
		IMAPProperties.messageFetchLimit = messageFetchLimit;
	}

	public static int getAttachmentDisplaySizeLimit() throws IMAPException {
		checkGlobalImapProperties();
		return attachmentDisplaySizeLimit;
	}

	static int getAttachmentDisplaySizeLimitInternal() {
		return attachmentDisplaySizeLimit;
	}

	public static void setAttachmentDisplaySizeLimit(final int attachmentDisplaySizeLimit) {
		IMAPProperties.attachmentDisplaySizeLimit = attachmentDisplaySizeLimit;
	}

	public static String[] getQuoteLineColors() throws IMAPException {
		checkGlobalImapProperties();
		final String[] retval = new String[quoteLineColors.length];
		System.arraycopy(quoteLineColors, 0, retval, 0, retval.length);
		return retval;
	}

	public static void setQuoteLineColors(final String[] quoteLineColors) {
		IMAPProperties.quoteLineColors = new String[quoteLineColors.length];
		System.arraycopy(quoteLineColors, 0, IMAPProperties.quoteLineColors, 0, quoteLineColors.length);
	}

	public static boolean isSupportsACLs() throws IMAPException {
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

	public static SpellCheckConfig getSpellCheckConfig() throws IMAPException {
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

	public static PartModifier getPartModifierImpl() throws IMAPException {
		checkGlobalImapProperties();
		return partModifierImpl;
	}

	static PartModifier getPartModifierImplInternal() {
		return partModifierImpl;
	}

	public static void setPartModifierImpl(final PartModifier partModifierImpl) {
		IMAPProperties.partModifierImpl = partModifierImpl;
	}

	public static boolean isSmtpAuth() throws IMAPException {
		checkGlobalImapProperties();
		return smtpAuth;
	}

	static boolean isSmtpAuthInternal() {
		return smtpAuth;
	}

	public static void setSmtpAuth(final boolean smtpAuth) {
		IMAPProperties.smtpAuth = smtpAuth;
	}

	public static int getImapConnectionTimeout() throws IMAPException {
		checkGlobalImapProperties();
		return imapConnectionTimeout;
	}

	static int getImapConnectionTimeoutInternal() {
		return imapConnectionTimeout;
	}

	public static void setImapConnectionTimeout(final int imapConnectionTimeout) {
		IMAPProperties.imapConnectionTimeout = imapConnectionTimeout;
	}

	public static int getImapTimeout() throws IMAPException {
		checkGlobalImapProperties();
		return imapTimeout;
	}

	static int getImapTimeoutInternal() {
		return imapTimeout;
	}

	public static void setImapTimeout(final int imapConnectionTimeout) {
		IMAPProperties.imapTimeout = imapConnectionTimeout;
	}

	public static boolean isUserFlagsEnabled() throws IMAPException {
		checkGlobalImapProperties();
		return userFlagsEnabled;
	}

	static boolean isUserFlagsEnabledInternal() {
		return userFlagsEnabled;
	}

	public static void setUserFlagsEnabled(final boolean userFlagsEnabled) {
		IMAPProperties.userFlagsEnabled = userFlagsEnabled;
	}

	public static long getMaxIMAPConnectionIdleTime() throws IMAPException {
		checkGlobalImapProperties();
		return maxIMAPConnectionIdleTime;
	}

	static long getMaxIMAPConnectionIdleTimeInternal() {
		return maxIMAPConnectionIdleTime;
	}

	public static void setMaxIMAPConnectionIdleTime(final long maxIMAPConnectionIdleTime) {
		IMAPProperties.maxIMAPConnectionIdleTime = maxIMAPConnectionIdleTime;
	}

	public static int getMaxNumOfIMAPConnections() throws IMAPException {
		checkGlobalImapProperties();
		return maxNumOfIMAPConnections;
	}

	static int getMaxNumOfIMAPConnectionsInternal() {
		return maxNumOfIMAPConnections;
	}

	public static void setMaxNumOfIMAPConnections(final int maxNumOfIMAPConnections) {
		IMAPProperties.maxNumOfIMAPConnections = maxNumOfIMAPConnections;
	}

	public static String getImapAuthEnc() throws IMAPException {
		checkGlobalImapProperties();
		return imapAuthEnc;
	}

	static String getImapAuthEncInternal() {
		return imapAuthEnc;
	}

	public static void setImapAuthEnc(final String imapAuthEnc) {
		IMAPProperties.imapAuthEnc = imapAuthEnc;
	}

	public static String getDefaultMimeCharset() throws IMAPException {
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

	public static boolean isImapsEnabled() throws IMAPException {
		checkGlobalImapProperties();
		return imapsEnabled;
	}

	static boolean isImapsEnabledInternal() {
		return imapsEnabled;
	}

	public static void setImapsEnabled(final boolean imapsEnables) {
		IMAPProperties.imapsEnabled = imapsEnables;
	}

	public static int getImapsPort() throws IMAPException {
		checkGlobalImapProperties();
		return imapsPort;
	}

	static int getImapsPortInternal() {
		return imapsPort;
	}

	public static void setImapsPort(final int imapsPort) {
		IMAPProperties.imapsPort = imapsPort;
	}

	public static boolean isSmtpsEnabled() throws IMAPException {
		checkGlobalImapProperties();
		return smtpsEnabled;
	}

	static boolean isSmtpsEnabledInternal() {
		return smtpsEnabled;
	}

	public static void setSmtpsEnabled(final boolean smtpsEnabled) {
		IMAPProperties.smtpsEnabled = smtpsEnabled;
	}

	public static int getSmtpsPort() throws IMAPException {
		checkGlobalImapProperties();
		return smtpsPort;
	}

	static int getSmtpsPortInternal() {
		return smtpsPort;
	}

	public static void setSmtpsPort(final int smtpsPort) {
		IMAPProperties.smtpsPort = smtpsPort;
	}

	public static boolean isAllowNestedDefaultFolderOnAltNamespace() throws IMAPException {
		checkGlobalImapProperties();
		return allowNestedDefaultFolderOnAltNamespace;
	}

	static boolean isAllowNestedDefaultFolderOnAltNamespaceInternal() {
		return allowNestedDefaultFolderOnAltNamespace;
	}

	public static void setAllowNestedDefaultFolderOnAltNamespace(final boolean allowNestedDefaultFolderOnAltNamespace) {
		IMAPProperties.allowNestedDefaultFolderOnAltNamespace = allowNestedDefaultFolderOnAltNamespace;
	}

	public static boolean isIgnoreSubscription() throws IMAPException {
		checkGlobalImapProperties();
		return ignoreSubscription;
	}

	static boolean isIgnoreSubscriptionInternal() {
		return ignoreSubscription;
	}

	public static void setIgnoreSubscription(final boolean ignoreSubscription) {
		IMAPProperties.ignoreSubscription = ignoreSubscription;
	}

	public static boolean isSMTPEnvelopeFrom() throws IMAPException {
		checkGlobalImapProperties();
		return smtpEnvelopeFrom;
	}

	static boolean isSMTPEnvelopeFromInternal() {
		return smtpEnvelopeFrom;
	}

	public static void setSMTPEnvelopeFrom(final boolean setSMTPEnvelopeFrom) {
		IMAPProperties.smtpEnvelopeFrom = setSMTPEnvelopeFrom;
	}

	public static boolean isSpamEnabled() throws IMAPException {
		checkGlobalImapProperties();
		return spamEnabled;
	}

	static boolean isSpamEnabledInternal() {
		return spamEnabled;
	}

	public static void setSpamEnabled(final boolean spamEnabled) {
		IMAPProperties.spamEnabled = spamEnabled;
	}

	public static boolean isWatcherEnabled() throws IMAPException {
		checkGlobalImapProperties();
		return watcherEnabled;
	}

	static boolean isWatcherEnabledInternal() {
		return watcherEnabled;
	}

	public static void setWatcherEnabled(final boolean watcherEnabled) {
		IMAPProperties.watcherEnabled = watcherEnabled;
	}

	public static boolean isWatcherShallClose() throws IMAPException {
		checkGlobalImapProperties();
		return watcherShallClose;
	}

	static boolean isWatcherShallCloseInternal() {
		return watcherShallClose;
	}

	public static void setWatcherShallClose(final boolean watcherShallClose) {
		IMAPProperties.watcherShallClose = watcherShallClose;
	}

	public static int getWatcherTime() throws IMAPException {
		checkGlobalImapProperties();
		return watcherTime;
	}

	static int getWatcherTimeInternal() {
		return watcherTime;
	}

	public static void setWatcherTime(final int watcherTime) {
		IMAPProperties.watcherTime = watcherTime;
	}

	public static int getWatcherFrequency() throws IMAPException {
		checkGlobalImapProperties();
		return watcherFrequency;
	}

	static int getWatcherFrequencyInternal() {
		return watcherFrequency;
	}

	public static void setWatcherFrequency(final int watcherFrequency) {
		IMAPProperties.watcherFrequency = watcherFrequency;
	}

	public static IMAPPropertiesFactory.IMAPCredSrc getImapCredSrc() throws IMAPException {
		checkGlobalImapProperties();
		return imapCredSrc;
	}

	static IMAPPropertiesFactory.IMAPCredSrc getImapCredSrcInternal() {
		return imapCredSrc;
	}

	public static void setImapCredSrc(final IMAPPropertiesFactory.IMAPCredSrc imapCredSrc) {
		IMAPProperties.imapCredSrc = imapCredSrc;
	}

	public static IMAPPropertiesFactory.IMAPLoginType getImapLoginType() throws IMAPException {
		checkGlobalImapProperties();
		return imapLoginType;
	}

	static IMAPPropertiesFactory.IMAPLoginType getImapLoginTypeInternal() {
		return imapLoginType;
	}

	public static void setImapLoginType(final IMAPPropertiesFactory.IMAPLoginType imapLoginType) {
		IMAPProperties.imapLoginType = imapLoginType;
	}

	public static Properties getJavaMailProperties() throws IMAPException {
		checkGlobalImapProperties();
		return javaMailProperties;
	}

	static Properties getJavaMailPropertiesInternal() {
		return javaMailProperties;
	}

	public static void setJavaMailProperties(final Properties javaMailProperties) {
		IMAPProperties.javaMailProperties = javaMailProperties;
	}

	private static final void checkGlobalImapProperties() throws IMAPException {
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
