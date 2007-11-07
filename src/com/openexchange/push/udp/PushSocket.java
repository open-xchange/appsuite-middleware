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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PushSocket
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class PushSocket implements Runnable {
	
	private Thread thread;

    private boolean running = true;
	
	private static DatagramSocket datagramSocket;
	
	private static final Log LOG = LogFactory.getLog(PushSocket.class);
	
	public PushSocket(final PushConfigInterface config) {
		final int serverRegisterPort = config.getRegisterPort();
		final InetAddress senderAddress = config.getSenderAddress();
		
		try {
			if (config.isPushEnabled()) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Starting Push Register Socket on Port: " + serverRegisterPort);
				}
			
				if (senderAddress != null) {
					datagramSocket = new DatagramSocket(serverRegisterPort, senderAddress);
				} else {
					datagramSocket = new DatagramSocket(serverRegisterPort);
				} 
			
				thread = new Thread(this);
				thread.setName(PushSocket.class.getName());
				thread.start();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Push Register Socket is disabled");
				}
			}
		} catch (Exception exc) {
			LOG.error("PushSocket", exc);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (running) {
			final DatagramPacket datagramPacket = new DatagramPacket(new byte[2048], 2048);
			try {
				datagramSocket.receive(datagramPacket);
				
				if (datagramPacket.getLength() > 0) {
					final PushRequest serverRegisterRequest = new PushRequest();
					serverRegisterRequest.init(datagramPacket);
				} else {
					LOG.warn("recieved empty udp package: " + datagramSocket);
				}
			} catch (SocketException e) {
			    if (running) {
			        LOG.error(e.getMessage(), e);
			    }
			} catch (IOException e) {
                LOG.error(e.getMessage(), e);
			}
		}
	}

	public void close() {
	    running  = false;
	    if (null != datagramSocket) {
	        datagramSocket.close();
	        datagramSocket = null;
	    }
	    if (null != thread) {
    	    try {
                thread.join();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            thread = null;
	    }
	}

	public static DatagramSocket getPushDatagramSocket() {
		return datagramSocket;
	}
}
