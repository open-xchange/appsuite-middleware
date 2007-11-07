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

package com.openexchange.groupware.calendar;

/**
 * CalendarCommonCollection
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class RecurringResults {
    
    private static final int DEFAULT_SIZE = 4;
    private RecurringResult recurring_result[];
    private int counter;
    
    
    public RecurringResults() {
        createRecurringRepository(DEFAULT_SIZE);
    }
    
    private final void createRecurringRepository(int size) {
        if (size < 1) {
            size = DEFAULT_SIZE;
        }
        recurring_result = new RecurringResult[size];
    }
    
    public final void add(final RecurringResult rr) {
        //CalendarCommonCollection.debugRecurringResult(rr); // uncomment this in runtime edition
        if (counter == recurring_result.length) {
            final RecurringResult new_recurring_result[] = new RecurringResult[recurring_result.length*2];
            System.arraycopy(recurring_result, 0, new_recurring_result, 0, counter);
            recurring_result = new_recurring_result;
        }
        recurring_result[counter] = rr;
        counter++;
    }
    
    public final RecurringResult getRecurringResultByPosition(final int recurring_position) {
        if (recurring_position <= counter) {
            final int internal_position = recurring_position-1;
            if (recurring_result[internal_position] != null) {
                if (recurring_result[internal_position].getPosition() == recurring_position)  {
                    return recurring_result[internal_position];
                }
                for (int a = 0; a < counter; a++) {
                    if (recurring_result[a].getPosition() == recurring_position) {
                        return recurring_result[a];
                    }
                }
            }
        }
        return null;
    }
    
    public final RecurringResult getRecurringResult(final int position) {
        if (position <= counter && position >= 0) {
            return recurring_result[position];
        }
        return null;
    }
    
    public final int size() {
        return counter;
    }
    
    final int getPositionByLong(final long l) {
        for (int a = 0; a < counter; a++) {
            if (recurring_result[a].getNormalized() == l) {
                return recurring_result[a].getPosition();
            }
        }
        return -1;
    }
    
}
