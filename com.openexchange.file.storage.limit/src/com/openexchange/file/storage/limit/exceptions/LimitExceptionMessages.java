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

package com.openexchange.file.storage.limit.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link LimitExceptionMessages}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class LimitExceptionMessages implements LocalizableStrings {

    public static final String STORAGE_QUOTA_EXCEEDED_MSG = "The total upload size (%1$s) exceeds the available free space of %2$s.";

    public static final String TOO_MANY_FILES_MSG = "The total number of files exceeds the maximum of %1$s.";

    public static final String FILE_QUOTA_PER_REQUEST_EXCEEDED_MSG = "'%1$s' (%2$s) exceeds the allowed size of %3$s per file.";

    public static final String NOT_ALLOWED_MSG = "You are not allowed to upload files to the given folder.";

    private LimitExceptionMessages() {
        super();
    }

}
