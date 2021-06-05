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

package com.openexchange.unifiedinbox;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link UnifiedInboxExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UnifiedInboxExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link UnifiedInboxExceptionMessage}.
     */
    private UnifiedInboxExceptionMessage() {
        super();
    }

    // Unified Mail does not support to create folders.
    public final static String CREATE_DENIED_MSG = "Unified Mail does not support folder creation.";

    // Unified Mail does not support to delete folders.
    public final static String DELETE_DENIED_MSG = "Unified Mail does not support folder deletion.";

    // Unified Mail does not support to update folders.
    public final static String UPDATE_DENIED_MSG = "Unified Mail does not support folder update.";

    // Unified Mail does not support to move messages.
    public final static String MOVE_MSGS_DENIED_MSG = "Unified Mail does not support to move messages.";

    // Unified Mail does not support to copy messages.
    public final static String COPY_MSGS_DENIED_MSG = "Unified Mail does not support to copy messages.";

    // Append messages failed.
    public final static String APPEND_MSGS_DENIED_MSG = "Append messages failed.";

    // Unified Mail does not support draft messages.
    public final static String DRAFTS_NOT_SUPPORTED_MSG = "Unified Mail does not support draft messages.";

    // Unified Mail does not support to move folders.
    public final static String MOVE_DENIED_MSG = "Unified Mail does not support to move folders.";

    // Unified Mail does not support mail folder creation
    public final static String FOLDER_CREATION_FAILED_MSG = "Unified Mail does not support mail folder creation";

    // Unified Mail does not support to clear INBOX folder.
    public final static String CLEAR_NOT_SUPPORTED_MSG = "Unified Mail does not support to clear INBOX folder.";

    // No connection available to access mailbox
    public final static String NOT_CONNECTED_MSG = "No connection available to access mailbox.";

    // Unknown default folder fullname: %1$s.
    public final static String UNKNOWN_DEFAULT_FOLDER_INDEX_MSG = "Unknown default folder full name: %1$s.";

    // Move operation aborted. Source and destination folder are equal.
    public final static String NO_EQUAL_MOVE_MSG = "Move operation aborted. Source and destination folder are the same.";

    // Request aborted due to timeout of %1$s %2$s.
    public final static String TIMEOUT_MSG = "Request aborted due to timeout of %1$s %2$s.";

    // Invalid destination folder. Don't know where to append the mails.
    public final static String INVALID_DESTINATION_FOLDER_MSG = "Invalid destination folder. E-Mails cannot be appended.";

}
