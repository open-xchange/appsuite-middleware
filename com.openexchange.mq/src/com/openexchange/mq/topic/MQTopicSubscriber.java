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

package com.openexchange.mq.topic;

import java.io.ByteArrayOutputStream;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MQTopicSubscriber} - A topic publisher intended to be re-used. Invoke {@link #close()} method when done.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MQTopicSubscriber extends MQTopicResource {

    public static interface MQTopicListener {

        /**
         * Passes published text to the listener.
         * 
         * @param text The text passed to the listener
         */
        void onText(String text);

        /**
         * Passes published Java object to the listener.
         * 
         * @param object The object passed to the listener
         */
        void onObject(Object object);

        /**
         * Passes published bytes to the listener.
         * 
         * @param bytes The bytes passed to the listener
         */
        void onBytes(byte[] bytes);
    }

    private TopicSubscriber topicSubscriber;

    private final MQTopicListener listener;

    /**
     * Initializes a new {@link MQTopicSubscriber}.
     * 
     * @throws OXException If initialization fails
     */
    public MQTopicSubscriber(final String topicName, final MQTopicListener listener) throws OXException {
        super(topicName);
        this.listener = listener;
    }

    @Override
    protected synchronized void initResource(final Topic topic) throws JMSException {
        topicSubscriber = topicSession.createSubscriber(topic);
        topicSubscriber.setMessageListener(new WrappingMessageListener(listener));
        topicConnection.start();
    }

    private static final class WrappingMessageListener implements MessageListener {

        private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MQTopicSubscriber.WrappingMessageListener.class));

        private final MQTopicListener listener;

        protected WrappingMessageListener(final MQTopicListener listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void onMessage(final Message message) {
            try {
                if (message instanceof TextMessage) {
                    listener.onText(((TextMessage) message).getText());
                } else if (message instanceof ObjectMessage) {
                    listener.onObject(((ObjectMessage) message).getObject());
                } else if (message instanceof BytesMessage) {
                    listener.onBytes(readBytesFrom((BytesMessage) message));
                } else {
                    throw new IllegalArgumentException("Unhandled message: " + message.getClass().getName());
                }
            } catch (final JMSException e) {
                LOG.error(e.getMessage(), e);
            } catch (final RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }
        }

    }

    protected static byte[] readBytesFrom(final BytesMessage bytesMessage) throws JMSException {
        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(4096);
        final int buflen = 2048;
        final byte[] buf = new byte[buflen];
        for (int read; (read = bytesMessage.readBytes(buf, buflen)) > 0;) {
            out.write(buf, 0, read);
        }
        return out.toByteArray();
    }

}
