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

package com.openexchange.calendar.printing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public enum CPType {
    DAYVIEW("DayView", 0),
    WORKWEEKVIEW("WorkWeekView", 1),
    WEEKVIEW("WeekView", 2),
    MONTHLYVIEW("MonthView", 3),
    YEARLYVIEW("YearView", 4);

    private String name;

    private int number;

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

    CPType(String name, int number) {
        this.setName(name);
        this.setNumber(number);
    }

    /**
     * Gets an enum instance via its number. Null if not found.
     */
    public static CPType getByNumber(int number) {
        for (CPType type : values()) {
            if (type.getNumber() == number) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets an enum instance via its name. Case-agnostic. Null if not found.
     */
    public static CPType getByName(String name) {
        for (CPType type : values()) {
            if (name.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }

    public static CPType getByTemplateName(String string) {
        Matcher matcher = Pattern.compile("cp_([^_]+)_.+$").matcher(string);
        if(! matcher.find()) {
            return null;
        }
        CPType find;

        String identifier = matcher.group(1);
        find = getByName(identifier);
        try {
        if(find == null) {
            find = getByNumber(Integer.valueOf(identifier).intValue());
        }
        } catch(NumberFormatException e){
            return null;
        }
        return find;
    }
}
