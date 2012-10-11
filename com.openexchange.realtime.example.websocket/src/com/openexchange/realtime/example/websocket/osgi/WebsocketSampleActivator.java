package com.openexchange.realtime.example.websocket.osgi;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.osgi.framework.ServiceReference;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.example.websocket.ConversionWSHandler;
import com.openexchange.realtime.example.websocket.WSHandler;
import com.openexchange.realtime.example.websocket.internal.HandlerLibrary;
import com.openexchange.realtime.example.websocket.internal.WSChannel;
import com.openexchange.realtime.example.websocket.internal.WebSocketServerPipelineFactory;
import com.openexchange.sessiond.SessiondService;

public class WebsocketSampleActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{SessiondService.class, MessageDispatcher.class};
	}

	@Override
	protected void startBundle() throws Exception {
		ConversionWSHandler.services = this;
		
		final HandlerLibrary library = new HandlerLibrary();
		
		track(WSHandler.class, new SimpleRegistryListener<WSHandler>() {

			@Override
			public void added(ServiceReference<WSHandler> ref,
					WSHandler service) {
				library.add(service);
			}

			@Override
			public void removed(ServiceReference<WSHandler> ref,
					WSHandler service) {
				library.remove(service);
			}
		});
		
        WSChannel channel = new WSChannel(library);

        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(channel, library, this));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8031));
        
		registerService(Channel.class, channel);
        
        openTrackers();

	}

}
