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
import java.util.Collection;
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
import org.elasticsearch.client.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONObject;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.adapter.IndexAdapter;

/**
 * {@link ElasticSearchAdapter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElasticSearchAdapter implements IndexAdapter {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ElasticSearchAdapter.class));

    private TransportClient client;

    private final String indexName;

    private final String indexType;

    /**
     * Initializes a new {@link ElasticSearchAdapter}.
     */
    public ElasticSearchAdapter() {
        super();
        indexName = "mail_index";
        indexType = "mail";
    }

    @Override
    public void start() {
        final Builder settingsBuilder = ImmutableSettings.settingsBuilder();
        /*
         * Look-up other nodes in cluster
         */
        settingsBuilder.put("client.transport.sniff", true);
        /*
         * Specify cluster name
         */
        settingsBuilder.put("cluster.name", "ox_cluster");
        /*
         * We act as client only
         */
        settingsBuilder.put("node.data", false);
        /*
         * Create the (transport) client
         */
        client = new TransportClient(settingsBuilder.build());
        /*
         * Create the index
         */
        try {
            client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
            LOG.info("Index \"" + indexName + "\" successfully created.");
        } catch (final Exception ex) {
            LOG.info("Index \"" + indexName + "\" already exists.");
        }
    }

    private void createMailTemplate() {
        final JSONObject mailTemplate = new JSONObject();
        
        
        
        client.admin().indices().preparePutMapping(indexName).setSource(mailTemplate.toString());
    }

    @Override
    public void stop() {
        if (null != client) {
            client.close();
            client = null;
        }
    }

    public XContentBuilder createDoc(final MailMessage mail) {
        try {
            final XContentBuilder b = JsonXContent.unCachedContentBuilder().startObject();
            b.field("subject", mail.getSubject());
            b.field("from", mail.getFrom()[0]);
            b.field("id", mail.getMailId());
            b.endObject();
            return b;
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void clusterInfo() {
        final NodesInfoResponse rsp = client.admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet();
        final StringBuilder sb =
            new StringBuilder(32).append("Cluster: ").append(rsp.getClusterName()).append(". Active nodes: ").append(
                rsp.getNodesMap().keySet());
        LOG.info(sb.toString());
    }

    public boolean indexExists(final String indexName) {
        // make sure node is up to create the index otherwise we get: blocked by: [1/not recovered from gateway];
        // waitForYellow();

        // Map map = client.admin().cluster().health(new ClusterHealthRequest(indexName)).actionGet().getIndices();
        final ImmutableMap<String, IndexMetaData> map =
            client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().getIndices();
        // System.out.println("Index info:" + map);
        return map.containsKey(indexName);
    }

    public void createIndex(final String indexName) {
        // no need for the following because of _default mapping under config
        // String fileAsString = Helper.readInputStream(getClass().getResourceAsStream("tweet.json"));
        // new CreateIndexRequest(indexName).mapping(indexType, fileAsString)
        client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
        // waitForYellow();
    }

    public void saveCreateIndex() {
        saveCreateIndex(indexName, true);
    }

    public void saveCreateIndex(final String name, final boolean log) {
        // if (!indexExists(name)) {
        try {
            createIndex(name);
            if (log) {
                LOG.info("Created index: " + name);
            }
        } catch (final Exception ex) {
            // } else {
            if (log) {
                LOG.info("Index " + indexName + " already exists");
            }
        }
    }

    void waitForYellow() {
        waitForYellow(indexName);
    }

    void waitForYellow(final String name) {
        client.admin().cluster().health(new ClusterHealthRequest(name).waitForYellowStatus()).actionGet();
    }

    void waitForGreen(final String name) {
        client.admin().cluster().health(new ClusterHealthRequest(name).waitForGreenStatus()).actionGet();
    }

    public void refresh() {
        refresh(indexName);
    }

    public void refresh(final Collection<String> indices) {
        final String[] sa = new String[indices.size()];
        int i = 0;
        for (final String index : indices) {
            sa[i++] = index;
        }
        refresh(sa);
    }

    public void refresh(final String... indices) {
        final RefreshResponse rsp = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        // assertEquals(1, rsp.getFailedShards());
    }

    public long countAll() {
        final CountResponse response = client.prepareCount(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        return response.getCount();
    }

    public void feedDoc(final String mailId, final XContentBuilder b) {
        // String getIndexName() = new SimpleDateFormat("yyyyMMdd").format(tw.getCreatedAt());
        final IndexRequestBuilder irb =
            client.prepareIndex(indexName, indexType, mailId).setConsistencyLevel(WriteConsistencyLevel.DEFAULT).setSource(b);
        irb.execute().actionGet();
    }

    public void deleteById(final String mailId) {
        final DeleteResponse response = client.prepareDelete(indexName, indexType, mailId).execute().actionGet();
    }

    public void deleteAll() {
        // client.prepareIndex().setOpType(OpType.)
        // there is an index delete operation
        // http://www.elasticsearch.com/docs/elasticsearch/rest_api/admin/indices/delete_index/

        client.prepareDeleteByQuery(indexName).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        refresh();
    }

    public OptimizeResponse optimize(final int optimizeToSegmentsAfterUpdate) {
        return client.admin().indices().optimize(new OptimizeRequest(indexName).maxNumSegments(optimizeToSegmentsAfterUpdate)).actionGet();
    }
}
