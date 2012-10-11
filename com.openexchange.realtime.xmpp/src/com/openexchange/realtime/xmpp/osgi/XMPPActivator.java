
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
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.xmpp.XMPPChannel;
import com.openexchange.realtime.xmpp.XMPPExtension;
import com.openexchange.realtime.xmpp.converter.MessageBodyToXMPP;
import com.openexchange.realtime.xmpp.converter.XMPPToMessageBody;
import com.openexchange.realtime.xmpp.internal.XMPPComponent;
import com.openexchange.realtime.xmpp.internal.XMPPHandler;
import com.openexchange.realtime.xmpp.internal.XMPPServerPipelineFactory;
import com.openexchange.realtime.xmpp.internal.extension.XMPPChatExtension;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

public class XMPPActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
        XMPPChatExtension.SERVICES_REFERENCE.set(this);

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
        XMPPChatExtension.SERVICES_REFERENCE.set(null);
        super.stopBundle();
    }

}
