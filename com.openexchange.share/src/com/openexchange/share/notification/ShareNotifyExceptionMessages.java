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

package com.openexchange.share.notification;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareNotifyExceptionMessages}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ShareNotifyExceptionMessages implements LocalizableStrings {

    public static final String INVALID_MAIL_ADDRESS_MSG = "\"%1$s\" is not a valid email address.";

    public static final String MISSING_MAIL_ADDRESS_MSG = "No notification mail could be sent to user \"%1$s\". We don't know his email address.";

    public static final String UNEXPECTED_ERROR_FOR_RECIPIENT_MSG = "An error occurred, we were unable to send an email to \"%2$s\".";

    public static final String UNEXPECTED_ERROR_MSG = "An error occurred, we were unable to send out the notification mails.";

    public static final String INSUFFICIENT_PERMISSIONS_MSG = "You don't have sufficient permissions to send notifications for this share.";

}
