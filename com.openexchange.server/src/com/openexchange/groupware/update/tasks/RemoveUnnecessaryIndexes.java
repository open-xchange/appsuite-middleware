/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.groupware.update.SimpleUpdateTask;
import com.openexchange.tools.update.Tools;

/**
 * {@link RemoveUnnecessaryIndexes} - Removes unnecessary indexes from certain tables (see Bug #21882).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoveUnnecessaryIndexes extends SimpleUpdateTask {

    public RemoveUnnecessaryIndexes() {
        super();
    }

    @Override
    protected void perform(final Connection con) throws SQLException {
        String tableName = "virtualBackupPermission";
        String indexName = existsIndex(con, tableName, new String[] { "cid", "tree", "user", "folderId" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }

        tableName = "virtualPermission";
        indexName = existsIndex(con, tableName, new String[] { "cid", "tree", "user", "folderId" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }

        tableName = "reminder";
        indexName = existsIndex(con, tableName, new String[] { "cid", "target_id" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }

        tableName = "chatMessageMap";
        indexName = existsIndex(con, tableName, new String[] { "cid", "chatId" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }

        tableName = "chatMessage";
        indexName = existsIndex(con, tableName, new String[] { "cid", "user" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }

        tableName = "chatMember";
        indexName = existsIndex(con, tableName, new String[] { "cid", "user", "chatId" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }

        tableName = "chatChunk";
        indexName = existsIndex(con, tableName, new String[] { "cid", "chatId" });
        if (null != indexName) {
            Tools.dropIndex(con, tableName, indexName);
        }
    }

}
