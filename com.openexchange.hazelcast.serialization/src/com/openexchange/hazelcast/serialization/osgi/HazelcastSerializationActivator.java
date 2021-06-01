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


package com.openexchange.hazelcast.serialization.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.hazelcast.serialization.PortableCheckForBooleanConfigOptionCallableFactory;
import com.openexchange.hazelcast.serialization.internal.DynamicPortableFactoryImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link HazelcastSerializationActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HazelcastSerializationActivator extends HousekeepingActivator {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastSerializationActivator.class);

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastSerializationActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[0];
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        /*
         * create & register dynamic factory
         */
        final DynamicPortableFactoryImpl dynamicFactory = new DynamicPortableFactoryImpl();
        registerService(DynamicPortableFactory.class, dynamicFactory);
        /*
         * track generic portable factories
         */
        final BundleContext context = this.context;
        track(CustomPortableFactory.class, new ServiceTrackerCustomizer<CustomPortableFactory, CustomPortableFactory>() {

            @Override
            public CustomPortableFactory addingService(ServiceReference<CustomPortableFactory> reference) {
                final CustomPortableFactory factory = context.getService(reference);
                dynamicFactory.register(factory);
                return factory;
            }

            @Override
            public void modifiedService(ServiceReference<CustomPortableFactory> reference, CustomPortableFactory service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<CustomPortableFactory> reference, CustomPortableFactory service) {
                dynamicFactory.unregister(service);
                context.ungetService(reference);
            }
        });
        trackService(ConfigurationService.class);
        openTrackers();

        registerService(CustomPortableFactory.class, new PortableCheckForBooleanConfigOptionCallableFactory(), null);
    }

    @Override
    public void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
