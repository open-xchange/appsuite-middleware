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

package com.openexchange.client.onboarding.json.osgi;

import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.json.OnboardingActionFactory;
import com.openexchange.client.onboarding.json.converter.OnboardingViewConverter;
import com.openexchange.client.onboarding.json.converter.PListDownloadConverter;
import com.openexchange.client.onboarding.json.converter.ScenarioConverter;
import com.openexchange.client.onboarding.json.converter.SignedPListDownloadConverter;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.config.cascade.ConfigViewFactory;

/**
 * {@link OnboardingJsonActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OnboardingJsonActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link OnboardingJsonActivator}.
     */
    public OnboardingJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CapabilityService.class, OnboardingService.class, ConfigViewFactory.class, DownloadLinkProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(OnboardingJsonActivator.class);
        logger.info("Starting bundle: \"com.openexchange.client.onboarding.json\"");

        registerService(ResultConverter.class, new OnboardingViewConverter(this));
        registerService(ResultConverter.class, new ScenarioConverter(this));
        registerService(ResultConverter.class, new PListDownloadConverter());
        registerService(ResultConverter.class, new SignedPListDownloadConverter());
        registerModule(new OnboardingActionFactory(this), "onboarding");
    }

}
