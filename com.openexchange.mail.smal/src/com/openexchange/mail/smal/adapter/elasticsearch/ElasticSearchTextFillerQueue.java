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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.action.support.replication.ReplicationType;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.smal.SMALMailAccess;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.text.TextProcessing;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link ElasticSearchTextFillerQueue}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElasticSearchTextFillerQueue implements Runnable {

    private static final org.apache.commons.logging.Log LOG =
        org.apache.commons.logging.LogFactory.getLog(ElasticSearchTextFillerQueue.class);

    private static final TextFiller POISON = new TextFiller(null, null, null, 0, 0, 0);

    private static final int MAX_NUM_CONCURRENT_FILLER_TASKS = Constants.MAX_NUM_CONCURRENT_FILLER_TASKS;

    private final BlockingQueue<TextFiller> queue;

    private final AtomicBoolean keepgoing;

    private final String indexPrefix;

    private final String type;

    private final ElasticSearchAdapter adapter;

    private volatile Future<Object> future;

    /**
     * The container for currently running concurrent filler tasks.
     */
    protected final AtomicReferenceArray<Future<Object>> concurrentFutures;

    /**
     * The counter for concurrent tasks.
     */
    protected final AtomicInteger taskCounter;

    /**
     * Initializes a new {@link ElasticSearchTextFillerQueue}.
     */
    public ElasticSearchTextFillerQueue(final ElasticSearchAdapter adapter) {
        super();
        concurrentFutures = new AtomicReferenceArray<Future<Object>>(new Future[MAX_NUM_CONCURRENT_FILLER_TASKS]);
        taskCounter = new AtomicInteger();
        this.adapter = adapter;
        keepgoing = new AtomicBoolean(true);
        queue = new LinkedBlockingQueue<TextFiller>();
        indexPrefix = Constants.INDEX_NAME_PREFIX;
        type = Constants.INDEX_TYPE;
    }

    /**
     * Checks if it is allowed to submit a further filler task to thread pool.
     * 
     * @return A positive number if it is allowed to submit to pool; otherwise <code>-1</code>
     */
    private int isSubmittable() {
        final int inc = taskCounter.incrementAndGet();
        if (inc > MAX_NUM_CONCURRENT_FILLER_TASKS) {
            taskCounter.decrementAndGet();
            return -1;
        }
        return inc;
    }

    /**
     * Starts consuming from queue.
     */
    public void start() {
        future = SMALServiceLookup.getServiceStatic(ThreadPoolService.class).submit(ThreadPools.task(this));
    }

    /**
     * Stop consuming from queue.
     */
    public void stop() {
        final int length = concurrentFutures.length();
        for (int i = 0; i < length; i++) {
            final Future<Object> f = concurrentFutures.get(i);
            if (null != f) {
                f.cancel(true);
            }
        }
        keepgoing.set(false);
        queue.offer(POISON);
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            LOG.error("Error stopping queue", e.getCause());
        } catch (final TimeoutException e) {
            future.cancel(true);
        }
    }

    /**
     * Adds specified text filler
     * 
     * @param filler The text filler
     */
    public void add(final TextFiller filler) {
        queue.offer(filler);
    }

    /**
     * Adds specified text fillers
     * 
     * @param fillers The text fillers
     */
    public void add(final Collection<TextFiller> fillers) {
        for (final TextFiller filler : fillers) {
            queue.offer(filler);
        }
    }

    @Override
    public void run() {
        try {
            final List<TextFiller> list = new ArrayList<TextFiller>(16);
            while (keepgoing.get()) {
                if (queue.isEmpty()) {
                    final TextFiller next;
                    try {
                        next = queue.take();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    if (POISON == next) {
                        return;
                    }
                    list.add(next);
                }
                queue.drainTo(list);
                final boolean quit = list.remove(POISON);
                if (!list.isEmpty()) {
                    final ThreadPoolService poolService = SMALServiceLookup.getServiceStatic(ThreadPoolService.class);
                    if (null == poolService) {
                        for (final List<TextFiller> fillers : TextFillerGrouper.groupTextFillersByFullName(list)) {
                            handleFillers(fillers);
                        }
                    } else {
                        for (final List<TextFiller> fillers : TextFillerGrouper.groupTextFillersByFullName(list)) {
                            final int taskNum = isSubmittable();
                            if (taskNum > 0) {
                                final Future<Object> f = poolService.submit(ThreadPools.task(new MaxAwareTask(fillers, taskNum)));
                                concurrentFutures.set(taskNum - 1, f);
                            } else { // Caller runs...
                                handleFillers(fillers);
                            }
                        }

                    }
                }
                if (quit) {
                    return;
                }
                list.clear();
            }
        } catch (final Exception e) {
            LOG.error("Failed text filler run.", e);
        }
    }

    /**
     * Handles specified equally-grouped fillers
     * 
     * @param fillers The equally-grouped fillers
     */
    protected void handleFillers(final List<TextFiller> fillers) {
        if (fillers.isEmpty()) {
            return;
        }
        try {
            final TextFiller first = fillers.get(0);
            final int contextId = first.getContextId();
            final int userId = first.getUserId();
            final int accountId = first.getAccountId();
            final String indexName = indexPrefix + contextId;
            final int size = fillers.size();
            /*
             * Request JSON representations of mails via multi-get
             */
            final MultiGetRequest mgr = new MultiGetRequest();
            Map<String, TextFiller> map = new HashMap<String, TextFiller>(size);
            for (final TextFiller filler : fillers) {
                final String uuid = filler.getUuid();
                mgr.add(indexName, type, uuid);
                map.put(uuid, filler);
            }
            final TransportClient client = adapter.getClient();
            final Map<String, Map<String, Object>> jsonObjects = new HashMap<String, Map<String, Object>>(size);
            for (final Iterator<MultiGetItemResponse> iter = client.multiGet(mgr).actionGet().iterator(); iter.hasNext();) {
                final GetResponse getResponse = iter.next().getResponse();
                if (null != getResponse) {
                    final Map<String, Object> jsonObject = getResponse.getSource();
                    if (checkJSONData(jsonObject)) {
                        jsonObjects.put((String) jsonObject.get(Constants.FIELD_ID), jsonObject);
                    } else if (null == jsonObject) { // Missing JSON data for current UUID
                        final TextFiller filler = map.get(getResponse.getId());
                        if (filler.queuedCounter > 0) {
                            // Discard
                        } else {
                            filler.queuedCounter = filler.queuedCounter + 1;
                            queue.offer(filler);
                        }
                    }
                }
            }
            map = null;
            /*
             * Extract text from associated mails and put into JSON data
             */
            MailAccess<?, ?> access = null;
            try {
                access = SMALMailAccess.getUnwrappedInstance(userId, contextId, accountId);
                access.connect(false);
                for (final TextFiller filler : fillers) {
                    final String mailId = filler.getMailId();
                    final Map<String, Object> jsonObject = jsonObjects.get(mailId);
                    if (null != jsonObject) {
                        try {
                            final String text =
                                TextProcessing.extractTextFrom(access.getMessageStorage().getMessage(filler.getFullName(), mailId, false));
                            jsonObject.put(Constants.FIELD_BODY, text);
                        } catch (final Exception e) {
                            LOG.error("Text could not be extracted from: " + filler, e);
                            jsonObject.put(Constants.FIELD_BODY, "");
                        }
                    }
                }
            } finally {
                SMALMailAccess.closeUnwrappedInstance(access);
                access = null;
            }
            /*
             * Push them to index with a bulk request
             */
            final BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (final Map<String, Object> jsonObject : jsonObjects.values()) {
                final IndexRequestBuilder irb = client.prepareIndex(indexName, type, (String) jsonObject.get(Constants.FIELD_UUID));
                irb.setReplicationType(ReplicationType.ASYNC).setOpType(OpType.INDEX).setConsistencyLevel(WriteConsistencyLevel.DEFAULT);
                irb.setSource(jsonObject);
                bulkRequest.add(irb);
            }
            if (bulkRequest.numberOfActions() > 0) {
                bulkRequest.execute().actionGet();
                adapter.refresh(indexName);
            }
        } catch (final OXException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        } catch (final ElasticSearchException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        } catch (final RuntimeException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        }
    }

    private static boolean checkJSONData(final Map<String, Object> jsonObject) {
        return null != jsonObject && !jsonObject.containsKey(Constants.FIELD_BODY);
    }

    private final class MaxAwareTask implements Task<Object> {

        private final List<TextFiller> fillers;

        private final int taskNum;

        protected MaxAwareTask(final List<TextFiller> fillers, final int taskNum) {
            super();
            this.fillers = fillers;
            this.taskNum = taskNum;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            // Nope
        }

        @Override
        public void beforeExecute(final Thread t) {
            // Nope
        }

        @Override
        public void afterExecute(final Throwable t) {
            concurrentFutures.set(taskNum - 1, null);
            taskCounter.decrementAndGet();
        }

        @Override
        public Object call() throws Exception {
            handleFillers(fillers);
            return null;
        }

    }

}
