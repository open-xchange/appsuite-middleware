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



package com.openexchange.ajp13;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * AJPv13ConnectionPool
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13ConnectionPool {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13ConnectionPool.class);

	private static final int CONNECTION_POOL_SIZE = AJPv13Config.getAJPConnectionPoolSize();

	private static final BlockingQueue<AJPv13Connection> CONNECTION_QUEUE = new ArrayBlockingQueue<AJPv13Connection>(
			CONNECTION_POOL_SIZE);
	
	private AJPv13ConnectionPool() {
		super();
	}

	/**
	 * Initializes the connection pool by creating the specified amount of
	 * connection instances
	 */
	public static void initConnectionPool() {
		for (int i = 0; i < CONNECTION_POOL_SIZE; i++) {
			CONNECTION_QUEUE.add(new AJPv13Connection());
		}
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder(50).append(CONNECTION_POOL_SIZE).append(
					" AJPv13Connection instances created in advance").toString());
		}
	}
	
	public static void resetConnectionPool() {
		CONNECTION_QUEUE.clear();
	}

	/**
	 * Fetches an existing connection from pool or creates & returns a new
	 * instance if none available in pool
	 */
	public static AJPv13Connection getAJPv13Connection(final AJPv13Listener l) {
		final AJPv13Connection ajpCon = CONNECTION_QUEUE.poll();
		if (ajpCon == null) {
			return new AJPv13Connection(l);
		}
		ajpCon.setListener(l);
		return ajpCon;
	}

	/**
	 * Puts given AJP Connection back in pool if there's enough space in queue
	 * otherwise the instance is going to be discarded
	 */
	public static boolean putBackAJPv13Connection(final AJPv13Connection ajpCon) {
		if (ajpCon == null) {
			return false;
		}
		return CONNECTION_QUEUE.offer(ajpCon);
	}

}
