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
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog.Table;
import com.openexchange.session.Session;

/**
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ReplaceDocumentIntoDelTableAction extends AbstractDocumentListAction {

    private static final int batchSize = 100;

    /**
     * Initializes a new {@link ReplaceDocumentIntoDelTableAction}.
     */
    public ReplaceDocumentIntoDelTableAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link ReplaceDocumentIntoDelTableAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param document The document to replace in the backup tables
     */
    public ReplaceDocumentIntoDelTableAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, DocumentMetadata document, Session session) {
        this(provider, queryCatalog, context, Collections.singletonList(document), session);
    }

    /**
     * Initializes a new {@link ReplaceDocumentIntoDelTableAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param documents The documents to replace in the backup tables
     */
    public ReplaceDocumentIntoDelTableAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> documents, Session session) {
        super(provider, queryCatalog, context, documents, session);
    }

    @Override
    protected Object[] getAdditionals(DocumentMetadata doc) {
        return new Object[0];
    }

    @Override
    public void perform() throws OXException {
        /*
         * replace entries in the del_infostore table only as there's no valuable information in the del_infostore_document table anymore
         */
        List<DocumentMetadata> documents = getDocuments();
        if (null == documents || 0 == documents.size()) {
            return;
        }
        final Integer contextID = Integer.valueOf(getContext().getContextId());
        List<DocumentMetadata>[] slices = getSlices(batchSize, documents);
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>(slices.length);
        for (int i = 0; i < slices.length; i++) {
            final List<DocumentMetadata> slice = slices[i];
            /*
             * REPLACE INTO del_infostore (...) VALUES (...);
             */
            updates.add(new Update(getQueryCatalog().getReplace(Table.DEL_INFOSTORE, slice.size(), "last_modified", "cid")) {

                @Override
                public void fillStatement() throws SQLException {
                    int parameterIndex = 1;
                    Long sequenceNumber = Long.valueOf(System.currentTimeMillis());
                    for (DocumentMetadata document : slice) {
                        document.setSequenceNumber(sequenceNumber);
                        parameterIndex = fillStmt(parameterIndex, stmt, getQueryCatalog().getWritableDelDocumentFields(), document, sequenceNumber, contextID);
                    }
                }
            });
        }
        /*
         * perform updates
         */
        doUpdates(updates);
    }

    @Override
    protected void undoAction() throws OXException {
        /*
         * clean up the del_infostore table again
         */
        List<DocumentMetadata> documents = getDocuments();
        if (null == documents || 0 == documents.size()) {
            return;
        }
        List<DocumentMetadata>[] slices = getSlices(batchSize, documents);
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>(slices.length << 1);
        for (int i = 0; i < slices.length; i++) {
            /*
             * DELETE FROM del_infostore WHERE id IN (...) AND cid=...;
             */
            List<String> deleteStmts = getQueryCatalog().getDelete(InfostoreQueryCatalog.Table.DEL_INFOSTORE, slices[i], false);
            for (String deleteStmt : deleteStmts) {
                updates.add(new Update(deleteStmt) {

                    @Override
                    public void fillStatement() throws SQLException {
                        stmt.setInt(1, getContext().getContextId());
                    }
                });
            }
        }
        /*
         * perform updates
         */
        doUpdates(updates);
    }

}
