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
import java.io.Serializable;
import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.queue.impl.MQQueueSenderImpl;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;

/**
 * {@link IndexingQueueSender}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexingQueueSender extends MQQueueSenderImpl {

    private static final int NON_PERSISTENT = DeliveryMode.NON_PERSISTENT;

    private static final int PERSISTENT = DeliveryMode.PERSISTENT;

    /**
     * Initializes a new {@link IndexingQueueSender}.
     * 
     * @throws OXException If initialization fails
     */
    public IndexingQueueSender() throws OXException {
        super(IndexingService.INDEXING_QUEUE);
    }

    @Override
    protected boolean isTransacted() {
        return false;
    }

    @Override
    protected int getAcknowledgeMode() {
        return Session.AUTO_ACKNOWLEDGE;
    }

    @Override
    public void sendTextMessage(final String text) throws OXException {
        throw new UnsupportedOperationException("Text messages not supported by queue: " + IndexingService.INDEXING_QUEUE);
    }

    @Override
    public void sendTextMessage(final String text, final int priority) throws OXException {
        throw new UnsupportedOperationException("Text messages not supported by queue: " + IndexingService.INDEXING_QUEUE);
    }

    @Override
    public void sendObjectMessage(final Serializable object) throws OXException {
        sendJobMessage((IndexingJob) object);
    }

    @Override
    public void sendObjectMessage(final Serializable object, final int priority) throws OXException {
        final IndexingJob job = (IndexingJob) object;
        job.setPriority(priority);
        sendJobMessage(job);
    }

    /**
     * Sends a message containing a {@link IndexingJob job}.
     * 
     * @param job The job to send
     * @throws OXException If send operation fails
     */
    public void sendJobMessage(final IndexingJob job) throws OXException {
        if (null == job) {
            return;
        }
        try {
            final BytesMessage message = queueSession.createBytesMessage();
            message.writeBytes(SerializableHelper.writeObject(job));
            queueSender.send(message, job.isDurable() ? PERSISTENT : NON_PERSISTENT, checkPriority(job.getPriority()), DEFAULT_TIME_TO_LIVE);
            commit();
        } catch (final JMSException e) {
            rollback(this);
            throw MQExceptionCodes.handleJMSException(e);
        } catch (final IOException e) {
            rollback(this);
            throw MQExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(this);
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static void rollback(final IndexingQueueSender sender) {
        if (!sender.isTransacted()) {
            return;
        }
        try {
            sender.rollback();
        } catch (final Exception e) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(IndexingQueueSender.class));
            logger.error(e.getMessage(), e);
        }
    }

}
