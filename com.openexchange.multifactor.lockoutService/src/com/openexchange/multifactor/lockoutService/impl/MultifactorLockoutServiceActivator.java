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

package com.openexchange.multifactor.lockoutService.impl;

import org.slf4j.Logger;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.multifactor.MultifactorLockoutService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.ratelimit.RateLimiterFactory;


public class MultifactorLockoutServiceActivator extends HousekeepingActivator{

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MultifactorLockoutServiceActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, RateLimiterFactory.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        final MultifactorLockoutService lockoutService = new LockoutServiceImpl(getServiceSafe(LeanConfigurationService.class), getServiceSafe(RateLimiterFactory.class));
        registerService(MultifactorLockoutService.class, lockoutService);

    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }

}
