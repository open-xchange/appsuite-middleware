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

package com.openexchange.regional.impl.service;

import static com.openexchange.java.Autoboxing.I;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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

    RegionalSettingsImpl(String timeFormat, String timeFormatLong, String dateFormat, String dateFormatShort, String dateFormatMedium, String dateFormatFull, String dateFormatLong, String numberFormat, Integer firstDayOfWeek, Integer firstDayOfYear) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateFormat == null) ? 0 : dateFormat.hashCode());
        result = prime * result + ((dateFormatFull == null) ? 0 : dateFormatFull.hashCode());
        result = prime * result + ((dateFormatLong == null) ? 0 : dateFormatLong.hashCode());
        result = prime * result + ((dateFormatMedium == null) ? 0 : dateFormatMedium.hashCode());
        result = prime * result + ((dateFormatShort == null) ? 0 : dateFormatShort.hashCode());
        result = prime * result + ((firstDayOfWeek == null) ? 0 : firstDayOfWeek.hashCode());
        result = prime * result + ((firstDayOfYear == null) ? 0 : firstDayOfYear.hashCode());
        result = prime * result + ((numberFormat == null) ? 0 : numberFormat.hashCode());
        result = prime * result + ((timeFormat == null) ? 0 : timeFormat.hashCode());
        result = prime * result + ((timeFormatLong == null) ? 0 : timeFormatLong.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegionalSettingsImpl other = (RegionalSettingsImpl) obj;
        if (dateFormat == null) {
            if (other.dateFormat != null)
                return false;
        } else if (!dateFormat.equals(other.dateFormat))
            return false;
        if (dateFormatFull == null) {
            if (other.dateFormatFull != null)
                return false;
        } else if (!dateFormatFull.equals(other.dateFormatFull))
            return false;
        if (dateFormatLong == null) {
            if (other.dateFormatLong != null)
                return false;
        } else if (!dateFormatLong.equals(other.dateFormatLong))
            return false;
        if (dateFormatMedium == null) {
            if (other.dateFormatMedium != null)
                return false;
        } else if (!dateFormatMedium.equals(other.dateFormatMedium))
            return false;
        if (dateFormatShort == null) {
            if (other.dateFormatShort != null)
                return false;
        } else if (!dateFormatShort.equals(other.dateFormatShort))
            return false;
        if (firstDayOfWeek == null) {
            if (other.firstDayOfWeek != null)
                return false;
        } else if (!firstDayOfWeek.equals(other.firstDayOfWeek))
            return false;
        if (firstDayOfYear == null) {
            if (other.firstDayOfYear != null)
                return false;
        } else if (!firstDayOfYear.equals(other.firstDayOfYear))
            return false;
        if (numberFormat == null) {
            if (other.numberFormat != null)
                return false;
        } else if (!numberFormat.equals(other.numberFormat))
            return false;
        if (timeFormat == null) {
            if (other.timeFormat != null)
                return false;
        } else if (!timeFormat.equals(other.timeFormat))
            return false;
        if (timeFormatLong == null) {
            if (other.timeFormatLong != null)
                return false;
        } else if (!timeFormatLong.equals(other.timeFormatLong))
            return false;
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
        Builder() {
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
            withFirstDayOfWeek(cal.getFirstDayOfWeek());
            withFirstDayOfYear(cal.getMinimalDaysInFirstWeek());
            return this;
        }

        public RegionalSettings build() {
            return new RegionalSettingsImpl(timeFormat, timeFormatLong, dateFormat, dateFormatShort, dateFormatMedium, dateFormatFull, dateFormatLong, numberFormat, firstDayOfWeek, firstDayOfYear);
        }
    }
}
