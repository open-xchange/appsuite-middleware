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

import static com.openexchange.tools.LocaleTools.toLowerCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.FIFOQueue;
import com.openexchange.tools.ajp13.AJPv13Config;
import com.openexchange.tools.ajp13.AJPv13Server;
import com.openexchange.tools.servlet.OXServletException;

/**
 * HttpServletManager
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletManager {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HttpServletManager.class);

	private static final Map<String, FIFOQueue<HttpServlet>> SERVLET_POOL = new HashMap<String, FIFOQueue<HttpServlet>>();

	private static Map<String, Constructor> servletConstructorMap;

	private static boolean initialized;

	private static final Lock INIT_LOCK = new ReentrantLock();

	private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

	private static final Lock READ_LOCK = RW_LOCK.readLock();

	private static final Lock WRITE_LOCK = RW_LOCK.writeLock();

	private HttpServletManager() {
		super();
	}

	public static HttpServlet getServlet(final String id) {
		READ_LOCK.lock();
		try {
			if (SERVLET_POOL.containsKey(id)) {
				final FIFOQueue<HttpServlet> servletQueue = SERVLET_POOL.get(id);
				if (servletQueue.isEmpty()) {
					/*
					 * Empty queue: create & return a new servlet instance
					 */
					final HttpServlet servletInst = createServletInstance(id);
					if (servletInst == null) {
						return new HttpErrorServlet(new StringBuilder(100).append("Servlet ").append(id).append(
								" could NOT be created").toString());
					}
					return servletInst;
				}
				final HttpServlet servletInstance = servletQueue.get();
				if (servletInstance instanceof SingleThreadModel) {
					/*
					 * If servlet class implements SingleThreadModel the same
					 * instance MUST NOT be used concurrently by multiple
					 * threads. So remove from queue.
					 */
					servletQueue.dequeue();
				}
				return servletInstance;
			}
			return null;
		} finally {
			READ_LOCK.unlock();
		}
	}

	/**
	 * Returns a new instance of <code>javax.servlet.http.HttpServlet</code>
	 * constructed from given <code>servletKey</code> argument
	 * 
	 * @param servletKey
	 * @return
	 */
	private static HttpServlet createServletInstance(final String servletKey) {
		final Constructor servletConstructor = servletConstructorMap.get(servletKey);
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

	public static void putServlet(final String id, final HttpServlet servletObj) {
		WRITE_LOCK.lock();
		try {
			if (SERVLET_POOL.containsKey(id)) {
				if (servletObj instanceof SingleThreadModel) {
					SERVLET_POOL.get(id).enqueue(servletObj);
				}
			} else {
				final FIFOQueue<HttpServlet> servlets = new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
				servlets.enqueue(servletObj);
				SERVLET_POOL.put(id, servlets);
			}
		} finally {
			WRITE_LOCK.unlock();
		}
	}

	public static final void destroyServlet(final String id, final HttpServlet servletObj) {
		WRITE_LOCK.lock();
		try {
			if (servletObj instanceof SingleThreadModel) {
				/*
				 * Single-thread are used per instance, so theres no reference
				 * used by HttpServletManager, cause any reference is completely
				 * removed on invocations of getServlet()
				 */
				return;
			}
			if (SERVLET_POOL.containsKey(id)) {
				SERVLET_POOL.remove(id);
			}
		} finally {
			WRITE_LOCK.unlock();
		}
	}

	public static Iterator<String> getServletKeysIterator() {
		return servletConstructorMap.keySet().iterator();
	}

	public static int getNumberOfServletKeys() {
		return servletConstructorMap.size();
	}

	public static void clearServletPool() {
		WRITE_LOCK.lock();
		try {
			SERVLET_POOL.clear();
		} finally {
			WRITE_LOCK.unlock();
		}
	}

	public final static void loadServletMapping() throws AbstractOXException {
		final String servletMappingDir = SystemConfig.getProperty(SystemConfig.Property.ServletMappingDir);
		if (servletMappingDir == null) {
			throw new OXServletException(OXServletException.Code.MISSING_SERVLET_DIR,
					SystemConfig.Property.ServletMappingDir.getPropertyName());
		}
		loadServletMapping(servletMappingDir.trim());
	}

	private final static String STR_PROPERTIES = ".properties";

	private final static Class[] CLASS_ARR = new Class[] {};

	public final static void loadServletMapping(final String servletMappingDir) throws AbstractOXException {
		if (!initialized) {
			INIT_LOCK.lock();
			try {
				if (servletConstructorMap != null) {
					/*
					 * Ensure servlets are only initialized one time
					 */
					return;
				}
				final File dir = new File(servletMappingDir);
				if (!dir.exists()) {
					throw new OXServletException(OXServletException.Code.DIR_NOT_EXISTS, servletMappingDir);
				} else if (!dir.isDirectory()) {
					throw new OXServletException(OXServletException.Code.NO_DIRECTORY, servletMappingDir);
				}
				final File[] propFiles = dir.listFiles(new FilenameFilter() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.io.FilenameFilter#accept(java.io.File,
					 *      java.lang.String)
					 */
					public boolean accept(final File dir, final String name) {
						return toLowerCase(name).endsWith(STR_PROPERTIES);

					}
				});
				servletConstructorMap = new HashMap<String, Constructor>();
				for (int i = 0; i < propFiles.length; i++) {
					final File f = propFiles[i];

					/*
					 * Read properties from file
					 */
					Properties properties = null;
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(f);
						properties = new Properties();
						properties.load(fis);
					} finally {
						if (fis != null) {
							fis.close();
							fis = null;
						}
					}
					/*
					 * Initialize servlets' default constructor
					 */
					final int size = properties.keySet().size();
					final Iterator<Object> iter = properties.keySet().iterator();
					NextMapping: for (int k = 0; k < size; k++) {
						String value = null;
						try {
							final String name = iter.next().toString();
							if (!checkServletPath(name)) {
								LOG.error(new StringBuilder("Invalid servlet path: ").append(name).toString());
								continue NextMapping;
							}
							Object tmp = properties.get(name);
							if (null == tmp || (value = tmp.toString().trim()).length() == 0) {
								if (LOG.isWarnEnabled()) {
									final OXServletException e = new OXServletException(
											OXServletException.Code.NO_CLASS_NAME_FOUND, name);
									LOG.warn(e.getLocalizedMessage(), e);
								}
								continue NextMapping;
							}
							tmp = null;
							if (servletConstructorMap.containsKey(name)) {
								final boolean isEqual = servletConstructorMap.get(name).toString().indexOf(value) != -1;
								if (!isEqual && LOG.isWarnEnabled()) {
									final OXServletException e = new OXServletException(
											OXServletException.Code.ALREADY_PRESENT, name, servletConstructorMap
													.get(name), value);
									LOG.warn(e.getLocalizedMessage(), e);
								}
							} else {
								servletConstructorMap.put(name, Class.forName(value).getConstructor(CLASS_ARR));
							}
						} catch (final SecurityException e) {
							if (LOG.isWarnEnabled()) {
								final OXServletException se = new OXServletException(
										OXServletException.Code.SECURITY_ERR, e, value);
								LOG.warn(se.getLocalizedMessage(), se);
							}
						} catch (final ClassNotFoundException e) {
							if (LOG.isWarnEnabled()) {
								final OXServletException se = new OXServletException(
										OXServletException.Code.CLASS_NOT_FOUND, e, value);
								LOG.warn(se.getLocalizedMessage(), se);
							}
						} catch (final NoSuchMethodException e) {
							if (LOG.isWarnEnabled()) {
								final OXServletException se = new OXServletException(
										OXServletException.Code.NO_DEFAULT_CONSTRUCTOR, e, value);
								LOG.warn(se.getLocalizedMessage(), se);
							}
						}
					}
				}
				initialized = true;
			} catch (final IOException exc) {
				throw new OXServletException(OXServletException.Code.SERVLET_MAPPINGS_NOT_LOADED, exc, exc
						.getLocalizedMessage());
			} finally {
				INIT_LOCK.unlock();
			}
		}
	}
	
	private static final Pattern PATTERN_SERVLET_PATH = Pattern.compile("([\\p{ASCII}&&[^\\s]]+)\\*?");
	
	private static boolean checkServletPath(final String servletPath) {
		return PATTERN_SERVLET_PATH.matcher(servletPath).matches();
	}

	private static final Object[] INIT_ARGS = new Object[] {};

	public static void createServlets() {
		WRITE_LOCK.lock();
		try {
			for (final Iterator<Map.Entry<String, Constructor>> iter = servletConstructorMap.entrySet().iterator(); iter
					.hasNext();) {
				final Map.Entry<String, Constructor> entry = iter.next();
				final String servletKey = entry.getKey();
				FIFOQueue<HttpServlet> servletQueue = null;
				/*
				 * Create a pool of servlet instances
				 */
				final Constructor servletConstructor = entry.getValue();
				if (servletConstructor == null) {
					servletQueue = new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
					servletQueue.enqueue(new HttpErrorServlet("No Servlet Constructor found for " + servletKey));
				} else {
					try {
						HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
						final boolean isSTM = servletInstance instanceof SingleThreadModel;
						servletQueue = isSTM ? new FIFOQueue<HttpServlet>(HttpServlet.class, AJPv13Config
								.getServletPoolSize()) : new FIFOQueue<HttpServlet>(HttpServlet.class, 1);
						final ServletConfig conf = AJPv13Server.SERVLET_CONFIGS.getConfig(servletInstance.getClass()
								.getCanonicalName(), servletKey);
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
				SERVLET_POOL.put(servletKey, servletQueue);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All Servlet Instances created & initialized");
			}
		} finally {
			WRITE_LOCK.unlock();
		}
	}
}
