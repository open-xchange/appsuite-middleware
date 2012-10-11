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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.adapter.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequest;
import org.elasticsearch.action.admin.indices.optimize.OptimizeResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.action.search.SearchOperationThreading;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.replication.ReplicationType;
import org.elasticsearch.client.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.action.index.IndexRequestBuilder;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.trove.map.TIntObjectMap;
import org.elasticsearch.common.trove.map.hash.TIntObjectHashMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.RemoteTransportException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.impl.SmalExceptionCodes;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.adapter.IndexAdapter;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link ElasticSearchAdapter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElasticSearchAdapter implements IndexAdapter {

    private static final int MAX_SEARCH_RESULTS = 1 << 20;

    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ElasticSearchAdapter.class));

    protected static final boolean DEBUG = LOG.isDebugEnabled();

    private static interface MailAccountLookup {

        MailAccount getMailAccount(int accountId) throws OXException;
    }

    private static final class InMemoryMailAccountLookup implements MailAccountLookup {

        private final TIntObjectMap<MailAccount> map;

        private final int userId;

        private final int contextId;

        /**
         * Initializes a new {@link ElasticSearchAdapter.InMemoryMailAccountLookup}.
         */
        public InMemoryMailAccountLookup(final Session session) {
            this(session.getUserId(), session.getContextId());
        }

        /**
         * Initializes a new {@link ElasticSearchAdapter.InMemoryMailAccountLookup}.
         */
        public InMemoryMailAccountLookup(final int userId, final int contextId) {
            super();
            this.contextId = contextId;
            this.userId = userId;
            map = new TIntObjectHashMap<MailAccount>(2);
        }

        @Override
        public MailAccount getMailAccount(final int accountId) throws OXException {
            MailAccount mailAccount = map.get(accountId);
            if (null == mailAccount) {
                mailAccount =
                    SmalServiceLookup.getServiceStatic(MailAccountStorageService.class).getMailAccount(accountId, userId, contextId);
                map.put(accountId, mailAccount);
            }
            return mailAccount;
        }

    }

    /**
     * The special header for extracted ElasticSearch UUID.
     */
    private static final String X_ELASTIC_SEARCH_UUID = "X-ElasticSearch-UUID";

    private volatile TransportClient client;

    private final String clusterName;

    private final String indexNamePrefix;

    private final String indexType;

    private volatile ElasticSearchTextFillerQueue textFillerQueue;

    /**
     * Initializes a new {@link ElasticSearchAdapter}.
     */
    public ElasticSearchAdapter() {
        super();
        clusterName = Constants.CLUSTER_NAME;
        indexNamePrefix = Constants.INDEX_NAME_PREFIX;
        indexType = Constants.INDEX_TYPE;
    }

    /**
     * Gets the ElasticSearch client
     *
     * @return The ElasticSearch client
     */
    public TransportClient getClient() {
        return client;
    }

    @Override
    public MailFields getIndexableFields() throws OXException {
        return Constants.INDEXABLE_FIELDS;
    }

    @Override
    public void start() throws OXException {
        final ElasticSearchTextFillerQueue q = textFillerQueue = new ElasticSearchTextFillerQueue(this);
        q.start();
        try {
            final Builder settingsBuilder = ImmutableSettings.settingsBuilder();
            /*
             * Look-up other nodes in cluster
             */
            settingsBuilder.put("client.transport.sniff", true);
            /*
             * Specify cluster name
             */
            settingsBuilder.put("cluster.name", clusterName);
            /*
             * We act as client only
             */
            settingsBuilder.put("node.data", false);
            /*
             * Create the (transport) client
             */
            client = new TransportClient(settingsBuilder.build());
            /*
             * Safe start-up in separate thread
             */
            final ThreadPoolService threadPool = SmalServiceLookup.getThreadPool();
            final TransportClient transportClient = client;
            final Future<Object> elasticSearchStartup = threadPool.submit(ThreadPools.task(new Runnable() {

                @Override
                public void run() {
                    transportClient.addTransportAddress(new InetSocketTransportAddress("192.168.32.36", 9300));
                }
            }));
            /*
             * Wait for start-up for 5 seconds
             */
            try {
                elasticSearchStartup.get(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, ElasticSearchException.class);
            } catch (final TimeoutException e) {
                throw SmalExceptionCodes.INDEX_FAULT.create("Failed start-up of ElasticSearch cluster within 5 seconds.");
            }
            /*
             * Output cluster information
             */
            clusterInfo();
        } catch (final NoNodeAvailableException e) {
            throw SmalExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final ElasticSearchException e) {
            throw SmalExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (null != client) {
            client.close();
            client = null;
        }
        final ElasticSearchTextFillerQueue q = textFillerQueue;
        if (null != q) {
            q.stop();
            textFillerQueue = null;
        }
    }

    protected void ensureStarted() {
        if (null == client) {
            throw new IllegalStateException("ElasticSearch adapter not initialized, yet.");
        }
    }

    @Override
    public void onSessionAdd(final Session session) throws OXException {
        ensureStarted();
        final int contextId = session.getContextId();
        createIndex(indexNamePrefix + contextId);
    }

    @Override
    public void onSessionGone(final Session session) throws OXException {
        ensureStarted();
        //
    }

    private void createIndex(final String indexName) throws OXException {
        /*
         * Create the index
         */
        try {
            client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
            LOG.info("Index \"" + indexName + "\" successfully created.");
            waitForYellow(indexName);
        } catch (final NoNodeAvailableException ex) {
            /*
             * No ElasticSearch node found.
             */
            throw SmalExceptionCodes.INDEX_FAULT.create(ex, ex.getMessage());
        } catch (final RemoteTransportException e) {
            final Throwable cause = e.getMostSpecificCause();
            if (!(cause instanceof IndexAlreadyExistsException)) {
                throw SmalExceptionCodes.INDEX_FAULT.create(cause, cause.getMessage());
            }
            LOG.info("Index \"" + indexName + "\" already exists.");
        } catch (final Exception ex) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(ex, ex.getMessage());
        }
        /*
         * Put mappings
         */
        try {
            /*
             * Create the mapping definition for a mail
             */
            final PutMappingRequestBuilder pmrb =
                client.admin().indices().preparePutMapping(indexName).setType(indexType).setSource(Mapping.JSON_MAPPINGS);
            pmrb.execute().actionGet();
            LOG.info("Mappings successfully created for \"" + indexName + "\".");
        } catch (final NoNodeAvailableException ex) {
            /*
             * No ElasticSearch node found.
             */
            throw SmalExceptionCodes.INDEX_FAULT.create(ex, ex.getMessage());
        } catch (final RemoteTransportException e) {
            final Throwable cause = e.getMostSpecificCause();
            throw SmalExceptionCodes.INDEX_FAULT.create(cause, cause.getMessage());
        } catch (final Exception ex) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(ex, ex.getMessage());
        }
    }

    @Override
    public boolean containsFolder(final String fullName, final int accountId, final Session session) throws OXException {
        try {
            ensureStarted();
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER_ID, session.getUserId()));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_ACCOUNT_ID, accountId));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FULL_NAME, fullName));
            /*
             * Build search request
             */
            final SearchRequestBuilder builder = client.prepareSearch(indexNamePrefix + session.getContextId()).setTypes(indexType);
            builder.setQuery(boolQuery).setSearchType(SearchType.COUNT);
            builder.setExplain(true);
            builder.setOperationThreading(SearchOperationThreading.THREAD_PER_SHARD);
            /*
             * Perform search
             */
            final SearchResponse rsp = builder.execute().actionGet(Constants.TIMEOUT_MILLIS);
            return (rsp.getHits().getTotalHits() > 0);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void deleteMessages(final Collection<String> mailIds, final String fullName, final int accountId, final Session session) throws OXException {
        try {
            ensureStarted();
            if (null == mailIds || mailIds.isEmpty()) {
                return;
            }
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER_ID, session.getUserId()));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_ACCOUNT_ID, accountId));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FULL_NAME, fullName));
            {
                final String[] sa = new String[mailIds.size()];
                final Iterator<String> iterator = mailIds.iterator();
                for (int i = 0; i < sa.length; i++) {
                    sa[i] = iterator.next();
                }
                boolQuery.must(QueryBuilders.inQuery(Constants.FIELD_ID, sa));
            }
            /*
             * Build delete request
             */
            final String indexName = indexNamePrefix + session.getContextId();
            client.prepareDeleteByQuery(indexName).setTypes(indexType).setQuery(boolQuery).execute().actionGet(Constants.TIMEOUT_MILLIS);
            refresh(indexName);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void deleteFolder(final String fullName, final int accountId, final Session session) throws OXException {
        try {
            ensureStarted();
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER_ID, session.getUserId()));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_ACCOUNT_ID, accountId));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FULL_NAME, fullName));
            /*
             * Build delete request
             */
            final String indexName = indexNamePrefix + session.getContextId();
            client.prepareDeleteByQuery(indexName).setTypes(indexType).setQuery(boolQuery).execute().actionGet(Constants.TIMEOUT_MILLIS);
            refresh(indexName);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public List<MailMessage> getMessages(final String[] optMailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int accountId, final Session session) throws OXException {
        try {
            ensureStarted();
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (null != optMailIds) {
                if (0 <= optMailIds.length) {
                    return Collections.<MailMessage> emptyList();
                }
                boolQuery.must(QueryBuilders.inQuery(Constants.FIELD_ID, optMailIds));
            }
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER_ID, session.getUserId()));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_ACCOUNT_ID, accountId));
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FULL_NAME, fullName));
            /*
             * Build search request
             */
            final int contextId = session.getContextId();
            final SearchRequestBuilder builder = client.prepareSearch(indexNamePrefix + contextId).setTypes(indexType);
            builder.setQuery(boolQuery).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
            builder.setExplain(true);
            builder.setOperationThreading(SearchOperationThreading.THREAD_PER_SHARD);
            if (null != sortField && null != order) {
                builder.addSort(sortField.getKey(), OrderDirection.DESC.equals(order) ? SortOrder.DESC : SortOrder.ASC);
            }
            builder.setSize(null == optMailIds ? MAX_SEARCH_RESULTS : optMailIds.length);
            /*
             * Perform search
             */
            final SearchResponse rsp = builder.execute().actionGet(Constants.TIMEOUT_MILLIS);
            final SearchHit[] docs = rsp.getHits().getHits();
            final List<MailMessage> mails = new ArrayList<MailMessage>(docs.length);
            final MailFields mailFields = null == fields ? new MailFields(true) : new MailFields(fields);
            final MailAccountLookup lookup = new InMemoryMailAccountLookup(session);
            for (final SearchHit sd : docs) {
                // to get explanation you'll need to enable this when querying:
                // System.out.println(sd.getExplanation().toString());

                // if we use in mapping: "_source" : {"enabled" : false}
                // we need to include all necessary fields in query and then to use doc.getFields()
                // instead of doc.getSource()
                final MailMessage mail = readDoc(sd.getSource(), mailFields, lookup, contextId);
                mail.setHeader(X_ELASTIC_SEARCH_UUID, sd.getId());
                mails.add(mail);
            }
            return mails;
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public List<MailMessage> search(final String query, final MailField[] fields, final Session session) throws OXException, InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS };

    @Override
    public List<MailMessage> all(final String optFullName, final int optAccountId, final Session session) throws OXException, InterruptedException {
        return search(optFullName, null, null, null, FIELDS, null, optAccountId, session, null);
    }

    @Override
    public List<MailMessage> search(final String optFullName, final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final IndexRange indexRange, final int optAccountId, final Session session, final boolean[] more) throws OXException {
        try {
            ensureStarted();
            final int contextId = session.getContextId();
            final String indexName = indexNamePrefix + contextId;
            /*
             * Build search query
             */
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER_ID, session.getUserId()));
            if (optAccountId >= 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_ACCOUNT_ID, optAccountId));
            }
            if (null != optFullName) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FULL_NAME, optFullName));
            }
            if (null != searchTerm) {
                boolQuery.must(SearchTerm2Query.searchTerm2Query(searchTerm));
            }
            /*
             * Count results
             */
            final long total;
            {
                final SearchRequestBuilder srb = client.prepareSearch(indexName).setTypes(indexType).addField(Constants.FIELD_ID);
                if (null != sortField && null != order) {
                    srb.addSort(sortField.getKey(), OrderDirection.DESC.equals(order) ? SortOrder.DESC : SortOrder.ASC);
                }
                srb.setQuery(boolQuery);
                srb.setSearchType(SearchType.COUNT);
                srb.setExplain(true);
                srb.setOperationThreading(SearchOperationThreading.THREAD_PER_SHARD);
                total = srb.execute().actionGet(Constants.TIMEOUT_MILLIS).getHits().getTotalHits();
            }
            if (DEBUG) {
                LOG.debug("Search in " + (null == optFullName ? "all folders" : optFullName) + " for " + (optAccountId >= 0 ? "account "+optAccountId : "all accounts") + " yields " + total + " results.");
            }
            /*
             * Page-wise retrieval of search results
             */
            final List<MailMessage> mails = new ArrayList<MailMessage>((int) total);
            final MailAccountLookup lookup = new InMemoryMailAccountLookup(session);
            final MailFields allFields = new MailFields(true);
            final int hitsPerPage = Constants.MAX_FILLER_CHUNK;
            int offset = 0;
            while (offset < total) {
                final SearchRequestBuilder srb = client.prepareSearch(indexName).setTypes(indexType).addField(Constants.FIELD_ID);
                if (null != fields) {
                    applyFields2Builder(fields, srb);
                }
                if (null != sortField && null != order) {
                    srb.addSort(sortField.getKey(), OrderDirection.DESC.equals(order) ? SortOrder.DESC : SortOrder.ASC);
                }
                /*
                 * Calculate the number of search hits to return
                 */
                int size = (int) (total - offset);
                if (size > hitsPerPage) {
                    size = hitsPerPage;
                }
                srb.setFrom(offset).setSize(size);
                srb.setQuery(boolQuery);
                srb.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
                srb.setExplain(true);
                srb.setOperationThreading(SearchOperationThreading.THREAD_PER_SHARD);
                /*
                 * Execute search & iterate hits
                 */
                final long st = DEBUG ? System.currentTimeMillis() : 0L;
                final SearchResponse rsp = srb.execute().actionGet(Constants.TIMEOUT_MILLIS);
                final SearchHit[] hits = rsp.getHits().getHits();
                offset += size;
                if (DEBUG) {
                    final long dur = System.currentTimeMillis() - st;
                    LOG.debug("ES search took " + dur + "msec with " + hits.length + " results in " + (null == optFullName ? "all folders" : optFullName) + " for " + (optAccountId >= 0 ? "account "+optAccountId+". " : "all accounts. ") + (total - offset) + " to go...");
                }
                for (final SearchHit searchHit : hits) {
                    final MailMessage mail;
                    if (null == fields) {
                        /*
                         * Read from source
                         */
                        mail = readDoc(searchHit.getSource(), allFields, lookup, contextId);
                    } else {
                        /*
                         * Read from search fields
                         */
                        mail = readDoc(searchHit, fields, lookup);
                    }
                    if (null != optFullName) {
                        mail.setFolder(optFullName);
                    }
                    if (optAccountId >= 0) {
                        mail.setAccountId(optAccountId);
                    }
                    mail.setHeader(X_ELASTIC_SEARCH_UUID, searchHit.getId());
                    mails.add(mail);
                }
            }
            /*
             * Finally return mails
             */
            return mails;
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static void applyFields2Builder(final MailField[] fields, final SearchRequestBuilder srb) {
        for (final MailField mailField : fields) {
            switch (mailField) {
            case ACCOUNT_NAME:
                srb.addField(Constants.FIELD_ACCOUNT_ID);
                break;
            case ID:
                srb.addField(Constants.FIELD_ID);
                break;
            case FOLDER_ID:
                srb.addField(Constants.FIELD_FULL_NAME);
                break;
            case FROM:
                srb.addField(Constants.FIELD_FROM);
                break;
            case TO:
                srb.addField(Constants.FIELD_TO);
                break;
            case CC:
                srb.addField(Constants.FIELD_CC);
                break;
            case BCC:
                srb.addField(Constants.FIELD_BCC);
                break;
            case SUBJECT:
                srb.addField(Constants.FIELD_SUBJECT);
                break;
            case RECEIVED_DATE:
                srb.addField(Constants.FIELD_RECEIVED_DATE);
                break;
            case SENT_DATE:
                srb.addField(Constants.FIELD_SENT_DATE);
                break;
            case SIZE:
                srb.addField(Constants.FIELD_SIZE);
                break;
            case FLAGS:
                srb.addFields(
                    Constants.FIELD_FLAG_ANSWERED,
                    Constants.FIELD_FLAG_DELETED,
                    Constants.FIELD_FLAG_DRAFT,
                    Constants.FIELD_FLAG_FLAGGED,
                    Constants.FIELD_FLAG_FORWARDED,
                    Constants.FIELD_FLAG_READ_ACK,
                    Constants.FIELD_FLAG_RECENT,
                    Constants.FIELD_FLAG_SEEN,
                    Constants.FIELD_FLAG_SPAM,
                    Constants.FIELD_FLAG_USER);
                break;
            default:
                break;
            }
        }
    }

    private MailMessage readDoc(final Map<String, Object> source, final MailFields fields, final MailAccountLookup lookup, final int contextId) throws OXException {
        try {
            final MailMessage mail = new IDMailMessage(null, null);
            if (null != source) {
                if (!source.containsKey(Constants.FIELD_BODY)) {
                    textFillerQueue.add(TextFiller.fillerFor(source, contextId));
                }
                mail.setMailId((String) source.get(Constants.FIELD_ID));
                mail.setFolder((String) source.get(Constants.FIELD_FULL_NAME));
                {
                    final int accountId = ((Integer) source.get(Constants.FIELD_ACCOUNT_ID)).intValue();
                    mail.setAccountId(accountId);
                    mail.setAccountName(lookup.getMailAccount(accountId).getName());
                }
                if (fields.contains(MailField.SUBJECT)) {
                    mail.setSubject((String) source.get(Constants.FIELD_SUBJECT));
                }
                if (fields.contains(MailField.SIZE)) {
                    final Number size = (Number) source.get(Constants.FIELD_SIZE);
                    if (null != size) {
                        mail.setSize(size.longValue());
                    }
                }
                if (fields.contains(MailField.RECEIVED_DATE)) {
                    final Number l = (Number) source.get(Constants.FIELD_RECEIVED_DATE);
                    if (null != l) {
                        mail.setReceivedDate(new Date(l.longValue()));
                    }
                }
                if (fields.contains(MailField.SENT_DATE)) {
                    final Number l = (Number) source.get(Constants.FIELD_SENT_DATE);
                    if (null != l) {
                        mail.setReceivedDate(new Date(l.longValue()));
                    }
                }
                if (fields.contains(MailField.FLAGS)) {
                    int flags = 0;
                    Boolean tmp = (Boolean) source.get(Constants.FIELD_FLAG_ANSWERED);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_ANSWERED;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_DELETED);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_DELETED;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_DRAFT);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_DRAFT;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_FLAGGED);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_FLAGGED;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_FORWARDED);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_FORWARDED;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_READ_ACK);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_READ_ACK;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_RECENT);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_RECENT;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_SEEN);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_SEEN;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_SPAM);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_SPAM;
                    }
                    tmp = (Boolean) source.get(Constants.FIELD_FLAG_USER);
                    if (null != tmp && tmp.booleanValue()) {
                        flags |= MailMessage.FLAG_USER;
                    }
                    mail.setFlags(flags);
                }
                if (fields.contains(MailField.FROM)) {
                    @SuppressWarnings("unchecked") final Collection<String> sAddrs = (Collection<String>) source.get(Constants.FIELD_FROM);
                    if (null != sAddrs) {
                        for (final String sAddr : sAddrs) {
                            try {
                                mail.addFrom(new QuotedInternetAddress(sAddr));
                            } catch (final AddressException e) {
                                mail.addFrom(new PlainTextAddress(sAddr));
                            }
                        }
                    }
                }
                if (fields.contains(MailField.TO)) {
                    @SuppressWarnings("unchecked") final Collection<String> sAddrs = (Collection<String>) source.get(Constants.FIELD_TO);
                    if (null != sAddrs) {
                        for (final String sAddr : sAddrs) {
                            try {
                                mail.addFrom(new QuotedInternetAddress(sAddr));
                            } catch (final AddressException e) {
                                mail.addFrom(new PlainTextAddress(sAddr));
                            }
                        }
                    }
                }
                if (fields.contains(MailField.CC)) {
                    @SuppressWarnings("unchecked") final Collection<String> sAddrs = (Collection<String>) source.get(Constants.FIELD_CC);
                    if (null != sAddrs) {
                        for (final String sAddr : sAddrs) {
                            try {
                                mail.addFrom(new QuotedInternetAddress(sAddr));
                            } catch (final AddressException e) {
                                mail.addFrom(new PlainTextAddress(sAddr));
                            }
                        }
                    }
                }
                if (fields.contains(MailField.BCC)) {
                    @SuppressWarnings("unchecked") final Collection<String> sAddrs = (Collection<String>) source.get(Constants.FIELD_BCC);
                    if (null != sAddrs) {
                        for (final String sAddr : sAddrs) {
                            try {
                                mail.addFrom(new QuotedInternetAddress(sAddr));
                            } catch (final AddressException e) {
                                mail.addFrom(new PlainTextAddress(sAddr));
                            }
                        }
                    }
                }
            }
            return mail;
        } catch (final ElasticSearchException e) {
            throw SmalExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private MailMessage readDoc(final SearchHit searchHit, final MailField[] fields, final MailAccountLookup lookup) throws OXException {
        final IDMailMessage mail = new IDMailMessage(null, null);
        for (final MailField mailField : fields) {
            switch (mailField) {
            case ACCOUNT_NAME:
                {
                    final Object tmp = searchHit.field(Constants.FIELD_ACCOUNT_ID).value();
                    if (null == tmp) {
                        mail.setAccountId(-1);
                        mail.setAccountName(null);
                    } else {
                        final int accountId = Integer.parseInt(tmp.toString());
                        mail.setAccountId(accountId);
                        mail.setAccountName(lookup.getMailAccount(accountId).getName());
                    }
                }
                break;
            case ID:
                mail.setMailId((String) searchHit.field(Constants.FIELD_ID).value());
                break;
            case FOLDER_ID:
                mail.setFolder((String) searchHit.field(Constants.FIELD_FULL_NAME).value());
                break;
            case FROM:
                {
                    final List<Object> sAddrs = searchHit.field(Constants.FIELD_FROM).getValues();
                    if (null != sAddrs) {
                        for (final Object sAddr : sAddrs) {
                            try {
                                mail.addFrom(new QuotedInternetAddress(sAddr.toString()));
                            } catch (final AddressException e) {
                                mail.addFrom(new PlainTextAddress(sAddr.toString()));
                            }
                        }
                    }
                }
                break;
            case TO:
                {
                    final List<Object> sAddrs = searchHit.field(Constants.FIELD_TO).getValues();
                    if (null != sAddrs) {
                        for (final Object sAddr : sAddrs) {
                            try {
                                mail.addTo(new QuotedInternetAddress(sAddr.toString()));
                            } catch (final AddressException e) {
                                mail.addTo(new PlainTextAddress(sAddr.toString()));
                            }
                        }
                    }
                }
                break;
            case CC:
                {
                    final List<Object> sAddrs = searchHit.field(Constants.FIELD_CC).getValues();
                    if (null != sAddrs) {
                        for (final Object sAddr : sAddrs) {
                            try {
                                mail.addCc(new QuotedInternetAddress(sAddr.toString()));
                            } catch (final AddressException e) {
                                mail.addCc(new PlainTextAddress(sAddr.toString()));
                            }
                        }
                    }
                }
                break;
            case BCC:
                {
                    final List<Object> sAddrs = searchHit.field(Constants.FIELD_BCC).getValues();
                    if (null != sAddrs) {
                        for (final Object sAddr : sAddrs) {
                            try {
                                mail.addBcc(new QuotedInternetAddress(sAddr.toString()));
                            } catch (final AddressException e) {
                                mail.addBcc(new PlainTextAddress(sAddr.toString()));
                            }
                        }
                    }
                }
                break;
            case SUBJECT:
                {
                    final Object tmp = searchHit.field(Constants.FIELD_SUBJECT).value();
                    if (null == tmp) {
                        mail.setSubject(null);
                    } else {
                        mail.setSubject(tmp.toString());
                    }
                }
                break;
            case RECEIVED_DATE:
                {
                    final Object tmp = searchHit.field(Constants.FIELD_RECEIVED_DATE).value().toString();
                    if (null == tmp) {
                        mail.setReceivedDate(null);
                    } else {
                        mail.setReceivedDate(new Date(Long.parseLong(tmp.toString())));
                    }
                }
                break;
            case SENT_DATE:
                {
                    final Object tmp = searchHit.field(Constants.FIELD_SENT_DATE).value().toString();
                    if (null == tmp) {
                        mail.setSentDate(null);
                    } else {
                        mail.setSentDate(new Date(Long.parseLong(tmp.toString())));
                    }
                }
                break;
            case SIZE:
                {
                    final Object tmp = searchHit.field(Constants.FIELD_SIZE).value().toString();
                    if (null == tmp) {
                        mail.setSize(-1L);
                    } else {
                        mail.setSize(Long.parseLong(tmp.toString()));
                    }
                }
                break;
            case FLAGS:
                {
                    int flags = 0;
                    Object tmp = searchHit.field(Constants.FIELD_FLAG_ANSWERED).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_ANSWERED;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_DELETED).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_DELETED;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_DRAFT).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_DRAFT;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_FLAGGED).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_FLAGGED;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_FORWARDED).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_FORWARDED;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_READ_ACK).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_READ_ACK;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_RECENT).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_RECENT;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_SEEN).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_SEEN;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_SPAM).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_SPAM;
                    }
                    tmp = searchHit.field(Constants.FIELD_FLAG_USER).value();
                    if (null != tmp && Boolean.parseBoolean(tmp.toString())) {
                        flags |= MailMessage.FLAG_USER;
                    }
                    mail.setFlags(flags);
                }
                break;
            default:
                break;
            }
        }
        return mail;
    }

    /**
     * Logs some cluster information.
     *
     * @throws OXException
     */
    public void clusterInfo() throws OXException {
        try {
            ensureStarted();
            final NodesInfoResponse rsp = client.admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet();
            final StringBuilder sb =
                new StringBuilder(32).append("Cluster: ").append(rsp.getClusterName()).append(". Active nodes: ").append(
                    rsp.getNodesMap().keySet().size());
            LOG.info(sb.toString());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /**
     * Checks if specified index exists.
     *
     * @param indexName The index name
     * @return <code>true</code> if index exists; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean indexExists(final String indexName) throws OXException {
        try {
            ensureStarted();
            final ImmutableMap<String, IndexMetaData> map =
                client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().getIndices();
            return map.containsKey(indexName);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    public void saveCreateIndex(final String indexName, final boolean log) {
        ensureStarted();
        // if (!indexExists(name)) {
        try {
            createIndex(indexName);
            if (log) {
                LOG.info("Created index: " + indexName);
            }
        } catch (final Exception ex) {
            // } else {
            if (log) {
                LOG.info("Index " + indexName + " already exists");
            }
        }
    }

    /**
     * Wait for at least one active shard.
     */
    public void waitForOneActiveShard(final String indexName) {
        ensureStarted();
        client.admin().cluster().health(new ClusterHealthRequest(indexName).waitForActiveShards(1)).actionGet();
        LOG.info("Now node has at least one active shard!");
    }

    /**
     * Waits for yellow status.
     */
    public void waitForYellow(final String indexName) {
        ensureStarted();
        client.admin().cluster().health(new ClusterHealthRequest(indexName).waitForYellowStatus()).actionGet();
    }

    /**
     * Waits for green status.
     */
    public void waitForGreen(final String indexName) {
        ensureStarted();
        client.admin().cluster().health(new ClusterHealthRequest(indexName).waitForGreenStatus()).actionGet();
    }

    /**
     * Explicitly refresh one or more indices (making the content indexed since the last refresh searchable).
     */
    public void refresh(final String indexName) {
        ensureStarted();
        client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }

    /**
     * Gets the number of mails held in index.
     *
     * @return The number of mails
     */
    public long countAll(final String indexName) {
        ensureStarted();
        final CountResponse response =
            client.prepareCount(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet(Constants.TIMEOUT_MILLIS);
        return response.getCount();
    }

    @Override
    public void add(final MailMessage mail, final Session session) throws OXException {
        try {
            ensureStarted();
            final String indexName = indexNamePrefix + session.getContextId();
            final String id = UUID.randomUUID().toString();
            final IndexRequestBuilder irb = client.prepareIndex(indexName, indexType, id);
            irb.setReplicationType(ReplicationType.ASYNC);
            irb.setOpType(OpType.CREATE);
            irb.setConsistencyLevel(WriteConsistencyLevel.DEFAULT).setSource(
                createDoc(id, mail, mail.getAccountId(), session, System.currentTimeMillis()));
            irb.setType(indexType);
            irb.execute().actionGet(Constants.TIMEOUT_MILLIS);
            refresh(indexName);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void add(final List<MailMessage> mails, final Session session) throws OXException {
        try {
            ensureStarted();
            if (null == mails || mails.isEmpty()) {
                return;
            }
            final String indexName = indexNamePrefix + session.getContextId();
            final long stamp = System.currentTimeMillis();
            final int size = mails.size();
            final int configuredBlockSize = Constants.MAX_FILLER_CHUNK;
            if (size <= configuredBlockSize) {
                addSublist(mails, session, indexName, stamp);
            } else {
                int fromIndex = 0;
                while (fromIndex < size) {
                    int toIndex = fromIndex + configuredBlockSize;
                    if (toIndex > size) {
                        toIndex = size;
                    }
                    addSublist(mails.subList(fromIndex, toIndex), session, indexName, stamp);
                    fromIndex = toIndex;
                }
            }
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private void addSublist(final Collection<MailMessage> mails, final Session session, final String indexName, final long stamp) throws OXException {
        try {
            ensureStarted();
            if (null == mails || mails.isEmpty()) {
                return;
            }
            final BulkRequestBuilder bulkRequest = client.prepareBulk();
            final List<TextFiller> fillers = new ArrayList<TextFiller>(mails.size());
            for (final MailMessage mail : mails) {
                final String uuid = UUID.randomUUID().toString();
                fillers.add(TextFiller.fillerFor(uuid, mail, session));
                final IndexRequestBuilder irb = client.prepareIndex(indexName, indexType, uuid);
                irb.setReplicationType(ReplicationType.ASYNC);
                irb.setOpType(OpType.CREATE);
                irb.setSource(createDoc(uuid, mail, mail.getAccountId(), session, stamp));
                irb.setType(indexType);
                bulkRequest.add(irb);
            }
            if (bulkRequest.numberOfActions() > 0) {
                bulkRequest.execute().actionGet(Constants.TIMEOUT_MILLIS);
                refresh(indexName);
                textFillerQueue.add(fillers);
            }
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static XContentBuilder createDoc(final String uuid, final MailMessage mail, final int accountId, final Session session, final long stamp) throws OXException {
        try {
            final XContentBuilder b = JsonXContent.unCachedContentBuilder().startObject();
            b.field(Constants.FIELD_TIMESTAMP, stamp);
            /*
             * Identifiers
             */
            b.field(Constants.FIELD_UUID, uuid);
            b.field(Constants.FIELD_USER_ID, session.getUserId());
            b.field(Constants.FIELD_ACCOUNT_ID, accountId);
            b.field(Constants.FIELD_FULL_NAME, mail.getFolder());
            b.field(Constants.FIELD_ID, mail.getMailId());
            /*
             * Write address fields
             */
            {
                String[] tmp = toStringArray(mail.getFrom());
                if (null != tmp) {
                    b.field(Constants.FIELD_FROM, tmp);
                }
                tmp = toStringArray(mail.getTo());
                if (null != tmp) {
                    b.field(Constants.FIELD_TO, tmp);
                }
                tmp = toStringArray(mail.getCc());
                if (null != tmp) {
                    b.field(Constants.FIELD_CC, tmp);
                }
                tmp = toStringArray(mail.getBcc());
                if (null != tmp) {
                    b.field(Constants.FIELD_BCC, tmp);
                }
            }
            /*
             * Write size
             */
            if (mail.containsSize()) {
                b.field(Constants.FIELD_SIZE, mail.getSize());
            }
            /*
             * Write date fields
             */
            {
                java.util.Date d = mail.getReceivedDate();
                if (null != d) {
                    b.field(Constants.FIELD_RECEIVED_DATE, d.getTime());
                }
                d = mail.getSentDate();
                if (null != d) {
                    b.field(Constants.FIELD_SENT_DATE, d.getTime());
                }
            }
            /*
             * Write flags
             */
            final int flags = mail.getFlags();
            b.field(Constants.FIELD_FLAG_ANSWERED, (flags & MailMessage.FLAG_ANSWERED) > 0);
            b.field(Constants.FIELD_FLAG_DELETED, (flags & MailMessage.FLAG_DELETED) > 0);
            b.field(Constants.FIELD_FLAG_DRAFT, (flags & MailMessage.FLAG_DRAFT) > 0);
            b.field(Constants.FIELD_FLAG_FLAGGED, (flags & MailMessage.FLAG_FLAGGED) > 0);
            b.field(Constants.FIELD_FLAG_RECENT, (flags & MailMessage.FLAG_RECENT) > 0);
            b.field(Constants.FIELD_FLAG_SEEN, (flags & MailMessage.FLAG_SEEN) > 0);
            b.field(Constants.FIELD_FLAG_USER, (flags & MailMessage.FLAG_USER) > 0);
            b.field(Constants.FIELD_FLAG_SPAM, (flags & MailMessage.FLAG_SPAM) > 0);
            b.field(Constants.FIELD_FLAG_FORWARDED, (flags & MailMessage.FLAG_FORWARDED) > 0);
            b.field(Constants.FIELD_FLAG_READ_ACK, (flags & MailMessage.FLAG_READ_ACK) > 0);
            /*
             * Subject
             */
            b.field(Constants.FIELD_SUBJECT, mail.getSubject());
            b.endObject();
            return b;
        } catch (final IOException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static String[] toStringArray(final InternetAddress[] addrs) {
        if (addrs == null || addrs.length <= 0) {
            return null;
        }
        final String[] sa = new String[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            sa[i] = addrs[i].toUnicodeString();
        }
        return sa;
    }

    @Override
    public void change(final List<MailMessage> mails, final Session session) throws OXException {
        try {
            ensureStarted();
            if (null == mails || mails.isEmpty()) {
                return;
            }
            final int contextId = session.getContextId();
            final String indexName = indexNamePrefix + contextId;
            final int size = mails.size();
            final int configuredBlockSize = Constants.MAX_FILLER_CHUNK;
            if (size <= configuredBlockSize) {
                changeSublist(mails, session, indexName, contextId);
            } else {
                final List<MailMessage> list;
                if (mails instanceof List) {
                    list = mails;
                } else {
                    list = new ArrayList<MailMessage>(mails);
                }
                int fromIndex = 0;
                while (fromIndex < size) {
                    int toIndex = fromIndex + configuredBlockSize;
                    if (toIndex > size) {
                        toIndex = size;
                    }
                    changeSublist(list.subList(fromIndex, toIndex), session, indexName, contextId);
                    fromIndex = toIndex;
                }
            }
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private void changeSublist(final Collection<MailMessage> mails, final Session session, final String indexName, final int contextId) throws OXException {
        /*
         * Request JSON representations
         */
        final Map<String, Map<String, Object>> jsonObjects;
        {
            final String[] mailIds = new String[mails.size()];
            String fullName = null;
            int accountId = -1;
            final Iterator<MailMessage> iterator = mails.iterator();
            boolean first = true;
            for (int i = 0; i < mailIds.length; i++) {
                if (first) {
                    final MailMessage m = iterator.next();
                    fullName = m.getFolder();
                    accountId = m.getAccountId();
                    mailIds[0] = m.getMailId();
                    first = false;
                } else {
                    mailIds[i] = iterator.next().getMailId();
                }
            }
            jsonObjects = getJSONMessages(mailIds, fullName, null, null, accountId, session);
        }
        /*
         * Apply new time stamp & flags and add to index
         */
        final BulkRequestBuilder bulkRequest = client.prepareBulk();
        final long stamp = System.currentTimeMillis();
        for (final MailMessage mail : mails) {
            final Map<String, Object> jsonObject = jsonObjects.get(mail.getMailId());
            if (null != jsonObject) {
                final IndexRequestBuilder irb = client.prepareIndex(indexName, indexType, (String) jsonObject.get(Constants.FIELD_UUID));
                irb.setReplicationType(ReplicationType.ASYNC);
                irb.setOpType(OpType.INDEX);
                irb.setSource(changeDoc(mail, jsonObject, stamp, contextId));
                irb.setType(indexType);
                bulkRequest.add(irb);
            }
        }
        if (bulkRequest.numberOfActions() > 0) {
            bulkRequest.execute().actionGet(Constants.TIMEOUT_MILLIS);
            refresh(indexName);
        }
    }

    private Map<String, Map<String, Object>> getJSONMessages(final String[] mailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final int accountId, final Session session) {
        ensureStarted();
        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        final Set<String> filter;
        if (null != mailIds) {
            if (0 >= mailIds.length) {
                return Collections.emptyMap();
            }
            if (mailIds.length <= 50) {
                /*
                 * Filtering is performed by query
                 */
                boolQuery.must(QueryBuilders.inQuery(Constants.FIELD_ID, mailIds));
                filter = null;
            } else {
                /*
                 * Request all and filter
                 */
                filter = new HashSet<String>(mailIds.length);
                for (final String mailId : mailIds) {
                    filter.add(mailId);
                }
            }
        } else {
            filter = null;
        }
        boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER_ID, session.getUserId()));
        boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_ACCOUNT_ID, accountId));
        boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FULL_NAME, fullName));
        /*
         * Build search request
         */
        final SearchRequestBuilder builder = client.prepareSearch(indexNamePrefix + session.getContextId()).setTypes(indexType);
        builder.setQuery(boolQuery).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        builder.setExplain(true);
        builder.setOperationThreading(SearchOperationThreading.THREAD_PER_SHARD);
        if (null != sortField && null != order) {
            builder.addSort(sortField.getKey(), OrderDirection.DESC.equals(order) ? SortOrder.DESC : SortOrder.ASC);
        }
        builder.setSize(null == mailIds ? MAX_SEARCH_RESULTS : mailIds.length);
        /*
         * Perform search
         */
        final SearchResponse rsp = builder.execute().actionGet(Constants.TIMEOUT_MILLIS);
        final SearchHits searchHits = rsp.getHits();
        final SearchHit[] docs = searchHits.getHits();
        final Map<String, Map<String, Object>> jsonObjects = new HashMap<String, Map<String, Object>>(docs.length);
        for (final SearchHit sd : docs) {
            final Map<String, Object> source = sd.getSource();
            final String mailId = (String) source.get(Constants.FIELD_ID);
            if (null == filter || filter.contains(mailId)) {
                jsonObjects.put(mailId, source);
            }
        }
        return jsonObjects;
    }

    @SuppressWarnings("unchecked")
    private XContentBuilder changeDoc(final MailMessage mail, final Map<String, Object> jsonObject, final long stamp, final int contextId) throws OXException {
        try {
            final XContentBuilder b = JsonXContent.unCachedContentBuilder().startObject();
            b.field(Constants.FIELD_TIMESTAMP, stamp);
            /*
             * Content present?
             */
            if (!jsonObject.containsKey(Constants.FIELD_BODY)) {
                textFillerQueue.add(TextFiller.fillerFor(jsonObject, contextId));
            }
            /*
             * Identifiers
             */
            b.field(Constants.FIELD_UUID, (String) jsonObject.get(Constants.FIELD_UUID));
            b.field(Constants.FIELD_USER_ID, ((Number) jsonObject.get(Constants.FIELD_USER_ID)).intValue());
            b.field(Constants.FIELD_ACCOUNT_ID, ((Number) jsonObject.get(Constants.FIELD_ACCOUNT_ID)).intValue());
            b.field(Constants.FIELD_FULL_NAME, (String) jsonObject.get(Constants.FIELD_FULL_NAME));
            b.field(Constants.FIELD_ID, (String) jsonObject.get(Constants.FIELD_ID));
            /*
             * Write address fields
             */
            {
                List<String> tmp = (List<String>) jsonObject.get(Constants.FIELD_FROM);
                if (null != tmp) {
                    b.field(Constants.FIELD_FROM, tmp.toArray(new String[0]));
                }
                tmp = (List<String>) jsonObject.get(Constants.FIELD_TO);
                if (null != tmp) {
                    b.field(Constants.FIELD_TO, tmp.toArray(new String[0]));
                }
                tmp = (List<String>) jsonObject.get(Constants.FIELD_CC);
                if (null != tmp) {
                    b.field(Constants.FIELD_CC, tmp.toArray(new String[0]));
                }
                tmp = (List<String>) jsonObject.get(Constants.FIELD_BCC);
                if (null != tmp) {
                    b.field(Constants.FIELD_BCC, tmp.toArray(new String[0]));
                }
            }
            /*
             * Write size
             */
            Number number = (Number) jsonObject.get(Constants.FIELD_SIZE);
            if (null != number) {
                b.field(Constants.FIELD_SIZE, number.longValue());
            }
            /*
             * Write date fields
             */
            number = (Number) jsonObject.get(Constants.FIELD_RECEIVED_DATE);
            if (null != number) {
                b.field(Constants.FIELD_RECEIVED_DATE, number.longValue());
            }
            number = (Number) jsonObject.get(Constants.FIELD_SENT_DATE);
            if (null != number) {
                b.field(Constants.FIELD_SENT_DATE, number.longValue());
            }
            /*
             * Write flags
             */
            final int flags = mail.getFlags();
            b.field(Constants.FIELD_FLAG_ANSWERED, (flags & MailMessage.FLAG_ANSWERED) > 0);
            b.field(Constants.FIELD_FLAG_DELETED, (flags & MailMessage.FLAG_DELETED) > 0);
            b.field(Constants.FIELD_FLAG_DRAFT, (flags & MailMessage.FLAG_DRAFT) > 0);
            b.field(Constants.FIELD_FLAG_FLAGGED, (flags & MailMessage.FLAG_FLAGGED) > 0);
            b.field(Constants.FIELD_FLAG_RECENT, (flags & MailMessage.FLAG_RECENT) > 0);
            b.field(Constants.FIELD_FLAG_SEEN, (flags & MailMessage.FLAG_SEEN) > 0);
            b.field(Constants.FIELD_FLAG_USER, (flags & MailMessage.FLAG_USER) > 0);
            b.field(Constants.FIELD_FLAG_SPAM, (flags & MailMessage.FLAG_SPAM) > 0);
            b.field(Constants.FIELD_FLAG_FORWARDED, (flags & MailMessage.FLAG_FORWARDED) > 0);
            b.field(Constants.FIELD_FLAG_READ_ACK, (flags & MailMessage.FLAG_READ_ACK) > 0);
            /*
             * Subject
             */
            b.field(Constants.FIELD_SUBJECT, (String) jsonObject.get(Constants.FIELD_SUBJECT));
            b.endObject();
            return b;
        } catch (final IOException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    public void deleteById(final String mailId, final String indexName) throws OXException {
        try {
            /* final DeleteResponse response = */client.prepareDelete(indexName, indexType, mailId).execute().actionGet();
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    public void deleteAll(final String indexName) throws OXException {
        try {
            ensureStarted();
            // client.prepareIndex().setOpType(OpType.)
            // there is an index delete operation
            // http://www.elasticsearch.com/docs/elasticsearch/rest_api/admin/indices/delete_index/

            client.prepareDeleteByQuery(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
            refresh(indexName);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    public OptimizeResponse optimize(final String indexName, final int optimizeToSegmentsAfterUpdate) throws OXException {
        try {
            ensureStarted();
            return client.admin().indices().optimize(new OptimizeRequest(indexName).maxNumSegments(optimizeToSegmentsAfterUpdate)).actionGet();
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static OXException handleRuntimeException(final RuntimeException e) {
        if (e instanceof ElasticSearchException) {
            final ElasticSearchException ese = (ElasticSearchException) e;
            final Throwable cause = ese.getMostSpecificCause();
            final Throwable launder = null == cause ? ese : cause;
            return SmalExceptionCodes.INDEX_FAULT.create(launder, launder.getMessage());
        }
        return SmalExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#addContent(com.openexchange.mail.dataobjects.MailMessage, com.openexchange.session.Session)
     */
    @Override
    public void addContent(final MailMessage mail, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

	@Override
	public void addContents() throws OXException {
		// TODO Auto-generated method stub

	}

}
