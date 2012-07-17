
package com.openexchange.realtime.example.telnet.osgi;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.example.telnet.TelnetChatPlugin;
import com.openexchange.realtime.example.telnet.TransformingTelnetChatPlugin;
import com.openexchange.realtime.example.telnet.internal.ExtensibleTelnetMessageHandler;
import com.openexchange.realtime.example.telnet.internal.TelnetChannel;
import com.openexchange.realtime.example.telnet.internal.TelnetChatServerPipelineFactory;

public class TelnetChannelActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { SimpleConverter.class, MessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
        TransformingTelnetChatPlugin.services = this;

        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool())); // TODO: Maybe use our ThreadPool in some way

        // Configure the pipeline factory.
        final ExtensibleTelnetMessageHandler handler = new ExtensibleTelnetMessageHandler();
        final TelnetChannel channel = new TelnetChannel();

        track(TelnetChatPlugin.class, new SimpleRegistryListener<TelnetChatPlugin>() {

            @Override
            public void added(ServiceReference<TelnetChatPlugin> ref, TelnetChatPlugin service) {
                handler.add(service);
                channel.addPlugin(service);
            }

            @Override
            public void removed(ServiceReference<TelnetChatPlugin> ref, TelnetChatPlugin service) {
                handler.remove(service);
                channel.removePlugin(service);
            }
        });

        bootstrap.setPipelineFactory(new TelnetChatServerPipelineFactory(handler, channel));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8300));

        registerService(Channel.class, channel);

        openTrackers();
    }

}
