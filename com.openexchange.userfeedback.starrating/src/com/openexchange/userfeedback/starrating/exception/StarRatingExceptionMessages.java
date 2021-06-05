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

package com.openexchange.userfeedback.starrating.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * 
 * {@link StarRatingExceptionMessages}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public final class StarRatingExceptionMessages implements LocalizableStrings {

    // The provided feedback score is invalid. Please choose a number > 0.
    public static final String SCORE_INVALID_MSG = "The provided feedback score is invalid. Please choose a number > 0.";

    // The provided feedback for key 'score' has an invalid type.
    public static final String SCORE_INVALID_TYPE_MSG = "The provided feedback for key 'score' has an invalid type.";

    // The provided feedback does not contain required key "score".
    public static final String KEY_MISSING_MSG = "The provided feedback does not contain required key '%s'.";

    // The provided feedback for key "score" cannot be parsed..
    public static final String BAD_VALUE_MSG = "The provided feedback for key '%s' cannot be parsed.";

    private StarRatingExceptionMessages() {
        super();
    }
}
