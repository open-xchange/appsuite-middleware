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

package com.openexchange.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.control.CompositeCacheManager;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.impl.Initialization;

/**
 * This class implements the configuration of the caching system.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Configuration implements Initialization {

	private static final Log LOG = LogFactory.getLog(Configuration.class);

	/**
	 * Remembers if the configuration has been loaded.
	 */
	private static final AtomicBoolean loaded = new AtomicBoolean();

	private static final Lock LOCK = new ReentrantLock();

	private static final String CACHE_CONF_FILE = "CACHECCF";

	private static Configuration instance;

	/**
	 * The cache manager instance
	 */
	private CompositeCacheManager ccmInstance;

	private final AtomicBoolean started = new AtomicBoolean();

	/**
	 * Private constructor prevents instantiation.
	 * 
	 * @throws IOException
	 *             If cache manager configuration fails
	 */
	private Configuration() {
		super();
	}

	/**
	 * Initializes the singleton instance of {@link Configuration}
	 * 
	 * @return The singleton instance of {@link Configuration}
	 */
	public static Configuration getInstance() {
		if (!loaded.get()) {
			LOCK.lock();
			try {
				if (null == instance) {
					instance = new Configuration();
					loaded.set(true);
				}
			} finally {
				LOCK.unlock();
			}
		}
		return instance;
	}

	/**
	 * Loads the configuration for the caching system.
	 * 
	 * @throws ConfigurationException
	 *             if the configuration can't be loaded.
	 * @deprecated Use common {@link Initialization#start()}/{@link Initialization#stop()}
	 *             on singleton instead
	 */
	public static void load() throws ConfigurationException {
		getInstance().configure();
	}

	private void configure() throws ConfigurationException {
		if (null != ccmInstance) {
			/*
			 * Already invoked
			 */
			return;
		}
		FileInputStream fis = null;
		try {
			final String cacheConfigFile = SystemConfig.getProperty(CACHE_CONF_FILE);
			if (cacheConfigFile == null) {
				LOG.error(new StringBuilder().append("Missing property \"").append(CACHE_CONF_FILE).append(
						"\" in system.properties").toString());
				return;
			}
			ccmInstance = CompositeCacheManager.getUnconfiguredInstance();
			final Properties props = new Properties();
			try {
				props.load((fis = new FileInputStream(cacheConfigFile)));
			} catch (final FileNotFoundException e) {
				throw new ConfigurationException(ConfigurationException.Code.FILE_NOT_FOUND, e, cacheConfigFile);
			} catch (final IOException e) {
				throw new ConfigurationException(ConfigurationException.Code.IO_ERROR, e, e.getLocalizedMessage());
			}
			ccmInstance.configure(props);
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

	/**
	 * Delegates to {@link CompositeCacheManager#freeCache(String)}: The cache
	 * identified through given <code>cacheName</code> is removed from cache
	 * manager and all of its items are going to be disposed.
	 * 
	 * @param cacheName
	 *            The name of the cache region that ought to be freed
	 */
	public void freeCache(final String cacheName) {
		if (null == ccmInstance) {
			return;
		}
		ccmInstance.freeCache(cacheName);
	}

	/*
	 * @see com.openexchange.server.Initialization#start()
	 */
	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error("JCS caching engine has already been started", new Throwable());
		}
		configure();
		started.set(true);
	}

	/*
	 * @see com.openexchange.server.Initialization#stop()
	 */
	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error("JCS caching engine cannot be stopped since it has not been started before", new Throwable());
		}
		if (null == ccmInstance) {
			return;
		}
		ccmInstance.shutDown();
		ccmInstance = null;
		started.set(false);
	}
}
