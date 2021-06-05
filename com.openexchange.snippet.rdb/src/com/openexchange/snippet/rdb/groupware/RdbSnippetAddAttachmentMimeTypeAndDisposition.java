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

package com.openexchange.snippet.rdb.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link RdbSnippetAddAttachmentMimeTypeAndDisposition}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbSnippetAddAttachmentMimeTypeAndDisposition extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link RdbSnippetAddAttachmentMimeTypeAndDisposition}.
     */
    public RdbSnippetAddAttachmentMimeTypeAndDisposition() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        byte rollback = 0;
        try {
            boolean mimeTypeExists = Tools.columnExists(con, "snippetAttachment", "mimeType");
            boolean dispositionExists = Tools.columnExists(con, "snippetAttachment", "disposition");
            if (mimeTypeExists && dispositionExists) {
                return;
            }

            Databases.startTransaction(con);
            rollback = 1;

            // https://stackoverflow.com/questions/7599519/alter-table-add-column-takes-a-long-time
            Column columnMimeType = new Column("mimeType", "VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL");
            Column columnDisposition = new Column("disposition", "VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL");
            Tools.checkAndAddColumns(con, "snippetAttachment", columnMimeType, columnDisposition);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { RdbSnippetFixAttachmentPrimaryKey.class.getName() };
    }

}
