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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SQL {

    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SQL.class);

    public static final String SELECT_EVENT_STMT =
        "SELECT creating_date,created_from,changing_date,changed_from,fid,pflag,timestampfield01,timestampfield02,timezone," +
            "intfield01,intfield02,intfield03,intfield04,intfield05,intfield06,intfield07,intfield08,field01,field02,field04,field06," +
            "field07,field08,field09,uid,organizer,sequence,organizerId,principal,principalId,filename " +
            "FROM prg_dates WHERE cid=? AND intfield01=?;";

    public static final String SELECT_EVENTS_IN_FOLDER_STMT =
        "SELECT creating_date,created_from,changing_date,changed_from,fid,pflag,timestampfield01,timestampfield02,timezone," +
            "intfield01,intfield02,intfield03,intfield04,intfield05,intfield06,intfield07,intfield08,field01,field02,field04,field06," +
            "field07,field08,field09,uid,organizer,sequence,organizerId,principal,principalId,filename " +
            "FROM prg_dates LEFT JOIN prg_dates_members ON prg_dates.cid=prg_dates_members.cid " +
            "AND prg_dates.intfield01=prg_dates_members.object_id " +
            "WHERE prg_dates.cid=? AND prg_dates.timestampfield01<=? AND prg_dates.timestampfield02>=? " +
            "AND (prg_dates.fid=? OR prg_dates_members.pfid=?);";

    public static final String SELECT_EVENTS_OF_USER_STMT =
        "SELECT creating_date,created_from,changing_date,changed_from,fid,pflag,timestampfield01,timestampfield02,timezone," +
            "intfield01,intfield02,intfield03,intfield04,intfield05,intfield06,intfield07,intfield08,field01,field02,field04,field06," +
            "field07,field08,field09,uid,organizer,sequence,organizerId,principal,principalId,filename, " +
            "m.pfid " +
            "FROM prg_dates AS d LEFT JOIN prg_dates_members AS m ON d.cid=m.cid AND d.intfield01=m.object_id " +
            "WHERE d.cid=? AND d.timestampfield01<=? AND d.timestampfield02>=? AND m.member_uid=?;";

    public static final String SELECT_ALARMS_STMT =
        "SELECT r.alarm,r.recurrence,r.description,m.reminder FROM reminder AS r LEFT JOIN prg_dates_members AS m ";

    public static final String SELECT_EXTERNAL_ATTENDEES_STMT =
        "SELECT mailAddress,displayName,confirm,reason FROM dateexternal WHERE cid=? AND objectId=?;";

    public static final String SELECT_INTERNAL_ATTENDEES_STMT =
        "SELECT r.id,r.type,r.ma,r.dn,m.confirm,m.reason FROM prg_date_rights AS r LEFT JOIN prg_dates_members AS m " +
            "ON r.cid=m.cid AND r.object_id=m.object_id AND r.id=m.member_uid WHERE r.cid=? AND r.object_id=?;";

    //    public static final String INSERT_EVENT_STMT =
    //        "INSERT INTO prg_dates (creating_date,created_from,changing_date,changed_from,fid,pflag,cid,timestampfield01,timestampfield02," +
    //        "timezone,intfield01,intfield02,intfield03,intfield04,intfield05,intfield06,intfield07,intfield08,field01,field02,field04," +
    //        "field06,field07,field08,field09,uid,organizer,sequence,organizerId,principal,principalId,filename) VALUES ;";

    public static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", stmt.toString(), (System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    public static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        }
    }

    /**
     * Appends a SQL clause for the given number of placeholders, i.e. either <code>=?</code> if <code>count</code> is <code>1</code>, or
     * an <code>IN</code> clause like <code>IN (?,?,?,?)</code> in case <code>count</code> is greater than <code>1</code>.
     *
     * @param stringBuilder The string builder to append the clause
     * @param count The number of placeholders to append
     * @return The string builder
     */
    public static StringBuilder appendPlaceholders(StringBuilder stringBuilder, int count) {
        if (0 >= count) {
            throw new IllegalArgumentException("count");
        }
        if (1 == count) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < count; i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        return stringBuilder;
    }

    private SQL() {
        super();
    }

}
