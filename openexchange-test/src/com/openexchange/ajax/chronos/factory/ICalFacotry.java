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
import net.fortuna.ical4j.model.DateTime;

/**
 * {@link ICalFacotry} - Builds iCAL for given {@link EventData}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ICalFacotry {

    /** Participant status */
    public static enum PartStat {
        ACCEPTED("ACCEPTED"),
        TENTATIVE("TEANTATIVE"),
        DECLINED("DECLINED"),
        NEEDS_ACTION("NEEDS-ACTION");

        private final String status;

        private PartStat(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }

    }

    /** iTIP methods */
    public static enum Method {
        PUBLISH,
        REQUEST,
        REPLY,
        ADD,
        CANCEL,
        REFRESH,
        COUNTER,
        DECLINECOUNTER;
    }

    public static enum ProdID {
        OX("-//Open-Xchange//7.10.0-Rev6//EN"),
        MICROSOFT(""),;

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

    /** {@link StringBuilder} instance to generate iCal */
    private StringBuilder sb;

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
     * If fields aren't set in the {@link EventData}, they will be filled
     * with standard data (modifies original {@link EventData})
     * 
     * @return The iCal file as {@link String}
     */
    public String build() {
        sb = new StringBuilder();

        setField("BEGIN:VCALENDAR");
        setField("VERSION:2.0");
        setValue("PRODID:", prodId, ProdID.OX);
        setValue("METHOD:", method.name().toUpperCase(), Method.REQUEST.name().toUpperCase());
        setVTimezone();

        for (EventData eventData : data) {
            setEvent(eventData);
        }
        setField("END:VCALENDAR");

        return sb.toString();
    }

    private void setVTimezone() {
        /*
         * TODO
         * Base daylight and standard on actual time zone
         * How to calculate dtstart and rrule?
         */
        //        String tzid = data.get(0).getStartDate().getTzid();
        String tzid = "Europe/Berlin";
        setField("BEGIN:VTIMEZONE");
        setValue("TZID:", tzid);
        setValue("TZURL:http://tzurl.org/zoneinfo-outlook/", tzid);

        setDayLigth(tzid);
        setStandard(tzid);

        setField("END:VTIMEZONE");
    }

    private void setDayLigth(String tzid) {
        TimeZone timeZone = TimeZone.getTimeZone(tzid);
        long offset = TimeUnit.MILLISECONDS.toHours(timeZone.getRawOffset());

        setField("BEGIN:DAYLIGHT");
        setValue("TZOFFSETFROM:", getOffset(offset));
        setValue("TZOFFSETTO:", getOffset(offset + 1));
        setValue("TZNAME:", timeZone.toZoneId());

        // String dtstart = ???
        String dtstart = "19700329T020000";
        setValue("DTSTART:", dtstart);

        // String rrule = ???;
        String rrule = "FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU";
        setValue("RRULE:", rrule);
        setField("END:DAYLIGHT");
    }

    private void setStandard(String tzid) {
        TimeZone timeZone = TimeZone.getTimeZone(tzid);
        long offset = TimeUnit.MILLISECONDS.toHours(timeZone.getRawOffset());

        setField("BEGIN:STANDARD");
        setValue("TZOFFSETTO:", getOffset(offset));
        setValue("TZOFFSETFROM:", getOffset(offset + 1));

        //  sb.append("TZNAME:").append(timeZone.toZoneId());
        setValue("TZNAME:", "CET");

        // String dtstart = ???
        String dtstart = "19701025T030000";
        setValue("DTSTART:", dtstart);

        // String rrule = ???;
        String rrule = "FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU";
        setValue("RRULE:", rrule);
        setField("END:STANDARD");
    }

    private void setEvent(EventData eventData) {
        setField("BEGIN:VEVENT");
        setValue("DTSTAMP:", Long.valueOf(System.currentTimeMillis()));

        setAttendees(eventData);

        setValue("CLASS:", eventData.getPropertyClass());
        setValue("CREATED:", null == eventData.getCreated() ? null : new DateTime(eventData.getCreated().longValue()), current(), (DateTime created) -> {
            eventData.setCreated(Long.valueOf(created.getTime()));
        });

        setValue("DESCRIPTION:", eventData.getDescription(), "This is an iMIP mail");

        sb.append("DTEND;TZID=").append(eventData.getEndDate().getTzid()).append(":").append(eventData.getEndDate().getValue());
        addNewLine();

        sb.append("DTSTART;TZID=").append(eventData.getStartDate().getTzid()).append(":").append(eventData.getStartDate().getValue());
        addNewLine();

        setValue("LAST-MODIFIED:", null == eventData.getLastModified() ? null : new DateTime(eventData.getLastModified().longValue()), current(), (DateTime mod) -> {
            eventData.setLastModified(Long.valueOf(mod.getTime()));
        });

        setValue("LOCATION:", eventData.getLocation(), "Germany, Olpe");

        sb.append("ORGANIZER;").append("CN=\"").append(eventData.getOrganizer().getCn()).append("\":");
        appendMailTo(eventData.getOrganizer().getEmail());
        addNewLine();

        setValue("SEQUENCE:", eventData.getSequence(), "0");
        setValue("SUMMARY:", eventData.getSummary(), "This is the summary", (String s) -> {
            eventData.setSummary(s);
        });

        setValue("TRANSP:", eventData.getTransp(), TranspEnum.OPAQUE);
        setValue("UID:", eventData.getUid(), UUID.randomUUID().toString(), (String uid) -> {
            eventData.setUid(uid);
        });

        setField("X-MICROSOFT-CDO-BUSYSTATUS:BUSY");
        setField("END:VEVENT");
    }

    private void setAttendees(EventData eventData) {
        for (Attendee a : eventData.getAttendees()) {
            sb.append("ATTENDEE;CN=").append(a.getCn());
            sb.append(";PARTSTAT=").append(null == a.getPartStat() ? PartStat.NEEDS_ACTION.toString() : a.getPartStat());
            sb.append(";CUTYPE=").append(a.getCuType());
            sb.append(";EMAIL=").append(a.getEmail());
            sb.append(":");
            appendMailTo(a.getEmail());
            addNewLine();
        }
    }

    private void setField(String field) {
        setValue(field, null, null);
    }

    private void setValue(String field, Object value) {
        setValue(field, value, null);
    }

    private void setValue(String field, Object value, Object defaultValue) {
        setValue(field, value, defaultValue, null);
    }

    private <T extends Object> void setValue(String field, T value, T defaultValue, Consumer<T> f) {
        sb.append(field);
        if (null == value) {
            if (null != defaultValue) {
                sb.append(defaultValue);
                if (null != f) {
                    f.accept(defaultValue);
                }
            }
        } else {
            sb.append(value);
        }
        addNewLine();
    }

    private String getOffset(long offset) {
        StringBuilder offBy = new StringBuilder();
        if (offset < 0) {
            offBy.append("-");
        } else {
            offBy.append("+");
        }
        if (Math.abs(offset) < 10) {
            offBy.append("0");
        }
        offBy.append(offset);
        offBy.append("00");
        return offBy.toString();
    }

    private DateTime current() {
        return new DateTime(System.currentTimeMillis());
    }

    private void appendMailTo(String mail) {
        if (false == mail.toLowerCase().startsWith("mailto")) {
            sb.append("mailto:");
        }
        sb.append(mail);
    }

    private void addNewLine() {
        sb.append("\n");
    }

}
