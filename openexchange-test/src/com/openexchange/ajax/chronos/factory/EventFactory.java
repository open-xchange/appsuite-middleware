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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;

/**
 * {@link EventFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class EventFactory {

    /**
     * Creates a single event for the specified user with in the specified interval.
     *
     * @param userId The user identifier
     * @param summary The summary of the event
     * @param startDate The start date
     * @param endDate The end date
     * @return The {@link EventData}
     */
    public static EventData createSingleEvent(int userId, String summary, DateTimeData startDate, DateTimeData endDate) {
        Attendee attendee = AttendeeFactory.createIndividual(userId);

        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(startDate);
        singleEvent.setEndDate(endDate);
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setSummary(summary);

        return singleEvent;
    }

    /**
     * Creates a simple daily two hour event with the specified amount of occurrences
     *
     * @param userId The user identifier
     * @param summary The summary of the event
     * @return The series {@link EventData}
     */
    public static EventData createSeriesEvent(int userId, String summary, int occurences) {
        EventData seriesEvent = createSingleTwoHourEvent(userId, summary);
        seriesEvent.setRrule("FREQ=DAILY;COUNT=" + occurences);
        return seriesEvent;
    }

    /**
     * Creates a simple daily two hour event with the specified amount of occurrences
     *
     * @param userId The user identifier
     * @param summary The summary of the event
     * @param startDate The start date
     * @param endDate The end date
     * @param occurences The number of occurences
     * @return The series {@link EventData}
     */
    public static EventData createSeriesEvent(int userId, String summary, DateTimeData startDate, DateTimeData endDate, int occurences) {
        EventData seriesEvent = createSingleEvent(userId, summary, startDate, endDate);
        seriesEvent.setRrule("FREQ=DAILY;COUNT=" + occurences);
        return seriesEvent;
    }

    /**
     * Creates a single event with the specified start and end time and with the specified attachment.
     *
     * @param userId The user identifier
     * @param summary The summary of the event
     * @param startDate The start date
     * @param endDate The end date
     * @param asset The {@link Asset} to attach
     * @return The {@link EventData}
     */
    public static EventData createSingleEventWithAttachment(int userId, String summary, Asset asset) {
        return createSingleEventWithAttachments(userId, summary, Collections.singletonList(asset));
    }

    public static EventData createSingleEventWithAttachments(int userId, String summary, List<Asset> assets) {
        EventData eventData = createSingleTwoHourEvent(userId, summary);
        for (Asset asset : assets) {
            eventData.addAttachmentsItem(createAttachment(asset));
        }
        return eventData;
    }

    public static EventData createSingleEventWithSingleAlarm(int userId, String summary, Alarm alarm) {
        EventData eventData = createSingleTwoHourEvent(userId, summary);
        eventData.setAlarms(Collections.singletonList(alarm));
        return eventData;
    }

    public static EventData createSingleEventWithSingleAlarm(int userId, String summary, DateTimeData startDate, DateTimeData endDate, Alarm alarm) {
        EventData eventData = createSingleEvent(userId, summary, startDate, endDate);
        eventData.setAlarms(Collections.singletonList(alarm));
        return eventData;
    }

    /**
     * Creates a {@link ChronosAttachment} out of the specified {@link Asset}
     *
     * @param asset The {@link Asset}
     * @return The {@link ChronosAttachment}
     */
    public static ChronosAttachment createAttachment(Asset asset) {
        ChronosAttachment attachment = new ChronosAttachment();
        attachment.setFilename(asset.getFilename());
        attachment.setFmtType(asset.getAssetType().name());
        attachment.setUri("cid:file_0");

        return attachment;
    }

    /**
     * Creates a single event two-hour event
     *
     * @param userId The user identifier
     * @param summary The event's summary
     * @return The {@link EventData}
     */
    public static EventData createSingleTwoHourEvent(int userId, String summary) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));

        return createSingleEvent(userId, summary, DateTimeUtil.getDateTime(start), DateTimeUtil.getDateTime(end));
    }
}
