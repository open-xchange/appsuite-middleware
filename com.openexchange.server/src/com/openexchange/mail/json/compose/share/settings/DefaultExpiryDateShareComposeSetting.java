/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail.json.compose.share.settings;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.ShareComposeHandler;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;


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
