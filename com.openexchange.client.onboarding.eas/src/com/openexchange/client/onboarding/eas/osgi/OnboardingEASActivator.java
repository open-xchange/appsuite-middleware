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

package com.openexchange.client.onboarding.eas.osgi;

import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.eas.EASOnboardingProvider;
import com.openexchange.client.onboarding.eas.custom.CustomLoginSource;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.serverconfig.ServerConfigService;

/**
 * {@link OnboardingEASActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class OnboardingEASActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link OnboardingEASActivator}.
     */
    public OnboardingEASActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, ConfigurationService.class, ServerConfigService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        RankingAwareNearRegistryServiceTracker<CustomLoginSource> loginSources = new RankingAwareNearRegistryServiceTracker<>(context, CustomLoginSource.class);
        rememberTracker(loginSources);
        openTrackers();

        registerService(OnboardingProvider.class, new EASOnboardingProvider(loginSources, this));
    }

}
