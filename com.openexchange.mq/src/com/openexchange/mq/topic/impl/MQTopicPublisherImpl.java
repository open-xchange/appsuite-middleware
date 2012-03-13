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

package com.openexchange.mq.topic.impl;

import java.io.Serializable;
import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.serviceLookup.MQServiceLookup;
import com.openexchange.mq.topic.MQTopicPublisher;

/**
 * {@link MQTopicPublisherImpl} - A topic publisher intended to be re-used. Invoke {@link #close()} method when done.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MQTopicPublisherImpl extends MQTopicResource implements MQTopicPublisher {

    protected static final int DEFAULT_PRIORITY = Message.DEFAULT_PRIORITY;

    protected static final long DEFAULT_TIME_TO_LIVE = Message.DEFAULT_TIME_TO_LIVE;

    private int deliveryMode;

    protected TopicPublisher topicPublisher;

    /**
     * Initializes a new {@link MQTopicPublisherImpl}.
     * 
     * @param topicName The name of topic to publish to
     * @throws OXException If initialization fails
     */
    public MQTopicPublisherImpl(final String topicName) throws OXException {
        super(topicName, null);
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
    protected synchronized void initResource(final Topic topic, final Object ignore) throws JMSException, OXException {
        topicPublisher = topicSession.createPublisher(topic);
        topicPublisher.setDisableMessageID(true);
        topicPublisher.setDisableMessageTimestamp(true);
        deliveryMode = MQServiceLookup.getMQService().isLocalOnlyTopic(topicName) ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT;
    }

    @Override
    public void commit() throws OXException {
        if (!isTransacted()) {
            /*
             * Not transactional
             */
            return;
        }
        try {
            topicSession.commit();
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void rollback() throws OXException {
        if (!isTransacted()) {
            /*
             * Not transactional
             */
            return;
        }
        try {
            topicSession.rollback();
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void publishTextMessage(final String text) throws OXException {
        if (null == text) {
            return;
        }
        try {
            final TextMessage message = topicSession.createTextMessage(text);
            topicPublisher.publish(message, deliveryMode, DEFAULT_PRIORITY, DEFAULT_TIME_TO_LIVE);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        }
    }

    @Override
    public void publishObjectMessage(final Serializable object) throws OXException {
        if (object instanceof String) {
            publishTextMessage((String) object);
            return;
        }
        if (null == object) {
            return;
        }
        try {
            final ObjectMessage message = topicSession.createObjectMessage(object);
            topicPublisher.publish(message, deliveryMode, DEFAULT_PRIORITY, DEFAULT_TIME_TO_LIVE);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        }
    }

    @Override
    public void publishBytesMessage(final byte[] bytes) throws OXException {
        if (null == bytes) {
            return;
        }
        try {
            final BytesMessage bytesMessage = topicSession.createBytesMessage();
            bytesMessage.writeBytes(bytes, 0, bytes.length);
            topicPublisher.publish(bytesMessage, deliveryMode, DEFAULT_PRIORITY, DEFAULT_TIME_TO_LIVE);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        }
    }

    @Override
    public void publishTextMessage(final String text, final int priority) throws OXException {
        if (null == text) {
            return;
        }
        try {
            final TextMessage message = topicSession.createTextMessage(text);
            topicPublisher.publish(message, deliveryMode, checkPriority(priority), DEFAULT_TIME_TO_LIVE);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        }
    }

    @Override
    public void publishObjectMessage(final Serializable object, final int priority) throws OXException {
        if (object instanceof String) {
            publishTextMessage((String) object);
            return;
        }
        if (null == object) {
            return;
        }
        try {
            final ObjectMessage message = topicSession.createObjectMessage(object);
            topicPublisher.publish(message, deliveryMode, checkPriority(priority), DEFAULT_TIME_TO_LIVE);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        }
    }

    @Override
    public void publishBytesMessage(final byte[] bytes, final int priority) throws OXException {
        if (null == bytes) {
            return;
        }
        try {
            final BytesMessage bytesMessage = topicSession.createBytesMessage();
            bytesMessage.writeBytes(bytes, 0, bytes.length);
            topicPublisher.publish(bytesMessage, deliveryMode, checkPriority(priority), DEFAULT_TIME_TO_LIVE);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        }
    }

    /**
     * Checks proper priority range.
     * 
     * @param priority The priority to check
     * @return The checked priority
     */
    protected static int checkPriority(final int priority) {
        if (priority >= 0 && priority <= 9) {
            return priority;
        }
        return Message.DEFAULT_PRIORITY;
    }

}
