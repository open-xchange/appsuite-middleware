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

package com.openexchange.server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ServiceProxy}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class ServiceProxy<S> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ServiceProxy.class);

	private final AtomicInteger countActive;

	private final AtomicBoolean waiting;

	private S service;

	/**
	 * 
	 */
	public ServiceProxy() {
		super();
		countActive = new AtomicInteger();
		waiting = new AtomicBoolean();
	}

	/**
	 * Sets the service of this service proxy
	 * 
	 * @param service
	 *            The service
	 */
	public void setService(final S service) {
		if (null == this.service) {
			this.service = service;
		}
	}

	/**
	 * Removes the service from this service proxy
	 */
	public void removeService() {
		if (null == service) {
			return;
		}
		if (countActive.get() > 0) {
			waiting.set(true);
			synchronized (countActive) {
				try {
					while (countActive.get() > 0) {
						countActive.wait();
					}
				} catch (final InterruptedException e) {
					LOG.error(e.getLocalizedMessage(), e);
				} finally {
					waiting.set(false);
				}
			}
		}
		this.service = null;
	}

	/**
	 * Gets the service or <code>null</code> if service is not active, yet<br>
	 * <b>Note:</b> Don't forget to unget the service via
	 * {@link #ungetService()}
	 * 
	 * <pre>
	 * 
	 * ...
	 * final MyService myService = MyService.getInstance();
	 * final Service s = myService.getService();
	 * try {
	 *     // Do something...
	 * } finally {
	 *     myService.ungetService();
	 * }
	 * ...
	 * 
	 * </pre>
	 * 
	 * @return The bundle service instance
	 */
	public S getService() {
		if (null == service) {
			return null;
		}
		countActive.incrementAndGet();
		return service;
	}

	/**
	 * Ungets the bundle service instance
	 */
	public void ungetService() {
		if (countActive.get() == 0) {
			return;
		}
		countActive.decrementAndGet();
		if (waiting.get()) {
			synchronized (countActive) {
				if (waiting.get()) {
					countActive.notifyAll();
				}
			}
		}
	}

}
