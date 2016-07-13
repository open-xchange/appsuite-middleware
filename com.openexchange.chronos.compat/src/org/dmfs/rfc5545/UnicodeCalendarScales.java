/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.rfc5545;

import java.util.HashMap;
import java.util.Map;

import org.dmfs.rfc5545.calendarmetrics.CalendarMetrics;
import org.dmfs.rfc5545.calendarmetrics.CalendarMetrics.CalendarMetricsFactory;
import org.dmfs.rfc5545.calendarmetrics.GregorianCalendarMetrics;
import org.dmfs.rfc5545.calendarmetrics.IslamicCalendarMetrics;
import org.dmfs.rfc5545.calendarmetrics.IslamicCalendarMetrics.LeapYearPattern;
import org.dmfs.rfc5545.calendarmetrics.JulianCalendarMetrics;


/**
 * Helper class to convert Unicode calendar names to {@link CalendarMetrics}.
 * 
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @see <a
 *      href="http://www.unicode.org/repos/cldr/tags/latest/common/bcp47/calendar.xml>http://www.unicode.org/repos/cldr/tags/latest/common/bcp47/calendar.xml</a
 *      >
 */
public final class UnicodeCalendarScales
{

	private final static Map<String, CalendarMetricsFactory> CALENDAR_SCALES = new HashMap<String, CalendarMetricsFactory>(10);


	public static CalendarMetricsFactory getCalendarMetricsForName(String calendarScaleName)
	{
		return CALENDAR_SCALES.get(calendarScaleName);
	}

	static
	{
		CALENDAR_SCALES.put(GregorianCalendarMetrics.CALENDAR_SCALE_ALIAS, GregorianCalendarMetrics.FACTORY);
		CALENDAR_SCALES.put(GregorianCalendarMetrics.CALENDAR_SCALE_NAME, GregorianCalendarMetrics.FACTORY);
		CALENDAR_SCALES.put(JulianCalendarMetrics.CALENDAR_SCALE_ALIAS, JulianCalendarMetrics.FACTORY);
		CALENDAR_SCALES.put(JulianCalendarMetrics.CALENDAR_SCALE_NAME, JulianCalendarMetrics.FACTORY);
		CALENDAR_SCALES.put(IslamicCalendarMetrics.CALENDAR_SCALE_TLBA, new IslamicCalendarMetrics.IslamicCalendarMetricsFactory(
			IslamicCalendarMetrics.CALENDAR_SCALE_TLBA, LeapYearPattern.II, false));
		CALENDAR_SCALES.put(IslamicCalendarMetrics.CALENDAR_SCALE_CIVIL, new IslamicCalendarMetrics.IslamicCalendarMetricsFactory(
			IslamicCalendarMetrics.CALENDAR_SCALE_CIVIL, LeapYearPattern.II, true));
		CALENDAR_SCALES.put("ISLAMICC", CALENDAR_SCALES.get(IslamicCalendarMetrics.CALENDAR_SCALE_CIVIL));
	}
}
