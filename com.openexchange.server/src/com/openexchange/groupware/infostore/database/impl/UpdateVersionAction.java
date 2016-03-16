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
import java.sql.DataTruncation;
import java.util.Collections;
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
        super(provider, queryCatalog, context, versions, oldVersions, modifiedColums, sequenceNumber, session);
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
            } catch (final OXException e) {
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
        return new Object[] { I(getContext().getContextId()), I(doc.getId()), I(doc.getVersion()), L(getTimestamp()) };
    }
}
