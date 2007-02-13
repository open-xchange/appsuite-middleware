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

import com.openexchange.groupware.contexts.ContextNotFoundException;
import com.openexchange.ssl.SSLSocket;
import com.openexchange.tools.encoding.Base64;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SessiondConnector
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class SessiondConnector {
	
	protected static SessiondConfig config = null;
	
	private Socket s = null;
	private SSLSocket ssl = null;
	private InputStream is = null;
	private OutputStream os = null;
	
	private static final Log LOG = LogFactory.getLog(SessiondConnector.class);
	
	protected SessiondConnector() {
		
	}
	
	public static SessiondConnector getInstance() {
		if (config == null) {
			LOG.error("SessiondConfig is null!");
			
			return null;
		}

		return new SessiondConnector();
	}
	
	public static void setConfig(final SessiondConfig config) {
		SessiondConnector.config = config;
	}
	
	public void close() {
		if (config.isTcpClientSocketEnabled()) {
			try {
				if (config.isSecureConnection()) {
					ssl.close();
				} else {
					s.close();
				}
			} catch (Exception exc) {
				LOG.error("close", exc);
			}
		}
	}
	
	public SessionObject addSession(final String username, final String password, final String client_ip) throws LoginException, InvalidCredentialsException, UserNotFoundException, UserNotActivatedException, PasswordExpiredException, ContextNotFoundException, MaxSessionLimitException, SessiondException {
		return SessionHandler.addSession(username, password, client_ip, null);
	} 
	
	public SessionObject addSession(final String username, final String password, final String sessionid, final String client_ip, final String host, final String data) throws LoginException, InvalidCredentialsException, UserNotFoundException, UserNotActivatedException, PasswordExpiredException, ContextNotFoundException, MaxSessionLimitException, SessiondException {
		if (config.isTcpClientSocketEnabled()) {
			// TCP Connection enabled
			
			try {
				initSockets();
				
				final byte[] b_out = ("add: "+sessionid+" "+makeAuthData(username, password, "EN", client_ip, host)+" "+data).getBytes();
				os.write(b_out);
				
				final byte[] b_in = new byte[8192];
				is.read(b_in);
				
				final String response = new String(b_in);
				
				if (response.startsWith("OK")) {
					return null;
				} else {
					return null;
				}
			} catch (Exception exc) {
				LOG.error("addSession", exc);
			}
			
			return null;
		} else {
			return SessionHandler.addSession(username, password, client_ip, host);
		}
	}
	
	public boolean refreshSession(final String sessionid) {
		if (config.isTcpClientSocketEnabled()) {
			try {
				// TCP Connection enabled
				
				initSockets();
				
				final byte[] b_out = ("ping: "+sessionid).getBytes();
				os.write(b_out);
				
				final byte[] b_in = new byte[8192];
				is.read(b_in);
				
				final String response = new String(b_in);
				
				if (response.startsWith("OK")) {
					return true;
				} else {
					return false;
				}
			} catch (Exception exc) {
				LOG.error("refreshSession", exc);
			}
			
			return false;
		} else {
			return SessionHandler.refreshSession(sessionid);
		}
	}
	
	public boolean removeSession(final String sessionid) {
		if (config.isTcpClientSocketEnabled()) {
			try {
				// TCP Connection enabled
				
				initSockets();
				
				final byte[] b_out = ("ping: "+sessionid).getBytes();
				os.write(b_out);
				
				final byte[] b_in = new byte[8192];
				is.read(b_in);
				
				final String response = new String(b_in);
				
				if (response.startsWith("OK")) {
					return true;
				} else {
					return false;
				}
			} catch (Exception exc) {
				LOG.error("removeSession", exc);
			}
			
			return false;
		} else {
			return SessionHandler.clearSession(sessionid);
		}
	}
	
	public SessionObject getSessionByRandomToken(final String randomToken) {
		return SessionHandler.getSessionByRandomToken(randomToken);
	}
	
	public SessionObject getSession(final String sessionid) {
		return getSession(sessionid, true);
	}
	
	public SessionObject getSession(final String sessionid, final boolean refresh) {
		if (config.isTcpClientSocketEnabled()) {
			try {
				// TCP Connection enabled
				
				initSockets();
				
				final byte[] b_out = ("read: "+sessionid).getBytes();
				os.write(b_out);
				
				final byte[] b_in = new byte[8192];
				is.read(b_in);
				
				final String response = new String(b_in);
				
				if (response != null && !response.startsWith("ERROR:")) {
					final SessionObject sessionobject = new SessionObject(sessionid);
					
					final String authdata = new String(Base64.decode(response), "UTF-8");
					
					final StringTokenizer st = new StringTokenizer(authdata, "\1");
					final String username = st.nextToken();
					final String password = st.nextToken();
					final String language = st.nextToken();
					final String localip = st.nextToken();
					final String host = st.nextToken();
					
					sessionobject.setUsername(username);
					sessionobject.setUserlogin(username);
					sessionobject.setPassword(password);
					sessionobject.setLanguage(language);
					sessionobject.setLocalIp(localip);
					sessionobject.setHost(host);
					
					return sessionobject;
				} else {
					return null;
				}
			} catch (Exception exc) {
				LOG.error("removeSession", exc);
			}
			
			return null;
		} else {
			return SessionHandler.getSession(sessionid, refresh);
		}
	}
	
	public Iterator getSessions() {
		if (config.isTcpClientSocketEnabled()) {
			LOG.warn("NOT IMPLEMENTED YET!");
			return null;
		} else {		
			return SessionHandler.getSessions();
		} 
	}
	
	private void initSockets() throws Exception {
		/*
		if (ENABLE_SSL) {
			ctx = new SSLCtx(config.getConfigPath() + "/sslcerts/oxCA/cacert.pem", config.getConfigPath() + "/sslcerts/oxCERTS/groupwarecert.pem", config.getConfigPath() + "/sslcerts/oxCERTS/groupwarekey.pem");
			ssl  = new SSLSocket(ctx, config.getHost(), config.getServerPort());
			is = ssl.getInputStream();
			os = ssl.getOutputStream();
		} else {
			s = new Socket(config.getHost(), config.getServerPort());
			is = s.getInputStream();
			os = s.getOutputStream();
		}
		 */
	}
	
	private String makeAuthData(final String uid, final String pass, final String language, final String localeip, final String remoteip) throws Exception {
		final String sendString = uid+"\1"+pass+"\1"+language+"\1"+localeip+"\1"+remoteip;
		return Base64.encode(sendString);
	}	
}





