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

package com.openexchange.mq.queue;

import static com.openexchange.mq.serviceLookup.MQServiceLookup.getMQService;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQCloseable;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQService;

/**
 * {@link MQQueueReceiver} - A queue receiver intended to be re-used. Invoke {@link #close()} method when done.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MQQueueReceiver implements MQCloseable {

    private volatile QueueConnection queueConnection;

    private final String queueName;

    private volatile QueueSession queueSession;

    private final QueueReceiver queueReceiver;

    /**
     * Initializes a new {@link MQQueueReceiver}.
     * 
     * @throws OXException If initialization fails
     */
    public MQQueueReceiver(final String queueName) throws OXException {
        super();
        if (null == queueName) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create("Queue name is null.");
        }
        this.queueName = queueName;
        boolean errorOccurred = true;
        try {
            final MQService service = getMQService();
            // Now we'll look up the connection factory:
            final QueueConnectionFactory queueConnectionFactory = service.lookupConnectionFactory(MQConstants.PATH_CONNECTION_FACTORY);
            // And look up the Queue:
            final Queue queue = service.lookupQueue(MQConstants.PREFIX_QUEUE + queueName);
            // Setup connection, session & sender
            final QueueConnection queueConnection = this.queueConnection = queueConnectionFactory.createQueueConnection();
            final QueueSession queueSession = this.queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            errorOccurred = false;
        } catch (final InvalidDestinationException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, queueName);
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        } finally {
            if (errorOccurred) {
                close();
            }
        }
    }

    /**
     * Gets the name of the queue associated with this receiver.
     * 
     * @return The queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Receives the next text message produced for this message consumer.
     * <p>
     * This call blocks indefinitely until a message is produced or until this message consumer is closed.
     * 
     * @return The next text produced for this message consumer, or <code>null</code> if this message consumer is concurrently closed
     * @exception OXException If receiver fails to receive the next message
     */
    public String receiveText() throws OXException {
        try {
            final Message message = queueReceiver.receive();
            if (!(message instanceof TextMessage)) {
                return null;
            }
            return ((TextMessage) message).getText();
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Receives the next message that arrives within the specified timeout interval.
     * <p>
     * This call blocks until a message arrives, the timeout expires, or this message consumer is closed. A timeout of zero never expires,
     * and the call blocks indefinitely.
     * 
     * @param timeout The timeout value in milliseconds
     * @return The next text produced for this message consumer, or null if the timeout expires or this message consumer is concurrently
     *         closed
     * @throws OXException If receiver fails to receive the next message
     */
    public String receiveText(final long timeout) throws OXException {
        try {
            final Message message = queueReceiver.receive(timeout);
            if (!(message instanceof TextMessage)) {
                return null;
            }
            return ((TextMessage) message).getText();
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Receives the next text if one is immediately available.
     * 
     * @return The next text produced for this message consumer, or null if one is not available
     * @throws OXException If the receiver fails to receive the next message
     */
    public String receiveTextNoWait() throws OXException {
        try {
            final Message message = queueReceiver.receiveNoWait();
            if (!(message instanceof TextMessage)) {
                return null;
            }
            return ((TextMessage) message).getText();
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Closes this queue receiver orderly.
     */
    @Override
    public final void close() {
        final QueueSession queueSession = this.queueSession;
        if (null != queueSession) {
            try {
                queueSession.close();
            } catch (final Exception e) {
                // Ignore
            }
            this.queueSession = null;
        }

        final QueueConnection queueConnection = this.queueConnection;
        if (null != queueConnection) {
            try {
                queueConnection.close();
            } catch (final Exception e) {
                // Ignore
            }
            this.queueConnection = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
