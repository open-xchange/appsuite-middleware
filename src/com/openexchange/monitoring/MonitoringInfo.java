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



package com.openexchange.monitoring;

import com.openexchange.database.Database;
import com.openexchange.tools.ajp13.AJPv13Listener;
import com.openexchange.tools.ajp13.AJPv13Server;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MonitoringInfo {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MonitoringInfo.class);
	
//	 Constants for connection types
	public static final int AJP_SOCKET = 0;
	
	public static final int AJAX = 1;
	
	public static final int OUTLOOK = 2;
	
	public static final int WEBDAV_STORAGE = 3;
	
	public static final int WEBDAV_USER = 4;
	
	public static final int SYNCML = 5;
	
	public static final int IMAP = 6;
	
	private static final int SESSION = 7;
	
	// Static fields
	private static int numberOfOpenAJPSockets;
	
	private static int numberOfAJAXConnections;
	
	private static int numberOfWebDAVUserConnections;
	
	private static int numberOfOutlookConnections;
	
	private static int numberOfSyncMLConnections;
	
	private static int numberOfIMAPConnections;
	
	private static int numberOfActiveSessions;
	
	private static int[] numberOfSessionsInContainer;
	
	public static int getNumberOfActiveSessions() {
		return getNumberOfConnections(SESSION);
	}
	
	public static void incrementNumberOfActiveSessions() {
		changeNumberOfConnections(SESSION, true);
	}
	
	public static void decrementNumberOfActiveSessions() {
		changeNumberOfConnections(SESSION, false);
	}
	
	public static void decrementNumberOfActiveSessions(final int number) {
		for (int a = 0; a < number; a++) {
			decrementNumberOfActiveSessions();
		}
	}
	
	public static int getNumberOfAJAXConnections() {
		return getNumberOfConnections(AJAX);
	}
	
	public static int getNumberOfRunningAJPListeners() {
		return AJPv13Listener.getNumberOfRunningAJPListeners();
	}
	
	public static int getNumberOfDBConnections() {
        return Database.getNumConnections();
	}
	
	public static int getNumberOfOpenSockets() {
		return AJPv13Server.getNumberOfOpenAJPSockets();
	}

	public static void incrementNumberOfConnections(final int connectionType) {
		changeNumberOfConnections(connectionType, true);
	}
	
	public static void decrementNumberOfConnections(final int connectionType) {
		changeNumberOfConnections(connectionType, false);
	}
	
	public int getNumberOfConnectionsPerSecond(final int connectionType) throws InterruptedException {
		final int firstVal = getNumberOfConnections(connectionType);
		Thread.sleep(1000);
		return getNumberOfConnections(connectionType) - firstVal;
	}
	
	public static int getNumberOfConnections(final int connectionType) {
		int retval = -1;
		switch (connectionType) {
		case AJP_SOCKET:
			retval = numberOfOpenAJPSockets;
			break;
		case AJAX:
			retval = numberOfAJAXConnections;
			break;
		case WEBDAV_USER:
			retval = numberOfWebDAVUserConnections;
			break;
		case OUTLOOK:
			retval = numberOfOutlookConnections;
			break;
		case SYNCML:
			retval = numberOfSyncMLConnections;
			break;
		case IMAP:
			retval = numberOfIMAPConnections;
			break;
		case SESSION:
			retval = numberOfActiveSessions;
			break;
		default:
			LOG.error(new StringBuilder("MonitoringInfo.getNumberOfConnections(): Unknown connection type: ").append(
					connectionType).toString());
		}
		return retval;
	}
	
	private static void changeNumberOfConnections(final int connectionType, final boolean increment) {
		synchronized (MonitoringInfo.class) {
			final int i = increment ? 1 : -1;
			switch (connectionType) {
			case AJP_SOCKET:
				numberOfOpenAJPSockets += i;
				break;
			case AJAX:
				numberOfAJAXConnections += i;
				break;
			case WEBDAV_USER:
				numberOfWebDAVUserConnections += i;
				break;
			case OUTLOOK:
				numberOfOutlookConnections += i;
				break;
			case SYNCML:
				numberOfSyncMLConnections += i;
				break;
			case IMAP:
				numberOfIMAPConnections += i;
				break;
			case SESSION:
				numberOfActiveSessions += i;
				break;
			default:
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder("MonitoringInfo.changeNumberOfConnections(): Unknown connection type: ")
							.append(connectionType).toString());
				}
			}
		}
	}
	
	private static final String TYPE_AJAX = "ajax";
	
	private static final String TYPE_WEBDAV_USER = "servlet/webdav.documents";
	
	private static final String TYPE_OUTLOOK = "servlet/webdav.";
	
	private static final String TYPE_SYNCML = "ajax/sync";
	
	private static final String TYPE_OXADMIN_UMIN = "oxadmin/umin";
	
	private static final String TYPE_ADMIN_UMIN = "admin/umin";
	
	public static final int getConnectionType(final String pathInfo) {
		if (pathInfo.startsWith(TYPE_AJAX)) {
			return MonitoringInfo.AJAX;
		} else if (pathInfo.startsWith(TYPE_WEBDAV_USER)) {
			return MonitoringInfo.WEBDAV_USER;
		} else if (pathInfo.startsWith(TYPE_OUTLOOK)) {
			return MonitoringInfo.OUTLOOK;
		} else if (pathInfo.startsWith(TYPE_SYNCML)) {
			return MonitoringInfo.SYNCML;
		} else if (pathInfo.startsWith(TYPE_OXADMIN_UMIN)) {
			return MonitoringInfo.AJAX;
		} else if (pathInfo.startsWith(TYPE_ADMIN_UMIN)) {
			return MonitoringInfo.AJAX;
		} else {
			return -1;
		}
	}
	
	public static void setNumberOfSessionsInContainer(final int[] numberOfSessionsInContainer) {
		MonitoringInfo.numberOfSessionsInContainer = new int[numberOfSessionsInContainer.length];
		System.arraycopy(numberOfSessionsInContainer, 0, MonitoringInfo.numberOfSessionsInContainer, 0,
				numberOfSessionsInContainer.length);
	}
	
	public static int[] getNumberOfSessionsInContainer() {
		final int[] retval = new int[numberOfSessionsInContainer.length];
		System.arraycopy(numberOfSessionsInContainer, 0, retval, 0, retval.length);
		return retval;
	}
}
