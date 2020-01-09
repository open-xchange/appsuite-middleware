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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.chronos.common.CalendarUtils.shiftToUTC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.exception.OXException;

/**
 * {@link RecurrenceIdListMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class RecurrenceIdListMapping<O> extends VarCharListMapping<O> {

    /**
     * Initializes a new {@link RecurrenceIdListMapping}.
     *
     * @param columnLabel The label of the column holding the value
     * @param readableName The readable name for the mapped field
     */
    protected RecurrenceIdListMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    protected abstract SortedSet<RecurrenceId> getRecurrenceIds(O object);

    protected abstract void setRecurrenceIds(O object, SortedSet<RecurrenceId> value);

    @Override
    public void set(O object, List<String> value) throws OXException {
        if (null == value) {
            setRecurrenceIds(object, null);
        } else {
            setRecurrenceIds(object, deserialize(value));
        }
    }

    @Override
    public List<String> get(O object) {
        SortedSet<RecurrenceId> recurrenceIds = getRecurrenceIds(object);
        if (null == recurrenceIds) {
            return null;
        }
        ArrayList<String> value = new ArrayList<String>(recurrenceIds.size());
        for (RecurrenceId recurrenceId : recurrenceIds) {
            value.add(shiftToUTC(recurrenceId.getValue()).toString());
        }
        return value;
    }

    private static SortedSet<RecurrenceId> deserialize(List<String> values) {
        if (null == values) {
            return null;
        }
        DateTime[] dateTimes = decode(values);
        if (0 == dateTimes.length) {
            return Collections.emptySortedSet();
        }
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
        recurrenceIds.add(new DefaultRecurrenceId(shiftToUTC(dateTimes[0])));
        if (1 == dateTimes.length) {
            return recurrenceIds;
        }
        /*
         * check if first timezone is applicable for all values ("normalized" recurrence ids)
         */
        if (false == dateTimes[0].isAllDay() && null != dateTimes[0].getTimeZone() && false == "UTC".equals(dateTimes[0].getTimeZone().getID())) {
            boolean considerNormalized = true;
            for (int i = 1; i < dateTimes.length; i++) {
                if (null != dateTimes[i].getTimeZone() && false == dateTimes[i].isAllDay()) {
                    considerNormalized = false;
                    break;
                }
            }
            if (considerNormalized) {
                for (int i = 1; i < dateTimes.length; i++) {
                    recurrenceIds.add(new DefaultRecurrenceId(shiftToUTC(dateTimes[i].swapTimeZone(dateTimes[0].getTimeZone()))));
                }
                return recurrenceIds;
            }
        }
        /*
         * return set of individual recurrence ids, otherwise
         */
        for (int i = 1; i < dateTimes.length; i++) {
            recurrenceIds.add(new DefaultRecurrenceId(shiftToUTC(dateTimes[i])));
        }
        return recurrenceIds;
    }

    private static DateTime[] decode(List<String> values) {
        if (null == values) {
            return null;
        }
        DateTime[] dateTimes = new DateTime[values.size()];
        for (int i = 0; i < values.size(); i++) {
            dateTimes[i] = CalendarUtils.decode(values.get(i));
        }
        return dateTimes;
    }

}
