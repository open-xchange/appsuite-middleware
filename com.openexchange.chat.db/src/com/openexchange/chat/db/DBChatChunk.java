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

package com.openexchange.chat.db;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.chat.ChatChunk;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;


/**
 * {@link DBChatChunk}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DBChatChunk implements ChatChunk {

    private final int chunkId;
    private final int chatId;
    private final int contextId;
    private final String sChunkId;
    private final String sChatId;
    private final String sContextId;
    private final long createdAt;

    /**
     * Initializes a new {@link DBChatChunk}.
     */
    public DBChatChunk(final int chunkId, final int chatId, final int contextId) {
        super();
        this.chunkId = chunkId;
        this.chatId = chatId;
        this.contextId = contextId;
        this.sChunkId = Integer.toString(chunkId);
        this.sChatId = Integer.toString(chatId);
        this.sContextId = Integer.toString(contextId);
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public String getChunkId() {
        return sChunkId;
    }

    @Override
    public String getChatId() {
        return sChatId;
    }

    public String getContextId() {
        return sContextId;
    }

    @Override
    public List<String> getMembers() throws OXException {
        final DatabaseService databaseService = DBChatServiceLookup.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT user FROM chatMember cm, chatChunk cc LEFT JOIN ON cid = ? AND chatId = ? WHERE chunkId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            stmt.setInt(3, chunkId);
            rs = stmt.executeQuery();
            final List<String> ret = new LinkedList<String>();
            while (rs.next()) {
                ret.add(Integer.toString(rs.getInt(1)));
            }
            return ret;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public Date getTimeStamp() {
        return new Date(createdAt);
    }

}
