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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.tools.versit.old;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.DateOrDateTimeValueDefinition;
import com.openexchange.tools.versit.values.DateTimeValue;
import com.openexchange.tools.versit.values.RecurrenceValue;

public class OldRecurrencePropertyDefinition extends OldPropertyDefinition {

	public OldRecurrencePropertyDefinition(String[] paramNames,
			OldParamDefinition[] params) {
		super(paramNames, params);
	}

	private static final String[] weekdays = { "SU", "MO", "TU", "WE", "TH",
			"FR", "SA" };

	private static final Pattern WeekNo = Pattern.compile("[1-5][+-]");

	private static final Pattern DayNo = Pattern
			.compile("([0-9]{1,2}[+-]?|LD)(?![0-9#])");

	private static final Pattern Num = Pattern.compile("[0-9]{1,3}(?![0-9#])");

	protected Object parseValue(final Property property, final OldScanner s, final byte[] value,
			final String charset) throws IOException {
		final StringScanner ss = new StringScanner(s, new String(value, charset)
				.trim().toUpperCase());
		final RecurrenceValue recur = new RecurrenceValue();
		switch (ss.peek) {
		case 'D':
			recur.Freq = RecurrenceValue.DAILY;
			ss.read();
			recur.Interval = ss.parseNumber();
			ss.skipWS();
			break;
		case 'W':
			recur.Freq = RecurrenceValue.WEEKLY;
			ss.read();
			recur.Interval = ss.parseNumber();
			ss.skipWS();
			while (ss.peek >= 'A' && ss.peek <= 'Z') {
				recur.ByDay.add(recur.new Weekday(0, parseWeekday(ss)));
				ss.skipWS();
			}
			break;
		case 'M':
			recur.Freq = RecurrenceValue.MONTHLY;
			ss.read();
			switch (ss.peek) {
			case 'P':
				ss.read();
				recur.Interval = ss.parseNumber();
				ss.skipWS();
				int weekNo = 0;
				String week = ss.regex(WeekNo);
				while (week != null || ss.peek >= 'A' && ss.peek <= 'Z') {
					if (week != null) {
						weekNo = (week.charAt(0) - '0')
								* (week.charAt(1) == '-' ? -1 : 1);
					} else {
						if (weekNo == 0) {
							throw new VersitException(ss, "Invalid recurrence");
						}
						recur.ByDay.add(recur.new Weekday(weekNo,
								parseWeekday(ss)));
					}
					ss.skipWS();
					week = ss.regex(WeekNo);
				}
				break;
			case 'D':
				ss.read();
				recur.Interval = ss.parseNumber();
				ss.skipWS();
				String day = ss.regex(DayNo);
				final ArrayList<Integer> days = new ArrayList<Integer>();
				while (day != null) {
					if ("LD".equals(day)) {
						day = "1-";
					}
					int sign = 1;
					switch (day.charAt(day.length() - 1)) {
					case '-':
						sign = -1; // no break
					case '+':
						day = day.substring(0, day.length() - 1);
					}
					days.add(Integer.valueOf(Integer.parseInt(day) * sign));
					ss.skipWS();
					day = ss.regex(DayNo);
				}
				recur.ByMonthDay = new int[days.size()];
				for (int i = 0; i < recur.ByMonthDay.length; i++) {
					recur.ByMonthDay[i] = days.get(i).intValue();
				}
				break;
			default:
				throw new VersitException(ss, "Invalid recurrence");
			}
			break;
		case 'Y':
			recur.Freq = RecurrenceValue.YEARLY;
			ss.read();
			boolean month;
			switch (ss.peek) {
			case 'M':
				month = true;
				break;
			case 'D':
				month = false;
				break;
			default:
				throw new VersitException(ss, "Invalid recurrence");
			}
			ss.read();
			recur.Interval = ss.parseNumber();
			ss.skipWS();
			String val = ss.regex(Num);
			final ArrayList<Integer> values = new ArrayList<Integer>();
			while (val != null) {
				values.add(Integer.valueOf(Integer.parseInt(val)));
				ss.skipWS();
				val = ss.regex(Num);
			}
			int[] vs = new int[values.size()];
			for (int i = 0; i < vs.length; i++) {
				vs[i] = values.get(i).intValue();
			}
			if (month) {
				recur.ByMonth = vs;
			} else {
				recur.ByYearDay = vs;
			}
			break;
		default:
			throw new VersitException(ss, "Invalid recurrence");
		}
		if (ss.peek == '#') {
			ss.read();
			recur.Count = ss.parseNumber();
			ss.skipWS();
		}
		if (ss.peek >= '0' && ss.peek <= '9') {
			recur.Until = (DateTimeValue) DateOrDateTimeValueDefinition.Default
					.createValue(ss, property);
		}
		if (recur.Count == -1 && recur.Until == null) {
			recur.Count = 2;
		}
		if (recur.Count == 0) {
			recur.Count = -1;
		}
		return recur;
	}

