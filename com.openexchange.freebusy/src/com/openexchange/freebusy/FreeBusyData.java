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

package com.openexchange.freebusy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyData}
 *
 * Data structure hosting a user's or resource's free/busy intervals.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyData {

    protected String participant;
    protected Date from;
    protected Date until;
    protected List<OXException> warnings;
    protected List<FreeBusyInterval> intervals;

    /**
     * Initializes a new {@link FreeBusyData}.
     */
    public FreeBusyData() {
        this(null, null, null);
    }

    /**
     * Initializes a new {@link FreeBusyData}.
     *
     * @param participant The participant, represented either as e-mail address or group-, user- or resource-ID
     * @param from The lower (inclusive) limit of the time-range
     * @param until The upper (exclusive) limit of the time-range
     */
    public FreeBusyData(String participant, Date from, Date until) {
        super();
        this.participant = participant;
        this.from = from;
        this.until = until;
    }

    /**
     * Resets all free/busy intervals.
     */
    public void clear() {
        this.intervals = null;
    }

    /**
     * Adds a free/busy interval.
     *
     * @param interval The interval
     */
    public void add(FreeBusyInterval interval) {
        if (null == this.intervals) {
            this.intervals = new ArrayList<FreeBusyInterval>();
        }
        this.intervals.add(interval);
    }

    /**
     * Adds multiple intervals.
     *
     * @param intervals The intervals to add
     */
    public void addAll(Collection<? extends FreeBusyInterval> intervals) {
        if (null == this.intervals) {
            this.intervals = new ArrayList<FreeBusyInterval>();
        }
        if (null != intervals) {
            this.intervals.addAll(intervals);
        }
    }

    /**
     * Adds all data from another {@link FreeBusyData} instance, i.e. all intervals and warnings.
     *
     * @param data The data to add
     */
    public void add(FreeBusyData data) {
        if (null != data) {
            if (data.hasWarnings()) {
                for (OXException warning : data.warnings) {
                    this.addWarning(warning);
                }
            }
            if (data.hasData()) {
                this.addAll(data.getIntervals());
            }
        }
    }

    /**
     * Gets all intervals.
     *
     * @return The intervals.
     */
    public List<FreeBusyInterval> getIntervals() {
        return null != this.intervals ? Collections.unmodifiableList(this.intervals) : null;
    }

    /**
     * Gets the participant, identified either by its internal user-/resource-ID or e-mail address.
     *
     * @return The participant
     */
    public String getParticipant() {
        return participant;
    }

    /**
     * Gets the lower (inclusive) limit of the covered time-range.
     *
     * @return The 'from' date
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Gets the upper (exclusive) limit of the covered time-range.
     *
     * @return The 'until' date
     */
    public Date getUntil() {
        return until;
    }

    /**
     * Gets a list of warnings in case data could not be retrieved.
     *
     * @return The warnings, if present, or <code>null</code>, otherwise
     */
    public List<OXException> getWarnings() {
        return null != warnings && 0 < warnings.size() ? Collections.unmodifiableList(warnings) : null;
    }

    /**
     * Gets a value indicating whether there are warnings or not.
     *
     * @return <code>true</code> in case of existing warnings, <code>false</code>, otherwise
     */
    public boolean hasWarnings() {
        return null != this.warnings && 0 < warnings.size();
    }

    /**
     * Gets a value indicating whether data is available, i.e. the intervals have been initialized.
     *
     * @return <code>true</code> in case of existing data, <code>false</code>, otherwise
     */
    public boolean hasData() {
        return null != this.intervals;
    }

    /**
     * Adds an {@link OXException} as warning.
     *
     * @param warning The exception
     */
    public void addWarning(OXException warning) {
        if (null == warnings) {
            warnings = new ArrayList<OXException>();
        }
        this.warnings.add(warning);
    }

    private static Date[] getTimes(List<FreeBusyInterval> intervals) {
        Set<Date> times = new HashSet<Date>();
        for (FreeBusyInterval freeBusyInterval : intervals) {
            times.add(freeBusyInterval.getStartTime());
            times.add(freeBusyInterval.getEndTime());
        }
        Date[] array = times.toArray(new Date[times.size()]);
        Arrays.sort(array);
        return array;
    }

    /**
     * Normalizes the contained free/busy intervals. This means<ul>
     * <li>the intervals are sorted chronologically, i.e. the earliest interval is first</li>
     * <li>all intervals beyond or above the 'from' and 'until' range are removed, intervals overlapping the boundaries are shortened to
     * fit</li>
     * <li>overlapping intervals are merged so that only the most conflicting ones of overlapping time ranges are used</li>
     * </ul>
     */
    public void normalize() {
        if (false == hasData()) {
            return; // nothing to do
        }
        /*
         * normalize to interval boundaries
         */
        Iterator<FreeBusyInterval> iterator = intervals.iterator();
        while (iterator.hasNext()) {
            FreeBusyInterval interval = iterator.next();
            if (null != interval.getEndTime() && interval.getEndTime().after(getFrom()) &&
                null != interval.getStartTime() && interval.getStartTime().before(getUntil())) {
                if (interval.getStartTime().before(getFrom())) {
                    interval.setStartTime(getFrom());
                }
                if (interval.getEndTime().after(getUntil())) {
                    interval.setEndTime(getUntil());
                }
            } else {
                // outside range
                iterator.remove();
            }
        }
        if (2 > intervals.size()) {
            return; // nothing more to do
        }
        /*
         * expand intervals to match all possible boundaries
         */
        Date[] times = getTimes(this.intervals);
        ArrayList<FreeBusyInterval> expandedIntervals = new ArrayList<FreeBusyInterval>();
        for (FreeBusyInterval interval : intervals) {
            List<Date> expandedTimes = new ArrayList<Date>();
            expandedTimes.add(interval.getStartTime());
            for (Date time : times) {
                if (interval.getStartTime().before(time) && interval.getEndTime().after(time)) {
                    expandedTimes.add(time);
                }
            }
            expandedTimes.add(interval.getEndTime());
            if (2 == expandedTimes.size()) {
                expandedIntervals.add(interval);
            } else {
                for (int i = 0; i < expandedTimes.size() - 1; i++) {
                    expandedIntervals.add(new FreeBusyInterval(expandedTimes.get(i), expandedTimes.get(i + 1), interval));
                }
            }
        }
        /*
         * condense all overlapping intervals to most conflicting one
         */
        Collections.sort(expandedIntervals);
        ArrayList<FreeBusyInterval> mergedIntervals = new ArrayList<FreeBusyInterval>();
        iterator = expandedIntervals.iterator();
        FreeBusyInterval current = iterator.next();
        while (iterator.hasNext()) {
            FreeBusyInterval next = iterator.next();
            if (current.getStartTime().equals(next.getStartTime()) && current.getEndTime().equals(next.getEndTime())) {
                if (false == current.getStatus().isMoreConflicting(next.getStatus())) {
                    /*
                     * skip current slot
                     */
                    current = next;
                }
                continue;
            }
            mergedIntervals.add(current);
            current = next;
        }
        mergedIntervals.add(current);
        /*
         * expand consecutive intervals again
         */
        iterator = mergedIntervals.iterator();
        while (iterator.hasNext()) {
            FreeBusyInterval slot = iterator.next();
            for (FreeBusyInterval freeBusyInterval : mergedIntervals) {
                if (freeBusyInterval.equalsIgnoreTimes(slot)) {
                    /*
                     * merge if next to another
                     */
                    if (freeBusyInterval.getStartTime().equals(slot.getEndTime())) {
                        freeBusyInterval.setStartTime(slot.getStartTime());
                        iterator.remove();
                        break;
                    } else if (freeBusyInterval.getEndTime().equals(slot.getStartTime())) {
                        freeBusyInterval.setEndTime(slot.getEndTime());
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        /*
         * take over sorted intervals
         */
        this.intervals = mergedIntervals;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(participant).append(" (").append(null != from ? sdf.format(from) : "")
            .append(" - ").append(null != until ? sdf.format(until) : "").append("):").append("\n");
        if (hasData()) {
            for (FreeBusyInterval interval : this.intervals) {
                stringBuilder.append(interval).append("\n");
            }
        }
        if (hasWarnings()) {
            for (OXException warning : warnings) {
                stringBuilder.append(warning.getLogMessage()).append("\n");
            }
        }
        return stringBuilder.toString();
    }

}
