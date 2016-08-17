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
