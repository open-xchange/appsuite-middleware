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

import java.io.IOException;
import java.io.Writer;
import java.util.TimeZone;

import org.dmfs.rfc5545.calendarmetrics.CalendarMetrics;
import org.dmfs.rfc5545.calendarmetrics.GregorianCalendarMetrics;


/**
 * Represents a DATE-TIME or DATE value as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. This class
 * stores all aspects of a DATE or DATETIME value.
 * <p>
 * Objects of this class are immutable.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class DateTime
{
	/**
	 * The default calendar scale. By default all {@link DateTime} and Date values use the Gregorian calendar scale if not specified otherwise.
	 */
	public final static CalendarMetrics GREGORIAN_CALENDAR_SCALE = new GregorianCalendarMetrics(Weekday.MO, 4);

	/**
	 * Static instance of the time zone UTC.
	 */
	public final static TimeZone UTC = TimeZone.getTimeZone("UTC");

	/**
	 * The {@link CalendarMetrics} of this {@link DateTime} object.
	 */
	private final CalendarMetrics mCalendarMetrics;

	/**
	 * The {@link TimeZone} of this {@link DateTime} object. May be <code>null</code> if the event is floating.
	 */
	private final TimeZone mTimezone;

	/**
	 * The milliseconds since the epoch of this {@link DateTime}. This will be {@link Long#MAX_VALUE} if it has not been calculated yet.
	 */
	private long mTimestamp = Long.MAX_VALUE;

	/**
	 * The packed instance of this {@link DateTime}. This will be {@link Long#MAX_VALUE} if it has not been calculated yet.
	 * 
	 * @see Instance.
	 */
	private long mInstance = Long.MAX_VALUE;

	/**
	 * The week of the year or <code>-1</code> if it hasn't been calculated yet.
	 */
	private int mWeekOfYear = -1;

	/**
	 * The day of the week or <code>-1</code> if it hasn't been calculated yet.
	 */
	private int mDayOfWeek = -1;

	/**
	 * All-day flag.
	 */
	private final boolean mAllday;


	/**
	 * Clone constructor changing the {@link CalendarMetrics}. It will represent the same absolute time, but instances will be in another calendar scale.
	 * All-day and floating instances will still be all-day respective floating.
	 * 
	 * @param calendarMetrics
	 *            The new calendar scale.
	 * @param dateTime
	 *            The {@link DateTime} representing the absolute time.
	 */
	public DateTime(CalendarMetrics calendarMetrics, DateTime dateTime)
	{
		mCalendarMetrics = calendarMetrics;
		mTimestamp = dateTime.getTimestamp();
		mTimezone = dateTime.mTimezone;
		// we can just copy the allday flag, because the new DateTime will still be aligned to midnight of that day, regardless of the calendar metrics
		mAllday = dateTime.mAllday;
	}


	/**
	 * Clone constructor that changes the {@link CalendarMetrics} and the {@link TimeZone}. It will represent the same absolute time, but instances will be in
	 * another calendar scale and time zone. You can use this to convert between calendar scales.
	 * <p>
	 * If the given {@link DateTime} is all-day the time zone will be ignored. If the given {@link DateTime} is floating it will be converted to an absolute
	 * time
	 * </p>
	 * 
	 * @param calendarMetrics
	 *            The calendar scale to use.
	 * @param timezone
	 *            The new {@link TimeZone}.
	 * @param dateTime
	 *            The {@link DateTime} to clone from.
	 */
	public DateTime(CalendarMetrics calendarMetrics, TimeZone timezone, DateTime dateTime)
	{
		mCalendarMetrics = calendarMetrics;
		mTimestamp = dateTime.getTimestamp();
		if (dateTime.mAllday)
		{
			mTimezone = null;
			if (calendarMetrics.scaleEquals(dateTime.mCalendarMetrics))
			{
				mInstance = dateTime.mInstance;
			}
		}
		else
		{
			mTimezone = timezone;
		}
		// we can just copy the allday flag, because the new DateTime will still be aligned to midnight of that day, regardless of the calendar metrics
		mAllday = dateTime.mAllday;
	}


	/**
	 * Create a new {@link DateTime} from the given time stamp using {@link #GREGORIAN_CALENDAR_SCALE} and {@link #UTC} time zone.
	 * 
	 * @param timestamp
	 *            The time in milliseconds since the epoch of this date-time value.
	 */
	public DateTime(long timestamp)
	{
		this(GREGORIAN_CALENDAR_SCALE, UTC, timestamp);
	}


	/**
	 * Create a new {@link DateTime} from the given time stamp using {@link #GREGORIAN_CALENDAR_SCALE} and the given time zone.
	 * 
	 * @param timezone
	 *            The {@link TimeZone} of the new instance.
	 * @param timestamp
	 *            The time in milliseconds since the epoch of this date-time value.
	 */
	public DateTime(TimeZone timezone, long timestamp)
	{
		this(GREGORIAN_CALENDAR_SCALE, timezone, timestamp);
	}


	/**
	 * Create a new {@link DateTime} from the given time stamp using the given {@link CalendarMetrics} and the given time zone.
	 * 
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} of the DateTime.
	 * @param timezone
	 *            The {@link TimeZone} of the new instance.
	 * @param timestamp
	 *            The time in milliseconds since the epoch of this date-time value.
	 */
	public DateTime(CalendarMetrics calendarMetrics, TimeZone timezone, long timestamp)
	{
		mCalendarMetrics = calendarMetrics;
		mTimestamp = timestamp;
		mTimezone = timezone;
		mAllday = false;
	}


	/**
	 * Creates a new {@link DateTime} for the given all-day date using the {@link #GREGORIAN_CALENDAR_SCALE}. As a result of this the all-day flag will be set
	 * to <code>true</code>.
	 * 
	 * @param year
	 *            The year of the event.
	 * @param month
	 *            The month of the event.
	 * @param dayOfMonth
	 *            The monthday of the event.
	 */
	public DateTime(int year, int month, int dayOfMonth)
	{
		mCalendarMetrics = GREGORIAN_CALENDAR_SCALE;
		mInstance = Instance.make(year, month, dayOfMonth, 0, 0, 0);
		mTimezone = null;
		mAllday = true;
	}


	/**
	 * Create a new floating DateTime using {@link #GREGORIAN_CALENDAR_SCALE}.
	 * 
	 * @param year
	 *            The year.
	 * @param month
	 *            The month.
	 * @param dayOfMonth
	 *            The day of the month.
	 * @param hours
	 *            The hour.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 */
	public DateTime(int year, int month, int dayOfMonth, int hours, int minutes, int seconds)
	{
		this((TimeZone) null, year, month, dayOfMonth, hours, minutes, seconds);
	}


	/**
	 * Creates a new absolute {@link DateTime} instance in the given {@link TimeZone} using the {@link #GREGORIAN_CALENDAR_SCALE}.
	 * 
	 * @param timezone
	 *            The {@link TimeZone} of the date, may be <code>null</code> to create a floating date.
	 * @param year
	 *            The year of the date.
	 * @param month
	 *            The month.
	 * @param dayOfMonth
	 *            The month day.
	 * @param hours
	 *            The hours.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 */
	public DateTime(TimeZone timezone, int year, int month, int dayOfMonth, int hours, int minutes, int seconds)
	{
		mCalendarMetrics = GREGORIAN_CALENDAR_SCALE;
		mInstance = Instance.make(year, month, dayOfMonth, hours, minutes, seconds);
		mTimezone = timezone;
		mAllday = false;
	}


	/**
	 * Creates a new {@link DateTime} for the given all-day date using the given calendar scale. As a result of this the all-day flag will be set to true.
	 * 
	 * @param calScale
	 *            The name of the calendar scale to use.
	 * @param year
	 *            The year of the event.
	 * @param month
	 *            The month of the event.
	 * @param dayOfMonth
	 *            The monthday of the event.
	 */
	public DateTime(String calScale, int year, int month, int dayOfMonth)
	{
		mCalendarMetrics = UnicodeCalendarScales.getCalendarMetricsForName(calScale).getCalendarMetrics(Weekday.MO);
		mInstance = Instance.make(year, month, dayOfMonth, 0, 0, 0);
		mTimezone = null;
		mAllday = true;
	}


	/**
	 * Create a new floating DateTime using the given calendar scale.
	 * 
	 * @param calScale
	 *            The name of the calendar scale to use.
	 * @param year
	 *            The year.
	 * @param month
	 *            The month.
	 * @param dayOfMonth
	 *            The day of the month.
	 * @param hours
	 *            The hour.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 */
	public DateTime(String calScale, int year, int month, int dayOfMonth, int hours, int minutes, int seconds)
	{
		this(calScale, null, year, month, dayOfMonth, hours, minutes, seconds);
	}


	/**
	 * Creates a new absolute {@link DateTime} instance in the given {@link TimeZone} and calendar scale.
	 * 
	 * @param calScale
	 *            The name of the calendar scale to use.
	 * @param timezone
	 *            The {@link TimeZone} of the date, may be <code>null</code> to create a floating date.
	 * @param year
	 *            The year of the date.
	 * @param month
	 *            The month.
	 * @param dayOfMonth
	 *            The month day.
	 * @param hours
	 *            The hours.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 */
	public DateTime(String calScale, TimeZone timezone, int year, int month, int dayOfMonth, int hours, int minutes, int seconds)
	{
		mCalendarMetrics = UnicodeCalendarScales.getCalendarMetricsForName(calScale).getCalendarMetrics(Weekday.MO);
		mInstance = Instance.make(year, month, dayOfMonth, hours, minutes, seconds);
		mTimezone = timezone;
		mAllday = false;
	}


	/**
	 * Creates a new {@link DateTime} for the given all-day date using the given calendar metrics.
	 * 
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} to use.
	 * @param year
	 *            The year of the event.
	 * @param month
	 *            The month of the event.
	 * @param dayOfMonth
	 *            The monthday of the event.
	 */
	public DateTime(CalendarMetrics calendarMetrics, int year, int month, int dayOfMonth)
	{
		mCalendarMetrics = calendarMetrics;
		mInstance = Instance.make(year, month, dayOfMonth, 0, 0, 0);
		mTimezone = null;
		mAllday = true;
	}


	/**
	 * Create a new floating DateTime using the given calendar metrics.
	 * 
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} to use.
	 * @param year
	 *            The year.
	 * @param month
	 *            The month.
	 * @param dayOfMonth
	 *            The day of the month.
	 * @param hours
	 *            The hour.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 */
	public DateTime(CalendarMetrics calendarMetrics, int year, int month, int dayOfMonth, int hours, int minutes, int seconds)
	{
		this(calendarMetrics, null, year, month, dayOfMonth, hours, minutes, seconds);
	}


	/**
	 * Creates a new absolute {@link DateTime} instance in the given {@link TimeZone} and calendar metrics.
	 * 
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} to use.
	 * @param timezone
	 *            The {@link TimeZone} of the date, may be <code>null</code> to create a floating date.
	 * @param year
	 *            The year of the date.
	 * @param month
	 *            The month.
	 * @param dayOfMonth
	 *            The month day.
	 * @param hours
	 *            The hours.
	 * @param minutes
	 *            The minutes.
	 * @param seconds
	 *            The seconds.
	 */
	public DateTime(CalendarMetrics calendarMetrics, TimeZone timezone, int year, int month, int dayOfMonth, int hours, int minutes, int seconds)
	{
		mCalendarMetrics = calendarMetrics;
		mInstance = Instance.make(year, month, dayOfMonth, hours, minutes, seconds);
		mTimezone = timezone;
		mAllday = false;
	}


	/**
	 * Internal constructor to create a {@link DateTime} providing all values. It's private, because we can't trust external entities to pass correct
	 * parameters.
	 * 
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} of the new DateTime.
	 * @param timezone
	 *            The {@link TimeZone} of the new DateTime, may be <code>null</code> for floating events.
	 * @param instance
	 *            The packed instance.
	 * @param allDay
	 *            The all-day flag.
	 * @param timeStamp
	 *            The time since the epoch in milliseconds.
	 */
	private DateTime(CalendarMetrics calendarMetrics, TimeZone timezone, long instance, boolean allDay, long timeStamp)
	{
		mCalendarMetrics = calendarMetrics;
		mInstance = instance;
		mTimezone = timezone;
		mAllday = allDay;
		mTimestamp = timeStamp;
	}


	/**
	 * Returns the {@link CalendarMetrics} of this {@link DateTime}.
	 * 
	 * @return The {@link CalendarMetrics} instance.
	 */
	public CalendarMetrics getCalendarMetrics()
	{
		return mCalendarMetrics;
	}


	/**
	 * Returns the {@link TimeZone} of this {@link DateTime}.
	 * 
	 * @return The {@link TimeZone} or <code>null</code> for floating instances.
	 */
	public TimeZone getTimeZone()
	{
		return mTimezone;
	}


	/**
	 * Returns the timestamp of this {@link DateTime} instance. For floating dates, this equals the timestamp in UTC.
	 * 
	 * @return the time in milliseconds since the epoch.
	 */
	public long getTimestamp()
	{
		if (mTimestamp == Long.MAX_VALUE)
		{
			long instance = getInstance();
			return mTimestamp = mCalendarMetrics.toMillis(mTimezone, Instance.year(instance), Instance.month(instance), Instance.dayOfMonth(instance),
				Instance.hour(instance), Instance.minute(instance), Instance.second(instance), 0);
		}
		return mTimestamp;
	}


	/**
	 * Get an all-day {@link DateTime} representing the day of this {@link DateTime} instance.
	 */
	public DateTime toAllDay()
	{
		if (mAllday)
		{
			return this;
		}

		long instance = getInstance();
		return new DateTime(Instance.year(instance), Instance.month(instance), Instance.dayOfMonth(instance));
	}


	/**
	 * Replace the current time zone by the given one, keeping the local time constant. In effect the absolute time will change by the difference of the offsets
	 * to UTC of both time zones. Use this to convert a floating time to an absolute time in specific time zone.
	 * 
	 * @param timezone
	 *            The new {@link TimeZone}.
	 * @throws IllegalStateException
	 *             if the date is all-day.
	 */
	public DateTime swapTimeZone(TimeZone timezone)
	{
		if (mAllday)
		{
			throw new IllegalStateException("can not swap the time zone of an all-day date");
		}

		TimeZone oldTimeZone = mTimezone;

		if (oldTimeZone == null && timezone == null || oldTimeZone != null && oldTimeZone.equals(timezone))
		{
			// time zone didn't change
			return this;
		}

		long timestamp = mTimestamp;
		if (timestamp == Long.MAX_VALUE || oldTimeZone != null && oldTimeZone.hasSameRules(timezone) || sameTimestamps(oldTimeZone, timezone))
		{
			// we don't have a timestamp or we don't need to change it
			return new DateTime(mCalendarMetrics, timezone, getInstance(), false, timestamp);
		}

		return new DateTime(mCalendarMetrics, timezone, getInstance(), false, Long.MAX_VALUE);
	}


	/**
	 * Replace the current time zone by the given one, keeping the absolute time constant. In effect the local time will change by the difference of the offsets
	 * to UTC of both time zones.
	 * 
	 * @param timezone
	 *            The new {@link TimeZone}.
	 * @throws IllegalStateException
	 *             if the date is all-day.
	 */
	public DateTime shiftTimeZone(TimeZone timezone)
	{
		if (mAllday)
		{
			throw new IllegalStateException("can not shift the time zone of an all-day date");
		}

		TimeZone oldTimeZone = mTimezone;

		if (oldTimeZone == null && timezone == null || oldTimeZone != null && oldTimeZone.equals(timezone))
		{
			// time zone didn't change
			return this;
		}

		long instance = mInstance;
		if (instance == Long.MAX_VALUE || oldTimeZone != null && oldTimeZone.hasSameRules(timezone) || sameTimestamps(oldTimeZone, timezone))
		{
			// we don't have an instance or we don't need to change it
			return new DateTime(mCalendarMetrics, timezone, getInstance(), false, getTimestamp());

		}

		return new DateTime(timezone, getTimestamp());
	}


	/**
	 * Returns whether this is an all-day instance.
	 * 
	 * @return <code>true</code> if this is all-day, <code>false</code> otherwise.
	 */
	public boolean isAllDay()
	{
		return mAllday;
	}


	/**
	 * Returns whether this is a floating instance.
	 * 
	 * @return <code>true</code> if this is floating, <code>false</code> otherwise.
	 */
	public boolean isFloating()
	{
		return mTimezone == null;
	}


	/**
	 * Returns the year of this date-time object.
	 * 
	 * @return The year of this date-time object.
	 */
	public int getYear()
	{
		return Instance.year(getInstance());
	}


	/**
	 * Returns the month of this date-time object.
	 * 
	 * @return The month of this date-time object.
	 */
	public int getMonth()
	{
		return Instance.month(getInstance());
	}


	/**
	 * Returns the week of year of this date-time object.
	 * 
	 * @return The week of year of this date-time object.
	 */
	public int getWeekOfYear()
	{
		int weekOfYear = mWeekOfYear;
		if (weekOfYear < 0)
		{
			long instance = getInstance();
			mWeekOfYear = weekOfYear = mCalendarMetrics.getWeekOfYear(Instance.year(instance), Instance.month(instance), Instance.dayOfMonth(instance));
		}
		return weekOfYear;
	}


	/**
	 * Returns the week day of this DateTime object.
	 * 
	 * @return The week day of this DateTime object.
	 */
	public int getDayOfWeek()
	{
		int dayOfweek = mDayOfWeek;
		if (dayOfweek < 0)
		{
			long instance = getInstance();
			mDayOfWeek = dayOfweek = mCalendarMetrics.getDayOfWeek(Instance.year(instance), Instance.month(instance), Instance.dayOfMonth(instance));
		}
		return dayOfweek;
	}


	/**
	 * Returns the month day of this date-time object.
	 * 
	 * @return The month day of this date-time object.
	 */
	public int getDayOfMonth()
	{
		return Instance.dayOfMonth(getInstance());
	}


	/**
	 * Returns the hours in this date-time object. For all-day dates this always returns <code>0</code>.
	 * 
	 * @return The hours in this date-time object.
	 */
	public int getHours()
	{
		return Instance.hour(getInstance());
	}


	/**
	 * Returns the minutes in this date-time object. For all-day dates this always returns <code>0</code>.
	 * 
	 * @return The minutes in this date-time object.
	 */
	public int getMinutes()
	{
		return Instance.minute(getInstance());
	}


	/**
	 * Returns the seconds in this date-time object. For all-day dates this always returns <code>0</code>.
	 * 
	 * @return The seconds in this date-time object.
	 */
	public int getSeconds()
	{
		return Instance.second(getInstance());
	}


	/**
	 * Add the given duration to this DateTime. This method returns a new DateTime instance that represents the value after the duration has been added. Values
	 * are added with respect to the time zone, so daylight saving changes are taken into account when adding hours, minutes or seconds.
	 * 
	 * @param duration
	 *            The {@link Duration} to add.
	 * @return The new {@link DateTime}.
	 */
	public DateTime addDuration(Duration duration)
	{
		if (duration == null)
		{
			throw new IllegalArgumentException("Duration must not be null");
		}

		if (duration.isZero())
		{
			return this;
		}

		if (mAllday && duration.getSecondsOfDay() > 0)
		{
			throw new IllegalArgumentException("Can't add a duration with time to an all-day DateTime.");
		}

		long newInstance = mInstance;
		if (newInstance == Long.MAX_VALUE && (mTimezone == null || !mTimezone.useDaylightTime()))
		{
			/*
			 * We don't have an instance value yet and timezone is null or it doesn't use daylight savings. That means, we can save a lot of time if we don't
			 * calculate the instance value, but add the milliseconds of the duration to the timestamp.
			 */
			return new DateTime(mCalendarMetrics, mTimezone, Long.MAX_VALUE, mAllday, mTimestamp + duration.toMillis());
		}

		if (duration.getRawDays() > 0)
		{
			if (duration.getSign() > 0)
			{
				newInstance = mCalendarMetrics.nextDay(getInstance(), duration.getRawDays());
			}
			else
			{
				newInstance = mCalendarMetrics.prevDay(getInstance(), duration.getRawDays());
			}
		}

		if (duration.getSecondsOfDay() == 0)
		{
			return new DateTime(mCalendarMetrics, mTimezone, newInstance, mAllday, Long.MAX_VALUE);
		}

		long newTimestamp = newInstance == mInstance ? getTimestamp() : mCalendarMetrics.toMillis(newInstance == Long.MAX_VALUE ? getInstance() : newInstance,
			mTimezone);
		newTimestamp += duration.getSign() * duration.getSecondsOfDay() * 1000;
		return new DateTime(mCalendarMetrics, mTimezone, newTimestamp);
	}


	/**
	 * Get the packed instance value of this DateTime. This is mostly for internal use.
	 * 
	 * @return The packed instance value.
	 */
	public long getInstance()
	{
		long instance = mInstance;
		if (instance == Long.MAX_VALUE)
		{
			return mInstance = mCalendarMetrics.toInstance(mTimestamp, mTimezone);
		}
		return instance;
	}


	/**
	 * Check if another date is earlier in time than this one.
	 * <p>
	 * Note: it doesn't make sense to compare a floating time to an absolute time. However, this method won't complain and treat floating times like UTC times.
	 * </p>
	 * 
	 * @param that
	 *            The {@link DateTime} to compare to.
	 * @return <code>true</code> if this instance is after the given instance.
	 */
	public boolean after(DateTime that)
	{
		return getTimestamp() > that.getTimestamp();
	}


	/**
	 * Check if another date is later in time than this one.
	 * <p>
	 * Note: it doesn't make sense to compare a floating time to an absolute time. However, this method won't complain and treat floating times like UTC times.
	 * </p>
	 * 
	 * @param that
	 *            The {@link DateTime} to compare to.
	 * @return <code>true</code> if this instance is before the given instance.
	 */
	public boolean before(DateTime that)
	{
		return getTimestamp() < that.getTimestamp();
	}


	/**
	 * Parses a date-time string as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. This method uses the
	 * default calendar scale {@link #GREGORIAN_CALENDAR_SCALE}. Unless the given String ends with "Z" the resulting {@link DateTime} will be floating.
	 * 
	 * @param string
	 *            A valid date-time string.
	 * @return A new {@link DateTime} instance.
	 * 
	 * @see #parse(CalendarMetrics, TimeZone, String)
	 */
	public static DateTime parse(String string)
	{
		return parse(GREGORIAN_CALENDAR_SCALE, null, string);
	}


	/**
	 * Parses a date-time string as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. This method uses the
	 * default calendar scale {@link #GREGORIAN_CALENDAR_SCALE}.
	 * 
	 * @param timeZone
	 *            A time zone to apply to non-allday and non-UTC date-time values. If timeZone is <code>null</code> the event will be floating.
	 * @param string
	 *            A valid date-time string.
	 * @return A new {@link DateTime} instance.
	 * 
	 * @see #parse(CalendarMetrics, TimeZone, String)
	 */
	public static DateTime parse(String timeZone, String string)
	{
		return parse(GREGORIAN_CALENDAR_SCALE, timeZone == null ? null : TimeZone.getTimeZone(timeZone), string);
	}


	/**
	 * Parses a date-time string as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. This method uses the
	 * default calendar scale {@link #GREGORIAN_CALENDAR_SCALE}.
	 * 
	 * @param timeZone
	 *            A time zone to apply to non-allday and non-UTC date-time values. If timeZone is <code>null</code> the event will be floating.
	 * @param string
	 *            A valid date-time string.
	 * @return A new {@link DateTime} instance.
	 * 
	 * @see #parse(CalendarMetrics, TimeZone, String)
	 */
	public static DateTime parse(TimeZone timeZone, String string)
	{
		return parse(GREGORIAN_CALENDAR_SCALE, timeZone, string);
	}


	/**
	 * Parses a date-time string as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. This method takes the
	 * name of the calendar scale to use.
	 * 
	 * 
	 * @param calScale
	 *            The name of the calendar scale to use.
	 * @param timeZone
	 *            A time zone to apply to non-allday and non-UTC date-time values. If timeZone is <code>null</code> the event will be floating.
	 * @param string
	 *            A valid date-time string.
	 * @return A new {@link DateTime} instance.
	 * 
	 * @see #parse(CalendarMetrics, TimeZone, String)
	 */
	public static DateTime parse(String calScale, TimeZone timeZone, String string)
	{
		return parse(UnicodeCalendarScales.getCalendarMetricsForName(calScale).getCalendarMetrics(Weekday.MO), timeZone, string);
	}


	/**
	 * Parses a date-time string as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. There are three valid
	 * forms of such a String:
	 * <ul>
	 * <li><code>YYYYMMDD</code></li>
	 * <li><code>YYYYMMDD'T'HHMMSS</code></li>
	 * <li><code>YYYYMMDD'T'HHMMSS'Z'</code></li>
	 * </ul>
	 * where YYYYMMDD means a date (year, month, day of month) and HHMMSS means a time (hour, minute, second). <code>'T'</code> and <code>'Z'</code> stand for
	 * the literals <code>T</code> and <code>Z</code>.
	 * 
	 * If 'Z' is present the time zone is UTC. Otherwise the time zone is specified in an additional parameter or if no such parameter exists it's floating
	 * (i.e. always local time).
	 * <p>
	 * Use {@link #swapTimeZone(TimeZone)} to set a specific time zone.
	 * </p>
	 * 
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} to use.
	 * @param timeZone
	 *            A time zone to apply to non-allday and non-UTC date-time values. If timeZone is <code>null</code> the event will be floating unless it ends
	 *            with 'Z'.
	 * @param string
	 *            A valid date-time string.
	 * @return A new {@link DateTime} instance.
	 */
	public static DateTime parse(CalendarMetrics calendarMetrics, TimeZone timeZone, String string)
	{
		if (string == null)
		{
			throw new NullPointerException("a date-time string must not be null");
		}

		try
		{
			if (string.length() == 8)
			{
				return new DateTime(calendarMetrics, parseFourDigits(string, 0), parseTwoDigits(string, 4) - 1, parseTwoDigits(string, 6));
			}
			else if (string.length() == 15 && string.charAt(8) == 'T')
			{
				return new DateTime(calendarMetrics, timeZone, parseFourDigits(string, 0), parseTwoDigits(string, 4) - 1, parseTwoDigits(string, 6),
					parseTwoDigits(string, 9), parseTwoDigits(string, 11), parseTwoDigits(string, 13));
			}
			else if (string.length() == 16 && string.charAt(8) == 'T' && string.charAt(15) == 'Z')
			{
				return new DateTime(calendarMetrics, UTC, parseFourDigits(string, 0), parseTwoDigits(string, 4) - 1, parseTwoDigits(string, 6), parseTwoDigits(
					string, 9), parseTwoDigits(string, 11), parseTwoDigits(string, 13));
			}
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("illegal characters in date-time string: '" + string + "'", e);
		}
		throw new IllegalArgumentException("illegal date-time string: '" + string + "'");
	}


	@Override
	public int hashCode()
	{
		return (int) getTimestamp();
	}


	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DateTime))
		{
			return false;
		}

		DateTime other = (DateTime) obj;

		if (mInstance != Long.MAX_VALUE && other.mInstance != Long.MAX_VALUE)
		{
			// compare by instance
			return mInstance == other.mInstance && mAllday == other.mAllday && mCalendarMetrics.scaleEquals(other.mCalendarMetrics)
				&& (mTimezone == other.mTimezone || mTimezone != null && other.mTimezone != null && sameTimestamps(mTimezone, other.mTimezone));
		}
		else
		{
			// compare by timestamp
			return mAllday == other.mAllday && mCalendarMetrics.scaleEquals(other.mCalendarMetrics) && getTimestamp() == other.getTimestamp()
				&& (mTimezone == other.mTimezone || mTimezone != null && other.mTimezone != null && sameTimestamps(mTimezone, other.mTimezone));

		}
	}


	@Override
	public String toString()
	{
		long instance = getInstance();

		// build a date string that complies to RFC 5545
		StringBuilder result = new StringBuilder(16);
		Instance.writeTo(result, instance, mAllday);
		TimeZone tz = mTimezone;
		if (!mAllday && tz != null && "UTC".equals(tz.getID()))
		{
			result.append('Z');
		}

		return result.toString();
	}


	/**
	 * <p>
	 * Write the date-time string represented by this object to the given {@link StringBuilder}. The written value conforms to <a
	 * href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>.
	 * </p>
	 * <h3>Examples:</h3>
	 * 
	 * <pre>
	 * 20150320
	 * 20150320T123000
	 * 20150320T123000Z
	 * </pre>
	 * 
	 * @param out
	 *            The {@link StringBuilder} to write to.
	 * @throws IOException
	 */
	public void writeTo(StringBuilder out) throws IOException
	{
		long instance = getInstance();

		Instance.writeTo(out, instance, mAllday);
		TimeZone tz = mTimezone;
		if (!mAllday && tz != null && "UTC".equals(tz.getID()))
		{
			out.append('Z');
		}
	}


	/**
	 * Write the date-time string represented by this object to the given {@link Writer}.. The written value conforms to <a
	 * href="https://tools.ietf.org/html/rfc5545#section-3.3.5">RFC 5545, Section 3.3.5</a>. </p> <h3>Examples:</h3>
	 * 
	 * <pre>
	 * 20150320
	 * 20150320T123000
	 * 20150320T123000Z
	 * </pre>
	 * 
	 * @param out
	 *            The {@link Writer} to write to.
	 * @throws IOException
	 */
	public void writeTo(Writer out) throws IOException
	{
		long instance = getInstance();

		Instance.writeTo(out, instance, mAllday);
		TimeZone tz = mTimezone;
		if (!mAllday && tz != null && "UTC".equals(tz.getID()))
		{
			out.write('Z');
		}
	}


	/**
	 * Checks whether the timestamp of an event would change when switching from one {@link TimeZone} to another one.
	 * 
	 * @param first
	 *            A {@link TimeZone}.
	 * @param second
	 *            Another {@link TimeZone}.
	 * @return <code>true</code> if the timestamp won't change when switching the time zones.
	 */
	private static boolean sameTimestamps(TimeZone first, TimeZone second)
	{
		if (first == second)
		{
			return true;
		}

		String firstId = first != null ? first.getID() : null;
		if (second == null && ("UTC".equals(firstId) || UTC.equals(first) || UTC.hasSameRules(first)))
		{
			// second time zone is "floating" and first one is UTC, we don't need to change the time stamp
			return true;
		}

		String secondId = second != null ? second.getID() : null;
		if (first == null && ("UTC".equals(secondId) || UTC.equals(second) || UTC.hasSameRules(second)))
		{
			// first time zone is "floating" and second one is UTC, we don't need to change the time stamp
			return true;
		}

		return first != null && second != null && (firstId.equals(secondId) || first.equals(second) || first.hasSameRules(second));
	}


	/**
	 * Parses the next four characters in the given {@link String} at the given offset as an integer.
	 * 
	 * @param string
	 *            The {@link String} to parse.
	 * @param offset
	 *            The offset of the number in the string.
	 * @return The integer value.
	 * @throws NumberFormatException
	 *             if the String doesn't contain digits at the given offset.
	 */
	private static int parseFourDigits(String string, int offset)
	{
		return parseTwoDigits(string, offset) * 100 + parseTwoDigits(string, offset + 2);
	}


	/**
	 * Parses the next two characters in the given {@link String} at the given offset as an integer.
	 * 
	 * @param string
	 *            The {@link String} to parse.
	 * @param offset
	 *            The offset of the number in the string.
	 * @return The integer value.
	 * @throws NumberFormatException
	 *             if the String doesn't contain digits at the given offset.
	 */
	private static int parseTwoDigits(String string, int offset)
	{
		int d1 = string.charAt(offset) - '0';
		int d2 = string.charAt(offset + 1) - '0';

		if (d1 < 0 || d2 < 0 || d1 > 9 || d2 > 9)
		{
			throw new NumberFormatException("illegal digit in number " + string.substring(offset, 2));
		}

		return d1 * 10 + d2;
	}

}
