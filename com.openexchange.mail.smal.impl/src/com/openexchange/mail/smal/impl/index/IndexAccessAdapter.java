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

import static com.openexchange.index.solr.mail.SolrMailUtility.releaseAccess;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.mail.MailUUID;
import com.openexchange.index.solr.mail.SolrMailConstants;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.AbstractSearchTermVisitor;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.session.Session;

/**
 * {@link IndexAccessAdapter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexAccessAdapter implements SolrMailConstants {
    
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

    /**
     * Checks for presence of denoted folder in index.
     * 
     * @param accountId The account identifier
     * @param fullName The folder's full name
     * @param session The session
     * @return <code>true</code> if present; otherwise <code>false</code>
     * @throws OXException If check fails
     * @throws InterruptedException If check is interrupted
     */
    public boolean containsFolder(final int accountId, final String fullName, final Session session) throws OXException, InterruptedException {
        if (null == fullName || accountId < 0) {
            return false;
        }
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return false;
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            final int contextId = session.getContextId();
            final int userId = session.getUserId();
            indexAccess = facade.acquireIndexAccess(Types.EMAIL, userId, contextId);
            return exists(userId, contextId, accountId, fullName, indexAccess);
        } finally {
            releaseAccess(facade, indexAccess);
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
                .setType(Type.MAIL)
                .setFolder(fullName).build();
                
                indexAccess.deleteByQuery(query);
                return;
            }
            /*
             * Delete by identifier
             */
            for (final String id : optMailIds) {
                final MailUUID indexId = new MailUUID(contextId, userId, accountId, fullName, id);
                indexAccess.deleteById(indexId.getUUID());
            }
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

    // /**
    // * Performs the query derived from given search term.
    // *
    // * @param optAccountId The optional account identifier or <code>-1</code> to not restrict to a certain account
    // * @param optFullName The optional full name to restrict search results to specified folder
    // * @param sortField The sort field
    // * @param order The order direction
    // * @param fields The fields to pre-fill in returned {@link MailMessage} instances; if <code>null</code> all available fields are
    // filled
    // * @param session The session
    // * @return The search result
    // * @throws OXException If search fails
    // * @throws InterruptedException If processing is interrupted
    // */
    // public List<MailMessage> all(final int optAccountId, final String optFullName, final MailSortField sortField, final OrderDirection
    // order, final MailField[] fields, final Session session) throws OXException, InterruptedException {
    // return search(optAccountId, optFullName, null, sortField, order, fields, null, session);
    // }

    public List<MailMessage> search(final int accountId, final String fullName, final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final IndexRange indexRange, final Session session) throws OXException, InterruptedException {
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
            final MailFields mailFields = new MailFields(fields);
            if (null != sortField) {
                final MailField sf = MailField.getField(sortField.getField());
                if (null != sf) {
                    mailFields.add(sf);
                }
            }

            /*
             * search
             */            
            final Map<String, Object> params = new HashMap<String, Object>(1);
            params.put("accountId", accountId);
            final QueryParameters.Builder builder = new QueryParameters.Builder(params)
                                                        .setOffset(0)
                                                        .setLength(Integer.MAX_VALUE)
                                                        .setType(IndexDocument.Type.MAIL)
                                                        .setFolder(fullName);
            
            
            if (null != sortField) {
                final MailField field = MailField.getField(sortField.getField());
                final MailIndexField indexSortField = MailIndexField.getFor(field);
                if (indexSortField != null) {
                    builder.setSortField(indexSortField);
                    builder.setOrder(OrderDirection.DESC.equals(order) ? "desc" : "asc");
                }
            }
            
            final QueryParameters parameters;
            if (searchTerm == null) {
                parameters = builder.setHandler(SearchHandler.ALL_REQUEST).build();
            } else {
                final SimpleSearchTermVisitor visitor = new SimpleSearchTermVisitor();
                searchTerm.accept(visitor);
                if (visitor.simple) {
                    parameters = builder.setHandler(SearchHandler.SIMPLE).setPattern(searchTerm.getPattern().toString()).build();
                } else {
                    parameters = builder.setHandler(SearchHandler.CUSTOM).setSearchTerm(searchTerm).build();
                }
            }

            final IndexResult<MailMessage> result = indexAccess.query(parameters);
            List<IndexDocument<MailMessage>> documents = result.getResults();
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((documents == null) || documents.isEmpty()) {
                    return Collections.emptyList();
                }
                if ((fromIndex) > documents.size()) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return Collections.emptyList();
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= documents.size()) {
                    toIndex = documents.size();
                }
                documents = documents.subList(fromIndex, toIndex);
            }
            return IndexDocumentHelper.messagesFrom(documents);
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

    // /**
    // * Performs the query derived from given search term.
    // *
    // * @param optAccountId The optional account identifier or <code>-1</code> to not restrict to a certain account
    // * @param optFullName The optional full name to restrict search results to specified folder
    // * @param searchTerm The search term
    // * @param sortField The sort field
    // * @param order The order direction
    // * @param fields The fields to pre-fill in returned {@link MailMessage} instances; if <code>null</code> all available fields are
    // filled
    // * @param indexRange The index range
    // * @param session The session
    // * @return The search result
    // * @throws OXException If search fails
    // * @throws InterruptedException If processing is interrupted
    // */
    // public List<MailMessage> search(final int optAccountId, final String optFullName, final SearchTerm<?> searchTerm, final MailSortField
    // sortField, final OrderDirection order, final MailField[] fields, final IndexRange indexRange, final Session session) throws
    // OXException, InterruptedException {
    // final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
    // if (null == facade) {
    // // Index service missing
    // return Collections.emptyList();
    // }
    // IndexAccess<MailMessage> indexAccess = null;
    // try {
    // indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
    // /*
    // * Check folder existence in index
    // */
    // if ((optAccountId >= 0) && (null != optFullName) && !exists(optAccountId, optFullName, indexAccess)) {
    // throw MailExceptionCode.FOLDER_NOT_FOUND.create(optFullName);
    // }
    // final MailFields mailFields = new MailFields(fields);
    // if (null != sortField) {
    // final MailField sf = MailField.getField(sortField.getField());
    // if (null != sf) {
    // mailFields.add(sf);
    // }
    // }
    // final QueryParameters.Builder builder;
    // {
    // final StringBuilder queryBuilder = new StringBuilder(128);
    // queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
    // queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
    // if (optAccountId >= 0) {
    // queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(optAccountId).append(')');
    // }
    // if (null != optFullName) {
    // queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(optFullName).append("\")");
    // }
    // if (null != searchTerm) {
    // queryBuilder.append(" AND (").append(SearchTerm2Query.searchTerm2Query(searchTerm)).append(')');
    // }
    // builder = new QueryParameters.Builder(queryBuilder.toString());
    // }
    // if (null != sortField) {
    // final Map<String, Object> params = new HashMap<String, Object>(2);
    // final MailField field = MailField.getField(sortField.getField());
    // final List<String> list = SolrMailUtility.getField2NameMap().get(field);
    // if (null != list && !list.isEmpty()) {
    // params.put("sort", list.get(0));
    // params.put("order", OrderDirection.DESC.equals(order) ? "desc" : "asc");
    // builder.setParameters(params);
    // }
    // }
    // final QueryParameters parameters = builder.setOffset(0).setLength(Integer.MAX_VALUE).setType(IndexDocument.Type.MAIL).build();
    // final IndexResult<MailMessage> result = indexAccess.query(parameters);
    // List<IndexDocument<MailMessage>> documents = result.getResults();
    // if (indexRange != null) {
    // final int fromIndex = indexRange.start;
    // int toIndex = indexRange.end;
    // if ((documents == null) || documents.isEmpty()) {
    // return Collections.emptyList();
    // }
    // if ((fromIndex) > documents.size()) {
    // /*
    // * Return empty iterator if start is out of range
    // */
    // return Collections.emptyList();
    // }
    // /*
    // * Reset end index if out of range
    // */
    // if (toIndex >= documents.size()) {
    // toIndex = documents.size();
    // }
    // documents = documents.subList(fromIndex, toIndex);
    // }
    // return IndexDocumentHelper.messagesFrom(documents);
    // } finally {
    // releaseAccess(facade, indexAccess);
    // }
    // }

    private boolean exists(final int userId, final int contextId, final int accountId, final String fullName, final IndexAccess<MailMessage> indexAccess) throws OXException, InterruptedException {
        // Query parameters
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountId", accountId);
        final QueryParameters qp =
            new QueryParameters.Builder(params).setLength(1).setOffset(0).setType(IndexDocument.Type.MAIL).setHandler(
                SearchHandler.ALL_REQUEST).setFolder(fullName).build();
        return indexAccess.query(qp).getNumFound() > 0L;
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
            indexAccess.addAttachments(new StandardIndexDocument<MailMessage>(message, IndexDocument.Type.MAIL), true);
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
                new QueryParameters.Builder(params).setHandler(SearchHandler.ALL_REQUEST).setFolder(fullName).setType(Type.MAIL).build();
            final IndexResult<MailMessage> result = indexAccess.query(query);
            final List<MailMessage> mails = new ArrayList<MailMessage>();
            mails.addAll(IndexDocumentHelper.messagesFrom(result.getResults()));

            return mails;
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

    // /**
    // * Gets the denoted messages by specified identifiers.
    // *
    // * @param accountId The account identifier
    // * @param fullName The full name of the folder holding the messages
    // * @param optMailIds The mail identifiers; if <code>null</code> all folder's messages are retrieved
    // * @param sortField The sort field
    // * @param order The sort order
    // * @param fields The fields
    // * @param session The session providing user information
    // * @return The queried messages
    // * @throws OXException If messages cannot be returned
    // * @throws InterruptedException If querying messages is interrupted
    // */
    // public List<MailMessage> getMessages(final int accountId, final String fullName, final String[] optMailIds, final MailSortField
    // sortField, final OrderDirection order, final MailField[] fields, final Session session) throws OXException, InterruptedException {
    // if (null == optMailIds || 0 == optMailIds.length) {
    // return search(accountId, fullName, null, sortField, order, fields, null, session);
    // }
    // final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
    // if (null == facade) {
    // // Index service missing
    // return Collections.emptyList();
    // }
    // IndexAccess<MailMessage> indexAccess = null;
    // try {
    // indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
    // /*
    // * Check folder existence in index
    // */
    // if ((accountId >= 0) && (null != fullName) && !exists(accountId, fullName, indexAccess)) {
    // throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullName);
    // }
    // final StringBuilder queryBuilder = new StringBuilder(128);
    // {
    // queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
    // queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
    // if (accountId >= 0) {
    // queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
    // }
    // if (null != fullName) {
    // queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
    // }
    // }
    // final int size = optMailIds.length;
    // final List<MailMessage> mails = new ArrayList<MailMessage>(size);
    // final String fieldId = FIELD_ID;
    // final int resetLen = queryBuilder.length();
    // int off = 0;
    // while (off < size) {
    // if (Thread.interrupted()) {
    // throw new InterruptedException("Thread interrupted while getting documents.");
    // }
    // int endIndex = off + GET_ROWS;
    // if (endIndex >= size) {
    // endIndex = size;
    // }
    // final int len = endIndex - off;
    // final String[] ids = new String[len];
    // System.arraycopy(optMailIds, off, ids, 0, len);
    // queryBuilder.setLength(resetLen);
    // queryBuilder.append(" AND (").append(fieldId).append(':').append(ids[0]);
    // for (int i = 1; i < len; i++) {
    // queryBuilder.append(" OR ").append(fieldId).append(':').append(ids[i]);
    // }
    // queryBuilder.append(')');
    // final QueryParameters queryParameters = new QueryParameters.Builder(queryBuilder.toString()).setLength(endIndex - off).setOffset(
    // 0).setType(IndexDocument.Type.MAIL).setParameters(Collections.<String, Object> emptyMap()).build();
    // final IndexResult<MailMessage> result = indexAccess.query(queryParameters);
    // mails.addAll(IndexDocumentHelper.messagesFrom(result.getResults()));
    // off = endIndex;
    // }
    // if (null != sortField) {
    // Collections.sort(mails, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getUserLocale(session)));
    // }
    // return mails;
    // } finally {
    // releaseAccess(facade, indexAccess);
    // }
    // }

//    private Locale getUserLocale(final Session session) {
//        if (session instanceof ServerSession) {
//            return ((ServerSession) session).getUser().getLocale();
//        }
//        final UserService userService = SmalServiceLookup.getServiceStatic(UserService.class);
//        final ContextService contextService = SmalServiceLookup.getServiceStatic(ContextService.class);
//        if (null == userService || null == contextService) {
//            return LanguageDetectionService.DEFAULT_LOCALE;
//        }
//        try {
//            return userService.getUser(session.getUserId(), contextService.getContext(session.getContextId())).getLocale();
//        } catch (final OXException e) {
//            return LanguageDetectionService.DEFAULT_LOCALE;
//        }
//    }

    private static final class SimpleSearchTermVisitor extends AbstractSearchTermVisitor {

        boolean simple = true;

        /**
         * Initializes a new {@link IndexAccessAdapter.SimpleSearchTermVisitor}.
         */
        SimpleSearchTermVisitor() {
            super();
        }

        @Override
        public void visit(final ANDTerm term) {
            simple = false;
        }

        @Override
        public void visit(final ORTerm term) {
            simple = false;
        }

    }

}
