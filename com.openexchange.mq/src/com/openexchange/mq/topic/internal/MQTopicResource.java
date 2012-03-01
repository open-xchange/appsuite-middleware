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

package com.openexchange.mq.topic.internal;

import static com.openexchange.mq.serviceLookup.MQServiceLookup.getMQService;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQCloseable;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQService;

/**
 * {@link MQTopicResource}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MQTopicResource implements MQCloseable {

    protected volatile TopicConnection topicConnection;

    protected final String topicName;

    protected volatile TopicSession topicSession;

    /**
     * Initializes a new {@link MQTopicResource}.
     */
    public MQTopicResource(final String topicName) throws OXException {
        super();
        if (null == topicName) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create("Topic name is null.");
        }
        this.topicName = topicName;
        boolean errorOccurred = true;
        try {
            final MQService service = getMQService();
            // Now we'll look up the connection factory:
            final TopicConnectionFactory topicConnectionFactory = service.lookupConnectionFactory(MQConstants.NAME_CONNECTION_FACTORY);
            // And look up the TOpic:
            final Topic topic = service.lookupTopic(topicName);
            // Setup connection, session & sender
            final TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
            this.topicConnection = topicConnection;
            final TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            this.topicSession = topicSession;
            initResource(topic);
            errorOccurred = false;
        } catch (final InvalidDestinationException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, topicName);
        } catch (final JMSException e) {
            throw MQExceptionCodes.handleJMSException(e);
        } finally {
            if (errorOccurred) {
                close();
            }
        }
    }

    /**
     * Initializes the resource.
     * 
     * @throws JMSException If initialization fails
     */
    protected abstract void initResource(Topic topic) throws JMSException;

    /**
     * Gets the name of the topic associated with this sender.
     * 
     * @return The topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Closes this queue sender orderly.
     */
    @Override
    public final void close() {
        final TopicSession topicSession = this.topicSession;
        if (null != topicSession) {
            try {
                topicSession.close();
            } catch (final Exception e) {
                // Ignore
            }
            this.topicSession = null;
        }

        final TopicConnection topicConnection = this.topicConnection;
        if (null != topicConnection) {
            try {
                topicConnection.close();
            } catch (final Exception e) {
                // Ignore
            }
            this.topicConnection = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
