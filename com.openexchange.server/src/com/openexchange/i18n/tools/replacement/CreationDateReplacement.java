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

package com.openexchange.i18n.tools.replacement;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link CreationDateReplacement} - Creation date replacement.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class CreationDateReplacement extends AbstractDateReplacement {

    /**
     * Initializes a new {@link CreationDateReplacement}
     *
     * @param creationDate The creation date
     * @param locale The locale
     */
    public CreationDateReplacement(final Date creationDate, final Locale locale) {
        this(creationDate, locale, null);
    }

    /**
     * Initializes a new {@link CreationDateReplacement}
     *
     * @param creationDate The creation date
     * @param locale The locale
     * @param timeZone The time zone
     */
    public CreationDateReplacement(final Date creationDate, final Locale locale, final TimeZone timeZone) {
        super(trimDateToMinutesOnly(creationDate), true, locale, timeZone);
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.CREATION_DATETIME;
    }

    private static Date trimDateToMinutesOnly(final Date d) {
        if (d == null) {
            return d;
        }
        final Calendar helper = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        helper.setTime(d);
        helper.set(Calendar.SECOND, 0);
        helper.set(Calendar.MILLISECOND, 0);
        return helper.getTime();
    }
}
