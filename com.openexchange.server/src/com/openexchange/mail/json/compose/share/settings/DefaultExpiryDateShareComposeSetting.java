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

package com.openexchange.mail.json.compose.share.settings;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.ShareComposeHandler;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.user.User;


/**
 * This setting is used to announce the default expiry date for new Drive Mails to clients.
 * If not set explicitly, no expiry date is pre-selected except if com.openexchange.mail.compose.share.requiredExpiration
 * is true. In that case the last (highest) value of com.openexchange.mail.compose.share.expiryDates is chosen.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.4
 */
public class DefaultExpiryDateShareComposeSetting extends AbstractShareComposeSetting<String> {

    /**
     * Initializes a new {@link DefaultExpiryDateShareComposeSetting}.
     */
    public DefaultExpiryDateShareComposeSetting(ShareComposeHandler shareComposeHandler) {
        super("defaultExpiryDate", shareComposeHandler);
    }

    @Override
    protected String getSettingValue(Session session, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        String[] expiryDates = getExpiryDates(session);
        String defaultExpiryDate = Utilities.getValueFromProperty(PROPERTY_DEFAULT_EXPIRY_DATE, null, session);
        boolean requiresExpiration = Boolean.valueOf(Utilities.getBoolFromProperty(PROPERTY_REQUIRED_EXPIRATION, false, session)).booleanValue();
        if (requiresExpiration && Strings.isEmpty(defaultExpiryDate)) {
            defaultExpiryDate = getHighestExpiryDate(expiryDates);
        }

        if (Strings.isNotEmpty(defaultExpiryDate) && !Arrays.contains(expiryDates, defaultExpiryDate)) {
            String tmp = null;
            if (requiresExpiration) {
                tmp = getHighestExpiryDate(expiryDates);
            }

            LOG.warn("Value '{}' for property '{}' is not defined in '{}'. Falling back to default: {}", defaultExpiryDate, PROPERTY_DEFAULT_EXPIRY_DATE, PROPERTY_EXPIRY_DATES, tmp);
            defaultExpiryDate = tmp;
        }

        return defaultExpiryDate;
    }

    private static String getHighestExpiryDate(String[] expiryDates) {
        // We expect ascending order of values according to the properties documentation; return the highest one here
        return expiryDates[expiryDates.length - 1];
    }

}
