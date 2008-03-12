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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A request handler pool to hold pre-initialized instances of
 * <code>AJPv13RequestHandler</code>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13RequestHandlerPool {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13RequestHandlerPool.class);

	private static BlockingQueue<AJPv13RequestHandler> REQUEST_HANDLER_POOL;

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private AJPv13RequestHandlerPool() {
		super();
	}

	/**
	 * Checks if AJP request handler pool is initialized.
	 * 
	 * @return <code>true</code> if initialized; otherwise <code>false</code>
	 */
	public static boolean isInitialized() {
		return initialized.get();
	}

	/**
	 * Initializes the AJP request handler pool
	 */
	public static void initPool() {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (null == REQUEST_HANDLER_POOL) {
					final int poolSize = AJPv13Config.getAJPRequestHandlerPoolSize();
					REQUEST_HANDLER_POOL = new ArrayBlockingQueue<AJPv13RequestHandler>(poolSize);
					for (int i = 0; i < poolSize; i++) {
						REQUEST_HANDLER_POOL.add(new AJPv13RequestHandler());
					}
					initialized.set(true);
					LOG.info("AJPv13-RequestHandler-Pool initialized with " + poolSize);
				}
			}
		}
	}

	/**
	 * Resets the AJP request handler pool
	 */
	public static void resetPool() {
		if (initialized.get()) {
			synchronized (initialized) {
				if (null != REQUEST_HANDLER_POOL) {
					REQUEST_HANDLER_POOL.clear();
					REQUEST_HANDLER_POOL = null;
					initialized.set(false);
				}
			}
		}
	}

	/**
	 * Fetches an existing instance from pool or creates & returns a new one.
	 * The given connection is then assigned to the request handler instance.
	 * 
	 * @param ajpCon
	 *            The AJP connection which is assigned to returned AJP request
	 *            handler
	 * @return A pooled or newly created AJP request handler
	 */
	public static AJPv13RequestHandler getRequestHandler(final AJPv13Connection ajpCon) {
		AJPv13RequestHandler reqHandler = REQUEST_HANDLER_POOL.poll();
		if (reqHandler == null) {
			reqHandler = new AJPv13RequestHandler();
		}
		reqHandler.setAJPConnection(ajpCon);
		return reqHandler;
	}

	/**
	 * Puts back the given request handler instance into pool if space
	 * available. Otherwise it's going to be discarded.
	 * 
	 * @param reqHandler
	 *            The AJP request handler which shall be put back into pool
	 * @return <code>true</code> if AJP request handler was successfully put
	 *         back into pool; otherwise <code>false</code>
	 */
	public static boolean putRequestHandler(final AJPv13RequestHandler reqHandler) {
		if (reqHandler == null) {
			return false;
		}
		return REQUEST_HANDLER_POOL.offer(reqHandler);
	}

}
