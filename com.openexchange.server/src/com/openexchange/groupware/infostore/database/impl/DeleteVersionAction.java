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

public class DeleteVersionAction extends AbstractDocumentListAction {

    /**
     * Initializes a new {@link DeleteVersionAction}.
     */
    public DeleteVersionAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link DeleteVersionAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param version The version to delete
     */
    public DeleteVersionAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, DocumentMetadata version, Session session) {
        this(provider, queryCatalog, context, Collections.singletonList(version), session);
    }

    /**
     * Initializes a new {@link DeleteVersionAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param versions The versions to delete
     */
    public DeleteVersionAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> versions, Session session) {
        super(provider, queryCatalog, context, versions, session);
    }

    @Override
    protected void undoAction() throws OXException {
        if (getDocuments().isEmpty()) {
            return;
        }
        final List<DocumentMetadata> documents = getDocuments();
        final List<DocumentMetadata>[] slices = getSlices(batchSize, documents);
        final List<UpdateBlock> updates = new ArrayList<UpdateBlock>();
        for (final DocumentMetadata doc : getDocuments()) {
            updates.add(new Update(getQueryCatalog().getVersionInsert()) {
                @Override
                public void fillStatement() throws SQLException {
                    fillStmt(stmt,getQueryCatalog().getWritableVersionFields(),doc,Integer.valueOf(getContext().getContextId()));
                }
            });
        }
        for (int j = 0; j < slices.length; j++) {
            updates.add(new Update(getQueryCatalog().getVersionDelete(InfostoreQueryCatalog.Table.DEL_INFOSTORE_DOCUMENT, slices[j])){
                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, getContext().getContextId());
                }
            });
        }

        doUpdates(updates);
    }

    @Override
    public void perform() throws OXException {
        /*
         * replaces entries in the del_infostore_document table for each document version to be deleted, then removes the document
         * versions from the infostore_document table.
         */
        List<DocumentMetadata> documents = getDocuments();
        if (null == documents || documents.isEmpty()) {
            return;
        }
        /*
         * prepare update batches for
         */
        final Integer contextID = Integer.valueOf(getContext().getContextId());
        List<DocumentMetadata>[] slices = getSlices(batchSize, documents);
        List<UpdateBlock> updates = new ArrayList<UpdateBlock>(slices.length * 2);
        for (int i = 0; i < slices.length; i++) {
            final List<DocumentMetadata> slice = slices[i];
            /*
             * add batches to replace any values in the del_infostore_document table
             */
            updates.add(new Update(getQueryCatalog().getReplace(InfostoreQueryCatalog.Table.DEL_INFOSTORE_DOCUMENT, slice.size(), "cid")) {

                @Override
                public void fillStatement() throws SQLException {
                    int parameterIndex = 1;
                    for (DocumentMetadata document : slice) {
                        parameterIndex = fillStmt(parameterIndex, stmt, getQueryCatalog().getWritableDelVersionFields(), document, contextID);
                    }
                }
            });
            /*
             * add batches to remove values from the infostore_document table
             */
            updates.add(new Update(getQueryCatalog().getVersionDelete(InfostoreQueryCatalog.Table.INFOSTORE_DOCUMENT, slice)) {

                @Override
                public void fillStatement() throws SQLException {
                    stmt.setInt(1, contextID.intValue());
                }
            });
        }
        /*
         * perform updates
         */
        doUpdates(updates);
    }

    private int batchSize = 1000;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return null;
    }
}
