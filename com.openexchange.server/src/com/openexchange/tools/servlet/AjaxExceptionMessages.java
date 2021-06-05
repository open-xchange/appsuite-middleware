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

package com.openexchange.tools.servlet;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link AjaxExceptionMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AjaxExceptionMessages implements LocalizableStrings {

    public static final String NON_SECURE_DENIED_MSG = "The requested operation is not permitted via a non-secure connection.";
    public static final String DISABLED_ACTION_MSG = "The requested operation is disabled.";
    public static final String NO_PERMISSION_FOR_MODULE = "You do not have appropriate permissions for module %1$s.";
    public static final String CONFLICT = "The object has been changed in the meantime. Please reload the view and try again.";
    public static final String NO_IMAGE_FILE_MSG = "The file \"%1$s\" (\"%2$s\") can't be imported as image. Only image types (JPG, GIF, BMP or PNG) are supported.";
    public static final String MISSING_COOKIE_MSG = "The requested operation requires a valid session. Please login and try again.";
    public static final String MISSING_FIELD_MSG = "Missing the following field in JSON data: %1$s";
    public static final String HTML_TOO_BIG_MSG = "The HTML content is too big and therefore cannot be safely displayed. Please select to download its content if you want to see it.";
    public static final String INVALID_JSON_REQUEST_BODY = "Invalid JSON data received from client";
    public static final String NOT_ALLOWED_URI_PARAM_MSG = "Client sent not allowed request parameter \"%1$s\" within the URI.";

    /**
     * Initializes a new {@link AjaxExceptionMessages}
     */
    private AjaxExceptionMessages() {
        super();
    }

}
