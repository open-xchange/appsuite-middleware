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

package com.openexchange.report.appsuite.serialization.osgi;

import static com.openexchange.report.appsuite.serialization.osgi.StringParserServiceRegistry.getServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.report.appsuite.serialization.internal.Services;
import com.openexchange.tools.strings.StringParser;

/**
 * Activates the serialization for the Report bundle by registering the {@link PortableReportFactory}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ReportSerializationActivator extends HousekeepingActivator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            StringParser.class, ConfigurationService.class
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAvailability(final Class<?> clazz) {
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        getServiceRegistry().removeService(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        final ServiceRegistry registry = getServiceRegistry();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (final Class<?> classe : classes) {
            final Object service = getService(classe);
            if (null != service) {
                registry.addService(classe, service);
            }
        }
        Services.setServices(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopBundle() throws Exception {
        Services.setServices(null);
        getServiceRegistry().clearRegistry();
        super.stopBundle();
    }
}
