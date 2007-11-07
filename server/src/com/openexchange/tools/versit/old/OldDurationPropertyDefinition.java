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

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Scanner;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.values.DurationValue;

public class OldDurationPropertyDefinition extends OldShortPropertyDefinition {

	public OldDurationPropertyDefinition(String[] paramNames,
			OldParamDefinition[] params) {
		super(paramNames, params);
	}

	protected Object parseValue(final Property property, final StringScanner ss)
			throws IOException {
		final DurationValue dur = new DurationValue();
		if (ss.peek != 'P') {
			throw new VersitException(ss, "Duration expected");
		}
		ss.read();
		if (ss.peek == 'T') {
			parseTime(ss, dur);
		} else {
			int num = ss.parseNumber();
			if (ss.peek == 'Y') {
				dur.Years = num;
				ss.read();
				num = ss.parseNumber();
			}
			if (ss.peek == 'M') {
				dur.Months = num;
				ss.read();
				num = ss.parseNumber();
			}
			if (ss.peek == 'W') {
				dur.Weeks = num;
				ss.read();
				num = ss.parseNumber();
			}
			if (ss.peek == 'D') {
				dur.Days = num;
				ss.read();
			}
			if (ss.peek == 'T') {
				parseTime(ss, dur);
			}
		}
		return dur;
	}

	void parseTime(final Scanner s, final DurationValue dur) throws IOException {
		s.read();
		int num = s.parseNumber();
		if (s.peek == 'H') {
			dur.Hours = num;
			s.read();
			num = s.parseNumber();
		}
		if (s.peek == 'M') {
			dur.Hours = num;
			s.read();
			num = s.parseNumber();
		}
		if (s.peek == 'S') {
			dur.Hours = num;
			s.read();
		}
	}

	protected String writeValue(final Property property, final Object value) {
		final DurationValue dur = (DurationValue) value;
		final StringBuilder sb = new StringBuilder();
		sb.append('P');
		if (dur.Years != 0) {
			sb.append(dur.Years);
			sb.append('Y');
		}
		if (dur.Months != 0) {
			sb.append(dur.Months);
			sb.append('M');
		}
		if (dur.Weeks != 0) {
			sb.append(dur.Weeks);
			sb.append('W');
		}
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
		return sb.toString();
	}

}
