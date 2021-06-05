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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.session.Session;

public class DeleteDocumentAction extends AbstractDocumentListAction {

    private static final int batchSize = 1000;

    /**
     * Initializes a new {@link DeleteDocumentAction}.
     */
    public DeleteDocumentAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link DeleteDocumentAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param document The document to delete
     */
    public DeleteDocumentAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, DocumentMetadata document, Session session) {
        this(provider, queryCatalog, context, Collections.singletonList(document), session);
    }

    /**
     * Initializes a new {@link DeleteDocumentAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param documents The documents to delete
     */
    public DeleteDocumentAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> documents, Session session) {
        super(provider, queryCatalog, context, documents, session);
    }

    @Override
    protected void undoAction() throws OXException {
        if (getDocuments().isEmpty()) {
            return;
        }
        final UpdateBlock[] updates = new UpdateBlock[getDocuments().size()];
        int i = 0;
        for(final DocumentMetadata doc : getDocuments()) {
            updates[i++] = new Update(getQueryCatalog().getDocumentInsert()) {

                @Override
                public void fillStatement() throws SQLException {
                    fillStmt(stmt,getQueryCatalog().getWritableDocumentFields(), doc, Long.valueOf(System.currentTimeMillis()), Integer.valueOf(getContext().getContextId()));
                }

            };
        }

        doUpdates(updates);
    }

    @Override
    public void perform() throws OXException {
        if (getDocuments().isEmpty()) {
            return;
        }

        final List<DocumentMetadata> documents = getDocuments();
        final List<DocumentMetadata>[] slices = getSlices(batchSize, documents);

        final List<UpdateBlock> updates = new ArrayList<UpdateBlock>(slices.length << 1);

        for(int j = 0, size = slices.length; j < size; j++) {
            final List<String> deleteStmts = getQueryCatalog().getDelete(InfostoreQueryCatalog.Table.INFOSTORE, slices[j], false);
            for (final String deleteStmt : deleteStmts) {
                updates.add(new Update(deleteStmt){

                    @Override
                    public void fillStatement() throws SQLException {
                        stmt.setInt(1, getContext().getContextId());
                    }

                });
            }
        }

        doUpdates(updates);
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return null;
    }

}
