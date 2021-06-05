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

package com.openexchange.client.onboarding.osgi;

import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.client.onboarding.internal.ConfiguredScenario;
import com.openexchange.client.onboarding.internal.OnboardingConfig;
import com.openexchange.client.onboarding.internal.OnboardingServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;

/**
 * {@link OnboardingReloadable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingReloadable implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OnboardingReloadable.class);

    private final OnboardingServiceImpl serviceImpl;

    /**
     * Initializes a new {@link OnboardingReloadable}.
     */
    public OnboardingReloadable(OnboardingServiceImpl serviceImpl) {
        super();
        this.serviceImpl = serviceImpl;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            Map<String, ConfiguredScenario> scenarios = OnboardingConfig.parseScenarios(configService);
            serviceImpl.setConfiguredScenarios(scenarios);
        } catch (OXException e) {
            LOG.error("Failed to reload on-boarding scenarios", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames(OnboardingConfig.getScenariosConfigFileName()).build();
    }

}
