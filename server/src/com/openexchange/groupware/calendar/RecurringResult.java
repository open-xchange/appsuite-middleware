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

package com.openexchange.groupware.calendar;

/**
 * {@link RecurringResult} - Represents an occurrence in a recurring event.
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public final class RecurringResult {

	private final long normalized;

	private final long start;

	private final long diff;

	private final int lengthOffset;

	private final int position;

	/**
	 * Initializes a new {@link RecurringResult}
	 * 
	 * @param start
	 *            The start time in milliseconds
	 * @param diff
	 *            The single event's duration in milliseconds
	 * @param lengthOffset
	 *            The length offset (actually the duration in days)
	 * @param position
	 *            The one-based position
	 */
	public RecurringResult(final long start, final long diff, final int lengthOffset, final int position) {
		this.start = start;
		normalized = CalendarRecurringCollection.normalizeLong(start);
		this.diff = diff;
		this.lengthOffset = lengthOffset;
		this.position = position;
	}

	/**
	 * Gets the recurring result's start time in milliseconds which is the
	 * number of milliseconds since January 1, 1970, 00:00:00 GMT
	 * 
	 * @return The recurring result's start time in milliseconds which is the
	 *         number of milliseconds since January 1, 1970, 00:00:00 GMT
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Gets the normalized recurring result's start time in milliseconds
	 * 
	 * @return The normalized recurring result's start time in milliseconds
	 * @see #getStart()
	 */
	public long getNormalized() {
		return normalized;
	}

	/**
	 * Gets the recurring result's end time in milliseconds which is the number
	 * of milliseconds since January 1, 1970, 00:00:00 GMT
	 * 
	 * @return The recurring result's end time in milliseconds which is the
	 *         number of milliseconds since January 1, 1970, 00:00:00 GMT
	 */
	public long getEnd() {
		return start + diff + (lengthOffset * Constants.MILLI_DAY);
	}

	/**
	 * Gets the result's duration in milliseconds
	 * 
	 * @return The result's duration in milliseconds
	 */
	public long getDiff() {
		return diff;
	}

	/**
	 * Gets the length offset (actually the duration in days)
	 * 
	 * @return The length offset (actually the duration in days)
	 */
	public int getOffset() {
		return lengthOffset;
	}

	/**
	 * Gets the one-based position
	 * 
	 * @return The one-based position
	 */
	public int getPosition() {
		return position;
	}

}
