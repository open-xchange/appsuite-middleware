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
import java.sql.SQLException;
import java.util.List;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.session.Session;

public class CreateVersionAction extends AbstractDocumentListAction {

    /**
     * Initializes a new {@link CreateVersionAction}.
     */
    public CreateVersionAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link CreateVersionAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param versions The versions to create
     */
    public CreateVersionAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> versions, Session session) {
        super(provider, queryCatalog, context, versions, session);
    }

    @Override
    protected void undoAction() throws OXException {
        final UpdateBlock update = new Update(getQueryCatalog().getVersionDelete(InfostoreQueryCatalog.Table.INFOSTORE_DOCUMENT, getDocuments())){

            @Override
            public void fillStatement() throws SQLException {
                stmt.setInt(1, getContext().getContextId());
            }
        };

        doUpdates(update);
    }

    @Override
    public void perform() throws OXException {
        assureExistence();

        List<DocumentMetadata> documents = getDocuments();
        try {
            InfostoreQueryCatalog queryCatalog = getQueryCatalog();
            doUpdates(queryCatalog.getVersionInsert(), queryCatalog.getWritableVersionFields(), documents);
        } catch (OXException e) {
            if (!DBPoolingExceptionCodes.SQL_ERROR.equals(e) || !(e.getCause() instanceof SQLException)) {
                throw e;
            }

            SQLException sqle = (SQLException) e.getCause();
            if (Databases.isPrimaryKeyConflictInMySQL(sqle)) {
                DocumentMetadata document = documents.get(0);
                throw InfostoreExceptionCodes.CONCURRENT_VERSION_CREATION.create(sqle, Integer.valueOf(document.getVersion()), Integer.valueOf(document.getId()));
            }

            throw e;
        }
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return new Object[] { I(getContext().getContextId()) };
    }
}
