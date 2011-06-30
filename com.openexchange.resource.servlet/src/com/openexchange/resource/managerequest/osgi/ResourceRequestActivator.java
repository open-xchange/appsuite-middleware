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

package com.openexchange.resource.managerequest.osgi;

import static com.openexchange.resource.managerequest.services.ResourceRequestServiceRegistry.getServiceRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.managerequest.preferences.Module;
import com.openexchange.resource.managerequest.request.ResourceManageRequest;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.user.UserService;

/**
 * {@link ResourceRequestActivator} - {@link BundleActivator Activator} for resource servlet.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceRequestActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ResourceRequestActivator.class);

    private ServiceRegistration handlerRegistration;

    private ServiceRegistration preferencesItemRegistration;

    /**
     * Initializes a new {@link ResourceRequestActivator}
     */
    public ResourceRequestActivator() {
        super();
    }

    private static final Class<?>[] NEEDED_SERVICES = { ResourceService.class, UserService.class };

    /*
     * (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregisterRequestHandler();
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        /*
         * Register servlet on both available HTTP service and resource service
         */
        getServiceRegistry().addService(clazz, getService(clazz));
        if (getServiceRegistry().size() == NEEDED_SERVICES.length) {
            registerRequestHandler();
        }
    }

    /*
     * (non-Javadoc)
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
            registerRequestHandler();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void registerRequestHandler() {
        /*
         * Register request handler
         */
        handlerRegistration = context.registerService(AJAXRequestHandler.class.getName(), new ResourceManageRequest(), null);
        preferencesItemRegistration = context.registerService(PreferencesItemService.class.getName(), new Module(), null);
    }

    private void unregisterRequestHandler() {
        if (handlerRegistration != null) {
            /*
             * Unregister service
             */
            handlerRegistration.unregister();
            handlerRegistration = null;
        }
        if (preferencesItemRegistration != null) {
            preferencesItemRegistration.unregister();
            preferencesItemRegistration = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#stopBundle()
     */
    @Override
    protected void stopBundle() throws Exception {
        try {
            unregisterRequestHandler();
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
