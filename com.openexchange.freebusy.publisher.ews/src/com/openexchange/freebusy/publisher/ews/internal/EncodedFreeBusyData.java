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

package com.openexchange.freebusy.publisher.ews.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyInterval;

/**
 * {@link EncodedFreeBusyData}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EncodedFreeBusyData {

    /**
     * Number of milliseconds between 1601 (~ Windows) and 1970 (~ Unix)
     */
    private static final long MILLIS_BETWEEN_EPOCHS = 11644473600000L;

    private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final Map<Integer, List<Byte>> busyData;
    private final Map<Integer, List<Byte>> tentativeData;
    private final Map<Integer, List<Byte>> awayData;
    private final Map<Integer, List<Byte>> mergedData;
    private final long publishStart;
    private final long publishEnd;

    /**
     * Initializes a new {@link EncodedFreeBusyData}.
     *
     * @param freeBusyData The free/busy data to encode
     */
    public EncodedFreeBusyData(FreeBusyData freeBusyData) {
        super();
        this.busyData = createFreeBusyData(normalize(freeBusyData, BusyStatus.RESERVED));
        this.tentativeData = createFreeBusyData(normalize(freeBusyData, BusyStatus.TEMPORARY));
        this.awayData = createFreeBusyData(normalize(freeBusyData, BusyStatus.ABSENT));
        this.mergedData = createFreeBusyData(normalize(freeBusyData, BusyStatus.RESERVED, BusyStatus.ABSENT));
        this.publishStart = getMinutesSince1601(freeBusyData.getFrom());
        this.publishEnd = getMinutesSince1601(freeBusyData.getUntil());
    }

    /**
     * Gets the encoded busy times.
     *
     * @return The busy times.
     */
    public List<String> getBusyTimes() {
        return getTimes(busyData);
    }

    public List<Integer> getBusyMonths() {
        return getMonths(busyData);
    }

    public List<String> getTentativeTimes() {
        return getTimes(tentativeData);
    }

    public List<Integer> getTentativeMonths() {
        return getMonths(tentativeData);
    }

    public List<String> getAwayTimes() {
        return getTimes(awayData);
    }

    public List<Integer> getAwayMonths() {
        return getMonths(awayData);
    }

    public List<String> getMergedTimes() {
        return getTimes(mergedData);
    }

    public List<Integer> getMergedMonths() {
        return getMonths(mergedData);
    }

    public long getPublishStart() {
        return publishStart;
    }

    public long getPublishEnd() {
        return publishEnd;
    }

    private Map<Integer, List<Byte>> createFreeBusyData(List<FreeBusyInterval> intervals) {
        Map<Integer, List<Byte>> map = new HashMap<Integer, List<Byte>>();
        for (FreeBusyInterval interval : intervals) {
            append(interval.getStartTime(), interval.getEndTime(), map);
        }
        return map;
    }

    private List<FreeBusyInterval> normalize(FreeBusyData freeBusyData, BusyStatus...status) {
        /*
         * collect matching time ranges
         */
        List<FreeBusyInterval> intervals = new ArrayList<FreeBusyInterval>();
        for (FreeBusyInterval interval : freeBusyData.getIntervals()) {
            if (matches(interval, status) && null != interval.getEndTime() && interval.getEndTime().after(freeBusyData.getFrom()) &&
                    null != interval.getStartTime() && interval.getStartTime().before(freeBusyData.getUntil())) {
                Date start = interval.getStartTime().before(freeBusyData.getFrom()) ? freeBusyData.getFrom() : interval.getStartTime();
                Date end = interval.getEndTime().after(freeBusyData.getUntil()) ? freeBusyData.getUntil() : interval.getEndTime();
                intervals.add(new FreeBusyInterval(start, end, interval.getStatus()));
            }
        }
        if (1 < intervals.size()) {
            /*
             * sort ranges
             */
            Collections.sort(intervals);
            /*
             * merge ranges
             */
            List<FreeBusyInterval> mergedIntervals = new ArrayList<FreeBusyInterval>();
            Iterator<FreeBusyInterval> iterator = intervals.iterator();
            FreeBusyInterval current = iterator.next();
            while (iterator.hasNext()) {
                FreeBusyInterval next = iterator.next();
                if (current.getEndTime().after(next.getStartTime())) {
                    // overlapping
                    if (current.getEndTime().before(next.getEndTime())) {
                        current.setEndTime(next.getEndTime());
                    }
                } else {
                    mergedIntervals.add(current);
                    current = next;
                }
            }
            mergedIntervals.add(current);
            intervals = mergedIntervals;
        }
        /*
         * expand at month boundaries if needed
         */
        List<FreeBusyInterval> expandedIntervals = new ArrayList<FreeBusyInterval>();
        for (FreeBusyInterval interval : intervals) {
            addMonthWise(expandedIntervals, interval);
        }
        return expandedIntervals;
    }

    private void addMonthWise(List<FreeBusyInterval> intervals, FreeBusyInterval interval) {
        Date start = interval.getStartTime();
        for (; false == isSameMonth(start, interval.getEndTime()); start = getStartOfNextMonth(start)) {
            intervals.add(new FreeBusyInterval(start, getEndOfMonth(start), interval.getStatus()));
        }
        intervals.add(new FreeBusyInterval(start, interval.getEndTime(), interval.getStatus()));
    }

    private boolean isSameMonth(Date date1, Date date2) {
        calendar.setTime(date1);
        int month1 = calendar.get(Calendar.MONTH);
        int year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(date2);
        int month2 = calendar.get(Calendar.MONTH);
        int year2 = calendar.get(Calendar.YEAR);
        return month1 == month2 && year1 == year2;
    }

    private static boolean matches(FreeBusyInterval interval, BusyStatus[] status) {
        for (BusyStatus s : status) {
            if (s.equals(interval.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private Date getStartOfNextMonth(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getEndOfMonth(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

    private void append(Date start, Date end, Map<Integer, List<Byte>> data) {
        Integer month = Integer.valueOf(getFreeBusyMonth(start));
        List<Byte> bytes = data.get(month);
        if (null == bytes) {
            bytes = new ArrayList<Byte>();
            data.put(month, bytes);
        }
        append(start, end, bytes);
    }

    private void append(Date start, Date end, List<Byte> data) {
        int startMinutes = getFreeBusyTime(start);
        data.add(Byte.valueOf((byte)(startMinutes & 0xff)));
        data.add(Byte.valueOf((byte)((startMinutes >> 8) & 0xff)));
        int endMinutes = getFreeBusyTime(end);
        data.add(Byte.valueOf((byte)(endMinutes & 0xff)));
        data.add(Byte.valueOf((byte)((endMinutes >> 8) & 0xff)));
    }

    /**
     * Gets the number of minutes since the first day of the month as
     * represented by the supplied time.
     *
     * @param date the date
     * @return the free busy time
     */
    private int getFreeBusyTime(Date date) {
        calendar.setTime(date);
        int days = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        int hours =  24 * days + calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        return 60 * hours + minutes;
    }

    /**
     * Gets the free busy month representation for the supplied date.
     *
     * @param date the date
     * @return the free busy month
     */
    private int getFreeBusyMonth(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) * 16 + (calendar.get(Calendar.MONTH) + 1);
    }

    private static List<Integer> getMonths(Map<Integer, List<Byte>> data) {
        return null != data && 0 < data.size() ? new ArrayList<Integer>(data.keySet()) : null;
    }

    private static List<String> getTimes(Map<Integer, List<Byte>> data) {
        if (null != data && 0 < data.size()) {
            List<String> times = new ArrayList<String>(data.size());
            for (List<Byte> bytes : data.values()) {
                times.add(encode(bytes));
            }
            return times;
        }
        return null;
    }

    private static String encode(List<Byte> data) {
        if (null != data) {
            byte[] bytes = new byte[data.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = data.get(i).byteValue();
            }
            return new String(Base64.encodeBase64(bytes));
        } else {
            return null;
        }
    }

    private static long getMinutesSince1601(Date date) {
        return (MILLIS_BETWEEN_EPOCHS + date.getTime()) / (60 * 1000);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPublishStart()).append(" - ").append(getPublishEnd()) .append('\n')
            .append("Away: " + awayData).append('\n')
            .append("Busy: " + busyData).append('\n')
            .append("Tentative: " + tentativeData).append('\n')
            .append("Merged: " + mergedData).append('\n')
        ;
        return stringBuilder.toString();
    }

}
