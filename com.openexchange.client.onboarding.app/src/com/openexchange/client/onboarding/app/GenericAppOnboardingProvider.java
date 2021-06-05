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

package com.openexchange.client.onboarding.app;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.client.onboarding.AvailabilityResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.LinkResult;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.Result;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link GenericAppOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class GenericAppOnboardingProvider implements OnboardingProvider {

    private final String identifier;
    private final EnumSet<Device> supportedDevices;
    private final EnumSet<OnboardingType> supportedTypes;
    private final AvailabilityResult availabilityResult;

    /**
     * Initializes a new {@link GenericAppOnboardingProvider}.
     */
    public GenericAppOnboardingProvider() {
        super();
        identifier = BuiltInProvider.GENERIC_APP.getId();
        supportedDevices = EnumSet.of(Device.APPLE_IPAD, Device.APPLE_IPHONE, Device.ANDROID_PHONE, Device.ANDROID_TABLET);
        supportedTypes = EnumSet.of(OnboardingType.LINK);
        availabilityResult = AvailabilityResult.available();
    }

    @Override
    public String getDescription() {
        return "Provides links for arbitrary locations.";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public AvailabilityResult isAvailable(Session session) throws OXException {
        return availabilityResult;
    }

    @Override
    public AvailabilityResult isAvailable(int userId, int contextId) throws OXException {
        return availabilityResult;
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public EnumSet<OnboardingType> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<Device> getSupportedDevices() {
        return Collections.unmodifiableSet(supportedDevices);
    }

    @Override
    public Result execute(OnboardingRequest request, Result previousResult, Session session) throws OXException {
        Device device = request.getDevice();
        if (!supportedDevices.contains(device)) {
            throw OnboardingExceptionCodes.UNSUPPORTED_DEVICE.create(identifier, device.getId());
        }

        Scenario scenario = request.getScenario();
        if (!Device.getActionsFor(request.getClientDevice(), device, scenario.getType(), session).contains(request.getAction())) {
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(request.getAction().getId());
        }

        switch (scenario.getType()) {
            case LINK:
                {
                    // Return the link from associated scenario
                    Link link = request.getScenario().getLink();
                    if (null == link) {
                        throw OnboardingExceptionCodes.MISSING_PROPERTY.create("link");
                    }
                    return new LinkResult(link);
                }
            case MANUAL:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            case PLIST:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
        }
    }

}
