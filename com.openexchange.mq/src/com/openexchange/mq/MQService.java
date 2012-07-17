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

package com.openexchange.mq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import com.openexchange.exception.OXException;
import com.openexchange.mq.example.MQJmsQueueExample;
import com.openexchange.mq.example.MQJmsTopicExample;

/**
 * {@link MQService} - The generic Message Queue service.
 * <p>
 * See <a href="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/jms_tutorialTOC.html">JMS Tutorial</a>.
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
 * <h3>Hint</h3>
 * <p>
 * Please note that JMS connections, sessions, producers and consumers are <span class="emphasis"><em>designed to be re-used</em></span>.
 * </p>
 * <p>
 * It's an anti-pattern to create new connections, sessions, producers and consumers for each message you produce or consume. If you do
 * this, your application will perform very poorly. This is discussed further in the section on performance tuning <a
 * title="Chapter 46. Performance Tuning" href="perf-tuning.html">Chapter 46, <i>Performance Tuning</i></a>.
 * </p>
 * <p>
 * Prefer to store only primitive data types (including Strings) to avoid data serialization issues short and long-term.
 * </p>
 * </div>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MQService extends MQConstants {

    /**
     * The service reference.
     */
    public static final AtomicReference<MQService> SERVICE_REFERENCE = new AtomicReference<MQService>();

    /**
     * Gets the special queue for managing Message Queue system.
     * 
     * @return The special queue for managing Message Queue system.
     */
    Queue getManagementQueue() throws OXException;

    /**
     * Returns the names of the JMS topics available on this server.
     */
    List<String> getTopicNames();

    /**
     * Returns the names of the JMS queues available on this server.
     */
    List<String> getQueueNames();

    /**
     * Returns the names of the JMS connection factories available on this server.
     */
    List<String> getConnectionFactoryNames();

    /**
     * Lookup in the registry for registered {@link ConnectionFactory}.
     * <p>
     * A {@link ConnectionFactory} is the main starting point to use message queue services in JMS-like manner:<br>
     * <img src="http://docs.oracle.com/javaee/1.4/tutorial/doc/images/jms-programmingModel.gif" alt="jms-model">
     * 
     * @param name The name of the {@link ConnectionFactory}
     * @return The looked-up {@link ConnectionFactory} instance.
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    <F extends ConnectionFactory> F lookupConnectionFactory(String name) throws OXException;

    /**
     * Lookup in the registry for registered default {@link ConnectionFactory}.
     * <p>
     * A {@link ConnectionFactory} is the main starting point to use message queue services in JMS-like manner:<br>
     * <img src="http://docs.oracle.com/javaee/1.4/tutorial/doc/images/jms-programmingModel.gif" alt="jms-model">
     * 
     * @return The default {@link ConnectionFactory} instance.
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    <F extends ConnectionFactory> F lookupDefaultConnectionFactory() throws OXException;

    /*-
     * -------------------------------------------------------------------------------------------------
     * ----------------------------------- Lookup methods for Queues -----------------------------------
     * -------------------------------------------------------------------------------------------------
     */

    /**
     * Lookup in the registry for registered {@link Queue}.
     * <p>
     * A queue follows the Point-to-Point Messaging Domain:<br>
     * <ul>
     * <li>Each message has only one consumer.</li>
     * <li>A sender and a receiver of a message have no timing dependencies. The receiver can fetch the message whether or not it was
     * running when the client sent the message.</li>
     * <li>The receiver acknowledges the successful processing of a message.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.2.gif" alt="p2p">
     * 
     * @param name The name of the queue
     * @return The looked-up {@link Queue} instance.
     * @throws OXException If such a queue does not exist
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Queue lookupQueue(String name) throws OXException;

    /**
     * Lookup in the registry for registered {@link Queue}.
     * <p>
     * A queue follows the Point-to-Point Messaging Domain:<br>
     * <ul>
     * <li>Each message has only one consumer.</li>
     * <li>A sender and a receiver of a message have no timing dependencies. The receiver can fetch the message whether or not it was
     * running when the client sent the message.</li>
     * <li>The receiver acknowledges the successful processing of a message.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.2.gif" alt="p2p">
     * 
     * @param name The name of the queue
     * @param createIfAbsent <code>true</code> to create such a queue if absent; otherwise <code>false</code> to respond with an error
     * @param params Optional parameters for the queue in case of creation; pass <code>null</code> to create with default parameters (e.g.
     *            see {@link MQConstants#QUEUE_PARAM_DURABLE}
     * @return The looked-up {@link Queue} instance.
     * @throws OXException If such a queue does not exist or could not be created
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Queue lookupQueue(String name, boolean createIfAbsent, Map<String, Object> params) throws OXException;

    /**
     * Lookup in the registry for registered local-only {@link Queue}; meaning sent messages are not distributed within cluster, and
     * therefore only receivable on the same server.
     * <p>
     * A queue follows the Point-to-Point Messaging Domain:<br>
     * <ul>
     * <li>Each message has only one consumer.</li>
     * <li>A sender and a receiver of a message have no timing dependencies. The receiver can fetch the message whether or not it was
     * running when the client sent the message.</li>
     * <li>The receiver acknowledges the successful processing of a message.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.2.gif" alt="p2p">
     * 
     * @param name The name of the queue
     * @return The looked-up {@link Queue} instance.
     * @throws OXException If such a queue does not exist
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Queue lookupLocalOnlyQueue(String name) throws OXException;

    /**
     * Lookup in the registry for registered {@link Queue}; meaning sent messages are not distributed within cluster, and therefore only
     * receivable on the same server.
     * <p>
     * A queue follows the Point-to-Point Messaging Domain:<br>
     * <ul>
     * <li>Each message has only one consumer.</li>
     * <li>A sender and a receiver of a message have no timing dependencies. The receiver can fetch the message whether or not it was
     * running when the client sent the message.</li>
     * <li>The receiver acknowledges the successful processing of a message.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.2.gif" alt="p2p">
     * 
     * @param name The name of the queue
     * @param createIfAbsent <code>true</code> to create such a queue if absent; otherwise <code>false</code> to respond with an error
     * @param params Optional parameters for the queue in case of creation; pass <code>null</code> to create with default parameters (e.g.
     *            see {@link MQConstants#QUEUE_PARAM_DURABLE}
     * @return The looked-up {@link Queue} instance.
     * @throws OXException If such a queue does not exist or could not be created
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Queue lookupLocalOnlyQueue(String name, boolean createIfAbsent, Map<String, Object> params) throws OXException;

    /**
     * Checks if the denoted queue is local-only.
     * 
     * @return <code>true</code> if local-only; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isLocalOnlyQueue(String name) throws OXException;

    /*-
     * -------------------------------------------------------------------------------------------------
     * ----------------------------------- Lookup methods for Topics -----------------------------------
     * -------------------------------------------------------------------------------------------------
     */

    /**
     * Lookup in the registry for registered {@link Topic}.
     * <p>
     * A topic follows the Publish/Subscribe Messaging Domain:<br>
     * <ul>
     * <li>Each message may have multiple consumers.</li>
     * <li>Publishers and subscribers have a timing dependency. A client that subscribes to a topic can consume only messages published
     * after the client has created a subscription, and the subscriber must continue to be active in order for it to consume messages.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.3.gif" alt="pub-sub">
     * 
     * @param name The name of the topic
     * @return The looked-up {@link Topic} instance.
     * @throws OXException If such a topic does not exist
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Topic lookupTopic(String name) throws OXException;

    /**
     * Lookup in the registry for registered {@link Topic}.
     * <p>
     * A topic follows the Publish/Subscribe Messaging Domain:<br>
     * <ul>
     * <li>Each message may have multiple consumers.</li>
     * <li>Publishers and subscribers have a timing dependency. A client that subscribes to a topic can consume only messages published
     * after the client has created a subscription, and the subscriber must continue to be active in order for it to consume messages.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.3.gif" alt="pub-sub">
     * 
     * @param name The name of the topic
     * @param createIfAbsent <code>true</code> to create such a queue if absent; otherwise <code>false</code> to respond with an error
     * @return The looked-up {@link Topic} instance.
     * @throws OXException If such a topic does not exist or could not be created
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Topic lookupTopic(String name, boolean createIfAbsent) throws OXException;

    /**
     * Lookup in the registry for registered {@link Topic}; meaning published messages are not distributed within cluster, and therefore can
     * only be subscribed on the same server.
     * <p>
     * A topic follows the Publish/Subscribe Messaging Domain:<br>
     * <ul>
     * <li>Each message may have multiple consumers.</li>
     * <li>Publishers and subscribers have a timing dependency. A client that subscribes to a topic can consume only messages published
     * after the client has created a subscription, and the subscriber must continue to be active in order for it to consume messages.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.3.gif" alt="pub-sub">
     * 
     * @param name The name of the topic
     * @return The looked-up {@link Topic} instance.
     * @throws OXException If such a topic does not exist
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Topic lookupLocalOnlyTopic(String name) throws OXException;

    /**
     * Lookup in the registry for registered {@link Topic}; meaning published messages are not distributed within cluster, and therefore can
     * only be subscribed on the same server.
     * <p>
     * A topic follows the Publish/Subscribe Messaging Domain:<br>
     * <ul>
     * <li>Each message may have multiple consumers.</li>
     * <li>Publishers and subscribers have a timing dependency. A client that subscribes to a topic can consume only messages published
     * after the client has created a subscription, and the subscriber must continue to be active in order for it to consume messages.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.3.gif" alt="pub-sub">
     * 
     * @param name The name of the topic
     * @param createIfAbsent <code>true</code> to create such a queue if absent; otherwise <code>false</code> to respond with an error
     * @return The looked-up {@link Topic} instance.
     * @throws OXException If such a topic does not exist or could not be created
     * @see MQJmsQueueExample
     * @see MQJmsTopicExample
     */
    Topic lookupLocalOnlyTopic(String name, boolean createIfAbsent) throws OXException;

    /**
     * Checks if the denoted topic is local-only.
     * 
     * @return <code>true</code> if local-only; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isLocalOnlyTopic(String name) throws OXException;

    /*-
     * ---------------------- Delete routines ----------------------------
     */

    /**
     * Deletes specified queue.
     * 
     * @param name The queue name
     * @throws OXException If deletion fails
     */
    public void deleteQueue(String name) throws OXException;

    /**
     * Deletes specified local queue.
     * 
     * @param name The queue name
     * @throws OXException If deletion fails
     */
    public void deleteLocaleQueue(String name) throws OXException;

    /**
     * Deletes specified topic.
     * 
     * @param name The topic name
     * @throws OXException If deletion fails
     */
    public void deleteTopic(String name) throws OXException;

    /**
     * Deletes specified local topic.
     * 
     * @param name The topic name
     * @throws OXException If deletion fails
     */
    public void deleteLocaleTopic(String name) throws OXException;
}
