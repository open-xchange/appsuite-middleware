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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.tools.sql.DBUtils;

/**
 * Build a subclass of {@link SimpleStatementsUpdateTask} if all you want to do is execute a bunch of statements.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class SimpleStatementsUpdateTask extends SimpleUpdateTask {

    private final List<StatementHolder> statements = new ArrayList<StatementHolder>();

    public SimpleStatementsUpdateTask() {
        statements();
    }

    /**
     * Define Statements with {@link #add(String, Object...)} here.
     */
    protected abstract void statements();

    /**
     * Add a statement to be executed. The ? in the statement will be filled with objects from the values array
     */
    public void add(String statement, Object...values) {
        statements.add(new StatementHolder(statement, values));
    }

    @Override
    protected final void perform(Connection con) throws SQLException {
        for (StatementHolder sqlStatement : statements) {
            sqlStatement.execute(con);
        }
    }

    protected static class StatementHolder {
        private final String statement;
        private final Object[] values;

        public StatementHolder(String statement, Object...values) {
            this.statement = statement;
            this.values = values;
        }

        public void execute(Connection con) throws SQLException {
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(statement);
                for(int i = 0; i < values.length; i++) {
                    stmt.setObject(i+1, values[i]);
                }
                stmt.execute();
            } finally {
                if (stmt != null) {
                    DBUtils.closeSQLStuff(stmt);
                }
            }
        }
    }
}
