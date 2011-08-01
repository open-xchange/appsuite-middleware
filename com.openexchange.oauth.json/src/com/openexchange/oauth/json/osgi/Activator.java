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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.oauth.json.osgi;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.oauthaccount.AccountServlet;
import com.openexchange.oauth.json.oauthaccount.multiple.AccountMultipleHandlerFactory;
import com.openexchange.oauth.json.oauthmeta.MetaDataServlet;
import com.openexchange.oauth.json.oauthmeta.multiple.MetaDataMultipleHandlerFactory;
import com.openexchange.oauth.json.service.ServiceRegistry;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.RegistryServiceTrackerCustomizer;
import com.openexchange.tools.service.SessionServletRegistration;

/**
 * {@link Activator} - Activator for JSON folder interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Activator extends DeferredActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Activator.class));

    private List<ServiceRegistration> serviceRegistrations;

    private List<ServiceTracker> trackers;

    private OSGiOAuthService oAuthService;

    private WhiteboardSecretService secretService;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        ServiceRegistry.getInstance().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        ServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            final ServiceRegistry registry = ServiceRegistry.getInstance();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (final Class<?> classe : classes) {
                final Object service = getService(classe);
                if (null != service) {
                    registry.addService(classe, service);
                }
            }
            /*
             * Service trackers
             */
            trackers = new ArrayList<ServiceTracker>(6);
            trackers.add(new ServiceTracker(context, OAuthService.class.getName(), new RegistryServiceTrackerCustomizer<OAuthService>(
                context,
                registry,
                OAuthService.class)));
            /*
             * Tracker for HTTPService to register servlets
             */
            trackers.add(new SessionServletRegistration(context, new AccountServlet(), "/ajax/" + AccountMultipleHandlerFactory.MODULE));
            trackers.add(new SessionServletRegistration(context,new MetaDataServlet(), "/ajax/" + MetaDataMultipleHandlerFactory.MODULE));
            /*
             * Open trackers
             */
            for (final ServiceTracker tracker : trackers) {
                tracker.open();
            }
            /*
             * Service registrations
             */
            serviceRegistrations = new ArrayList<ServiceRegistration>(4);
            serviceRegistrations.add(context.registerService(
                MultipleHandlerFactoryService.class.getName(),
                new AccountMultipleHandlerFactory(),
                null));
            oAuthService = new OSGiOAuthService().start(context);
            // registry.addService(OAuthService.class, oAuthService);
            AbstractOAuthAJAXActionService.setOAuthService(oAuthService);
            secretService = new WhiteboardSecretService(context);
            secretService.open();
            AbstractOAuthAJAXActionService.setSecretService(secretService);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            if(secretService != null) {
                secretService.close();
            }
            if (null != trackers) {
                /*
                 * Close trackers
                 */
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
                trackers = null;
            }
            if (null != serviceRegistrations) {
                /*
                 * Unregister
                 */
                while (!serviceRegistrations.isEmpty()) {
                    serviceRegistrations.remove(0).unregister();
                }
                serviceRegistrations = null;
            }
            if (null != oAuthService) {
                oAuthService.stop();
                oAuthService = null;
            }
            AbstractOAuthAJAXActionService.setOAuthService(null);
            ServiceRegistry.getInstance().clearRegistry();
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(Activator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

}
