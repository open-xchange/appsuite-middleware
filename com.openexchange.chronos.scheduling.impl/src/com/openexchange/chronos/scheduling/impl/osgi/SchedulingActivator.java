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

package com.openexchange.chronos.scheduling.impl.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.itip.IncomingSchedulingMailFactory;
import com.openexchange.chronos.scheduling.SchedulingBroker;
import com.openexchange.chronos.scheduling.TransportProvider;
import com.openexchange.chronos.scheduling.impl.SchedulingBrokerImpl;
import com.openexchange.chronos.scheduling.impl.incoming.IncomingSchedulingMailFactoryImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link SchedulingActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class SchedulingActivator extends HousekeepingActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingActivator.class);

    private SchedulingBrokerImpl broker;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, ContextService.class, ICalService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { CryptographicAwareMailAccessFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOGGER.info("Starting calendar scheduling related services");

        broker = new SchedulingBrokerImpl(context, this);
        /*
         * Register service tracker
         */
        track(TransportProvider.class, broker);
        openTrackers();

        /*
         * Register broker as service
         */
        registerService(SchedulingBroker.class, broker);

        /*
         * Register factory
         */
        registerService(IncomingSchedulingMailFactory.class, new IncomingSchedulingMailFactoryImpl(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        broker.close();
        unregisterService(SchedulingBroker.class);
        super.stopBundle();
    }

}
