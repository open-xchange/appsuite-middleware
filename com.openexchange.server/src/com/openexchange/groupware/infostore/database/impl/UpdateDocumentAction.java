/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    }

    @Override
    protected void undoAction() throws OXException {
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

        setTimestamp(System.currentTimeMillis());
        if (counter <= 0) {
            throw InfostoreExceptionCodes.MODIFIED_CONCURRENTLY.create();
        }
    }

    @Override
    protected Object[] getAdditionals(final DocumentMetadata doc) {
        return new Object[] { I(getContext().getContextId()), I(doc.getId()), L(getTimestamp()) };
    }
}
