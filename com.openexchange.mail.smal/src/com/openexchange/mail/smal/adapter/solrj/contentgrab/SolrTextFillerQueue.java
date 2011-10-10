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

package com.openexchange.mail.smal.adapter.solrj.contentgrab;

import static com.openexchange.mail.smal.adapter.IndexAdapters.detectLocale;
import static com.openexchange.mail.smal.adapter.solrj.SolrUtils.rollback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.elasticsearch.ElasticSearchException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.smal.SMALExceptionCodes;
import com.openexchange.mail.smal.SMALMailAccess;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.solrj.SolrConstants;
import com.openexchange.mail.smal.adapter.solrj.SolrUtils;
import com.openexchange.mail.smal.adapter.solrj.management.CommonsHttpSolrServerManagement;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link SolrTextFillerQueue}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrTextFillerQueue implements Runnable, SolrConstants {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SolrTextFillerQueue.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final Future<Object> PLACEHOLDER = new Future<Object>() {

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }
    };

    private static final TextFiller POISON = new TextFiller(null, null, null, 0, 0, 0);

    private final Lock lock;

    private final Condition condition;

    private final BlockingQueue<TextFiller> queue;

    private final AtomicBoolean keepgoing;

    private volatile Future<Object> future;

    /**
     * The container for currently running concurrent filler tasks.
     */
    protected final AtomicReferenceArray<Future<Object>> concurrentFutures;

    private final String simpleName;

    private final int maxNumConcurrentFillerTasks;

    private final CommonsHttpSolrServerManagement serverManagement;

    /**
     * Initializes a new {@link SolrTextFillerQueue}.
     */
    public SolrTextFillerQueue(final CommonsHttpSolrServerManagement serverManagement) {
        super();
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.serverManagement = serverManagement;
        maxNumConcurrentFillerTasks = MAX_NUM_CONCURRENT_FILLER_TASKS;
        concurrentFutures = new AtomicReferenceArray<Future<Object>>(maxNumConcurrentFillerTasks);
        keepgoing = new AtomicBoolean(true);
        queue = new LinkedBlockingQueue<TextFiller>();
        simpleName = getClass().getSimpleName();
    }

    /**
     * Signal to start consume possible available elements from queue.
     */
    public void signalConsume() {
        lock.lock();
        try {
            if (DEBUG) {
                LOG.debug("Wating on condition...");
            }
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Starts consuming from queue.
     */
    public void start() {
        future = SMALServiceLookup.getServiceStatic(ThreadPoolService.class).submit(ThreadPools.task(this, simpleName));
    }

    /**
     * Stop consuming from queue.
     */
    public void stop() {
        final int length = concurrentFutures.length();
        for (int i = 0; i < length; i++) {
            final Future<Object> f = concurrentFutures.get(i);
            if (null != f && PLACEHOLDER != f) {
                f.cancel(true);
            }
        }
        lock.lock();
        try {
            if (DEBUG) {
                LOG.debug("Wating on condition...");
            }
            condition.signalAll();
        } finally {
            lock.unlock();
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
    public void add(final TextFiller filler, final boolean signalConsume) {
        if (queue.offer(filler) && DEBUG) {
			LOG.debug("SolrTextFillerQueue.add() Added text filler (queue-size=" + queue.size() + "): " + filler);
			if (signalConsume) {
	            signalConsume();
	        }
		}
    }

    /**
     * Adds specified text fillers
     * 
     * @param fillers The text fillers
     */
    public void add(final Collection<TextFiller> fillers, final boolean signalConsume) {
        for (final TextFiller filler : fillers) {
            add(filler, false);
        }
        if (signalConsume) {
            signalConsume();
        }
    }

    @Override
    public void run() {
        try {
            final List<TextFiller> list = new ArrayList<TextFiller>(16);
            while (keepgoing.get()) {
            	lock.lock();
            	try {
            		if (DEBUG) {
                        LOG.debug("Waiting on condition...");
                    }
                    condition.await(1, TimeUnit.HOURS);
                    if (!keepgoing.get()) {
                        return;
                    }
				} finally {
					lock.unlock();
				}
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
                    if (DEBUG) {
                        LOG.debug("Processing " + list.size() + " text fillers from queue");
                    }
                    for (final List<TextFiller> fillers : TextFillerGrouper.groupTextFillersByFullName(list)) {
                        handleFillers(fillers);
                    }
                }
                if (quit) {
                    return;
                }
                list.clear();
            }
        } catch (final InterruptedException e) {
            LOG.error("Interrupted text filler run.", e);
        } catch (final Exception e) {
            LOG.error("Failed text filler run.", e);
        }
    }

    /**
     * Handles specified equally-grouped fillers
     * 
     * @param groupedFillers The equally-grouped fillers
     * @param threadDesc The thread description
     * @throws InterruptedException If thread is interrupted
     */
    protected void handleFillers(final List<TextFiller> groupedFillers) throws InterruptedException {
        final ThreadPoolService poolService = SMALServiceLookup.getServiceStatic(ThreadPoolService.class);
        final int size = groupedFillers.size();
        final int configuredBlockSize = MAX_FILLER_CHUNK;
        if (size <= configuredBlockSize) {
            scheduleFillers(groupedFillers, poolService);
        } else {
            int fromIndex = 0;
            while (fromIndex < size) {
                final int toIndex = fromIndex + configuredBlockSize;
                if (toIndex > size) {
                    if (DEBUG) {
                        LOG.debug("Scheduling " + (size - fromIndex) + " text fillers...");
                    }
                    scheduleFillers(groupedFillers.subList(fromIndex, size), poolService);
                    fromIndex = size;
                } else {
                    if (DEBUG) {
                        LOG.debug("Scheduling " + (size - toIndex) + " text fillers...");
                    }
                    scheduleFillers(groupedFillers.subList(fromIndex, toIndex), poolService);
                    fromIndex = toIndex;
                }
            }
        }
    }

    private void scheduleFillers(final List<TextFiller> groupedFillersSublist, final ThreadPoolService poolService) throws InterruptedException {
        if (null == poolService) {
            /*
             * Caller runs because thread pool is absent
             */
            handleFillersSublist(groupedFillersSublist, simpleName);
        } else {
            int index = -1;
            for (int i = 0; i < maxNumConcurrentFillerTasks; i++) {
                if (concurrentFutures.compareAndSet(i, null, PLACEHOLDER)) {
                    index = i; // Found a free slot
                    break;
                }
            }
            if (index < 0) {
                /*
                 * Caller runs because other worker threads are busy
                 */
                handleFillersSublist(groupedFillersSublist, simpleName);
            } else {
                /*
                 * Submit to a free worker thread
                 */
                final MaxAwareTask task = new MaxAwareTask(groupedFillersSublist, index);
                final Future<Object> f = poolService.submit(ThreadPools.task(task));
                concurrentFutures.set(index, f);
                task.start();
            }
        }
    }

    /**
     * Handles specified chunk of equally-grouped fillers
     * 
     * @param fillersChunk The chunk of equally-grouped fillers
     * @param threadDesc The thread description
     * @throws InterruptedException If thread is interrupted
     */
    protected void handleFillersSublist(final List<TextFiller> fillersChunk, final String threadDesc) throws InterruptedException {
        if (fillersChunk.isEmpty()) {
            return;
        }
        try {
            /*
             * Handle fillers in chunks
             */
            final int size = fillersChunk.size();
            final int configuredBlockSize = MAX_FILLER_CHUNK;
            if (size <= configuredBlockSize) {
                pushMailTextBodies(fillersChunk, threadDesc);
            } else {
                final Thread thread = Thread.currentThread();
                int fromIndex = 0;
                while (fromIndex < size) {
                    if (thread.isInterrupted()) {
                        throw new InterruptedException("Text filler thread interrupted");
                    }
                    int toIndex = fromIndex + configuredBlockSize;
                    if (toIndex > size) {
                        toIndex = size;
                    }
                    pushMailTextBodies(fillersChunk.subList(fromIndex, toIndex), threadDesc);
                    fromIndex = toIndex;
                }
            }
        } catch (final OXException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        } catch (final ElasticSearchException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        } catch (final RuntimeException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        }
    }

    private void pushMailTextBodies(final List<TextFiller> fillers, final String threadDesc) throws OXException, InterruptedException {
        if (fillers.isEmpty()) {
            return;
        }
        final TextFiller first = fillers.get(0);
        final int contextId = first.getContextId();
        final int userId = first.getUserId();
        final int accountId = first.getAccountId();
        final int size = fillers.size();
        CommonsHttpSolrServer solrServer = null;
        boolean rollback = false;
        try {
            solrServer =
                serverManagement.getSolrServer(SMALServiceLookup.getServiceStatic(ConfigIndexService.class).getReadOnlyURL(
                    contextId,
                    userId,
                    Types.EMAIL));
            /*
             * Query existing documents
             */
            final Thread thread = Thread.currentThread();
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            final Map<String, SolrDocument> documents;
            {
                final Map<String, TextFiller> map = new HashMap<String, TextFiller>(size);
                final SolrQuery solrQuery;
                {
                    final StringBuilder q = new StringBuilder(2048);
                    for (final TextFiller filler : fillers) {
                        final String uuid = filler.getUuid();
                        map.put(uuid, filler);
                        q.append(" OR ").append(FIELD_UUID).append(':').append(uuid);
                    }
                    q.delete(0, 4);
                    solrQuery = new SolrQuery(q.toString());
                }
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Text filler thread interrupted");
                }
                solrQuery.setRows(Integer.valueOf(size));
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                final int rsize = results.size();
                documents = new HashMap<String, SolrDocument>(rsize);
                for (int i = 0; i < rsize; i++) {
                    final SolrDocument solrDocument = results.get(i);
                    if (checkSolrDocument(solrDocument)) {
                        final String uuid = solrDocument.getFieldValue(FIELD_UUID).toString();
                        documents.put(uuid, solrDocument);
                        map.remove(uuid); // Processed, so remove from map
                    }
                }
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Text filler thread interrupted");
                }
                for (final TextFiller filler : map.values()) {
                    if (filler.queuedCounter > 0) {
                        // Discard
                    } else {
                        filler.queuedCounter = filler.queuedCounter + 1;
                        queue.offer(filler);
                    }
                }
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Text filler thread interrupted");
                }
            }
            /*
             * Empty?
             */
            if (documents.isEmpty()) {
                final long dur = System.currentTimeMillis() - st;
                final StringBuilder sb = new StringBuilder(64);
                sb.append("Thread \"").append(threadDesc);
                sb.append("\" added ").append(0);
                sb.append(" mail bodies in ").append(dur).append("msec");
                sb.append(" from folder \"").append(fillers.get(0).getFullName()).append('"');
                sb.append(" for account ").append(accountId);
                sb.append(" of user ").append(userId);
                sb.append(" in context ").append(contextId);
                LOG.debug(sb.toString());
                return;
            }
            /*
             * Initialize batch addition
             */
            final List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>(documents.size());
            /*
             * Iterate fillers & extract text
             */
            grabTextFor(fillers, contextId, userId, accountId, documents, inputDocuments);
            /*
             * Add to index
             */
            if (!inputDocuments.isEmpty()) {
                final int docSize = inputDocuments.size();
                int off = 0;
                while (off < docSize) {
                    if (thread.isInterrupted()) {
                        throw new InterruptedException("Text filler thread interrupted");
                    }
                    int toIndex = off + 10;
                    if (toIndex > docSize) {
                        toIndex = docSize;
                    }
                    solrServer.add(inputDocuments.subList(off, toIndex));
                    rollback = true;
                    off = toIndex;
                }
                SolrUtils.commitWithTimeout(solrServer);
                if (DEBUG) {
                    final long dur = System.currentTimeMillis() - st;
                    final StringBuilder sb = new StringBuilder(64);
                    sb.append("Thread \"").append(threadDesc);
                    sb.append("\" added ").append(docSize);
                    sb.append(" mail bodies in ").append(dur).append("msec");
                    sb.append(" from folder \"").append(fillers.get(0).getFullName()).append('"');
                    sb.append(" for account ").append(accountId);
                    sb.append(" of user ").append(userId);
                    sb.append(" in context ").append(contextId);
                    LOG.debug(sb.toString());
                }
            } else if (DEBUG) {
                final long dur = System.currentTimeMillis() - st;
                final StringBuilder sb = new StringBuilder(64);
                sb.append("Thread \"").append(threadDesc);
                sb.append("\" added ").append(0);
                sb.append(" mail bodies in ").append(dur).append("msec");
                sb.append(" from folder \"").append(fillers.get(0).getFullName()).append('"');
                sb.append(" for account ").append(accountId);
                sb.append(" of user ").append(userId);
                sb.append(" in context ").append(contextId);
                LOG.debug(sb.toString());
            }
        } catch (final SolrServerException e) {
            rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void grabTextFor(final List<TextFiller> fillers, final int contextId, final int userId, final int accountId, final Map<String, SolrDocument> documents, final List<SolrInputDocument> inputDocuments) throws OXException, InterruptedException {
        MailAccess<?, ?> access = null;
        long st = 0;
        try {
            access = SMALMailAccess.getUnwrappedInstance(userId, contextId, accountId);
            access.connect(false);
            st = System.currentTimeMillis();
            if (DEBUG) {
                LOG.debug("Acquired mail connection at " + st);
            }
            final Thread thread = Thread.currentThread();
            final IMailMessageStorage messageStorage = access.getMessageStorage();
            final String[] container = new String[1];
            // final TextFinder textFinder = new TextFinder();
            for (final TextFiller filler : fillers) {
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Text filler thread interrupted");
                }
                final String uuid = filler.getUuid();
                final SolrDocument solrDocument = documents.get(uuid);
                if (null != solrDocument) {
                    try {
                        final SolrInputDocument inputDocument = new SolrInputDocument();
                        for (final Entry<String, Object> entry : solrDocument.entrySet()) {
                            final String name = entry.getKey();
                            final SolrInputField field = new SolrInputField(name);
                            field.setValue(entry.getValue(), 1.0f);
                            inputDocument.put(name, field);
                        }
                        /*-
                         * Get text
                         * 
                         * --> mime4j
                         */
                        container[0] = filler.getMailId();
                        final String text = messageStorage.getPrimaryContents(filler.getFullName(), container)[0];
                        //final String text = textFinder.getText(messageStorage.getMessage(filler.getFullName(), filler.getMailId(), false));
                        if (null != text) {
                            final Locale locale = detectLocale(text);
                            inputDocument.setField(FIELD_CONTENT_PREFIX + locale.getLanguage(), text);
                        }
                        inputDocument.setField(FIELD_CONTENT_FLAG, Boolean.TRUE);
                        inputDocuments.add(inputDocument);
                        /*
                         * Remove from map
                         */
                        documents.remove(uuid);
                    } catch (final Exception e) {
                        LOG.error("Text could not be extracted from: " + filler, e);
                    }
                }
            }
        } finally {
            SMALMailAccess.closeUnwrappedInstance(access);
            final long dur = System.currentTimeMillis() - st;
            if (DEBUG) {
                LOG.debug("Held mail connection for " + dur + "msec");
            }
            access = null;
        }
    }

    /**
     * Checks specified document if content still needs to be added.
     * 
     * @param solrDocument The document to check
     * @return <code>true</code> if no content is present in document; otherwise <code>false</code> if content was found
     */
    public static boolean checkSolrDocument(final SolrDocument solrDocument) {
        if (null == solrDocument) {
            return false;
        }
        final Boolean contentFlag = (Boolean) solrDocument.getFieldValue(FIELD_CONTENT_FLAG);
        return null == contentFlag || !contentFlag.booleanValue();
    }

    private final class MaxAwareTask implements Task<Object> {

        private final List<TextFiller> fillers;
        private final int indexPos;
        private final CountDownLatch startSignal;

        protected MaxAwareTask(final List<TextFiller> fillers, final int indexPos) {
            super();
            this.startSignal = new CountDownLatch(1);
            this.fillers = fillers;
            this.indexPos = indexPos;
        }

        /**
         * Opens this task for processing.
         */
        protected void start() {
            startSignal.countDown();
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
            // Nope
        }

        @Override
        public Object call() throws Exception {
            try {
                startSignal.await();
                handleFillersSublist(fillers, String.valueOf(indexPos + 1));
                return null;
            } finally {
                concurrentFutures.set(indexPos, null);
            }
        }

    }

}
