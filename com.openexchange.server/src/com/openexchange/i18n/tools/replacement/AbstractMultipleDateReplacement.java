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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.LocaleTools;
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
        this.dateFormat = locale == null ? DateFormat.getDateInstance(DateFormat.DEFAULT, LocaleTools.DEFAULT_LOCALE) : DateFormat
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

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }
}
