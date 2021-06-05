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

package com.openexchange.chronos.scheduling;

import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.regional.RegionalSettings;

/**
 * {@link RecipientSettings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public interface RecipientSettings {

    /**
     * Gets the recipient.
     *
     * @return The recipient
     */
    CalendarUser getRecipient();

    /**
     * Gets the calendar user type of the recipient.
     *
     * @return The calendar user type of the recipient
     */
    CalendarUserType getRecipientType();

    /**
     * Gets a value indicating the preferred message format for the recipient.
     * <p>
     * The returned <code>int</code> value is wither <code>1</code> (text only), <code>2</code> (HTML only), or <code>3</code> (both).
     *
     * @return The desired message format
     * @see com.openexchange.mail.usersetting.UserSettingMail#getMsgFormat()
     */
    int getMsgFormat();

    /**
     * Gets the preferred locale to use for the recipient.
     *
     * @return The preferred locale
     */
    Locale getLocale();

    /**
     * Gets the preferred timezone to use for the recipient.
     *
     * @return The preferred timezone
     */
    TimeZone getTimeZone();

    /**
     * Gets customized regional settings to use for the recipient, if configured.
     *
     * @return The preferred regional settings, or <code>null</code> if not configured
     */
    RegionalSettings getRegionalSettings();

    /**
     * Gets a direct link to a specific event, from the recipient point of view.
     *
     * @param event The event to generate the link for
     * @return The direct link, or <code>null</code> if not applicable
     */
    String getDirectLink(Event event);

}
