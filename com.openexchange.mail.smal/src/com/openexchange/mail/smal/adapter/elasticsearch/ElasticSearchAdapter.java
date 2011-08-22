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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.adapter.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.elasticsearch.action.delete.DeleteResponse;
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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.RemoteTransportException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.SMALExceptionCodes;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.text.TextProcessing;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link ElasticSearchAdapter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElasticSearchAdapter implements IndexAdapter {

    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ElasticSearchAdapter.class));

    /**
     * The special header for extracted ElkasticSearch UUID.
     */
    private static final String X_ELASTIC_SEARCH_UUID = "X-ElasticSearch-UUID";

    protected volatile TransportClient client;

    private final String clusterName;

    private final String indexNamePrefix;

    private final String indexType;

    protected final AtomicBoolean syncFlag;

    /**
     * Initializes a new {@link ElasticSearchAdapter}.
     */
    public ElasticSearchAdapter() {
        super();
        syncFlag = new AtomicBoolean();
        clusterName = Constants.CLUSTER_NAME;
        indexNamePrefix = Constants.INDEX_NAME_PREFIX;
        indexType = Constants.INDEX_TYPE;
    }

    @Override
    public void start() throws OXException {
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
            client.addTransportAddress(new InetSocketTransportAddress("192.168.32.36", 9300));
            clusterInfo();
        } catch (final NoNodeAvailableException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final ElasticSearchException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final Exception e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (null != client) {
            client.close();
            client = null;
        }
    }

    private void ensureStarted() {
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
            throw SMALExceptionCodes.INDEX_FAULT.create(ex, ex.getMessage());
        } catch (final RemoteTransportException e) {
            final Throwable cause = e.getMostSpecificCause();
            if (!(cause instanceof IndexAlreadyExistsException)) {
                throw SMALExceptionCodes.INDEX_FAULT.create(cause, cause.getMessage());
            }
            LOG.info("Index \"" + indexName + "\" already exists.");
        } catch (final Exception ex) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(ex, ex.getMessage());
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
            throw SMALExceptionCodes.INDEX_FAULT.create(ex, ex.getMessage());
        } catch (final RemoteTransportException e) {
            final Throwable cause = e.getMostSpecificCause();
            throw SMALExceptionCodes.INDEX_FAULT.create(cause, cause.getMessage());
        } catch (final Exception ex) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(ex, ex.getMessage());
        }
    }

    @Override
    public boolean containsFolder(final String fullName, final int accountId, final Session session) throws OXException {
        ensureStarted();
        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER, session.getUserId()));
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
        final SearchResponse rsp = builder.execute().actionGet();
        return (rsp.getHits().getTotalHits() > 0);
    }

    @Override
    public void deleteMessages(final Collection<String> mailIds, final String fullName, final int accountId, final Session session) throws OXException {
        ensureStarted();
        if (null == mailIds || mailIds.isEmpty()) {
            return;
        }
        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER, session.getUserId()));
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
        client.prepareDeleteByQuery(indexName).setTypes(indexType).setQuery(boolQuery).execute().actionGet();
        refresh(indexName);
    }

    @Override
    public List<MailMessage> getMessages(final String[] mailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int accountId, final Session session) throws OXException {
        ensureStarted();
        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (null != mailIds) {
            if (0 <= mailIds.length) {
                return Collections.<MailMessage> emptyList();
            }
            boolQuery.must(QueryBuilders.inQuery(Constants.FIELD_ID, mailIds));
        }
        boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_USER, session.getUserId()));
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
        builder.setSize(null == mailIds ? 100000 : mailIds.length);
        /*
         * Perform search
         */
        final SearchResponse rsp = builder.execute().actionGet();
        final SearchHit[] docs = rsp.getHits().getHits();
        final List<MailMessage> mails = new ArrayList<MailMessage>(docs.length);
        final MailFields mailFields = new MailFields(fields);
        for (final SearchHit sd : docs) {
            // to get explanation you'll need to enable this when querying:
            // System.out.println(sd.getExplanation().toString());

            // if we use in mapping: "_source" : {"enabled" : false}
            // we need to include all necessary fields in query and then to use doc.getFields()
            // instead of doc.getSource()
            final MailMessage mail = readDoc(sd.getSource(), mailFields);
            mail.setHeader(X_ELASTIC_SEARCH_UUID, sd.getId());
            mails.add(mail);
        }
        return mails;
    }

    @Override
    public List<MailMessage> search(final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final Session session) throws OXException {
        ensureStarted();
        try {
            final QueryBuilder queryBuilder = SearchTerm2Query.searchTerm2Query(searchTerm);
            /*
             * Compose search request
             */
            final SearchRequestBuilder builder = client.prepareSearch(indexNamePrefix + session.getContextId()).setTypes(indexType);
            // builder.addSort("createdAt", SortOrder.DESC);
            // builder.setFrom(page * hitsPerPage).setSize(hitsPerPage);
            builder.setSize(Integer.MAX_VALUE);
            builder.setQuery(queryBuilder);
            builder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
            builder.setExplain(true);
            builder.setOperationThreading(SearchOperationThreading.THREAD_PER_SHARD);
            /*
             * Perform search
             */
            final SearchResponse rsp = builder.execute().actionGet();
            final SearchHit[] docs = rsp.getHits().getHits();
            final List<MailMessage> mails = new ArrayList<MailMessage>(docs.length);
            final MailFields mailFields = new MailFields(true);
            for (final SearchHit sd : docs) {
                // to get explanation you'll need to enable this when querying:
                // System.out.println(sd.getExplanation().toString());

                // if we use in mapping: "_source" : {"enabled" : false}
                // we need to include all necessary fields in query and then to use doc.getFields()
                // instead of doc.getSource()
                final MailMessage mail = readDoc(sd.getSource(), mailFields);
                mail.setHeader(X_ELASTIC_SEARCH_UUID, sd.getId());
                mails.add(mail);
            }
            return mails;
        } catch (final ElasticSearchException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        }
    }

    private static MailMessage readDoc(final Map<String, Object> source, final MailFields fields) throws OXException {
        try {
            final MailMessage mail = new IDMailMessage(null, null);
            if (null != source) {
                mail.setMailId((String) source.get(Constants.FIELD_ID));
                mail.setFolder((String) source.get(Constants.FIELD_FULL_NAME));
                mail.setAccountId(((Integer) source.get(Constants.FIELD_ACCOUNT_ID)).intValue());
                if (fields.contains(MailField.SUBJECT)) {
                    mail.setSubject((String) source.get(Constants.FIELD_SUBJECT));
                }
                if (fields.contains(MailField.SIZE)) {
                    mail.setSize(((Long) source.get(Constants.FIELD_ACCOUNT_ID)).longValue());
                }
                if (fields.contains(MailField.RECEIVED_DATE)) {
                    final long value = ((Long) source.get(Constants.FIELD_RECEIVED_DATE)).longValue();
                    mail.setReceivedDate(new Date(value));
                }
                if (fields.contains(MailField.SENT_DATE)) {
                    final long value = ((Long) source.get(Constants.FIELD_SENT_DATE)).longValue();
                    mail.setReceivedDate(new Date(value));
                }
                if (fields.contains(MailField.FLAGS)) {
                    int flags = 0;
                    if (((Boolean) source.get(Constants.FIELD_FLAG_ANSWERED)).booleanValue()) {
                        flags |= MailMessage.FLAG_ANSWERED;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_DELETED)).booleanValue()) {
                        flags |= MailMessage.FLAG_DELETED;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_DRAFT)).booleanValue()) {
                        flags |= MailMessage.FLAG_DRAFT;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_FLAGGED)).booleanValue()) {
                        flags |= MailMessage.FLAG_FLAGGED;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_FORWARDED)).booleanValue()) {
                        flags |= MailMessage.FLAG_FORWARDED;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_READ_ACK)).booleanValue()) {
                        flags |= MailMessage.FLAG_READ_ACK;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_SEEN)).booleanValue()) {
                        flags |= MailMessage.FLAG_SEEN;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_SPAM)).booleanValue()) {
                        flags |= MailMessage.FLAG_SPAM;
                    }
                    if (((Boolean) source.get(Constants.FIELD_FLAG_USER)).booleanValue()) {
                        flags |= MailMessage.FLAG_USER;
                    }
                    mail.setFlags(flags);
                }
                if (fields.contains(MailField.FROM)) {
                    final String[] sAddrs = (String[]) source.get(Constants.FIELD_FROM);
                    for (final String sAddr : sAddrs) {
                        try {
                            mail.addFrom(new QuotedInternetAddress(sAddr));
                        } catch (final AddressException e) {
                            mail.addFrom(new PlainTextAddress(sAddr));
                        }
                    }
                }
                if (fields.contains(MailField.TO)) {
                    final String[] sAddrs = (String[]) source.get(Constants.FIELD_TO);
                    for (final String sAddr : sAddrs) {
                        try {
                            mail.addFrom(new QuotedInternetAddress(sAddr));
                        } catch (final AddressException e) {
                            mail.addFrom(new PlainTextAddress(sAddr));
                        }
                    }
                }
                if (fields.contains(MailField.CC)) {
                    final String[] sAddrs = (String[]) source.get(Constants.FIELD_CC);
                    for (final String sAddr : sAddrs) {
                        try {
                            mail.addFrom(new QuotedInternetAddress(sAddr));
                        } catch (final AddressException e) {
                            mail.addFrom(new PlainTextAddress(sAddr));
                        }
                    }
                }
                if (fields.contains(MailField.BCC)) {
                    final String[] sAddrs = (String[]) source.get(Constants.FIELD_BCC);
                    for (final String sAddr : sAddrs) {
                        try {
                            mail.addFrom(new QuotedInternetAddress(sAddr));
                        } catch (final AddressException e) {
                            mail.addFrom(new PlainTextAddress(sAddr));
                        }
                    }
                }
            }
            return mail;
        } catch (final ElasticSearchException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Logs some cluster information.
     */
    public void clusterInfo() {
        ensureStarted();
        final NodesInfoResponse rsp = client.admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet();
        final StringBuilder sb =
            new StringBuilder(32).append("Cluster: ").append(rsp.getClusterName()).append(". Active nodes: ").append(
                rsp.getNodesMap().keySet().size());
        LOG.info(sb.toString());
    }

    /**
     * Checks if specified index exists.
     * 
     * @param indexName The index name
     * @return <code>true</code> if index exists; otherwise <code>false</code>
     */
    public boolean indexExists(final String indexName) {
        ensureStarted();
        final ImmutableMap<String, IndexMetaData> map =
            client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().getIndices();
        return map.containsKey(indexName);
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
        final CountResponse response = client.prepareCount(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        return response.getCount();
    }

    @Override
    public void add(final MailMessage mail, final Session session) throws OXException {
        ensureStarted();
        try {
            final String indexName = indexNamePrefix + session.getContextId();
            final String id = UUID.randomUUID().toString();
            final IndexRequestBuilder irb = client.prepareIndex(indexName, indexType, id);
            irb.setReplicationType(ReplicationType.ASYNC);
            irb.setOpType(OpType.CREATE);
            irb.setConsistencyLevel(WriteConsistencyLevel.DEFAULT).setSource(
                createDoc(id, mail, mail.getAccountId(), session, System.currentTimeMillis()));
            irb.execute().actionGet();
            refresh(indexName);
        } catch (final ElasticSearchException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        }
    }

    @Override
    public void add(final Collection<MailMessage> mails, final Session session) throws OXException {
        ensureStarted();
        if (null == mails || mails.isEmpty()) {
            return;
        }
        final String indexName = indexNamePrefix + session.getContextId();
        final BulkRequestBuilder bulkRequest = client.prepareBulk();
        final long stamp = System.currentTimeMillis();
        for (final MailMessage mail : mails) {
            final String id = UUID.randomUUID().toString();
            final IndexRequestBuilder irb = client.prepareIndex(indexName, indexType, id);
            irb.setReplicationType(ReplicationType.ASYNC);
            irb.setOpType(OpType.CREATE);
            irb.setSource(createDoc(id, mail, mail.getAccountId(), session, stamp));
            bulkRequest.add(irb);
        }
        if (bulkRequest.numberOfActions() > 0) {
            bulkRequest.execute().actionGet();
            refresh(indexName);
        }
    }

    private XContentBuilder createDoc(final String id, final MailMessage mail, final int accountId, final Session session, final long stamp) throws OXException {
        try {
            final XContentBuilder b = JsonXContent.unCachedContentBuilder().startObject();
            b.field(Constants.FIELD_TIMESTAMP, stamp);
            /*
             * Body content
             */
            {
                b.field(Constants.FIELD_BODY, TextProcessing.getTextFrom(mail, session));
            }
            /*
             * Identifiers
             */
            b.field(Constants.FIELD_UUID, id);
            b.field(Constants.FIELD_USER, session.getUserId());
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
            createFlags(mail, b);
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
        final String[] ret = new String[addrs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = addrs[i].toUnicodeString();
        }
        return ret;
    }

    @Override
    public void change(final Collection<MailMessage> mails, final Session session) throws OXException {
        ensureStarted();
        if (null == mails || mails.isEmpty()) {
            return;
        }
        final String indexName = indexNamePrefix + session.getContextId();
        /*
         * Request ids
         */
        final String[] uuids;
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
                    first = false;
                } else {
                    mailIds[i] = iterator.next().getMailId();
                }
            }
            final List<MailMessage> list = getMessages(mailIds, fullName, null, null, new MailFields(MailField.ID).toArray(), accountId, session);
            uuids = new String[list.size()];
            for (int i = 0; i < uuids.length; i++) {
                uuids[i] = list.get(i).getFirstHeader(X_ELASTIC_SEARCH_UUID);
            }
        }
        int i = 0;
        final BulkRequestBuilder bulkRequest = client.prepareBulk();
        final long stamp = System.currentTimeMillis();
        for (final MailMessage mail : mails) {
            final String uuid = uuids[i++];
            final IndexRequestBuilder irb = client.prepareIndex(indexName, indexType, uuid);
            irb.setReplicationType(ReplicationType.ASYNC);
            irb.setOpType(OpType.INDEX);
            irb.setSource(changeDoc(uuid, mail, mail.getAccountId(), session, stamp));
            bulkRequest.add(irb);
        }
        if (bulkRequest.numberOfActions() > 0) {
            bulkRequest.execute().actionGet();
            refresh(indexName);
        }
    }

    private XContentBuilder changeDoc(final String id, final MailMessage mail, final int accountId, final Session session, final long stamp) throws OXException {
        try {
            final XContentBuilder b = JsonXContent.unCachedContentBuilder().startObject();
            b.field(Constants.FIELD_TIMESTAMP, stamp);
            /*
             * Identifiers
             */
            b.field(Constants.FIELD_UUID, id);
            // b.field(Constants.FIELD_USER, session.getUserId());
            // b.field(Constants.FIELD_ACCOUNT_ID, accountId);
            // b.field(Constants.FIELD_FULL_NAME, mail.getFolder());
            // b.field(Constants.FIELD_ID, mail.getMailId());
            /*
             * Write flags
             */
            createFlags(mail, b);
            b.endObject();
            return b;
        } catch (final IOException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static void createFlags(final MailMessage mail, final XContentBuilder b) throws IOException {
        final int flags = mail.getFlags();
        b.field(Constants.FIELD_FLAG_ANSWERED, (flags & MailMessage.FLAG_ANSWERED) > 0);
        b.field(Constants.FIELD_FLAG_DELETED, (flags & MailMessage.FLAG_DELETED) > 0);
        b.field(Constants.FIELD_FLAG_DRAFT, (flags & MailMessage.FLAG_DRAFT) > 0);
        b.field(Constants.FIELD_FLAG_FLAGGED, (flags & MailMessage.FLAG_FLAGGED) > 0);
        b.field(Constants.FIELD_FLAG_SEEN, (flags & MailMessage.FLAG_SEEN) > 0);
        b.field(Constants.FIELD_FLAG_USER, (flags & MailMessage.FLAG_USER) > 0);
        b.field(Constants.FIELD_FLAG_SPAM, (flags & MailMessage.FLAG_SPAM) > 0);
        b.field(Constants.FIELD_FLAG_FORWARDED, (flags & MailMessage.FLAG_FORWARDED) > 0);
        b.field(Constants.FIELD_FLAG_READ_ACK, (flags & MailMessage.FLAG_READ_ACK) > 0);
    }

    public void deleteById(final String mailId, final String indexName) {
        final DeleteResponse response = client.prepareDelete(indexName, indexType, mailId).execute().actionGet();
    }

    public void deleteAll(final String indexName) {
        ensureStarted();
        // client.prepareIndex().setOpType(OpType.)
        // there is an index delete operation
        // http://www.elasticsearch.com/docs/elasticsearch/rest_api/admin/indices/delete_index/

        client.prepareDeleteByQuery(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        refresh(indexName);
    }

    public OptimizeResponse optimize(final String indexName, final int optimizeToSegmentsAfterUpdate) {
        ensureStarted();
        return client.admin().indices().optimize(new OptimizeRequest(indexName).maxNumSegments(optimizeToSegmentsAfterUpdate)).actionGet();
    }

    @Override
    public boolean sync(final String fullName, final int accountId, final Session session) throws OXException {
        ensureStarted();
        if (!syncFlag.compareAndSet(false, true)) {
            return false;
        }
        /*
         * Start syncer
         */
        SMALServiceLookup.getInstance().getService(ThreadPoolService.class).submit(
            ThreadPools.task(new SyncRunnable(fullName, accountId, session)),
            CallerRunsBehavior.getInstance());
        return true;
    }

    protected static final MailField[] FIELDS = new MailFields(MailField.ID, MailField.FLAGS).toArray();

    private final class SyncRunnable implements Runnable {

        private final String fullName;

        private final int accountId;

        private final Session session;

        /**
         * Initializes a new {@link ElasticSearchAdapter.SyncRunnable}.
         */
        public SyncRunnable(final String fullName, final int accountId, final Session session) {
            super();
            this.fullName = fullName;
            this.accountId = accountId;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
                    MailAccess.getInstance(session, accountId);
                mailAccess.connect(false);
                try {
                    final MailMessage[] mails =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            IndexRange.NULL,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.ASC,
                            FIELDS);

                } finally {
                    mailAccess.close(true);
                }
            } catch (final Exception e) {
                LOG.error("Synchronizing \"" + fullName + "\" mails with index failed.", e);
            } finally {
                syncFlag.set(false);
            }
        }

    } // End of SyncRunnable class
}
