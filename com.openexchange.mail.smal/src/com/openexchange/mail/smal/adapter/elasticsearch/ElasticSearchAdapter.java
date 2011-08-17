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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequest;
import org.elasticsearch.action.admin.indices.optimize.OptimizeResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.action.index.IndexRequestBuilder;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.SMALExceptionCodes;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
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

    private static final char DELIM = '/';

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
    }

    @Override
    public void stop() {
        if (null != client) {
            client.close();
            client = null;
        }
    }

    @Override
    public List<MailMessage> search(final SearchTerm<?> searchTerm, final Session session) throws OXException {
        try {
            final QueryBuilder queryBuilder = SearchTerm2Query.searchTerm2Query(searchTerm);
            /*
             * Compose search request
             */
            final SearchRequestBuilder builder = client.prepareSearch(indexNamePrefix + session.getContextId());
            // builder.addSort("createdAt", SortOrder.DESC);
            // builder.setFrom(page * hitsPerPage).setSize(hitsPerPage);
            builder.setQuery(queryBuilder);
            /*
             * Perform search
             */
            final SearchResponse rsp = builder.execute().actionGet();
            final SearchHit[] docs = rsp.getHits().getHits();
            final List<MailMessage> mails = new ArrayList<MailMessage>(docs.length);
            final MailPath helper = new MailPath();
            for (final SearchHit sd : docs) {
                // to get explanation you'll need to enable this when querying:
                // System.out.println(sd.getExplanation().toString());

                // if we use in mapping: "_source" : {"enabled" : false}
                // we need to include all necessary fields in query and then to use doc.getFields()
                // instead of doc.getSource()
                final MailMessage mail = readDoc(sd.getSource(), sd.getId(), helper);
                mails.add(mail);
            }
            return mails;
        } catch (final ElasticSearchException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        }
    }

    private static MailMessage readDoc(final Map<String, Object> source, final String idAsStr, final MailPath helper) throws OXException {
        helper.setMailIdentifierString(idAsStr);
        final MailMessage mail = new IDMailMessage(helper.getMailID(), helper.getFolder());
        mail.setAccountId(helper.getAccountId());
        if (null != source) {
            // tweet.setText((String) source.get("tweetText"));
            // tweet.setCreatedAt(Helper.toDateNoNPE((String) source.get("createdAt")));
            // tweet.setFromUserId((Integer) source.get("fromUserId"));
        }
        return mail;
    }

    /**
     * Logs some cluster information.
     */
    public void clusterInfo() {
        final NodesInfoResponse rsp = client.admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet();
        final StringBuilder sb =
            new StringBuilder(32).append("Cluster: ").append(rsp.getClusterName()).append(". Active nodes: ").append(
                rsp.getNodesMap().keySet());
        LOG.info(sb.toString());
    }

    /**
     * Checks if specified index exists.
     * 
     * @param indexName The index name
     * @return <code>true</code> if index exists; otherwise <code>false</code>
     */
    public boolean indexExists(final String indexName) {
        final ImmutableMap<String, IndexMetaData> map =
            client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().getIndices();
        return map.containsKey(indexName);
    }

    

    public void createIndex(final String indexName) {
        /*
         * Create the index
         */
        try {
            client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
            LOG.info("Index \"" + indexName + "\" successfully created.");
            // waitForYellow();
            /*
             * Create the mapping definition for a mail
             */
            Mapping.createMailMapping(client, indexName);
        } catch (final Exception ex) {
            LOG.info("Index \"" + indexName + "\" already exists.");
        }
    }

    public void saveCreateIndex(final String indexName, final boolean log) {
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
     * Waits for yellow status.
     */
    void waitForYellow(final String indexName) {
        client.admin().cluster().health(new ClusterHealthRequest(indexName).waitForYellowStatus()).actionGet();
    }

    /**
     * Waits for green status.
     */
    void waitForGreen(final String indexName) {
        client.admin().cluster().health(new ClusterHealthRequest(indexName).waitForGreenStatus()).actionGet();
    }

    /**
     * Explicitly refresh one or more indices (making the content indexed since the last refresh searchable).
     */
    public void refresh(final String indexName) {
        final RefreshResponse rsp = client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }

    /**
     * Gets the number of mails held in index.
     * 
     * @return The number of mails
     */
    public long countAll(final String indexName) {
        final CountResponse response = client.prepareCount(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        return response.getCount();
    }

    @Override
    public void add(final MailMessage mail, final Session session) throws OXException {
        try {
            final String id = new MailPath(mail.getAccountId(), mail.getFolder(), mail.getMailId()).toString();
            final XContentBuilder b = createDoc(id, mail);
            final IndexRequestBuilder irb =
                client.prepareIndex(indexNamePrefix + session.getContextId(), indexType, id).setConsistencyLevel(WriteConsistencyLevel.DEFAULT).setSource(b);
            irb.execute().actionGet();
        } catch (final ElasticSearchException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        }
    }

    @Override
    public void add(final MailMessage[] mails, final Session session) throws OXException {
        final BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (final MailMessage mail : mails) {
            // bulkRequest.add(client.prepareIndex(indexName, indexType, "1")
            // .setSource(jsonBuilder()
            // .startObject()
            // .field("user", "kimchy")
            // .field("postDate", new Date())
            // .field("message", "trying out Elastic Search")
            // .endObject()
            // )
            // );

        }

    }

    public XContentBuilder createDoc(final String id, final MailMessage mail) {
        try {
            final XContentBuilder b = JsonXContent.unCachedContentBuilder().startObject();
            b.field("subject", mail.getSubject());
            b.field("from", mail.getFrom()[0]);
            b.field("id", id);
            b.endObject();
            return b;
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void deleteById(final String mailId, final String indexName) {
        final DeleteResponse response = client.prepareDelete(indexName, indexType, mailId).execute().actionGet();
    }

    public void deleteAll(final String indexName) {
        // client.prepareIndex().setOpType(OpType.)
        // there is an index delete operation
        // http://www.elasticsearch.com/docs/elasticsearch/rest_api/admin/indices/delete_index/

        client.prepareDeleteByQuery(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        refresh(indexName);
    }

    public OptimizeResponse optimize(final String indexName, final int optimizeToSegmentsAfterUpdate) {
        return client.admin().indices().optimize(new OptimizeRequest(indexName).maxNumSegments(optimizeToSegmentsAfterUpdate)).actionGet();
    }

    @Override
    public boolean sync(final String fullName, final int accountId, final Session session) throws OXException {
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
                final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(session, accountId);
                mailAccess.connect(false);
                try {
                    final MailMessage[] mails = mailAccess.getMessageStorage().getAllMessages(fullName, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS);
                    
                    
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
