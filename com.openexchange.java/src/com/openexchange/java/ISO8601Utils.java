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

package com.openexchange.java;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link ISO8601Utils} - Utilities methods for manipulating dates in iso8601 format.
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
 * This is a copy of <code>org.codehaus.jackson.map.util.ISO8601Utils</code> from <a href="https://github.com/FasterXML/jackson">FasterXML</a>.
 * </div>
 * <p>
 * This is much much faster and GC friendly than using <code>SimpleDateFormat</code> so highly suitable if you un-/serialize lots of
 * {@link Date date objects}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public enum ISO8601Utils {

    ;

    /** ID to represent the 'GMT' string */
    private static final String GMT_ID = "GMT";

    /** The GMT time zone */
    private static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone(GMT_ID);

    /**
     * Format a date into 'yyyy-MM-ddThh:mm:ssZ' (GMT timezone, no milliseconds precision)
     *
     * @param date the date to format
     * @return the date formatted as 'yyyy-MM-ddThh:mm:ssZ'
     */
    public static String format(Date date) {
        return format(date, false, TIMEZONE_GMT);
    }

    /**
     * Format a date into 'yyyy-MM-ddThh:mm:ss[.sss]Z' (GMT timezone)
     *
     * @param date   the date to format
     * @param millis true to include millis precision otherwise false
     * @return the date formatted as 'yyyy-MM-ddThh:mm:ss[.sss]Z'
     */
    public static String format(Date date, boolean millis) {
        return format(date, millis, TIMEZONE_GMT);
    }

    /**
     * Format date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     *
     * @param date   the date to format
     * @param millis true to include millis precision otherwise false
     * @param tz     timezone to use for the formatting (GMT will produce 'Z')
     * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     */
    public static String format(Date date, boolean millis, TimeZone tz) {
        Calendar calendar = new GregorianCalendar(tz, Locale.US);
        calendar.setTime(date);

        // estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
        int capacity = "yyyy-MM-ddThh:mm:ss".length();
        capacity += millis ? ".sss".length() : 0;
        capacity += tz.getRawOffset() == 0 ? "Z".length() : "+hh:mm".length();
        StringBuilder formatted = new StringBuilder(capacity);

        padInt(formatted, calendar.get(Calendar.YEAR), "yyyy".length());
        formatted.append('-');
        padInt(formatted, calendar.get(Calendar.MONTH) + 1, "MM".length());
        formatted.append('-');
        padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), "dd".length());
        formatted.append('T');
        padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), "hh".length());
        formatted.append(':');
        padInt(formatted, calendar.get(Calendar.MINUTE), "mm".length());
        formatted.append(':');
        padInt(formatted, calendar.get(Calendar.SECOND), "ss".length());
        if (millis) {
            formatted.append('.');
            padInt(formatted, calendar.get(Calendar.MILLISECOND), "sss".length());
        }

        int offset = tz.getOffset(calendar.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            formatted.append(offset < 0 ? '-' : '+');
            padInt(formatted, hours, "hh".length());
            formatted.append(':');
            padInt(formatted, minutes, "mm".length());
        } else {
            formatted.append('Z');
        }

        return formatted.toString();
    }


    /**
     * Parse a date from ISO-8601 formatted string. It expects a format yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     *
     * @param date ISO string to parse in the appropriate format.
     * @return the parsed date
     * @throws IllegalArgumentException if the date is not in the appropriate format
     */
    public static Date parse(String date) {
        try {
            int offset = 0;

            // extract year
            int year = parseInt(date, offset, offset += 4);
            checkOffset(date, offset, '-');

            // extract month
            int month = parseInt(date, offset += 1, offset += 2);
            checkOffset(date, offset, '-');

            // extract day
            int day = parseInt(date, offset += 1, offset += 2);

            // extract hours, minutes, seconds and milliseconds as well as time zone
            int hour, minutes, seconds, milliseconds;
            TimeZone timezone;
            if (offset < date.length()) {
                checkOffset(date, offset, 'T');

                hour = parseInt(date, offset += 1, offset += 2);
                checkOffset(date, offset, ':');

                minutes = parseInt(date, offset += 1, offset += 2);
                checkOffset(date, offset, ':');

                seconds = parseInt(date, offset += 1, offset += 2);
                // milliseconds can be optional in the format
                milliseconds = 0; // always use 0 otherwise returned date will include millis of current time
                if (date.charAt(offset) == '.') {
                    checkOffset(date, offset, '.');
                    milliseconds = parseInt(date, offset += 1, offset += 3);
                }

                // extract timezone
                String timezoneId;
                char timezoneIndicator = date.charAt(offset);
                if (timezoneIndicator == '+' || timezoneIndicator == '-') {
                    timezoneId = GMT_ID + date.substring(offset);
                } else if (timezoneIndicator == 'Z') {
                    timezoneId = GMT_ID;
                } else {
                    throw new IndexOutOfBoundsException("Invalid time zone indicator " + timezoneIndicator);
                }
                timezone = TimeZone.getTimeZone(timezoneId);
                if (!timezone.getID().equals(timezoneId)) {
                    throw new IndexOutOfBoundsException();
                }
            } else {
                hour = 0;
                minutes = 0;
                seconds = 0;
                milliseconds = 0;
                timezone = TimeZone.getTimeZone(GMT_ID);
            }


            Calendar calendar = new GregorianCalendar(timezone);
            calendar.setLenient(false);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, seconds);
            calendar.set(Calendar.MILLISECOND, milliseconds);

            return calendar.getTime();
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Failed to parse date " + date, e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse date " + date, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse date " + date, e);
        }
    }

    /**
     * Check if the expected character exist at the given offset of the
     *
     * @param value    the string to check at the specified offset
     * @param offset   the offset to look for the expected character
     * @param expected the expected character
     * @throws IndexOutOfBoundsException if the expected character is not found
     */
    private static void checkOffset(String value, int offset, char expected) throws IndexOutOfBoundsException {
        char found = value.charAt(offset);
        if (found != expected) {
            throw new IndexOutOfBoundsException("Expected '" + expected + "' character but found '" + found + "'");
        }
    }

    /**
     * Parse an integer located between 2 given offsets in a string
     *
     * @param value      the string to parse
     * @param beginIndex the start index for the integer in the string
     * @param endIndex   the end index for the integer in the string
     * @return the int
     * @throws NumberFormatException if the value is not a number
     */
    private static int parseInt(String value, int beginIndex, int endIndex) throws NumberFormatException {
        if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
            throw new NumberFormatException(value);
        }
        // use same logic as in Integer.parseInt() but less generic we're not supporting negative values
        int i = beginIndex;
        int result = 0;
        int digit;
        if (i < endIndex) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value);
            }
            result = -digit;
        }
        while (i < endIndex) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value);
            }
            result *= 10;
            result -= digit;
        }
        return -result;
    }

    /**
     * Zero pad a number to a specified length
     *
     * @param buffer buffer to use for padding
     * @param value  the integer value to pad if necessary.
     * @param length the length of the string we should zero pad
     */
    private static void padInt(StringBuilder buffer, int value, int length) {
        String strValue = Integer.toString(value);
        for (int i = length - strValue.length(); i > 0; i--) {
            buffer.append('0');
        }
        buffer.append(strValue);
    }

}
