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

package com.openexchange.mq.hornetq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.management.ConnectionFactoryControl;
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.api.jms.management.TopicControl;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQService;

/**
 * {@link HornetQService} - The HornetQ Message Queue service.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HornetQService implements MQService {

    private static final String SERVER = "-" + UUIDs.getUnformattedString(UUID.randomUUID());

    private final HornetQEmbeddedJMS jmsServer;

    private final ConnectionFactory defaultConnectionFactory;

    private volatile Queue managementQueue;

    /**
     * Initializes a new {@link HornetQService}.
     */
    public HornetQService(final HornetQEmbeddedJMS jmsServer) {
        super();
        this.jmsServer = jmsServer;
        // org.hornetq.jms.client.HornetQJMSConnectionFactory
        defaultConnectionFactory = (ConnectionFactory) jmsServer.lookup(NAME_CONNECTION_FACTORY);
    }

    /**
     * Gets the special queue for managing HornetQ.
     * 
     * @return The special queue for managing HornetQ.
     */
    @Override
    public Queue getManagementQueue() {
        Queue managementQueue = this.managementQueue;
        if (null == managementQueue) {
            synchronized (this) {
                managementQueue = this.managementQueue;
                if (null == managementQueue) {
                    this.managementQueue = managementQueue = HornetQJMSClient.createQueue("hornetq.management");
                }
            }
        }
        return managementQueue;
    }

    @Override
    public List<String> getQueueNames() {
        final Object[] queueControls = jmsServer.getHornetQServer().getManagementService().getResources(JMSQueueControl.class);
        final List<String> names = new ArrayList<String>(queueControls.length);
        for (int i = 0; i < queueControls.length; i++) {
            names.add(((JMSQueueControl) queueControls[i]).getName());
        }
        return names;
    }

    @Override
    public List<String> getTopicNames() {
        final Object[] topicControls = jmsServer.getHornetQServer().getManagementService().getResources(TopicControl.class);
        final List<String> names = new ArrayList<String>(topicControls.length);
        for (int i = 0; i < topicControls.length; i++) {
            names.add(((TopicControl) topicControls[i]).getName());
        }
        return names;
    }

    @Override
    public List<String> getConnectionFactoryNames() {
        final Object[] cfControls = jmsServer.getHornetQServer().getManagementService().getResources(ConnectionFactoryControl.class);
        final List<String> names = new ArrayList<String>(cfControls.length);
        for (int i = 0; i < cfControls.length; i++) {
            names.add(((ConnectionFactoryControl) cfControls[i]).getName());
        }
        return names;
    }

    /*-
     * ----------------------- Methods for JMS-like access to message queue -----------------------
     * 
     * Check with http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/prog_model.html
     * 
     */

    @Override
    public <F extends ConnectionFactory> F lookupConnectionFactory(final String name) throws OXException {
        try {
            @SuppressWarnings("unchecked") final F connectionFactory = (F) jmsServer.lookup(name);
            if (null == connectionFactory) {
                throw MQExceptionCodes.CF_NOT_FOUND.create(name);
            }
            return connectionFactory;
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.CF_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.CF_NOT_FOUND.create(e, name);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends ConnectionFactory> F lookupDefaultConnectionFactory() throws OXException {
        return (F) defaultConnectionFactory;
    }

    /*-
     * -------------------------------------------------------------------------------------------------
     * ----------------------------------- Lookup methods for Queues -----------------------------------
     * -------------------------------------------------------------------------------------------------
     */

    @Override
    public Queue lookupQueue(final String name) throws OXException {
        try {
            final Queue queue = (Queue) jmsServer.lookup(PREFIX_QUEUE + name);
            if (null == queue) {
                throw MQExceptionCodes.QUEUE_NOT_FOUND.create(name);
            }
            return queue;
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, name);
        }
    }

    @Override
    public Queue lookupQueue(final String name, final boolean createIfAbsent, final Map<String, Object> params) throws OXException {
        if (!createIfAbsent) {
            return lookupQueue(name);
        }
        // Create if absent
        final String identifier = PREFIX_QUEUE + name;
        Queue queue;
        try {
            queue = (Queue) jmsServer.lookup(identifier);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            queue = null;
        } catch (final Exception e) {
            queue = null;
        }
        if (null == queue) {
            try {
                final JMSServerManager serverManager = jmsServer.getServerManager();
                final String selector = (String) (null == params ? null : params.get(QUEUE_PARAM_SELECTOR));
                final boolean durable;
                {
                    if (null == params) {
                        durable = false;
                    } else {
                        final Object object = params.get(QUEUE_PARAM_DURABLE);
                        durable = null == object ? false : Boolean.parseBoolean(object.toString());
                    }
                }
                final JMSQueueConfiguration config = new JMSQueueConfigurationImpl(name, selector, durable, identifier);
                final String[] bindings = config.getBindings();
                serverManager.createQueue(false, config.getName(), config.getSelector(), config.isDurable(), bindings);
            } catch (final Exception e) {
                throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return lookupQueue(name);
    }

    @Override
    public Queue lookupLocalOnlyQueue(final String name) throws OXException {
        try {
            final Queue queue = (Queue) jmsServer.lookup(PREFIX_QUEUE + name + SERVER);
            if (null == queue) {
                throw MQExceptionCodes.QUEUE_NOT_FOUND.create(name);
            }
            return queue;
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, name);
        }
    }

    @Override
    public Queue lookupLocalOnlyQueue(final String name, final boolean createIfAbsent, final Map<String, Object> params) throws OXException {
        if (!createIfAbsent) {
            return lookupQueue(name);
        }
        // Create if absent
        final String identifier = PREFIX_QUEUE + name + SERVER;
        Queue queue;
        try {
            queue = (Queue) jmsServer.lookup(identifier);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            queue = null;
        } catch (final Exception e) {
            queue = null;
        }
        if (null == queue) {
            try {
                final JMSServerManager serverManager = jmsServer.getServerManager();
                final String selector = (String) (null == params ? null : params.get(QUEUE_PARAM_SELECTOR));
                final boolean durable;
                {
                    if (null == params) {
                        durable = false;
                    } else {
                        final Object object = params.get(QUEUE_PARAM_DURABLE);
                        durable = null == object ? false : Boolean.parseBoolean(object.toString());
                    }
                }
                final JMSQueueConfiguration config = new JMSQueueConfigurationImpl(name, selector, durable, identifier);
                final String[] bindings = config.getBindings();
                serverManager.createQueue(false, config.getName(), config.getSelector(), config.isDurable(), bindings);
            } catch (final Exception e) {
                throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return lookupQueue(name);
    }

    @Override
    public boolean isLocalOnlyQueue(final String name) throws OXException {
        try {
            return null != ((Queue) jmsServer.lookup(PREFIX_QUEUE + name + SERVER));
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /*-
     * -------------------------------------------------------------------------------------------------
     * ----------------------------------- Lookup methods for Topics -----------------------------------
     * -------------------------------------------------------------------------------------------------
     */

    @Override
    public Topic lookupTopic(final String name) throws OXException {
        try {
            final Topic topic = (Topic) jmsServer.lookup(name);
            if (null == topic) {
                throw MQExceptionCodes.TOPIC_NOT_FOUND.create(PREFIX_TOPIC + name);
            }
            return topic;
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, name);
        }
    }

    @Override
    public Topic lookupTopic(final String name, final boolean createIfAbsent) throws OXException {
        if (!createIfAbsent) {
            return lookupTopic(name);
        }
        // Create if absent
        final String identifier = PREFIX_TOPIC + name;
        Topic topic;
        try {
            topic = (Topic) jmsServer.lookup(identifier);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            topic = null;
        } catch (final Exception e) {
            topic = null;
        }
        if (null == topic) {
            try {
                final JMSServerManager serverManager = jmsServer.getServerManager();
                final TopicConfiguration config = new TopicConfigurationImpl(name, identifier);
                final String[] bindings = config.getBindings();
                serverManager.createTopic(false, config.getName(), bindings);
            } catch (final Exception e) {
                throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return lookupTopic(name);
    }

    @Override
    public Topic lookupLocalOnlyTopic(final String name) throws OXException {
        try {
            final Topic topic = (Topic) jmsServer.lookup(name);
            if (null == topic) {
                throw MQExceptionCodes.TOPIC_NOT_FOUND.create(PREFIX_TOPIC + name + SERVER);
            }
            return topic;
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, name);
        }
    }

    @Override
    public Topic lookupLocalOnlyTopic(final String name, final boolean createIfAbsent) throws OXException {
        if (!createIfAbsent) {
            return lookupTopic(name);
        }
        // Create if absent
        final String identifier = PREFIX_TOPIC + name + SERVER;
        Topic topic;
        try {
            topic = (Topic) jmsServer.lookup(identifier);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            topic = null;
        } catch (final Exception e) {
            topic = null;
        }
        if (null == topic) {
            try {
                final JMSServerManager serverManager = jmsServer.getServerManager();
                final TopicConfiguration config = new TopicConfigurationImpl(name, identifier);
                final String[] bindings = config.getBindings();
                serverManager.createTopic(false, config.getName(), bindings);
            } catch (final Exception e) {
                throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return lookupTopic(name);
    }

    @Override
    public boolean isLocalOnlyTopic(final String name) throws OXException {
        try {
            return null != ((Queue) jmsServer.lookup(PREFIX_TOPIC + name + SERVER));
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
