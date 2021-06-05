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

package com.openexchange.client.onboarding.internal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Platform;
import com.openexchange.client.onboarding.service.OnboardingView;

/**
 * {@link OnboardingViewImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingViewImpl implements OnboardingView {

    private final EnumSet<Platform> platforms;
    private final EnumMap<Device, List<CompositeId>> devices;
    private final ClientDevice clientDevice;

    /**
     * Initializes a new {@link OnboardingViewImpl}.
     *
     * @param clientDevice The target device
     */
    public OnboardingViewImpl(ClientDevice clientDevice) {
        super();
        this.clientDevice = clientDevice;
        platforms = EnumSet.noneOf(Platform.class);
        devices = new EnumMap<Device, List<CompositeId>>(Device.class);
    }

    @Override
    public ClientDevice getClientDevice() {
        return clientDevice;
    }

    /**
     * Adds the specified on-boarding devices to this view
     *
     * @param availableDevices The available devices to add
     */
    public void add(Map<Device, List<CompositeId>> availableDevices) {
        for (Map.Entry<Device, List<CompositeId>> availableDevice : availableDevices.entrySet()) {
            add(availableDevice.getKey(), availableDevice.getValue());
        }
    }

    /**
     * Adds the specified on-boarding device to this view
     *
     * @param device The device to add
     * @param compositeIds The composite identifiers for available scenarios
     */
    public void add(Device device, List<CompositeId> compositeIds) {
        platforms.add(device.getPlatform());

        List<CompositeId> existingCompositeIds = this.devices.get(device);
        if (null == existingCompositeIds) {
            existingCompositeIds = new ArrayList<CompositeId>(compositeIds.size());
            this.devices.put(device, existingCompositeIds);
        }
        existingCompositeIds.addAll(compositeIds);
    }

    @Override
    public Set<Platform> getPlatforms() {
        return platforms;
    }

    @Override
    public Map<Device, List<CompositeId>> getDevices() {
        return devices;
    }

}
