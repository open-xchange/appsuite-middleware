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

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.ValueDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.values.DurationValue;


/**
 * @author Viktor Pracht
 */
public class DurationValueDefinition extends ValueDefinition {

	public static final ValueDefinition Default = new DurationValueDefinition();

	/**
	 * Transformed LL(1) grammar:
	 * 
	 * <pre>
	 *     
	 *     	 value  = ([&quot;+&quot;] / &quot;-&quot;) &quot;P&quot; (time / 1*DIGIT (date / week))
	 *     	 date   = day [time]
	 *     	 day    = &quot;D&quot;
	 *     	 week   = &quot;W&quot;
	 *     	 time   = &quot;T&quot; 1*DIGIT (hour / minute / second)
	 *     	 hour   = &quot;H&quot; [1*DIGIT minute]
	 *     	 minute = &quot;M&quot; [1*DIGIT second]
	 *     	 second = &quot;S&quot;
	 *     	 
	 * </pre>
	 */
	public Object createValue(final StringScanner s, final Property property)
			throws IOException {
		final DurationValue dur = new DurationValue();
		if (s.peek == '+') {
			s.read();
		} else if (s.peek == '-') {
			dur.Negative = true;
			s.read();
		}
		if (s.peek != 'P') {
			throw new VersitException(s, "Duration expected");
		}
		s.read();
		if (s.peek == 'T') {
			parseTime(s, dur);
		} else {
			final int num = s.parseNumber();
			if (s.peek == 'D') {
				dur.Days = num;
				if (s.peek == 'T') {
					parseTime(s, dur);
				}
			} else if (s.peek == 'W') {
				dur.Weeks = num;
			} else {
				throw new VersitException(s, "Duration expected");
			}
		}
		return dur;
	}

	private void parseTime(final StringScanner s, final DurationValue dur)
			throws IOException {
		s.read();
		final int num = s.parseNumber();
		switch (s.peek) {
		case 'H':
			parseHour(s, num, dur);
			break;
		case 'M':
			parseMinute(s, num, dur);
			break;
		case 'S':
			parseSecond(s, num, dur);
			break;
		default:
			throw new VersitException(s, "Duration expected");
		}
	}

	private void parseHour(final StringScanner s, final int num, final DurationValue dur)
			throws IOException {
		if (s.peek != 'H') {
			throw new VersitException(s, "Duration expected");
		}
		s.read();
		dur.Hours = num;
		if (s.peek >= '0' && s.peek <= '9') {
			parseMinute(s, s.parseNumber(), dur);
		}
	}

	private void parseMinute(final StringScanner s, final int num, final DurationValue dur)
			throws IOException {
		if (s.peek != 'M') {
			throw new VersitException(s, "Duration expected");
		}
		s.read();
		dur.Minutes = num;
		if (s.peek >= '0' && s.peek <= '9') {
			parseSecond(s, s.parseNumber(), dur);
		}
	}

	private void parseSecond(final StringScanner s, final int num, final DurationValue dur)
			throws IOException {
		if (s.peek != 'S') {
			throw new VersitException(s, "Duration expected");
		}
		s.read();
		dur.Seconds = num;
	}

	public String writeValue(final Object value) {
		final DurationValue dur = (DurationValue) value;
		final StringBuilder sb = new StringBuilder();
		sb.append(dur.Negative ? "-P" : "P");
		if (dur.Weeks != 0) {
			sb.append(dur.Weeks);
			sb.append('W');
		} else {
			if (dur.Days != 0) {
				sb.append(dur.Days);
				sb.append('D');
			}
			if (dur.Hours != 0 || dur.Minutes != 0 || dur.Seconds != 0) {
				sb.append('T');
				if (dur.Hours != 0) {
					sb.append(dur.Hours);
					sb.append('H');
					if (dur.Minutes != 0 || dur.Seconds != 0) {
						sb.append(dur.Minutes);
						sb.append('M');
						if (dur.Seconds != 0) {
							sb.append(dur.Seconds);
							sb.append('S');
						}
					}
				} else {
					if (dur.Minutes != 0) {
						sb.append(dur.Minutes);
						sb.append('M');
					}
					if (dur.Seconds != 0) {
						sb.append(dur.Seconds);
						sb.append('S');
					}
				}
			}
		}
		return sb.toString();
	}

}
