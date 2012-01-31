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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.caldav;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Patches}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Patches {

    private Patches() {
    	// prevent instantiation
    }
    
    /**
     * {@link Incoming}
     * 
     * Patches for incoming iCal files.
     * 
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Incoming {
    	
        private static final Pattern RRULE_UNTIL = Pattern.compile("(^RRULE:(?:.+;)?UNTIL=)(\\d{8})(T\\d{6}Z)(;|$)", Pattern.MULTILINE);

        private Incoming() {
        	// prevent instantiation
        }
    	
        /**
         * Applies all known patches to the supplied incoming iCal file.
         * @param iCal
         * @return
         */
        public static String applyAll(final String iCal) {
        	return correctRRuleUntil(iCal);
        }
        
        /**
		 * Removes the time specific part from an UNTIL field containing a 
		 * date-time value. 
		 * 
		 * For example, the following RRULE's semantic means that the last 
		 * occurrence of the rule should be on 2012-02-09:
		 * <code>RRULE:FREQ=DAILY;INTERVAL=1;UNTIL=20120209T235959Z</code>
		 * 
		 * Inside OX, that means that the "until" property of the appointment 
		 * series should be set to 2012-02-09 (UTC), too, which is achieved by 
		 * replaying the UNTIL-part in the RRULE with a date-only-value.
		 * 
		 * So, the time specific part from an UNTIL field containing a 
		 * date-time value is removed here, resulting in the above line 
		 * getting changed to: 
		 * <code>RRULE:FREQ=DAILY;INTERVAL=1;UNTIL=20120209</code>
		 * 
         * @param iCal
         * @return
         */
    	public static String correctRRuleUntil(final String iCal) {
        	return RRULE_UNTIL.matcher(iCal).replaceAll("$1$2$4");
        }
    }
    
    /**
     * {@link Outgoing}
     * 
     * Patches for outgoing iCal files.
     * 
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Outgoing {
    	
        private static final Pattern EMPTY_RDATE = Pattern.compile("^RDATE:\\r\\n", Pattern.MULTILINE);
        private static final Pattern RRULE_UNTIL= Pattern.compile("(^RRULE:(?:.+;)?UNTIL=)(\\d{8})(;|$)", Pattern.MULTILINE);
        
        private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
        private static final SimpleDateFormat DATE_FORMAT;
        private static final SimpleDateFormat DATETIME_FORMAT;
        
        static {
        	DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
        	DATE_FORMAT.setTimeZone(UTC);
        	DATETIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        	DATETIME_FORMAT.setTimeZone(UTC);
        }

        private Outgoing() {
        	// prevent instantiation
        }

        /**
         * Applies all known patches to the supplied outgoing iCal file.
         * @param iCal
         * @return
         * @throws ParseException 
         */
        public static String applyAll(final String iCal) throws ParseException {
        	return correctRRuleUntil(removeEmptyRDates(iCal));
        }

        /**
         * Corrects UNTIL fields containing a date without time information
         * by adding time-specific information.
    	 * 
    	 * For example, in the OX world, the following RRULE's semantic means 
    	 * that the last occurrence of the rule should be on 2012-02-09:
    	 * <code>RRULE:FREQ=DAILY;INTERVAL=1;UNTIL=20120209</code>
    	 *  
    	 * But since the client expects this information to be a date-time,
    	 * one second before the next date, this part is added to the date, 
    	 * resulting in the above line getting changed to:
    	 * <code>RRULE:FREQ=DAILY;INTERVAL=1;UNTIL=20120209T235959Z</code>
         * 
         * @param iCal
         * @return
         * @throws ParseException 
         */
        public static String correctRRuleUntil(final String iCal) throws ParseException {
        	final StringBuffer stringBuffer = new StringBuffer();
        	final Matcher matcher = RRULE_UNTIL.matcher(iCal);        	
            while (matcher.find()) {
            	final Date originalUntil = DATE_FORMAT.parse(matcher.group(2));
            	final Calendar calendar = Calendar.getInstance(UTC);
            	calendar.setTime(originalUntil);
            	calendar.add(Calendar.SECOND, -1);
        		final String correctedUntil = DATETIME_FORMAT.format(calendar.getTime());
        		matcher.appendReplacement(stringBuffer, matcher.group(1) + correctedUntil + matcher.group(3));
            }
            matcher.appendTail(stringBuffer);
            return stringBuffer.toString();
        }
        
        /**
         * Removes empty RDATE components without information in iCal strings 
         * (those are generated by the {@link VTimeZone} onsets)
         * 
         * @param iCal
         * @return
         */
        public static String removeEmptyRDates(final String iCal) {
        	return EMPTY_RDATE.matcher(iCal).replaceAll("");
        }    	
    }
}
