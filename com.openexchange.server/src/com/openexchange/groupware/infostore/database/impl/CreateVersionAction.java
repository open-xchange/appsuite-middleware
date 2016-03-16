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
