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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.onboarding;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link Device} - An enumeration for available on-boarding devices.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum Device implements IdEntity {

    /**
     * The device for an Apple Mac; <code>"apple.mac"</code>
     */
    APPLE_MAC(Platform.APPLE.getId() + ".mac", OnboardingStrings.DEVICE_APPLE_MAC_DISPLAY_NAME, OnboardingStrings.DEVICE_APPLE_MAC_DESCRIPTION, "device_icon_apple_mac.png", Platform.APPLE),
    /**
     * The device for an Apple iPad; <code>"apple.ipad"</code>
     */
    APPLE_IPAD(Platform.APPLE.getId() + ".ipad", OnboardingStrings.DEVICE_APPLE_IPAD_DISPLAY_NAME, OnboardingStrings.DEVICE_APPLE_IPAD_DESCRIPTION, "device_icon_apple_ipad.png", Platform.APPLE),
    /**
     * The device for an Apple iPhone; <code>"apple.iphone"</code>
     */
    APPLE_IPHONE(Platform.APPLE.getId() + ".iphone", OnboardingStrings.DEVICE_APPLE_IPHONE_DISPLAY_NAME, OnboardingStrings.DEVICE_APPLE_IPHONE_DESCRIPTION, "device_icon_apple_iphone.png", Platform.APPLE),

    /**
     * The device for an Android/Google tablet; <code>"android.tablet"</code>
     */
    ANDROID_TABLET(Platform.ANDROID_GOOGLE.getId() + ".tablet", OnboardingStrings.DEVICE_ANDROID_TABLET_DISPLAY_NAME, OnboardingStrings.DEVICE_ANDROID_TABLET_DESCRIPTION, "device_icon_android_tablet.png", Platform.ANDROID_GOOGLE),
    /**
     * The device for an Android/Google phone; <code>"android.phone"</code>
     */
    ANDROID_PHONE(Platform.ANDROID_GOOGLE.getId() + ".phone", OnboardingStrings.DEVICE_ANDROID_PHONE_DISPLAY_NAME, OnboardingStrings.DEVICE_ANDROID_PHONE_DESCRIPTION, "device_icon_android_phone.png", Platform.ANDROID_GOOGLE),

    /**
     * The device for a Windows Desktop 8 + 10; <code>"windows.desktop"</code>
     */
    WINDOWS_DESKTOP_8_10(Platform.WINDOWS.getId() + ".desktop", OnboardingStrings.DEVICE_WINDOWS_DESKTOP_DISPLAY_NAME, OnboardingStrings.DEVICE_WINDOWS_DESKTOP_DESCRIPTION, "device_icon_windows_desktop.png", Platform.WINDOWS),

    ;

    private final String id;

    private final String enabledProperty;
    private final String displayNameProperty;
    private final String iconProperty;
    private final String descriptionProperty;

    private final String defaultDisplayName;
    private final String defaultIcon;
    private final String defaultDescription;

    private final Platform platform;

    private Device(String id, String defaultDisplayName, String defaultDescription, String defaultIcon, Platform platform) {
        this.id = id;
        this.platform = platform;

        String prefix = "com.openexchange.onboarding." + id;
        enabledProperty = prefix + ".enabled";
        displayNameProperty = prefix + ".displayName";
        iconProperty = prefix + ".icon";
        descriptionProperty = prefix + ".description";

        this.defaultDisplayName = defaultDisplayName;
        this.defaultIcon = defaultIcon;
        this.defaultDescription = defaultDescription;
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return OnboardingUtility.getBoolValue(enabledProperty, true, session);
    }

    /**
     * Gets the platform associated with this entity
     *
     * @return The platform
     */
    public Platform getPlatform() {
        return platform;
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, defaultDisplayName, true, session);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return OnboardingUtility.loadIconImageFromProperty(iconProperty, defaultIcon, session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(descriptionProperty, defaultDescription, true, session);
    }

    /**
     * Gets the device for specified identifier
     *
     * @param id The identifier to look-up
     * @return The associated device or <code>null</code>
     */
    public static Device deviceFor(String id) {
        if (null == id) {
            return null;
        }

        for (Device device : values()) {
            if (id.equals(device.getId())) {
                return device;
            }
        }
        return null;
    }

}
