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

package com.openexchange.chronos.ical.ical4j;

import static com.openexchange.java.Autoboxing.I;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateProperty;

/**
 * {@link ParserTools}
 *
 * copied from com.openexchange.data.conversion.ical.ical4j.internal.ParserTools
 *
 * @since v7.10.0
 */
public final class ParserTools {

    /**
     * Prevent instantiation.
     */
    private ParserTools() {
        super();
    }

    public static Date parseDate(final CalendarComponent component, final DateProperty property, final TimeZone timeZone) {
        final DateProperty value = (DateProperty) component.getProperty(property.getName());
        Date retval = new Date(value.getDate().getTime());
        if (inDefaultTimeZone(value, timeZone)) {
            retval = recalculate(retval, timeZone);
        }
        return retval;
    }

    /**
     * Parses a date. If the value is a datetime, the timezone will be applied if needed, if the value is a date
     * the time will be 00:00 UTC
     */
    public static Date parseDateConsideringDateType(final CalendarComponent component, final DateProperty property, final TimeZone timeZone) {
        DateProperty value = (DateProperty)component.getProperty(property.getName());
        return toDateConsideringDateType(value, timeZone);
    }

    /**
     * Parses a date. If the value is a datetime, the timezone will be applied if needed, if the value is a date
     * the time will be 00:00 UTC
     */
    public static Date toDateConsideringDateType(final DateProperty value, final TimeZone timeZone) {
        final boolean isDateTime = isDateTime(value);
        final TimeZone UTC = TimeZone.getTimeZone("UTC");
        Date date;
        if (isDateTime) {
            date = new Date(value.getDate().getTime());
            if (inDefaultTimeZone(value, timeZone)) {
                date = recalculate(date, timeZone);
            }
        } else {
            date = toDate(value, UTC);
        }
        return date;
    }

    public static boolean isDateTime(final CalendarComponent component, final DateProperty property) {
        return isDateTime(component, property.getName());
    }

    public static boolean isDateTime(final CalendarComponent component, final String name) {
        final DateProperty value = (DateProperty) component.getProperty(name);
        return isDateTime(value);
    }

    public static boolean isDateTime(final DateProperty value) {
        return value.getDate() instanceof DateTime;
    }

    /**
     * Returns whether the specified property is in UTC
     *
     * @param dateProperty The date property
     * @param timeZone The timezone
     * @return <code>true</cod> if the date is in UTC timezone; <code>false</code> otherwise 
     */
    public static boolean inDefaultTimeZone(final DateProperty dateProperty, final TimeZone timeZone) {
        if (dateProperty.getParameter("TZID") != null) {
            return false;
        }
        return !dateProperty.isUtc();
    }

    /**
     * Transforms date from the default timezone to the date in the given timezone.
     */
    public static Date recalculate(final Date date, final TimeZone timeZone) {

        final java.util.Calendar inDefault = new GregorianCalendar();
        inDefault.setTime(date);

        final java.util.Calendar inTimeZone = new GregorianCalendar();
        inTimeZone.setTimeZone(timeZone);
        inTimeZone.set(
            inDefault.get(java.util.Calendar.YEAR),
            inDefault.get(java.util.Calendar.MONTH),
            inDefault.get(java.util.Calendar.DATE),
            inDefault.get(java.util.Calendar.HOUR_OF_DAY),
            inDefault.get(java.util.Calendar.MINUTE),
            inDefault.get(java.util.Calendar.SECOND));
        inTimeZone.set(java.util.Calendar.MILLISECOND, 0);
        return inTimeZone.getTime();
    }

    /**
     * Transforms date from the default timezone to midnight in the given timezone.
     */
    public static Date recalculateMidnight(final Date date, final TimeZone timeZone) {

        final java.util.Calendar inDefault = new GregorianCalendar();
        inDefault.setTime(date);

        final java.util.Calendar inTimeZone = new GregorianCalendar();
        inTimeZone.setTimeZone(timeZone);
        inTimeZone.set(
            inDefault.get(java.util.Calendar.YEAR),
            inDefault.get(java.util.Calendar.MONTH),
            inDefault.get(java.util.Calendar.DATE),
            0,0,0);
        inTimeZone.set(java.util.Calendar.MILLISECOND, 0);
        return inTimeZone.getTime();
    }

    /**
     * Converts the specified {@link DateProperty} to {@link Date} 
     *
     * @param dateProperty The date property to convert
     * @param tz The timezone
     * @return The {@link Date}
     */
    public static Date toDate(final DateProperty dateProperty, final TimeZone tz) {
        return new Date(dateProperty.getDate().getTime());
    }

    public static Date recalculateAsNeeded(final net.fortuna.ical4j.model.Date icaldate, final Property property, final TimeZone tz) {
        boolean mustRecalculate = true;
        if (property.getParameter("TZID") != null) {
            mustRecalculate = false;
        } else if (DateTime.class.isInstance(icaldate)) {
            final DateTime dateTime = (DateTime) icaldate;
            mustRecalculate = !dateTime.isUtc();
        }
        if (mustRecalculate) {
            return ParserTools.recalculate(icaldate, tz);
        }
        return new Date(icaldate.getTime());
    }

