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

package com.openexchange.client.onboarding.drive.client.windows.osgi;

import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.drive.client.windows.DriveWindowsClientOnboardingProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link DriveWindowsClientOnboardingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DriveWindowsClientOnboardingActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DriveWindowsClientOnboardingActivator}.
     */
    public DriveWindowsClientOnboardingActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, ConfigurationService.class, DriveUpdateService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DriveWindowsClientOnboardingActivator.class);
        logger.info("Starting bundle \"{}\"...", context.getBundle().getSymbolicName());
        registerService(OnboardingProvider.class, new DriveWindowsClientOnboardingProvider(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DriveWindowsClientOnboardingActivator.class);
        logger.info("Stopping bundle \"{}\"...", context.getBundle().getSymbolicName());
        super.stopBundle();
    }

}
