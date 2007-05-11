package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OXAdminCoreInterface extends Remote {
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "OXAdminCore";

    /**
     * This methods checks if all plugins have been loaded successfully
     * 
     * @return true if all plugins are loaded successfully
     *         false if don't
     * @throws RemoteException 
     */
    public boolean allPluginsLoaded() throws RemoteException;
}
