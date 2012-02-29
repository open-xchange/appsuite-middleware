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
import java.io.Serializable;
import javax.jms.BytesMessage;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQCloseable;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQService;

/**
 * {@link MQQueueSender} - A queue sender intended to be re-used. Invoke {@link #close()} method when done.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MQQueueSender implements MQCloseable {

    private volatile QueueConnection queueConnection;

    private final String queueName;

    private volatile QueueSession queueSession;

    private final QueueSender queueSender;

    /**
     * Initializes a new {@link MQQueueSender}.
     * 
     * @throws OXException If initialization fails
     */
    public MQQueueSender(final String queueName) throws OXException {
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
            queueSender = queueSession.createSender(queue);
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
     * Gets the name of the queue associated with this sender.
     * 
     * @return The queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sends a message containing a <code>java.lang.String</code>.
     * 
     * @param text The <code>java.lang.String</code> to send
     * @throws OXException If send operation fails
     */
    public void sendTextMessage(final String text) throws OXException {
        if (null == text) {
            return;
        }
        try {
            final TextMessage message = queueSession.createTextMessage(text);
            queueSender.send(message);
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sends a message containing a serializable Java object.
     * 
     * @param object The serializable object to send
     * @throws OXException If send operation fails
     */
    public void sendObjectMessage(final Serializable object) throws OXException {
        if (object instanceof String) {
            sendTextMessage((String) object);
            return;
        }
        if (null == object) {
            return;
        }
        try {
            final ObjectMessage message = queueSession.createObjectMessage(object);
            queueSender.send(message);
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sends a message containing <code>byte</code>s.
     * 
     * @param bytes The <code>byte</code> array to send
     * @throws OXException If send operation fails
     */
    public void sendBytesMessage(final byte[] bytes) throws OXException {
        if (null == bytes) {
            return;
        }
        try {
            final BytesMessage bytesMessage = queueSession.createBytesMessage();
            bytesMessage.writeBytes(bytes, 0, bytes.length);
            queueSender.send(bytesMessage);
        } catch (final JMSException e) {
            throw MQExceptionCodes.JMS_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Closes this queue sender orderly.
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
