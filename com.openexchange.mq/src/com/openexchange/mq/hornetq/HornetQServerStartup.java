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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQServerStartup;

/**
 * {@link HornetQServerStartup} - The start-up implementation for hornetq message queue.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HornetQServerStartup implements MQServerStartup {

    private volatile EmbeddedJMS jmsServer;

    /**
     * Initializes a new {@link HornetQServerStartup}.
     */
    public HornetQServerStartup() {
        super();
    }

    @Override
    public void start() throws OXException {
        try {
            // Step 1. Create HornetQ core configuration, and set the properties accordingly
            final Configuration configuration = new ConfigurationImpl();
            configuration.setPersistenceEnabled(false);
            configuration.setSecurityEnabled(false);
            configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

            final TransportConfiguration connectorConfig = new TransportConfiguration(NettyConnectorFactory.class.getName());
            configuration.getConnectorConfigurations().put("connector", connectorConfig);

            // Step 2. Create the JMS configuration
            final JMSConfiguration jmsConfig = new JMSConfigurationImpl();

            // Step 3. Configure the JMS ConnectionFactory
            final List<String> connectorNames = new ArrayList<String>();
            connectorNames.add("connector");
            final ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl("cf", false, connectorNames, "/cf");
            jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

            // Step 4. Configure the JMS Queue
            final JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl("queue1", null, false, "/queue/queue1");
            jmsConfig.getQueueConfigurations().add(queueConfig);

            // Step 5. Start the JMS Server using the HornetQ core server and the JMS configuration
            final EmbeddedJMS jmsServer = new EmbeddedJMS();
            jmsServer.setConfiguration(configuration);
            jmsServer.setJmsConfiguration(jmsConfig);
            jmsServer.start();
            this.jmsServer = jmsServer;
        } catch (final Exception e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void stop() {
        final EmbeddedJMS jmsServer = this.jmsServer;
        if (null != jmsServer) {
            try {
                jmsServer.stop();
            } catch (final Exception e) {
                // Stop failed
                final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(HornetQServerStartup.class));
                log.error("Stopping HornetQ server failed.", e);
            }
            this.jmsServer = null;
        }
    }

}
