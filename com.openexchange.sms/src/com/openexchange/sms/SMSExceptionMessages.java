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

package com.openexchange.sms;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link SMSExceptionMessages}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class SMSExceptionMessages implements LocalizableStrings {

    // Could not parse phone number %1$s
    public final static String PARSING_ERROR_MSG = "Could not parse phone number %1$s";

    // Unknown country tag: %1$s
    public final static String UNKNOWN_COUNTRY_MSG = "Unknown country tag: %1$s";

    // Message is too long (%1$s characters). Maximum length is %2$s characters.
    public static final String MESSAGE_TOO_LONG_MSG = "Message is too long (%1$s characters). Maximum length is %2$s characters.";

    private SMSExceptionMessages() {
        super();
    }

}
