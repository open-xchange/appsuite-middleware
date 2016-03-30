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

package com.openexchange.data.conversion.ical.ical4j;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.util.CompatibilityHints;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.FreeBusyInformation;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ical4j.internal.AppointmentConverters;
import com.openexchange.data.conversion.ical.ical4j.internal.AttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.FreeBusyConverters;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.data.conversion.ical.ical4j.internal.TaskConverters;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedStringReader;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * {@link ICal4JParser} - The {@link ICalParser} using <a href="http://ical4j.sourceforge.net/">ICal4j</a> library.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Tobias Prinz <tobias.prinz@open-xchange.com> (bug workarounds)
 */
public class ICal4JParser implements ICalParser {

    private static final Charset UTF8 = Charsets.UTF_8;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICal4JParser.class);

    private static final Map<String, Integer> WEEKDAYS = new HashMap<String, Integer>(7);
    static {
        WEEKDAYS.put("MO", Integer.valueOf(CalendarObject.MONDAY));
        WEEKDAYS.put("TU", Integer.valueOf(CalendarObject.TUESDAY));
        WEEKDAYS.put("WE", Integer.valueOf(CalendarObject.WEDNESDAY));
        WEEKDAYS.put("TH", Integer.valueOf(CalendarObject.THURSDAY));
        WEEKDAYS.put("FR", Integer.valueOf(CalendarObject.FRIDAY));
        WEEKDAYS.put("SA", Integer.valueOf(CalendarObject.SATURDAY));
        WEEKDAYS.put("SO", Integer.valueOf(CalendarObject.SUNDAY));
    }

	private int limit = -1;

    public ICal4JParser() {
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_NOTES_COMPATIBILITY, true);
        CompatibilityHints.setHintEnabled(
        		CompatibilityHints.KEY_RELAXED_PARSING, true);
        CompatibilityHints.setHintEnabled(
              	CompatibilityHints.KEY_RELAXED_VALIDATION, true);

    }

    @Override
    public List<CalendarDataObject> parseAppointments(final String icalText, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseAppointments(new ByteArrayInputStream(icalText.getBytes(UTF8)), defaultTZ, ctx, errors, warnings);
        } catch (final UnsupportedCharsetException e) {
            LOG.error("", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<CalendarDataObject> parseAppointments(final InputStream ical, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ical, UTF8));

            List<CalendarDataObject> appointments = null;
            while (true) {
                final net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if (calendar == null) {
                    break;
                }
                final ComponentList vevents = calendar.getComponents("VEVENT");
                final int myLimit = limit < 0 ? vevents.size() : limit < vevents.size() ? limit : vevents.size();
                if (null == appointments) {
                    appointments = new ArrayList<CalendarDataObject>(myLimit);
                }
                for (int i = 0; i < myLimit; i++) {
                    final Object componentObj = vevents.get(i);
                    final Component vevent = (Component) componentObj;
                    try {
                        appointments.add(convertAppointment(i, (VEvent) vevent, defaultTZ, ctx, warnings));
                    } catch (final ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }

            return appointments;
        } catch (final UnsupportedCharsetException e) {
            // IGNORE
        } catch (final ConversionError e){
        	errors.add(e);
        } finally {
            closeSafe(reader);
        }

        return Collections.emptyList();
    }

	@Override
	public List<FreeBusyInformation> parseFreeBusy(String icalText, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseFreeBusy(new ByteArrayInputStream(icalText.getBytes(UTF8)), defaultTZ, ctx, errors, warnings);
        } catch (UnsupportedCharsetException e) {
            LOG.error("", e);
        }
        return Collections.emptyList();
	}

	@Override
	public List<FreeBusyInformation> parseFreeBusy(InputStream ical, TimeZone defaultTZ, Context ctx, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError {
        List<FreeBusyInformation> fbInfos = new ArrayList<FreeBusyInformation>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ical, UTF8));
            while (true) {
                net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if (null == calendar) {
                	break;
                }
                ComponentList freebusies = calendar.getComponents("VFREEBUSY");
                int myLimit = limit < 0 ? freebusies.size() : limit < freebusies.size() ? limit : freebusies.size();
                for (int i = 0; i < myLimit; i++) {
                    Component vevent = (Component) freebusies.get(i);
                    try {
                        fbInfos.add(convertFreeBusy(i, (VFreeBusy)vevent, defaultTZ, ctx, warnings));
                    } catch (ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }
        } catch (UnsupportedCharsetException e) {
            LOG.error("", e);
            // IGNORE
        } catch (ConversionError e) {
        	errors.add(e);
        } finally {
            closeSafe(reader);
        }
        return fbInfos;
	}

	private FreeBusyInformation convertFreeBusy(int index, VFreeBusy freeBusy, TimeZone defaultTZ, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
        FreeBusyInformation fbInfo = new FreeBusyInformation();
        TimeZone tz = determineTimeZone(freeBusy, defaultTZ);
        for (AttributeConverter<VFreeBusy, FreeBusyInformation> converter : FreeBusyConverters.REQUEST) {
            if (converter.hasProperty(freeBusy)) {
				converter.parse(index, freeBusy, fbInfo, tz, ctx, warnings);
            }
        }
//        fbInfo.setTimezone(getTimeZoneID(tz));
        return fbInfo;

	}

    @Override
    public String parseProperty(final String propertyName, final InputStream ical) {
        if (null == propertyName || null == ical) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ical, UTF8));
            final net.fortuna.ical4j.model.Calendar calendar = parse(reader);
            if (calendar == null) {
                return null;
            }
            final Property property = calendar.getProperty(propertyName.toUpperCase(Locale.US));
            return null == property ? null : property.getValue();
        } catch (final UnsupportedCharsetException e) {
            // IGNORE
            return null;
        } catch (final ConversionError e){
            return null;
        } catch (final RuntimeException e){
            return null;
        } finally {
            closeSafe(reader);
        }
    }

    @Override
    public List<Task> parseTasks(final String icalText, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        try {
            return parseTasks(new ByteArrayInputStream(icalText.getBytes(UTF8)), defaultTZ, ctx, errors, warnings);
        } catch (final UnsupportedCharsetException e) {
            LOG.error("", e);
        }
        return new LinkedList<Task>();
    }

    @Override
    public List<Task> parseTasks(final InputStream ical, final TimeZone defaultTZ, final Context ctx, final List<ConversionError> errors, final List<ConversionWarning> warnings) throws ConversionError {
        final List<Task> tasks = new ArrayList<Task>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ical, UTF8));
            while(true) {
                final net.fortuna.ical4j.model.Calendar calendar = parse(reader);
                if(calendar == null) { break; }

                ComponentList todos = calendar.getComponents("VTODO");
                int myLimit = limit < 0 ? todos.size() : limit < todos.size() ? limit : todos.size();

                for(int i = 0; i < myLimit; i++) {
                    final Component vtodo = (Component) todos.get(i);
                    try {
                        tasks.add(convertTask(i, (VToDo) vtodo, defaultTZ, ctx, warnings ));
                    } catch (final ConversionError conversionError) {
                        errors.add(conversionError);
                    }
                }
            }

        } catch (final UnsupportedCharsetException e) {
            // IGNORE
        } finally {
            closeSafe(reader);
        }
        return tasks;
    }

    protected static void closeSafe(final Closeable closeable) {
        Streams.close(closeable);
    }

    protected CalendarDataObject convertAppointment(int index, VEvent vevent, TimeZone defaultTZ, List<AttributeConverter<VEvent,Appointment>> all, Context ctx, List<ConversionWarning> warnings) throws ConversionError {

        final CalendarDataObject appointment = new CalendarDataObject();

        final TimeZone tz = determineTimeZone(vevent, defaultTZ);

        for (final AttributeConverter<VEvent, Appointment> converter : all) {
            if (converter.hasProperty(vevent)) {
                converter.parse(index, vevent, appointment, tz, ctx, warnings);
            }
            converter.verify(index, appointment, warnings);
        }

        /*
         *  Very difficult to keep overview, which timezone has to be used here.
         *  Last deciscion was to use the given timezone from the ical as we need to store it with the appointment
         *  to avoid mix-ups in group-series-appointments passing the DST borders.
         *  See bug 23586
         */
        appointment.setTimezone(tz.getID());

        return appointment;
    }


    protected CalendarDataObject convertAppointment(final int index, final VEvent vevent, final TimeZone defaultTZ, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
        return convertAppointment(index, vevent, defaultTZ, AppointmentConverters.ALL, ctx, warnings);
    }

    protected Task convertTask(final int index, final VToDo vtodo, final TimeZone defaultTZ, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError{
        final TimeZone tz = determineTimeZone(vtodo, defaultTZ);
        final Task task = new Task();
        for (final AttributeConverter<VToDo, Task> converter : TaskConverters.ALL) {
            if (converter.hasProperty(vtodo)) {
                converter.parse(index, vtodo, task, tz, ctx, warnings);
            }
            converter.verify(index, task, warnings);
        }
        return task;
    }


    private static final TimeZone determineTimeZone(final CalendarComponent component,
        final TimeZone defaultTZ){
        for (final String name : new String[] { Property.DTSTART, Property.DTEND, Property.DUE, Property.COMPLETED }) {
            final DateProperty dateProp = (DateProperty) component.getProperty(name);
            if (dateProp != null) {
                return chooseTimeZone(dateProp, defaultTZ);
            }
        }

        return defaultTZ;
    }

    private static final TimeZone chooseTimeZone(final DateProperty dateProperty, final TimeZone defaultTZ) {
        TimeZone tz = defaultTZ;
        if (dateProperty.getParameter("TZID") == null
        || dateProperty.getParameter("TZID").getValue() == null){
                return defaultTZ;
        }
        if(dateProperty.isUtc()) {
        	tz = TimeZone.getTimeZone("UTC");
        }
        Parameter tzid = dateProperty.getParameter("TZID");
		String tzidName = tzid.getValue();
		TimeZone inTZID = TimeZone.getTimeZone(tzidName);

        /* now, if the Java core devs had been smart, they'd made TimeZone.getTimeZone(name,fallback) public. But they did not, so have to do this: */
		if(inTZID.getID().equals("GMT") && ! tzidName.equals("GMT")){
			inTZID = ParserTools.findTzidBySimilarity(tzidName);
		}

        if (null != inTZID) {
            tz = inTZID;
        }
        return tz;
    }

	private String getTimeZoneID(final TimeZone tz) {
        if(net.fortuna.ical4j.model.TimeZone.class.isAssignableFrom(tz.getClass())) {
            return "UTC";
        }
        if(tz.getID().equals("GMT")) { // Hack for VTIMEZONE. iCal4J sets timezone to GMT, though we prefer UTC
            return "UTC";
        }
        return tz.getID();
    }

	protected net.fortuna.ical4j.model.Calendar parse(final BufferedReader reader) throws ConversionError {
	    return parse(reader, null);
	}

    protected net.fortuna.ical4j.model.Calendar parse(final BufferedReader reader, final Collection<Exception> exceptions) throws ConversionError {
        final CalendarBuilder builder = new CalendarBuilder();
        try {
            final StringBuilder chunk = new StringBuilder();
            boolean read = false;
            boolean timezoneStarted = false; //hack to fix bug 11958
            boolean timezoneEnded = false; //hack to fix bug 11958
            boolean timezoneRead = false; //hack to fix bug 11958
            final StringBuilder timezoneInfo = new StringBuilder(); //hack to fix bug 11958
            // Copy until we find an END:VCALENDAR
            boolean beginFound = false;
            for (String line; (line = reader.readLine()) != null;) {
            	if(!beginFound && line.endsWith("BEGIN:VCALENDAR")){
            		line = removeByteOrderMarks(line);
            	}
                if(line.startsWith("BEGIN:VCALENDAR")) {
                    beginFound = true;
                } else if ( !beginFound && !"".equals(line)) {
                    continue; // ignore bad lines between "VCALENDAR" Tags.
                }
                if(!line.startsWith("END:VCALENDAR")){ //hack to fix bug 11958
                	if(line.matches("^\\s*BEGIN:VTIMEZONE")){
                		timezoneStarted = true;
                	}
                    if(!line.matches("\\s*")) {
                        read = true;
                        if(timezoneStarted && !timezoneEnded){ //hack to fix bug 11958
                        	timezoneInfo.append(line).append('\n');
                        } else {
                        	chunk.append(line).append('\n');
                        }
                    }
                	if(line.matches("^\\s*END:VTIMEZONE")){ //hack to fix bug 11958
                		timezoneEnded = true;
                		timezoneRead = true && timezoneStarted;
                	}
                } else {
                    break;
                }
            }
            if(!read) {  return null; }
            chunk.append("END:VCALENDAR\n");
            if(timezoneRead){
            	int locationForInsertion = chunk.indexOf("BEGIN:");
            	if(locationForInsertion > -1){
            		locationForInsertion = chunk.indexOf("BEGIN:", locationForInsertion + 1);
            		if(locationForInsertion > -1){
            			chunk.insert(locationForInsertion, timezoneInfo);
            		}
            	}
            }
            final UnsynchronizedStringReader chunkedReader = new UnsynchronizedStringReader(
            	workaroundFor19463(
            	workaroundFor16895(
            	workaroundFor16613(
            	workaroundFor16367(
            	workaroundFor17492(
            	workaroundFor17963(
            	workaroundFor20453(
            	workaroundFor27706And28942(
            	workaroundFor29282(
            	workaroundFor30027(
            	removeAnnoyingWhitespaces(chunk.toString()
                )))))))))))
            ); // FIXME: Encoding?
            try {
                return builder.build(chunkedReader);
            } catch (NullPointerException e) {
                LOG.warn(composeErrorMessage(e, chunkedReader.getString()), e);
                throw new ConversionError(-1, ConversionWarning.Code.PARSE_EXCEPTION, e, e.getMessage());
            }
        } catch (final IOException e) {
            if (null == exceptions) {
                LOG.error("", e);
            } else {
                exceptions.add(e);
            }
        } catch (final ParserException e) {
            LOG.warn("", e);
            throw new ConversionError(-1, ConversionWarning.Code.PARSE_EXCEPTION, e, e.getMessage());
        }
        return null;
    }

    private String workaroundFor17963(final String input) {
    	return input.replaceAll("EXDATE:(\\d+)([\\n\\r])", "EXDATE:$1T000000$2");
	}

	private String workaroundFor17492(final String input) {
    	return input.replaceAll(";SCHEDULE-AGENT=", ";X-CALDAV-SCHEDULE-AGENT=");
	}

	private String workaroundFor19463(final String input) {
		return input
			.replaceAll("TZOFFSETFROM:\\s*(\\d\\d\\d\\d)", "TZOFFSETFROM:+$1")
			.replaceAll("TZOFFSETTO:\\s*(\\d\\d\\d\\d)",   "TZOFFSETTO:+$1")
			;
	}

	private String workaroundFor20453(final String input) {
		return input
			.replaceAll("DTEND;\\s*\n", "")
			;
	}

	private String workaroundFor29282(final String input) {
	    return input.replaceAll("0000([0-9]{4}T[0-9]{6}Z)", "1970$1");
	}

	private String workaroundFor27706And28942(final String input) {
	    Matcher m = Pattern.compile("DTSTAMP;TZID=([^:]+):\\s*([0-9]{8}T[0-9]{6})").matcher(input);
	    final StringBuffer sb = new StringBuffer(input.length());
	    while (m.find()) {
	        final TimeZone tz = getTimeZone(m.group(1));
	        m.appendReplacement(sb, Strings.quoteReplacement("DTSTAMP:" + getUtcPropertyFrom(m.group(2), tz)));
        }
	    m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

	    m = Pattern.compile("COMPLETED;TZID=([^:]+):\\s*([0-9]{8}T[0-9]{6})").matcher(sb.toString());
        sb.setLength(0);
        while (m.find()) {
            final TimeZone tz = getTimeZone(m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("COMPLETED:" + getUtcPropertyFrom(m.group(2), tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = Pattern.compile("LAST-MODIFIED;TZID=([^:]+):\\s*([0-9]{8}T[0-9]{6})").matcher(sb.toString());
        sb.setLength(0);
        while (m.find()) {
            final TimeZone tz = getTimeZone(m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("LAST-MODIFIED:" + getUtcPropertyFrom(m.group(2), tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = Pattern.compile("CREATED;TZID=([^:]+):\\s*([0-9]{8}T[0-9]{6})").matcher(sb.toString());
        sb.setLength(0);
        while (m.find()) {
            final TimeZone tz = getTimeZone(m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("CREATED:" + getUtcPropertyFrom(m.group(2), tz)));
        }
        m.appendTail(sb);

        // -------------------------------------------------------------------------------------------------- //

        m = Pattern.compile("TRIGGER;TZID=([^:]+):\\s*([0-9]{8}T[0-9]{6})").matcher(sb.toString());
        sb.setLength(0);
        while (m.find()) {
            final TimeZone tz = getTimeZone(m.group(1));
            m.appendReplacement(sb, Strings.quoteReplacement("TRIGGER:" + getUtcPropertyFrom(m.group(2), tz)));
        }
        m.appendTail(sb);

        return sb.toString();
    }

	private static final SimpleDateFormat UTC_PROPERTY;

	static {
	    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
	    sdf.setTimeZone(getTimeZone("UTC"));
	    UTC_PROPERTY = sdf;
	}

	private String getUtcPropertyFrom(final String s, final TimeZone tz) {
	    try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            sdf.setTimeZone(tz);
            final Date d = sdf.parse(s);
            synchronized (UTC_PROPERTY) {
                return UTC_PROPERTY.format(d);
            }
        } catch (final ParseException e) {
            return s;
        }
	}

	private String workaroundFor30027(final String input) {
        final Matcher m = Pattern.compile(":\\s{2,}([0-9]{8}T[0-9]{6}Z?)[ \\t]*").matcher(input);
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
		/* Bug in Zimbra: They like to use an EMAIL element for the
		 * ATTENDEE property, though there is none.
		 */
		return input.replaceAll("ATTENDEE([^\n]*?);EMAIL=", "ATTENDEE$1;X-ZIMBRA-EMAIL=");
	}

	private String workaroundFor16367(final String input) {
        /* Bug in MS Exchange: If you use a CN element, it must have a value.
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
        return input.replaceAll("\nATTACH(.*?);ID=(.+?)([:;])" , "\nATTACH$1$3");
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

	@Override
	public void setLimit(int amount) {
		this.limit = amount;
	}

	private String composeErrorMessage(Exception e, String ical) {
        final StringBuilder sb = new StringBuilder(ical.length() + 256);
        sb.append("Parsing of iCal content failed: ");
        sb.append(e.getMessage());
        if (LOG.isDebugEnabled()) {
            sb.append(". Affected iCal content:").append('\n');
            dumpIcal(ical, sb);
        }
        return sb.toString();
    }

	private void dumpIcal(String ical, StringBuilder sb) {
        String[] lines = Strings.splitByCRLF(ical);
        DecimalFormat df = new DecimalFormat("0000");
        int count = 1;
        for (final String line : lines) {
            sb.append(df.format(count++)).append(' ').append(line).append('\n');
        }
        OutputStreamWriter writer = null;
        try {
            File file = File.createTempFile("parsefailed", ".ics", new File(System.getProperty("java.io.tmpdir")));
            writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
            writer.write(ical);
            writer.flush();
        } catch (IOException e) {
            LOG.error("Problem writing not parsable iCal to tmp directory.", e);
        } finally {
            Streams.close(writer);
        }
    }

}
