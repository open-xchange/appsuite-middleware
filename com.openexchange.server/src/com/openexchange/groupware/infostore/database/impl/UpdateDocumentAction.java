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
import java.util.Collections;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.session.Session;

public class UpdateDocumentAction extends AbstractDocumentUpdateAction {

    private long updateToTimestamp;

    /**
     * Initializes a new {@link UpdateDocumentAction}.
     */
    public UpdateDocumentAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link UpdateDocumentAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param document The document to update
     * @param oldDocument The document being updated
     * @param modifiedColums The columns to update
     * @param The sequence number to catch concurrent modifications
     */
    public UpdateDocumentAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, DocumentMetadata document,
        DocumentMetadata oldDocument, Metadata[] modifiedColums, long sequenceNumber, Session session) {
        this(provider, queryCatalog, context, Collections.singletonList(document), Collections.singletonList(oldDocument), modifiedColums, sequenceNumber, session);
    }

    /**
     * Initializes a new {@link UpdateDocumentAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param documents The documents to update
     * @param oldDocuments The documents being updated
     * @param modifiedColums The columns to update
     * @param The sequence number to catch concurrent modifications
     */
    public UpdateDocumentAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, List<DocumentMetadata> documents, List<DocumentMetadata> oldDocuments, Metadata[] modifiedColums, long sequenceNumber, Session session) {
        super(provider, queryCatalog, context, documents, oldDocuments, modifiedColums, sequenceNumber, session);
        setUpdateToTimestamp(System.currentTimeMillis());
    }

    @Override
    protected void undoAction() throws OXException {
        setUpdateToTimestamp(System.currentTimeMillis());
        int counter = doUpdates(getQueryCatalog().getDocumentUpdate(getModified()), getQueryCatalog().filterForDocument(getModified()), getOldDocuments());
        if (counter < 0) {
            throw InfostoreExceptionCodes.UPDATED_BETWEEN_DO_AND_UNDO.create();
        }
    }

    @Override
    public void perform() throws OXException {
        int counter = 0;
        {
            Metadata[] fields = getQueryCatalog().filterForDocument(getModified());
            fields = getQueryCatalog().filterWritable(fields);
            counter = doUpdates(getQueryCatalog().getDocumentUpdate(fields), fields, getDocuments());
        }
        setTimestamp(getUpdateToTimestamp());
        setUpdateToTimestamp(System.currentTimeMillis());
        if (counter <= 0) {
            throw InfostoreExceptionCodes.MODIFIED_CONCURRENTLY.create();
        }
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return new Object[] {  L(getUpdateToTimestamp()), I(getContext().getContextId()), I(doc.getId()), L(getTimestamp()) };
    }

    @Override
    public void setTimestamp(final long ts) {
        super.setTimestamp(ts);
        for (DocumentMetadata documentMeta : getDocuments()) {
            documentMeta.setSequenceNumber(ts);
        }
    }

    public void setUpdateToTimestamp(final long ts) {
        this.updateToTimestamp = ts;
    }

    public long getUpdateToTimestamp(){
        return this.updateToTimestamp;
    }
}
