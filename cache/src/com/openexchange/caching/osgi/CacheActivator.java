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

package com.openexchange.caching.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.osgi.framework.ServiceRegistration;

import com.openexchange.caching.CacheInformationMBean;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.internal.JCSCacheInformation;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.management.ManagementException;
import com.openexchange.management.ManagementService;
import com.openexchange.server.osgiservice.DeferredActivator;

/**
 * {@link CacheActivator} - The {@link DeferredActivator} implementation for
 * cache bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CacheActivator extends DeferredActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CacheActivator.class);

	private final Dictionary<String, String> dictionary;

	private ServiceRegistration serviceRegistration;

	private ObjectName objectName;

	/**
	 * Initializes a new {@link CacheActivator}
	 */
	public CacheActivator() {
		super();
		dictionary = new Hashtable<String, String>();
		dictionary.put("name", "oxcache");
	}

	private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class, ManagementService.class };

	@Override
	protected Class<?>[] getNeededServices() {
		return NEEDED_SERVICES;
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		if (ManagementService.class.equals(clazz)) {
			try {
				unregisterCacheMBean();
			} catch (final ManagementException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
		/*
		 * TODO: Reconfigure with newly available configuration service?
		 */
		if (ManagementService.class.equals(clazz)) {
			try {
				registerCacheMBean();
			} catch (final MalformedObjectNameException e) {
				LOG.error(e.getMessage(), e);
			} catch (final ManagementException e) {
				LOG.error(e.getMessage(), e);
			} catch (final NotCompliantMBeanException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	protected void startBundle() throws Exception {
		JCSCacheServiceInit.getInstance().start(getService(ConfigurationService.class));
		/*
		 * Register MBean
		 */
		registerCacheMBean();
		/*
		 * Register service
		 */
		serviceRegistration = context.registerService(CacheService.class.getName(), JCSCacheService.getInstance(),
				dictionary);
	}

	@Override
	protected void stopBundle() throws Exception {
		try {
			/*
			 * Unregister MBean
			 */
			unregisterCacheMBean();
			/*
			 * Stop cache
			 */
			JCSCacheServiceInit.getInstance().stop();
		} finally {
			serviceRegistration.unregister();
		}
	}

	private void registerCacheMBean() throws MalformedObjectNameException, ManagementException, NotCompliantMBeanException {
		if (objectName == null) {
			objectName = getObjectName(JCSCacheInformation.class.getName(), CacheInformationMBean.CACHE_DOMAIN);
			getService(ManagementService.class).registerMBean(objectName, new JCSCacheInformation());
		}
	}

	private void unregisterCacheMBean() throws ManagementException {
		if (objectName != null) {
			try {
				getService(ManagementService.class).unregisterMBean(objectName);
			} finally {
				objectName = null;
			}
		}
	}

	/**
	 * Creates an appropriate instance of {@link ObjectName} from specified
	 * class name and domain name.
	 * 
	 * @param className
	 *            The class name to use as object name
	 * @param domain
	 *            The domain name
	 * @return An appropriate instance of {@link ObjectName}
	 * @throws MalformedObjectNameException
	 *             If instantiation of {@link ObjectName} fails
	 */
	private static ObjectName getObjectName(final String className, final String domain)
			throws MalformedObjectNameException {
		final int pos = className.lastIndexOf('.');
		return new ObjectName(CacheInformationMBean.CACHE_DOMAIN, "name", pos == -1 ? className : className.substring(pos + 1));
	}

}
