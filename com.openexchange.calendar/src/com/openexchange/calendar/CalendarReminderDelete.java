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

package com.openexchange.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CalendarReminderDelete} - The {@link TargetService}
 * implementation for calendar module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class CalendarReminderDelete implements TargetService {

	/**
	 * Initializes a new {@link CalendarReminderDelete}
	 */
	public CalendarReminderDelete() {
		super();
	}

	@Override
    public void updateTargetObject(final Context ctx, final Connection con, final int targetId)
			throws OXException {
		updateAppointmentReminder(con, -1, targetId, ctx.getContextId());
		updateAppointmentLastModified(con, targetId, ctx.getContextId());
	}

	@Override
    public void updateTargetObject(final Context ctx, final Connection con, final int targetId, final int userId)
			throws OXException {
		updateAppointmentReminder(con, userId, targetId, ctx.getContextId());
		updateAppointmentLastModified(con, targetId, ctx.getContextId());
	}

	private static final String SQL_DEL_ALL_REMINDER = "UPDATE prg_dates_members SET reminder = ? WHERE cid = ? AND object_id = ?";

	private static final String SQL_DEL_SINGLE_REMINDER = "UPDATE prg_dates_members SET reminder = ? WHERE cid = ? AND object_id = ? AND member_uid = ?";

	private static void updateAppointmentReminder(final Connection con, final int userId, final int objectId,
			final int cid) throws OXException {
		final PreparedStatement stmt;
		try {
			stmt = con.prepareStatement(userId == -1 ? SQL_DEL_ALL_REMINDER : SQL_DEL_SINGLE_REMINDER);
		} catch (final SQLException e) {
			throw handleSQLException(e);
		}
		try {
			int pos = 1;
			stmt.setNull(pos++, Types.INTEGER);
			stmt.setInt(pos++, cid);
			stmt.setInt(pos++, objectId);
			if (userId != -1) {
				stmt.setInt(pos++, userId);
			}
			stmt.executeUpdate();
		} catch (final SQLException e) {
			throw handleSQLException(e);
		} finally {
			DBUtils.closeSQLStuff(null, stmt);
		}
	}

	private static final String SQL_UP_LAST_MODIFIED = "UPDATE prg_dates SET changing_date = ? WHERE cid = ? AND intfield01 = ?";

	private static void updateAppointmentLastModified(final Connection con, final int objectId, final int cid)
			throws OXException {
		final PreparedStatement stmt;
		try {
			stmt = con.prepareStatement(SQL_UP_LAST_MODIFIED);
		} catch (final SQLException e) {
			throw handleSQLException(e);
		}
		try {
			int pos = 1;
			stmt.setLong(pos++, System.currentTimeMillis());
			stmt.setInt(pos++, cid);
			stmt.setInt(pos++, objectId);
			stmt.executeUpdate();
		} catch (final SQLException e) {
			throw handleSQLException(e);
		} finally {
			DBUtils.closeSQLStuff(null, stmt);
		}
	}

	private static OXException handleSQLException(final SQLException e) {
		return OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
	}
}
