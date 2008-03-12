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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;

import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.tools.FIFOQueue;
import com.openexchange.tools.NonBlockingRWLock;

/**
 * {@link HttpServletManager} - The HTTP servlet manager
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletManager {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HttpServletManager.class);

	private static final Map<String, FIFOQueue<HttpServlet>> SERVLET_POOL = new HashMap<String, FIFOQueue<HttpServlet>>();

	private static Map<String, Constructor<?>> servletConstructorMap;

	private static final NonBlockingRWLock RW_LOCK = new NonBlockingRWLock();

	private HttpServletManager() {
		super();
	}

	/**
	 * Determines the instance of {@link HttpServlet} that corresponds to given
	 * path; e.g. <code>/servlet/path</code>
	 * 
	 * @param path
	 *            The servlet path to lookup
	 * @param pathStorage
	 *            A container to keep the actual servlet path contained in
	 *            servlet mapping for later servlet release
	 * @return The instance of {@link HttpServlet}
	 */
	public static HttpServlet getServlet(final String path, final StringBuilder pathStorage) {
		int state;
		HttpServlet retval = null;
		do {
			state = RW_LOCK.acquireRead();
			if (SERVLET_POOL.containsKey(path)) {
				pathStorage.append(path);
				retval = getServletInternal(path);
			} else {
				/*
				 * Try through resolving
				 */
				final int size = SERVLET_POOL.size();
				final Iterator<String> iter = SERVLET_POOL.keySet().iterator();
				for (int i = 0; i < size; i++) {
					final String currentPath = iter.next();
					if (Pattern.compile(currentPath.replaceFirst("\\*", ".*"), Pattern.CASE_INSENSITIVE).matcher(path)
							.matches()) {
						pathStorage.append(currentPath);
						retval = getServletInternal(currentPath);
					}
				}
			}
		} while (!RW_LOCK.releaseRead(state));
		return retval;
	}

	/**
	 * Gets the servlet instance bound to given path
	 * 
	 * @param path
	 *            The servlet path
	 * @return The servlet instance dequeued from pool or newly created
	 */
	private static final HttpServlet getServletInternal(final String path) {
		final FIFOQueue<HttpServlet> servletQueue = SERVLET_POOL.get(path);
		if (servletQueue.isEmpty()) {
			/*
			 * Empty queue: create & return a new servlet instance
			 */
			final HttpServlet servletInst = createServletInstance(path);
			if (servletInst == null) {
				return new HttpErrorServlet(new StringBuilder(100).append("Servlet ").append(path).append(
						" could NOT be created").toString());
			}
			return servletInst;
		}
		final HttpServlet servletInstance = servletQueue.get();
		if (servletInstance instanceof SingleThreadModel) {
			/*
			 * If servlet class implements SingleThreadModel the same instance
			 * MUST NOT be used concurrently by multiple threads. So remove from
			 * queue.
			 */
			servletQueue.dequeue();
		}
		return servletInstance;
	}

	/**
	 * Returns a new instance of <code>javax.servlet.http.HttpServlet</code>
	 * constructed from given <code>servletKey</code> argument
	 * 
	 * @param servletKey
	 * @return
	 */
	private static final HttpServlet createServletInstance(final String servletKey) {
		final Constructor<?> servletConstructor = servletConstructorMap.get(servletKey);
		if (servletConstructor == null) {
			return null;
		}
		try {
			final HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(new Object[] {});
			servletInstance.init(AJPv13Server.SERVLET_CONFIGS.getConfig(servletInstance.getClass().getCanonicalName(),
					servletKey));
			return servletInstance;
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
		}
		return null;
	}

	/**
	 * Puts a servlet bound to given ID into this servlet manager's pool
	 * 
	 * @param path
	 *            The servlet's path
	 * @param servletObj
	 *            The servlet instance
	 */
	public static final void putServlet(final String path, final HttpServlet servletObj) {
		if (SERVLET_POOL.containsKey(path) && !(servletObj instanceof SingleThreadModel)) {
			return;
		}
		RW_LOCK.acquireWrite();
		try {
			if (SERVLET_POOL.containsKey(path)) {
				/*
				 * Since heading condition failed the servlet must be an
				 * instance of SingleThreadModel
				 */
				SERVLET_POOL.get(path).enqueue(servletObj);
			} else {
				final FIFOQueue<HttpServlet> servlets = new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
				final ServletConfig conf = AJPv13Server.SERVLET_CONFIGS.getConfig(servletObj.getClass()
						.getCanonicalName(), path);
				try {
					servletObj.init(conf);
				} catch (final ServletException e) {
					LOG.error("Servlet cannot be put into pool", e);
				}
				servlets.enqueue(servletObj);
				SERVLET_POOL.put(path, servlets);
			}
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	/**
	 * Registers a servlet if not already contained
	 * 
	 * @param id
	 *            The servlet's ID or alias (e.g. <code>my/servlet</code>).
	 *            Servlet's path without leading '/' character
	 * @param servlet
	 *            The servlet instance
	 * @param initParams
	 *            The servlet's init parameters
	 * @throws ServletException
	 *             If servlet's initialization fails or another servlet has
	 *             already been registered with the same alias
	 */
	public static final void registerServlet(final String id, final HttpServlet servlet,
			final Dictionary<String, String> initParams) throws ServletException {
		RW_LOCK.acquireWrite();
		try {
			final String path = new URI(id.charAt(0) == '/' ? id.substring(1) : id).normalize().toString();
			if (SERVLET_POOL.containsKey(path)) {
				throw new ServletException(new StringBuilder(256).append("A servlet with alias \"").append(id).append(
						"\" has already been registered before.").toString());
			}
			if (null != initParams && !initParams.isEmpty()) {
				AJPv13Server.SERVLET_CONFIGS.setConfig(servlet.getClass().getCanonicalName(), initParams);
			}
			final FIFOQueue<HttpServlet> servletQueue = new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
			final ServletConfig conf = AJPv13Server.SERVLET_CONFIGS.getConfig(servlet.getClass().getCanonicalName(),
					path);
			servlet.init(conf);
			servletQueue.enqueue(servlet);
			/*
			 * Put into servlet pool for being accessible
			 */
			SERVLET_POOL.put(path, servletQueue);
			/*
			 * Try to determine default constructor for later instantiations
			 */
			final Constructor<?>[] constructors = servlet.getClass().getConstructors();
			for (int i = 0; i < constructors.length; i++) {
				if (constructors[i].getParameterTypes().length == 0) {
					servletConstructorMap.put(path, constructors[i]);
				}
			}
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(64).append("New servlet \"").append(servlet.getClass().getCanonicalName())
						.append("\" successfully registered to \"").append(path).append('"'));
			}
		} catch (final URISyntaxException e) {
			throw new ServletException("Servlet path is not a valid URI", e);
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	/**
	 * Unregisters the servlet bound to given ID from mapping.
	 * 
	 * @param id
	 *            The servlet ID or alias
	 */
	public static final void unregisterServlet(final String id) {
		RW_LOCK.acquireWrite();
		try {
			AJPv13Server.SERVLET_CONFIGS.removeConfig(SERVLET_POOL.get(id).dequeue().getClass().getCanonicalName());
			SERVLET_POOL.remove(id);
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	/**
	 * Destroys the servlet that is bound to given ID.
	 * 
	 * @param id
	 *            The servlet ID
	 * @param servletObj
	 *            The servlet instance
	 */
	public static final void destroyServlet(final String id, final HttpServlet servletObj) {
		RW_LOCK.acquireWrite();
		try {
			if (servletObj instanceof SingleThreadModel) {
				/*
				 * Single-thread are used per instance, so theres no reference
				 * used by HttpServletManager, cause any reference is completely
				 * removed on invocations of getServlet()
				 */
				return;
			}
			SERVLET_POOL.remove(id);
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	private static final void clearServletPool() {
		RW_LOCK.acquireWrite();
		try {
			SERVLET_POOL.clear();
		} finally {
			RW_LOCK.releaseWrite();
		}
	}

	/**
	 * Initializes HTTP servlet manager with specified initial servlet
	 * constructor map
	 * 
	 * @param servletConstructorMap
	 *            The servlet constructor map
	 */
	final static void initHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap) {
		HttpServletManager.servletConstructorMap = servletConstructorMap;
		createServlets();
	}

	/**
	 * Releases the HTTP servlet manager
	 */
	final static void releaseHttpServletManager() {
		HttpServletManager.servletConstructorMap.clear();
		HttpServletManager.servletConstructorMap = null;
		clearServletPool();
	}

	private static final Object[] INIT_ARGS = new Object[] {};

	private static final void createServlets() {
		RW_LOCK.acquireWrite();
		try {
			for (final Iterator<Map.Entry<String, Constructor<?>>> iter = servletConstructorMap.entrySet().iterator(); iter
					.hasNext();) {
				final Map.Entry<String, Constructor<?>> entry = iter.next();
				String path;
				try {
					path = new URI(entry.getKey().charAt(0) == '/' ? entry.getKey().substring(1) : entry.getKey())
							.normalize().toString();
				} catch (final URISyntaxException e) {
					LOG.error("Invalid servlet path skipped: " + entry.getKey());
					continue;
				}
				FIFOQueue<HttpServlet> servletQueue = null;
				/*
				 * Create a pool of servlet instances
				 */
				final Constructor<?> servletConstructor = entry.getValue();
				if (servletConstructor == null) {
					servletQueue = new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
					servletQueue.enqueue(new HttpErrorServlet("No Servlet Constructor found for " + path));
				} else {
					try {
						HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
						final boolean isSTM = servletInstance instanceof SingleThreadModel;
						servletQueue = isSTM ? new FIFOQueue<HttpServlet>(HttpServlet.class, AJPv13Config
								.getServletPoolSize()) : new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
						final ServletConfig conf = AJPv13Server.SERVLET_CONFIGS.getConfig(servletInstance.getClass()
								.getCanonicalName(), path);
						servletInstance.init(conf);
						servletQueue.enqueue(servletInstance);
						if (isSTM) {
							/*
							 * Enqueue more than one instance if it implements
							 * SingleThreadModel
							 */
							final int remainingSize = AJPv13Config.getServletPoolSize() - 1;
							for (int i = 0; i < remainingSize; i++) {
								servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
								servletInstance.init(conf);
								servletQueue.enqueue(servletInstance);
							}
						}
					} catch (final Throwable t) {
						LOG.error(t.getMessage(), t);
					}
				}
				SERVLET_POOL.put(path, servletQueue);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All Servlet Instances created & initialized");
			}
		} finally {
			RW_LOCK.releaseWrite();
		}
	}
}
