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

package com.openexchange.mail.mime.processing;

import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.user.User;

/**
 * {@link LocaleAndTimeZone} - Helper class to pack up {@link Locale} and {@link TimeZone} combination.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class LocaleAndTimeZone {

    /**
     * The locale.
     */
    final Locale locale;

    /**
     * The time zone.
     */
    final TimeZone timeZone;

    /**
     * Initializes a new {@link LocaleAndTimeZone} from specified user.
     *
     * @param user The user
     */
    LocaleAndTimeZone(User user) {
        this(user.getLocale(), user.getTimeZone());
    }

    /**
     * Initializes a new {@link LocaleAndTimeZone}.
     *
     * @param locale The locale
     * @param timeZoneId The time zone ID
     */
    LocaleAndTimeZone(Locale locale, String timeZoneId) {
        this(locale, TimeZoneUtils.getTimeZone(timeZoneId));
    }

    /**
     * Initializes a new {@link LocaleAndTimeZone}.
     *
     * @param locale The locale
     * @param timeZone The time zone
     */
    LocaleAndTimeZone(Locale locale, TimeZone timeZone) {
        super();
        this.locale = locale;
        this.timeZone = timeZone;
    }

}
