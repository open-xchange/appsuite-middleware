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

import com.openexchange.ssl.SSLSocket;
import com.openexchange.tools.encoding.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SessionRequest
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class SessionRequest implements Runnable {
	
	private static final String OK = "OK: done";
	
	private static final String ERROR_INVALID_AUTH_DATA = "ERROR: invalid auth data";
	
	private static final String ERROR_UNKNOWN_COMMAND = "ERROR: Unknown command";
	
	private static final String ADD = "add:";
	// private static final String CHECKUSER = "checkuser:";
	private static final String PING = "ping:";
	private static final String CLEAR = "clear:";
	private static final String GETAUTH = "getauth:";
	private static final String GETSESSIONS = "getsessions:";
	
	private Socket socket;
	
	private SSLSocket sslSocket;
	
	private Thread th;
	
	private boolean isSecureConnection;
	
	private SessiondConfig config;
	
	private static final Log LOG = LogFactory.getLog(SessionRequest.class);
	
	public SessionRequest(Socket socket, SessiondConfig config) {
		this.socket = socket;
		this.config = config;
	}
	
	public SessionRequest(SSLSocket sslSocket, SessiondConfig config) {
		this.sslSocket = sslSocket;
		this.config = config;
	}
	
	public void init() {
		isSecureConnection = config.isSecureConnection();
		
		th = new Thread(this);
		th.start();
	}
	
	private void endCon(final Socket s, final SSLSocket s_ssl) {
		try {
			if (isSecureConnection) {
				if (s_ssl != null) {
					s_ssl.close();
				}
			} else {
				if (s != null) {
					s.close();
				}
			}
		} catch (Exception exc) {
			LOG.error("endCon", exc);
		}
	}
	
	public void run() {
		InputStream is = null;
		OutputStream os = null;
		
		try {
			if (isSecureConnection) {
				is = new BufferedInputStream(sslSocket.getInputStream());
				os = new BufferedOutputStream(sslSocket.getOutputStream());
			} else {
				is = socket.getInputStream();
				os = socket.getOutputStream();
			}
			
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final byte[] b = new byte[8192];
			
			final int i = is.read(b);
			baos.write(b, 0, i);
	
			final String[] data = new String(baos.toByteArray()).trim().split(" ");
			
			final String action = data[0];
			
			if (action.startsWith(ADD)) {
				actionAdd(data, os);
			} else if (action.startsWith(PING)) {
				actionPing(data, os);
			} else if (action.startsWith(CLEAR)) {
				actionClear(data, os);
			} else if (action.startsWith(GETAUTH)) {
				actionGetAuth(data, os);
			} else if (action.startsWith(GETSESSIONS)) {
				actionGetSessions(data, os);
			} else {
				LOG.warn("Unknown command:" + action);
				sendResponse(os, ERROR_UNKNOWN_COMMAND.getBytes());
			}
		} catch (Exception exc) {
			LOG.error("run", exc);
		} finally {
			endCon(socket, sslSocket);
		}
	}
	
	protected void sendResponse(final OutputStream os, final byte[] b) {
		try {
			os.write(b);
			os.flush();
		} catch (IOException exc) {
			LOG.error("sendResponse", exc);
		}
	}
	
	protected void actionAdd(final String[] data, final OutputStream os) {
		try {
			final String authData = new String(Base64.decode(data[3]), "UTF-8");
			
			final String[] sAuthData = authData.split("\1");
			
			if (sAuthData.length >= 5) {
				final String uid = sAuthData[0];
				final String password = sAuthData[1];
				final String localIp = sAuthData[3];
				final String host = sAuthData[4];
				final SessionObject sessionObj = SessionHandler.addSession(uid, password, localIp, host);
				sendResponse(os, ("OK: " + sessionObj.getSessionID()).getBytes());
			} else {
				sendResponse(os, ERROR_INVALID_AUTH_DATA.getBytes());
			}
		} catch (Exception exc) {
			LOG.error("addAction", exc);
			sendResponse(os, ("ERROR: " + exc.getMessage()).getBytes());
		}
	}
	
	protected void actionPing(final String[] data, final OutputStream os) {
		try {
			final String sessionid = data[1];
			
			if (SessionHandler.refreshSession(sessionid)) {
				sendResponse(os, OK.getBytes());
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("session id not found: " + sessionid);
				}
				sendResponse(os, ("ERROR: No Session found: " + sessionid).getBytes());
			}
		} catch (Exception exc) {
			LOG.error("actionPing", exc);
			sendResponse(os, ("ERROR: " + exc.getMessage()).getBytes());
		}
	}
	
	protected void actionClear(final String[] data, final OutputStream os) {
		final String sessionid = data[1];
		
		if (SessionHandler.clearSession(sessionid)) {
			sendResponse(os, OK.getBytes());
		} else {
			sendResponse(os, "ERROR: Session don't exists".getBytes());
		}
	}
	
	protected void actionGetAuth(final String[] data, final OutputStream os) {
		final String sessionId = data[1];
		final SessionObject sessionObj = SessionHandler.getSession(sessionId, true);
		
		if (sessionObj != null) {
			sendResponse(os, null); // new BASE64Encoder().encode((sessionObj.getUsername() + "\1" + sessionObj.getPassword() + "\1" + sessionObj.getLanguage() + "\1" + sessionObj.getLocalIp() + "\1" + sessionObj.getHost()).getBytes("UTF-8")));
		} else {
			sendResponse(os, "ERROR: No Session found".getBytes());
		}
	}
	
	protected void actionGetSessions(final String[] data, final OutputStream os) {
		try {
			final String authData = new String(Base64.decode(data[1]), "UTF-8");
			
			long l_cr = 0;
			long l_lt = 0;
			long l_ts = 0;
			
			final String[] sAuthData = authData.split("\1");
			final String uid = sAuthData[0];
			final String password = sAuthData[1];

			if (uid.equals(config.getSessionAuthUser())) {
				SessionObject sessionobject = null;
				final SessiondConnector sc = SessiondConnector.getInstance();
				sessionobject = sc.addSession(uid, password, "localhost");
				
				String tmp_sessionid = null;
				
				final long current = System.currentTimeMillis();
				final Iterator it = SessionHandler.getSessions();
				
				while (it.hasNext()) {
					sessionobject = (SessionObject)it.next();
					tmp_sessionid = sessionobject.getSessionID();
					
					l_cr = sessionobject.getCreationtime().getTime();
					l_ts = sessionobject.getTimestamp().getTime();
					l_lt = sessionobject.getLifetime();
					
					if ((l_ts + l_lt) < current) {
						it.remove();
					} else {
						final String response = Base64.encode((sessionobject.getUsername() + "\1" + sessionobject.getLanguage() + "\1" + sessionobject.getLocalIp() + "\1" + l_cr + "\1" + l_lt).getBytes("UTF-8"));
						
						os.write((tmp_sessionid + ' ' + response + "\n\1\n").getBytes());
						os.flush();
					}
				}
			}
		} catch (Exception exc) {
			LOG.error("actionGetSessions" , exc);
		}
	}
}



