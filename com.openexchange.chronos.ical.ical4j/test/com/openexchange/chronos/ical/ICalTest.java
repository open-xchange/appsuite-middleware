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

package com.openexchange.chronos.ical;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.impl.ICalParametersImpl;
import com.openexchange.chronos.ical.impl.ICalServiceImpl;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.util.TimeZones;
import net.fortuna.ical4j.data.UnfoldingReader;

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

    protected ImportedCalendar importICal(String iCal) throws Exception {
        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        return iCalService.importICal(inputStream, null);
    }

    protected Event importEvent(String iCal) throws Exception {
        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        return iCalService.importICal(inputStream, null).getEvents().get(0);
    }

    protected String exportEvent(Event event) throws Exception {
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

    protected static String unfold(String iCal) throws IOException {
        int bufferLength = 1024;
        UnfoldingReader reader = new UnfoldingReader(new StringReader(iCal), bufferLength, true);
        try {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[bufferLength];
            for (int read; (read = reader.read(buffer, 0, bufferLength)) > 0;) {
                stringBuilder.append(buffer, 0, read);
            }
            return 0 == stringBuilder.length() ? null : stringBuilder.toString();
        } finally {
            Streams.close(reader);
        }
    }

}
