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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.tools.TemplateReplacement;

/**
 * {@link AbstractMultipleDateReplacement} - An abstract class for date string
 * replacements using {@link DateFormat#format(Date)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public abstract class AbstractMultipleDateReplacement implements TemplateReplacement {

    private static final String PAT_ZONE = ", z";

    protected Date[] dates;

    protected boolean changed;

    protected DateFormat dateFormat;

    protected Locale locale;

    protected TimeZone timeZone;

    /**
     * Initializes a new {@link AbstractMultipleDateReplacement}
     *
     * @param dates The dates
     */
    protected AbstractMultipleDateReplacement(final Date[] dates) {
        this(dates, null, null);
    }

    /**
     * Initializes a new {@link AbstractMultipleDateReplacement}
     *
     * @param dates The dates
     * @param locale The locale
     * @param timeZone The time zone; may be <code>null</code>
     *
     */
    protected AbstractMultipleDateReplacement(final Date[] dates, final Locale locale, final TimeZone timeZone) {
        super();
        if (dates == null) {
            this.dates = null;
        } else {
            this.dates = new Date[dates.length];
            for (int i = 0; i < dates.length; i++) {
                this.dates[i] = (Date) dates[i].clone();
            }
        }
        this.dateFormat = locale == null ? DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH) : DateFormat
                .getDateInstance(DateFormat.DEFAULT, locale);
        if (timeZone != null) {
            if (dateFormat instanceof SimpleDateFormat) {
                /*
                 * Extend pattern to contain time zone information
                 */
                final SimpleDateFormat simpleDateFormat = (SimpleDateFormat) dateFormat;
                final String pattern = simpleDateFormat.toPattern();
                simpleDateFormat.applyPattern(new StringBuilder(pattern.length() + 3).append(pattern).append(PAT_ZONE)
                        .toString());

            }
            this.timeZone = timeZone;
            this.dateFormat.setTimeZone(timeZone);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final AbstractMultipleDateReplacement clone = (AbstractMultipleDateReplacement) super.clone();
        if (dates == null) {
            clone.dates = null;
        } else {
            clone.dates = new Date[dates.length];
            for (int i = 0; i < dates.length; i++) {
                clone.dates[i] = (Date) dates[i].clone();
            }
        }
        clone.dateFormat = (DateFormat) (this.dateFormat == null ? null : dateFormat.clone());
        clone.locale = (Locale) (this.locale == null ? null : this.locale.clone());
        clone.timeZone = (TimeZone) (this.timeZone == null ? null : timeZone.clone());
        return clone;
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    @Override
    public final boolean changed() {
        return changed;
    }

    @Override
    public boolean relevantChange() {
        return changed();
    }

    @Override
    public final TemplateReplacement setChanged(final boolean changed) {
        this.changed = changed;
        return this;
    }

    /**
     * Gets the replacement for associated dates template or an empty string if
     * applied {@link Date} array is <code>null</code> or empty.
     */
    @Override
    public String getReplacement() {
        if (dates == null || dates.length == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(dates.length << 4);
        builder.append(dateFormat.format(dates[0]));
        for (int i = 1; i < dates.length; i++) {
            builder.append(", ").append(dateFormat.format(dates[i]));
        }
        return builder.toString();
    }

    /**
     * Applies given time zone to this replacement.
     * <p>
     * If given time zone is <code>null</code>, it is treated as a no-op.
     *
     * @param timeZone The new time zone to apply
     * @return This replacement with new time zone applied
     */
    @Override
    public final TemplateReplacement setTimeZone(final TimeZone timeZone) {
        applyTimeZone(timeZone);
        return this;
    }

    /**
     * Applies given locale to this replacement.
     * <p>
     * If given locale is <code>null</code>, it is treated as a no-op.
     *
     * @param locale The new locale to apply
     * @return This replacement with new locale applied
     */
    @Override
    public final TemplateReplacement setLocale(final Locale locale) {
        applyLocale(locale);
        return this;
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!AbstractMultipleDateReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (!getToken().equals(other.getToken())) {
            /*
             * Token mismatch
             */
            return false;
        }
        if (!other.changed()) {
            /*
             * Other replacement does not reflect a changed value; leave
             * unchanged
             */
            return false;
        }
        final AbstractMultipleDateReplacement o = (AbstractMultipleDateReplacement) other;
        final Date[] oDates = o.dates;
        if (oDates == null) {
            dates = null;
        } else {
            dates = new Date[oDates.length];
            for (int i = 0; i < oDates.length; i++) {
                dates[i] = (Date) oDates[i].clone();
            }
        }
        this.changed = true;
        return true;
    }

    private void applyLocale(final Locale locale) {
        if (locale == null || locale.equals(this.locale)) {
            return;
        }
        this.locale = locale;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        if (this.timeZone != null) {
            this.dateFormat.setTimeZone(timeZone);
        }

    }

    private void applyTimeZone(final TimeZone timeZone) {
        if (timeZone == null || timeZone.equals(this.timeZone)) {
            return;
        }
        this.timeZone = timeZone;
        this.dateFormat.setTimeZone(timeZone);
    }
}
