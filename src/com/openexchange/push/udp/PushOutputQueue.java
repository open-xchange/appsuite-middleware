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



package com.openexchange.push.udp;

import com.openexchange.event.InvalidStateException;
import com.openexchange.server.ServerTimer;
import com.openexchange.tools.StringCollection;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PushOutputQueue
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushOutputQueue extends TimerTask {
	
	private static PushConfigInterface pushConfigInterface = null;
	
	private static boolean isFirst = true;
	
	private static boolean isInit = false;
	
	private static ArrayList queue1 = new ArrayList();
	
	private static ArrayList queue2 = new ArrayList();
	
	private static int delay = 60000;
	
	private static int remoteHostTimeOut = 3600000;
	
	private static HashSet<RemoteHostObject> remoteHost = null;
	
	private static InetAddress senderAddress = null;
	
	private static boolean isEnabled = false;
	
	private static final Log LOG = LogFactory.getLog(PushOutputQueue.class);
	
	public PushOutputQueue(PushConfigInterface pushConfigInterface) {
		this.pushConfigInterface = pushConfigInterface;
		
		remoteHost = pushConfigInterface.getRemoteHost();
		
		if (pushConfigInterface.isPushEnabled()) {
			LOG.info("Starting PushOutputQueue");
			
			remoteHost = pushConfigInterface.getRemoteHost();
			
			delay = pushConfigInterface.getOutputQueueDelay();
			
			remoteHostTimeOut = pushConfigInterface.getRemoteHostTimeOut();
			
			Timer t = ServerTimer.getTimer();
			t.schedule(this, delay, delay);
			
			isEnabled = true;
		} else {
			LOG.info("PushOutputQueue is disabled");
		}
		
		isInit = true;
	}
	
	public static void addRemoteHostObject(RemoteHostObject remoteHostObject) {
		remoteHost.add(remoteHostObject);
	}
	
	public static void add(PushObject pushObject) throws InvalidStateException {
		LOG.debug("add PushObject: " + pushObject);
		
		if (!isEnabled) {
			return ;
		}
		
		if (!isInit) {
			throw new InvalidStateException("PushOutputQueue not initialisiert!");
		}
		
		if (isFirst) {
			queue1.add(pushObject);
		} else {
			queue2.add(pushObject);
		}
	}
	
	public static void add(RegisterObject registerObject) throws InvalidStateException {
		add(registerObject, false);
	}
	
	public static void add(RegisterObject registerObject, boolean noDelay) throws InvalidStateException {
		LOG.debug("add RegisterObject: " + registerObject);
		
		if (!isEnabled) {
			return ;
		}
		
		if (!isInit) {
			throw new InvalidStateException("PushOutputQueue not initialisiert!");
		}
		
		if (noDelay) {
			createRegisterPackage(registerObject);
		} else {
			if (isFirst) {
				queue1.add(registerObject);
			} else {
				queue2.add(registerObject);
			}
		}
		
	}
	
	public void run() {
		try {
			if (isFirst) {
				isFirst = false;
				action(queue1);
			} else {
				isFirst = true;
				action(queue2);
			}
		} catch (Exception exc) {
			LOG.error("run", exc);
		}
	}
	
	protected static void action(ArrayList al) {
		LOG.debug("get push objects from queue: " + al.size());
		
		for (int a = 0; a < al.size(); a++) {
			Object o = al.get(a);
			
			if (o instanceof PushObject) {
				createPushPackage((PushObject)o);
			} else if (o instanceof RegisterObject) {
				createRegisterPackage((RegisterObject)o);
			}
		}
		
		al.clear();
	}
	
	protected static void createPushPackage(PushObject pushObject) {
		int users[] = pushObject.getUsers();
		for (int a = 0; a < users.length; a++) {
			int contextId = pushObject.getContextId();
			
			if (RegisterHandler.isRegistered(users[a], contextId)) {
				RegisterObject registerObj = RegisterHandler.getRegisterObject(users[a], contextId);
				StringBuffer sb = new StringBuffer();
				sb.append(pushObject.getFolderId());
				sb.append('\1');
				try {
					makePackage(sb.toString().getBytes(), registerObj.getHostAddress(), registerObj.getPort());
				} catch (Exception exc) {
					LOG.error("createPushPackage", exc);
				}
			}
		}
		
		if (pushConfigInterface.isEventDistributionEnabled()) {
			if (!pushObject.isSync()) {
				Iterator<RemoteHostObject> iterator = remoteHost.iterator();
				while (iterator.hasNext()) {
					RemoteHostObject remoteHostObject = iterator.next();
					
					try {
						StringBuffer sb = new StringBuffer();
						sb.append(PushRequest.MAGIC);
						sb.append('\1');
						
						StringBuffer data = new StringBuffer();
						data.append(PushRequest.PUSH_SYNC);
						data.append('\1');
						data.append(pushObject.getFolderId());
						data.append('\1');
						data.append(pushObject.getModule());
						data.append('\1');
						data.append(pushObject.getContextId());
						data.append('\1');
						data.append(StringCollection.convertArray2String(pushObject.getUsers()));
						
						sb.append(data.length());
						sb.append('\1');
						sb.append(data);
						
						byte b[] = sb.toString().getBytes();
						
						if (System.currentTimeMillis() <= (remoteHostObject.getTimer().getTime()+remoteHostTimeOut)) {
							makePackage(b, remoteHostObject.getHost(), remoteHostObject.getPort());
						} else {
							LOG.trace("remote host object is timed out");
						}
					} catch (Exception exc) {
						LOG.error("createPushPackage", exc);
					}
				}
			}
		}
	}
	
	protected static void createRegisterPackage(RegisterObject registerObject) {
		if (!registerObject.isSync()) {
			StringBuffer sb = new StringBuffer();
			sb.append("OK\1");
			try {
				makePackage(sb.toString().getBytes(), registerObject.getHostAddress(), registerObject.getPort());
			} catch (Exception exc) {
				LOG.error("createRegisterPackage", exc);
			}
		}
		
		if (pushConfigInterface.isRegisterDistributionEnabled()) {
			Iterator<RemoteHostObject> iterator = remoteHost.iterator();
			while (iterator.hasNext()) {
				RemoteHostObject remoteHostObject = iterator.next();
				try {
					StringBuffer sb = new StringBuffer();
					sb.append(PushRequest.MAGIC);
					sb.append('\1');
					
					StringBuffer data = new StringBuffer();
					data.append(PushRequest.REGISTER_SYNC);
					data.append('\1');
					data.append(registerObject.getUserId());
					data.append('\1');
					data.append(registerObject.getContextId());
					data.append('\1');
					data.append(registerObject.getHostAddress());
					data.append('\1');
					data.append(registerObject.getPort());
					data.append('\1');
					data.append(registerObject.isSync());
					data.append('\1');
					
					sb.append(data.length());
					sb.append('\1');
					sb.append(data);
					
					byte b[] = sb.toString().getBytes();
					if (System.currentTimeMillis() <= (remoteHostObject.getTimer().getTime()+remoteHostTimeOut)) {
						makePackage(b, remoteHostObject.getHost(), remoteHostObject.getPort());
					} else {
						LOG.trace("remote host object is timed out");
					}
				} catch (Exception exc) {
					LOG.error("createRegisterPackage", exc);
				}
			}
		}
	}
	
	protected static void makePackage(byte[] b, String host, int port) throws Exception {
		makePackage(b, InetAddress.getByName(host), port);
	}
	
	protected static void makePackage(byte[] b, InetAddress host, int port) throws Exception {
		DatagramSocket datagramSocket = PushSocket.getPushDatagramSocket();
		DatagramPacket datagramPackage = new DatagramPacket(b, b.length, host, port);
		datagramSocket.send(datagramPackage);
	}
	
	public static int getQueueSize() {
		if (isFirst) {
			return queue1.size();
		} else {
			return queue2.size();
		}
	}
}
