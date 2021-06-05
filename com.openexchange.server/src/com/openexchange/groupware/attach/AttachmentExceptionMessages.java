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

package com.openexchange.groupware.attach;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AttachmentExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class AttachmentExceptionMessages implements LocalizableStrings {

    public static final String OVER_LIMIT_MSG = "Attachment cannot be saved. File store limit is exceeded.";

    public static final String SAVE_FAILED_MSG = "Could not save file to the file store.";

    public static final String FILE_MISSING_MSG = "Attachments must contain a file.";

    public static final String READ_FAILED_MSG = "Could not retrieve file.";

    public static final String ATTACHMENT_NOT_FOUND_MSG = "The attachment you requested no longer exists. Please refresh the view.";

    public static final String DELETE_FAILED_MSG = "Could not delete attachment.";

    public static final String INVALID_CHARACTERS_MSG = "Attachment metadata contains invalid characters.";

    public static final String FILESTORE_DOWN_MSG = "Unable to access the file store.";

    public static final String INVALID_REQUEST_PARAMETER_MSG = "Invalid parameter sent in request. Parameter '%1$s' was '%2$s' which does not look like a number.";

    private AttachmentExceptionMessages() {
        super();
    }
}
