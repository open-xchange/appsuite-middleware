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

package com.openexchange.client.onboarding.driveapp;

import java.util.Collections;
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
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link DriveAppOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DriveAppOnboardingProvider implements OnboardingProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final Set<Device> supportedDevices;
    private final Set<OnboardingType> supportedTypes;

    /**
     * Initializes a new {@link DriveAppOnboardingProvider}.
     */
    public DriveAppOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = BuiltInProvider.DRIVE_APP.getId();
        supportedDevices = EnumSet.of(Device.APPLE_IPAD, Device.APPLE_IPHONE, Device.APPLE_MAC, Device.ANDROID_PHONE, Device.ANDROID_TABLET);
        supportedTypes = EnumSet.of(OnboardingType.LINK);
    }

    @Override
    public String getDescription() {
        return "Provides links for the Drive App.";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public AvailabilityResult isAvailable(Session session) throws OXException {
        boolean available = OnboardingUtility.hasCapability("drive", session);
        return new AvailabilityResult(available, "drive");
    }

    @Override
    public AvailabilityResult isAvailable(int userId, int contextId) throws OXException {
        boolean available = OnboardingUtility.hasCapability("drive", userId, contextId);
        return new AvailabilityResult(available, "drive");
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
                return doExecuteLink(request, session);
            case MANUAL:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            case PLIST:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_TYPE.create(identifier, scenario.getType().getId());
        }
    }

    private Result doExecuteLink(OnboardingRequest request, Session session) throws OXException {
        return linkResult(request, session);
    }

    private Result linkResult(OnboardingRequest request, Session session) throws OXException {
        return new LinkResult(getAppStoreLink(request, session));
    }

    private Link getAppStoreLink(OnboardingRequest request, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        LinkType linkType;
        String propertyName;
        {
            Device device = request.getDevice();
            switch (device.getPlatform()) {
                case APPLE:
                    {
                        switch (device) {
                            case APPLE_MAC:
                                propertyName = "com.openexchange.client.onboarding.driveapp.store.apple.macappstore";
                                linkType = LinkType.APPLE_MAC_STORE;
                                break;
                            default:
                                propertyName = "com.openexchange.client.onboarding.driveapp.store.apple.appstore";
                                linkType = LinkType.APPLE_APP_STORE;
                                break;
                        }
                    }
                    break;
                case ANDROID_GOOGLE:
                    propertyName = "com.openexchange.client.onboarding.driveapp.store.google.playstore";
                    linkType = LinkType.GOOGLE_PLAY_STORE;
                    break;
                default:
                    throw OnboardingExceptionCodes.UNSUPPORTED_DEVICE.create(identifier, device.getId());
            }
        }

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            throw OnboardingExceptionCodes.MISSING_PROPERTY.create(propertyName);
        }

        return new Link(value, linkType, null);
    }

}
