/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.client.onboarding.internal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * Initializes a new {@link OnboardingViewImpl}.
     */
    public OnboardingViewImpl() {
        super();
        platforms = EnumSet.noneOf(Platform.class);
        devices = new EnumMap<Device, List<CompositeId>>(Device.class);
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
