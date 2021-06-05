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

package com.openexchange.file.storage.webdav.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link WebdavExceptionMessages}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class WebdavExceptionMessages implements LocalizableStrings {

    // Missing capability for file storage %1$s
    public static final String MISSING_CAP_MSG = "Missing capability for file storage %1$s";

    // Invalid config: %1$s
    public static final String INVALID_CONFIG_MSG = "Invalid config: %1$s";

    // The document could not be updated because it was modified. Please try again.
    public static final String MODIFIED_CONCURRENTLY_MSG_DISPLAY = "The document could not be updated because it was modified. Please try again.";

    // The connection check failed
    public static final String PING_FAILED = "The connection check failed.";

    // Cannot connect to URI: %1$s. Please change and try again.
    public static final String URL_NOT_ALLOWED_MSG = "Cannot connect to URI: %1$s. Please change and try again.";

    // The given URL is invalid. Please change it and try again.
    public static final String BAD_URL_MSG = "The given URL is invalid. Please change it and try again.";

}
