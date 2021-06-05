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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.session.Session;

public class DeleteAllVersionsAction extends AbstractDocumentListAction {

    /**
     * Initializes a new {@link DeleteAllVersionsAction}.
     *
     * @param session The session
     */
    public DeleteAllVersionsAction(Session session) {
        super(session);
    }

    @Override
    protected void undoAction() throws OXException {
        if (getDocuments().size()==0) {
            return;
        }
        final UpdateBlock[] updates = new UpdateBlock[getDocuments().size()];
        int i = 0;
        for(final DocumentMetadata doc : getDocuments()) {
            updates[i++] = new Update(getQueryCatalog().getVersionInsert()) {

                @Override
                public void fillStatement() throws SQLException {
                    fillStmt(stmt,getQueryCatalog().getWritableVersionFields(),doc,Integer.valueOf(getContext().getContextId()));
                }

            };
        }

        doUpdates(updates);
    }

    @Override
    public void perform() throws OXException {
        if (getDocuments().size()==0) {
            return;
        }
        final UpdateBlock[] updates = new UpdateBlock[1];
        updates[0] = new Update("DELETE FROM infostore_document WHERE cid = ?") {

            @Override
            public void fillStatement() throws SQLException {
                stmt.setInt(1,getContext().getContextId());
            }

        };

        doUpdates(updates);
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return null;
    }

}
