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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.calendar.printing;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public enum CalendarPrintingType {
    DAYVIEW("DayView", 0),
    WORKWEEKVIEW("WorkWeekView", 1),
    WEEKVIEW("WeekView", 2),
    MONTHLYVIEW("MonthlyView", 3),
    YEARLYVIEW("YearlyView", 4);

    private String name;

    private int number;

    CalendarPrintingType(String name, int number) {
        this.setName(name);
        this.setNumber(number);
    }


    /**
     * Gets an enum instance via its number. Null if not found.
     */
    public static CalendarPrintingType getByNumber(int number) {
        for (CalendarPrintingType type : values()) {
            if (type.getNumber() == number)
                return type;
        }
        return null;
    }

    /**
     * Gets an enum instance via its name. Case-agnostic. Null if not found.
     */
    public static CalendarPrintingType getByName(String name) {
        for (CalendarPrintingType type : values()) {
            if (name.equalsIgnoreCase(type.getName()))
                return type;
        }
        return null;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public void setNumber(int number) {
        this.number = number;
    }


    public int getNumber() {
        return number;
    }
}
