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

package com.openexchange.multifactor.provider.sms.impl;

import java.util.Objects;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.multifactor.MultifactorRequest;

/**
 * {@link SMSMessageCreator} creates SMS messages containing a secret multifactor token
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class SMSMessageCreator {

    /**
     * Creates a new, localized message containing the secret multifactor token
     *
     * @param multifactorRequest The request containing the locale for localization
     * @param token The secret token to include in the message
     * @return The localized message containing the secret multifactor token
     */
    public static String createMessage(MultifactorRequest multifactorRequest, String token) {
        Objects.requireNonNull(multifactorRequest, "session must not be null");
        StringBuilder sb = new StringBuilder();
        sb.append(StringHelper.valueOf(multifactorRequest.getLocale()).getString(MultifactorSMSStrings.MULTIFACTOR_SMS_TEXT));
        sb.append(token);
        return sb.toString();
    }
}
