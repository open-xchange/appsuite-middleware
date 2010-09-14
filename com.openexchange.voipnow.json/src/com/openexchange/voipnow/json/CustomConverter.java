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

package com.openexchange.voipnow.json;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.axis2.databinding.utils.ConverterUtil;

public final class CustomConverter {

	private static ThreadLocal<Boolean> enabled = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/*
	 * Sneak CustomConverter into
	 * org.apache.axis2.databinding.utils.ConverterUtil
	 */
	static {
		try {
			final Field customClass = ConverterUtil.class
					.getDeclaredField("customClass");
			customClass.setAccessible(true);
			customClass.set(null, CustomConverter.class);
			final Field isCustomClassPresent = ConverterUtil.class
					.getDeclaredField("isCustomClassPresent");
			isCustomClassPresent.setAccessible(true);
			isCustomClassPresent.set(null, true);
		} catch (final Exception e) {
		}
	}

	public static String convertToString(final Calendar value) {
		// lexical form of the calendar is '-'? yyyy '-' mm '-' dd 'T' hh
		// ':' mm ':' ss ('.' s+)? (zzzzzz)?
		if (value.get(Calendar.ZONE_OFFSET) == -1) {
			value.setTimeZone(TimeZone.getDefault());
		}
		final StringBuffer dateString = new StringBuffer(28);
		ConverterUtil.appendDate(dateString, value);
		dateString.append("T");
		// adding hours
		ConverterUtil.appendTime(value, dateString);
		ConverterUtil.appendTimeZone(value, dateString);
		return dateString.toString();
	}

	public static String convertToString(final Date value) {
		if (enabled.get()) {
			final StringBuffer s = new StringBuffer(11);
			final Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTime(value);
			ConverterUtil.appendDate(s, calendar);
			return s.toString();
		} else {
			// lexical form of the date is '-'? yyyy '-' mm '-' dd zzzzzz?
			final Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTime(value);
			if (!calendar.isSet(Calendar.ZONE_OFFSET)) {
				calendar.setTimeZone(TimeZone.getDefault());
			}
			final StringBuffer dateString = new StringBuffer(16);
			ConverterUtil.appendDate(dateString, calendar);
			ConverterUtil.appendTimeZone(calendar, dateString);
			return dateString.toString();
		}
	}

	public static void setEnabled(final boolean enabled) {
		CustomConverter.enabled.set(enabled);
	}

}
