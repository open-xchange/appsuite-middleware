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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.chat.db.groupware;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.update.Tools.columnExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.db.DBChatServiceLookup;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.ServiceExceptionCodes;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;


/**
 * {@link DBChatAlterTableTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DBChatAlterTableTask extends UpdateTaskAdapter {

    private static final String TABLE_CHAT_MEMBER = "chatMember";
    private static final String TABLE_CHAT_MESSAGE = "chatMessage";
    private static final String TABLE_CHAT_CHUNK = "chatChunk";

    private static final String ALTER_CHAT_MEMBER_1 = "ALTER TABLE "+TABLE_CHAT_MEMBER +" ADD chunkId INT4 UNSIGNED NOT NULL";

    private static final String ALTER_CHAT_MEMBER_2 = "ALTER TABLE "+TABLE_CHAT_MEMBER+" ADD lastPoll BIGINT(64) DEFAULT NULL";

    private static final String ALTER_CHAT_MESSAGE = "ALTER TABLE "+TABLE_CHAT_MESSAGE+" ADD chunkId INT4 UNSIGNED NOT NULL";

    private static final String CREATE_CHAT_CHUNK = "CREATE TABLE "+TABLE_CHAT_CHUNK+" (\n" +
        " cid INT4 unsigned NOT NULL,\n" +
        " chatId INT4 unsigned NOT NULL,\n" +
        " chunkId INT4 unsigned NOT NULL,\n" +
        " -- user INT4 unsigned NOT NULL,\n" +
        " createdAt BIGINT(64) DEFAULT NULL,\n" +
        " PRIMARY KEY (cid, chatId, chunkId),\n" +
        " INDEX `chat` (cid, chatId)\n" +
        " -- INDEX `userMessage` (cid, user, chatId)\n" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    
    /**
     * Initializes a new {@link DBChatAlterTableTask}.
     */
    public DBChatAlterTableTask() {
        super();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final DatabaseService databaseService = DBChatServiceLookup.getService(DatabaseService.class);
        if (databaseService == null) {
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final int contextId = params.getContextId();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            alterChatMember(con);
            alterChatMessage(con);
            alterChatChunk(con);
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            DBUtils.rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[]{DBChatCreateTableTask.class.getName()};
    }

    private void alterChatMember(final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (DBUtils.tableExists(con, TABLE_CHAT_MEMBER)) {
                if (!columnExists(con, TABLE_CHAT_MEMBER, "chunkId")) {
                    stmt = con.prepareStatement(ALTER_CHAT_MEMBER_1);
                    stmt.execute();
                    stmt.close();
                    if (Tools.hasPrimaryKey(con, TABLE_CHAT_MEMBER)) {
                        Tools.dropPrimaryKey(con, TABLE_CHAT_MEMBER);
                    }
                    Tools.createPrimaryKey(con, TABLE_CHAT_MEMBER, new String[]{"cid", "user", "chatId", "chunkId"});
                }
                if (!columnExists(con, TABLE_CHAT_MEMBER, "lastPoll")) {
                    if (stmt != null) {
                        closeSQLStuff(stmt);
                    }
                    stmt = con.prepareStatement(ALTER_CHAT_MEMBER_2);
                    stmt.execute();
                }
            } else {
                stmt = con.prepareStatement(DBChatCreateTableService.getCreateStmts()[1]);
                stmt.execute();
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void alterChatMessage(final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (DBUtils.tableExists(con, TABLE_CHAT_MESSAGE)) {
                if (!columnExists(con, TABLE_CHAT_MESSAGE, "chunkId")) {
                    stmt = con.prepareStatement(ALTER_CHAT_MESSAGE);
                    stmt.execute();
                    if (Tools.existsIndex(con, TABLE_CHAT_MESSAGE, new String[]{"cid", "chatId", "chunkId"}) == null) {
                        Tools.createIndex(con, TABLE_CHAT_MESSAGE, "chunkMessage", new String[]{"cid", "chatId", "chunkId"}, false);
                    }
                    if (Tools.existsIndex(con, TABLE_CHAT_MESSAGE, new String[] { "cid", "user", "chatId"}) == null) {
                        Tools.createIndex(con, TABLE_CHAT_MESSAGE, "userMessage", new String[] {"cid", "user", "chatId"}, false);
                    }
                }
            } else {
                stmt = con.prepareStatement(DBChatCreateTableService.getCreateStmts()[2]);
                stmt.execute();
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void alterChatChunk(final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (DBUtils.tableExists(con, TABLE_CHAT_CHUNK)) {
                return;
            } else {
                stmt = con.prepareStatement(CREATE_CHAT_CHUNK);
                stmt.execute();
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
