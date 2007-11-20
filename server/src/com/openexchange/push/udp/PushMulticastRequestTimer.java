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

import com.openexchange.server.impl.ServerTimer;

import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PushOutputQueue
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushMulticastRequestTimer extends TimerTask {
	
	private static PushConfigInterface pushConfigInterface;
	
	private static int multicastPort;
	
	private static InetAddress multicastAddress;
	
	private static int remoteHostFresh = 1200000;
	
	private static String MULTICAST_REQUEST_DATA;
	
	private static String MULTICAST_REQUEST;
	
	private static byte[] MULTICAST_REQUEST_BYTES;
	
	private static final Log LOG = LogFactory.getLog(PushMulticastRequestTimer.class);
	
	public PushMulticastRequestTimer(PushConfigInterface pushConfigInterface) {
		this.pushConfigInterface = pushConfigInterface;
		
		InetAddress hostname = pushConfigInterface.getHostName();
		
		if (hostname == null) {
			try {
				hostname = InetAddress.getLocalHost();
			} catch (UnknownHostException exc) {
				LOG.warn("unable to resolv local address", exc);
			}
		}
		
		MULTICAST_REQUEST_DATA = new StringWriter()
		.append(String.valueOf(PushRequest.REMOTE_HOST_REGISTER))
		.append('\1')
		.append(hostname == null ? "localhost" : hostname.getHostName())
		.append('\1')
		.append(String.valueOf(pushConfigInterface.getRegisterPort())).toString();
		
		MULTICAST_REQUEST = new StringWriter()
		.append(String.valueOf(PushRequest.MAGIC))
		.append('\1')
		.append(String.valueOf(MULTICAST_REQUEST_DATA.length()))
		.append('\1')
		.append(MULTICAST_REQUEST_DATA).toString();
		
		MULTICAST_REQUEST_BYTES = MULTICAST_REQUEST.getBytes();
		
		if (pushConfigInterface.isMultiCastEnabled()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Starting MulticastRequest");
			}
			
			multicastPort = pushConfigInterface.getMultiCastPort();
			
			multicastAddress = pushConfigInterface.getMultiCastAddress();
			
			remoteHostFresh = pushConfigInterface.getRemoteHostRefresh();
			
			final Timer t = ServerTimer.getTimer();
			t.schedule(this, new Date(), remoteHostFresh);
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("MulticastRequest is disabled");
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
			if (LOG.isDebugEnabled()) {
				LOG.debug("sending MulticastRequestPackage: " + new String(MULTICAST_REQUEST_BYTES));
			}
			final MulticastSocket multicastSocket = PushMulticastSocket.getPushMulticastSocket();
			final DatagramPacket datagramPacket = new DatagramPacket(MULTICAST_REQUEST_BYTES, MULTICAST_REQUEST_BYTES.length, multicastAddress, multicastPort);
			multicastSocket.send(datagramPacket);
		} catch (Exception exc) {
			LOG.error(exc.getMessage(), exc);
		}
	}
}
