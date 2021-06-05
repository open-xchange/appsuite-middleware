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

/**
 * {@link WeekDay} a mapping of weekday names to weekday numbers
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public enum WeekDay {
    sunday(1),
    monday(2),
    thuesday(3),
    wednesday(4),
    thursday(5),
    friday(6),
    saturday(7);

    private int number;

    /**
     * Initializes a new {@link WeekDay}.
     */
    private WeekDay(int number) {
        this.number = number;
    }

    /**
     * Returns the {@link WeekDay} from the specified number
     * 
     * @param number The number of the weekday
     * @return The WeekDay
     * @throws IllegalArgumentException if an invalid weekday number is specified
     */
    public static WeekDay getWeekDayByNumber(int number) {
        if (number <= 0 || WeekDay.values().length < number) {
            throw new IllegalArgumentException("Invalid weekday number is specified: Valid numbers are: 1-7");
        }
        return WeekDay.values()[number - 1];
    }

    /**
     * Returns the number
     *
     * @return The weekday number
     */
    public int getNumber() {
        return number;
    }
}
