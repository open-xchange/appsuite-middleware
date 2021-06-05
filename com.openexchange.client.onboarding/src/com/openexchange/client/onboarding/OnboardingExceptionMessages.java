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
 * {@link OnboardingExceptionMessages} - Translatable messages for {@link OnboardingExceptionCodes}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public final class OnboardingExceptionMessages implements LocalizableStrings {

    /**
     * Prevent instantiation.
     */
    private OnboardingExceptionMessages() {
        super();
    }

    // Your selection is not supported.
    public static final String ENTITY_NOT_SUPPORTED_MSG = "Your selection is not supported.";

    // The chosen configuration is not supported.
    public static final String CONFIGURATION_NOT_SUPPORTED_MSG = "The chosen configuration is not supported.";

    // The execution has been denied. Please choose another option.
    public static final String EXECUTION_DENIED_MSG = "The execution has been denied. Please choose another option.";

    // Execution failed. Please choose another option.
    public static final String EXECUTION_FAILED_MSG = "Execution failed. Please choose another option.";

    // Sent quota exceeded. You are only allowed to send 1 SMS in %1$s seconds.
    public static final String SENT_QUOTA_EXCEEDED_MSG = "Sent quota exceeded. You are only allowed to send 1 SMS in %1$s seconds.";

    // The download link is invalid.
    public static final String INVALID_DOWNLOAD_LINK_MSG = "The download link is invalid.";

    // The download link is invalid.
    public static final String INVALID_PHONE_NUMBER_MSG = "The phone number %1$s is invalid.";

}
