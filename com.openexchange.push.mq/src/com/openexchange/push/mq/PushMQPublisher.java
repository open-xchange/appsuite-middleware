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

package com.openexchange.push.mq;

import java.io.IOException;
import java.io.Serializable;
import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.topic.impl.MQTopicPublisherImpl;


/**
 * {@link PushMQPublisher}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PushMQPublisher extends MQTopicPublisherImpl {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PushMQPublisher.class));

    /**
     * Initializes a new {@link PushMQPublisher}.
     * @param topicName
     * @throws OXException
     */
    public PushMQPublisher(String topicName) throws OXException {
        super(topicName);
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
    public void publishTextMessage(final String text) throws OXException {
        throw new UnsupportedOperationException("Text messages not supported by topic: " + topicName);
    }

    @Override
    public void publishTextMessage(final String text, final int priority) throws OXException {
        LOG.warn("Text messages not supported by topic: " + topicName);
        throw new UnsupportedOperationException("Text messages not supported by topic: " + topicName);
    }

    @Override
    public void publishObjectMessage(final Serializable object) throws OXException {
        LOG.warn("Object messages not supported by topic: " + topicName);
        publishMQObject((PushMQObject) object);
    }
    
    public void publishMQObject(final PushMQObject obj) throws OXException {
        if (null == obj) {
            return;
        }
        try {
            final BytesMessage message = topicSession.createBytesMessage();
            message.writeBytes(SerializableHelper.writeObject(obj));
            topicPublisher.send(message, DeliveryMode.PERSISTENT, 4, DEFAULT_TIME_TO_LIVE);
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

    private static void rollback(final PushMQPublisher publisher) {
        if (!publisher.isTransacted()) {
            return;
        }
        try {
            publisher.rollback();
        } catch (final Exception e) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(PushMQPublisher.class));
            logger.error(e.getMessage(), e);
        }
    }

}
