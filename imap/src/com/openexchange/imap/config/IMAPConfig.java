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

import javax.mail.MessagingException;

import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPConfig extends MailConfig {

	private static final String PROTOCOL_IMAP_SECURE = "imaps";

	/**
	 * Gets the block size in which large IMAP commands' UIDs/sequence numbers
	 * arguments get splitted.
	 * 
	 * @return The block size
	 */
	public static int getBlockSize() {
		return IMAPProperties.getInstance().getBlockSize();
	}

	/**
	 * Gets the imapAuthEnc
	 * 
	 * @return the imapAuthEnc
	 */
	public static String getImapAuthEnc() {
		return IMAPProperties.getInstance().getImapAuthEnc();
	}

	/**
	 * Gets the imapConnectionIdleTime
	 * 
	 * @return the imapConnectionIdleTime
	 */
	public static int getImapConnectionIdleTime() {
		return IMAPProperties.getInstance().getImapConnectionIdleTime();
	}

	/**
	 * Gets the imapConnectionTimeout
	 * 
	 * @return the imapConnectionTimeout
	 */
	public static int getImapConnectionTimeout() {
		return IMAPProperties.getInstance().getImapConnectionTimeout();
	}

	/**
	 * Gets the imapTimeout
	 * 
	 * @return the imapTimeout
	 */
	public static int getImapTimeout() {
		return IMAPProperties.getInstance().getImapTimeout();
	}

	/**
	 * Gets the user2acl implementation's canonical class name
	 * 
	 * @return The user2acl implementation's canonical class name
	 */
	public static String getUser2AclImpl() {
		return IMAPProperties.getInstance().getUser2AclImpl();
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
		if (IMAPProperties.getInstance().getNewACLExtMap().containsKey(imapServer)) {
			return IMAPProperties.getInstance().getNewACLExtMap().get(imapServer).booleanValue();
		}
		return false;
	}

	/**
	 * Gets the fastFetch
	 * 
	 * @return the fastFetch
	 */
	public static boolean isFastFetch() {
		return IMAPProperties.getInstance().isFastFetch();
	}

	/**
	 * Checks if mbox format is enabled
	 * 
	 * @return <code>true</code> if mbox format is enabled; otherwise
	 *         <code>false</code>
	 */
	public static boolean isMBoxEnabled() {
		return IMAPProperties.getInstance().isMBoxEnabled();
	}

	/**
	 * Gets the global supportsACLs
	 * 
	 * @return the global supportsACLs
	 */
	public static BoolCapVal isSupportsACLsConfig() {
		return IMAPProperties.getInstance().getSupportsACLs();
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
		IMAPProperties.getInstance().getNewACLExtMap().put(imapServer, Boolean.valueOf(newACLExt));
	}

	private final AtomicBoolean capabilitiesLoaded = new AtomicBoolean();

	private IMAPCapabilities imapCapabilities;

	private int imapPort;

	/*
	 * User-specific fields
	 */
	private String imapServer;

	private boolean secure;

	/**
	 * Default constructor
	 */
	public IMAPConfig() {
		super();
	}

	@Override
	public MailCapabilities getCapabilities() {
		return capabilitiesLoaded.get() ? imapCapabilities : MailCapabilities.EMPTY_CAPS;
	}

	/**
	 * Gets the IMAP capabilities
	 * 
	 * @return The IMAP capabilities
	 */
	public IMAPCapabilities getImapCapabilities() {
		return imapCapabilities;
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
	 * Initializes IMAP server's capabilities if not done, yet
	 * 
	 * @param imapStore
	 *            The IMAP store from which to fetch the capabilities
	 * @throws MailConfigException
	 *             If IMAP capabilities cannot be initialized
	 */
	public void initializeCapabilities(final IMAPStore imapStore) throws MailConfigException {
		if (!capabilitiesLoaded.get()) {
			synchronized (capabilitiesLoaded) {
				if (capabilitiesLoaded.get()) {
					return;
				}
				try {
					final IMAPCapabilities imapCaps = new IMAPCapabilities();
					imapCaps.setACL(imapStore.hasCapability(IMAPCapabilities.CAP_ACL));
					imapCaps.setThreadReferences(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_REFERENCES));
					imapCaps.setThreadOrderedSubject(imapStore
							.hasCapability(IMAPCapabilities.CAP_THREAD_ORDEREDSUBJECT));
					imapCaps.setQuota(imapStore.hasCapability(IMAPCapabilities.CAP_QUOTA));
					imapCaps.setSort(imapStore.hasCapability(IMAPCapabilities.CAP_SORT));
					imapCaps.setIMAP4(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4));
					imapCaps.setIMAP4rev1(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4_REV1));
					imapCaps.setUIDPlus(imapStore.hasCapability(IMAPCapabilities.CAP_UIDPLUS));
					imapCaps.setNamespace(imapStore.hasCapability(IMAPCapabilities.CAP_NAMESPACE));
					imapCaps.setIdle(imapStore.hasCapability(IMAPCapabilities.CAP_IDLE));
					imapCaps.setHasSubscription(!IMAPConfig.isIgnoreSubscription());
					imapCapabilities = imapCaps;
					capabilitiesLoaded.set(true);
				} catch (final MessagingException e) {
					throw new MailConfigException(e);
				}
			}
		}
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
	 * Gets the imapSearch
	 * 
	 * @return the imapSearch
	 */
	public boolean isImapSearch() {
		final boolean imapSearch = IMAPProperties.getInstance().isImapSearch();
		if (capabilitiesLoaded.get()) {
			return (imapSearch && (imapCapabilities.hasIMAP4rev1() || imapCapabilities.hasIMAP4()));
		}
		return imapSearch;
	}

	/**
	 * Gets the imapSort
	 * 
	 * @return the imapSort
	 */
	public boolean isImapSort() {
		final boolean imapSort = IMAPProperties.getInstance().isImapSort();
		if (capabilitiesLoaded.get()) {
			return (imapSort && imapCapabilities.hasSort());
		}
		return imapSort;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	/**
	 * Gets the supportsACLs
	 * 
	 * @return the supportsACLs
	 */
	public boolean isSupportsACLs() {
		if (capabilitiesLoaded.get() && BoolCapVal.AUTO.equals(IMAPProperties.getInstance().getSupportsACLs())) {
			return imapCapabilities.hasPermissions();
		}
		return BoolCapVal.TRUE.equals(IMAPProperties.getInstance().getSupportsACLs()) ? true : false;
	}

	@Override
	protected void parseServerURL(final String serverURL) {
		imapServer = serverURL;
		imapPort = 143;
		{
			final String[] parsed = parseProtocol(imapServer);
			if (parsed != null) {
				secure = PROTOCOL_IMAP_SECURE.equals(parsed[0]);
				imapServer = parsed[1];
			} else {
				secure = false;
			}
			final int pos = imapServer.indexOf(':');
			if (pos > -1) {
				imapPort = Integer.parseInt(imapServer.substring(pos + 1));
				imapServer = imapServer.substring(0, pos);
			}
		}
	}
}
