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
 *    conditions of the Creative Commons License, Version .5, Attribution,
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
 *     Copyright (C) 016-00 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version  as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 0111-1307 USA
 *
 */

package com.openexchange.regional.impl.service;

import static com.openexchange.java.Autoboxing.I;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.openexchange.regional.RegionalSettingField;
import com.openexchange.regional.RegionalSettings;

/**
 * {@link RegionalSettingsImpl2} contains regional settings for a user
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class RegionalSettingsImpl implements RegionalSettings {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static RegionalSettingsImpl.Builder newBuilder() {
        return new RegionalSettingsImpl.Builder();
    }

    private static final long serialVersionUID = -7398915075051830L;

    private final String timeFormat;
    private final String timeFormatLong;
    private final String dateFormat;
    private final String dateFormatShort;
    private final String dateFormatMedium;
    private final String dateFormatFull;
    private final String dateFormatLong;
    private final String numberFormat;
    private final Integer firstDayOfWeek;
    private final Integer firstDayOfYear;

    private RegionalSettingsImpl(String timeFormat, String timeFormatLong, String dateFormat, String dateFormatShort, String dateFormatMedium, String dateFormatFull, String dateFormatLong, String numberFormat, Integer firstDayOfWeek,
        Integer firstDayOfYear) {
        super();
        this.timeFormat = timeFormat;
        this.timeFormatLong = timeFormatLong;
        this.dateFormat = dateFormat;
        this.dateFormatShort = dateFormatShort;
        this.dateFormatMedium = dateFormatMedium;
        this.dateFormatFull = dateFormatFull;
        this.dateFormatLong = dateFormatLong;
        this.numberFormat = numberFormat;
        this.firstDayOfWeek = firstDayOfWeek;
        this.firstDayOfYear = firstDayOfYear;
    }

    @Override
    public String getTimeFormat() {
        return timeFormat;
    }

    @Override
    public String getTimeFormatLong() {
        return timeFormatLong;
    }

    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public String getDateFormatLong() {
        return dateFormatLong;
    }

    @Override
    public String getDateFormatShort() {
        return dateFormatShort;
    }

    @Override
    public String getDateFormatMedium() {
        return dateFormatMedium;
    }

    @Override
    public String getDateFormatFull() {
        return dateFormatFull;
    }

    @Override
    public String getNumberFormat() {
        return numberFormat;
    }

    @Override
    public Integer getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    @Override
    public Integer getFirstDayOfYear() {
        return firstDayOfYear;
    }

    @Override
    public boolean isFieldSet(RegionalSettingField field) {
        return true;
    }

    @Override
    public String toString() {
        return "RegionalSettingsImpl [ timeFormat=" + timeFormat + " | timeFormatLong=" + timeFormatLong + " | dateFormat=" + dateFormat + " | dateFormatLong=" + dateFormatLong + " | numberFormat=" + numberFormat + " | firstDayOfWeek=" + firstDayOfWeek + " | firstDayOfYear=" + firstDayOfYear + "]";
    }

    /**
     *
     * {@link Builder} is a builder for {@link RegionalSettingsImpl2}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.3
     */
    public static class Builder {

        private String timeFormat;
        private String timeFormatLong;
        private String dateFormat;
        private String dateFormatShort;
        private String dateFormatMedium;
        private String dateFormatFull;
        private String dateFormatLong;
        private String numberFormat;
        private Integer firstDayOfWeek;
        private Integer firstDayOfYear;

        /**
         * Initializes a new {@link RegionalSettingsImpl.Builder}.
         */
        private Builder() {
            super();
        }

        public Builder withTimeFormat(String timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }

        public Builder withTimeFormatLong(String timeFormatLong) {
            this.timeFormatLong = timeFormatLong;
            return this;
        }

        public Builder withDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public Builder withDateFormatShort(String dateFormatShort) {
            this.dateFormatShort = dateFormatShort;
            return this;
        }

        public Builder withDateFormatMedium(String dateFormatMedium) {
            this.dateFormatMedium = dateFormatMedium;
            return this;
        }

        public Builder withDateFormatFull(String dateFormatFull) {
            this.dateFormatFull = dateFormatFull;
            return this;
        }

        public Builder withDateFormatLong(String dateFormatLong) {
            this.dateFormatLong = dateFormatLong;
            return this;
        }

        public Builder withNumberFormat(String numberFormat) {
            this.numberFormat = numberFormat;
            return this;
        }

        public Builder withFirstDayOfWeek(int firstDayOfWeek) {
            this.firstDayOfWeek = I(firstDayOfWeek);
            return this;
        }

        public Builder withFirstDayOfYear(int firstDayOfYear) {
            this.firstDayOfYear = I(firstDayOfYear);
            return this;
        }

        public Builder withDefaults(Locale locale) {
            withNumberFormat(NumberFormat.getInstance(locale).format(1234.56));
            withDateFormat(((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, locale)).toPattern());
            withDateFormatLong(((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG, locale)).toPattern());
            withDateFormatShort(((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale)).toPattern());
            withDateFormatMedium(((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale)).toPattern());
            withDateFormatFull(((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, locale)).toPattern());
            withTimeFormat(((SimpleDateFormat) SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, locale)).toPattern());
            withTimeFormatLong(((SimpleDateFormat) SimpleDateFormat.getTimeInstance(SimpleDateFormat.DEFAULT, locale)).toPattern());
            Calendar cal = Calendar.getInstance(locale);
            withFirstDayOfWeek(I(cal.getFirstDayOfWeek()));
            withFirstDayOfYear(I(cal.getMinimalDaysInFirstWeek()));
            return this;
        }

        public RegionalSettings build() {
            return new RegionalSettingsImpl(timeFormat, timeFormatLong, dateFormat, dateFormatShort, dateFormatMedium, dateFormatFull, dateFormatLong, numberFormat, firstDayOfWeek, firstDayOfYear);
        }
    }
}
