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

package com.openexchange.resource.servlet.osgi;

import static com.openexchange.resource.servlet.services.ResourceServletServiceRegistry.getServiceRegistry;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.servlet.ResourceServlet;
import com.openexchange.resource.servlet.request.ResourceRequest;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;

/**
 * {@link ResourceServletActivator} - {@link BundleActivator Activator} for
 * resource servlet.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ResourceServletActivator extends DeferredActivator {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ResourceServletActivator.class);

	private static final String SC_SRVLT_ALIAS = "ajax/resource";

	private ServiceRegistration serviceRegistration;

	/**
	 * Initializes a new {@link ResourceServletActivator}
	 */
	public ResourceServletActivator() {
		super();
	}

	private static final Class<?>[] NEEDED_SERVICES = { ResourceService.class, HttpService.class };

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openexchange.server.osgiservice.DeferredActivator#getNeededServices()
	 */
	@Override
	protected Class<?>[] getNeededServices() {
		return NEEDED_SERVICES;
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		/*
		 * Unregister servlet on both absent HTTP service and resource service
		 */
		final HttpService httpService = getServiceRegistry().getService(HttpService.class);
		if (httpService == null) {
			LOG.error("HTTP service is null. Resource servlet cannot be unregistered");
		} else {
			/*
			 * Unregister servlet
			 */
			httpService.unregister(SC_SRVLT_ALIAS);
			if (LOG.isInfoEnabled()) {
				LOG.info("Resource servlet successfully unregistered");
			}
		}
		getServiceRegistry().removeService(clazz);
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
		/*
		 * Register servlet on both available HTTP service and resource service
		 */
		getServiceRegistry().addService(clazz, getService(clazz));
		final HttpService httpService = getServiceRegistry().getService(HttpService.class);
		if (httpService != null) {
			try {
				/*
				 * Register servlet
				 */
				httpService.registerServlet(SC_SRVLT_ALIAS, new ResourceServlet(), null, null);
				if (LOG.isInfoEnabled()) {
					LOG.info("Resource servlet successfully registered");
				}
			} catch (final ServletException e) {
				LOG.error(e.getMessage(), e);
			} catch (final NamespaceException e) {
				LOG.error(e.getMessage(), e);
			}
		}
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
			/*
			 * Register request handler
			 */
			final Dictionary<String, String> dictionary = new Hashtable<String, String>();
			dictionary.put("module", "resource");
			serviceRegistration = context.registerService(AJAXRequestHandler.class.getName(), new ResourceRequest(),
					dictionary);
			/*
			 * Register servlet to newly available HTTP service
			 */
			final HttpService httpService = getServiceRegistry().getService(HttpService.class);
			if (httpService == null) {
				LOG.error("HTTP service is null. Resource servlet cannot be registered");
				return;
			}
			try {
				/*
				 * Register servlet
				 */
				httpService.registerServlet(SC_SRVLT_ALIAS, new ResourceServlet(), null, null);
				if (LOG.isInfoEnabled()) {
					LOG.info("Resource servlet successfully registered");
				}
			} catch (final ServletException e) {
				// TODO: Generate ResourceServletException
				throw e;
			} catch (final NamespaceException e) {
				// TODO: Generate ResourceServletException
				throw e;
			}
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
			 * Unregister servlet
			 */
			final HttpService httpService = getServiceRegistry().getService(HttpService.class);
			if (httpService == null) {
				LOG.error("HTTP service is null. Resource servlet cannot be unregistered");
			} else {
				/*
				 * Unregister servlet
				 */
				httpService.unregister(SC_SRVLT_ALIAS);
				if (LOG.isInfoEnabled()) {
					LOG.info("Resource servlet successfully unregistered");
				}
			}
			/*
			 * Unregister service
			 */
			serviceRegistration.unregister();
			serviceRegistration = null;
			/*
			 * Clear service registry
			 */
			getServiceRegistry().clearRegistry();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

}
