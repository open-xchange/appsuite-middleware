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
 *    trademarks of the OX Software GmbH group of companies.
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
