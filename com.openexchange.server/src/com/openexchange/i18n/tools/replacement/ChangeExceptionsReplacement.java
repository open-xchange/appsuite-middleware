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
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link ChangeExceptionsReplacement} - The replacement for change exceptions
 * of a recurring calendar object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ChangeExceptionsReplacement extends AbstractFormatMultipleDateReplacement {

    private boolean changeException;

    private String recurrenceTitle;

    /**
     * Initializes a new {@link ChangeExceptionsReplacement}
     *
     * @param dates The change exception dates
     */
    public ChangeExceptionsReplacement(final Date[] dates) {
        this(dates, null, null);
    }

    /**
     * Initializes a new {@link ChangeExceptionsReplacement}
     *
     * @param dates The change exception dates
     * @param locale The locale
     * @param timeZone The time zone
     */
    public ChangeExceptionsReplacement(final Date[] dates, final Locale locale, final TimeZone timeZone) {
        super(dates, Notifications.FORMAT_CHANGE_EXCEPTIONS, locale, timeZone);
        fallback = Notifications.NO_CHANGE_EXCEPTIONS;
    }

    @Override
    public String getReplacement() {
        if (dates == null || dates.length == 0) {
            return "";
        }
        // Get dates' replacement
        final String datesRepl;
        {
            final StringBuilder builder = new StringBuilder(dates.length << 4);
            builder.append(dateFormat.format(dates[0]));
            for (int i = 1; i < dates.length; i++) {
                builder.append(", ").append(dateFormat.format(dates[i]));
            }
            datesRepl = builder.toString();
        }
        if (changeException) {
            // Event denotes a change exception
            format = Notifications.FORMAT_CHANGE_EXCEPTION_OF;
            final String result = String.format(StringHelper.valueOf(locale == null ? Locale.ENGLISH : locale)
                    .getString(format), recurrenceTitle, datesRepl);
            return new StringBuilder(1 + result.length()).append(result).append('\n').toString();
        }
        // Normal replacement
        final String result = String.format(StringHelper.valueOf(locale == null ? Locale.ENGLISH : locale)
                .getString(format), datesRepl);
        if (changed) {
            return new StringBuilder(PREFIX_MODIFIED.length() + result.length() + 1).append(PREFIX_MODIFIED).append(
                    result).append('\n').toString();
        }
        return new StringBuilder(1 + result.length()).append(result).append('\n').toString();
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.CHANGE_EXCEPTIONS;
    }

    /**
     * Checks if associated event is a change exception.
     *
     * @return <code>true</code> if associated event is a change exception;
     *         otherwise <code>false</code>
     */
    public boolean isChangeException() {
        return changeException;
    }

    /**
     * Sets whether associated event is a change exception.
     *
     * @param changeException <code>true</code> if associated event is a change
     *            exception; otherwise <code>false</code>
     */
    public void setChangeException(final boolean changeException) {
        this.changeException = changeException;
    }

    /**
     * Gets the recurrence title.<br>
     * Only useful if {@link #isChangeException()} returns <code>true</code>.
     *
     * @return The recurrence title
     */
    public String getRecurrenceTitle() {
        return recurrenceTitle;
    }

    /**
     * Sets the recurrence title.<br>
     * Only useful if {@link #isChangeException()} returns <code>true</code>.
     *
     * @param recurrenceTitle The recurrence title
     */
    public void setRecurrenceTitle(final String recurrenceTitle) {
        this.recurrenceTitle = recurrenceTitle;
    }

}
