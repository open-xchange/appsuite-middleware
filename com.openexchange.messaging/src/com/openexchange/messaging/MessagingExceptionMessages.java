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

package com.openexchange.messaging;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MessagingExceptionMessages} - Exception messages for {@link OXException} that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MessagingExceptionMessages implements LocalizableStrings {

    // A messaging error occurred: %1$s
    public static final String MESSAGING_ERROR_MSG = "A messaging error occurred: %1$s";

    // Wrongly formatted address: %1$s.
    public static final String ADDRESS_ERROR_MSG = "Wrongly formatted address: %1$s.";

    // Unknown messaging content: %1$s.
    public static final String UNKNOWN_MESSAGING_CONTENT_MSG = "Unknown messaging content: %1$s.";

    // Invalid parameter: %1$s with value '%2$s'.
    public static final String INVALID_PARAMETER_MSG = "Invalid parameter: %1$s with value '%2$s'.";

    // Messaging part is read-only: %1$s
    public static final String READ_ONLY_MSG = "Messaging part is read-only.: %1$s";

    // Unknown color label index: %1$s
    public static final String UNKNOWN_COLOR_LABEL_MSG = "Unknown color label index: %1$s";

    // A duplicate folder named "%1$s" already exists below parent folder "%2$s".
    public static final String DUPLICATE_FOLDER_MSG = "A duplicate folder named \"%1$s\" already exists below parent folder \"%2$s\".";

    // No create access on mail folder %1$s.
    public static final String NO_CREATE_ACCESS_MSG = "No create access on mail folder %1$s.";

    // Not connected
    public static final String NOT_CONNECTED_MSG = "Not connected";

    // Invalid sorting column. Cannot sort by %1$s.
    public static final String INVALID_SORTING_COLUMN_MSG = "Invalid sorting column. Cannot sort by %1$s.";

    // No attachment found with section identifier %1$s in message %2$s in folder %3$s.
    public static final String ATTACHMENT_NOT_FOUND_MSG = "No attachment found with section identifier %1$s in message %2$s in folder %3$s.";

    // Message %1$s not found in folder %2$s.
    public static final String MESSAGE_NOT_FOUND_MSG = "Message %1$s not found in folder %2$s.";

    // Invalid OAuth account specified.
    public static final String INVALID_OAUTH_ACCOUNT_MSG = "Invalid OAuth account specified.";

    // The account configuration is invalid.
    public static final String INVALID_ACCOUNT_CONFIG_MSG = "The account configuration is invalid.";

    /**
     * Initializes a new {@link MessagingExceptionMessages}.
     */
    private MessagingExceptionMessages() {
        super();
    }

}
