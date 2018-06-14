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

package com.openexchange.ajax.chronos.factory;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;

/**
 * {@link ICalFacotry} - Builds iCAL for given {@link EventData}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ICalFacotry {

    enum Method {
        PUBLISH,
        REQUEST,
        REPLY,
        ADD,
        CANCEL,
        REFRESH,
        COUNTER,
        DECLINECOUNTER;
    }

    enum ProdID {
        OX("-//Open-Xchange//7.10.0-Rev6//EN"),
        MICROSOFT(""),
        ;

        private final String id;

        /**
         * Initializes a new {@link ICalFacotry.ProdID}.
         * 
         */
        private ProdID(String id) {
            this.id = id;
        }

        public String getId(Locale locale) {
            // TODO Localize & set correct version
            return id;
        }
    }

    /** The method to use in the iCal file */
    private Method method;

    /** All relevant {@link EventData} */
    private List<EventData> data;

    /** The ID of the manufacture */
    private ProdID prodId;

    /**
     * Initializes a new {@link ICalFacotry}.
     * 
     * @param data The Events
     */
    public ICalFacotry(List<EventData> data) {
        this(data, Method.REQUEST, ProdID.OX);
    }

    /**
     * Initializes a new {@link ICalFacotry}.
     * 
     * @param data The Events
     * @param method The iTIP {@link Method}
     */
    public ICalFacotry(List<EventData> data, Method method) {
        this(data, method, ProdID.OX);
    }

    /**
     * Initializes a new {@link ICalFacotry}.
     * 
     * @param data The Events
     * @param method The iTIP {@link Method}
     * @param prodId The {@link ProdID}
     */
    public ICalFacotry(List<EventData> data, Method method, ProdID prodId) {
        super();
        this.data = data;
        this.method = method;
        this.prodId = prodId;
    }

    public ICalFacotry addMethod(Method method) {
        this.method = method;
        return this;
    }

    public ICalFacotry addProdID(ProdID prodId) {
        this.prodId = prodId;
        return this;
    }

    public ICalFacotry addEventData(List<EventData> data) {
        this.data = data;
        return this;
    }

    /**
     * Builds the iCal file with all available data.
     * 
     * @return The iCal file as {@link String}
     */
    public String build() {
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN:VCALENDAR");
        addNewLine(sb);
        sb.append("VERSION:2.0");
        addNewLine(sb);

        if (null == prodId) {
            prodId = ProdID.OX;
        }
        sb.append("PRODID:").append(prodId.getId(null));
        addNewLine(sb);

        if (null == method) {
            method = Method.REQUEST;
        }
        sb.append("METHOD:").append(method.name().toUpperCase());
        addNewLine(sb);

        setVTimezone(sb);

        for (EventData eventData : data) {
            setEvent(sb, eventData);
        }
        sb.append("END:VCALENDAR");

        return sb.toString();
    }

    private void setVTimezone(StringBuilder sb) {
        /*
         * TODO
         * Base daylight and standard on actual time zone
         * How to calculate dtstart and rrule?
         */
        //        String tzid = data.get(0).getStartDate().getTzid();
        String tzid = "Europe/Berlin";
        sb.append("BEGIN:VTIMEZONE");
        addNewLine(sb);
        sb.append("TZID:").append(tzid);
        addNewLine(sb);
        sb.append("TZURL:http://tzurl.org/zoneinfo-outlook/").append(tzid);
        addNewLine(sb);

        setDayLigth(sb, tzid);
        setStandard(sb, tzid);

        sb.append("END:VTIMEZONE");
        addNewLine(sb);
    }

    private void setDayLigth(StringBuilder sb, String tzid) {
        TimeZone timeZone = TimeZone.getTimeZone(tzid);
        sb.append("BEGIN:DAYLIGHT");
        addNewLine(sb);
        long offset = TimeUnit.MILLISECONDS.toHours(timeZone.getRawOffset());
        sb.append("TZOFFSETFROM:");
        appendOffset(sb, offset);
        sb.append("TZOFFSETTO:");
        appendOffset(sb, offset + 1);
        sb.append("TZNAME:").append(timeZone.toZoneId());
        addNewLine(sb);
        // String dtstart = ???
        String dtstart = "19700329T020000";
        sb.append("DTSTART:").append(dtstart);
        addNewLine(sb);
        // String rrule = ???;
        String rrule = "FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU";
        sb.append("RRULE:").append(rrule);
        addNewLine(sb);
        sb.append("END:DAYLIGHT");
        addNewLine(sb);
    }

    private void appendOffset(StringBuilder sb, long offset) {
        if (offset < 0) {
            sb.append("-");
        } else {
            sb.append("+");
        }
        if (Math.abs(offset) < 10) {
            sb.append("0");
        }
        sb.append(offset);
        sb.append("00");
        addNewLine(sb);
    }

    private void setStandard(StringBuilder sb, String tzid) {
        TimeZone timeZone = TimeZone.getTimeZone(tzid);
        sb.append("BEGIN:STANDARD");
        addNewLine(sb);
        long offset = TimeUnit.MILLISECONDS.toHours(timeZone.getRawOffset());
        sb.append("TZOFFSETTO:");
        appendOffset(sb, offset);
        sb.append("TZOFFSETFROM:");
        appendOffset(sb, offset + 1);
        //  sb.append("TZNAME:").append(timeZone.toZoneId());
        sb.append("TZNAME:").append("CET");
        addNewLine(sb);
        // String dtstart = ???
        String dtstart = "19701025T030000";
        sb.append("DTSTART:").append(dtstart);
        addNewLine(sb);
        // String rrule = ???;
        String rrule = "FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU";
        sb.append("RRULE:").append(rrule);
        addNewLine(sb);
        sb.append("END:STANDARD");
        addNewLine(sb);
    }

    private void setEvent(StringBuilder sb, EventData eventData) {
        sb.append("BEGIN:VEVENT");
        addNewLine(sb);

        sb.append("DTSTAMP:").append(System.currentTimeMillis());
        addNewLine(sb);

        setAttendees(sb, eventData);

        sb.append("CLASS:").append(eventData.getPropertyClass());
        addNewLine(sb);

        sb.append("CREATED:");
        nullTimestamp(sb, eventData.getCreated(), (Long created) -> {
            eventData.setCreated(created);
        });
        addNewLine(sb);

        sb.append("DESCRIPTION:").append(eventData.getDescription());
        addNewLine(sb);

        sb.append("DTEND;TZID=").append(eventData.getEndDate().getTzid()).append(":").append(eventData.getEndDate().getValue());
        addNewLine(sb);

        sb.append("DTSTART;TZID=").append(eventData.getStartDate().getTzid()).append(":").append(eventData.getStartDate().getValue());
        addNewLine(sb);

        sb.append("LAST-MODIFIED:");
        nullTimestamp(sb, eventData.getLastModified(), (Long mod) -> {
            eventData.setLastModified(mod);
        });
        addNewLine(sb);

        sb.append("LOCATION:").append(eventData.getLocation());
        addNewLine(sb);

        sb.append("ORGANIZER:").append("CN=\"").append(eventData.getOrganizer().getCn()).append("\":");
        appendMailTo(sb, eventData.getOrganizer().getEmail());
        addNewLine(sb);

        sb.append("SEQUENCE:").append(null == eventData.getSequence() ? "0" : eventData.getSequence());
        addNewLine(sb);

        sb.append("SUMMARY:").append(eventData.getSummary());
        addNewLine(sb);

        sb.append("TRANSP:").append(null == eventData.getTransp() ? TranspEnum.OPAQUE : eventData.getTransp());
        addNewLine(sb);

        sb.append("UID:");
        if (null == eventData.getUid()) {
            eventData.setUid(UUID.randomUUID().toString());
        }
        sb.append(eventData.getUid());
        addNewLine(sb);

        sb.append("X-MICROSOFT-CDO-BUSYSTATUS:BUSY");
        addNewLine(sb);

        sb.append("END:VEVENT");
        addNewLine(sb);

    }

    private void setAttendees(StringBuilder sb, EventData eventData) {
        for (Attendee a : eventData.getAttendees()) {
            sb.append("ATTENDEE;CN=").append(a.getCn());
            sb.append(";PARTSTAT=").append(a.getPartStat());
            sb.append(";CUTYPE=").append(a.getCuType());
            sb.append(";EMAIL=").append(a.getEmail());
            sb.append(":");
            appendMailTo(sb, a.getEmail());
            addNewLine(sb);
        }
    }

    private void nullTimestamp(StringBuilder sb, Long created, Consumer<Long> f) {
        if (null == created) {
            created = Long.valueOf(System.currentTimeMillis());
            f.accept(created);
        }
        sb.append(created);
    }

    private void appendMailTo(StringBuilder sb, String mail) {
        if (false == mail.toLowerCase().startsWith("mailto")) {
            sb.append("mailto:");
        }
        sb.append(mail);
    }

    private void addNewLine(StringBuilder sb) {
        sb.append("\n");
    }

}
