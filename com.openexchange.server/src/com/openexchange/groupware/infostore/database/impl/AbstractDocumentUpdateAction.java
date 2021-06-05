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

import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.session.Session;

public abstract class AbstractDocumentUpdateAction extends AbstractDocumentListAction {

	private List<DocumentMetadata> oldDocuments;
	private Metadata[] modified;
	private long timestamp;

    /**
     * Initializes a new {@link AbstractDocumentUpdateAction}.
     */
    protected AbstractDocumentUpdateAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link AbstractDocumentUpdateAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param documents The versions to update
     * @param oldDocuments The versions being updated
     * @param modifiedColums The columns to update
     * @param The sequence number to catch concurrent modifications
     */
    protected AbstractDocumentUpdateAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context,
        List<DocumentMetadata> documents, List<DocumentMetadata> oldDocuments, Metadata[] modifiedColums, long sequenceNumber, Session session) {
        super(provider, queryCatalog, context, documents, session);
        setOldDocuments(oldDocuments);
        setModified(modifiedColums);
        setTimestamp(sequenceNumber);
    }

	public void setOldDocuments(final List<DocumentMetadata> oldDocuments) {
		this.oldDocuments = oldDocuments;
	}

	public List<DocumentMetadata> getOldDocuments() {
		return oldDocuments;
	}

	public void setModified(final Metadata...modified) {
		this.modified = modified;
	}

	public Metadata[] getModified(){
		return modified;
	}

	public void setTimestamp(final long ts) {
		this.timestamp = ts;
	}

	public long getTimestamp(){
		return this.timestamp;
	}

}
