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

package com.openexchange.sessiond.osgi;

import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.ServiceRegistration;

import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.cache.SessionCache;
import com.openexchange.sessiond.impl.SessionControlObject;
import com.openexchange.sessiond.impl.SessionHandler;
import com.openexchange.sessiond.impl.SessionImpl;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.sessiond.impl.SessiondInit;

/**
 * {@link SessiondActivator} - Activator for sessiond bundle
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondActivator extends DeferredActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SessiondActivator.class);

	private final AtomicBoolean started;

	private ServiceRegistration sessiondServiceRegistration;

	private boolean reconfigureCache;

	/**
	 * Initializes a new {@link SessiondActivator}
	 */
	public SessiondActivator() {
		super();
		started = new AtomicBoolean();
	}

	private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class, CacheService.class };

	@Override
	protected Class<?>[] getNeededServices() {
		return NEEDED_SERVICES;
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		/*
		 * Don't stop the sessiond
		 */
		if (LOG.isWarnEnabled()) {
			LOG.warn("Absent service: " + clazz.getName());
		}
		if (CacheService.class.equals(clazz)) {
			// TODO: free affected cache regions

			reconfigureCache = true;
		}
		getServiceRegistry().removeService(clazz);
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Re-available service: " + clazz.getName());
		}
		getServiceRegistry().addService(clazz, getService(clazz));
		if (CacheService.class.equals(clazz)) {
			// TODO: free affected cache regions

			reconfigureCache = true;
		}
	}

	@Override
	protected void startBundle() throws Exception {
		try {
			/*
			 * (Re-)Initialize service registry with available services
			 */
			{
				final ServiceRegistry registry = getServiceRegistry();
				registry.clearRegistry();
				final Class<?>[] classes = getNeededServices();
				for (int i = 0; i < classes.length; i++) {
					final Object service = getService(classes[i]);
					if (null != service) {
						registry.addService(classes[i], service);
					}
				}
			}
			if (!started.compareAndSet(false, true)) {
				/*
				 * Don't start the bundle again. A duplicate call to
				 * startBundle() is probably caused by temporary absent
				 * service(s) whose re-availability causes to trigger this
				 * method again.
				 */
				LOG.info("A temporary absent service is available again");
				if (reconfigureCache) {
					// TODO: re-configure affected cache regions
					reconfigureCache = false;
				}
				return;
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("starting bundle: com.openexchange.sessiond");
			}
			SessiondInit.getInstance().start();
			sessiondServiceRegistration = context.registerService(SessiondService.class.getName(),
					new SessiondServiceImpl(), null);
		} catch (final Exception e) {
			LOG.error("SessiondActivator: start: ", e);
			// Try to stop what already has been started.
			SessiondInit.getInstance().stop();
			throw e;
		}
	}

	@Override
	protected void stopBundle() throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("stopping bundle: com.openexchange.sessiond");
		}
		try {
			if (null != sessiondServiceRegistration) {
				sessiondServiceRegistration.unregister();
				sessiondServiceRegistration = null;
			}
			/*
			 * Put remaining sessions into cache for remote distribution
			 */
			final List<SessionControlObject> sessions = SessionHandler.getSessions();
			try {
				for (final SessionControlObject sessionControlObject : sessions) {
					if (null != sessionControlObject) {
						SessionCache.getInstance().putCachedSession(
								((SessionImpl) (sessionControlObject.getSession())).createCachedSession());
					}
				}
				if (LOG.isInfoEnabled()) {
					LOG.info("stopping bundle: "
							+ "Remaining active sessions were put into session cache for remote distribution");
				}
			} catch (final ServiceException e) {
				LOG.warn("Missing caching service."
						+ " Remaining active sessions could not be put into session cache for remote distribution", e);
			}
			/*
			 * Stop sessiond
			 */
			SessiondInit.getInstance().stop();
			/*
			 * Clear service registry
			 */
			getServiceRegistry().clearRegistry();
		} catch (final Exception e) {
			LOG.error("SessiondActivator: stop: ", e);
			throw e;
		} finally {
			started.set(false);
		}
	}

}
