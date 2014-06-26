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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DBChatDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DBChatDeleteListener implements DeleteListener {

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            deleteUserEntriesFromDB(event, writeCon);
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            deleteContextEntriesFromDB(event, writeCon);
        } else {
            return;
        }
    }

    private void deleteContextEntriesFromDB(final DeleteEvent event, final Connection writeCon) throws OXException {
        final int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            final int pos = 1;
            stmt = writeCon.prepareStatement("DELETE FROM chat WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);

            stmt = writeCon.prepareStatement("DELETE FROM chatMember WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);

            stmt = writeCon.prepareStatement("DELETE FROM chatMessage WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);

            stmt = writeCon.prepareStatement("DELETE FROM chatMessageMap WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);

            stmt = writeCon.prepareStatement("DELETE FROM chatChunk WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);

            stmt = writeCon.prepareStatement("DELETE FROM chatPresence WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private void deleteUserEntriesFromDB(final DeleteEvent event, final Connection writeCon) throws OXException {
        final int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            int pos;
            final int userId = event.getId();
            final int admin = event.getContext().getMailadmin();
            /*
             * Delete in case of administrator
             */
            if (userId == admin) {
                deleteContextEntriesFromDB(event, writeCon);
                return;
            }
            /*
             * Determine single chats. Those chats with number of members less than 3 that associated user participates with
             */
            {
                final TIntList singleChatIds = new TIntLinkedList();
                {
                    ResultSet rs = null;
                    try {
                        stmt =
                            writeCon.prepareStatement("SELECT cm1.chatId FROM chatMember AS cm1 WHERE cid = ? AND (SELECT COUNT(cm2.user) FROM chatMember AS cm2 WHERE cm2.cid = ? AND cm2.chatId = cm1.chatId) < ? AND cm1.user = ? GROUP BY cm1.chatId");
                        pos = 1;
                        stmt.setInt(pos++, contextId);
                        stmt.setInt(pos++, contextId);
                        stmt.setInt(pos++, 3); // Less than 3
                        stmt.setInt(pos, userId);
                        rs = stmt.executeQuery();
                        while (rs.next()) {
                            singleChatIds.add(rs.getInt(1));
                        }
                    } catch (final SQLException e) {
                        throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
                    } finally {
                        closeSQLStuff(rs, stmt);
                    }
                }
                /*
                 * Delete single chats' stuff
                 */
                stmt = writeCon.prepareStatement("DELETE FROM chat WHERE cid = ? AND chatId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                for (final TIntIterator it = singleChatIds.iterator(); it.hasNext();) {
                    stmt.setInt(pos, it.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(stmt);
                stmt = writeCon.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND chatId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                for (final TIntIterator it = singleChatIds.iterator(); it.hasNext();) {
                    stmt.setInt(pos, it.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(stmt);
                stmt = writeCon.prepareStatement("DELETE FROM chatMessage WHERE cid = ? AND chatId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                for (final TIntIterator it = singleChatIds.iterator(); it.hasNext();) {
                    stmt.setInt(pos, it.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(stmt);
                stmt = writeCon.prepareStatement("DELETE FROM chatChunk WHERE cid = ? AND chatId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                for (final TIntIterator it = singleChatIds.iterator(); it.hasNext();) {
                    stmt.setInt(pos, it.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(stmt);
                stmt = writeCon.prepareStatement("DELETE FROM chatMessageMap WHERE cid = ? AND chatId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                for (final TIntIterator it = singleChatIds.iterator(); it.hasNext();) {
                    stmt.setInt(pos, it.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Update ownership
             */
            stmt = writeCon.prepareStatement("UPDATE chat SET user = ? WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, admin);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            /*
             * Delete member entries
             */
            stmt = writeCon.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            /*
             * Delete messages and their mappings
             */
            stmt = writeCon.prepareStatement("DELETE FROM chatMessageMap WHERE cid = ? AND messageId IN (SELECT messageId FROM chatMessage WHERE cid = ? AND user = ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = writeCon.prepareStatement("DELETE FROM chatMessage WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            /*
             * Delete presence entry
             */
            stmt = writeCon.prepareStatement("DELETE FROM chatPresence WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
