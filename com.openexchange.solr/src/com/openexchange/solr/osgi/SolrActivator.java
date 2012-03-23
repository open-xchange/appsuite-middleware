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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.internal.DelegationSolrAccessService;
import com.openexchange.solr.internal.Services;
import com.openexchange.solr.internal.EmbeddedSolrAccessService;
import com.openexchange.solr.internal.SolrServerRMIImpl;
import com.openexchange.solr.rmi.SolrServerRMI;

/**
* {@link SolrActivator}
* 
* @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
*/
public class SolrActivator extends HousekeepingActivator {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrActivator.class));

    private volatile EmbeddedSolrAccessService managementService;
    
    private static SolrServerRMI solrRMI;
    

    @Override
    protected Class<?>[] getNeededServices() {        
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        final EmbeddedSolrAccessService managementService = this.managementService = new EmbeddedSolrAccessService();
        managementService.startUp();
        final DelegationSolrAccessService delegationService = new DelegationSolrAccessService(managementService);
        registerService(SolrAccessService.class, delegationService);
        registerRMIInterface();
    }
    
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        
        unregisterRMIInterface();
        final EmbeddedSolrAccessService managementService = this.managementService;
        if (managementService != null) {
            managementService.shutDown();
            this.managementService = null;
        }
    }
    
    private void registerRMIInterface() throws UnknownHostException, RemoteException, AlreadyBoundException {
        LOG.info("Registering Solr RMI Interface.");
        final ConfigurationService config = getService(ConfigurationService.class);
        solrRMI = new SolrServerRMIImpl(managementService);
        final SolrServerRMI stub = (SolrServerRMI) UnicastRemoteObject.exportObject(solrRMI, 0);
        final InetAddress addr = InetAddress.getLocalHost();   
        final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
        Registry registry = null;
        try {            
            registry = LocateRegistry.createRegistry(rmiPort, RMISocketFactory.getDefaultSocketFactory(),
                new RMIServerSocketFactory() {
                    @Override
                    public ServerSocket createServerSocket(final int port) throws IOException {
                        final ServerSocket socket = new ServerSocket(port, 0, addr);
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
            registry.bind(SolrServerRMI.RMI_NAME, stub);
        }
    }
    
    private void unregisterRMIInterface() throws UnknownHostException, NotBoundException {
        try {
            LOG.info("Unregistering Solr RMI Interface.");
            final ConfigurationService config = getService(ConfigurationService.class);
            final InetAddress addr = InetAddress.getLocalHost(); 
            final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
            final Registry registry = LocateRegistry.getRegistry(addr.getHostAddress(), rmiPort);
            
            registry.unbind(SolrServerRMI.RMI_NAME);
        } catch (RemoteException r) {
            LOG.error("Could not get RMI registry.", r);
        }
    }

}
