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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.config.GlobalMailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailConfig.BoolCapVal;

/**
 * {@link GlobalIMAPConfig} - Global IMAP properties
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class GlobalIMAPConfig extends GlobalMailConfig {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(GlobalIMAPConfig.class);

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	/*
	 * Fields for global properties
	 */
	private boolean imapSort;

	private boolean imapSearch;

	private boolean fastFetch;

	private boolean imapsEnabled;

	private int imapsPort;

	private BoolCapVal supportsACLs;

	private int imapTimeout;

	private int imapConnectionTimeout;

	private int imapConnectionIdleTime;

	private String imapAuthEnc;

	private String user2AclImpl;

	private final Map<String, Boolean> newACLExtMap = new ConcurrentHashMap<String, Boolean>();

	/**
	 * Initializes a new global IMAP config
	 */
	public GlobalIMAPConfig() {
		super();
	}

	/*
	 * @see com.openexchange.mail.config.GlobalMailConfig#loadGlobalSubConfig()
	 */
	@Override
	protected void loadGlobalSubConfig() throws MailConfigException {
		loadGlobalIMAPConfig();
	}

	private void loadGlobalIMAPConfig() throws MailConfigException {
		final Properties imapProperties;
		{
			String propFile = SystemConfig.getProperty("IMAPProperties");
			if (propFile == null || (propFile = propFile.trim()).length() == 0) {
				throw new MailConfigException("Property \"IMAPProperties\" not defined in system.properties");
			}
			imapProperties = readPropertiesFromFile(propFile);
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
				logBuilder.append("\tIMAP Connection Timeout: Invalid value \"").append(imapConTimeoutStr).append(
						"\". Setting to fallback: ").append(imapConnectionTimeout).append('\n');
			}
		}

		{
			final String maxConIdleTime = imapProperties.getProperty("maxIMAPConnectionIdleTime", "60000").trim();
			try {
				imapConnectionIdleTime = Integer.parseInt(maxConIdleTime);
				logBuilder.append("\tMax IMAP Connection Idle Time: ").append(imapConnectionIdleTime).append('\n');
			} catch (final NumberFormatException e) {
				imapConnectionIdleTime = 60000;
				logBuilder.append("\tMax IMAP Connection Idle Time: Invalid value \"").append(maxConIdleTime).append(
						"\". Setting to fallback: ").append(imapConnectionIdleTime).append('\n');
			}
		}

		{
			final String imapAuthEncStr = imapProperties.getProperty("imapAuthEnc", "UTF-8").trim();
			if (Charset.isSupported(imapAuthEncStr)) {
				imapAuthEnc = imapAuthEncStr;
				logBuilder.append("\tAuthentication Encoding: ").append(imapAuthEnc).append('\n');
			} else {
				imapAuthEnc = "UTF-8";
				logBuilder.append("\tAuthentication Encoding: Unsupported charset \"").append(imapAuthEncStr).append(
						"\". Setting to fallback: ").append(imapAuthEnc).append('\n');
			}
		}

		{
			user2AclImpl = imapProperties.getProperty("User2ACLImpl");
			if (null != user2AclImpl) {
				user2AclImpl.trim();
			}
		}

		logBuilder.append("Global IMAP properties successfully loaded!");
		if (LOG.isInfoEnabled()) {
			LOG.info(logBuilder.toString());
		}
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
	 * Gets the imapsEnabled
	 * 
	 * @return the imapsEnabled
	 */
	boolean isImapsEnabled() {
		return imapsEnabled;
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
	 * Gets the imapsPort
	 * 
	 * @return the imapsPort
	 */
	int getImapsPort() {
		return imapsPort;
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
	 * Gets the newACLExtMap
	 * 
	 * @return the newACLExtMap
	 */
	Map<String, Boolean> getNewACLExtMap() {
		return newACLExtMap;
	}

}