    /**
     * Takes an Outlook timezone id and guesses the appropriate
     * Java timezone. It does so by counting all timezones that
     * have somewhat matching names, sorts them by their UTC
     * offset and chooses one of those that have the most often
     * used offset, e.g. if you have three zones that have a +1
     * offset and one with +2, you'll get one of the +1 ones.
     *
     * This thing would be way better if we had the timezone
     * information that we probably painfully extracted somewhere
     * in ical4j but got rid off again before this method is
     * needed.
     */
    public static TimeZone findTzidBySimilarity(String tzidName) {
    	if ("Z".equals(tzidName)){
    		return TimeZone.getTimeZone("Zulu");
    	}
    	//generate name variations of the outlook timezone name
    	List<String> candidates1 = new LinkedList<String>();
    	if (tzidName.indexOf(',') >= 0){
    		String[] split = tzidName.split(",");
    		for(String tmp: split) {
                candidates1.add(tmp.trim());
            }
    	} else {
    		candidates1.add(tzidName);
    	}
    	//compare all Java timezones to the candidates
    	String[] availableIDs = TimeZone.getAvailableIDs();
    	List<TimeZone> candidates2 = new LinkedList<TimeZone>();
    	for(String javaId: availableIDs){
    		for(String idPart: candidates1){
    			if (javaId.equals(idPart)){
    				candidates2.add(TimeZone.getTimeZone(javaId));
    			}
    			if (javaId.equalsIgnoreCase(idPart)){
    				candidates2.add(TimeZone.getTimeZone(javaId));
    			}
    			if (javaId.contains(idPart)){
    				candidates2.add(TimeZone.getTimeZone(javaId));
    			}
    			if (javaId.toLowerCase().contains(idPart.toLowerCase())){
    				candidates2.add(TimeZone.getTimeZone(javaId));
    			}
    		}
    	}
		//now count how many different offsets there are
    	BidiMap occurrences = new TreeBidiMap();
    	int highestNumberOccurrences = 0;
        for (TimeZone cand : candidates2) {
            Integer offset = I(cand.getRawOffset());
            if (!occurrences.containsKey(offset)) {
                occurrences.put(offset, I(0));
            }
            int numOccurrences = ((Integer) occurrences.get(offset)).intValue() + 1;
            occurrences.put(offset, I(numOccurrences));
            highestNumberOccurrences = highestNumberOccurrences < numOccurrences ? numOccurrences : highestNumberOccurrences;
        }
    	//select the most often occurring ones and take the one with the shortest name (probably a generic name)
    	Integer mostCommonOffset = (Integer) occurrences.getKey(I(highestNumberOccurrences));
    	int maxlength = Integer.MAX_VALUE;
    	TimeZone candidate = null;
    	for(TimeZone cand: candidates2){
    		if (cand.getRawOffset() == mostCommonOffset.intValue()){
    			int l2 = cand.getID().length();
    			if (l2 < maxlength){
    				candidate = cand;
    				maxlength = l2;
    			}
    		}
    	}
    	return candidate;
	}

    public static final TimeZone chooseTimeZone(final DateProperty dateProperty, final TimeZone defaultTZ) {
        TimeZone tz = defaultTZ;
        if (dateProperty.getParameter("TZID") == null
        || dateProperty.getParameter("TZID").getValue() == null){
                return defaultTZ;
        }
        if (dateProperty.isUtc()) {
        	tz = TimeZone.getTimeZone("UTC");
        }
        Parameter tzid = dateProperty.getParameter("TZID");
		String tzidName = tzid.getValue();
		TimeZone inTZID = TimeZone.getTimeZone(tzidName);

        /* now, if the Java core devs had been smart, they'd made TimeZone.getTimeZone(name,fallback) public. But they did not, so have to do this: */
		if (inTZID.getID().equals("GMT") && ! tzidName.equals("GMT")){
			inTZID = ParserTools.findTzidBySimilarity(tzidName);
		}

        if (null != inTZID) {
            tz = inTZID;
        }
        return tz;
    }

    /**
     * Gets a value indicating whether the supplied {@link DateProperty} holds a value that appears to be a date or it's time part refers
     * to midnight (<code>00:00:00</code>). In particular, this method returns <code>true</code> if
     * <ul>
     * <li>The property is marked as date explicitly (via <code>VALUE=DATE</code> parameter)</li>
     * <li>The property's value matches the date format <code>yyyyMMdd'T000000'</code></li>
     * <li>The property's value matches the date format <code>yyyyMMdd</code> <i>and</i> has a length of <code>8</code></li>
     * </ul>
     * <p/>
     * This method might be used to double-check the plausibility of the <code>X-MICROSOFT-CDO-ALLDAYEVENT</code> property.
     *
     * @param dateProperty The date property to check
     * @return <code>true</code> if the date property's value is either marked as date or is a date with time at 00:00:00
     */
    public static boolean isDateOrMidnight(Property dateProperty) {
        if (null != dateProperty) {
            if (Value.DATE.equals(dateProperty.getParameter(Parameter.VALUE))) {
                return true;
            }
            String value = dateProperty.getValue();
            if (Strings.isNotEmpty(value)) {
                try {
                    new SimpleDateFormat("yyyyMMdd'T000000'").parse(value);
                    return true;
                } catch (ParseException e) {
                    // not midnight
                }
                try {
                    new SimpleDateFormat("yyyyMMdd").parse(value);
                    if (8 == value.length()) {
                        return true;
                    }
                } catch (ParseException e) {
                    // not date only
                }
            }
        }
        return false;
    }

}
