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
 * {@link DeleteExceptionsReplacement} - The replacement for delete exceptions
 * of a recurring calendar object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class DeleteExceptionsReplacement extends AbstractFormatMultipleDateReplacement {

    /**
     * Initializes a new {@link DeleteExceptionsReplacement}
     *
     * @param dates The delete exception dates
     */
    public DeleteExceptionsReplacement(final Date[] dates) {
        this(dates, null, null);
    }

    /**
     * Initializes a new {@link DeleteExceptionsReplacement}
     *
     * @param dates The delete exception dates
     * @param locale The locale
     * @param timeZone The time zone
     */
    public DeleteExceptionsReplacement(final Date[] dates, final Locale locale, final TimeZone timeZone) {
        super(dates, Notifications.FORMAT_DELETE_EXCEPTIONS, locale, timeZone);
        fallback = Notifications.NO_DELETE_EXCEPTIONS;
    }

    @Override
    public String getReplacement() {
        if (dates == null || dates.length == 0) {
            return "";
        }
        final String repl = super.getReplacement();
        return new StringBuilder(repl.length() + 1).append(repl).append('\n').toString();
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.DELETE_EXCEPTIONS;
    }

}
