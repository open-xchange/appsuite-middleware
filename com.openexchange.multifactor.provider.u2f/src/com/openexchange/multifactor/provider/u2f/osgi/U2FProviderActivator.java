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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

    static Logger logger = org.slf4j.LoggerFactory.getLogger(U2FProviderActivator.class);

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
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stop(context);
    }
}
