package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.rmi.OXAdminCoreInterface;

public class OXAdminCoreImpl implements OXAdminCoreInterface {

    private BundleContext context = null;
    
    
    /**
     * @param context
     */
    public OXAdminCoreImpl(BundleContext context) {
        super();
        this.context = context;
    }


    public boolean allPluginsLoaded() throws RemoteException {
        final ArrayList<Bundle> bundlelist = AdminDaemon.getBundlelist();
        final Bundle[] bundles = context.getBundles();
        final List<Bundle> allbundlelist = Arrays.asList(bundles);
        // First one is the system bundle, which is always loaded, so we ignore it here. As we cannot remove
        // in this type of list we make a new one...
        final List<Bundle> subList = allbundlelist.subList(1, allbundlelist.size());
        
        return bundlelist.containsAll(subList);
    }

}
