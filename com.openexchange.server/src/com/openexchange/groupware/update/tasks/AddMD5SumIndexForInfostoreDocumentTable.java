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

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddMD5SumIndexForInfostoreDocumentTable} - Extends infostore document tables by the <code>(`cid`, `file_md5sum`)</code> index.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddMD5SumIndexForInfostoreDocumentTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddMD5SumIndexForInfostoreDocumentTable}.
     */
    public AddMD5SumIndexForInfostoreDocumentTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            startTransaction(con);
            rollback = 1;
            if (null == Tools.existsIndex(con, "infostore_document", new String[] { "cid", "file_md5sum"})) {
                Tools.createIndex(con, "infostore_document", "md5sumIndex", new String[] { "cid", "file_md5sum"}, false);
            }
            if (null == Tools.existsIndex(con, "del_infostore_document", new String[] { "cid", "file_md5sum"})) {
                Tools.createIndex(con, "del_infostore_document", "md5sumIndex", new String[] { "cid", "file_md5sum"}, false);
            }
            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddMetaForInfostoreDocumentTable.class.getName() };
    }
}
