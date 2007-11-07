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



package com.openexchange.tools.versit.valuedefinitions.rfc2445;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.ValueDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.values.DateTimeValue;
import com.openexchange.tools.versit.values.RecurrenceValue;
import com.openexchange.tools.versit.values.RecurrenceValue.Weekday;

/**
 * @author Viktor Pracht
 */
public class RecurrenceValueDefinition extends ValueDefinition {

	public static final ValueDefinition Default = new RecurrenceValueDefinition();

	private static final String[] freqs = { "SECONDLY", "MINUTELY", "HOURLY",
			"DAILY", "WEEKLY", "MONTHLY", "YEARLY" };

	private static final String[] weekdays = { "SU", "MO", "TU", "WE", "TH",
			"FR", "SA" };

	public Object createValue(final StringScanner s, final Property property)
			throws IOException {
		final RecurrenceValue recur = new RecurrenceValue();
		if (!s.imatch("FREQ=")) {
			throw new VersitException(s, "Invalid recurrence");
		}
		final String freq = s.parseName().toUpperCase();
		GetFreq: {
			for (int i = 0; i < freqs.length; i++) {
				if (freq.equals(freqs[i])) {
					recur.Freq = i;
					break GetFreq;
				}
			}
			throw new VersitException(s, "Invalid recurrence");
		}
		while (s.peek == ';') {
			s.read();
			switch (s.peek) {
			case 'I':
				if (!s.imatch("INTERVAL=")) {
					throw new VersitException(s, "Invalid recurrence");
				}
				recur.Interval = s.parseNumber();
				break;
			case 'B':
				if (!s.imatch("BY")) {
					throw new VersitException(s, "Invalid recurrence");
				}
				switch (s.peek) {
				case 'S':
					if (!s.imatch("SE")) {
						throw new VersitException(s, "Invalid recurrence");
					}
					switch (s.peek) {
					case 'C':
						if (!s.imatch("COND=")) {
							throw new VersitException(s, "Invalid recurrence");
						}
						recur.BySecond = s.parseNumList();
						break;
					case 'T':
						if (!s.imatch("TPOS=")) {
							throw new VersitException(s, "Invalid recurrence");
						}
						recur.BySetPos = s.parseNumList();
						break;
					default:
						throw new VersitException(s, "Invalid recurrence");
					}
					break;
				case 'M':
					s.read();
					switch (s.peek) {
					case 'I':
						if (!s.imatch("INUTE=")) {
							throw new VersitException(s, "Invalid recurrence");
						}
						recur.ByMinute = s.parseNumList();
						break;
					case 'O':
						if (!s.imatch("ONTH")) {
							throw new VersitException(s, "Invalid recurrence");
						}
						switch (s.peek) {
						case 'D':
							if (!s.imatch("DAY=")) {
								throw new VersitException(s, "Invalid recurrence");
							}
							recur.ByMonthDay = s.parseNumList();
							break;
						case '=':
							s.read();
							recur.ByMonth = s.parseNumList();
							break;
						default:
							throw new VersitException(s, "Invalid recurrence");
						}
						break;
					default:
						throw new VersitException(s, "Invalid recurrence");
					}
					break;
				case 'H':
					if (!s.imatch("HOUR=")) {
						throw new VersitException(s, "Invalid recurrence");
					}
					recur.ByHour = s.parseNumList();
					break;
				case 'D':
					if (!s.imatch("DAY=")) {
						throw new VersitException(s, "Invalid recurrence");
					}
					while (true) {
						int week = 0;
						if (s.peek == '+' || s.peek == '-' || s.peek >= '0' && s.peek <= '9') {
							int sign = 1;
							if (s.peek == '+') {
								s.read();
							} else if (s.peek == '-') {
								sign = -1;
								s.read();
							}
							week = sign * s.parseNumber();
						}
						recur.ByDay.add(recur.new Weekday(week, parseWeekday(s)));
						if (s.peek != ',') {
							break;
						}
						s.read();
					}
					break;
				case 'Y':
					if (!s.imatch("YEARDAY=")) {
						throw new VersitException(s, "Invalid recurrence");
					}
					recur.ByYearDay = s.parseNumList();
					break;
				case 'W':
					if (!s.imatch("WEEKNO=")) {
						throw new VersitException(s, "Invalid recurrence");
					}
					recur.ByWeekNo = s.parseNumList();
					break;
				default:
					throw new VersitException(s, "Invalid recurrence");
				}
				break;
			case 'C':
				if (!s.imatch("COUNT=")) {
					throw new VersitException(s, "Invalid recurrence");
				}
				recur.Count = s.parseNumber();
				break;
			case 'U':
				if (!s.imatch("UNTIL=")) {
					throw new VersitException(s, "Invalid recurrence");
				}
				recur.Until = (DateTimeValue) DateOrDateTimeValueDefinition.Default.createValue(s, property);
				if (!recur.Until.isUTC) {
					throw new VersitException(s, "UTC time expected");
				}
				break;
			case 'W':
				if (!s.imatch("WKST=")) {
					throw new VersitException(s, "Invalid recurrence");
				}
				recur.WeekStart = parseWeekday(s);
				break;
			default:
				throw new VersitException(s, "Invalid recurrence");
			}
		}
		return recur;
	}

