/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.calendar.old;

import com.openexchange.groupware.calendar.CalendarCollectionUtils;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.RecurringResultInterface;

/**
 * {@link RecurringResult} - Represents an occurrence in a recurring event.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public final class RecurringResult implements RecurringResultInterface {

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
		normalized = CalendarCollectionUtils.normalizeLong(start);
		this.diff = diff;
		this.lengthOffset = lengthOffset;
		this.position = position;
	}

	@Override
    public long getStart() {
		return start;
	}

	@Override
    public long getNormalized() {
		return normalized;
	}

	@Override
    public long getEnd() {
		return start + diff + (lengthOffset * Constants.MILLI_DAY);
	}

	@Override
    public long getDiff() {
		return diff;
	}

	@Override
    public int getOffset() {
		return lengthOffset;
	}

	@Override
    public int getPosition() {
		return position;
	}

}
