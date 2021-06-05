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

package com.openexchange.client.onboarding.notification;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link OnboardingNotificationStrings}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class OnboardingNotificationStrings implements LocalizableStrings {

    private OnboardingNotificationStrings() {
        super();
    }

    // The user salutation; e.g. "Dear John Doe,"
    public static final String SALUTATION = "Dear %1$s,";

    // The content of the E-Mail providing the profile attachment
    public static final String CONTENT = "to automatically configure your device, please download & install the attached configuration profile.";

    // The content of the E-Mail providing the profile attachment
    public static final String CONTENT_WITH_FILENAME = "to automatically configure your device, please download & install the attached configuration profile \"%1$s\".";

    // The subject of the E-Mail providing the profile attachment
    public static final String SUBJECT = "Your device configuration";

}
