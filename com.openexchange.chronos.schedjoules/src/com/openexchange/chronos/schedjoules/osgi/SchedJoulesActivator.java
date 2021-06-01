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

package com.openexchange.chronos.schedjoules.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.http.SchedJoulesHttpConfiguration;
import com.openexchange.chronos.schedjoules.impl.SchedJoulesServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.timer.TimerService;

/**
 * {@link SchedJoulesActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link SchedJoulesActivator}.
     */
    public SchedJoulesActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, LeanConfigurationService.class, ICalService.class, TimerService.class, HttpClientService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger log = LoggerFactory.getLogger(SchedJoulesActivator.class);
        Services.setServiceLookup(this);

        SchedJoulesService service = new SchedJoulesServiceImpl();
        registerService(SchedJoulesService.class, service);
        registerService(Reloadable.class, (Reloadable) service);
        registerService(SpecificHttpClientConfigProvider.class, new SchedJoulesHttpConfiguration(this));
        log.info("Registered SchedJoules Service.");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterService(SchedJoulesService.class);
        Services.setServiceLookup(null);
        Logger log = LoggerFactory.getLogger(SchedJoulesActivator.class);
        log.info("Unregistered SchedJoules Service.");
        super.stopBundle();
    }
}
