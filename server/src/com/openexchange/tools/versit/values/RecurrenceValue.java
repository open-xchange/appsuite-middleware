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



package com.openexchange.tools.versit.values;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Viktor Pracht
 */
public class RecurrenceValue {

	public static final int SECONDLY = 0;

	public static final int MINUTELY = 1;

	public static final int HOURLY = 2;

	public static final int DAILY = 3;

	public static final int WEEKLY = 4;

	public static final int MONTHLY = 5;

	public static final int YEARLY = 6;

	// One of the constants above.
	public int Freq;

	public int Interval = 1;

	/**
	 * Months (1 to 12) on which the event occurs.
	 */
	public int[] ByMonth = {};

	/**
	 * Week numbers on which the event occurs. Valid values are 1 to 53 when
	 * counting from the start and -1 to -53 when counting from the end.
	 */
	public int[] ByWeekNo = {};

	/**
	 * Days of the year on which the event occurs. Valid values are 1 to 366
	 * when counting from the start and -1 to -366 when counting from the end.
	 */
	public int[] ByYearDay = {};

	/**
	 * Days of the month on which the event occurs. Valid values are 1 to 31
	 * when counting from the start and -1 to -31 when counting from the end.
	 */
	public int[] ByMonthDay = {};

	/**
	 * Weekdays (Recurrence.Weekday instances) on which the event occurs.
	 */
	public ArrayList<Weekday> ByDay = new ArrayList<Weekday>();

	/**
	 * Hours (0 to 23) on which the event occurs.
	 */
	public int[] ByHour = {};

	/**
	 * Minutes (0 to 59) on which the event occurs.
	 */
	public int[] ByMinute = {};

	/**
	 * Seconds (0 to 59) on which the event occurs.
	 */
	public int[] BySecond = {};

	public int[] BySetPos = {};

	public int Count = -1;

	public DateTimeValue Until = null;

	/**
	 * Start of the week (Calendar.MONDAY or Calendar.SUNDAY)
	 */
	public int WeekStart = Calendar.MONDAY;

	/**
	 * Used in RecurrenceValue.ByDay
	 * 
	 * @author Viktor Pracht
	 */
	public class Weekday {

		/**
		 * Number of the week inside the main frequency unit. 0 indicates every
		 * week, positive values indicate week numbers counted from the start,
		 * negative values indicate week numbers counted from the end (-1 is
		 * last).
		 */
		public int week;

		/**
		 * Day of the week (Calendar.SUNDAY to Calendar.SATURDAY)
		 */
		public int day;

		public Weekday(int week, int day) {
			this.week = week;
			this.day = day;
		}

	}

}
