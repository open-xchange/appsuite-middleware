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

package com.openexchange.management.osgi;

import static com.openexchange.management.services.ManagementServiceRegistry.getServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.management.ManagementService;
import com.openexchange.management.internal.ManagementAgentImpl;
import com.openexchange.management.internal.ManagementInit;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;

/**
 * {@link ManagementActivator} - Activator for management bundle
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagementActivator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(ManagementActivator.class);

    private static final String BUNDLE_ID_ADMIN = "com.openexchange.admin";

    private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class };

    private ServiceRegistration serviceRegistration;

    /**
     * Initializes a new {@link ManagementActivator}
     */
    public ManagementActivator() {
        super();
    }

    private static boolean isAdminBundleInstalled(final BundleContext context) {
        for (final Bundle bundle : context.getBundles()) {
            if (BUNDLE_ID_ADMIN.equals(bundle.getSymbolicName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));
        /*
         * TODO: Should the management bundle be restarted due to re-available configuration service?
         */
        /**
         * <pre>
         * stopInternal();
         * startInternal();
         * </pre>
         */
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        /*
         * Just remove absent service from service registry but do not stop management bundle
         */
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.management");
        if (isAdminBundleInstalled(context)) {
            LOG.info("Canceling start of com.openexchange.management since admin bundle is running");
            return;
        }
        /*
         * Fill service registry
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
        startInternal();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.management");
        if (isAdminBundleInstalled(context)) {
            LOG.info("Canceling stop of com.openexchange.management since admin bundle is running");
            return;
        }
        stopInternal();
        /*
         * Clear service registry
         */
        getServiceRegistry().clearRegistry();
    }

    private void startInternal() throws AbstractOXException {
        ManagementInit.getInstance().start();
        /*
         * Register management service
         */
        serviceRegistration = context.registerService(ManagementService.class.getCanonicalName(), ManagementAgentImpl.getInstance(), null);
    }

    private void stopInternal() throws AbstractOXException {
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        if (ManagementInit.getInstance().isStarted()) {
            ManagementInit.getInstance().stop();
        }
    }
}
