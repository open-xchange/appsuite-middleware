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

package com.openexchange.mq.queue;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import com.openexchange.exception.OXException;
import com.openexchange.mq.queue.impl.MQQueueResource;
import com.openexchange.mq.queue.internal.WrappingMessageListener;

/**
 * {@link MQQueueAsyncReceiver} - An asynchronous queue receiver intended to be re-used. It subscribes specified {@link MQQueueListener
 * listener} to given topic.
 * <p>
 * Invoke {@link #close()} method when done.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MQQueueAsyncReceiver extends MQQueueResource {

    private QueueReceiver queueReceiver;

    private final MQQueueListener listener;

    /**
     * Initializes a new {@link MQQueueAsyncReceiver}.
     * 
     * @param queueName The name of queue to receive from
     * @throws OXException If initialization fails
     * @throws NullPointerException If listener is <code>null</code>
     */
    public MQQueueAsyncReceiver(final String queueName, final MQQueueListener listener) throws OXException {
        super(queueName, listener);
        if (null == listener) {
            throw new NullPointerException("listener is null.");
        }
        this.listener = listener;
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
    protected synchronized void initResource(final Queue queue, final Object listener) throws JMSException {
        queueReceiver = queueSession.createReceiver(queue);
        final WrappingMessageListener msgListener = new WrappingMessageListener((MQQueueListener) listener);
        queueReceiver.setMessageListener(msgListener);
        queueConnection.setExceptionListener(msgListener);
        queueConnection.start();
    }

    @Override
    public void close() {
        try {
            listener.close();
        } catch (final Exception e) {
            // Ignore
        }
        super.close();
    }

}
