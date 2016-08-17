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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Tools} - Utility methods for calendaring.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    private static final Map<String, TimeZone> zoneCache = new ConcurrentHashMap<String, TimeZone>();

    private static CalendarCollectionService calColl = new CalendarCollection();

    /**
     * Prevent instantiation.
     */
    private Tools() {
        super();
    }

    /**
     * Formats specified date's time millis into a date string.<br>
     * e.g.: <code>&quot;Jan 13, 2009&quot;</code>
     *
     * @param timeMillis The date's time millis to format
     * @return The date string.
     */
    public static String getUTCDateFormat(final long timeMillis) {
        return getUTCDateFormat(new Date(timeMillis));
    }

    /**
     * Formats specified date into a date string.<br>
     * e.g.: <code>&quot;Jan 13, 2009&quot;</code>
     *
     * @param date The date to format
     * @return The date string.
     */
    public static String getUTCDateFormat(final Date date) {
        return DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH).format(date);
    }

    public static Context getContext(final Session so) throws OXException {
        return ContextStorage.getInstance().getContext(so.getContextId());
    }

    public static User getUser(final Session so, final Context ctx) throws OXException {
        return UserStorage.getInstance().getUser(so.getUserId(), ctx);
    }

    public static UserConfiguration getUserConfiguration(final Context ctx, final int userId) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, ctx);
    }

    public static UserPermissionBits getUserPermissionBits(final Context ctx, final int userId) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    public static TimeZone getTimeZone(final String ID) {
        TimeZone zone = zoneCache.get(ID);
        if (zone == null) {
            zone = TimeZone.getTimeZone(ID);
            zoneCache.put(ID, zone);
        }
        return zone;
    }

    private static final String SQL_TITLE = "SELECT " + calColl.getFieldName(Appointment.TITLE) + " FROM prg_dates AS pd WHERE cid = ? AND " + calColl.getFieldName(Appointment.OBJECT_ID) + " = ?";

    /**
     * Gets the appointment's title associated with given object ID in given context.
     *
     * @param objectId The object ID
     * @param ctx The context
     * @return The appointment's title or <code>null</code>
     * @throws OXException If determining appointment's title fails
     */
    public static String getAppointmentTitle(final int objectId, final Context ctx) throws OXException {
        final Connection con = Database.get(ctx, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_TITLE);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, objectId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        } finally {
            DBUtils.closeResources(rs, stmt, con, true, ctx);
        }
    }

    private static final String SQL_FOLDER1 = "SELECT " + calColl.getFieldName(Appointment.FOLDER_ID) + " FROM prg_dates WHERE cid = ? AND " + calColl.getFieldName(Appointment.OBJECT_ID) + " = ?";

    private static final String SQL_FOLDER2 = "SELECT pfid FROM prg_dates_members WHERE cid = ? AND object_id = ? AND member_uid = ?";

    /**
     * Gets the appointment's folder associated with given object ID in given context.
     *
     * @param objectId The object ID
     * @param userId The session user
     * @param ctx The context
     * @return The appointment's folder associated with given object ID in given context.
     * @throws OXException If determining appointment's folder fails
     */
    public static int getAppointmentFolder(final int objectId, final int userId, final Context ctx) throws OXException {
        final Connection con = Database.get(ctx, false);
        int folderId;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_FOLDER1);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, objectId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OXException.notFound("");
            }
            folderId = rs.getInt(1);
            if (folderId <= 0) {
                DBUtils.closeSQLStuff(rs, stmt);
                /*
                 * Determine user's private folder which holds the appointment
                 */
                stmt = con.prepareStatement(SQL_FOLDER2);
                pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, objectId);
                stmt.setInt(pos++, userId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw OXException.notFound("");
                }
                folderId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        } finally {
            DBUtils.closeResources(rs, stmt, con, true, ctx);
        }
        return folderId;
    }

    /**
     * Generates a SQL IN string.
     */
    public static String getSqlInString(final TIntSet arr) {
        if (arr == null) {
            return null;
        }
        final int length = arr.size();
        if (length <= 0) {
            return null;
        }
        final TIntIterator iter = arr.iterator();
        final StringBuilder sb = new StringBuilder(length << 2);
        sb.append('(');
        sb.append(iter.next());
        for (int a = 1; a < length; a++) {
            sb.append(',');
            sb.append(iter.next());
        }
        sb.append(')');
        return sb.toString();
    }

}
