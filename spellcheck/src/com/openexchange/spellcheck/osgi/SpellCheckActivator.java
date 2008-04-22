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

package com.openexchange.spellcheck.osgi;

import static com.openexchange.spellcheck.services.SpellCheckServiceRegistry.getServiceRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.service.http.HttpService;

import com.openexchange.config.ConfigurationService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.spellcheck.internal.SpellCheckInit;

/**
 * {@link SpellCheckActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpellCheckActivator extends DeferredActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpellCheckActivator.class);

	private final AtomicBoolean started;

	/**
	 * Initializes a new {@link SpellCheckActivator}
	 */
	public SpellCheckActivator() {
		super();
		started = new AtomicBoolean();
	}

	private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class, HttpService.class };

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.osgiservice.DeferredActivator#getNeededServices()
	 */
	@Override
	protected Class<?>[] getNeededServices() {
		return NEEDED_SERVICES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.osgiservice.DeferredActivator#handleUnavailability(java.lang.Class)
	 */
	@Override
	protected void handleUnavailability(Class<?> clazz) {
		/*
		 * Never stop the server even if a needed service is absent
		 */
		if (LOG.isWarnEnabled()) {
			LOG.warn("Absent service: " + clazz.getName());
		}
		getServiceRegistry().removeService(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.osgiservice.DeferredActivator#startBundle()
	 */
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
				return;
			}
			/*
			 * Start spell check
			 */
			SpellCheckInit.getInstance().start();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.osgiservice.DeferredActivator#stopBundle()
	 */
	@Override
	protected void stopBundle() throws Exception {
		try {
			/*
			 * Stop spell check
			 */
			SpellCheckInit.getInstance().stop();
			/*
			 * Clear service registry
			 */
			getServiceRegistry().clearRegistry();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		} finally {
			started.set(false);
		}
	}

}
