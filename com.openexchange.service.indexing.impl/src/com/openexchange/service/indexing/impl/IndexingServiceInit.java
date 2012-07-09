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

package com.openexchange.service.indexing.impl;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQService;
import com.openexchange.mq.queue.MQQueueAsyncReceiver;
import com.openexchange.mq.queue.MQQueueSender;
import com.openexchange.server.ServiceLookup;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link IndexingServiceInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexingServiceInit {

    private final ServiceLookup services;

    private IndexingQueueSender sender;

    private IndexingQueueAsyncReceiver receiver;

    private final int maxConcurrentJobs;

    /**
     * Initializes a new {@link IndexingServiceInit}.
     */
    public IndexingServiceInit(final int maxConcurrentJobs, final ServiceLookup services) {
        super();
        this.services = services;
        this.maxConcurrentJobs = maxConcurrentJobs;
    }

    /**
     * Initializes indexing service.
     * 
     * @throws OXException If initialization fails
     */
    public synchronized void init() throws OXException {
        /*
         * Check for existence of needed durable queue
         */
        final String indexingQueue = IndexingService.INDEXING_QUEUE;
        final MQService service = services.getService(MQService.class);

        /*-
         * TODO: Delete if needed
        {
            try {
                service.lookupQueue(indexingQueue, true, new HashMap<String, Object>(1));
            } catch (final Exception e) {
                // Ignore
            }
            service.deleteQueue(indexingQueue);
        }
         */

        final Map<String, Object> params = new HashMap<String, Object>(1);
        params.put(MQConstants.QUEUE_PARAM_DURABLE, Boolean.TRUE);
        service.lookupQueue(indexingQueue, true, params);
        /*
         * Create queue sender
         */
        sender = new IndexingQueueSender();
    }

    /**
     * Initializes indexing service receiver.
     * 
     * @throws OXException If initialization fails
     */
    public synchronized void initReceiver() throws OXException {
        if (null == receiver) {
            /*
             * Create async. queue receiver
             */
            final ThreadPoolService threadPool = services.getService(ThreadPoolService.class);
            final IndexingJobExecutor executor = new IndexingJobExecutor(maxConcurrentJobs, threadPool).start();
            final IndexingServiceQueueListener listener = new IndexingServiceQueueListener(executor);
            receiver = new IndexingQueueAsyncReceiver(listener);
        }
    }

    /**
     * Drops indexing service receiver.
     */
    public synchronized void dropReceiver() {
        final MQQueueAsyncReceiver receiver = this.receiver;
        if (null != receiver) {
            receiver.close();
            this.receiver = null;
        }
    }

    /**
     * Drops indexing service.
     */
    public synchronized void drop() {
        final MQQueueSender sender = this.sender;
        if (null != sender) {
            sender.close();
            this.sender = null;
        }
        final MQQueueAsyncReceiver receiver = this.receiver;
        if (null != receiver) {
            receiver.close();
            this.receiver = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        drop();
        super.finalize();
    }

    /**
     * Gets the sender
     * 
     * @return The sender or <code>null</code> if already closed
     */
    public IndexingQueueSender getSender() {
        return sender;
    }

    /**
     * Gets the receiver
     * 
     * @return The receiver or <code>null</code> if already closed
     */
    public MQQueueAsyncReceiver getReceiver() {
        return receiver;
    }

}
