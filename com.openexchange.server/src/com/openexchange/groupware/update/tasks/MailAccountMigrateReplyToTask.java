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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MailAccountMigrateReplyToTask} - Migrate "replyTo" information from properties table to account tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountMigrateReplyToTask extends UpdateTaskAdapter {

    public MailAccountMigrateReplyToTask() {
        super();
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection con = Database.getNoTimeout(contextId, true);
        boolean committed = false;
        try {
            con.setAutoCommit(false); // BEGIN
            process("user_mail_account", con);
            process("user_transport_account", con);
            con.commit(); // COMMIT
            committed = true;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (!committed) {
                DBUtils.rollback(con);
            }
            DBUtils.autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void process(final String tableName, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, id, value FROM "+tableName+"_properties WHERE name = ?");
            stmt.setString(1, "replyto");
            rs = stmt.executeQuery();
            if (rs.next()) {
                class Prop {

                    int context;
                    int user;
                    int id;
                    String value;

                    Prop(final ResultSet rs) throws SQLException {
                        super();
                        this.context = rs.getInt(1);
                        this.user = rs.getInt(2);
                        this.id = rs.getInt(3);
                        this.value = rs.getString(4);
                    }
                } // End of class Prop
                final List<Prop> props = new LinkedList<Prop>();
                do {
                    props.add(new Prop(rs));
                } while (rs.next());
                closeSQLStuff(rs, stmt);
                rs = null;

                stmt = con.prepareStatement("UPDATE "+tableName+" SET replyTo = ? WHERE cid = ? AND user = ? AND id = ?");
                int pos;
                for (final Prop prop : props) {
                    pos = 1;
                    stmt.setString(pos++, prop.value);
                    stmt.setInt(pos++, prop.context);
                    stmt.setInt(pos++, prop.user);
                    stmt.setInt(pos, prop.id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }
}
