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
                if (object instanceof String) {
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
