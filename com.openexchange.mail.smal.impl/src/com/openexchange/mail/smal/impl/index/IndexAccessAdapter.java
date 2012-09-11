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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.session.Session;
import static com.openexchange.mail.index.MailUtility.releaseAccess;;

/**
 * {@link IndexAccessAdapter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexAccessAdapter {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IndexAccessAdapter.class));

    private static final IndexAccessAdapter INSTANCE = new IndexAccessAdapter();

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static IndexAccessAdapter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link IndexAccessAdapter}.
     */
    private IndexAccessAdapter() {
        super();
    }

    /**
     * Gets the tracked indexing service.
     * 
     * @return The indexing service or <code>null</code> if absent
     */
    public IndexingService getIndexingService() {
        return SmalServiceLookup.getServiceStatic(IndexingService.class);
    }
    
    public IndexAccess<MailMessage> getIndexAccess(final Session session) throws OXException {
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        final IndexAccess<MailMessage> indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
        return indexAccess;
    }
    
    public void releaseIndexAccess(IndexAccess<MailMessage> indexAccess) {
        if (indexAccess != null) {
            final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
            try {
                facade.releaseIndexAccess(indexAccess);
            } catch (OXException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    public void delete(final int accountId, final String fullName, final String[] optMailIds, final Session session) throws OXException, InterruptedException {
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return;
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
            final int contextId = session.getContextId();
            final int userId = session.getUserId();
            /*
             * Delete whole folder?
             */
            if (null == optMailIds || 0 == optMailIds.length) {
                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("accountId", accountId);
                final QueryParameters query = new QueryParameters.Builder(params)
                .setHandler(SearchHandler.ALL_REQUEST)
                .setFolders(Collections.singleton(fullName)).build();
                
                indexAccess.deleteByQuery(query);
                return;
            }
            /*
             * Delete by identifier
             */
            for (final String id : optMailIds) {
                final MailUUID indexId = new MailUUID(contextId, userId, accountId, fullName, id);
                indexAccess.deleteById(indexId.toString());
            }
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

    private boolean exists(final int userId, final int contextId, final int accountId, final String fullName, final IndexAccess<MailMessage> indexAccess) throws OXException, InterruptedException {
        // Query parameters
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountId", accountId);
        final QueryParameters qp =
            new QueryParameters.Builder(params).setLength(1).setOffset(0).setHandler(
                SearchHandler.ALL_REQUEST).setFolders(Collections.singleton(fullName)).build();
        final Set<MailIndexField> fields = new HashSet<MailIndexField>(1);
        fields.add(MailIndexField.ID);
        return indexAccess.query(qp, fields).getNumFound() > 0L;
    }

    /**
     * Adds specified message's content information to index.
     * 
     * @param message The message
     * @param session The associated session
     * @throws OXException If adding message's content information to index fails
     */
    public void addContent(final MailMessage message, final Session session) throws OXException {
        if (null == message) {
            return;
        }
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return;
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
            indexAccess.addAttachments(new StandardIndexDocument<MailMessage>(message), true);
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

    public List<MailMessage> getMessages(final int accountId, final String fullName, final Session session, final MailSortField sortField, final OrderDirection order) throws OXException, InterruptedException {
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return Collections.emptyList();
        }

        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
            /*
             * Check folder existence in index
             */
            final int contextId = session.getContextId();
            final int userId = session.getUserId();
            if ((accountId >= 0) && (null != fullName) && !exists(userId, contextId, accountId, fullName, indexAccess)) {
                throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullName);
            }

            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("accountId", accountId);
            if (null != sortField) {
                final MailField field = MailField.getField(sortField.getField());
                final MailIndexField indexSortField = MailIndexField.getFor(field);
                if (indexSortField != null) {
                    params.put("sort", indexSortField);
                }

                if (order != null) {
                    params.put("order", OrderDirection.DESC.equals(order) ? "desc" : "asc");
                }
            }
            final QueryParameters query =
                new QueryParameters.Builder(params).setHandler(SearchHandler.ALL_REQUEST).setFolders(Collections.singleton(fullName)).build();
            final IndexResult<MailMessage> result = indexAccess.query(query, null);
            final List<MailMessage> mails = new ArrayList<MailMessage>();
            mails.addAll(IndexDocumentHelper.messagesFrom(result.getResults()));

            return mails;
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

}
