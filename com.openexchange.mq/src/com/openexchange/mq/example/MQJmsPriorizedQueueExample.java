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

package com.openexchange.mq.example;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQService;

/**
 * {@link MQJmsPriorizedQueueExample} - Example class for a simple Point-to-Point example using JMS classes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MQJmsPriorizedQueueExample {

    final MQService service;

    /**
     * Initializes a new {@link MQJmsPriorizedQueueExample}.
     */
    public MQJmsPriorizedQueueExample(final MQService service) {
        super();
        this.service = service;
    }

    /**
     * Test the MQ server for a Point-to-Point scenario testing various message priorities.
     * <p>
     * See <a href="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/advanced.html#1024730">here</a>
     */
    public void test() {
        Thread t = null;
        try {
            // Now we'll look up the connection factory:
            final QueueConnectionFactory queueConnectionFactory = service.lookupDefaultConnectionFactory();
            // And look up the Queue:
            final Queue queue = service.lookupQueue("myNewQueue", true, null);

            QueueConnection queueConnection = null;
            try {
                /*-
                 * 1. Create connection.
                 * 2. Create session from connection; false means session is not transacted.
                 * 3. Create sender and text message.
                 * 4. Send messages, varying text slightly.
                 * 5. Send end-of-messages message.
                 * 6. Finally, close connection.
                 */
                queueConnection = queueConnectionFactory.createQueueConnection();
                final QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                final QueueSender queueSender = queueSession.createSender(queue);
                final TextMessage message = queueSession.createTextMessage();
                for (int i = 0; i < 10; i++) {
                    message.setText("This is message " + (i + 1));
                    System.out.println("Sending message: " + message.getText());
                    queueSender.send(message, Message.DEFAULT_DELIVERY_MODE, i, Message.DEFAULT_TIME_TO_LIVE);
                }

                /*
                 * Send a non-text control message indicating end of messages. Send with lowest priority to not suppress actual text message.
                 */
                queueSender.send(queueSession.createMessage(), Message.DEFAULT_DELIVERY_MODE, 0, Message.DEFAULT_TIME_TO_LIVE);
            } catch (final JMSException e) {
                System.out.println("Exception occurred: " + e.toString());
            } finally {
                if (queueConnection != null) {
                    try {
                        queueConnection.close();
                    } catch (final JMSException e) {
                        // Ignore
                    }
                }
            }

            /*
             * Receive messages in another thread
             */
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // Now we'll look up the connection factory:
                        final QueueConnectionFactory queueConnectionFactory = service.lookupDefaultConnectionFactory();
                        // And look up the Queue:
                        final Queue queue = service.lookupQueue("myNewQueue");

                        QueueConnection queueConnection = null;
                        try {
                            /*-
                             * 1. Create connection.
                             * 2. Create session from connection; false means session is not transacted.
                             * 3. Create receiver, then start message delivery.
                             * 4. Receive all text messages from queue until
                             *    a non-text message is received indicating end of
                             *    message stream.
                             * 5. Close connection.
                             */
                            queueConnection = queueConnectionFactory.createQueueConnection();
                            final QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                            final QueueReceiver queueReceiver = queueSession.createReceiver(queue);
                            queueConnection.start();
                            while (true) {
                                final Message m = queueReceiver.receive(1);
                                if (m != null) {
                                    if (m instanceof TextMessage) {
                                        final TextMessage message = (TextMessage) m;
                                        System.out.println("Reading sent message: " + message.getText());
                                    } else {
                                        break;
                                    }
                                }
                            }
                        } catch (final JMSException e) {
                            System.out.println("Exception occurred: " + e.toString());
                        } finally {
                            if (queueConnection != null) {
                                try {
                                    queueConnection.close();
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

        } catch (final OXException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
