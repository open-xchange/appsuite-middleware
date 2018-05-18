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

package com.openexchange.chronos.common;

import java.util.Comparator;
import java.util.TimeZone;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrenceIdComparator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RecurrenceIdComparator implements Comparator<RecurrenceId> {

    /** A default comparator using the <code>UTC</code> timezone when comparing <i>floating</i> recurrence identifiers */
    public static RecurrenceIdComparator DEFAULT_COMPARATOR = new RecurrenceIdComparator();

    private final TimeZone timeZone;

    /**
     * Initializes a new {@link RecurrenceIdComparator}.
     *
     * @param timeZone The timezone to consider for <i>floating</i> recurrence ids, i.e. the actual 'perspective' of the comparison, or
     *            <code>null</code> to fall back to UTC
     */
    public RecurrenceIdComparator(TimeZone timeZone) {
        super();
        this.timeZone = timeZone;
    }

    /**
     * Initializes a new {@link RecurrenceIdComparator}.
     * <p>/
     * <i>Floating</i> recurrence ids are compared from an UTC timezone perspective.
     */
    public RecurrenceIdComparator() {
        this(TimeZones.UTC);
    }

    @Override
    public int compare(RecurrenceId recurrenceId1, RecurrenceId recurrenceId2) {
        if (null == recurrenceId1) {
            return null == recurrenceId2 ? 0 : -1;
        }
        if (null == recurrenceId2) {
            return 1;
        }
        return CalendarUtils.compare(recurrenceId1.getValue(), recurrenceId2.getValue(), timeZone);
    }

}