	private int parseWeekday(final StringScanner s) throws IOException {
		final String weekday = s.parseName();
		for (int i = 0; i < 7; i++) {
			if (weekday.equals(weekdays[i])) {
				return Calendar.SUNDAY + i;
			}
		}
		throw new VersitException(s, "Invalid recurrence");
	}

	public String writeValue(final Object value) {
		final RecurrenceValue recur = (RecurrenceValue) value;
		final StringBuilder sb = new StringBuilder();
		sb.append("FREQ=");
		sb.append(freqs[recur.Freq]);
		if (recur.Until != null) {
			sb.append(";UNTIL=");
			sb.append(DateOrDateTimeValueDefinition.Default
					.writeValue(recur.Until));
		} else if (recur.Count != -1) {
			sb.append(";COUNT=");
			sb.append(recur.Count);
		}
		if (recur.Interval != 1) {
			sb.append(";INTERVAL=");
			sb.append(recur.Interval);
		}
		appendList(sb, ";BYSECOND=", recur.BySecond);
		appendList(sb, ";BYMINUTE=", recur.ByMinute);
		appendList(sb, ";BYHOUR=", recur.ByHour);
		if (!recur.ByDay.isEmpty()) {
			sb.append(";BYDAY=");
			final int size = recur.ByDay.size();
			final Iterator i = recur.ByDay.iterator();
			if (size > 0) {
				appendWeekday(sb, i.next());
				for (int k = 1; k < size; k++) {
					sb.append(',');
					appendWeekday(sb, i.next());
				}
			}
		}
		appendList(sb, ";BYMONTHDAY=", recur.ByMonthDay);
		appendList(sb, ";BYYEARDAY=", recur.ByYearDay);
		appendList(sb, ";BYWEEKNO=", recur.ByWeekNo);
		appendList(sb, ";BYMONTH=", recur.ByMonth);
		appendList(sb, ";BYSETPOS=", recur.BySetPos);
		if (recur.WeekStart != Calendar.MONDAY) {
			sb.append(";WKST=");
			sb.append(weekdays[recur.WeekStart - Calendar.SUNDAY]);
		}
		return sb.toString();
	}

	private void appendList(final StringBuilder sb, final String header, final int[] list) {
		if (list.length == 0) {
			return;
		}
		sb.append(header);
		sb.append(list[0]);
		for (int i = 1; i < list.length; i++) {
			sb.append(',');
			sb.append(list[i]);
		}
	}

	private void appendWeekday(final StringBuilder sb, final Object weekday) {
		final Weekday wd = (Weekday) weekday;
		if (wd.week != 0) {
			sb.append(wd.week);
		}
		sb.append(weekdays[wd.day - Calendar.SUNDAY]);
	}

}
