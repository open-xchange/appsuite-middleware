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
            updates.add(new Update(getQueryCatalog().getReplace(InfostoreQueryCatalog.Table.DEL_INFOSTORE_DOCUMENT, slice.size())) {

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
