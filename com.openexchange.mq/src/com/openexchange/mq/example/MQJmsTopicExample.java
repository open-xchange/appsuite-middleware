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

package com.openexchange.mq.example;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQService;

/**
 * {@link MQJmsTopicExample} - Example class for a simple Publish/Subscribe example using JMS classes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MQJmsTopicExample {

    final MQService service;

    /**
     * Initializes a new {@link MQJmsTopicExample}.
     */
    public MQJmsTopicExample(final MQService service) {
        super();
        this.service = service;
    }

    /**
     * Test the MQ server for a Publish/Subscribe scenario.
     */
    public void test() {
        Thread t = null;
        try {
            /*
             * Receive messages in another thread
             */

            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // Now we'll look up the connection factory:
                        final TopicConnectionFactory topicConnectionFactory = service.lookupDefaultConnectionFactory();
                        // And look up the Queue:
                        final Topic topic = service.lookupTopic(MQConstants.NAME_TOPIC);

                        TopicConnection topicConnection = null;
                        try {
                            /*-
                             * 1. Create connection.
                             * 2. Create session from connection; false means session is not transacted.
                             * 3. Create subscriber.
                             * 4. Register message listener (TextListener).
                             * 5. Receive text messages from topic.
                             * 6. When all messages have been received, enter Q to quit.
                             * 7. Close connection.
                             */
                            topicConnection = topicConnectionFactory.createTopicConnection();
                            final TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                            final TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);
                            topicSubscriber.setMessageListener(new MessageListener() {

                                @Override
                                public void onMessage(final Message message) {
                                    TextMessage msg = null;
                                    try {
                                        if (message instanceof TextMessage) {
                                            msg = (TextMessage) message;
                                            System.out.println("Reading published message: " + msg.getText());
                                        } else {
                                            System.out.println("Message of wrong type: " + message.getClass().getName());
                                        }
                                    } catch (final JMSException e) {
                                        System.out.println("JMSException in onMessage(): " + e.toString());
                                    } catch (final Throwable t) {
                                        System.out.println("Exception in onMessage():" + t.getMessage());
                                    }
                                }
                            });
                            topicConnection.start();

                            /*-
                             * 
                            System.out.println("To end program, enter Q or q, " + "then <return>");
                            final InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                            char answer = '\0';
                            while (!((answer == 'q') || (answer == 'Q'))) {
                                try {
                                    answer = (char) inputStreamReader.read();
                                } catch (final IOException e) {
                                    System.out.println("I/O exception: " + e.toString());
                                }
                            }
                             * 
                             */

                            try {
                                final long millis = 3000L;
                                System.out.println("Awaiting published messages for " + millis + "msec.");
                                Thread.sleep(millis);
                                System.out.println("Finished subscription for topic " + topic.getTopicName());
                            } catch (final InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                        } catch (final JMSException e) {
                            System.out.println("Exception occurred: " + e.toString());
                        } finally {
                            if (topicConnection != null) {
                                try {
                                    topicConnection.close();
                                } catch (final JMSException e) {
                                    // Ignore
                                }
                            }
                        }
                    } catch (final OXException e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            t.start();


            
            // Now we'll look up the connection factory:
            final TopicConnectionFactory topicConnectionFactory = service.lookupDefaultConnectionFactory();
            // And look up the Queue:
            final Topic topic = service.lookupTopic(MQConstants.NAME_TOPIC);

            TopicConnection topicConnection = null;
            try {
                /*-
                 * 1. Create connection.
                 * 2. Create session from connection; false means session is not transacted.
                 * 3. Create publisher and text message.
                 * 4. Send messages, varying text slightly.
                 * 5. Finally, close connection.
                 */
                topicConnection = topicConnectionFactory.createTopicConnection();
                final TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                final TopicPublisher topicPublisher = topicSession.createPublisher(topic);
                final TextMessage message = topicSession.createTextMessage();
                for (int i = 0; i < 10; i++) {
                    message.setText("This is message " + (i + 1));
                    System.out.println("Publishing message: " + message.getText());
                    topicPublisher.publish(message);
                }
            } catch (final JMSException e) {
                System.out.println("Exception occurred: " + e.toString());
            } finally {
                if (topicConnection != null) {
                    try {
                        topicConnection.close();
                    } catch (final JMSException e) {
                        // Ignore
                    }
                }
            }

        } catch (final OXException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
