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

package com.openexchange.client.onboarding;

import java.util.EnumSet;
import java.util.Set;

/**
 * {@link GenericClientDevice} - The client device, which is the target for the on-boarding action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public enum GenericClientDevice implements ClientDevice {

    /**
     * <code>"desktop"</code> - The client is a Desktop PC
     */
    DESKTOP("desktop", EnumSet.of(Device.APPLE_MAC, Device.WINDOWS_DESKTOP_8_10)),
    /**
     * <code>"tablet"</code> - The client is a tablet device (possibly not supporting SMS)
     */
    TABLET("tablet", EnumSet.of(Device.ANDROID_TABLET, Device.APPLE_IPAD)),
    /**
     * <code>"smartphone"</code> - The client is a smart phone device
     */
    SMARTPHONE("smartphone", EnumSet.of(Device.ANDROID_PHONE, Device.APPLE_IPHONE)),
    ;

    private final String id;
    private final Set<Device> impliedDevices;

    private GenericClientDevice(String id, Set<Device> impliedDevices) {
        this.id = id;
        this.impliedDevices = impliedDevices;
    }

    @Override
    public boolean implies(Device device) {
        return null != device && impliedDevices.contains(device);
    }

    @Override
    public boolean matches(Device device) {
        return implies(device);
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the client device associated with specified identifier
     *
     * @param id The identifier to resolve
     * @return The client device or <code>null</code>
     */
    public static GenericClientDevice clientDeviceFor(String id) {
        if (null != id) {
            for (GenericClientDevice clientDevice : GenericClientDevice.values()) {
                if (id.equalsIgnoreCase(clientDevice.id)) {
                    return clientDevice;
                }
            }
        }
        return null;
    }

}
