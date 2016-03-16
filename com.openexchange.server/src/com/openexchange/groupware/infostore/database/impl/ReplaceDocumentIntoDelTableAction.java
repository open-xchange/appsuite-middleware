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
            updates.add(new Update(getQueryCatalog().getReplace(Table.DEL_INFOSTORE, slice.size())) {

                @Override
                public void fillStatement() throws SQLException {
                    int parameterIndex = 1;
                    for (DocumentMetadata document : slice) {
                        parameterIndex = fillStmt(parameterIndex, stmt, getQueryCatalog().getWritableDelDocumentFields(), document, contextID);
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
