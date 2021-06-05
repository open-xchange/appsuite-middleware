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

package com.openexchange.exception;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link OXExceptionStrings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXExceptionStrings implements LocalizableStrings {

    // Text displayed to user if there is no message.
    public static final String DEFAULT_MESSAGE = "[Not available]";

    // The default message displayed to user.
    public static final String MESSAGE = "An error occurred inside the server which prevented it from fulfilling the request.";

    // The default message displayed to user when a re-try is recommended
    public static final String MESSAGE_RETRY = "A temporary error occurred inside the server which prevented it from fulfilling the request. Please try again later.";

    // The default message displayed to user when processing was intentionally denied.
    public static final String MESSAGE_DENIED = "The server is refusing to process the request.";

    // The general message for a conflicting update operation.
    public static final String MESSAGE_CONFLICT = "The object has been changed in the meantime.";

    // The general message for a missing object.
    public static final String MESSAGE_NOT_FOUND = "Object not found. %1$s";

    // The general message if a user has no access to a certain module (e.g. calendar)
    public static final String MESSAGE_PERMISSION_MODULE = "No permission for module: %1$s.";

    // The general message if a user has no permission to access a certain folder.
    public static final String MESSAGE_PERMISSION_FOLDER = "No folder permission.";

    // The general message for a missing field.
    public static final String MESSAGE_MISSING_FIELD = "Missing field: %s";

    // The general message if an error occurred while reading or writing to the database
    public static final String SQL_ERROR_MSG = "Error while reading/writing data from/to the database.";

    // General message if a setting cannot be put into database because it exceeds a column's capacity constraints
    public static final String DATA_TRUNCATION_ERROR_MSG = "Data cannot be stored into the database because it is too big";

    // The request sent by the client was syntactically incorrect
    public static final String BAD_REQUEST = "The request sent by the client was syntactically incorrect";

    /**
     * Initializes a new {@link OXExceptionStrings}.
     */
    public OXExceptionStrings() {
        super();
    }

}
