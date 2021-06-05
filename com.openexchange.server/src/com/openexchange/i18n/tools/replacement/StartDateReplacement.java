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

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link StartDateReplacement} - Start date replacement
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class StartDateReplacement extends AbstractFormatDateReplacement {

    /**
     * Initializes a new {@link StartDateReplacement}
     *
     * @param startDate The start date
     * @param fulltime <code>true</code> if given start date denotes a full-time
     *            start date; otherwise <code>false</code>
     */
    public StartDateReplacement(final Date startDate, final boolean fulltime) {
        this(startDate, fulltime, null, null);
    }

    /**
     * Initializes a new {@link StartDateReplacement}
     *
     * @param startDate The start date
     * @param fulltime <code>true</code> if given start date denotes a full-time
     *            start date; otherwise <code>false</code>
     * @param locale The locale
     * @param timeZone The time zone
     */
    public StartDateReplacement(final Date startDate, final boolean fulltime, final Locale locale,
            final TimeZone timeZone) {
        super(startDate, !fulltime, Notifications.FORMAT_START_DATE, locale, timeZone);
        fallback = Notifications.NO_START_DATE;
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.START;
    }

}
