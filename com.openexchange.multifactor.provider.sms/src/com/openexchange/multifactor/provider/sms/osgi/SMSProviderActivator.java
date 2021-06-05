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

package com.openexchange.multifactor.provider.sms.osgi;

import java.io.IOException;
import java.io.ObjectInputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorToken;
import com.openexchange.multifactor.provider.sms.SMSMultifactorDevice;
import com.openexchange.multifactor.provider.sms.demo.impl.DemoAwareTokenCreationStrategy;
import com.openexchange.multifactor.provider.sms.impl.MultifactorSMSProvider;
import com.openexchange.multifactor.provider.sms.storage.SMSMultifactorDeviceStorage;
import com.openexchange.multifactor.provider.sms.token.HOTPTokenCreationStrategy;
import com.openexchange.multifactor.storage.hazelcast.impl.HazelcastMultifactorTokenStorage;
import com.openexchange.multifactor.storage.impl.MemoryMultifactorDeviceStorage;
import com.openexchange.multifactor.storage.impl.MemoryMultifactorTokenStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sms.PhoneNumberParserService;
import com.openexchange.sms.SMSServiceSPI;

/**
 * {@link SMSProviderActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class SMSProviderActivator extends HousekeepingActivator {

    static final Logger logger = org.slf4j.LoggerFactory.getLogger(SMSProviderActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {LeanConfigurationService.class, SMSServiceSPI.class, PhoneNumberParserService.class, SMSMultifactorDeviceStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = super.context;
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        final LeanConfigurationService configurationService = getServiceSafe(LeanConfigurationService.class);
        final SMSMultifactorDeviceStorage storage = getServiceSafe(SMSMultifactorDeviceStorage.class);
        final SMSServiceSPI smsService = getServiceSafe(SMSServiceSPI.class);
        final PhoneNumberParserService phoneNumberParser = getServiceSafe(PhoneNumberParserService.class);

        //If no hazelcast is available, the default storage is memory based
        final MemoryMultifactorTokenStorage<MultifactorToken<String>> defaultStorage = new MemoryMultifactorTokenStorage<MultifactorToken<String>>();

        final DemoAwareTokenCreationStrategy tokenCreationStrategy = new DemoAwareTokenCreationStrategy(configurationService, new HOTPTokenCreationStrategy());
        registerService(Reloadable.class, tokenCreationStrategy);
        final MultifactorSMSProvider smsProvider =
            new MultifactorSMSProvider(
                configurationService,
                storage,
                new MemoryMultifactorDeviceStorage<SMSMultifactorDevice>(),
                tokenCreationStrategy,
                smsService,
                phoneNumberParser).setTokenStorage(defaultStorage);
        registerService(MultifactorProvider.class, smsProvider);
        registerService(Reloadable.class, smsProvider);

        //Track hazelcast; hazelcast is preferred as token storage if available
        final ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> hazelcastTracker =
            new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>(){

                @Override
                public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                    final HazelcastInstance hzService = context.getService(reference);
                    final HazelcastMultifactorTokenStorage<MultifactorToken<String>> hzTokenStorage = new HazelcastMultifactorTokenStorage<MultifactorToken<String>>(
                        hzService,
                        "MULTIFACTOR_SMS_TOKENS",
                        portable -> {
                            //Map portable to SMSToken
                            try (ObjectInputStream s = portable.getTokenValueData()){
                                final String tokenValue = (String)s.readObject();
                                return new MultifactorToken<String>(tokenValue, portable.getLifeTime().orElse(null));
                            } catch (ClassNotFoundException | IOException e) {
                                logger.error(e.getMessage(), e);
                                return null;
                            }
                        });
                    smsProvider.setTokenStorage(hzTokenStorage);
                    return hzService;
                }

                @Override
                public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                    //no-op
                }

                @Override
                public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                    //Switch back to default storage if hazelcast becomes unavailable
                    smsProvider.setTokenStorage(defaultStorage);
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