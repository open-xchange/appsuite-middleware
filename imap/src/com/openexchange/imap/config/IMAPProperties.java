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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailConfig.BoolCapVal;
import com.openexchange.mail.config.MailConfigException;

/**
 * {@link IMAPProperties}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPProperties extends AbstractProtocolProperties {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPProperties.class);

	private static final IMAPProperties instance = new IMAPProperties();

	/**
	 * Gets the singleton instance of {@link IMAPProperties}
	 * 
	 * @return The singleton instance of {@link IMAPProperties}
	 */
	public static IMAPProperties getInstance() {
		return instance;
	}

	/*
	 * Fields for global properties
	 */
	private boolean imapSort;

	private boolean imapSearch;

	private boolean fastFetch;

	private BoolCapVal supportsACLs;

	private int imapTimeout;

	private int imapConnectionTimeout;

	private int imapConnectionIdleTime;

	private String imapAuthEnc;

	private String user2AclImpl;

	private boolean mboxEnabled;

	private int blockSize;

	private final Map<String, Boolean> newACLExtMap;

	private ConfigurationServiceHolder configurationServiceHolder;

	/**
	 * Initializes a new {@link IMAPProperties}
	 */
	private IMAPProperties() {
		super();
		newACLExtMap = new ConcurrentHashMap<String, Boolean>();
	}

	/**
	 * Gets the configuration service holder
	 * 
	 * @return The configuration service holder
	 */
	public ConfigurationServiceHolder getConfigurationServiceHolder() {
		return configurationServiceHolder;
	}

	/**
	 * Sets the configuration service holder
	 * 
	 * @param configurationServiceHolder
	 *            The configuration service holder
	 */
	public void setConfigurationServiceHolder(final ConfigurationServiceHolder configurationServiceHolder) {
		this.configurationServiceHolder = configurationServiceHolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.config.AbstractProtocolProperties#loadProperties0()
	 */
	@Override
	protected void loadProperties0() throws MailConfigException {
		final StringBuilder logBuilder = new StringBuilder(1024);
		logBuilder.append("\nLoading global IMAP properties...\n");

		final ConfigurationService configuration = configurationServiceHolder.getService();
		try {
			{
				final String imapSortStr = configuration.getProperty("com.openexchange.imap.imapSort", "application")
						.trim();
				imapSort = "imap".equalsIgnoreCase(imapSortStr);
				logBuilder.append("\tIMAP-Sort: ").append(imapSort).append('\n');
			}

			{
				final String imapSearchStr = configuration.getProperty("com.openexchange.imap.imapSearch", "imap")
						.trim();
				imapSearch = "imap".equalsIgnoreCase(imapSearchStr);
				logBuilder.append("\tIMAP-Search: ").append(imapSearch).append('\n');
			}

			{
				final String fastFetchStr = configuration.getProperty("com.openexchange.imap.imapFastFetch", STR_TRUE)
						.trim();
				fastFetch = Boolean.parseBoolean(fastFetchStr);
				logBuilder.append("\tFast Fetch Enabled: ").append(fastFetch).append('\n');
			}

			{
				final String supportsACLsStr = configuration.getProperty("com.openexchange.imap.imapSupportsACL",
						STR_FALSE).trim();
				supportsACLs = BoolCapVal.parseBoolCapVal(supportsACLsStr);
				logBuilder.append("\tSupport ACLs: ").append(supportsACLs).append('\n');
			}

			{
				final String imapTimeoutStr = configuration.getProperty("com.openexchange.imap.imapTimeout", "0")
						.trim();
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
				final String imapConTimeoutStr = configuration.getProperty(
						"com.openexchange.imap.imapConnectionTimeout", "0").trim();
				try {
					imapConnectionTimeout = Integer.parseInt(imapConTimeoutStr);
					logBuilder.append("\tIMAP Connection Timeout: ").append(imapConnectionTimeout).append('\n');
				} catch (final NumberFormatException e) {
					imapConnectionTimeout = 0;
					logBuilder.append("\tIMAP Connection Timeout: Invalid value \"").append(imapConTimeoutStr).append(
							"\". Setting to fallback: ").append(imapConnectionTimeout).append('\n');
				}
			}

			{
				final String maxConIdleTime = configuration.getProperty(
						"com.openexchange.imap.maxIMAPConnectionIdleTime", "60000").trim();
				try {
					imapConnectionIdleTime = Integer.parseInt(maxConIdleTime);
					logBuilder.append("\tMax IMAP Connection Idle Time: ").append(imapConnectionIdleTime).append('\n');
				} catch (final NumberFormatException e) {
					imapConnectionIdleTime = 60000;
					logBuilder.append("\tMax IMAP Connection Idle Time: Invalid value \"").append(maxConIdleTime)
							.append("\". Setting to fallback: ").append(imapConnectionIdleTime).append('\n');
				}
			}

			{
				final String imapAuthEncStr = configuration.getProperty("com.openexchange.imap.imapAuthEnc", "UTF-8")
						.trim();
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
				user2AclImpl = configuration.getProperty("com.openexchange.imap.User2ACLImpl");
				if (null != user2AclImpl) {
					user2AclImpl = user2AclImpl.trim();
				} else {
					throw new MailConfigException("Missing IMAP property \"com.openexchange.imap.User2ACLImpl\"");
				}
			}

			{
				final String mboxEnabledStr = configuration.getProperty("com.openexchange.imap.mboxEnabled", STR_FALSE)
						.trim();
				mboxEnabled = Boolean.parseBoolean(mboxEnabledStr);
				logBuilder.append("\tMBox Enabled: ").append(mboxEnabled).append('\n');
			}

			{
				final String blockSizeStr = configuration.getProperty("com.openexchange.imap.blockSize", "1000").trim();
				try {
					blockSize = Integer.parseInt(blockSizeStr);
					logBuilder.append("\tBlock Size: ").append(blockSize).append('\n');
				} catch (final NumberFormatException e) {
					blockSize = 1000;
					logBuilder.append("\tBlock Size: Invalid value \"").append(blockSizeStr).append(
							"\". Setting to fallback: ").append(blockSize).append('\n');
				}
			}
		} finally {
			configurationServiceHolder.ungetService(configuration);
		}

		logBuilder.append("Global IMAP properties successfully loaded!");
		if (LOG.isInfoEnabled()) {
			LOG.info(logBuilder.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.config.AbstractProtocolProperties#resetFields()
	 */
	@Override
	protected void resetFields() {
		imapSort = false;
		imapSearch = false;
		fastFetch = false;
		supportsACLs = null;
		imapTimeout = 0;
		imapConnectionTimeout = 0;
		imapConnectionIdleTime = 0;
		imapAuthEnc = null;
		user2AclImpl = null;
		mboxEnabled = false;
		blockSize = 0;
	}

	/**
	 * Gets the fastFetch
	 * 
	 * @return the fastFetch
	 */
	boolean isFastFetch() {
		return fastFetch;
	}

	/**
	 * Gets the imapAuthEnc
	 * 
	 * @return the imapAuthEnc
	 */
	String getImapAuthEnc() {
		return imapAuthEnc;
	}

	/**
	 * Gets the imapConnectionIdleTime
	 * 
	 * @return the imapConnectionIdleTime
	 */
	int getImapConnectionIdleTime() {
		return imapConnectionIdleTime;
	}

	/**
	 * Gets the imapConnectionTimeout
	 * 
	 * @return the imapConnectionTimeout
	 */
	int getImapConnectionTimeout() {
		return imapConnectionTimeout;
	}

	/**
	 * Gets the imapSearch
	 * 
	 * @return the imapSearch
	 */
	boolean isImapSearch() {
		return imapSearch;
	}

	/**
	 * Gets the imapSort
	 * 
	 * @return the imapSort
	 */
	boolean isImapSort() {
		return imapSort;
	}

	/**
	 * Gets the imapTimeout
	 * 
	 * @return the imapTimeout
	 */
	int getImapTimeout() {
		return imapTimeout;
	}

	/**
	 * Gets the supportsACLs
	 * 
	 * @return the supportsACLs
	 */
	BoolCapVal getSupportsACLs() {
		return supportsACLs;
	}

	/**
	 * Gets the user2AclImpl
	 * 
	 * @return the user2AclImpl
	 */
	String getUser2AclImpl() {
		return user2AclImpl;
	}

	/**
	 * Gets the mboxEnabled
	 * 
	 * @return The mboxEnabled
	 */
	boolean isMBoxEnabled() {
		return mboxEnabled;
	}

	/**
	 * Gets the block size in which large IMAP commands' UIDs/sequence numbers
	 * arguments get splitted
	 * 
	 * @return The block size
	 */
	int getBlockSize() {
		return blockSize;
	}

	/**
	 * Gets the newACLExtMap
	 * 
	 * @return the newACLExtMap
	 */
	Map<String, Boolean> getNewACLExtMap() {
		return newACLExtMap;
	}
}
