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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * SocketHandler
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class SocketHandler implements Runnable {
	
	private ServerSocket ss;
	
//	private SSLServerSocket ssl;
//	private SSLCtx ctx;
	
	private Thread th;
	
	private boolean objectstream = false;
	
	private SessiondConfig config = null;
	
	private boolean isSecureConnection = false;
	
	private static final Log LOG = LogFactory.getLog(SocketHandler.class);
	
	public SocketHandler(SessiondConfig config) {
		this.config = config;
		
		isSecureConnection = config.isSecureConnection();
		
		startSocket();
	}
	
	private void startSocket() {
		try {
			final String bindAddress = config.getServerBindAddress();
			
			boolean acceptAll = false;
			if (bindAddress == null) {
				acceptAll = true;
			} else {
				if ("*".equals(bindAddress)) {
					acceptAll = true;
				}
			}
			
			if (isSecureConnection) {
//				ctx = new SSLCtx(config.getCAFile(), config.getCertFile(), config.getKeyFile());
//				
//				if (objectstream) {
//					if (acceptAll) {
//						ssl = new SSLServerSocket(ctx, config.getServerObjectStreamPort(), 0);
//					} else {
//						ssl = new SSLServerSocket(ctx, config.getServerPort(), 0, InetAddress.getByName(config.getServerBindAddress()));
//					}
//				} else {
//					if (acceptAll) {
//						ssl = new SSLServerSocket(ctx, config.getServerObjectStreamPort(), 0);
//					} else {
//						ssl = new SSLServerSocket(ctx, config.getServerPort(), 0, InetAddress.getByName(config.getServerBindAddress()));
//					}
//				}
			} else {
				if (objectstream) {
					if (acceptAll) {
						ss = new ServerSocket(config.getServerObjectStreamPort(), 0);
					} else {
						ss = new ServerSocket(config.getServerObjectStreamPort(), 0, InetAddress.getByName(config.getServerBindAddress()));
					}
				} else {
					if (acceptAll) {
						ss = new ServerSocket(config.getServerPort(), 0);
					} else {
						ss = new ServerSocket(config.getServerPort(), 0, InetAddress.getByName(config.getServerBindAddress()));
					}
				}
			}
			
			th = new Thread(this);
            th.setName(SocketHandler.class.getName());
			th.start();
		} catch (Exception exc) {
			LOG.error("startSocket", exc);
		}
	}
	
	private SessionRequest sessionReq;
	
	public void run() {
		while (th != null) {
			Socket socket = null;
//			SSLSocket sslSocket = null;
			
			try {
				if (isSecureConnection) {
//					sslSocket = ssl.accept();
//					sslSocket.setSoTimeout(60000);
//					
//					if (!objectstream) {
//						doRequest(sslSocket, config);
//					}
				} else {
					socket = ss.accept();
					socket.setSoTimeout(60000);
					
					if (!objectstream) {
						doRequest(socket, config);
					}
				}
			} catch (Exception exc) {
				LOG.error("run", exc);
			}
		}
	}

	private void doRequest(final Socket socket, final SessiondConfig config) {
		sessionReq = new SessionRequest(socket, config);
		sessionReq.init();
	}

	public void close(){
		
		sessionReq.close();
		
		try {
			th.interrupt();
		
			th.join();
		} catch (InterruptedException ie){
			LOG.info("Stopping Thread", ie);
		}
	}
	
//	private void doRequest(final SSLSocket sslSocket, final SessiondConfig config) {
//		final SessionRequest sessionReq = new SessionRequest(sslSocket, config);
//		sessionReq.init();
//	}
}



