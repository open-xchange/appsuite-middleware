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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;
import com.openexchange.tools.servlet.OXServletException;

/**
 * {@link HttpServletManagerInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HttpServletManagerInit implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HttpServletManagerInit.class);

	private static final HttpServletManagerInit instance = new HttpServletManagerInit();

	private final AtomicBoolean started = new AtomicBoolean();

	private final AtomicBoolean initialized = new AtomicBoolean();

	private final Lock initLock = new ReentrantLock();

	/**
	 * No instantiation
	 */
	private HttpServletManagerInit() {
		super();
	}

	public static HttpServletManagerInit getInstance() {
		return instance;
	}

	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error(this.getClass().getName() + " already started");
			return;
		}
		initServletMappings();
		started.set(true);
		if (LOG.isInfoEnabled()) {
			LOG.info("HTTP servlet manager successfully initialized");
		}
	}

	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error(this.getClass().getName() + " cannot be stopped since it has not been started before");
			return;
		}
		releaseServletMappings();
		started.set(false);
		if (LOG.isInfoEnabled()) {
			LOG.info("HTTP servlet manager successfully stopped");
		}
	}

	private void releaseServletMappings() {
		if (initialized.get()) {
			initLock.lock();
			try {
				if (!initialized.get()) {
					/*
					 * Ensure servlets are only initialized one time
					 */
					return;
				}
				HttpServletManager.releaseHttpServletManager();
				initialized.set(false);
			} finally {
				initLock.unlock();
			}

		}
	}

	private final static String STR_PROPERTIES = ".properties";

	private void initServletMappings() throws OXServletException {
		if (!initialized.get()) {
			initLock.lock();
			try {
				if (initialized.get()) {
					/*
					 * Ensure servlets are only initialized one time
					 */
					return;
				}
				final String servletMappingDir = SystemConfig.getProperty(SystemConfig.Property.ServletMappingDir);
				if (servletMappingDir == null) {
					throw new OXServletException(OXServletException.Code.MISSING_SERVLET_DIR,
							SystemConfig.Property.ServletMappingDir.getPropertyName());
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
				final Map<String, Constructor<?>> servletConstructorMap = new HashMap<String, Constructor<?>>();
				for (int i = 0; i < propFiles.length; i++) {
					/*
					 * Read properties from file
					 */
					final Properties properties = getPropertiesFromFile(propFiles[i]);
					/*
					 * Initialize servlets' default constructors
					 */
					final int size = properties.keySet().size();
					final Iterator<Object> iter = properties.keySet().iterator();
					for (int k = 0; k < size; k++) {
						addServletClass(iter.next().toString().trim(), properties, servletConstructorMap);
					}
				}
				HttpServletManager.initHttpServletManager(servletConstructorMap);
				initialized.set(true);
			} catch (final IOException exc) {
				throw new OXServletException(OXServletException.Code.SERVLET_MAPPINGS_NOT_LOADED, exc, exc
						.getLocalizedMessage());
			} finally {
				initLock.unlock();
			}
		}
	}

	private static Properties getPropertiesFromFile(final File f) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			final Properties properties = new Properties();
			properties.load(fis);
			return properties;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				fis = null;
			}
		}
	}

	private final static Class<?>[] CLASS_ARR = new Class[] {};

	private static void addServletClass(final String name, final Properties properties,
			final Map<String, Constructor<?>> servletConstructorMap) {
		String value = null;
		try {
			if (!checkServletPath(name)) {
				LOG.error(new StringBuilder("Invalid servlet path: ").append(name).toString());
				return;
			}
			Object tmp = properties.get(name);
			if (null == tmp || (value = tmp.toString().trim()).length() == 0) {
				if (LOG.isWarnEnabled()) {
					final OXServletException e = new OXServletException(OXServletException.Code.NO_CLASS_NAME_FOUND,
							name);
					LOG.warn(e.getLocalizedMessage(), e);
				}
				return;
			}
			tmp = null;
			if (servletConstructorMap.containsKey(name)) {
				final boolean isEqual = servletConstructorMap.get(name).toString().indexOf(value) != -1;
				if (!isEqual && LOG.isWarnEnabled()) {
					final OXServletException e = new OXServletException(OXServletException.Code.ALREADY_PRESENT, name,
							servletConstructorMap.get(name), value);
					LOG.warn(e.getLocalizedMessage(), e);
				}
			} else {
				servletConstructorMap.put(name, Class.forName(value).getConstructor(CLASS_ARR));
			}
		} catch (final SecurityException e) {
			if (LOG.isWarnEnabled()) {
				final OXServletException se = new OXServletException(OXServletException.Code.SECURITY_ERR, e, value);
				LOG.warn(se.getLocalizedMessage(), se);
			}
		} catch (final ClassNotFoundException e) {
			if (LOG.isWarnEnabled()) {
				final OXServletException se = new OXServletException(OXServletException.Code.CLASS_NOT_FOUND, e, value);
				LOG.warn(se.getLocalizedMessage(), se);
			}
		} catch (final NoSuchMethodException e) {
			if (LOG.isWarnEnabled()) {
				final OXServletException se = new OXServletException(OXServletException.Code.NO_DEFAULT_CONSTRUCTOR, e,
						value);
				LOG.warn(se.getLocalizedMessage(), se);
			}
		}
	}

	private static final Pattern PATTERN_SERVLET_PATH = Pattern.compile("([\\p{ASCII}&&[^\\p{Blank}]]+)\\*?");

	private static boolean checkServletPath(final String servletPath) {
		return PATTERN_SERVLET_PATH.matcher(servletPath).matches();
	}
}
