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

import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.session.Session;

public abstract class AbstractDocumentListAction extends AbstractInfostoreAction {

    private List<DocumentMetadata> documents;

    /**
     * Initializes a new {@link AbstractDocumentListAction}.
     *
     * @param optSession The optional session
     */
    protected AbstractDocumentListAction(Session optSession) {
        super(optSession);
    }

    /**
     * Initializes a new {@link AbstractDocumentListAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param documents The documents to create
     */
    protected AbstractDocumentListAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> documents, Session session) {
        super(session);
        setQueryCatalog(queryCatalog);
        setContext(context);
        setProvider(provider);
        setDocuments(documents);
    }

    public int doUpdates(final String query, final Metadata[] fields, final List<DocumentMetadata> docs) throws OXException {
        final UpdateBlock[] updates = new UpdateBlock[docs.size()];
        int i = 0;

        for (final DocumentMetadata doc : docs) {
            updates[i++] = new Update(query) {

                @Override
                public void fillStatement() throws SQLException {
                    Object[] additionals = getAdditionals(doc);
                    if (additionals != null) {
                        fillStmt(stmt, fields, doc, additionals);
                    } else {
                        fillStmt(stmt, fields, doc);
                    }
                }

            };
        }

        return doUpdates(updates);
    }

    protected abstract Object[] getAdditionals(DocumentMetadata doc);

    public void setDocuments(final List<DocumentMetadata> documents) {
        // Documents will never be null but this will avoid analysis tools to report null pointer ..  
        this.documents = null == documents ? Collections.emptyList() : documents;
    }

    public List<DocumentMetadata> getDocuments() {
        return this.documents;
    }

    public List<DocumentMetadata>[] getSlices(final int batchSize, final List<DocumentMetadata> documents) {
        final boolean addOne = (0 != (documents.size() % batchSize));
        int numberOfSlices = documents.size() / batchSize;
        if (addOne) {
            numberOfSlices += 1;
        }

        final List<DocumentMetadata>[] slices = new List[numberOfSlices];

        final int max = documents.size();
        for (int i = 0; i < numberOfSlices; i++) {
            final int start = i * batchSize;
            int end = (i + 1) * batchSize;
            if (end > max) {
                end = max;
            }

            final List<DocumentMetadata> slice = documents.subList(start, end);
            slices[i] = slice;

        }

        return slices;
    }

    protected void assureExistence() throws OXException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            writeCon = getProvider().getWriteConnection(getContext());
            stmt = writeCon.prepareStatement("SELECT id FROM infostore WHERE cid = " + getContext().getContextId() + " AND id = ? FOR UPDATE");
            for (final DocumentMetadata document : getDocuments()) {
                stmt.setInt(1, document.getId());
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw InfostoreExceptionCodes.DOCUMENT_NOT_EXIST.create();
                }
            }
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (writeCon != null) {
                getProvider().releaseWriteConnection(getContext(), writeCon);
            }
        }

    }
}
