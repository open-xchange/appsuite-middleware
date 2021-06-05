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

package com.openexchange.chronos.scheduling.impl.imip.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.scheduling.TransportProvider;
import com.openexchange.chronos.scheduling.impl.imip.IMipTransportProvider;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link SchedulingIMipActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class SchedulingIMipActivator extends HousekeepingActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingIMipActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ICalService.class, RecurrenceService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { ContactService.class, HtmlService.class, UserService.class, VersionService.class, LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOGGER.info("Starting transport provider for iMIP");

        /*
         * Register TransportProvider
         */
        registerService(TransportProvider.class, new IMipTransportProvider(this));
    }

}
