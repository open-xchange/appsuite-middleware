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

package com.openexchange.sms.tools;

import com.openexchange.i18n.LocalizableStrings;

/**
 *
 * {@link SMSBucketExceptionMessages}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class SMSBucketExceptionMessages implements LocalizableStrings {

    /**
     * Prevent instantiation.
     */
    private SMSBucketExceptionMessages() {
        super();
    }

    // You have exceeded the maximum number of sms allowed. Please try again after %1$s hours
    public static final String SMS_LIMIT_REACHED_MSG = "You have exceeded the maximum number of sms allowed. Please try again after %1$s hours";

    // SMS limits apply. You can only send one more message in the next %1$s hours
    public static final String NEXT_TO_LAST_SMS_SENT = "SMS limits apply. You can only send one more message in the next %1$s hours";

}
