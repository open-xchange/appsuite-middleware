/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.blackwhitelist.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.blackwhitelist.BlackWhiteListInterface;
import com.openexchange.blackwhitelist.BlackWhiteListServlet;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.osgi.service.http.HttpServices;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends DeferredActivator {

    private String alias;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, BlackWhiteListInterface.class, DispatcherPrefixService.class };
    }

    @Override
    protected synchronized void handleAvailability(final Class<?> clazz) {
        registerServlet();
    }

    @Override
    protected synchronized void handleUnavailability(final Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final ServiceRegistry registry = ServletServiceRegistry.getInstance();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (int i = 0; i < classes.length; i++) {
            final Object service = getService(classes[i]);
            if (service != null) {
                registry.addService(classes[i], service);
            }
        }

        registerServlet();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        unregisterServlet();
        ServletServiceRegistry.getInstance().clearRegistry();
    }

    private void registerServlet() {
        HttpService httpService = ServletServiceRegistry.getInstance().getService(HttpService.class);
        if (null != httpService) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Activator.class);
            try {
                String alias = getService(DispatcherPrefixService.class).getPrefix() + "blackwhitelist";
                httpService.registerServlet(alias, new BlackWhiteListServlet(), null, null);
                this.alias = alias;
                logger.info("Black-/Whitelist Servlet registered.");
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    private void unregisterServlet() {
        HttpService httpService = getService(HttpService.class);
        if (httpService != null) {
            String alias = this.alias;
            if (null != alias) {
                this.alias = null;
                HttpServices.unregister(alias, httpService);
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Activator.class);
                logger.info("Black-/Whitelist Servlet unregistered.");
            }
        }
    }

}
