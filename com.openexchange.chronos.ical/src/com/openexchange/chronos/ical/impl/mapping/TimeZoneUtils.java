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

package com.openexchange.chronos.ical.impl.mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.io.TimezoneInfo;
import biweekly.property.ICalProperty;

import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

/**
 * {@link TimeZoneUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class TimeZoneUtils {
	
	private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");
	
	private static final Set<String> OLSON_AREAS = com.openexchange.tools.arrays.Collections.unmodifiableSet( 
			"Africa", "America", "Antarctica", "Arctic", "Asia", "Atlantic", "Australia", "Europe", "Indian", "Pacific");
	
	public static TimeZone selectTimeZone(String parsedTimeZoneID, TimeZone fallback) {
		return selectTimeZone(parsedTimeZoneID, TimeZone.getAvailableIDs(), fallback);
	}
	
	public static TimeZone selectTimeZone(TimeZone parsedTimeZone, TimeZone fallback) {
		/*
		 * prefer a default Java timezone matching the identifier if possible
		 */
		if (Strings.isNotEmpty(parsedTimeZone.getID())) {
			TimeZone matchingTimeZone = optTimeZone(parsedTimeZone.getID(), null);
			if (null != matchingTimeZone && matchingTimeZone.getRawOffset() == parsedTimeZone.getRawOffset()) {
				return matchingTimeZone;
			}
		}
		return selectTimeZone(parsedTimeZone.getID(), TimeZone.getAvailableIDs(parsedTimeZone.getRawOffset()), fallback);
	}
	
	private static TimeZone selectTimeZone(String parsedTimeZoneID, String[] availableIDs, TimeZone fallback) {
		/*
		 * prefer a default Java timezone by identifier if possible
		 */
		if (Strings.isNotEmpty(parsedTimeZoneID)) {
			TimeZone matchingTimeZone = optTimeZone(parsedTimeZoneID, fallback);
			if (null != matchingTimeZone) {
				return matchingTimeZone;
			}
		}
		if (null != availableIDs && 0 < availableIDs.length) {
			String timezoneID = getMostSimilarID(parsedTimeZoneID, availableIDs);
			if (null != timezoneID) {
				return TimeZone.getTimeZone(timezoneID);
			}
		}
		/*
		 * no matching timezone found
		 */
		return fallback;
	}
	

    public static TimeZone selectTimeZone(biweekly.property.DateOrDateTimeProperty property, ICalParameters parameters) {
		if (property.getValue().getRawComponents().isUtc()) {
			return TimeZones.UTC;			
		}
		TimeZone parsedTimeZone = getTimeZone(parameters, property);
		if (null == parsedTimeZone) {
			String tzidParameter = property.getParameter("TZID");
			if (Strings.isNotEmpty(tzidParameter)) {
				return selectTimeZone(tzidParameter, TimeZone.getAvailableIDs(), null);
			} else {
				return null;
			}
		}
		return selectTimeZone(parsedTimeZone.getID(), TimeZone.getAvailableIDs(parsedTimeZone.getRawOffset()), null);
    }

	private static Set<String> extractWords(String timezoneID) {
		if (null == timezoneID) {
			return Collections.emptySet();
		}
		Set<String> words = new HashSet<String>();
		Matcher matcher = WORD_PATTERN.matcher(timezoneID);
		while (matcher.find()) {
			words.add(matcher.group().toLowerCase());			
		}
		return words;
	}
	
	private static String getOlsonArea(Set<String> words) {
		for (String olsonArea : OLSON_AREAS) {
			if (words.contains(olsonArea)) {
				return olsonArea;
			}
		}
		return null;
	}
	
	private static int calculateSimilarity(Set<String> timezoneID1words, String timezoneID2) {
		int similarity = 0;
		Set<String> timezoneID2words = extractWords(timezoneID2);
		if (timezoneID1words.containsAll(timezoneID2words) || timezoneID2words.containsAll(timezoneID1words)) {
			return 10 * (timezoneID1words.size() + timezoneID2words.size()); // match
		}
		if (1 == timezoneID2words.size() && timezoneID2.toUpperCase().equals(timezoneID2)) {
			similarity += 20; // bonus for abbreviation or common names
		}
		String timezoneID1Area = getOlsonArea(timezoneID1words);
		String timezoneID2Area = getOlsonArea(timezoneID2words);
		if (null == timezoneID1Area && null != timezoneID2Area ||
			null != timezoneID1Area && null == timezoneID2Area) {
			similarity -= 10; // malus for area mismatch
		}
		if (null != timezoneID1Area && timezoneID1Area.equals(timezoneID2Area)) {
			similarity += 10; // bonus for area match
		}		
		for (String word : timezoneID2words) {
			if (timezoneID1words.contains(word)) {
				similarity += 5;
			}			
		}
		return similarity;
	}
	
	private static String getMostSimilarID(String timezoneID, String[] availableIDs) {
		if (null == availableIDs || 0 == availableIDs.length) {
			return null;
		}
		if (1 == availableIDs.length) {
			return availableIDs[0];
		}
		Set<String> timezoneIDwords = extractWords(timezoneID);
		String currentID = availableIDs[0];
		int currentSimilarity = calculateSimilarity(timezoneIDwords, currentID);		
		for (int i = 1; i < availableIDs.length; i++) {
			String id = availableIDs[i];
			int similarity = calculateSimilarity(timezoneIDwords, id);
			if (similarity > currentSimilarity || similarity == currentSimilarity && id.length() < currentID.length()) {
				currentID = id;
				currentSimilarity = similarity;
			}
		}
		return currentID;
	}
	
    private static TimeZone getTimeZone(ICalParameters parameters, ICalProperty property) {
		TimezoneInfo timezoneInfo = parameters.get(ICalParameters.TIMEZONE_INFO, TimezoneInfo.class);
		if (null != timezoneInfo) {
			return timezoneInfo.getTimeZone(property);
		}
		return null;
    }

	/**
	 * Optionally gets the Java timezone for a given identifier.
	 * 
	 * @param id The timezone identifier
	 * @param fallback The fallback timezone to return if no matching timezone was found
	 * @return The matching Java timezone, or the fallback if not found
	 */
	private static TimeZone optTimeZone(String id, TimeZone fallback) {
		TimeZone timeZone = TimeZone.getTimeZone(id);
		if ("GMT".equals(timeZone.getID()) && false == "GMT".equalsIgnoreCase(id)) {
			return null;
		}
		return timeZone;
	}
	
}
