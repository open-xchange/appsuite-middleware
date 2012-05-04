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

package com.openexchange.mq.hornetq;

import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.deployers.impl.FileConfigurationParser;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.server.JMSServerConfigParser;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.hornetq.jms.server.impl.JMSServerConfigParserImpl;
import org.hornetq.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.UnsynchronizedStringReader;
import com.openexchange.log.LogFactory;
import com.openexchange.mq.MQConstants;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQServerStartup;
import com.openexchange.mq.MQService;
import com.openexchange.mq.serviceLookup.MQServiceLookup;

/**
 * {@link HornetQServerStartup} - The start-up implementation for HornetQ message queue.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HornetQServerStartup implements MQServerStartup {

    private HornetQEmbeddedJMS jmsServer; // Set in synchronized context

    private HornetQService hornetQService;

    /**
     * Initializes a new {@link HornetQServerStartup}.
     */
    public HornetQServerStartup() {
        super();
    }

    @Override
    public MQService getService() throws OXException {
        return hornetQService;
    }

    private static final Pattern PATTERN_CONFIGPATH = Pattern.compile(Pattern.quote("@oxgroupwaresysconfdir@"));

    private static final Pattern PATTERN_SERVER_ID = Pattern.compile("-?"+Pattern.quote("@serverid@"));

    private static final Pattern PATTERN_MY_IP = Pattern.compile(Pattern.quote("@myip@"));

    @Override
    public synchronized void start() throws OXException {
        try {
            if (null != this.jmsServer) {
                // Already started
                return;
            }
            final HornetQEmbeddedJMS jmsServer;
            {
                /*
                 * Create HornetQ core configuration, and set the properties accordingly
                 */
                final ConfigurationService service = MQServiceLookup.getService(ConfigurationService.class);
                /*
                 * Look for a possible "hornetq-configuration.xml" file
                 */
                String hornetqConfigXml = service.getText("hornetq-configuration.xml");
                if (null == hornetqConfigXml) {
                    jmsServer = createEmbeddedJms();
                } else {
                    /*
                     * Parse into configuration
                     */
                    final String configPath = "/tmp/hornetq";
                    final Configuration configuration;
                    {
                        hornetqConfigXml = PATTERN_CONFIGPATH.matcher(hornetqConfigXml).replaceAll(configPath);
                        hornetqConfigXml = PATTERN_SERVER_ID.matcher(hornetqConfigXml).replaceAll(HornetQService.getServer());

                        final String ip = toIpString();
                        hornetqConfigXml = PATTERN_MY_IP.matcher(hornetqConfigXml).replaceAll(ip);

                        final Element e = stringToElement(XMLUtil.replaceSystemProps(hornetqConfigXml));

                        configuration = new ConfigurationImpl();
                        /*configuration.setPersistenceEnabled(false);*/
                        configuration.setSecurityEnabled(false);
                        configuration.setJournalSyncNonTransactional(false);
    
                        final FileConfigurationParser parser = new FileConfigurationParser();
                        parser.setValidateAIO(true);
                        parser.parseMainConfig(e, configuration);

                        hornetqConfigXml = null; // Help GC
                    }
                    /*
                     * Parse possible JMS configuration
                     */
                    final JMSConfiguration jmsConfiguration;
                    {
                        String hornetqJmsXml = service.getText("hornetq-jms.xml");
                        if (null == hornetqJmsXml) {
                            jmsConfiguration = new JMSConfigurationImpl();
                            /*
                             * Configure the JMS ConnectionFactory
                             */
                            final ConnectionFactoryConfiguration cfConfig =
                                new ConnectionFactoryConfigurationImpl(
                                    MQConstants.NAME_CONNECTION_FACTORY,
                                    false,
                                    Arrays.asList("in-vm-connector"),
                                    MQConstants.NAME_CONNECTION_FACTORY);
                            cfConfig.setBlockOnDurableSend(false);
                            cfConfig.setBlockOnNonDurableSend(false);
                            jmsConfiguration.getConnectionFactoryConfigurations().add(cfConfig);
                            /*
                             * Configure JMS queue & topic
                             */
                            jmsConfiguration.getQueueConfigurations().add(
                                new JMSQueueConfigurationImpl(
                                    MQConstants.NAME_QUEUE,
                                    null,
                                    false,
                                    MQConstants.PREFIX_QUEUE + MQConstants.NAME_QUEUE));
                            jmsConfiguration.getTopicConfigurations().add(
                                new TopicConfigurationImpl(MQConstants.NAME_TOPIC, MQConstants.PREFIX_TOPIC + MQConstants.NAME_TOPIC));
                        } else {
                            hornetqJmsXml = PATTERN_CONFIGPATH.matcher(hornetqJmsXml).replaceAll(configPath);
                            final Element e = stringToElement(XMLUtil.replaceSystemProps(hornetqJmsXml));
    
                            final JMSServerConfigParser jmsServerConfigParser = new JMSServerConfigParserImpl();
                            jmsConfiguration = jmsServerConfigParser.parseConfiguration(e);
                            if (!configuration.isClustered()) {
                                /*
                                 * Clustering disabled per HornetQ configuration "hornetq-configuration.xml"
                                 */
                                for (final ConnectionFactoryConfiguration connectionFactoryConfiguration : jmsConfiguration.getConnectionFactoryConfigurations()) {
                                    connectionFactoryConfiguration.setHA(false);
                                }
                            }

                            /*-
                             * 
                            for (final ConnectionFactoryConfiguration connectionFactoryConfiguration : jmsConfiguration.getConnectionFactoryConfigurations()) {
                                connectionFactoryConfiguration.setBlockOnDurableSend(false);
                                connectionFactoryConfiguration.setBlockOnNonDurableSend(false);
                            }
                             * 
                             */
                            hornetqJmsXml = null; // Help GC
                        }
                    }
                    /*
                     * Create the JMS Server using the HornetQ core server and the JMS configuration
                     */
                    jmsServer = new HornetQEmbeddedJMS();
                    jmsServer.setConfiguration(configuration);
                    jmsServer.setJmsConfiguration(jmsConfiguration);
                }
            }
            /*
             * Start the JMS Server
             */
            jmsServer.start();

            this.jmsServer = jmsServer;
            hornetQService = new HornetQService(jmsServer);
        } catch (final Exception e) {
            throw MQExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private HornetQEmbeddedJMS createEmbeddedJms() throws Exception {
        final Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        // Set acceptor: An acceptor defines a way in which connections can be made to the HornetQ server.
        {
            final Map<String, Object> params = new HashMap<String, Object>(2);
            params.put(
                org.hornetq.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME,
                Integer.valueOf(MQConstants.MQ_LISTEN_PORT));
            params.put(org.hornetq.core.remoting.impl.netty.TransportConstants.USE_NIO_PROP_NAME, Boolean.TRUE);
            final TransportConfiguration transportConfig =
                new TransportConfiguration(NettyAcceptorFactory.class.getName(), params, "netty-connector");
            configuration.getAcceptorConfigurations().add(transportConfig);

            configuration.getAcceptorConfigurations().add(
                new TransportConfiguration(InVMAcceptorFactory.class.getName(), new HashMap<String, Object>(1), "in-vm-connector"));
        }
        // Set connector: Whereas acceptors are used on the server to define how we accept connections, connectors are used by a client
        // to define how it connects to a server.
        {
            final Map<String, Object> params = new HashMap<String, Object>(2);
            params.put(
                org.hornetq.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME,
                Integer.valueOf(MQConstants.MQ_LISTEN_PORT));
            params.put(org.hornetq.core.remoting.impl.netty.TransportConstants.USE_NIO_PROP_NAME, Boolean.TRUE);
            final TransportConfiguration transportConfig =
                new TransportConfiguration(NettyConnectorFactory.class.getName(), params, "netty-connector");
            configuration.getConnectorConfigurations().put("netty-connector", transportConfig);

            configuration.getConnectorConfigurations().put(
                "in-vm-connector",
                new TransportConfiguration(InVMConnectorFactory.class.getName(), new HashMap<String, Object>(1), "in-vm-connector"));
        }

        {
            /*-
             * <broadcast-groups>
                   <broadcast-group name="my-broadcast-group">
                       <local-bind-address>172.16.9.3</local-bind-address>
                       <local-bind-port>5432</local-bind-port>
                       <group-address>231.7.7.7</group-address>
                       <group-port>9876</group-port>
                       <broadcast-period>2000</broadcast-period>
                       <connector-ref connector-name="netty-connector"/>
                   </broadcast-group>
                </broadcast-groups>
             */

            final Reader reader =
                new UnsynchronizedStringReader("" + 
                    "<configuration xmlns=\"urn:hornetq\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:hornetq /schema/hornetq-configuration.xsd\">" + 
                    "  <broadcast-groups>\n" + 
                    "   <broadcast-group name=\"my-broadcast-group\">\n" + 
                    "       <local-bind-address>172.16.9.3</local-bind-address>\n" + 
                    "       <local-bind-port>5432</local-bind-port>\n" + 
                    "       <group-address>231.7.7.7</group-address>\n" + 
                    "       <group-port>9876</group-port>\n" + 
                    "       <broadcast-period>2000</broadcast-period>\n" + 
                    "       <connector-ref>netty-connector</connector-ref>\n" + 
                    "   </broadcast-group>\n" + 
                    "  </broadcast-groups>\n" + 
                    "</configuration>");
            String xml = org.hornetq.utils.XMLUtil.readerToString(reader);
            xml = XMLUtil.replaceSystemProps(xml);
            final Element e = org.hornetq.utils.XMLUtil.stringToElement(xml);

            final FileConfigurationParser parser = new FileConfigurationParser();
            parser.setValidateAIO(true);
            parser.parseMainConfig(e, configuration);
        }

        // Step 2. Create the JMS configuration
        final JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        // Step 3. Configure the JMS ConnectionFactory
        final ConnectionFactoryConfiguration cfConfig =
            new ConnectionFactoryConfigurationImpl(
                MQConstants.NAME_CONNECTION_FACTORY,
                false,
                Arrays.asList("in-vm-connector"),
                MQConstants.NAME_CONNECTION_FACTORY);
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        // Step 4. Configure the JMS Queue & Topic
        final String queueName = MQConstants.NAME_QUEUE;
        final JMSQueueConfiguration queueConfig =
            new JMSQueueConfigurationImpl(queueName, null, false, MQConstants.PREFIX_QUEUE + queueName);
        jmsConfig.getQueueConfigurations().add(queueConfig);

        final String topicName = MQConstants.NAME_TOPIC;
        final TopicConfiguration topicConfiguration = new TopicConfigurationImpl(topicName, MQConstants.PREFIX_TOPIC + topicName);
        jmsConfig.getTopicConfigurations().add(topicConfiguration);

        // Step 5. Create the JMS Server using the HornetQ core server and the JMS configuration
        final HornetQEmbeddedJMS jmsServer = new HornetQEmbeddedJMS();
        jmsServer.setConfiguration(configuration);
        for (final ConnectionFactoryConfiguration connectionFactoryConfiguration : jmsConfig.getConnectionFactoryConfigurations()) {
            connectionFactoryConfiguration.setBlockOnDurableSend(false);
            connectionFactoryConfiguration.setBlockOnNonDurableSend(false);
        }
        jmsServer.setJmsConfiguration(jmsConfig);
        return jmsServer;
    }

    @Override
    public synchronized void stop() {
        final EmbeddedJMS jmsServer = this.jmsServer;
        if (null != jmsServer) {
            try {
                jmsServer.stop();
            } catch (final Exception e) {
                // Stop failed
                final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(HornetQServerStartup.class));
                log.error("Stopping HornetQ server failed.", e);
            }
            hornetQService = null;
            this.jmsServer = null;
        }
    }

    private static Element stringToElement(final String xml) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6529766
        factory.setNamespaceAware(true);
        final DocumentBuilder parser = factory.newDocumentBuilder();
        final Document doc = parser.parse(new InputSource(new UnsynchronizedStringReader(xml)));
        return doc.getDocumentElement();
    }

    private static String toIpString() {
        try {
            final byte[] ipAddr = InetAddress.getLocalHost().getAddress();
            /*
             * Convert to dot representation
             */
            final StringBuilder ipAddrStr = new StringBuilder(16);
            ipAddrStr.append(ipAddr[0] & 0xFF);
            for (int i = 1; i < ipAddr.length; i++) {
                ipAddrStr.append('.').append(ipAddr[i] & 0xFF);
            }
            return ipAddrStr.toString();
        } catch (final UnknownHostException e) {
            return TransportConstants.DEFAULT_HOST;
        }
    }

}
