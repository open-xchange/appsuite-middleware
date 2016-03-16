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

import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

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
                    fillStmt(stmt, fields, doc, getAdditionals(doc));
                }

            };
        }

        return doUpdates(updates);
    }

    protected abstract Object[] getAdditionals(DocumentMetadata doc);

    public void setDocuments(final List<DocumentMetadata> documents) {
        this.documents = documents;
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
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (writeCon != null) {
                getProvider().releaseWriteConnection(getContext(), writeCon);
            }
        }

    }
}
