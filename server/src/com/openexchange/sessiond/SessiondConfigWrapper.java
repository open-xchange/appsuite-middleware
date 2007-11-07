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



package com.openexchange.sessiond;

import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.server.Version;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SessionConfig
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class SessiondConfigWrapper extends AbstractConfigWrapper implements SessiondConfig{
	
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
	
	private static final Log LOG = LogFactory.getLog(SessiondConfigWrapper.class);
	
	public SessiondConfigWrapper(String propfile) {
		if (isInit) {
			return ;
		}
		
		if (propfile == null) {
			LOG.error("missing propfile");
			return ;
		}
		Properties prop = null;
		
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("try to load propfile: " + propfile);
			}
			
			prop = new Properties();
			prop.load(new FileInputStream(propfile));
		} catch (FileNotFoundException exc) {
			LOG.error("cannot find propfile: " + propfile, exc);
		} catch (IOException exc) {
			LOG.error("cannot read propfile: " + propfile, exc);
		}
		
		isServerSocketEnabled = parseProperty(prop, "com.openexchange.sessiond.isServerSocketEnabled", isServerSocketEnabled);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.isServerSocketEnabled=" + isServerSocketEnabled);
		}
		
		isServerObjectStreamSocketEnabled = parseProperty(prop, "com.openexchange.sessiond.isServerObjectStreamSocketEnabled", isServerObjectStreamSocketEnabled);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.isServerObjectStreamSocketEnabled=" + isServerObjectStreamSocketEnabled);
		}
		
		isTcpClientSocketEnabled = parseProperty(prop, "com.openexchange.sessiond.isTcpClientSocketEnabled", isTcpClientSocketEnabled);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.isTcpClientSocketEnabled=" + isTcpClientSocketEnabled);
		}

		sessionContainerTimeout = parseProperty(prop, "com.openexchange.sessiond.sessionContainerTimeout", sessionContainerTimeout);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.sessionContainerTimeout=" + sessionContainerTimeout);
		}

		numberOfSessionContainers = parseProperty(prop, "com.openexchange.sessiond.numberOfSessionContainers", numberOfSessionContainers);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.numberOfSessionContainers=" + numberOfSessionContainers);
		}
		
		serverPort = parseProperty(prop, "com.openexchange.sessiond.serverPort", serverPort);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.serverPort=" + serverPort);
		}
		
		serverObjectStreamPort = parseProperty(prop, "com.openexchange.sessiond.serverObjectStreamPort", serverObjectStreamPort);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.serverObjectStreamPort=" + serverObjectStreamPort);
		}
		
		maxSession = parseProperty(prop, "com.openexchange.sessiond.maxSession", maxSession);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.maxSession=" + maxSession);
		}
		
		serverBindAddress = parseProperty(prop, "com.openexchange.sessiond.serverBindAddress", serverBindAddress);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.serverBindAddress=" + serverBindAddress);
		}
		
		isDoubleLoginPermitted = parseProperty(prop, "com.openexchange.sessiond.isDoubleLoginPermitted", isDoubleLoginPermitted);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.isDoubleLoginPermitted=" + isDoubleLoginPermitted);
		}
		
		sessionAuthUser = parseProperty(prop, "com.openexchange.sessiond.sessionAuthUser", sessionAuthUser);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.sessionAuthUser=" + sessionAuthUser);
		}
		
		isSecureConnectionEnabled = parseProperty(prop, "com.openexchange.sessiond.isSecureConnection", isSecureConnectionEnabled);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.isSecureConnection=" + isSecureConnectionEnabled);
		}
		
		sessionDefaultLifeTime = parseProperty(prop, "com.openexchange.sessiond.sessionDefaultLifeTime", sessionDefaultLifeTime);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.sessionDefaultLifeTime=" + sessionDefaultLifeTime);
		}

		randomTokenTimeout = parseProperty(prop, "com.openexchange.sessiond.randomTokenTimeout", randomTokenTimeout);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.randomTokenTimeout=" + randomTokenTimeout);
		}
		
		caFile = parseProperty(prop, "com.openexchange.sessiond.caFile", caFile);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.caFile=" + caFile);
		}
		
		certFile = parseProperty(prop, "com.openexchange.sessiond.certFile", certFile);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.certFile=" + certFile);
		}
		
		keyFile = parseProperty(prop, "com.openexchange.sessiond.keyFile", keyFile);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sessiond property: com.openexchange.sessiond.keyFile=" + keyFile);
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
	
	public boolean isSecureConnection() {
		return Version.isSSL();
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





