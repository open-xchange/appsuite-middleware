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

package com.openexchange.snippet.mime.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.snippet.ReferenceType;
import com.openexchange.snippet.mime.MimeSnippetManagement;

/**
 * {@link MimeSnippetDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSnippetDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link MimeSnippetDeleteListener}.
     */
    public MimeSnippetDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER != event.getType()) {
            return;
        }
        /*
         * Writable connection
         */
        final int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            final int userId = event.getId();
            final List<String> ids;
            {
                ResultSet rs = null;
                try {
                    stmt = writeCon.prepareStatement("SELECT id FROM snippet WHERE cid = ? AND user = ? AND refType=" + ReferenceType.FILE_STORAGE.getType());
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    rs = stmt.executeQuery();
                    ids = new LinkedList<String>();
                    while (rs.next()) {
                        ids.add(rs.getString(1));
                    }
                } finally {
                    Databases.closeSQLStuff(rs);
                }
            }
            Databases.closeSQLStuff(stmt);
            stmt = null;
            if (ids.isEmpty()) {
                return;
            }
            /*
             * Delete them
             */
            for (final String id : ids) {
                MimeSnippetManagement.deleteSnippet(id, userId, contextId, writeCon);
            }
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
