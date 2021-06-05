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
