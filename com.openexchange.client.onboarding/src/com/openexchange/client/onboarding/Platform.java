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

package com.openexchange.client.onboarding;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link Platform} - A supported on-boarding platform.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum Platform implements Entity {

    /**
     * The Apple platform for OSX Desktop applications and iOS devices.
     */
    APPLE("apple", OnboardingStrings.PLATFORM_APPLE_DISPLAY_NAME, "fa-apple"),
    /**
     * The Android/Google platform for Android devices
     */
    ANDROID_GOOGLE("android", OnboardingStrings.PLATFORM_ANDROID_DISPLAY_NAME, "fa-android"),
    /**
     * The Windows platform for Windows Desktop applications.
     */
    WINDOWS("windows", OnboardingStrings.PLATFORM_WINDOWS_DISPLAY_NAME, "fa-windows"),
    ;

    private final String id;
    private final FontAwesomeIcon icon;

    private final String enabledProperty;
    private final String displayNameProperty;

    private final String defaultDisplayName;

    private Platform(String id, String defaultDisplayName, String fontAwesomeName) {
        this.id = id;
        icon = new FontAwesomeIcon(fontAwesomeName);

        String prefix = "com.openexchange.client.onboarding." + id;
        enabledProperty = prefix + ".enabled";
        displayNameProperty = prefix + ".displayName";

        this.defaultDisplayName = defaultDisplayName;
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return OnboardingUtility.getBoolValue(enabledProperty, true, session);
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        return OnboardingUtility.getBoolValue(enabledProperty, true, userId, contextId);
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, defaultDisplayName, true, session);
    }

    @Override
    public String getDisplayName(int userId, int contextId) throws OXException {
        return OnboardingUtility.getTranslationFromProperty(displayNameProperty, defaultDisplayName, true, userId, contextId);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return icon;
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return null;
    }

    /**
     * Gets the platform for specified identifier
     *
     * @param id The identifier to look-up
     * @return The associated platform or <code>null</code>
     */
    public static Platform platformFor(String id) {
        if (null == id) {
            return null;
        }

        for (Platform platform : values()) {
            if (id.equals(platform.getId())) {
                return platform;
            }
        }
        return null;
    }
}
