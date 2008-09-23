package com.openexchange.admin.contextrestore.osgi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.openexchange.admin.contextrestore.rmi.OXContextRestoreInterface;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Activator implements BundleActivator {

    private static Registry registry = null;
    
    private static Log log = LogFactory.getLog(Activator.class);
    
    private static OXContextRestore contextRestore = null;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        try {

            registry = AdminDaemon.getRegistry();

            contextRestore = new OXContextRestore();
            final OXContextRestoreInterface oxctxrest_stub = (OXContextRestoreInterface) UnicastRemoteObject.exportObject(contextRestore, 0);

            // bind all NEW Objects to registry
            registry.bind(OXContextRestoreInterface.RMI_NAME, oxctxrest_stub);
            log.info("RMI Interface for context restore bound to RMI registry");
        } catch (final RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final AlreadyBoundException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.fatal("Error while creating one instance for RMI interface", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        try {
            if (null != registry) {
                registry.unbind(OXContextRestoreInterface.RMI_NAME);
            }
        } catch (final AccessException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NotBoundException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

}
