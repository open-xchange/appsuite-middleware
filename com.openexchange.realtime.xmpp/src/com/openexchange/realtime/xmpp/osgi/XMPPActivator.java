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


package com.openexchange.realtime.xmpp.osgi;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.xmpp.XMPPChannel;
import com.openexchange.realtime.xmpp.XMPPExtension;
import com.openexchange.realtime.xmpp.converter.MessageBodyToXMPP;
import com.openexchange.realtime.xmpp.converter.XMPPToMessageBody;
import com.openexchange.realtime.xmpp.internal.XMPPComponent;
import com.openexchange.realtime.xmpp.internal.XMPPHandler;
import com.openexchange.realtime.xmpp.internal.XMPPServerPipelineFactory;
import com.openexchange.realtime.xmpp.internal.extension.XMPPChatExtension;
import com.openexchange.realtime.xmpp.packet.XMPPStanza;
import com.openexchange.realtime.xmpp.transformer.XMPPPayloadElementTransformer;
import com.openexchange.realtime.xmpp.transformer.XMPPPayloadTreeTransformer;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

public class XMPPActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
        XMPPChatExtension.SERVICES.set(this);
        XMPPStanza.SERVICES.set(this);
        XMPPPayloadElementTransformer.SERVICES.set(this);

        final XMPPChannel channel = new XMPPChannel();
        final XMPPHandler handler = new XMPPHandler();

        track(XMPPExtension.class, new SimpleRegistryListener<XMPPExtension>() {

            @Override
            public void added(ServiceReference<XMPPExtension> ref, XMPPExtension service) {
                channel.addExtension(service);

                for (String resource : service.getComponents()) {
                    XMPPComponent component = handler.getComponent(resource);
                    if (component == null) {
                        component = new XMPPComponent(resource);
                    }
                    component.putExtension(service);
                    handler.addComponent(component);
                }
                
                for (XMPPPayloadElementTransformer transformer : service.getElementTransformers()) {
                    XMPPPayloadTreeTransformer.transformers.put(transformer.getElementPath(), transformer);
                }
            }

            @Override
            public void removed(ServiceReference<XMPPExtension> ref, XMPPExtension service) {
                channel.removeExtension(service);

                Set<String> toRemove = new HashSet<String>();
                for (String resource : service.getComponents()) {
                    XMPPComponent component = handler.getComponent(resource);
                    if (component != null) {
                        component.removeExtension(service.getServiceName());
                        if (component.getExtensions().isEmpty()) {
                            toRemove.add(resource);
                        }
                    }
                }
                for (String remove : toRemove) {
                    handler.removeComponent(remove);
                }
                
                for (XMPPPayloadElementTransformer transformer : service.getElementTransformers()) {
                    XMPPPayloadTreeTransformer.transformers.remove(transformer.getElementPath());
                }
            }
        });
        openTrackers();

        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        ChannelFactory factory = new NioServerSocketChannelFactory(threadPool.getExecutor(), threadPool.getExecutor());
        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new XMPPServerPipelineFactory(channel, handler));

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.bind(new InetSocketAddress(5222));

        // Converter registration
        registerService(SimplePayloadConverter.class, new MessageBodyToXMPP());
        registerService(SimplePayloadConverter.class, new XMPPToMessageBody());

        // Channel registration
        registerService(Channel.class, channel);

        // Core Extensions registration
        registerService(XMPPExtension.class, new XMPPChatExtension());

    }

    @Override
    protected void stopBundle() throws Exception {
        XMPPChatExtension.SERVICES.set(null);
        super.stopBundle();
    }

}
