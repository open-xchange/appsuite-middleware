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

import static com.openexchange.java.Autoboxing.i;
import com.openexchange.regional.RegionalSettingField;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.impl.service.RegionalSettingsImpl.Builder;

/**
 * {@link SerialiserUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
class SerialiserUtil {

    /**
     * Retrieves the value of the specified field from the specified settings object
     * 
     * @param regionalSettings The settings object
     * @param field The set field
     * @return The value of the field
     * @throws IllegalArgumentException if the specified field does not exist
     */
    static Object getField(RegionalSettings regionalSettings, RegionalSettingField field) {
        switch (field) {
            case DATE:
                return regionalSettings.getDateFormat();
            case DATE_FULL:
                return regionalSettings.getDateFormatFull();
            case DATE_LONG:
                return regionalSettings.getDateFormatLong();
            case DATE_MEDIUM:
                return regionalSettings.getDateFormatMedium();
            case DATE_SHORT:
                return regionalSettings.getDateFormatShort();
            case FIRST_DAY_OF_WEEK:
                return getWeekDay(regionalSettings.getFirstDayOfWeek());
            case FIRST_DAY_OF_YEAR:
                return regionalSettings.getFirstDayOfYear();
            case NUMBER:
                return regionalSettings.getNumberFormat();
            case TIME:
                return regionalSettings.getTimeFormat();
            case TIME_LONG:
                return regionalSettings.getTimeFormatLong();
            default:
                throw new IllegalArgumentException("No such field '" + field.getName() + "'");
        }
    }

    /**
     * Gets the {@link WeekDay}. If the specified number is <code>null</code>
     * then <code>null</code> is returned, otherwise the WeekDay is parsed.
     * If an invalid number is specified then {@link WeekDay#sunday} is
     * returned.
     * 
     * @param num The number of the weekday
     * @return The week day or <code>null</code> if the number is <code>null</code>
     */
    static WeekDay getWeekDay(Integer num) {
        if (num == null) {
            return null;
        }
        try {
            return WeekDay.getWeekDayByNumber(i(num));
        } catch (IllegalArgumentException e) {
            return WeekDay.sunday;
        }
    }

    /**
     * Sets the value of the specified {@link RegionalSettingField} to the specified instance
     * of the builder
     *
     * @param builder The builder
     * @param field The field to set
     * @param object The value of the field
     */
    static void setField(Builder builder, RegionalSettingField field, Object object) {
        switch (field) {
            case DATE:
                if (object instanceof String) {
                    builder.withDateFormat((String) object);
                }
                break;
            case DATE_FULL:
                if (object instanceof String) {
                    builder.withDateFormatFull((String) object);
                }
                break;
            case DATE_LONG:
                if (object instanceof String) {
                    builder.withDateFormatLong((String) object);
                }
                break;
            case DATE_MEDIUM:
                if (object instanceof String) {
                    builder.withDateFormatMedium((String) object);
                }
                break;
            case DATE_SHORT:
                if (object instanceof String) {
                    builder.withDateFormatShort((String) object);
                }
                break;
            case FIRST_DAY_OF_WEEK:
                if (WeekDay.class.isInstance(object)) {
                    builder.withFirstDayOfWeek(((WeekDay) object).getNumber());
                } else if (object instanceof String) {
                    WeekDay weekday;
                    try {
                        weekday = WeekDay.valueOf((String) object);
                    } catch (Exception e) {
                        weekday = WeekDay.sunday;
                    }
                    builder.withFirstDayOfWeek(weekday.getNumber());
                } else if (object instanceof Integer) {
                    builder.withFirstDayOfWeek(i((Integer) object));
                }
                break;
            case FIRST_DAY_OF_YEAR:
                if (object instanceof String) {
                    builder.withFirstDayOfYear(Integer.parseInt((String) object));
                } else if (object instanceof Integer) {
                    builder.withFirstDayOfYear(i((Integer) object));
                }
                break;
            case NUMBER:
                if (object instanceof String) {
                    builder.withNumberFormat((String) object);
                }
                break;
            case TIME:
                if (object instanceof String) {
                    builder.withTimeFormat((String) object);
                }
                break;
            case TIME_LONG:
                if (object instanceof String) {
                    builder.withTimeFormatLong((String) object);
                }
                break;
            default:
                throw new IllegalArgumentException("No such field '" + field.getName() + "'");
        }
    }
}
