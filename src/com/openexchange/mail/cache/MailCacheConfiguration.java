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

package com.openexchange.mail.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.jcs.engine.control.CompositeCacheManager;

import com.openexchange.configuration.SystemConfig;

/**
 * {@link MailCacheConfiguration} - Loads the configuration for mail caches
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailCacheConfiguration {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailCacheConfiguration.class);

	/**
	 * Remembers if the configuration has been loaded.
	 */
	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static CompositeCacheManager ccmInstance;

	private static final Lock LOCK = new ReentrantLock();

	/**
	 * No instantiation
	 */
	private MailCacheConfiguration() {
		super();
	}

	/**
	 * Loads the configuration for the caching system.
	 * 
	 * @throws IOException
	 *             if the configuration can't be loaded.
	 */
	public static void load() throws IOException {
		if (!initialized.get()) {
			LOCK.lock();
			try {
				if (ccmInstance == null) {
					configure();
					initialized.set(true);
				}
			} finally {
				LOCK.unlock();
			}
		}
	}

	private static void configure() throws IOException {
		FileInputStream fis = null;
		try {
			final String cacheConfigFile = SystemConfig.getProperty(SystemConfig.Property.MailCacheConfig);
			if (cacheConfigFile == null) {
				LOG.error(new StringBuilder(256).append("Missing property \"").append(
						SystemConfig.Property.MailCacheConfig.getPropertyName()).append("\" in system.properties")
						.toString());
				return;
			}
			ccmInstance = CompositeCacheManager.getUnconfiguredInstance();
			final Properties props = new Properties();
			try {
				props.load((fis = new FileInputStream(cacheConfigFile)));
			} catch (FileNotFoundException fnfe) {
				LOG.error(new StringBuilder(256).append("Missing cache configuration file \"").append(cacheConfigFile)
						.append('"').toString(), fnfe);
			}
			ccmInstance.configure(props);
		} finally {
			if (fis != null) {
				fis.close();
				fis = null;
			}
		}
	}
}
