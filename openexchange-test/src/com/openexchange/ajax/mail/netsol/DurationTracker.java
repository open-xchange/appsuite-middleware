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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ajax.mail.netsol;

/**
 * {@link DurationTracker} - Tracks execution times and provides maximum,
 * minimum, and average times.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class DurationTracker {

	private final long[] durations;

	private boolean full;

	private int pointer;

	private long maxDuration = Long.MIN_VALUE;

	private long minDuration = Long.MAX_VALUE;

	/**
	 * Initializes a new {@link DurationTracker}
	 */
	public DurationTracker(final int length) {
		super();
		durations = new long[length];
	}

	/**
	 * Adds a duration time to this duration tracker
	 *
	 * @param duration
	 *            The duration time
	 */
	public void addDuration(final long duration) {
		maxDuration = Math.max(duration, maxDuration);
		minDuration = Math.min(duration, minDuration);
		durations[pointer++] = duration;
		pointer %= durations.length;
		if (!full && (pointer == 0)) {
			full = true;
		}
	}

	/**
	 * Get max. duration
	 *
	 * @return Max. duration
	 */
	public long getMaxDuration() {
		return maxDuration;
	}

	/**
	 * Get min. duration
	 *
	 * @return Min. duration
	 */
	public long getMinDuration() {
		return minDuration;
	}

	/**
	 * Compute the average duration
	 *
	 * @return The average duration
	 */
	public double computeAvgDuration() {
		long avgDuration = 0;
		final int len = full ? durations.length : pointer;
		for (int i = 0; i < len; i++) {
			avgDuration += durations[i];
		}
		return (avgDuration / (double) len);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(64);
		sb.append("Max. duration=").append(maxDuration).append("msec, Min. duration=").append(minDuration);
		sb.append("msec, Avg. duration=").append(computeAvgDuration()).append("msec");
		return sb.toString();
	}
}
