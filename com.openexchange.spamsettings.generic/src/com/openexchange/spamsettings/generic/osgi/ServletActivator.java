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


package com.openexchange.spamsettings.generic.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;
import com.openexchange.spamsettings.generic.service.SpamSettingService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ServletActivator extends DeferredActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletActivator.class);

    private SpamSettingsServletRegisterer servletRegisterer;

    public ServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, SpamSettingService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (servletRegisterer != null) {
            LOG.warn("Absent service: {}", clazz.getName());

            SpamSettingsServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
            servletRegisterer.registerServlet();
            SpamSettingsModulePreferences.setModule(true);
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (servletRegisterer != null) {
            LOG.info("Re-available service: {}", clazz.getName());
            servletRegisterer.unregisterServlet();
            SpamSettingsModulePreferences.setModule(false);
            SpamSettingsServiceRegistry.getServiceRegistry().removeService(clazz);
        }

    }

    @Override
    protected void startBundle() throws Exception {
        SpamSettingsServletRegisterer.PREFIX.set(getService(DispatcherPrefixService.class));
        final ServiceRegistry registry = SpamSettingsServiceRegistry.getServiceRegistry();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (final Class<?> classe : classes) {
            final Object service = getService(classe);
            if (service != null) {
                registry.addService(classe, service);
            }
        }
        servletRegisterer = new SpamSettingsServletRegisterer();
        servletRegisterer.registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        if (servletRegisterer != null) {
            servletRegisterer.unregisterServlet();
            servletRegisterer = null;
            SpamSettingsModulePreferences.setModule(false);
            SpamSettingsServiceRegistry.getServiceRegistry().clearRegistry();
            SpamSettingsServletRegisterer.PREFIX.set(null);
        }
    }
}
