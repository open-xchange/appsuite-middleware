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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.ical;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.chronos.ical.impl.ICalParametersImpl;
import com.openexchange.chronos.ical.impl.ICalServiceImpl;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.util.TimeZones;

/**
 * {@link ICalTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalTest {

    private static final String[] DATE_PATTERNS = { "dd/MM/yyyy HH:mm", "dd.MM.yyyy HH:mm", "yyyy-MM-dd HH:mm" };

    protected final ICalService iCalService;

    protected ICalTest() {
        super();
        this.iCalService = new ICalServiceImpl();
    }

    //	private String serialize(Event event) throws IOException {
    //		ICalParametersImpl parameters = new ICalParametersImpl();
    //		TimezoneInfo tzInfo = new TimezoneInfo();
    //		tzInfo.setDefaultTimeZone(null);
    //		parameters.set(ICalParameters.TIMEZONE_INFO, tzInfo);
    //
    //		VEvent vEvent = new ICalMapper().exportEvent(event, null, parameters, null);
    //
    //		ICalendar iCalendar = new ICalendar();
    //		iCalendar.addEvent(vEvent);
    //		StringWriter writer = null;
    //		ICalWriter iCalWriter = null;
    //		try {
    //			writer = new StringWriter();
    //			iCalWriter = new ICalWriter(writer, ICalVersion.V2_0);
    //			iCalWriter.setTimezoneInfo(tzInfo);
    //			iCalWriter.write(iCalendar);
    //			return writer.toString();
    //		} finally {
    //			Streams.close(iCalWriter, writer);
    //		}
    //	}

    protected CalendarImport importICal(String iCal) throws Exception {
        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        return iCalService.importICal(inputStream, null);
    }

    protected EventData importEvent(String iCal) throws Exception {
        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        return iCalService.importICal(inputStream, null).getEvents().get(0);
    }

    protected String exportEvent(EventData event) throws Exception {
        ICalParametersImpl parameters = new ICalParametersImpl();
        CalendarExport calendarExport = iCalService.exportICal(parameters);
        calendarExport.add(event);
        byte[] iCal = calendarExport.toByteArray();
        return new String(iCal, Charsets.UTF_8);
    }

    protected static Date D(String value) throws ParseException {
        return D(value, TimeZones.UTC);
    }

    protected static Date D(String value, String timeZoneID) throws ParseException {
        return D(value, TimeZone.getTimeZone(timeZoneID));
    }

    protected static Date D(String value, TimeZone timeZone) throws ParseException {
        for (String pattern : DATE_PATTERNS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                if (null != timeZone) {
                    dateFormat.setTimeZone(timeZone);
                }
                return dateFormat.parse(value);
            } catch (ParseException e) {
                // next
            }
        }
        throw new ParseException(value, 0);
    }

}
