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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tagging;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.publish.Transaction;
import com.openexchange.database.DBPoolingException;
import static com.openexchange.publish.Transaction.INT;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TaggingSQL {

    private final static String TAGGING_TABLE = "tags";

    public static void addTag(Tagged tagged) throws DBPoolingException, SQLException {
        if (tagExists(tagged)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(TAGGING_TABLE);
        sb.append(" (cid, object_id, folder_id, tag) ");
        sb.append("VALUES (?, ?, ?, ?)");

        Transaction.commitStatement(
            tagged.getContextId(),
            sb.toString(),
            tagged.getContextId(),
            tagged.getObjectId(),
            tagged.getFolderId(),
            tagged.getTag());
    }

    public static void deleteTag(Tagged tagged) throws DBPoolingException, SQLException {
        if (!tagExists(tagged)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(TAGGING_TABLE);
        sb.append(" WHERE cid = ? AND object_id = ? AND folder_id = ? AND tag = ?");

        Transaction.commitStatement(
            tagged.getContextId(),
            sb.toString(),
            tagged.getContextId(),
            tagged.getObjectId(),
            tagged.getFolderId(),
            tagged.getTag());
    }

    public static void deleteAllTags(int contextId, int objectId) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(TAGGING_TABLE);
        sb.append(" WHERE cid = ? AND object_id = ?");

        Transaction.commitStatement(contextId, sb.toString(), contextId, objectId);
    }

    public static List<String> getTags(int contextId, int objectId, int folderId) throws DBPoolingException, SQLException {
        List<String> retval = new ArrayList<String>();
        if (!hasTags(contextId, objectId, folderId)) {
            return retval;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(TAGGING_TABLE);
        sb.append(" WHERE cid = ? AND object_id = ? AND folder_id = ?");

        List<Map<String, Object>> tags = Transaction.commitQuery(contextId, sb.toString(), contextId, objectId, folderId);

        for (Map<String, Object> tag : tags) {
            retval.add((String) tag.get("tag"));
        }

        return retval;
    }

    public static List<Tagged> getObjects(int contextId, String tagString) throws DBPoolingException, SQLException {
        List<Tagged> retval = new ArrayList<Tagged>();
        
        SQLStatement statement = new TaggingSQLBuilder().build(tagString);
        
        Object[] args = new Object[statement.getTags().size() + 1];
        args[0] = contextId;
        for(int i = 1; i < args.length; i++) {
            args[i] = statement.getTags().get(i-1);
        }
        
        List<Map<String, Object>> tags = Transaction.commitQuery(contextId, statement.getSQLString(), args);

        for (Map<String, Object> tag : tags) {
            Tagged t = new Tagged();
            t.setContextId(INT(tag.get("cid")));
            t.setFolderId(INT(tag.get("folder_id")));
            t.setObjectId(INT(tag.get("object_id")));
            t.setTag((String) tag.get("tag"));

            retval.add(t);
        }
        return retval;
    }

    private static boolean tagExists(Tagged tagged) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(TAGGING_TABLE);
        sb.append(" WHERE cid = ? AND object_id = ? AND folder_id = ? AND tag = ?");

        List<Map<String, Object>> tags = Transaction.commitQuery(
            tagged.getContextId(),
            sb.toString(),
            tagged.getContextId(),
            tagged.getObjectId(),
            tagged.getFolderId(),
            tagged.getTag());

        return tags.size() > 0;
    }

    private static boolean hasTags(int contextId, int objectId, int folderId) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(TAGGING_TABLE);
        sb.append(" WHERE cid = ? AND object_id = ? AND folder_id = ?");

        List<Map<String, Object>> tags = Transaction.commitQuery(contextId, sb.toString(), contextId, objectId, folderId);

        return tags.size() > 0;
    }

    private static boolean tagExists(int contextId, String tag) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(TAGGING_TABLE);
        sb.append(" WHERE tag = ?");

        List<Map<String, Object>> tags = Transaction.commitQuery(contextId, sb.toString(), tag);

        return tags.size() > 0;
    }
}
