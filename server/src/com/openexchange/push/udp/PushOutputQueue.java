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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.event.InvalidStateException;
import com.openexchange.server.ServerTimer;
import com.openexchange.tools.StringCollection;

/**
 * PushOutputQueue
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushOutputQueue extends TimerTask {
	
	private static PushConfigInterface pushConfigInterface;
	
	private static boolean isFirst = true;
	
	private static boolean isInit;
	
	private static List<Object> queue1 = new ArrayList<Object>();
	
	private static List<Object> queue2 = new ArrayList<Object>();
	
	private static int delay = 60000;
	
	private static int remoteHostTimeOut = 3600000;
	
	private static Set<RemoteHostObject> remoteHost;
	
	private static InetAddress senderAddress = null;
	
	private static boolean isEnabled;
	
	private static final Log LOG = LogFactory.getLog(PushOutputQueue.class);
	
	public PushOutputQueue(PushConfigInterface pushConfigInterface) {
		this.pushConfigInterface = pushConfigInterface;
		
		remoteHost = pushConfigInterface.getRemoteHost();
		
		if (pushConfigInterface.isPushEnabled()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Starting PushOutputQueue");
			}
			
			remoteHost = pushConfigInterface.getRemoteHost();
			
			delay = pushConfigInterface.getOutputQueueDelay();
			
			remoteHostTimeOut = pushConfigInterface.getRemoteHostTimeOut();
			
			final Timer t = ServerTimer.getTimer();
			t.schedule(this, delay, delay);
			
			isEnabled = true;
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("PushOutputQueue is disabled");
			}
		}
		
		isInit = true;
	}
	
	public static void addRemoteHostObject(final RemoteHostObject remoteHostObject) {
		remoteHost.add(remoteHostObject);
	}
	
	public static void add(final PushObject pushObject) throws InvalidStateException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("add PushObject: " + pushObject);
		}
		
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
	
	public static void add(final RegisterObject registerObject) throws InvalidStateException {
		add(registerObject, false);
	}
	
	public static void add(final RegisterObject registerObject, final boolean noDelay) throws InvalidStateException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("add RegisterObject: " + registerObject);
		}
		
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
	
	/* (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		try {
			if (isFirst) {
				isFirst = false;
				action(queue1);
			} else {
				isFirst = true;
				action(queue2);
			}
		} catch (final Exception exc) {
			LOG.error(exc.getMessage(), exc);
		}
	}
	
	protected static void action(final List<Object> al) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("get push objects from queue: " + al.size());
		}
		
		for (int a = 0; a < al.size(); a++) {
			final Object o = al.get(a);
			
			if (o instanceof PushObject) {
				createPushPackage((PushObject)o);
			} else if (o instanceof RegisterObject) {
				createRegisterPackage((RegisterObject)o);
			}
		}
		
		al.clear();
	}
	
	protected static void createPushPackage(final PushObject pushObject) {
		final int users[] = pushObject.getUsers();
		for (int a = 0; a < users.length; a++) {
			final int contextId = pushObject.getContextId();
			
			if (RegisterHandler.isRegistered(users[a], contextId)) {
				final RegisterObject registerObj = RegisterHandler.getRegisterObject(users[a], contextId);
				final StringBuilder sb = new StringBuilder();
				sb.append(pushObject.getFolderId());
				sb.append('\1');
				try {
					makePackage(sb.toString().getBytes(), registerObj.getHostAddress(), registerObj.getPort());
				} catch (Exception exc) {
					LOG.error("createPushPackage", exc);
				}
			}
		}
		
		if (pushConfigInterface.isEventDistributionEnabled() && !pushObject.isSync()) {
			// if (!pushObject.isSync()) {
				for (RemoteHostObject remoteHostObject : remoteHost) {
					try {
						final StringBuilder sb = new StringBuilder();
						sb.append(PushRequest.MAGIC);
						sb.append('\1');
						
						final StringBuilder data = new StringBuilder();
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
						
						final byte b[] = sb.toString().getBytes();
						
						if (System.currentTimeMillis() <= (remoteHostObject.getTimer().getTime()+remoteHostTimeOut)) {
							makePackage(b, remoteHostObject.getHost(), remoteHostObject.getPort());
						} else {
							if (LOG.isTraceEnabled()) {
								LOG.trace("remote host object is timed out");
							}
						}
					} catch (Exception exc) {
						LOG.error("createPushPackage", exc);
					}
				}
			// }
		}
	}
	
	protected static void createRegisterPackage(final RegisterObject registerObject) {
		if (!registerObject.isSync()) {
			final StringBuilder sb = new StringBuilder();
			sb.append("OK\1");
			try {
				makePackage(sb.toString().getBytes(), registerObject.getHostAddress(), registerObject.getPort());
			} catch (Exception exc) {
				LOG.error("createRegisterPackage", exc);
			}
		}
		
		if (pushConfigInterface.isRegisterDistributionEnabled()) {
			for (RemoteHostObject remoteHostObject : remoteHost) {
				try {
					final StringBuilder sb = new StringBuilder();
					sb.append(PushRequest.MAGIC);
					sb.append('\1');
					
					final StringBuilder data = new StringBuilder();
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
					
					final byte b[] = sb.toString().getBytes();
					if (System.currentTimeMillis() <= (remoteHostObject.getTimer().getTime()+remoteHostTimeOut)) {
						makePackage(b, remoteHostObject.getHost(), remoteHostObject.getPort());
					} else {
						if (LOG.isTraceEnabled()) {
							LOG.trace("remote host object is timed out");
						}
					}
				} catch (Exception exc) {
					LOG.error("createRegisterPackage", exc);
				}
			}
		}
	}
	
	protected static void makePackage(final byte[] b, final String host, final int port) throws Exception {
		makePackage(b, InetAddress.getByName(host), port);
	}
	
	protected static void makePackage(final byte[] b, final InetAddress host, final int port) throws Exception {
		final DatagramSocket datagramSocket = PushSocket.getPushDatagramSocket();
		final DatagramPacket datagramPackage = new DatagramPacket(b, b.length, host, port);
		datagramSocket.send(datagramPackage);
	}
	
	public static int getQueueSize() {
		if (isFirst) {
			return queue1.size();
		}
		return queue2.size();
	}

    public void close() {
        cancel();
        // empty both queues
        try {
            action(queue1);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            action(queue2);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
