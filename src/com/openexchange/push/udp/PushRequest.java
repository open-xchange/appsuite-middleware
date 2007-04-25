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
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PushRequest
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushRequest {
	
	public static final int MAGIC = 1337;
	
	public static final int REGISTER = 1;
	
	public static final int REGISTER_SYNC = 2;
	
	public static final int PUSH_SYNC = 3;
	
	public static final int REMOTE_HOST_REGISTER = 4;
	
	private int currentLength;
	
	private static final Log LOG = LogFactory.getLog(PushRequest.class);
	
	public PushRequest() {
		
	} 
	
	public void init(final DatagramPacket datagramPacket) {
		try {
			final byte[] b = new byte[datagramPacket.getLength()];
			System.arraycopy(datagramPacket.getData(), 0, b, 0, b.length);
			String data = new String(b);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("push request data: " + data);
			}
			
			String s[] = data.split("\1");
			
			int pos = 0;
			final int magic = parseInt(s, pos++);
			
			if (magic != MAGIC) {
				throw new Exception("missing magic int!");
			}
			
			final int length = parseInt(s, pos++);
			
			final byte[] bData = new byte[length];
			System.arraycopy(b, currentLength, bData, 0, length);	
			
			data = new String(bData);
			s = data.split("\1");
			pos = 0;
			
			final int type = parseInt(s, pos++);
			
			int userId = 0;
			InetAddress hostAddress = null;
			int port = 0;
			int folderId = 0;
			int module = 0;
			int contextId = 0;
			
			RegisterObject registerObj = null;
			
			switch (type) {
				case REGISTER:
					userId = parseInt(s, pos++);
					contextId = parseInt(s, pos++);
					
					hostAddress = datagramPacket.getAddress();
					port = datagramPacket.getPort();
					
					registerObj = new RegisterObject(userId, contextId, hostAddress.getHostAddress(), port, false);
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("register package: user id=" + userId + ",host address=" + hostAddress+ ",port=" + port);
					}
					
					RegisterHandler.addRegisterObject(registerObj);
					PushOutputQueue.add(registerObj, true);
					break;
				case REGISTER_SYNC:
					userId = parseInt(s, pos++);
					contextId = parseInt(s, pos++);
					hostAddress = InetAddress.getByName(parseString(s, pos++));
					port = parseInt(s, pos++);
					
					registerObj = new RegisterObject(userId, contextId, hostAddress.getHostAddress(), port, true);
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("register sync package: " + registerObj);
					}
					
					RegisterHandler.addRegisterObject(registerObj);
					break;
				case PUSH_SYNC:
					folderId = parseInt(s, pos++);
					module = parseInt(s, pos++);
					contextId = parseInt(s, pos++);
					final int[] users = convertString2IntArray(parseString(s, pos++));
					
					final PushObject pushObject = new PushObject(folderId, module, contextId, users, true);
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("push sync package: " + pushObject);
					}
					
					PushOutputQueue.add(pushObject);					
					break;
				case REMOTE_HOST_REGISTER:
					final RemoteHostObject remoteHostObject = new RemoteHostObject();
					
					hostAddress = InetAddress.getByName(parseString(s, pos++));
					port = parseInt(s, pos++);
					
					remoteHostObject.setHost(hostAddress);
					remoteHostObject.setPort(port);
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("remost host register request: " + remoteHostObject);
					}
					
					PushOutputQueue.addRemoteHostObject(remoteHostObject);
					break;
				default:
					throw new Exception("invalid type in push request: " + type);
			}
		} catch (Exception exc) {
			LOG.error("PushRequest: " + exc, exc);
		}
	}
	
	public int parseInt(final String[] s, final int pos) {
		return Integer.parseInt(parseString(s, pos));
	}
	
	public String parseString(final String[] s, final int pos) {
		currentLength += s[pos].length()+1;
		if (s[pos].length() == 0) {
			return null;
		}
		
		return s[pos];
	}
	
	public boolean parseBoolean(final String[] s, final int pos) {
		return Boolean.parseBoolean(parseString(s, pos));
	}
	
	public int[] convertString2IntArray(final String s) {
		final String tmp[] = s.split(",");
		int i[] = new int[tmp.length];
		for (int a = 0; a < i.length; a++) {
			i[a] = Integer.parseInt(tmp[a]);
		}
		
		return i;
	}
}
