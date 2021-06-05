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

package com.openexchange.user.copy;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link UserCopyExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UserCopyExceptionMessages implements LocalizableStrings {

    // A user named %1$s already exists in destination context %2$s.
    public static final String USER_ALREADY_EXISTS_MSG = "A user named %1$s already exists in destination context %2$s.";

    // The user's files are owned by user %1$s in source context %2$s. Please set individual or context-associated file storage first.
    public static final String FILE_STORAGE_CONFLICT_MSG = "The user's files are owned by user %1$s in source context %2$s. Please set individual or context-associated file storage first.";

    // The user %1$s in source context %2$s does use Unified Quota and therefore cannot be copied.
    public static final String UNIFIED_QUOTA_CONFLICT_MSG = "The user %1$s in source context %2$s does use Unified Quota and therefore cannot be copied.";

    private UserCopyExceptionMessages() {
        super();
    }

}
