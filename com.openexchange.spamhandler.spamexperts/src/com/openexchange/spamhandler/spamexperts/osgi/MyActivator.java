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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.spamhandler.spamexperts.osgi;

import static com.openexchange.spamhandler.spamexperts.osgi.MyServiceRegistry.getServiceRegistry;

import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;

import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;
import com.openexchange.user.UserService;

public class MyActivator extends HousekeepingActivator {
	
	private static final Log LOG = com.openexchange.log.Log.loggerFor(MyActivator.class);

	private HTTPServletRegistration servletRegistration;

	public MyActivator() {
		super();		
	}
	
	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { UserService.class,DatabaseService.class,ContextService.class,ConfigurationService.class,HttpService.class};
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
		if (LOG.isWarnEnabled()) {
			LOG.warn("Absent service: " + clazz.getName());
		}
		
		getServiceRegistry().addService(clazz, getService(clazz));
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Re-available service: " + clazz.getName());
		}
		getServiceRegistry().removeService(clazz);
		
	}

	@Override
	protected void startBundle() throws Exception {
		
		// try to load all the needed services like config service and hostnameservice
		try {
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
			
			
			// register the http info/sso servlet
			servletRegistration = new HTTPServletRegistration(context, new com.openexchange.spamhandler.spamexperts.impl.MyServlet(), getFromConfig("com.openexchange.custom.spamexperts.panel_servlet"));
			
			
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
		
	}
	
	private String getFromConfig(final String key) throws OXException {
        final ConfigurationService configservice = MyServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);
        return configservice.getProperty(key);
    }

	@Override
	protected void stopBundle() throws Exception {
		try {
			// stop info/sso servlet 
			if(servletRegistration != null) {
			    servletRegistration.unregister();
			    servletRegistration = null;
			}
			getServiceRegistry().clearRegistry();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}
	
}
