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

package org.dmfs.rfc5545.calendarmetrics;

import java.util.TimeZone;

import org.dmfs.rfc5545.Instance;
import org.dmfs.rfc5545.Weekday;


/**
 * Provides a set of methods that return all kinds of information about a calendar and do some calendar calculations.
 * <p>
 * Please note that most methods use a <em>packed month</em> instead of just a month number. That's due to the need to specify leap months in certain
 * calendaring systems. Do <em>not</em> make any assumptions about the format of the packed month since this is an internal implementation detail. To get the
 * actual month number and the leap month flag use {@link #monthNum(int)} and {@link #isLeapMonth(int)}.
 * </p>
 * <p>
 * However, packed months are integers that are guaranteed to be comparable with respect to their natural order. So if <code>a</code> and <code>b</code> are two
 * packed months, it's guaranteed that <code>a&lt;b</code> if <code>a</code> comes before <code>b</code> in the year.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class CalendarMetrics
{

	public static abstract class CalendarMetricsFactory
	{
		public abstract CalendarMetrics getCalendarMetrics(Weekday weekStart);
	}

	/**
	 * The first day of the week.
	 */
	public final Weekday weekStart;

	/**
	 * The first day of the week int.
	 */
	public final int weekStartInt;

	/**
	 * The minimal number of days in the first week.
	 */
	public final int minDaysInFirstWeek;


	public CalendarMetrics(Weekday weekStart, int minDaysInFirstWeek)
	{
		this.weekStart = weekStart;
		this.weekStartInt = weekStart.ordinal();
		this.minDaysInFirstWeek = minDaysInFirstWeek;
	}


	/**
	 * Returns a packed month from a string representation. The packed months are arithmetically comparable with respect to their natural order.
	 * <p>
	 * The string representation is the 1-based month number followed by an optional <code>L</code> literal to indicate a leap month.
	 * </p>
	 * 
	 * @param month
	 *            The string representation of the month.
	 * @return A packed representation of the month.
	 * @throws IllegalArgumentException
	 *             if the given (leap-) month does not exist in this calendar scale or the month string is otherwise malformed.
	 */
	public int packedMonth(String month)
	{
		if (month == null)
		{
			throw new IllegalArgumentException("month strings must not be null");
		}

		final int len = month.length();
		if (len == 0 || len > 3)
		{
			throw new IllegalArgumentException("illegal month string " + month);
		}
		final char lastChar = month.charAt(len - 1);
		int leapBit = lastChar == 'L' || lastChar == 'l' ? 1 : 0;

		try
		{
			return (Integer.parseInt(month.substring(0, len - leapBit)) - 1) << 1 + leapBit;
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("illegal month string " + month, e);
		}
	}


	/**
	 * Convert the packed month to a numerical string representation.
	 * 
	 * @param packedMonth
	 *            The packed month.
	 * 
	 * @return The converted month.
	 */
	public String packedMonthToString(int packedMonth)
	{
		if ((packedMonth & 1) == 1)
		{
			return String.valueOf(packedMonth >>> 1) + "L";
		}
		else
		{
			return String.valueOf(packedMonth >>> 1);
		}
	}


	/**
	 * Returns a packed month, which is a combination of the month number and a leap month flag. The packed months are arithmetically comparable with respect to
	 * their natural order.
	 * <p>
	 * Note that not all calendar scales support leap months.
	 * </p>
	 * 
	 * @param monthNum
	 *            The 0-based month number.
	 * @param leapMonth
	 *            <code>true</code> if this is a leap month, <code>false</code> otherwise.
	 * @return A packed representation of the month.
	 * @throws IllegalArgumentException
	 *             if the given (leap-) month does not exist in this calendar scale.
	 */
	public int packedMonth(int monthNum, boolean leapMonth)
	{
		return leapMonth ? monthNum << 1 + 1 : monthNum << 1;
	}


	/**
	 * Returns whether a certain packed month is a leap month, i.e. a month that exists in certain years only.
	 * 
	 * @param packedMonth
	 *            The packed month to test.
	 * @return <code>true</code> if the month is a leap month, <code>false</code> otherwise.
	 */
	public boolean isLeapMonth(int packedMonth)
	{
		return (packedMonth & 1) == 1;
	}


	/**
	 * Returns the month number of a packed month.
	 * 
	 * @param packedMonth
	 *            The packed month.
	 * @return the month number that is encoded in the packed month.
	 */
	public int monthNum(int packedMonth)
	{
		return packedMonth >>> 1;
	}


	/**
	 * Returns a packed value that contains a packed month and a month day.
	 * 
	 * @param packedMonth
	 *            The packed month.
	 * @param day
	 *            The day of month.
	 * @return A packed value, containing the packed month and the day.
	 * 
	 * @see #packedMonth(int)
	 * @see #dayOfMonth(int)
	 */
	public static int monthAndDay(int packedMonth, int day)
	{
		return (packedMonth << 8) + day;
	}


	/**
	 * Get the packed month from a compound MonthAndDay value like {@link #getMonthAndDayOfYearDay(int, int)} and {@link #monthAndDay(int, int)} return it.
	 * 
	 * @param monthAndDay
	 *            An integer that contains a packed month and a day.
	 * @return The packed month.
	 */
	public static int packedMonth(int monthAndDay)
	{
		return monthAndDay >> 8;
	}


	/**
	 * Get the day of month from a compound MonthAndDay value like {@link #getMonthAndDayOfYearDay(int, int)} and {@link #monthAndDay(int, int)} return it.
	 * 
	 * @param monthAndDay
	 *            An integer that contains a packed month and a day.
	 * @return The day of month.
	 */
	public static int dayOfMonth(int monthAndDay)
	{
		return monthAndDay & 0xff;
	}


	/**
	 * Returns the largest month day number that the current calendar supports.
	 * 
	 * @return The maximum month number in this calendar scale.
	 */
	public abstract int getMaxMonthDayNum();


	/**
	 * Returns the largest year day number that the current calendar supports.
	 * 
	 * @return The maximum year day number in this calendar scale.
	 */
	public abstract int getMaxYearDayNum();


	/**
	 * Returns the largest week number that the current calendar supports.
	 * 
	 * @return The maximum ISO week number in this calendar scale.
	 */
	public abstract int getMaxWeekNoNum();


	/**
	 * Get the number of days in a specific packed month.
	 * 
	 * @param year
	 *            The year.
	 * @param packedMonth
	 *            The packed month.
	 * @return The number of days in that month.
	 */
	public abstract int getDaysPerPackedMonth(int year, int packedMonth);


	/**
	 * Determines the month of a given day of year.
	 * 
	 * @param year
	 *            The year.
	 * @param yearDay
	 *            The year day.
	 * @return the packed month.
	 */
	public abstract int getPackedMonthOfYearDay(int year, int yearDay);


	/**
	 * Determines the day of month for a given day of year.
	 * 
	 * @param year
	 *            The year.
	 * @param yearDay
	 *            The year day.
	 * @return the month day.
	 */
	public abstract int getDayOfMonthOfYearDay(int year, int yearDay);


	/**
	 * Determines the month and day for a given day of year. The result contains both, packed month and day in a single integer. To split the values use
	 * {@link #packedMonth(int)} and {@link #dayOfMonth(int)} like in:
	 * 
	 * <pre>
	 * int monthAndDay = mCalendarMetrics.getMonthAndDayOfYearDay(year, yearDay);
	 * int month = CalendarMetrics.packedMonth(monthAndDay);
	 * int dayOfMonth = CalendarMetrics.dayOfMonth(monthAndDay);
	 * </pre>
	 * 
	 * @param year
	 *            The year.
	 * @param yearDay
	 *            The day of the year. Must be a valid value between 1 and the maximum number of days in that year.
	 * @return an integer with the day of month in the lowest significant byte and the packed month in the second lowest significant byte.
	 */
	public abstract int getMonthAndDayOfYearDay(int year, int yearDay);


	/**
	 * Get the number of days preceding the given month in the given year.
	 * 
	 * @param year
	 *            The year.
	 * @param packedMonth
	 *            The packed month.
	 * @return The days in the year preceding the month.
	 */
	public abstract int getYearDaysForPackedMonth(int year, int packedMonth);


	/**
	 * Get the number of months in the given year.
	 * 
	 * @param year
	 *            The year.
	 * @return The number of months in that year.
	 */
	public abstract int getMonthsPerYear(int year);


	/**
	 * Get the number of days in the given year.
	 * 
	 * @param year
	 *            The year.
	 * @return The number of days in that year.
	 */
	public abstract int getDaysPerYear(int year);


	/**
	 * Get the number of ISO weeks in the given year.
	 * 
	 * @param year
	 *            The year.
	 * @return The number of weeks in that year.
	 */
	public abstract int getWeeksPerYear(int year);


	/**
	 * Get the ISO week number of the given date.
	 * <p>
	 * Note: this will return <code>0</code> if the week belongs to the previous ISO year and a value greater than {@link #getWeeksPerYear(int)} if the week
	 * belongs to the next ISO year.
	 * </p>
	 * 
	 * @param year
	 *            The year.
	 * @param packedMonth
	 *            The packed month.
	 * @param dayOfMonth
	 *            The day of month.
	 * @return The ISO week number.
	 */
	public int getWeekOfYear(int year, int packedMonth, int dayOfMonth)
	{
		return getWeekOfYear(year, getDayOfYear(year, packedMonth, dayOfMonth));
	}


	/**
	 * Get the ISO week number of the given date.
	 * <p>
	 * Note: this will return <code>0</code> if the week belongs to the previous ISO year and a value greater than {@link #getWeeksPerYear(int)} if the week
	 * belongs to the next ISO year.
	 * </p>
	 * 
	 * @param year
	 *            The year.
	 * @param yearDay
	 *            The day of year.
	 * @return The ISO week number.
	 */
	public abstract int getWeekOfYear(int year, int yearDay);


	/**
	 * Returns the day of week of the given year date.
	 * 
	 * @param year
	 *            The year.
	 * @param yearDay
	 *            The day of the year.
	 * @return An integer for the day of the week where <code>0</code> means Sunday and <code>6</code> means Saturday or the respective week days in the
	 *         respective calendar scale.
	 */
	public int getDayOfWeek(int year, int yearDay)
	{
		return (getWeekDayOfFirstYearDay(year) + yearDay - 1) % 7;
	}


	/**
	 * Returns the day of week of the given date.
	 * 
	 * @param year
	 *            The year.
	 * @param packedMonth
	 *            The packed month number.
	 * @param dayOfMonth
	 *            The day of the month.
	 * @return An int for the day of the week where <code>0</code> means Sunday and <code>6</code> means Saturday or the respective week days in the respective
	 *         calendar scale.
	 */
	public int getDayOfWeek(int year, int packedMonth, int dayOfMonth)
	{
		return getDayOfWeek(year, getDayOfYear(year, packedMonth, dayOfMonth));
	}


	/**
	 * Get the day of the year for the specified date.
	 * 
	 * @param year
	 *            The year.
	 * @param packedMonth
	 *            The packed month.
	 * @param dayOfMonth
	 *            The day of month.
	 * @return The day of year.
	 */
	public abstract int getDayOfYear(int year, int packedMonth, int dayOfMonth);


	/**
	 * Get the day of the year for the specified ISO week date, see <a href="http://en.wikipedia.org/wiki/ISO_week_date">ISO week date</a>
	 * <p>
	 * If the day belongs to the previous year zero or a negative value is returned. If the day belongs to the next year the result is larger than
	 * {@link #getDaysPerYear(int)} for <code>year</code>.
	 * </p>
	 * 
	 * @param year
	 *            The year.
	 * @param weekOfYear
	 *            The ISO week of the year.
	 * @param dayOfWeek
	 *            The day of the week.
	 * @return The day of year.
	 */
	public abstract int getYearDayOfIsoYear(int year, int weekOfYear, int dayOfWeek);


	/**
	 * Get the weekday of the first day (which is January the 1st in a Gregorian Calendar) in the given year.
	 * 
	 * @param year
	 *            The year.
	 * @return The weekday number.
	 */
	public abstract int getWeekDayOfFirstYearDay(int year);


	/**
	 * Get the day of year of the start of the first week in a year. Note this method returns values below 1 if the start of the week is in the previous year.
	 * The result depends on the values of {@link #weekStart} and {@link #minDaysInFirstWeek}.
	 * 
	 * @param year
	 *            The year.
	 * @return The day of year.
	 */
	public abstract int getYearDayOfFirstWeekStart(int year);


	/**
	 * Get the day of year of the start of the given week in a year. Note this method returns values below 1 if the start of the week is in the previous year.
	 * The result depends on the values of {@link #weekStart} and {@link #minDaysInFirstWeek}.
	 * 
	 * @param year
	 *            The year.
	 * @param week
	 *            The week.
	 * @return The day of year.
	 */
	public abstract int getYearDayOfWeekStart(int year, int week);


	/**
	 * Convert an instance to milliseconds since the epoch (i.e. since 1970-01-01 0:00:00 UTC).
	 * 
	 * @param instance
	 *            The instance to convert.
	 * @param timeZone
	 *            The time zone or <code>null</code> for all day and floating instances.
	 * @return The time in milliseconds since the epoch of this instance.
	 */
	public long toMillis(long instance, TimeZone timeZone)
	{
		return toMillis(timeZone == null || "UTC".equals(timeZone.getID()) ? null : timeZone, Instance.year(instance), Instance.month(instance),
			Instance.dayOfMonth(instance), Instance.hour(instance), Instance.minute(instance), Instance.second(instance), 0);
	}


	/**
	 * Convert the given (local) date to milliseconds since the epoch using the given {@link TimeZone}.
	 * 
	 * @param timeZone
	 *            The time zone or <code>null</code> for floating dates (and all-day dates).
	 * @param year
	 *            The year.
	 * @param packedMonth
	 *            The packed month.
	 * @param dayOfMonth
	 *            The day of the month.
	 * @param hours
	 *            The hour of the day.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 * @param millis
	 *            The milliseconds.
	 * @return The milliseconds since the epoch.
	 */
	public abstract long toMillis(TimeZone timeZone, int year, int packedMonth, int dayOfMonth, int hours, int minutes, int seconds, int millis);


	/**
	 * Converts a timestamp to an instance in the given {@link TimeZone}.
	 * 
	 * @param timestamp
	 *            The time in milliseconds since the epoch.
	 * @param timeZone
	 *            The time zone, may be <code>null</code> in which case UTC will be used.
	 * @return a packed instance.
	 */
	public abstract long toInstance(long timestamp, TimeZone timeZone);


	/**
	 * Validates the given instance.
	 * <p>
	 * At present this method doesn't check for leap seconds and it doesn't honor daylight saving switches. So it validates times between 2:00h and 3:00 to
	 * <code>true</code> even if the local time can't have these values due to daylight savings.
	 * </p>
	 * TODO: add check for daylight saving changes.
	 * 
	 * @param instance
	 *            The instance to validate.
	 * @return <code>true</code> if the date is valid, <code>false</code> otherwise.
	 */
	public boolean validate(long instance)
	{
		int year = Instance.year(instance);
		int month = Instance.month(instance);

		if (month < 0 || month >= getMonthsPerYear(year))
		{
			return false;
		}

		int day = Instance.dayOfMonth(instance);
		if (day < 1 || day > getDaysPerPackedMonth(year, month))
		{
			return false;
		}

		int hour = Instance.hour(instance);
		if (hour < 0 || hour > 23)
		{
			return false;
		}

		int minute = Instance.minute(instance);
		if (minute < 0 || minute > 59)
		{
			return false;
		}

		int second = Instance.second(instance);
		if (second < 0 || second > 59) // we don't support leap seconds yet
		{
			return false;
		}
		return true;
	}


	/*
	 * Methods to manipulate instances.
	 */

	/**
	 * Moves the given instance forward by one month. In many cases this is faster than {@link #nextMonth(long, int)} with <code>n = 1</code>.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @return A new instance.
	 */
	public abstract long nextMonth(long instance);


	/**
	 * Moves the given instance forward by one or more months.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @param n
	 *            The number of months to add.
	 * @return A new instance.
	 */
	public abstract long nextMonth(long instance, int n);


	/**
	 * Moves the given instance backward by one month. In many cases this is faster than {@link #prevMonth(long, int)} with <code>n = 1</code>.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @return A new instance.
	 */
	public abstract long prevMonth(long instance);


	/**
	 * Moves the given instance backward by one or more months.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @param n
	 *            The number of months to go back.
	 * @return A new instance.
	 */
	public abstract long prevMonth(long instance, int n);


	/**
	 * Moves the given instance forward to the next (existing day).
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @return A new instance.
	 */
	public abstract long nextDay(long instance);


	/**
	 * Moves the given instance forward by one or more days.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @param n
	 *            The number of days to add.
	 * @return A new instance.
	 */
	public abstract long nextDay(long instance, int n);


	/**
	 * Moves the given instance backward to the previous (existing) day.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @return A new instance.
	 */
	public abstract long prevDay(long instance);


	/**
	 * Moves the given instance backward by one or more days.
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @param n
	 *            The number of days to go back.
	 * @return A new instance.
	 */
	public abstract long prevDay(long instance, int n);


	/**
	 * Get the start date of the week the given instance is in.
	 * 
	 * @param instance
	 *            The instance.
	 * @return The instance of the start of the week.
	 */
	public long startOfWeek(long instance)
	{
		int currentDayOfWeek = getDayOfWeek(Instance.year(instance), Instance.month(instance), Instance.dayOfMonth(instance));

		int offset = (weekStartInt - currentDayOfWeek - 7) % 7;

		if (offset == 0)
		{
			return instance;
		}
		if (offset == -1)
		{
			return prevDay(instance);
		}
		else
		{
			return prevDay(instance, -offset);
		}
	}


	/**
	 * Changes the day of week of the given instance ot the given value, but keeping the week number constant
	 * 
	 * @param instance
	 *            The instance to modify.
	 * @param dayOfWeek
	 *            The new day of the week.
	 * @return A new instance.
	 */
	public long setDayOfWeek(long instance, int dayOfWeek)
	{
		int currentDayOfWeek = getDayOfWeek(Instance.year(instance), Instance.month(instance), Instance.dayOfMonth(instance));

		int offset = (weekStartInt - currentDayOfWeek - 7) % 7 + (dayOfWeek - weekStartInt + 7) % 7;

		switch (offset)
		{
			case -6:
			case -5:
			case -4:
			case -3:
			case -2:
				return prevDay(instance, -offset);
			case -1:
				return prevDay(instance);
			case 0:
				return instance;
			case 1:
				return nextDay(instance);
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
				return nextDay(instance, offset);
		}
		/*
		 * We will never get here, this is just to make the compiler happy.
		 */
		return instance;
	}


	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}


	/**
	 * By default two {@link CalendarMetrics} equal when they are of the same class and the week definition equals. Subclasses may override this to change this
	 * behavior, i.e. Islamic calendars need to match the leap year rule as well.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof CalendarMetrics))
		{
			return false;
		}

		// two CalendarMetrics equal when their classes and the week definition are the same
		return getClass() == obj.getClass() && minDaysInFirstWeek == ((CalendarMetrics) obj).minDaysInFirstWeek
			&& weekStart == ((CalendarMetrics) obj).weekStart;
	}


	/**
	 * Returns whether two {@link CalendarMetrics} implement the same calendar scale.
	 * 
	 * By default two {@link CalendarMetrics} use the same calendar scale when they are of the same class. Subclasses may override this to change this behavior,
	 * i.e. Islamic calendars need to match the leap year rule as well.
	 */
	public boolean scaleEquals(CalendarMetrics obj)
	{
		// two CalendarMetrics are of the same Scale classes are the same
		return getClass() == obj.getClass();
	}
}
