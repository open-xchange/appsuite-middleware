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

package com.openexchange.client.onboarding.emclient;

import java.util.EnumSet;
import java.util.Set;
import com.openexchange.client.onboarding.AvailabilityResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.LinkResult;
import com.openexchange.client.onboarding.LinkType;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.Result;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link OnboardingEMClientProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class OnboardingEMClientProvider implements OnboardingProvider {

    private static final String URL_PROPERTY = "com.openexchange.client.onboarding.emclient.url";
    private static final String EMCLIENT_PREMIUM = "emclient_premium";
    private static final String EMCLIENT_BASIC = "emclient_basic";

    private final String identifier;
    private final Set<Device> supportedDevices;
    private final Set<OnboardingType> supportedTypes;

    /**
     * Initializes a new {@link DriveWindowsClientOnboardingActivator}.
     */
    public OnboardingEMClientProvider() {
        super();
        identifier = BuiltInProvider.EM_CLIENT.getId();
        supportedDevices = EnumSet.of(Device.WINDOWS_DESKTOP_8_10);
        supportedTypes = EnumSet.of(OnboardingType.LINK);
    }

    @Override
    public String getDescription() {
        return "Provides a link for the eM Client.";
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public Set<OnboardingType> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<Device> getSupportedDevices() {
        return supportedDevices;
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
                return doExecuteLink(session);
            case MANUAL:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            case PLIST:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
        }
    }

    private Result doExecuteLink(Session session) throws OXException {
        return linkResult(session);
    }

    private Result linkResult(Session session) throws OXException {
        return new LinkResult(new Link(getDownloadLink(session), LinkType.COMMON, null));
    }

    private String getDownloadLink(Session session) throws OXException {
        String url = OnboardingUtility.getValueFromProperty(URL_PROPERTY, null, session);
        if (Strings.isEmpty(url)) {
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(URL_PROPERTY);
        }
        return url;
    }

    @Override
    public AvailabilityResult isAvailable(Session session) throws OXException {
        boolean available = OnboardingUtility.hasCapability(EMCLIENT_BASIC, session);
        if (!available) {
            available = OnboardingUtility.hasCapability(EMCLIENT_PREMIUM, session);
        }
        return new AvailabilityResult(available, EMCLIENT_BASIC, EMCLIENT_PREMIUM);
    }

    @Override
    public AvailabilityResult isAvailable(int userId, int contextId) throws OXException {
        boolean available = OnboardingUtility.hasCapability(EMCLIENT_BASIC, userId, contextId);
        if (!available) {
            available = OnboardingUtility.hasCapability(EMCLIENT_PREMIUM, userId, contextId);
        }
        return new AvailabilityResult(available, EMCLIENT_BASIC, EMCLIENT_PREMIUM);
    }

}
