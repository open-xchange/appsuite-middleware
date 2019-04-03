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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.ical.ical4j;

import static com.openexchange.java.Autoboxing.I;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedStringReader;
import com.openexchange.tools.TimeZoneUtils;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;

/**
 * {@link ParserTools}
 *
 * copied from com.openexchange.data.conversion.ical.ical4j.ICal4JParser
 *
 * @since v7.10.0
 */
public class ICal4JParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ICal4JParser.class);

    private static final String TZ_REGEX = ";TZID=([^:]+):\\\\s*([0-9]{8}T[0-9]{6})";

    private static final Pattern WORKAROUND_30027 = Pattern.compile(":\\s{2,}([0-9]{8}T[0-9]{6}Z?)[ \\t]*");

    private static final Pattern TRIGGER = Pattern.compile("TRIGGER" + TZ_REGEX);

    private static final Pattern CREATED = Pattern.compile("CREATED" + TZ_REGEX);

    private static final Pattern LAST_MODIFIED = Pattern.compile("LAST-MODIFIED" + TZ_REGEX);

    private static final Pattern COMPLETED = Pattern.compile("COMPLETED" + TZ_REGEX);

    private static final Pattern DTSTAMP = Pattern.compile("DTSTAMP" + TZ_REGEX);

    /**
     * Parses the given iCAL file to an {@link net.fortuna.ical4j.model.Calendar}
     * 
     * @param builder The {@link CalendarBuilder}
     * @param iCalFile The iCAL file as {@link InputStream}
     * @param importLimit The limit of events to import. See {@link ICalParameters#IMPORT_LIMIT}
     * @return {@link net.fortuna.ical4j.model.Calendar} of the given iCAL
     * @throws IOException If reading fails
     * @throws ParserException If iCAL contains errors
     * @throws OXException If limit is exceeded
     */
    public net.fortuna.ical4j.model.Calendar parse(CalendarBuilder builder, InputStream iCalFile, int importLimit) throws IOException, ParserException, OXException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(iCalFile, Charsets.UTF_8));
            return parse(builder, reader, importLimit);
        } finally {
            Streams.close(reader);
        }
    }

    public net.fortuna.ical4j.model.Calendar parse(CalendarBuilder builder, final BufferedReader reader, int importLimit) throws IOException, ParserException, OXException {
        StringBuilder chunk = new StringBuilder();
        StringBuilder ical = new StringBuilder();
        
        boolean read = false;
        boolean beginFound = false;
        boolean inTransition = false;
        
        int lineCount = 0;
        int eventCount = 0;
        
        for (String line; (line = reader.readLine()) != null;) {
            if (!beginFound) {
                if (line.endsWith("BEGIN:VCALENDAR")) {
                    line = removeByteOrderMarks(line);
                }
                if (line.startsWith("BEGIN:VCALENDAR")) {
                    beginFound = true;
                } else if (!"".equals(line)) {
                    continue; // ignore bad lines between "VCALENDAR" Tags.
                }
            } else if (line.startsWith("END:VCALENDAR")) { //hack to fix bug 11958
                break;
            }
            if (line.matches("\\s*")) {
                continue;
            }
            if (line.matches("^\\s*BEGIN:VEVENT")) {
                inTransition = true;
                if (importLimit >= 0 && eventCount++ > importLimit) {
                    // Too many events
                    LOGGER.debug("The defined maximum value of {} events was exceeded. Aborting the import.", I(importLimit));
                    throw ICalExceptionCodes.TOO_MANY_IMPORTS.create();
                }
            } else if (line.matches("^\\s*END:VEVENT")) {
                inTransition = false;
            }
            read = true;
            chunk.append(line).append('\n');
            if (++lineCount > 1000 && false == inTransition) {
                ical.append(applyWorkarounds(chunk));
                chunk = new StringBuilder();
                lineCount = 0;
            }
        }
        if (!read) {
            throw ICalExceptionCodes.NO_CALENDAR.create();
        }
        chunk.append("END:VCALENDAR");
        ical.append(applyWorkarounds(chunk));
        return builder.build(new UnsynchronizedStringReader(ical.toString())); // FIXME: Encoding?

    }

    private String applyWorkarounds(StringBuilder sb) {
        return workaroundFor19463(workaroundFor16895(workaroundFor16613(workaroundFor16367(workaroundFor17492(workaroundFor17963(workaroundFor20453(workaroundFor27706And28942(workaroundFor29282(workaroundFor30027(removeAnnoyingWhitespaces(sb.toString())))))))))));
    }

    private String workaroundFor17963(final String input) {
        return input.replaceAll("EXDATE:(\\d+)([\\n\\r])", "EXDATE:$1T000000$2");
    }

    private String workaroundFor17492(final String input) {
        return input.replaceAll(";SCHEDULE-AGENT=", ";X-CALDAV-SCHEDULE-AGENT=");
    }

    private String workaroundFor19463(final String input) {
        return input.replaceAll("TZOFFSETFROM:\\s*(\\d\\d\\d\\d)", "TZOFFSETFROM:+$1").replaceAll("TZOFFSETTO:\\s*(\\d\\d\\d\\d)", "TZOFFSETTO:+$1");
    }

    private String workaroundFor20453(final String input) {
        return input.replaceAll("DTEND;\\s*\n", "");
    }

    private String workaroundFor29282(final String input) {
        return input.replaceAll("0000([0-9]{4}T[0-9]{6}Z)", "1970$1");
    }

    private String workaroundFor27706And28942(final String input) {
        Map<TimeZone, SimpleDateFormat> zones = new HashMap<>();
        Matcher m = DTSTAMP.matcher(input);
        final StringBuffer sb = new StringBuffer(input.length());
        while (m.find()) {
            final TimeZone tz = getTimeZone(zones, m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("DTSTAMP:" + getUtcPropertyFrom(m.group(2), zones, tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = COMPLETED.matcher(sb.toString());
        sb.delete(0, sb.length());
        while (m.find()) {
            final TimeZone tz = getTimeZone(zones, m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("COMPLETED:" + getUtcPropertyFrom(m.group(2), zones, tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = LAST_MODIFIED.matcher(sb.toString());
        sb.delete(0, sb.length());
        while (m.find()) {
            final TimeZone tz = getTimeZone(zones, m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("LAST-MODIFIED:" + getUtcPropertyFrom(m.group(2), zones, tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = CREATED.matcher(sb.toString());
        sb.delete(0, sb.length());
        while (m.find()) {
            final TimeZone tz = getTimeZone(zones, m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("CREATED:" + getUtcPropertyFrom(m.group(2), zones, tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = TRIGGER.matcher(sb.toString());
        sb.delete(0, sb.length());
        while (m.find()) {
            final TimeZone tz = getTimeZone(zones, m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("TRIGGER:" + getUtcPropertyFrom(m.group(2), zones, tz)));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    private TimeZone getTimeZone(Map<TimeZone, SimpleDateFormat> zones, String ID) {
        if (Strings.isEmpty(ID)) {
            return TimeZone.getDefault();
        }
        for (Iterator<Entry<TimeZone, SimpleDateFormat>> iterator = zones.entrySet().iterator(); iterator.hasNext();) {
            Entry<TimeZone, SimpleDateFormat> entry = iterator.next();
            if (entry.getKey().getID().equals(ID)) {
                // Found
                return entry.getKey();
            }
        }

        // Insert new time zone
        TimeZone zone = TimeZoneUtils.getTimeZone(ID);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        format.setTimeZone(zone);
        zones.put(zone, format);
        return zone;
    }

    private static final SimpleDateFormat UTC_PROPERTY;

    static {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZoneUtils.getTimeZone("UTC"));
        UTC_PROPERTY = sdf;
    }

    private String getUtcPropertyFrom(final String s, Map<TimeZone, SimpleDateFormat> zones, final TimeZone tz) {
        try {
            SimpleDateFormat sdf = zones.get(tz);
            final Date d = sdf.parse(s);
            synchronized (UTC_PROPERTY) {
                return UTC_PROPERTY.format(d);
            }
        } catch (final ParseException e) {
            return s;
        }
    }

    private String workaroundFor30027(final String input) {
        final Matcher m = WORKAROUND_30027.matcher(input);
        final StringBuffer sb = new StringBuffer(input.length());
        while (m.find()) {
            m.appendReplacement(sb, ": $1");
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * Method written out of laziness: Because you can spread iCal attributes
     * over several lines with newlines followed by a white space while a normal
     * newline means a new attribute starts, one would need to parse the whole file
     * (with a lookahead) before fixing errors. That means no regular expressions
     * allowed. Since spreading just makes it nicer to read for humans, this method
     * strips those newline+whitespace elements so we can use simple regexps.
     */
    private String removeAnnoyingWhitespaces(final String input) {
        /*
         * [http://www.ietf.org/rfc/rfc2445.txt]
         *
         * Long content lines SHOULD be split into a multiple line
         * representations using a line "folding" technique. That is, a long
         * line can be split between any two characters by inserting a CRLF
         * immediately followed by a single linear white space character (i.e.,
         * SPACE, US-ASCII decimal 32 or HTAB, US-ASCII decimal 9). Any sequence
         * of CRLF followed immediately by a single linear white space character
         * is ignored (i.e., removed) when processing the content type.
         */
        return input.replaceAll("(?:\\r\\n?|\\n)[ \t]", "");
    }

    private String workaroundFor16895(final String input) {
        /*
         * Bug in Zimbra: They like to use an EMAIL element for the
         * ATTENDEE property, though there is none.
         */
        return input.replaceAll("ATTENDEE([^\n]*?);EMAIL=", "ATTENDEE$1;X-ZIMBRA-EMAIL=");
    }

    private String workaroundFor16367(final String input) {
        /*
         * Bug in MS Exchange: If you use a CN element, it must have a value.
         * MS Exchange has an empty value, which we now replace properly.
         */
        return input.replaceAll("CN=:", "CN=\"\":");
    }

    private String workaroundFor16613(final String input) {
        /*
         * Bug in Groupwise: There is no attribute ID for ATTACH. Experimental
         * ones are allowed, but they would start with X-GW for Groupwise.
         * We ignore those.
         */
        return input.replaceAll("\nATTACH(.*?);ID=(.+?)([:;])", "\nATTACH$1$3");
    }
    
    private String removeByteOrderMarks(String line){
        char[] buf = line.toCharArray();
        int length = buf.length;

        final char first = buf[0];
        if(length > 3) {
            if(Character.getNumericValue(first) < 0 && Character.getNumericValue(buf[1]) < 0 && Character.getNumericValue(buf[2]) < 0 && Character.getNumericValue(buf[3]) < 0){
                if(Character.getType(first) == 15 && Character.getType(buf[1]) == 15 && Character.getType(buf[2]) == 28 && Character.getType(buf[3]) == 28) {
                    return new String(Arrays.copyOfRange(buf, 3, length));
                }
                if(Character.getType(first) == 28 && Character.getType(buf[1]) == 28 && Character.getType(buf[2]) == 15 && Character.getType(buf[3]) == 15) {
                    return new String(Arrays.copyOfRange(buf, 3, length));
                }
            }
        }
        if(length > 1) {
            if(Character.getNumericValue(first) < 0 && Character.getNumericValue(buf[1]) < 0) {
                if(Character.getType(first) == 28 && Character.getType(buf[1]) == 28) {
                    return new String(Arrays.copyOfRange(buf, 2, length));
                }
            }
        }
        if(length > 0) {
            if(Character.getNumericValue(first) < 0) {
                if(Character.getType(first) == 16) {
                    return new String(Arrays.copyOfRange(buf, 1, length));
                }
            }
        }
        return line;
    }
}
