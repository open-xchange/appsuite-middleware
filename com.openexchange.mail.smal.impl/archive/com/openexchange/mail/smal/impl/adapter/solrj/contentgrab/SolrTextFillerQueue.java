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

package com.openexchange.mail.smal.impl.adapter.solrj.contentgrab;

import static com.openexchange.mail.smal.impl.adapter.IndexAdapters.detectLocale;
import static com.openexchange.mail.smal.impl.adapter.solrj.SolrUtils.commitSane;
import static com.openexchange.mail.smal.impl.adapter.solrj.SolrUtils.rollback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.smal.impl.SmalExceptionCodes;
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.adapter.solrj.SolrConstants;
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

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrTextFillerQueue.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final TextFiller POISON = new TextFiller(null, null, null, 0, 0, 0);

    /**
     * The max. running time of 1 minute.
     */
    private static final long MAX_RUNNING_TIME = 60000;

    /**
     * The wait time in milliseconds.
     */
    private static final long WAIT_TIME = 3000;

    private final StampedFuture placeHolder;

    private final BlockingQueue<TextFiller> queue;

    private final AtomicBoolean keepgoing;

    private volatile Future<Object> future;

    /**
     * The container for currently running concurrent filler tasks.
     */
    protected final AtomicReferenceArray<StampedFuture> concurrentFutures;

    private final String simpleName;

    private final int maxNumConcurrentFillerTasks;

    //private final CommonsHttpSolrServerManagement serverManagement;

    private final Gate gate;

    /**
     * Initializes a new {@link SolrTextFillerQueue}.
     */
    public SolrTextFillerQueue(/*final CommonsHttpSolrServerManagement serverManagement*/) {
        super();
        placeHolder = new StampedFuture(null);
        //this.serverManagement = serverManagement;
        maxNumConcurrentFillerTasks = MAX_NUM_CONCURRENT_FILLER_TASKS;
        concurrentFutures = new AtomicReferenceArray<StampedFuture>(maxNumConcurrentFillerTasks);
        keepgoing = new AtomicBoolean(true);
        queue = new LinkedBlockingQueue<TextFiller>();
        simpleName = getClass().getSimpleName();
        gate = new Gate(-1);
    }

    /**
     * Starts consuming from queue.
     */
    public void start() {
        future = SmalServiceLookup.getThreadPool().submit(ThreadPools.task(this, simpleName));
    }

    /**
     * Stop consuming from queue.
     */
    public void stop() {
        final int length = concurrentFutures.length();
        for (int i = 0; i < length; i++) {
            final StampedFuture sf = concurrentFutures.get(i);
            if (null != sf && placeHolder != sf) {
                final Future<Object> f = sf.getFuture();
                if (null != f) {
                    f.cancel(true);
                }
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
        if (queue.offer(filler) && DEBUG) {
			LOG.debug("SolrTextFillerQueue.add() Added text filler (queue-size=" + queue.size() + "): " + filler);
		}
    }

    /**
     * Adds specified text fillers
     *
     * @param fillers The text fillers
     */
    public void add(final Collection<TextFiller> fillers) {
        for (final TextFiller filler : fillers) {
            add(filler);
        }
    }

    /**
     * Pauses taking from queue.
     *
     * @return <code>true</code> if caller paused the consuming thread; otherwise <code>false</code> if already paused
     */
    public boolean pause() {
        return gate.close();
    }

    /**
     * Proceed taking from queue.
     */
    public void proceed() {
        gate.open();
    }

    @Override
    public void run() {
        try {
            final Gate gate = this.gate;
            // final int maxElements = MAX_FILLER_CHUNK << 1;
            final List<TextFiller> list = new ArrayList<TextFiller>(8192);
            while (keepgoing.get()) {
                /*
                 * Check if paused
                 */
                gate.pass();
                try {
                    /*
                     * Proceed taking from queue
                     */
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
                    Thread.sleep(100);
                    queue.drainTo(list);
                    final boolean quit = list.remove(POISON);
                    if (!list.isEmpty()) {
                        if (DEBUG) {
                            LOG.debug("Processing " + list.size() + " text fillers from queue");
                        }
                        for (final List<TextFiller> fillers : TextFillerGrouper.groupTextFillersByFullName(list)) {
                            if (DEBUG) {
                                LOG.debug("Scheduling " + fillers.size() + " text fillers. Remaining in queue: " + queue.size());
                            }
                            handleFillers(fillers);
                        }
                    }
                    if (quit) {
                        return;
                    }
                    list.clear();
                } finally {
                    gate.signalDone();
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
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
        final ThreadPoolService poolService = SmalServiceLookup.getThreadPool();
        final int size = groupedFillers.size();
        final int configuredBlockSize = MAX_FILLER_CHUNK;
        final long st = DEBUG ? System.currentTimeMillis() : 0L;
        if (size <= configuredBlockSize) {
            scheduleFillers(groupedFillers, poolService);
        } else {
            int fromIndex = 0;
            while (fromIndex < size) {
                final int toIndex = fromIndex + configuredBlockSize;
                if (toIndex > size) {
                    scheduleFillers(groupedFillers.subList(fromIndex, size), poolService);
                    fromIndex = size;
                } else {
                    scheduleFillers(groupedFillers.subList(fromIndex, toIndex), poolService);
                    fromIndex = toIndex;
                }
            }
        }
        if (DEBUG) {
            final long dur = System.currentTimeMillis() - st;
            LOG.debug("Scheduled " + groupedFillers.size() + " fillers within " + dur + "msec");
        }
    }

    private void scheduleFillers(final List<TextFiller> groupedFillersSublist, final ThreadPoolService poolService) throws InterruptedException {
        if (null == poolService) {
            /*
             * Caller runs because thread pool is absent
             */
            handleFillersSublist(groupedFillersSublist, simpleName);
        } else {
            /*
             * Find a free or elapsed slot
             */
            int index = -1;
            while (index < 0) {
                final long earliestStamp = System.currentTimeMillis() - MAX_RUNNING_TIME;
                for (int i = 0; (index < 0) && (i < maxNumConcurrentFillerTasks); i++) {
                    final StampedFuture sf = concurrentFutures.get(i);
                    if (null == sf) {
                        if (concurrentFutures.compareAndSet(i, null, placeHolder)) {
                            index = i; // Found a free slot
                        }
                    } else if (sf.getStamp() < earliestStamp) { // Elapsed
                        sf.getFuture().cancel(true);
                        if (DEBUG) {
                            LOG.debug("Cancelled elapsed task running for " + (System.currentTimeMillis() - sf.getStamp()) + "msec.");
                        }
                        if (concurrentFutures.compareAndSet(i, sf, placeHolder)) {
                            index = i; // Found a slot with an elapsed task
                        }
                    }
                }
                if (DEBUG) {
                    LOG.debug(index < 0 ? "Awaiting a free/elapsed slot..." : "Found a free/elapsed slot...");
                }
                if (index < 0) {
                    synchronized (placeHolder) {
                        placeHolder.wait(WAIT_TIME);
                    }
                }
            }
            /*
             * Submit to a free worker thread
             */
            final FillerHandlerTask task = new FillerHandlerTask(groupedFillersSublist, index);
            final Future<Object> f = poolService.submit(ThreadPools.task(task));
            final StampedFuture sf = new StampedFuture(f);
            concurrentFutures.set(index, sf);
            task.start(sf);
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
                        Thread.interrupted();
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
            LOG.warn("Failed pushing batch text contents to indexed mails. Retry one-by-one.", e);
            // Retry one-by-one
            final Thread thread = Thread.currentThread();
            for (final TextFiller textFiller : fillersChunk) {
                if (thread.isInterrupted()) {
                    Thread.interrupted();
                    throw new InterruptedException("Text filler thread interrupted");
                }
                try {
                    pushMailTextBodies(Collections.singletonList(textFiller), threadDesc);
                } catch (final OXException ignore) {
                    // Ignore
                    if (DEBUG) {
                        LOG.debug("Ignoring failed text filler handling.", ignore);
                    }
                }
            }
        } catch (final RuntimeException e) {
            LOG.error("Failed pushing text content to indexed mails.", e);
        } finally {
            if (DEBUG) {
                LOG.debug("Handled " + fillersChunk.size() + " fillers.");
            }
            synchronized (placeHolder) {
                placeHolder.notifyAll();
            }
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
            solrServer = null;
//                serverManagement.getSolrServer(SMALServiceLookup.getServiceStatic(SolrCoreConfigService.class).getReadOnlyURL(
//                    contextId,
//                    userId,
//                    Types.EMAIL));
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
                        q.append(" OR ").append(FIELD_UUID).append(":\"").append(uuid).append('"');
                    }
                    q.delete(0, 4);
                    solrQuery = new SolrQuery(q.toString());
                }
                if (thread.isInterrupted()) {
                    Thread.interrupted();
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
                    Thread.interrupted();
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
                    Thread.interrupted();
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
                        Thread.interrupted();
                        throw new InterruptedException("Text filler thread interrupted");
                    }
                    int toIndex = off + 10;
                    if (toIndex > docSize) {
                        toIndex = docSize;
                    }
                    final List<SolrInputDocument> docs = inputDocuments.subList(off, toIndex);
                    try {
                        solrServer.add(docs);
                        rollback = true;
                    } catch (final SolrServerException e) {
                        if (!(e.getRootCause() instanceof java.net.SocketTimeoutException)) {
                            throw e;
                        }
                        final CommonsHttpSolrServer noTimeoutSolrServer = null; //serverManagement.getNoTimeoutSolrServerFor(solrServer);
                        for (final SolrInputDocument doc : docs) {
                            noTimeoutSolrServer.add(doc);
                            rollback = true;
                        }
                    }
                    off = toIndex;
                }
                /*
                 * Commit sane
                 */
                commitSane(solrServer);
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
            throw SmalExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            rollback(rollback ? solrServer : null);
            throw SmalExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(rollback ? solrServer : null);
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void grabTextFor(final List<TextFiller> fillers, final int contextId, final int userId, final int accountId, final Map<String, SolrDocument> documents, final List<SolrInputDocument> inputDocuments) throws OXException, InterruptedException {
        MailAccess<?, ?> access = null;
        try {
            access = SmalMailAccess.getUnwrappedInstance(userId, contextId, accountId);
            access.connect(false);
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            final IMailMessageStorage messageStorage = access.getMessageStorage();
            final int fs = fillers.size();
            final String[] contents;
            {
                final String[] mailIds = new String[fs];
                for (int i = 0; i < fs; i++) {
                    mailIds[i] = fillers.get(i).getMailId();
                }
                contents = messageStorage.getPrimaryContents(fillers.get(0).getFullName(), mailIds);
            }
            SmalMailAccess.closeUnwrappedInstance(access);
            access = null;
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("Held mail connection for " + dur + "msec");
            }
            // final TextFinder textFinder = new TextFinder();
            final Thread thread = Thread.currentThread();
            for (int i = 0; i < fs; i++) {
                if (thread.isInterrupted()) {
                    Thread.interrupted();
                    throw new InterruptedException("Text filler thread interrupted");
                }
                final TextFiller filler = fillers.get(i);
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
                        final String text = contents[i];
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
            SmalMailAccess.closeUnwrappedInstance(access);
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

    private final class FillerHandlerTask implements Task<Object> {

        private final List<TextFiller> fillers;
        private final int indexPos;
        private final CountDownLatch startSignal;
        private volatile StampedFuture sf;

        protected FillerHandlerTask(final List<TextFiller> fillers, final int indexPos) {
            super();
            this.startSignal = new CountDownLatch(1);
            this.fillers = fillers;
            this.indexPos = indexPos;
        }

        /**
         * Opens this task for processing.
         *
         * @param sf The stamped future object associated with this task
         */
        protected void start(final StampedFuture sf) {
            this.sf = sf;
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
                final StampedFuture sf = this.sf;
                if (null != sf) {
                    sf.setStamp(System.currentTimeMillis());
                }
                handleFillersSublist(fillers, String.valueOf(indexPos + 1));
                return null;
            } finally {
                concurrentFutures.set(indexPos, null);
            }
        }

    }

}
