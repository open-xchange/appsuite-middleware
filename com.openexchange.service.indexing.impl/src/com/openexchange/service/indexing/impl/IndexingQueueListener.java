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

import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.mq.queue.MQQueueListener;
import com.openexchange.service.indexing.IndexingJob;

/**
 * {@link IndexingQueueListener} - The {@link MQQueueListener listener} that delegates incoming messages to
 * {@link IndexingJobExecutor executor}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexingQueueListener implements MQQueueListener {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingQueueListener.class));

    private final IndexingJobExecutor jobExecutor;

    /**
     * Initializes a new {@link IndexingQueueListener}.
     */
    public IndexingQueueListener(final IndexingJobExecutor executor) {
        super();
        jobExecutor = executor;
    }

    @Override
    public void close() {
        jobExecutor.stop();
    }

    @Override
    public void onText(final String text) {
        LOG.warn("Invalid indexing message type: text");
    }

    @Override
    public void onObjectMessage(final ObjectMessage objectMessage) {
        try {
            jobExecutor.addJob((IndexingJob) objectMessage.getObject());
        } catch (final JMSException e) {
            LOG.warn("A JMS error occurred: " + e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.warn("A runtime error occurred: " + e.getMessage(), e);
        }
    }

    @Override
    public void onBytes(final byte[] bytes) {
        try {
            jobExecutor.addJob(SerializableHelper.<IndexingJob> readObject(bytes));
        } catch (final ClassNotFoundException e) {
            LOG.warn("Invalid Java object in indexing message: " + e.getMessage(), e);
        } catch (final IOException e) {
            LOG.warn("Deserialization failed: " + e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.warn("A runtime error occurred: " + e.getMessage(), e);
        }
    }

}
