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

package com.openexchange.ajax.chronos.factory;

import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.EventFactory.Weekday;
import com.openexchange.testing.httpclient.models.DateTimeData;

/**
 * {@link RRuleFactory}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class RRuleFactory {

    /**
     * Creates a recurrence rule with the given frequency limited by the given amount of occurences
     *
     * @param freq The {@link RecurringFrequency}
     * @param occurences The amount of occurences
     * @return The recurrence rule
     */
    public static String getFrequencyWithOccurenceLimit(RecurringFrequency freq, int occurences ) {
        return "FREQ=" + freq.name() + ";COUNT=" + occurences;
    }

    /**
     * Creates a recurrence rule with the given frequency limited by the given amount of occurences
     *
     * @param freq The {@link RecurringFrequency}
     * @param occurences The amount of occurences
     * @param weekday The byday value
     * @return The recurrence rule
     */
    public static String getFrequencyWithOccurenceLimit(RecurringFrequency freq, int occurences, Weekday weekday) {
        return "FREQ=" + freq.name() + ";BYDAY=" + weekday.name() + ";COUNT=" + occurences;
    }

    /**
     * Creates a recurrence rule with the given frequency.
     *
     * @param freq The {@link RecurringFrequency}
     * @return The recurrence rule
     */
    public static String getFrequencyWithoutLimit(RecurringFrequency freq ) {
        return "FREQ=" + freq.name();
    }


    /**
     * Creates a recurrence rule with the given frequency limited by the given until date
     *
     * @param freq The {@link RecurringFrequency}
     * @param until The limiting date
     * @return The recurrence rule
     */
    public static String getFrequencyWithUntilLimit(RecurringFrequency freq, DateTimeData until ) {
        return getFrequencyWithUntilLimit(freq, until, null);
    }

    /**
     * Creates a recurrence rule with the given frequency limited by the given until date. Optionally a BYDAY value can be included as well
     *
     * @param freq The {@link RecurringFrequency}
     * @param until The limiting date
     * @param optWeekday An optional weekday for the BYDAY value
     * @return The recurrence rule
     */
    public static String getFrequencyWithUntilLimit(RecurringFrequency freq, DateTimeData until, Weekday optWeekday ) {
        if(optWeekday != null) {
            return "FREQ=" + freq.name() + ";BYDAY=" + optWeekday.name() + ";UNTIL=" + until.getValue();
        }
        return "FREQ=" + freq.name() + ";UNTIL=" + until.getValue();
    }



}
