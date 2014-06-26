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

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link DBChatCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatCreateTableService extends AbstractCreateTableImpl {

    private static final String TABLE_CHAT = "chat";
    private static final String TABLE_CHAT_MEMBER = "chatMember";
    private static final String TABLE_CHAT_MESSAGE = "chatMessage";
    private static final String TABLE_CHAT_MESSAGE_MAP = "chatMessageMap";
    private static final String TABLE_CHAT_PRESENCE = "chatPresence";
    private static final String TABLE_CHAT_CHUNK = "chatChunk";

    private static final String CREATE_CHAT = "CREATE TABLE "+TABLE_CHAT+" (\n" +
    		" cid INT4 unsigned NOT NULL,\n" +
    		" user INT4 unsigned NOT NULL,\n" +
    		" chatId INT4 unsigned NOT NULL,\n" +
    		" subject VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,\n" +
    		" createdAt BIGINT(64) DEFAULT NULL,\n" +
    		" PRIMARY KEY (cid, chatId),\n" +
    		" INDEX `user` (cid, user)\n" +
    		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String CREATE_CHAT_MEMBER = "CREATE TABLE "+TABLE_CHAT_MEMBER+" (\n" +
    		" cid INT4 unsigned NOT NULL,\n" +
    		" user INT4 unsigned NOT NULL,\n" +
    		" chatId INT4 unsigned NOT NULL,\n" +
    		" chunkId INT4 unsigned NOT NULL,\n" +
    		" opMode INT4 unsigned NOT NULL,\n" +
    		" lastPoll BIGINT(64) DEFAULT NULL,\n" +
    		" PRIMARY KEY (cid, user, chatId, chunkId),\n" +
    		" INDEX `user` (cid, user, chatId)\n" +
    		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String CREATE_CHAT_MESSAGE = "CREATE TABLE "+TABLE_CHAT_MESSAGE+" (\n" +
    		" cid INT4 unsigned NOT NULL,\n" +
    		" user INT4 unsigned NOT NULL,\n" +
    		" chatId INT4 unsigned NOT NULL,\n" +
    		" chunkId INT4 unsigned NOT NULL,\n" +
    		" messageId BINARY(16) NOT NULL,\n" +
    		" message TEXT NOT NULL,\n" +
    		" createdAt BIGINT(64) DEFAULT NULL,\n" +
    		" PRIMARY KEY (cid, chatId, messageId),\n" +
    		" INDEX `user` (cid, user),\n" +
    		" INDEX `userMessage` (cid, user, chatId),\n" +
    		" INDEX `chunkMessage` (cid, chatId, chunkId)\n" +
    		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String CREATE_CHAT_MESSAGE_MAP = "CREATE TABLE "+TABLE_CHAT_MESSAGE_MAP+" (\n" +
    		" cid INT4 unsigned NOT NULL,\n" +
    		" chatId INT4 unsigned NOT NULL,\n" +
    		" customId VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,\n" +
    		" messageId BINARY(16) NOT NULL,\n" +
    		" PRIMARY KEY (cid, chatId, messageId),\n" +
    		" INDEX `chat` (cid, chatId)\n" +
    		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String CREATE_CHAT_PRESENCE = "CREATE TABLE "+TABLE_CHAT_PRESENCE+" (\n" +
    		" cid INT4 unsigned NOT NULL,\n" +
    		" user INT4 unsigned NOT NULL,\n" +
    		" type INT4 unsigned NOT NULL DEFAULT 0, -- AVAILABLE\n" +
    		" mode INT4 unsigned NOT NULL DEFAULT 1, -- AVAILABLE\n" +
    		" statusMessage VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,\n" +
    		" lastModified BINARY(16) NOT NULL,\n" +
    		" PRIMARY KEY (cid, user),\n" +
    		" INDEX `available` (cid, type)\n" +
    		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String CREATE_CHAT_CHUNK = "CREATE TABLE "+TABLE_CHAT_CHUNK+"(\n" +
    		" cid INT4 unsigned NOT NULL,\n" +
            " chatId INT4 unsigned NOT NULL,\n" +
    		" chunkId INT4 unsigned NOT NULL,\n" +
    		" createdAt BIGINT(64) DEFAULT NULL,\n" +
    		" PRIMARY KEY (cid, chatId, chunkId),\n" +
    		" INDEX `chat` (cid, chatId)\n" +
    		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { TABLE_CHAT, TABLE_CHAT_MEMBER, TABLE_CHAT_MESSAGE, TABLE_CHAT_MESSAGE_MAP, TABLE_CHAT_PRESENCE, TABLE_CHAT_CHUNK };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { CREATE_CHAT, CREATE_CHAT_MEMBER, CREATE_CHAT_MESSAGE, CREATE_CHAT_MESSAGE_MAP, CREATE_CHAT_PRESENCE, CREATE_CHAT_CHUNK};
    }

    /**
     * Initializes a new {@link DBChatCreateTableService}.
     */
    public DBChatCreateTableService() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return getTablesToCreate();
    }

    @Override
    protected String[] getCreateStatements() {
        return getCreateStmts();
    }

}
