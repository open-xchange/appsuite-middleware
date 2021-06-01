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

package com.openexchange.client.onboarding.drive.client.windows;

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
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link DriveWindowsClientOnboardingProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DriveWindowsClientOnboardingProvider implements OnboardingProvider {

    private final ServiceLookup services;
    private final String identifier;
    private final Set<Device> supportedDevices;
    private final Set<OnboardingType> supportedTypes;

    /**
     * Initializes a new {@link DriveWindowsClientOnboardingProvider}.
     */
    public DriveWindowsClientOnboardingProvider(ServiceLookup services) {
        super();
        this.services = services;
        identifier = BuiltInProvider.DRIVE_WINDOWS_CLIENT.getId();
        supportedDevices = EnumSet.of(Device.WINDOWS_DESKTOP_8_10);
        supportedTypes = EnumSet.of(OnboardingType.LINK);
    }

    @Override
    public String getDescription() {
        return "Provides a link for the Drive Windows Client.";
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
        return new LinkResult(new Link(getDownloadLink(request, session), LinkType.COMMON, null));
    }

    private String getDownloadLink(OnboardingRequest request, Session session) throws OXException {
        DriveUpdateService driveUpdateService = services.getOptionalService(DriveUpdateService.class);
        if (null == driveUpdateService) {
            throw ServiceExceptionCode.absentService(DriveUpdateService.class);
        }

        return driveUpdateService.getInstallerDownloadUrl(request.getHostData(), session);
    }

}
