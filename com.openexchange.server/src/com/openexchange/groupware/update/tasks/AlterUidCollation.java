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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;

/**
 * {@link AlterUidCollation} Alters table login2user and changes the collation of column uid to utf8_bin.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AlterUidCollation implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AlterUidCollation.class));

    private static final String SQL = "ALTER TABLE login2user MODIFY uid VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL";

    public AlterUidCollation() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 60;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(Schema schema, int contextId) throws OXException {
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            if (doUmlautsWork(con)) {
                LOG.info("Collation allows inserting '\u00e4\u00f6\u00fc' and 'aou'.");
                return;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        LOG.info("Changing collation to allow inserting '\u00e4\u00f6\u00fc' and 'aou'.");
        Statement stmt = null;
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.execute(SQL);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private static final String USER = "INSERT INTO user (cid,id,mail,mailEnabled,preferredLanguage,shadowLastChange,timeZone,contactId,passwordMech,uidNumber,gidNumber,homeDirectory,loginShell) VALUES (2147483647,2147483647,'',false,'',0,'',0,'',0,0,'','')";

    private static final String UMLAUTS = "INSERT INTO login2user (cid,id,uid) VALUES (2147483647,2147483647,'\u00e4\u00f6\u00fc')";

    private static final String DUPLICATE = "INSERT INTO login2user (cid,id,uid) VALUES (2147483647,2147483647,'aou')";

    private boolean doUmlautsWork(Connection con) throws SQLException {
        boolean retval = true;
        Statement stmt = null;
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.execute(USER);
            stmt.execute(UMLAUTS);
            try {
                stmt.execute(DUPLICATE);
            } catch (SQLException e) {
                retval = false;
            }
        } finally {
            closeSQLStuff(stmt);
            rollback(con);
            autocommit(con);
        }
        return retval;
    }
}
