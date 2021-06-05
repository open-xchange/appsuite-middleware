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

package com.openexchange.chronos.provider.ical.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 *
 * {@link ICalProviderExceptionMessages}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalProviderExceptionMessages implements LocalizableStrings {

    public static final String MISSING_FEED_URI_MSG = "The feed URI is missing.";

    public static final String BAD_FEED_URI_MSG = "The given feed URI is invalid. Please change it and try again.";

    public static final String FEED_URI_NOT_ALLOWED_MSG = "Cannot connect to feed with URI: %1$s. Please change it and try again.";

    public static final String NO_FEED_MSG = "The provided URI %1$s does not contain content as expected. Please change it and try again.";

    public static final String NOT_ALLOWED_CHANGE_MSG = "The field %1$s cannot be changed.";

    public static final String BAD_PARAMETER_MSG = "The field '%1$s' contains an unexpected value '%2$s'";

    public static final String FEED_SIZE_EXCEEDED_MSG = "Unfortunately your requested feed cannot be subscribed due to size limitations.";

    public static final String UNEXPECTED_FEED_ERROR_MSG = "Unfortunately the given feed URI cannot be processed as expected.";

    public static final String REMOTE_SERVICE_UNAVAILABLE_MSG = "The remote service is unavailable at the moment. There is nothing we can do about it. Please try again later.";

    public static final String REMOTE_INTERNAL_SERVER_ERROR_MSG = "An internal server error occurred on the feed provider side. There is nothing we can do about it.";

    public static final String REMOTE_SERVER_ERROR_MSG = "A remote server error occurred on the feed provider side. There is nothing we can do about it.";

    public static final String CREDENTIALS_REQUIRED_MSG = "Access to this calendar is restricted. Please enter your credentials and try again.";

    public static final String CREDENTIALS_WRONG_MSG = "Authentication failed. Please enter your credentials and try again.";

    public static final String PASSWORD_REQUIRED_MSG = "Access to this calendar is restricted. Please enter your password and try again.";

    public static final String PASSWORD_WRONG_MSG = "Authentication failed. Please enter your password and try again.";

    public static final String CREDENTIALS_CHANGED_MSG = "Authentication failed due to a recent credentials change. Please remove the account and add it again with correct credentials.";

    /**
     * Initializes a new {@link ICalProviderExceptionMessages}.
     */
    private ICalProviderExceptionMessages() {
        super();
    }
}
