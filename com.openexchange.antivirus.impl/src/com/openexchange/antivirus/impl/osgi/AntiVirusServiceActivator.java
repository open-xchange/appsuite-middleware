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

package com.openexchange.antivirus.impl.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.antivirus.impl.AntiVirusResultEvaluatorServiceImpl;
import com.openexchange.antivirus.impl.AntiVirusServiceImpl;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.icap.ICAPClientFactoryService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AntiVirusServiceActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class AntiVirusServiceActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link AntiVirusServiceActivator}.
     */
    public AntiVirusServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, ICAPClientFactoryService.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger log = LoggerFactory.getLogger(AntiVirusServiceActivator.class);
        log.info("Starting Anti-Virus service");
        CapabilityServiceTracker t = new CapabilityServiceTracker(context, this);
        track(t.getServiceFilter(), t);
        openTrackers();
        registerService(AntiVirusService.class, new AntiVirusServiceImpl(this));
        registerService(AntiVirusResultEvaluatorService.class, new AntiVirusResultEvaluatorServiceImpl());
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger log = LoggerFactory.getLogger(AntiVirusServiceActivator.class);
        log.info("Stopping Anti-Virus service");
        super.stopBundle();
    }
}
