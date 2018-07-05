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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyUtils}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class FreeBusyUtils {

    /**
     * Combines the free/busy times of multiple free/busy results.
     *
     * @param freeBusyResults The free/busy results to combine
     * @return The combined free/busy result
     */
    public static FreeBusyResult combine(List<FreeBusyResult> freeBusyResults) {
        if (null == freeBusyResults || freeBusyResults.isEmpty()) {
            return null;
        }
        if (1 == freeBusyResults.size()) {
            return freeBusyResults.get(0);
        }
        List<OXException> warnings = new ArrayList<OXException>();
        List<FreeBusyTime> freeBusyTimes = new ArrayList<FreeBusyTime>();
        for (FreeBusyResult result : freeBusyResults) {
            if (null != result.getWarnings()) {
                warnings.addAll(result.getWarnings());
            }
            if (null != result.getFreeBusyTimes()) {
                freeBusyTimes.addAll(result.getFreeBusyTimes());
            }
        }
        Collections.sort(freeBusyTimes);
        return new FreeBusyResult(freeBusyTimes, warnings);
    }

    /**
     * Merges the free/busy times of multiple free/busy results.
     *
     * @param freeBusyResults The free/busy results to merge
     * @return The merged free/busy result
     */
    public static FreeBusyResult merge(List<FreeBusyResult> freeBusyResults) {
        if (null == freeBusyResults || freeBusyResults.isEmpty()) {
            return null;
        }
        if (1 == freeBusyResults.size()) {
            return freeBusyResults.get(0);
        }
        FreeBusyResult combinedResult = combine(freeBusyResults);
        if (null != combinedResult) {
            combinedResult.setFreeBusyTimes(mergeFreeBusy(combinedResult.getFreeBusyTimes()));
        }
        return combinedResult;
    }

    /**
     * Normalizes the given free/busy intervals. This means
     * <ul>
     * <li>the intervals are sorted chronologically, i.e. the earliest interval is first</li>
     * <li>overlapping intervals are merged so that only the most conflicting ones of overlapping time ranges are used</li>
     * </ul>
     *
     * @param freeBusyTimes The free/busy-times to merge
     * @return The merged free/busy times
     */
    public static List<FreeBusyTime> mergeFreeBusy(List<FreeBusyTime> freeBusyTimes) {
        if (null == freeBusyTimes) {
            return Collections.emptyList(); // nothing to do
        }
        if (2 > freeBusyTimes.size()) {
            return freeBusyTimes; // nothing more to do
        }
        /*
         * expand times to match all possible boundaries
         */
        Date[] times = getTimes(freeBusyTimes);
        ArrayList<FreeBusyTime> expandedIntervals = new ArrayList<FreeBusyTime>();
        for (FreeBusyTime freeBusyTime : freeBusyTimes) {
            List<Date> expandedTimes = new ArrayList<Date>();
            expandedTimes.add(freeBusyTime.getStartTime());
            for (Date time : times) {
                if (freeBusyTime.getStartTime().before(time) && freeBusyTime.getEndTime().after(time)) {
                    expandedTimes.add(time);
                }
            }
            expandedTimes.add(freeBusyTime.getEndTime());
            if (2 == expandedTimes.size()) {
                expandedIntervals.add(freeBusyTime);
            } else {
                for (int i = 0; i < expandedTimes.size() - 1; i++) {
                    expandedIntervals.add(new FreeBusyTime(freeBusyTime.getFbType(), expandedTimes.get(i), expandedTimes.get(i + 1)));
                }
            }
        }
        /*
         * condense all overlapping intervals to most conflicting one
         */
        Collections.sort(expandedIntervals);
        ArrayList<FreeBusyTime> mergedTimes = new ArrayList<FreeBusyTime>();
        Iterator<FreeBusyTime> iterator = expandedIntervals.iterator();
        FreeBusyTime current = iterator.next();
        while (iterator.hasNext()) {
            FreeBusyTime next = iterator.next();
            if (current.getStartTime().equals(next.getStartTime()) && current.getEndTime().equals(next.getEndTime())) {
                if (0 > current.getFbType().compareTo(next.getFbType())) {
                    /*
                     * less conflicting than next time, skip current timeslot
                     */
                    current = next;
                }
                continue;
            }
            mergedTimes.add(current);
            current = next;
        }
        mergedTimes.add(current);
        /*
         * expand consecutive intervals again
         */
        iterator = mergedTimes.iterator();
        while (iterator.hasNext()) {
            FreeBusyTime freeBusyTime = iterator.next();
            for (FreeBusyTime mergedTime : mergedTimes) {
                if (mergedTime.getFbType().getValue().equals(freeBusyTime.getFbType().getValue())) {
                    /*
                     * merge if next to another
                     */
                    if (mergedTime.getStartTime().equals(freeBusyTime.getEndTime())) {
                        mergedTime.setStartTime(freeBusyTime.getStartTime());
                        mergedTime.setEvent(null);
                        iterator.remove();
                        break;
                    } else if (mergedTime.getEndTime().equals(freeBusyTime.getStartTime())) {
                        mergedTime.setEndTime(freeBusyTime.getEndTime());
                        mergedTime.setEvent(null);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        /*
         * take over sorted intervals
         */
        return mergedTimes;
    }

    private static Date[] getTimes(List<FreeBusyTime> freeBusyTimes) {
        SortedSet<Date> times = new TreeSet<Date>();
        for (FreeBusyTime freeBusyTime : freeBusyTimes) {
            times.add(freeBusyTime.getStartTime());
            times.add(freeBusyTime.getEndTime());
        }
        return times.toArray(new Date[times.size()]);
    }

}
