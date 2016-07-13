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


/**
 * Represents duration values as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.6">RFC 5545, Section 3.3.6</a>.
 * <p>
 * Objects of this class are immutable.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class Duration
{
	private final static int PARSER_STATE_SIGN = 0;
	private final static int PARSER_STATE_P = 1;
	private final static int PARSER_STATE_D = 3;
	private final static int PARSER_STATE_T = 4;
	private final static int PARSER_STATE_H = 5;
	private final static int PARSER_STATE_M = 6;
	private final static int PARSER_STATE_S = 7;
	private final static int PARSER_STATE_W = 8;

	/**
	 * A factor that sets the sign of the Duration value. Either <code>1</code> or <code>-1</code>.
	 */
	private final int mSign;

	/**
	 * The number of days of the duration.
	 */
	private final int mDays;

	/**
	 * The time in seconds. We split it into hours, minutes and seconds whenever we need to.
	 */
	private final int mTime;


	/**
	 * Create a duration of the given number of weeks.
	 * 
	 * @param sign
	 *            The factor that determines the sign. Must be either <code>1</code> or <code>-1</code>.
	 * @param weeks
	 *            The number of weeks of the duration.
	 */
	public Duration(int sign, int weeks)
	{
		this(sign, weeks * 7, 0);
	}


	/**
	 * Create a duration of the given day and time values.
	 * 
	 * @param sign
	 *            The factor that determines the sign. Must be either <code>1</code> or <code>-1</code>.
	 * @param days
	 *            The number of days of the duration.
	 * @param hours
	 *            The number of hours of the duration.
	 * @param minutes
	 *            The number of minutes of the duration.
	 * @param seconds
	 *            The number of seconds of the duration.
	 */
	public Duration(int sign, int days, int hours, int minutes, int seconds)
	{
		this(sign, days, hours * 3600 + minutes * 60 + seconds);
	}


	/**
	 * Create a duration of the given day and time values.
	 * 
	 * @param sign
	 *            The factor that determines the sign. Must be either <code>1</code> or <code>-1</code>.
	 * @param days
	 *            The number of days of the duration.
	 * @param time
	 *            The time of the duration in seconds.
	 */
	public Duration(int sign, int days, int time)
	{
		if (sign != 1 && sign != -1)
		{
			throw new IllegalArgumentException("sign must be 1 or -1");
		}

		if (days < 0 || time < 0)
		{
			throw new IllegalArgumentException("Duration values must be >=0");
		}

		this.mSign = sign;
		this.mDays = days;
		this.mTime = time;
	}


	/**
	 * Returns whether this Duration represents zero time.
	 * 
	 * @return <code>true</code> if all values are <code>0</code>, <code>false</code> otherwise.
	 */
	public boolean isZero()
	{
		return mDays + mTime == 0; // since both, mDays and mTime, are >= 0 this only evaluates to true if both equal 0
	}


	/**
	 * Return the sign of this duration.
	 * 
	 * @return Either <code>1</code> or <code>-1</code>.
	 */
	public int getSign()
	{
		return mSign;
	}


	/**
	 * Return the weeks of this duration. This will return <code>0</code> if the number of days <em>is not</em> a multiple of 7 or the duration has a time part.
	 * 
	 * @return The number of weeks.
	 */
	public int getWeeks()
	{
		return (mDays % 7) + mTime == 0 ? mDays / 7 : 0;
	}


	/**
	 * Return the days of this duration. This will return <code>0</code> if the number of days <em>is</em> a multiple of 7 and the duration has no time part.
	 * 
	 * Use {@link #getRawDays()} to get the actual number of days.
	 * 
	 * @return The number of days.
	 */
	public int getDays()
	{
		return (mDays % 7) + mTime > 0 ? mDays : 0;
	}


	/**
	 * Returns the actual number of days, without taking weeks into account. See {@link #getDays()} to get the number of days as in the resulting Duration
	 * string.
	 * 
	 * @return The actual number of days of this duration.
	 */
	public int getRawDays()
	{
		return mDays;
	}


	/**
	 * Return the hours of this duration.
	 * 
	 * @return The number of hours.
	 */
	public int getHours()
	{
		return mTime / 3600;
	}


	/**
	 * Return the minutes of this duration.
	 * 
	 * @return The number of minutes.
	 */
	public int getMinutes()
	{
		return (mTime / 60) % 60;
	}


	/**
	 * Return the seconds of this duration.
	 * 
	 * @return The number of seconds.
	 */
	public int getSeconds()
	{
		return mTime % 60;
	}


	/**
	 * Returns the time part in seconds. The result equals:
	 * 
	 * <pre>
	 * getHours() * 3600 + getMinutes * 60 + getSeconds()
	 * </pre>
	 * 
	 * but this method is faster than calling the above.
	 * 
	 * @return
	 */
	public int getSecondsOfDay()
	{
		return mTime;
	}


	/**
	 * Returns the value of this Duration in milliseconds, assuming days of 24 hours. This is not always correct. The actual number of milliseconds may be less
	 * if a daylight savings transition occurs in a specific interval.
	 * 
	 * @return The duration in milliseconds.
	 */
	public long toMillis()
	{
		return mSign * (mDays * 24L * 3600L + mTime) * 1000L;
	}


	/**
	 * Add this duration to the given timestamp, taking daylight savings in the given time zone into account.
	 * 
	 * @param timezone
	 *            The {@link TimeZone} of the event or <code>null</code> in case of a floating event.
	 * @param timestamp
	 *            The timestamp in milliseconds since the epoch.
	 * @return The new timestamp.
	 */
	public long addTo(TimeZone timezone, long timestamp)
	{
		if (isZero())
		{
			return timestamp;
		}
		if (timezone == null || !timezone.useDaylightTime())
		{
			// if the timezone doesn't have daylight time we can simply add the duration in milliseconds to the timestamp
			return timestamp + toMillis();
		}
		return new DateTime(timezone, timestamp).addDuration(this).getTimestamp();
	}


	/**
	 * Add another {@link Duration} to this one and return a new {@link Duration} object.
	 * 
	 * @param other
	 *            The other {@link Duration} to add.
	 * @return The resulting {@link Duration}.
	 */
	public Duration addDuration(Duration other)
	{
		if (other == null)
		{
			throw new IllegalArgumentException("Duration must not be null");
		}

		if (other.isZero())
		{
			return this;
		}

		if (this.isZero())
		{
			return other;
		}

		if (mSign == other.mSign)
		{
			// both durations have the same sign, we can just add the values
			int newDays = mDays + other.mDays;

			int newTime = mTime + other.mTime;

			return new Duration(mSign, newDays, newTime);
		}

		int newDays = mSign * mDays + other.mSign * other.mDays;

		int newTime = mSign * mTime + other.mSign * other.mTime;

		if (newDays >= 0 && newTime >= 0)
		{
			return new Duration(1, newDays, newTime);
		}

		if (newDays < 0 && newTime < 0)
		{
			return new Duration(-1, -newDays, -newTime);
		}

		/*
		 * We have days and time with opposite signs. That's a problem, because it's not allowed by RFC 5545.
		 * 
		 * The only way to resolve that is by assuming 24 hour days and do the math to bring both values to the same sign.
		 */

		int totalSeconds = newDays * 24 * 3600 + newTime;

		newDays = totalSeconds / (24 * 3600);
		newTime = totalSeconds % (24 * 3600);

		if (totalSeconds >= 0)
		{
			return new Duration(1, newDays, newTime);
		}
		else
		{
			return new Duration(-1, -newDays, -newTime);
		}
	}


	/**
	 * Parse the given Duration String to a {@link Duration} value.
	 * 
	 * @param durationString
	 *            A String that conforms to a Duration as specified in <a href="https://tools.ietf.org/html/rfc5545#section-3.3.6">RFC 5545, Section 3.3.6</a>.
	 * @return A {@link Duration} instance.
	 * @throws IllegalArgumentException
	 *             if the Duration String is malformed.
	 */
	public static Duration parse(String durationString)
	{
		if (durationString == null || durationString.length() < 3)
		{
			throw new IllegalArgumentException("Invalid Duration string: " + durationString);
		}

		int sign = 1;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;

		int currentValue = 0;
		int parserState = -1;
		int nextState = -1;
		boolean hadD = false;
		boolean hadT = false;

		for (int i = 0, count = durationString.length(); i < count; ++i)
		{
			char c = durationString.charAt(i);
			boolean wasDigit = false;
			switch (c)
			{
				case '-':
				{
					sign = -1;
				}
				case '+':
				{
					nextState = PARSER_STATE_SIGN;
					break;
				}
				case 'P':
				case 'p':
				{
					nextState = PARSER_STATE_P;
					break;
				}
				case 'D':
				case 'd':
				{
					days = currentValue;
					currentValue = 0;
					nextState = PARSER_STATE_D;
					hadD = true;
					break;
				}
				case 'T':
				case 't':
				{
					nextState = PARSER_STATE_T;
					hadT = true;
					break;
				}
				case 'H':
				case 'h':
				{
					hours = currentValue;
					currentValue = 0;
					nextState = PARSER_STATE_H;
					break;
				}
				case 'M':
				case 'm':
				{
					minutes = currentValue;
					currentValue = 0;
					nextState = PARSER_STATE_M;
					break;
				}
				case 'S':
				case 's':
				{
					seconds = currentValue;
					currentValue = 0;
					nextState = PARSER_STATE_S;
					break;
				}
				case 'W':
				case 'w':
				{
					weeks = currentValue;
					currentValue = 0;
					nextState = PARSER_STATE_W;
					break;
				}
				default:
				{
					if (c < '0' || c > '9')
					{
						throw new IllegalArgumentException("Unexpected char '" + c + "' at position " + i);
					}
					currentValue = currentValue * 10 + (c - '0');
					wasDigit = true;
				}
			}

			if (parserState > nextState || !wasDigit && parserState == nextState)
			{
				throw new IllegalArgumentException("Unexpected char '" + c + "' at position " + i);
			}

			parserState = nextState;
		}

		if (parserState <= PARSER_STATE_P || !hadD && parserState == PARSER_STATE_T || !hadT && parserState > PARSER_STATE_T && parserState < PARSER_STATE_W)
		{
			throw new IllegalArgumentException("Invalid duration string: " + durationString);
		}

		if (weeks != 0)
		{
			return new Duration(sign, weeks);
		}
		else
		{
			return new Duration(sign, days, hours, minutes, seconds);
		}
	}


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(15);
		writeTo(sb);
		return sb.toString();
	}


	/**
	 * Write the Duration String to the given {@link StringBuilder}.
	 * 
	 * @param builder
	 *            A {@link StringBuilder} to write to.
	 */
	public void writeTo(StringBuilder builder)
	{
		if (mSign < 0 && (mDays > 0 || mTime > 0))
		{
			builder.append('-');
		}

		builder.append('P');

		int weeks = getWeeks();
		if (weeks > 0)
		{
			builder.append(weeks);
			builder.append('W');
		}
		else
		{
			if (mDays > 0)
			{
				builder.append(mDays);
				builder.append('D');
			}

			if (mTime != 0)
			{
				int hours = getHours();
				int minutes = getMinutes();
				int seconds = getSeconds();

				builder.append('T');
				if (hours > 0)
				{
					builder.append(hours);
					builder.append('H');
				}
				if (minutes > 0)
				{
					builder.append(minutes);
					builder.append('M');
				}
				if (seconds > 0)
				{
					builder.append(seconds);
					builder.append('S');
				}
			}
			else if (mDays == 0)
			{
				builder.append("0D");
			}
		}
	}


	/**
	 * Write the Duration String to the given {@link Writer}.
	 * 
	 * @param writer
	 *            A {@link Writer} to write to.
	 * @throws IOException
	 */
	public void writeTo(Writer writer) throws IOException
	{
		if (mSign < 0 && (mDays > 0 || mTime > 0))
		{
			writer.append('-');
		}

		writer.append('P');

		int weeks = getWeeks();
		if (weeks > 0)
		{
			writer.write(Integer.toString(weeks));
			writer.write('W');
		}
		else
		{
			if (mDays > 0)
			{
				writer.write(Integer.toString(mDays));
				writer.write('D');
			}

			if (mTime != 0)
			{
				int hours = getHours();
				int minutes = getMinutes();
				int seconds = getSeconds();

				writer.write('T');
				if (hours > 0)
				{
					writer.write(Integer.toString(hours));
					writer.write('H');
				}
				if (minutes > 0)
				{
					writer.write(Integer.toString(minutes));
					writer.write('M');
				}
				if (seconds > 0)
				{
					writer.write(Integer.toString(seconds));
					writer.write('S');
				}
			}
			else if (mDays == 0)
			{
				writer.write("0D");
			}
		}
	}


	@Override
	public int hashCode()
	{
		return mSign * (mDays * 24 * 3600 + mTime);
	}


	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Duration))
		{
			return false;
		}
		Duration other = (Duration) obj;
		return mSign == other.mSign && mDays == other.mDays && mTime == other.mTime;
	}
}
