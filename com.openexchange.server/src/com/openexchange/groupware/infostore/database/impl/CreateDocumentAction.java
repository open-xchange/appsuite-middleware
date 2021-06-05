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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.session.Session;

public class CreateDocumentAction extends AbstractDocumentListAction {

    private long timestamp;

    /**
     * Initializes a new {@link CreateDocumentAction}.
     * 
     * @param session The {@link Session}
     */
    public CreateDocumentAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link CreateDocumentAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param documents The documents to create
     * @param session The {@link Session}
     */
    public CreateDocumentAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> documents, Session session) {
        super(provider, queryCatalog, context, documents, session);
        setTimestamp(System.currentTimeMillis());
    }

    @Override
    protected void undoAction() throws OXException {
        final List<String> deleteStmts = getQueryCatalog().getDelete(InfostoreQueryCatalog.Table.INFOSTORE, getDocuments());

        final List<UpdateBlock> updates = new ArrayList<UpdateBlock>(2);

        for (final String deleteStmt : deleteStmts) {
            final UpdateBlock update = new Update(deleteStmt){

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, getContext().getContextId());
                }
            };
            updates.add(update);
        }

        doUpdates(updates);
    }

    @Override
    public void perform() throws OXException {
        doUpdates( getQueryCatalog().getDocumentInsert(), getQueryCatalog().getWritableDocumentFields(), getDocuments());
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return new Object[] { L(getTimestamp()), I(getContext().getContextId()) };
    }

    public void setTimestamp(final long ts) {
        this.timestamp = ts;
        for (DocumentMetadata documentMeta : getDocuments()) {
            documentMeta.setSequenceNumber(ts);
        }
    }

    public long getTimestamp(){
        return this.timestamp;
    }
}
