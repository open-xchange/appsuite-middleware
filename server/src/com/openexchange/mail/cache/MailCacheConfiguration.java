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

import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailCacheConfiguration} - Loads the configuration for mail caches
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailCacheConfiguration implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailCacheConfiguration.class);

	private static final MailCacheConfiguration instance = new MailCacheConfiguration();

	/**
	 * No instantiation
	 */
	private MailCacheConfiguration() {
		super();
	}

	/**
	 * Initializes the singleton instance of {@link MailCacheConfiguration}
	 * 
	 * @return The singleton instance of {@link MailCacheConfiguration}
	 */
	public static MailCacheConfiguration getInstance() {
		return instance;
	}

	private void configure() throws ConfigurationException {
		final String cacheConfigFile = SystemConfig.getProperty(SystemConfig.Property.MailCacheConfig);
		if (cacheConfigFile == null) {
			throw new ConfigurationException(ConfigurationException.Code.PROPERTY_MISSING,
					SystemConfig.Property.MailCacheConfig.getPropertyName());
		}
		final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
		try {
			cacheService.loadConfiguration(cacheConfigFile.trim());
		} catch (final CacheException e) {
			throw new ConfigurationException(e);
		}
	}

	/**
	 * Delegates to {@link CacheService#freeCache(String)}
	 * 
	 * @param cacheName
	 *            The name of the cache region that ought to be freed
	 */
	public void freeCache(final String cacheName) {
		try {
			ServerServiceRegistry.getInstance().getService(CacheService.class).freeCache(cacheName);
		} catch (final CacheException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * @see com.openexchange.server.Initialization#start()
	 */
	public void start() throws AbstractOXException {
		configure();
	}

	/*
	 * @see com.openexchange.server.Initialization#stop()
	 */
	public void stop() throws AbstractOXException {
		final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
		cacheService.freeCache(MailConnectionCache.REGION_NAME);
		cacheService.freeCache(MailMessageCache.REGION_NAME);
	}
}
