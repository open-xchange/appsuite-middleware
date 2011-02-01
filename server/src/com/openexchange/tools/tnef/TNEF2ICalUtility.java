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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.tools.tnef;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIPropName;
import net.freeutils.tnef.MAPIProps;


/**
 * {@link TNEF2ICalUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TNEF2ICalUtility {

    /**
     * Initializes a new {@link TNEF2ICalUtility}.
     */
    private TNEF2ICalUtility() {
        super();
    }

    /**
     * Generates an appropriate {@link DateTime} instance from given date.
     * 
     * @param date The date
     * @return The {@link DateTime} instance
     */
    public static DateTime toDateTime(final java.util.Date date) {
        // TODO add default timezone
        final DateTime retval = new DateTime(true);
        retval.setTime(date.getTime());
        return retval;
    }

    private static final TimeZoneRegistry TIME_ZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    /**
     * Generates an appropriate {@link DateTime} instance from given date.
     * 
     * @param date The date
     * @param tzid The optional time zone identifier; may be <code>null</code>
     * @return The {@link DateTime} instance
     */
    public static DateTime toDateTime(final java.util.Date date, final String tzid) {
        if (tzid == null) {
            return toDateTime(date);
        }

        final net.fortuna.ical4j.model.TimeZone ical4jTimezone = TIME_ZONE_REGISTRY.getTimeZone(tzid);
        if (ical4jTimezone == null) {
            return toDateTime(date);
        }

        final DateTime retval = new DateTime(false);
        retval.setTimeZone(ical4jTimezone);
        retval.setTime(date.getTime());
        return retval;
    }

    public static Date pureISOToLocalQDateTime(final String dtStr) {
        return pureISOToLocalQDateTime(dtStr, false);
    }

    public static Date pureISOToLocalQDateTime(final String dtStr, final boolean bDateOnly) {
        final Calendar cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        int year, month, day, hour, minute, second;
        if (bDateOnly) {
            year = Integer.parseInt(left(dtStr, 4));
            month = Integer.parseInt(mid(dtStr, 4, 2));
            day = Integer.parseInt(mid(dtStr, 6, 2));
            hour = 0;
            minute = 0;
            second = 0;
        } else {
            year = Integer.parseInt(left(dtStr, 4));
            month = Integer.parseInt(mid(dtStr, 4, 2));
            day = Integer.parseInt(mid(dtStr, 6, 2));
            hour = Integer.parseInt(mid(dtStr, 9, 2));
            minute = Integer.parseInt(mid(dtStr, 11, 2));
            second = Integer.parseInt(mid(dtStr, 13, 2));
        }
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        if (!bDateOnly) {
            /*
             * Correct for GMT ( == Zulu time == UTC )
             */
            if (dtStr.charAt(dtStr.length() - 1) == 'Z') {
                // TODO:
            }
        }

        return cal.getTime();
    }

    /**
     * Extracts the substring from specified position with guiven length.
     * 
     * @param s The string to extract from
     * @param pos The position
     * @param len The length
     * @return The extracted string
     */
    public static String mid(final String s, final int pos, final int len) {
        if (null == s) {
            return null;
        }
        if (len < 0) {
            return s.substring(pos);
        }
        final char[] ca = s.toCharArray();
        final int length = Math.min(ca.length, pos + len);
        final StringBuilder sb = new StringBuilder(len);
        for (int i = pos; i < length; i++) {
            sb.append(ca[i]);
        }
        return sb.toString();
    }

    /**
     * Gets the <i>n</i> leftmost characters from specified string.
     * 
     * @param s The string
     * @param n The number of leftmost characters
     * @return The string with <i>n</i> leftmost characters
     */
    private static String left(final String s, final int n) {
        if (null == s) {
            return null;
        }
        final char[] ca = s.toCharArray();
        final int length = ca.length;
        final StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n && i < length; i++) {
            sb.append(ca[i]);
        }
        return sb.toString();
    }

    /**
     * Removes specified characters from given string.
     * 
     * @param s The string
     * @param chars The characters to remove
     * @return The string with characters removed
     */
    public static String remove(final String s, final char... chars) {
        if (null == s) {
            return null;
        }
        final char[] ca = s.toCharArray();
        final int length = ca.length;
        final StringBuilder sb = new StringBuilder(length);
        Arrays.sort(chars);
        for (int i = 0; i < length; i++) {
            final char cur = ca[i];
            if (Arrays.binarySearch(chars, cur) < 0) {
                sb.append(cur);
            }
        }
        return sb.toString();
    }

    /**
     * Checks if given string is empty.
     * 
     * @param s The string to check
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public static boolean isEmpty(final String s) {
        if (null == s) {
            return true;
        }
        boolean whiteSpace = true;
        final char[] chars = s.toCharArray();
        final int length = chars.length;
        for (int i = 0; whiteSpace && i < length; i++) {
            whiteSpace &= Character.isWhitespace(chars[i]);
        }
        return whiteSpace;
    }

    /**
     * Finds a property by ID.
     * 
     * @param <V> The return type to cast to
     * @param id The property ID
     * @param mapiProps The MAPI properties to search in
     * @return The found property's value or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static <V> V findProp(final int id, final MAPIProps mapiProps) throws IOException {
        return findProp(id, (V) null, mapiProps);
    }

    /**
     * Finds a property by ID.
     * 
     * @param <V> The return type to cast to
     * @param id The property ID
     * @param fallback The fallback if missing
     * @param mapiProps The MAPI properties to search in
     * @return The found property's value or fallback if missing
     * @throws IOException If an I/O error occurs
     */
    public static <V> V findProp(final int id, final V fallback, final MAPIProps mapiProps) throws IOException {
        @SuppressWarnings("unchecked") final V retval = (V) mapiProps.getPropValue(id);
        return null == retval ? fallback : retval;
    }

    /**
     * Finds a property by ID.
     * 
     * @param id The property ID
     * @param mapiProps The MAPI properties to search in
     * @return The string of found property's value or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static String findPropString(final int id, final MAPIProps mapiProps) throws IOException {
        return findPropString(id, null, mapiProps);
    }

    /**
     * Finds a property by ID.
     * 
     * @param id The property ID
     * @param fallback The fallback if missing
     * @param mapiProps The MAPI properties to search in
     * @return The string of found property's value or fallback if missing
     * @throws IOException If an I/O error occurs
     */
    public static String findPropString(final int id, final String fallback, final MAPIProps mapiProps) throws IOException {
        final Object retval = mapiProps.getPropValue(id);
        return null == retval ? fallback : retval.toString();
    }

    /**
     * Find property by name.
     * 
     * @param name The name
     * @param mapiProps The MAPI properties to search in
     * @return The string of found property's value or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static <V> V findNamedProp(final String name, final MAPIProps mapiProps) throws IOException {
        return findNamedProp(name, (V) null, mapiProps);
    }

    /**
     * Find property by name.
     * 
     * @param name The name
     * @param fallback The fallback value
     * @param mapiProps The MAPI properties to search in
     * @return The string of found property's value or fallback value
     * @throws IOException If an I/O error occurs
     */
    public static <V> V findNamedProp(final String name, final V fallback, final MAPIProps mapiProps) throws IOException {
        if (name.startsWith("0x")) {
            final int id = Integer.parseInt(name.substring(2), 16);
            for (final MAPIProp mapiProp : mapiProps.getProps()) {
                final MAPIPropName propName = mapiProp.getName();
                if (null != propName && propName.getID() == id) {
                    return (V) mapiProp.getValue();
                }
            }
        } else {
            for (final MAPIProp mapiProp : mapiProps.getProps()) {
                final MAPIPropName propName = mapiProp.getName();
                if (null != propName) {
                    final String n = propName.getName();
                    if (n != null && n.equals(name)) {
                        return (V) mapiProp.getValue();
                    }
                }
            }
        }
        return fallback;
    }

    /**
     * Find property by name.
     * 
     * @param name The name
     * @param mapiProps The MAPI properties to search in
     * @return The string of found property's value or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static String findNamedPropString(final String name, final MAPIProps mapiProps) throws IOException {
        return findNamedPropString(name, null, mapiProps);
    }

    /**
     * Find property by name.
     * 
     * @param name The name
     * @param fallback The fallback value
     * @param mapiProps The MAPI properties to search in
     * @return The string of found property's value or fallback value
     * @throws IOException If an I/O error occurs
     */
    public static String findNamedPropString(final String name, final String fallback, final MAPIProps mapiProps) throws IOException {
        if (name.startsWith("0x")) {
            final int id = Integer.parseInt(name.substring(2), 16);
            for (final MAPIProp mapiProp : mapiProps.getProps()) {
                final MAPIPropName propName = mapiProp.getName();
                if (null != propName && propName.getID() == id) {
                    return mapiProp.getValue().toString();
                }
            }
        } else {
            for (final MAPIProp mapiProp : mapiProps.getProps()) {
                final MAPIPropName propName = mapiProp.getName();
                if (null != propName) {
                    final String n = propName.getName();
                    if (n != null && n.equals(name)) {
                        return mapiProp.getValue().toString();
                    }
                }
            }
        }
        return fallback;
    }
    
}
