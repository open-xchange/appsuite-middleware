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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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


package com.openexchange.http.requestwatcher.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.http.requestwatcher.internal.RequestWatcherServiceImpl;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

public class RequestWatcherActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(RequestWatcherActivator.class));
    private RequestWatcherServiceImpl requestWatcher;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, TimerService.class };
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#handleAvailability(java.lang.Class)
     */
    @Override
    protected void handleAvailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Service " + clazz.getName() + " is available again.");
        }
        Object service = getService(clazz);
        RequestWatcherServiceRegistry.getInstance().addService(clazz, service);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#handleUnavailability(java.lang.Class)
     */
    @Override
    protected void handleUnavailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Service " + clazz.getName() + " is no longer available.");
        }
        RequestWatcherServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void startBundle() throws OXException {
            if (LOG.isInfoEnabled()) {
                LOG.info("Starting RequestWatcher.");
            }

            RequestWatcherServiceRegistry requestWatcherServiceRegistry = RequestWatcherServiceRegistry.getInstance();

            /*
             * initialize the registry, handleUn/Availability keeps track of services.
             * Otherwise use trackService(ConfigurationService.class) and openTrackers() to let the superclass handle the services.
             */
            initializeServiceRegistry(requestWatcherServiceRegistry);

            requestWatcher = new RequestWatcherServiceImpl();
            registerService(RequestWatcherService.class, requestWatcher);
    }

    @Override
    protected void stopBundle() throws Exception {
        //Stop the Watcher
        requestWatcher.stopWatching();

        /*
         * Clear the registry from the services we are tracking.
         * Otherwise use super.stopBundle(); if we let the superclass handle the services.
         */
        RequestWatcherServiceRegistry.getInstance().clearRegistry();

        if (LOG.isInfoEnabled()) {
            LOG.info("Unregistering RequestWatcherService");
        }
        unregisterServices();
    }

    /**
     * Initialize the package wide service registry with the services we declared as needed.
     * @param serviceRegistry the registry to fill
     */
    private void initializeServiceRegistry(final RequestWatcherServiceRegistry serviceRegistry) {
        serviceRegistry.clearRegistry();
        Class<?>[] serviceClasses = getNeededServices();
        for (Class<?> serviceClass : serviceClasses) {
            Object service = getService(serviceClass);
            if (service != null) {
                serviceRegistry.addService(serviceClass, service);
            }
        }
    }

}

