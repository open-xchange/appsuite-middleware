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

package com.openexchange.multifactor.provider.u2f.osgi;

import java.io.IOException;
import java.io.ObjectInputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.provider.u2f.impl.MultifactorU2FProvider;
import com.openexchange.multifactor.provider.u2f.impl.SignToken;
import com.openexchange.multifactor.provider.u2f.impl.U2FMultifactorDevice;
import com.openexchange.multifactor.provider.u2f.storage.U2FMultifactorDeviceStorage;
import com.openexchange.multifactor.storage.hazelcast.impl.HazelcastMultifactorTokenStorage;
import com.openexchange.multifactor.storage.impl.MemoryMultifactorDeviceStorage;
import com.openexchange.multifactor.storage.impl.MemoryMultifactorTokenStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.yubico.u2f.data.messages.SignRequestData;

/**
 * {@link U2FProviderActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class U2FProviderActivator extends HousekeepingActivator {

    static final Logger logger = org.slf4j.LoggerFactory.getLogger(U2FProviderActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {LeanConfigurationService.class, U2FMultifactorDeviceStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = super.context;
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        final MemoryMultifactorTokenStorage<SignToken> memoryTokenStorage = new MemoryMultifactorTokenStorage<SignToken>();
        final MultifactorU2FProvider u2fProvider = new MultifactorU2FProvider(
            getServiceSafe(LeanConfigurationService.class),
            getServiceSafe(U2FMultifactorDeviceStorage.class),
            new MemoryMultifactorDeviceStorage<U2FMultifactorDevice>(),
            memoryTokenStorage);
        registerService(MultifactorProvider.class, u2fProvider);

        //Switch to hazelcast token storage, if hazelcast is available
        final ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> hazelcastTracker =
            new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                @Override
                public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                    final HazelcastInstance hzService = context.getService(reference);
                    final HazelcastMultifactorTokenStorage<SignToken> hzTokenStorage = new HazelcastMultifactorTokenStorage<SignToken>(
                        hzService,
                        "MULTIFACTOR_U2F_TOKENS",
                        portable -> {
                            //Map portable to U2F SignToken
                            try(ObjectInputStream s = portable.getTokenValueData()) {
                                final SignRequestData requestData = (SignRequestData)s.readObject();
                                return new SignToken(requestData, portable.getLifeTime().orElse(null));
                            } catch (ClassNotFoundException | IOException e) {
                                logger.error(e.getMessage(), e);
                                return null;
                            }
                        });
                    u2fProvider.setTokenStorage(hzTokenStorage);
                    return hzService;
                }

                @Override
                public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                    //no-op
                }

                @Override
                public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                    //Switch back to the default storage
                    u2fProvider.setTokenStorage(memoryTokenStorage);
                }
        };
        track(HazelcastInstance.class, hazelcastTracker);
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
