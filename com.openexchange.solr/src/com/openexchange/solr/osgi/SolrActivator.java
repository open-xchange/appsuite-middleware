package com.openexchange.solr.osgi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.log.LogFactory;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.service.messaging.MessagingService;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreConfigService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.groupware.SolrCoreLoginHandler;
import com.openexchange.solr.groupware.SolrCoreStoresCreateTableTask;
import com.openexchange.solr.groupware.SolrCoresCreateTableService;
import com.openexchange.solr.groupware.SolrCoresCreateTableTask;
import com.openexchange.solr.internal.DelegationSolrAccessImpl;
import com.openexchange.solr.internal.EmbeddedSolrAccessImpl;
import com.openexchange.solr.internal.MessagingConstants;
import com.openexchange.solr.internal.RMISolrAccessImpl;
import com.openexchange.solr.internal.Services;
import com.openexchange.solr.internal.SolrCoreConfigServiceImpl;
import com.openexchange.solr.rmi.RMISolrAccessService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link SolrActivator}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrActivator extends HousekeepingActivator {

	static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrActivator.class));

	private volatile EmbeddedSolrAccessImpl embeddedAccess;

	private static RMISolrAccessService solrRMI;

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class, DatabaseService.class, MessagingService.class, ThreadPoolService.class };
	}

	@Override
	protected void startBundle() throws Exception {
		Services.setServiceLookup(this);
		final EmbeddedSolrAccessImpl embeddedAccess = this.embeddedAccess = new EmbeddedSolrAccessImpl();
		embeddedAccess.startUp();
		final DelegationSolrAccessImpl accessService = new DelegationSolrAccessImpl(embeddedAccess);
		registerService(SolrAccessService.class, accessService);
		final SolrCoreConfigServiceImpl coreService = new SolrCoreConfigServiceImpl();
		registerService(SolrCoreConfigService.class, coreService);
		addService(SolrCoreConfigService.class, coreService);
		registerRMIInterface();

		final SolrCoresCreateTableService createTableService = new SolrCoresCreateTableService();
		registerService(CreateTableService.class, createTableService);
		registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new SolrCoreStoresCreateTableTask(), new SolrCoresCreateTableTask(createTableService)));
		registerService(LoginHandlerService.class, new SolrCoreLoginHandler(embeddedAccess));
	
		final Dictionary<String, Object> ht = new Hashtable<String, Object>();
        ht.put(EventConstants.EVENT_TOPIC, new String[] { MessagingConstants.START_CORE_TOPIC });
		final EventHandler startCoreEventHandler = new EventHandler() {			
			@Override
			public void handleEvent(final Event event) {
				final String topic = event.getTopic();
				if (topic.equals(MessagingConstants.START_CORE_TOPIC)) {
					final Object property = event.getProperty(MessagingConstants.PROP_IDENTIFIER);
					if (property != null && property instanceof SolrCoreIdentifier) {
						final SolrCoreIdentifier identifier = (SolrCoreIdentifier) property;
						try {
							final ConfigurationService config = Services.getService(ConfigurationService.class);
							final boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
							if (isSolrNode && !embeddedAccess.hasActiveCore(identifier)) {
								embeddedAccess.startCore(identifier);
							}
						} catch (final OXException e) {
							LOG.error("Could not start solr core.", e);
						}						
					}
				}				
			}
		};
		
		registerService(EventHandler.class, startCoreEventHandler);
	}

	@Override
	protected void stopBundle() throws Exception {
		super.stopBundle();

		unregisterRMIInterface();
		final EmbeddedSolrAccessImpl embeddedAccess = this.embeddedAccess;
		if (embeddedAccess != null) {
			embeddedAccess.shutDown();
			this.embeddedAccess = null;
		}
	}

	private void registerRMIInterface() throws UnknownHostException, RemoteException, AlreadyBoundException {
		LOG.info("Registering Solr RMI Interface.");
		final ConfigurationService config = getService(ConfigurationService.class);
		final EmbeddedSolrAccessImpl embeddedAccess = this.embeddedAccess;
		solrRMI = new RMISolrAccessImpl(embeddedAccess);
		final RMISolrAccessService stub = (RMISolrAccessService) UnicastRemoteObject.exportObject(solrRMI, 0);
		final InetAddress addr = InetAddress.getLocalHost();
		final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
		Registry registry = null;
		try {
			registry = LocateRegistry.createRegistry(rmiPort, RMISocketFactory.getDefaultSocketFactory(), new RMIServerSocketFactory() {
				@Override
				public ServerSocket createServerSocket(final int port) throws IOException {
					final ServerSocket socket = new ServerSocket(port, 0, addr);
					return socket;
				}

			});
		} catch (final RemoteException e) {
			LOG.info("RMI registry seems to be already exported.");
			try {
				registry = LocateRegistry.getRegistry(addr.getHostAddress(), rmiPort);
			} catch (final RemoteException r) {
				LOG.error("Could not get RMI registry. SolrServerRMI will not be registered!", r);
				solrRMI = null;
			}
		}

		if (registry != null) {
			registry.bind(RMISolrAccessService.RMI_NAME, stub);
		}
	}

	private void unregisterRMIInterface() throws UnknownHostException, NotBoundException {
		try {
			LOG.info("Unregistering Solr RMI Interface.");
			final ConfigurationService config = getService(ConfigurationService.class);
			final InetAddress addr = InetAddress.getLocalHost();
			final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
			final Registry registry = LocateRegistry.getRegistry(addr.getHostAddress(), rmiPort);

			registry.unbind(RMISolrAccessService.RMI_NAME);
		} catch (final RemoteException r) {
			LOG.error("Could not get RMI registry.", r);
		}
	}

}
