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

package com.openexchange.tools.servlet.http;

import java.lang.reflect.Constructor;

import javax.servlet.http.HttpServlet;

import com.openexchange.tools.FIFOQueue;
import com.openexchange.tools.servlet.ServletConfigLoader;

/**
 * {@link ServletQueue} - The servlet queue backed by a {@link FIFOQueue fi-fo
 * queue} and capable to create new servlet instances on demand.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ServletQueue extends FIFOQueue<HttpServlet> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ServletQueue.class);

	private static final Object[] INIT_ARGS = new Object[] {};

	private final Constructor<?> servletConstructor;

	/**
	 * Initializes a new concurrent {@link ServletQueue}
	 * 
	 * @param maxsize
	 *            The max. size
	 * @param servletConstructor
	 *            The servlet constructor to create new servlet instances on
	 *            demand
	 */
	public ServletQueue(final int maxsize, final Constructor<?> servletConstructor) {
		this(maxsize, true, servletConstructor);
	}

	/**
	 * Initializes a new {@link ServletQueue}
	 * 
	 * @param maxsize
	 *            The max. size
	 * @param isSynchronized
	 *            <code>true</code> for a concurrent queue; otherwise
	 *            <code>false</code>
	 * @param servletConstructor
	 *            The servlet constructor to create new servlet instances on
	 *            demand
	 */
	public ServletQueue(final int maxsize, final boolean isSynchronized, final Constructor<?> servletConstructor) {
		super(maxsize, isSynchronized);
		this.servletConstructor = servletConstructor;
	}

	/**
	 * Creates a new instance of <code>javax.servlet.http.HttpServlet</code>
	 * initialized with servlet config obtained by given <code>servletKey</code>
	 * argument.
	 * 
	 * @param servletKey
	 *            The servlet key
	 * @return A new instance of <code>javax.servlet.http.HttpServlet</code>
	 *         initialized with servlet config obtained by given
	 *         <code>servletKey</code> argument; or <code>null</code> if no
	 *         servlet is bound to given servlet key or initialization fails
	 */
	public HttpServlet createServletInstance(final String servletKey) {
		if (servletConstructor == null) {
			return null;
		}
		try {
			final HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
			servletInstance.init(ServletConfigLoader.getDefaultInstance().getConfig(
					servletInstance.getClass().getCanonicalName(), servletKey));
			return servletInstance;
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
		}
		return null;
	}
}
