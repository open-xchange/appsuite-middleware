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
import java.sql.DataTruncation;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.session.Session;

public class UpdateVersionAction extends AbstractDocumentUpdateAction {

    /**
     * Initializes a new {@link UpdateVersionAction}.
     */
    public UpdateVersionAction(Session session) {
        super(session);
    }

    /**
     * Initializes a new {@link UpdateVersionAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param version The version to update
     * @param oldVersion The version being updated
     * @param modifiedColums The columns to update
     * @param The sequence number to catch concurrent modifications
     */
    public UpdateVersionAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context, DocumentMetadata version,
        DocumentMetadata oldVersion, Metadata[] modifiedColums, long sequenceNumber, Session session) {
        this(provider, queryCatalog, context, Collections.singletonList(version), Collections.singletonList(oldVersion), modifiedColums, sequenceNumber, session);
    }

    /**
     * Initializes a new {@link UpdateVersionAction}.
     *
     * @param provider The database provider
     * @param queryCatalog The query catalog
     * @param context The context
     * @param versions The versions to update
     * @param oldVersions The versions being updated
     * @param modifiedColums The columns to update
     * @param The sequence number to catch concurrent modifications
     */
    public UpdateVersionAction(DBProvider provider, InfostoreQueryCatalog queryCatalog, Context context,
        List<DocumentMetadata> versions, List<DocumentMetadata> oldVersions, Metadata[] modifiedColums, long sequenceNumber, Session session) {
        super(provider, queryCatalog, context, versions, oldVersions, modifiedColums, getMaxSequenceNumber(oldVersions, sequenceNumber), session);
    }

    /**
     * Helper method to select either the max sequenceNumber or the higher last modified date of the DocumentMetadata as the last modified date can be set manually by the client
     * @param oldVersions The oldVersions to be updated
     * @param sequenceNumber The provided sequenceNumber
     * @return The max sequence number
     */
    private static long getMaxSequenceNumber(List<DocumentMetadata> oldVersions, long sequenceNumber) {
        for (DocumentMetadata oldVersion : oldVersions) {
            Date lastModified = oldVersion.getLastModified();
            sequenceNumber = Math.max(sequenceNumber, null == lastModified ? 0 : lastModified.getTime());
        }
        return sequenceNumber;
    }

    @Override
    protected void undoAction() throws OXException {
        int counter = doUpdates(getQueryCatalog().getVersionUpdate(getModified()), getQueryCatalog().filterForVersion(getModified()), getOldDocuments());

        if (counter < 0) {
            throw InfostoreExceptionCodes.UPDATED_BETWEEN_DO_AND_UNDO.create();
        }
    }

    @Override
    public void perform() throws OXException {
        int counter = 0;
        {
            Metadata[] fields = getQueryCatalog().filterForVersion(getModified());
            fields = getQueryCatalog().filterWritable(fields);
            try {
                counter = doUpdates(getQueryCatalog().getVersionUpdate(fields), fields, getDocuments());
            } catch (OXException e) {
                final Throwable cause = e.getCause();
                if (!(cause instanceof DataTruncation)) {
                    throw e;
                }
                final DataTruncation dt = (DataTruncation) cause;
                // final String sfields[] = DBUtils.parseTruncatedFields(dt);
                throw OXCalendarExceptionCodes.TRUNCATED_SQL_ERROR.create(dt, new Object[0]);
            }
        }

        setTimestamp(System.currentTimeMillis());
        if (counter <= 0) {
            throw InfostoreExceptionCodes.MODIFIED_CONCURRENTLY.create();
        }
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return new Object[] { I(getContext().getContextId()), I(doc.getId()), I(doc.getVersion()) };
    }
}
