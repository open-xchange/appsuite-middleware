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

package com.openexchange.groupware.ldap;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.ContextDelete;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;


/**
 * {@link UserContextDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class UserContextDeleteListener extends ContextDelete {

    /**
     * Initializes a new {@link UserContextDeleteListener}.
     */
    public UserContextDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (isContextDelete(event)) {
            handleContextDeletion(writeCon, event.getContext());
        }
    }

    private void handleContextDeletion(Connection writeCon, Context ctx) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();

            StringBuilder sb = new StringBuilder(48).append("DELETE FROM ");
            int contextId = ctx.getContextId();

            stmt.addBatch(craftStatement("login2user", contextId, sb));
            stmt.addBatch(craftStatement("user_setting_server", contextId, sb));
            stmt.addBatch(craftStatement("user_setting_mail", contextId, sb));
            stmt.addBatch(craftStatement("user_setting_admin", contextId, sb));
            stmt.addBatch(craftStatement("user_configuration", contextId, sb));
            stmt.addBatch(craftStatement("user_alias", contextId, sb));
            stmt.addBatch(craftStatement("user_attribute", contextId, sb));
            stmt.addBatch(craftStatement("user", contextId, sb));

            stmt.executeBatch();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private String craftStatement(String tableName, int contextId, StringBuilder sb) {
        int reslen = sb.length();
        String stmt = sb.append(tableName).append(" WHERE cid=").append(contextId).toString();
        sb.setLength(reslen);
        return stmt;
    }

}