	private int parseWeekday(final StringScanner s) throws IOException {
		final String weekday = String.valueOf((char) s.read()) + (char)s.read();
		for (int i = 0; i < 7; i++) {
			if (weekday.equals(weekdays[i])) {
				return Calendar.SUNDAY + i;
			}
		}
		throw new VersitException(s, "Invalid recurrence: " + weekday);
	}

	protected String writeValue(final Property property) {
		final StringBuilder sb = new StringBuilder();
		final RecurrenceValue recur = (RecurrenceValue) property.getValue();
		switch (recur.Freq) {
		case RecurrenceValue.DAILY:
			sb.append('D');
			sb.append(recur.Interval);
			break;
		case RecurrenceValue.WEEKLY:
			sb.append('W');
			sb.append(recur.Interval);
			final int weeklySize = recur.ByDay.size();
			final Iterator weeklyIter = recur.ByDay.iterator();
			for (int k = 0; k < weeklySize; k++) {
				sb.append(' ');
				sb.append(weekdays[((RecurrenceValue.Weekday) weeklyIter.next()).day
						- Calendar.SUNDAY]);
			}
			break;
		case RecurrenceValue.MONTHLY:
			sb.append('M');
			if (recur.ByDay.size() > 0) {
				sb.append('P');
				sb.append(recur.Interval);
				int occurrence = 0;
				final int monthlySize = recur.ByDay.size();
				final Iterator monthlyIter = recur.ByDay.iterator();
				for (int k = 0; k < monthlySize; k++) {
					final RecurrenceValue.Weekday day = (RecurrenceValue.Weekday) monthlyIter
							.next();
					if (day.week != occurrence) {
						sb.append(' ');
						sb.append(Math.abs(day.week));
						sb.append(day.week < 0 ? '-' : '+');
						occurrence = day.week;
					}
					sb.append(' ');
					sb.append(weekdays[day.day]);
				}
			} else {
				sb.append('D');
				sb.append(recur.Interval);
				for (int i = 0; i < recur.ByMonthDay.length; i++) {
					sb.append(' ');
					sb.append(Math.abs(recur.ByMonthDay[i]));
					if (recur.ByMonthDay[i] < 0) {
						sb.append('-');
					}
				}
			}
			break;
		case RecurrenceValue.YEARLY:
			sb.append('Y');
			if (recur.ByMonth.length > 0) {
				sb.append('M');
				sb.append(recur.Interval);
				for (int i = 0; i < recur.ByMonth.length; i++) {
					sb.append(' ');
					sb.append(recur.ByMonth[i]);
				}
			} else {
				sb.append('D');
				sb.append(recur.Interval);
				for (int i = 0; i < recur.ByYearDay.length; i++) {
					sb.append(' ');
					sb.append(recur.ByYearDay[i]);
				}
			}
		}
		switch (recur.Count) {
		case -1:
			if (recur.Until == null) {
				sb.append(" #0");
			}
		case 2:
			break;
		default:
			sb.append(" #");
			sb.append(recur.Count);
		}
		if (recur.Until != null) {
			sb.append(' ');
			sb.append(DateOrDateTimeValueDefinition.Default
					.writeValue(recur.Until));
		}
		return sb.toString();
	}

}
