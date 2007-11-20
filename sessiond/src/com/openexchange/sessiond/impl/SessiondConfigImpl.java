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

package com.openexchange.sessiond.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.config.Configuration;

/**
 * SessionConfig
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class SessiondConfigImpl extends AbstractConfigWrapper implements
		SessiondConfigInterface {

	private boolean isServerSocketEnabled;

	private boolean isServerObjectStreamSocketEnabled;

	private boolean isTcpClientSocketEnabled;

	private int sessionContainerTimeout = 4200000;

	private int numberOfSessionContainers = 8;

	private int serverPort = 33333;

	private int serverObjectStreamPort = 33335;

	private int maxSession = 5000;

	private String serverBindAddress = "localhost";

	private boolean isDoubleLoginPermitted = true;

	private String sessionAuthUser = "mailadmin";

	private boolean isSecureConnectionEnabled;

	private int sessionDefaultLifeTime = 3600000;

	private int randomTokenTimeout = 60000;

	private String caFile;

	private String certFile;

	private String keyFile;

	private static boolean isInit;

	private static final Log LOG = LogFactory.getLog(SessiondConfigImpl.class);
	
	public SessiondConfigImpl(final Configuration conf) {
		this(conf, false);
	}

	public SessiondConfigImpl(final Configuration conf, final boolean ignoreIsInit) {
		if (!ignoreIsInit && isInit) {
			return;
		}

		try {
			isServerSocketEnabled = parseProperty(conf,
					"com.openexchange.sessiond.isServerSocketEnabled",
					isServerSocketEnabled);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.isServerSocketEnabled="
								+ isServerSocketEnabled);
			}

			isServerObjectStreamSocketEnabled = parseProperty(
					conf,
					"com.openexchange.sessiond.isServerObjectStreamSocketEnabled",
					isServerObjectStreamSocketEnabled);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.isServerObjectStreamSocketEnabled="
								+ isServerObjectStreamSocketEnabled);
			}

			isTcpClientSocketEnabled = parseProperty(conf,
					"com.openexchange.sessiond.isTcpClientSocketEnabled",
					isTcpClientSocketEnabled);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.isTcpClientSocketEnabled="
								+ isTcpClientSocketEnabled);
			}

			sessionContainerTimeout = parseProperty(conf,
					"com.openexchange.sessiond.sessionContainerTimeout",
					sessionContainerTimeout);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.sessionContainerTimeout="
								+ sessionContainerTimeout);
			}

			numberOfSessionContainers = parseProperty(conf,
					"com.openexchange.sessiond.numberOfSessionContainers",
					numberOfSessionContainers);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.numberOfSessionContainers="
								+ numberOfSessionContainers);
			}

			serverPort = parseProperty(conf,
					"com.openexchange.sessiond.serverPort", serverPort);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.serverPort="
								+ serverPort);
			}

			serverObjectStreamPort = parseProperty(conf,
					"com.openexchange.sessiond.serverObjectStreamPort",
					serverObjectStreamPort);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.serverObjectStreamPort="
								+ serverObjectStreamPort);
			}

			maxSession = parseProperty(conf,
					"com.openexchange.sessiond.maxSession", maxSession);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.maxSession="
								+ maxSession);
			}

			serverBindAddress = parseProperty(conf,
					"com.openexchange.sessiond.serverBindAddress",
					serverBindAddress);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.serverBindAddress="
								+ serverBindAddress);
			}

			isDoubleLoginPermitted = parseProperty(conf,
					"com.openexchange.sessiond.isDoubleLoginPermitted",
					isDoubleLoginPermitted);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.isDoubleLoginPermitted="
								+ isDoubleLoginPermitted);
			}

			sessionAuthUser = parseProperty(conf,
					"com.openexchange.sessiond.sessionAuthUser",
					sessionAuthUser);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.sessionAuthUser="
								+ sessionAuthUser);
			}

			isSecureConnectionEnabled = parseProperty(conf,
					"com.openexchange.sessiond.isSecureConnection",
					isSecureConnectionEnabled);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.isSecureConnection="
								+ isSecureConnectionEnabled);
			}

			sessionDefaultLifeTime = parseProperty(conf,
					"com.openexchange.sessiond.sessionDefaultLifeTime",
					sessionDefaultLifeTime);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.sessionDefaultLifeTime="
								+ sessionDefaultLifeTime);
			}

			randomTokenTimeout = parseProperty(conf,
					"com.openexchange.sessiond.randomTokenTimeout",
					randomTokenTimeout);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.randomTokenTimeout="
								+ randomTokenTimeout);
			}

			caFile = parseProperty(conf, "com.openexchange.sessiond.caFile",
					caFile);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.caFile="
								+ caFile);
			}

			certFile = parseProperty(conf,
					"com.openexchange.sessiond.certFile", certFile);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.certFile="
								+ certFile);
			}

			keyFile = parseProperty(conf, "com.openexchange.sessiond.keyFile",
					keyFile);
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("Sessiond property: com.openexchange.sessiond.keyFile="
								+ keyFile);
			}
		} finally {
			ConfigurationService.getInstance().ungetService();
		}
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getServerObjectStreamPort() {
		return serverObjectStreamPort;
	}

	public boolean isServerSocketEnabled() {
		return isServerSocketEnabled;
	}

	public boolean isServerObjectStreamSocketEnabled() {
		return isServerObjectStreamSocketEnabled;
	}

	public boolean isTcpClientSocketEnabled() {
		return isTcpClientSocketEnabled;
	}

	public int getSessionContainerTimeout() {
		return sessionContainerTimeout;
	}

	public int getNumberOfSessionContainers() {
		return numberOfSessionContainers;
	}

	public int getMaxSessions() {
		return maxSession;
	}

	public String getServerBindAddress() {
		return serverBindAddress;
	}

	public boolean isDoubleLoginPermitted() {
		return isDoubleLoginPermitted;
	}

	public String getSessionAuthUser() {
		return sessionAuthUser;
	}

	public int getLifeTime() {
		return sessionDefaultLifeTime;
	}

	public String getCAFile() {
		return caFile;
	}

	public String getCertFile() {
		return certFile;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public int getRandomTokenTimeout() {
		return randomTokenTimeout;
	}
}
