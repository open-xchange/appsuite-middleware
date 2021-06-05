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

package com.openexchange.userfeedback.nps.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * 
 * {@link NPSExceptionMessages}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public final class NPSExceptionMessages implements LocalizableStrings {

    // The provided feedback for '%s' is invalid.
    public static final String INVALID_VALUE_MSG = "The provided feedback for '%s' is invalid.";

    // The provided feedback for key '%s' has an invalid type.
    public static final String INVALID_TYPE_MSG = "The provided feedback for key '%s' has an invalid type.";

    // The provided feedback does not contain required key '%s'.
    public static final String KEY_MISSING_MSG = "The provided feedback does not contain required key '%s'.";

    // The provided feedback for key '%s' cannot be parsed.
    public static final String BAD_VALUE_MSG = "The provided feedback for key '%s' cannot be parsed.";

    private NPSExceptionMessages() {
        super();
    }
}
