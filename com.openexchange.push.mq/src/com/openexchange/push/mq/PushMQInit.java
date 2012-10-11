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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import javax.jms.JMSException;
import javax.jms.Topic;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQService;
import com.openexchange.mq.topic.MQTopicAsyncSubscriber;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.mq.registry.PushMQServiceRegistry;

/**
 * {@link PushMQInit}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PushMQInit {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PushMQInit.class));

    private Topic topic;

    private PushMQPublisher publisher;

    private MQTopicAsyncSubscriber subscriber;

    private final MQService mqService;

    private static volatile PushMQInit init;

    /**
     * Initializes a new {@link PushMQInit}.
     */
    public PushMQInit() {
        ServiceRegistry registry = PushMQServiceRegistry.getServiceRegistry();
        mqService = registry.getService(MQService.class);
        init = this;
    }

    public PushMQPublisher getPublisher() {
        return publisher;
    }

    public MQTopicAsyncSubscriber getSubscriber() {
        return subscriber;
    }

    public void init() throws OXException, JMSException {
        topic = mqService.lookupTopic("oxEventTopic");
        publisher = new PushMQPublisher(topic.getTopicName());
        PushMQListener listener = new PushMQListener();
        subscriber = new MQTopicAsyncSubscriber(topic.getTopicName(), listener);
    }

    public void close() {
        subscriber.close();
        publisher.close();
    }

    public static PushMQInit getInit() {
        return init;
    }

    public void stopListening() {
        if (subscriber != null) {
            subscriber.close();
            subscriber = null;
            LOG.info("PushMQ listener closed.");
        }
    }

    public void startListening() {
        try {
            PushMQListener listener = new PushMQListener();
            subscriber = new MQTopicAsyncSubscriber(topic.getTopicName(), listener);
            LOG.info("PushMQ listener started.");
        } catch (OXException e) {
            LOG.error("Start of PushMQ listener failed.", e);
        } catch (JMSException e) {
            LOG.error("Start of PushMQ listener failed.", e);
        }
    }
}
