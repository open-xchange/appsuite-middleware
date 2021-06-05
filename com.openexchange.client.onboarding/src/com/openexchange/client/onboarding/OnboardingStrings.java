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

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link OnboardingStrings} - Translatable string literals for on-boarding module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link OnboardingStrings}.
     */
    private OnboardingStrings() {
        super();
    }

    // The display name for Apple platform
    public static final String PLATFORM_APPLE_DISPLAY_NAME = "Apple";


    // The display name for Windows platform
    public static final String PLATFORM_WINDOWS_DISPLAY_NAME = "Windows";


    // The display name for Android/Google platform
    public static final String PLATFORM_ANDROID_DISPLAY_NAME = "Android";

    // ----------------------------------------------------------------------------------------------------------------------

    // The display name for an Apple Mac
    public static final String DEVICE_APPLE_MAC_DISPLAY_NAME = "Mac";


    // The display name for an Apple iPad
    public static final String DEVICE_APPLE_IPAD_DISPLAY_NAME = "iPad";


    // The display name for an Apple iPhone
    public static final String DEVICE_APPLE_IPHONE_DISPLAY_NAME = "iPhone";


    // The display name for an Android/Google tablet
    public static final String DEVICE_ANDROID_TABLET_DISPLAY_NAME = "Tablet";


    // The display name for an Android/Google phone
    public static final String DEVICE_ANDROID_PHONE_DISPLAY_NAME = "Smartphone";


    // The display name for a Windows Desktop
    public static final String DEVICE_WINDOWS_DESKTOP_DISPLAY_NAME = "Laptop + PC";

    // ----------------------------------------------------------------------------------------------------------------------

    // Mail successfully sent
    public static final String RESULT_MAIL_SENT = "Mail successfully sent";

    // ----------------------------------------------------------------------------------------------------------------------

    // SMS successfully sent
    public static final String RESULT_SMS_SENT = "SMS successfully sent";

}
