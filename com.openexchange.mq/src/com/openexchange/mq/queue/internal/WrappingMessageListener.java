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

package com.openexchange.mq.queue.internal;

import javax.jms.BytesMessage;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.mq.internal.AbstractWrappingMessageListener;
import com.openexchange.mq.queue.MQQueueListener;
import com.openexchange.mq.topic.MQTopicListener;

/**
 * {@link WrappingMessageListener} - A {@link MessageListener} that wraps a given {@link MQTopicListener}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WrappingMessageListener extends AbstractWrappingMessageListener implements MessageListener, ExceptionListener {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(WrappingMessageListener.class));

    private final MQQueueListener listener;

    /**
     * Initializes a new {@link WrappingMessageListener}.
     * 
     * @param listener The topic listener to delegate the callbacks to
     */
    public WrappingMessageListener(final MQQueueListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            if (message instanceof TextMessage) {
                listener.onText(((TextMessage) message).getText());
                acknowledge(message);
            } else if (message instanceof ObjectMessage) {
                listener.onObjectMessage((ObjectMessage) message);
                acknowledge(message);
            } else if (message instanceof BytesMessage) {
                listener.onBytes(readBytesFrom((BytesMessage) message));
                acknowledge(message);
            } else {
                throw new IllegalArgumentException("Unhandled message: " + message.getClass().getName());
            }
        } catch (final JMSException e) {
            LOG.error(e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void onException(final JMSException e) {
        LOG.error("A JMS error occurred.", e);
    }

}
