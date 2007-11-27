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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;

import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.mail.config.GlobalMailConfig;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPConfig extends MailConfig {

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
	public static IMAPConfig getImapConfig(final Session session) throws MailConfigException {
		final IMAPConfig imapConf = new IMAPConfig();
		fillLoginAndPassword(imapConf, session);
		/*
		 * Fetch user object and create its IMAP properties
		 */
		final User user = UserStorage.getStorageUser(session.getUserId(), session.getContext());
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
	 * Gets the fastFetch
	 * 
	 * @return the fastFetch
	 */
	public static boolean isFastFetch() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).isFastFetch();
	}

	/**
	 * Gets the imapAuthEnc
	 * 
	 * @return the imapAuthEnc
	 */
	public static String getImapAuthEnc() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getImapAuthEnc();
	}

	/**
	 * Gets the imapConnectionIdleTime
	 * 
	 * @return the imapConnectionIdleTime
	 */
	public static int getImapConnectionIdleTime() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getImapConnectionIdleTime();
	}

	/**
	 * Gets the imapConnectionTimeout
	 * 
	 * @return the imapConnectionTimeout
	 */
	public static int getImapConnectionTimeout() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getImapConnectionTimeout();
	}

	/**
	 * Gets the imapSearch
	 * 
	 * @return the imapSearch
	 */
	public boolean isImapSearch() {
		final boolean imapSearch = ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).isImapSearch();
		if (capabilitiesLoaded.get()) {
			return (imapSearch && (imapCapabilities.hasIMAP4rev1() || imapCapabilities.hasIMAP4()));
		}
		return imapSearch;
	}

	/**
	 * Gets the imapsEnabled
	 * 
	 * @return the imapsEnabled
	 */
	public static boolean isImapsEnabled() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).isImapsEnabled();
	}

	/**
	 * Gets the imapSort
	 * 
	 * @return the imapSort
	 */
	public boolean isImapSort() {
		final boolean imapSort = ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).isImapSort();
		if (capabilitiesLoaded.get()) {
			return (imapSort && imapCapabilities.hasSort());
		}
		return imapSort;
	}

	/**
	 * Gets the imapsPort
	 * 
	 * @return the imapsPort
	 */
	public static int getImapsPort() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getImapsPort();
	}

	/**
	 * Gets the imapTimeout
	 * 
	 * @return the imapTimeout
	 */
	public static int getImapTimeout() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getImapTimeout();
	}

	/**
	 * Gets the global supportsACLs
	 * 
	 * @return the global supportsACLs
	 */
	public static boolean isSupportsACLsConfig() {
		return BoolCapVal.TRUE.equals(((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getSupportsACLs()) ? true
				: false;
	}

	/**
	 * Gets the user2acl implementation's canonical class name
	 * 
	 * @return The user2acl implementation's canonical class name
	 */
	public static String getUser2AclImpl() {
		return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getUser2AclImpl();
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
		if (((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getNewACLExtMap().containsKey(imapServer)) {
			return ((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getNewACLExtMap().get(imapServer).booleanValue();
		}
		return false;
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
		((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getNewACLExtMap().put(imapServer,
				Boolean.valueOf(newACLExt));
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
				imapCaps.setHasSubscription(!IMAPConfig.isIgnoreSubscription());
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
	 */
	public boolean isSupportsACLs() {
		if (capabilitiesLoaded.get()
				&& BoolCapVal.AUTO.equals(((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getSupportsACLs())) {
			return imapCapabilities.hasACL();
		}
		return BoolCapVal.TRUE.equals(((GlobalIMAPConfig) GlobalMailConfig.getInstance()).getSupportsACLs()) ? true
				: false;
	}
}
