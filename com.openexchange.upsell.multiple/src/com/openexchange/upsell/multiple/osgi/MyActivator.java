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

package com.openexchange.upsell.multiple.osgi;

import static com.openexchange.upsell.multiple.osgi.MyServiceRegistry.getServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.tools.service.SessionServletRegistration;
import com.openexchange.upsell.multiple.api.UpsellURLService;
import com.openexchange.upsell.multiple.impl.MyServlet;
import com.openexchange.user.UserService;

public class MyActivator extends HousekeepingActivator {

    private static transient final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MyActivator.class));

    private SessionServletRegistration servletRegistration;

    public MyActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            UserService.class, DatabaseService.class, ContextService.class, ConfigurationService.class };
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
			servletRegistration = new SessionServletRegistration(context, new MyServlet(), getFromConfig("com.openexchange.upsell.multiple.servlet"));
			servletRegistration.open();

            // Open service trackers
			track(UpsellURLService.class, new UrlServiceInstallationServiceListener(context));
			openTrackers();


		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}

	}

    private String getFromConfig(final String key) throws OXException {
        final ConfigurationService configservice = MyServiceRegistry.getServiceRegistry().getService(ConfigurationService.class, true);
        return configservice.getProperty(key);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {

            /*
             * Close service trackers
             */
            cleanUp();

            // stop info/sso servlet
            if(servletRegistration != null) {
                servletRegistration.close();
            }

            getServiceRegistry().clearRegistry();
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

}
