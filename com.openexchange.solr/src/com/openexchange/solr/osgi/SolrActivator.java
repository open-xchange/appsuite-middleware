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
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
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
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.service.messaging.MessagingService;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreConfigService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrMBean;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.groupware.SolrCoreLoginHandler;
import com.openexchange.solr.groupware.SolrCoresCreateTableService;
import com.openexchange.solr.groupware.SolrCoresCreateTableTask;
import com.openexchange.solr.internal.DelegationSolrAccessImpl;
import com.openexchange.solr.internal.EmbeddedSolrAccessImpl;
import com.openexchange.solr.internal.MessagingConstants;
import com.openexchange.solr.internal.RMISolrAccessImpl;
import com.openexchange.solr.internal.Services;
import com.openexchange.solr.internal.SolrCoreConfigServiceImpl;
import com.openexchange.solr.internal.SolrMBeanImpl;
import com.openexchange.solr.rmi.RMISolrAccessService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link SolrActivator}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrActivator extends HousekeepingActivator {

	static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrActivator.class));

	private volatile EmbeddedSolrAccessImpl embeddedAccess;

	private static RMISolrAccessService solrRMI;

    private SolrMBean solrMBean;

    private ObjectName solrMBeanName;

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class, DatabaseService.class, MessagingService.class, ThreadPoolService.class };
	}

	@Override
	protected void startBundle() throws OXException {
		Services.setServiceLookup(this);
		new CheckConfigDBTables(getService(DatabaseService.class)).checkTables();
		EmbeddedSolrAccessImpl embeddedAccess = this.embeddedAccess = new EmbeddedSolrAccessImpl();
		embeddedAccess.startUp();
		DelegationSolrAccessImpl accessService = new DelegationSolrAccessImpl(embeddedAccess);
		registerService(SolrAccessService.class, accessService);
		SolrCoreConfigServiceImpl coreService = new SolrCoreConfigServiceImpl();
		registerService(SolrCoreConfigService.class, coreService);
		addService(SolrCoreConfigService.class, coreService);
		registerRMIInterface();

		SolrCoresCreateTableService createTableService = new SolrCoresCreateTableService();
		registerService(CreateTableService.class, createTableService);
		registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new SolrCoresCreateTableTask(createTableService)));
		// new SolrCoreStoresCreateTableTask()
		registerService(LoginHandlerService.class, new SolrCoreLoginHandler(embeddedAccess));
		registerEventHandler();
		registerMBean(coreService);		
		openTrackers();
	}

	@Override
	protected void stopBundle() throws Exception {
		super.stopBundle();

		unregisterRMIInterface();
		ManagementService managementService = Services.optService(ManagementService.class);
		if (managementService != null && solrMBeanName != null) {
		    managementService.unregisterMBean(solrMBeanName);
		    solrMBean = null;
		}
		EmbeddedSolrAccessImpl embeddedAccess = this.embeddedAccess;
		if (embeddedAccess != null) {
			embeddedAccess.shutDown();
			this.embeddedAccess = null;
		}
	}
	
	private void registerMBean(SolrCoreConfigServiceImpl coreService) {
	    try {
            solrMBeanName = new ObjectName(SolrMBean.DOMAIN, "name", "Solr Control");
            solrMBean = new SolrMBeanImpl(embeddedAccess, coreService);
            track(ManagementService.class, new SimpleRegistryListener<ManagementService>() {

                @Override
                public void added(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.registerMBean(solrMBeanName, solrMBean);
                    } catch (OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                @Override
                public void removed(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.unregisterMBean(solrMBeanName);
                    } catch (OXException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
        } catch (MalformedObjectNameException e) {
            LOG.error(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            LOG.error(e.getMessage(), e);
        }
	}
	
	private void registerEventHandler() {
	    Dictionary<String, Object> ht = new Hashtable<String, Object>();
        ht.put(EventConstants.EVENT_TOPIC, new String[] { MessagingConstants.START_CORE_TOPIC });
        EventHandler startCoreEventHandler = new EventHandler() {         
            @Override
            public void handleEvent(Event event) {
                String topic = event.getTopic();
                if (topic.equals(MessagingConstants.START_CORE_TOPIC)) {
                    Object property = event.getProperty(MessagingConstants.PROP_IDENTIFIER);
                    if (property != null && property instanceof SolrCoreIdentifier) {
                        SolrCoreIdentifier identifier = (SolrCoreIdentifier) property;
                        try {
                            ConfigurationService config = Services.getService(ConfigurationService.class);
                            boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
                            if (isSolrNode && !embeddedAccess.hasActiveCore(identifier)) {
                                embeddedAccess.startCore(identifier);
                            }
                        } catch (OXException e) {
                            LOG.error("Could not start solr core.", e);
                        }                       
                    }
                }               
            }
        };      
        registerService(EventHandler.class, startCoreEventHandler);
	}

	private void registerRMIInterface() {
		LOG.info("Registering Solr RMI Interface.");
		ConfigurationService config = getService(ConfigurationService.class);
		EmbeddedSolrAccessImpl embeddedAccess = this.embeddedAccess;
		solrRMI = new RMISolrAccessImpl(embeddedAccess);
        try {
            RMISolrAccessService stub = (RMISolrAccessService) UnicastRemoteObject.exportObject(solrRMI, 0);
            final InetAddress addr = InetAddress.getLocalHost();
            int rmiPort = config.getIntProperty("RMI_PORT", 1099);
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(rmiPort, RMISocketFactory.getDefaultSocketFactory(), new RMIServerSocketFactory() {
                    @Override
                    public ServerSocket createServerSocket(int port) throws IOException {
                        ServerSocket socket = new ServerSocket(port, 0, addr);
                        return socket;
                    }

                });
            } catch (RemoteException e) {
                LOG.info("RMI registry seems to be already exported.");
                try {
                    registry = LocateRegistry.getRegistry(addr.getHostAddress(), rmiPort);
                } catch (RemoteException r) {
                    LOG.error("Could not get RMI registry. SolrServerRMI will not be registered!", r);
                    solrRMI = null;
                }
            }

            if (registry != null) {
                registry.bind(RMISolrAccessService.RMI_NAME, stub);
            }
        } catch (RemoteException e) {
            LOG.error(e.getMessage(), e);
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        } catch (AlreadyBoundException e) {
            LOG.error(e.getMessage(), e);
        }
		
	}

	private void unregisterRMIInterface() throws UnknownHostException, NotBoundException {
		try {
			LOG.info("Unregistering Solr RMI Interface.");
			ConfigurationService config = getService(ConfigurationService.class);
			InetAddress addr = InetAddress.getLocalHost();
			int rmiPort = config.getIntProperty("RMI_PORT", 1099);
			Registry registry = LocateRegistry.getRegistry(addr.getHostAddress(), rmiPort);

			registry.unbind(RMISolrAccessService.RMI_NAME);
		} catch (RemoteException r) {
			LOG.error("Could not get RMI registry.", r);
		}
	}

}
