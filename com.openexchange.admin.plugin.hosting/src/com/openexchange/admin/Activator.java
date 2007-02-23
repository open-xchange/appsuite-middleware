package com.openexchange.admin;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.openexchange.admin.dataSource.I_AdminJobExecutor;
import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXGroup;
import com.openexchange.admin.dataSource.I_OXResource;
import com.openexchange.admin.dataSource.I_OXUser;
import com.openexchange.admin.dataSource.I_OXUtil;
import com.openexchange.admin.rmi.OXContextInterface;

public class Activator implements BundleActivator {

    private static Registry registry = null;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        private static com.openexchange.admin.dataSource.impl.OXContext oxctx = null;
        private static com.openexchange.admin.dataSource.impl.OXUtil oxutil = null;
        private static com.openexchange.admin.dataSource.impl.OXUser oxuser = null;
        private static com.openexchange.admin.dataSource.impl.OXGroup oxgrp = null;
        private static com.openexchange.admin.dataSource.impl.OXResource oxres = null;
        private static com.openexchange.admin.dataSource.impl.AdminJobExecutor ajx = null;

        private static com.openexchange.admin.rmi.impl.OXContext oxctx_v2 = null;
        private static com.openexchange.admin.rmi.impl.OXUtil oxutil_v2 = null;

        // Create all OLD Objects and bind export them
        oxctx = new com.openexchange.admin.dataSource.impl.OXContext();
        I_OXContext oxctx_stub = (I_OXContext) UnicastRemoteObject.exportObject(oxctx, 0);

        oxutil = new com.openexchange.admin.dataSource.impl.OXUtil();
        I_OXUtil oxutil_stub = (I_OXUtil) UnicastRemoteObject.exportObject(oxutil, 0);

        oxuser = new com.openexchange.admin.dataSource.impl.OXUser();
        I_OXUser oxuser_stub = (I_OXUser) UnicastRemoteObject.exportObject(oxuser, 0);

        oxgrp = new com.openexchange.admin.dataSource.impl.OXGroup();
        I_OXGroup oxgrp_stub = (I_OXGroup) UnicastRemoteObject.exportObject(oxgrp, 0);

        oxres = new com.openexchange.admin.dataSource.impl.OXResource();
        I_OXResource oxres_stub = (I_OXResource) UnicastRemoteObject.exportObject(oxres, 0);

        ajx = new com.openexchange.admin.dataSource.impl.AdminJobExecutor();
        ClientAdminThread.ajx = ajx;
        I_AdminJobExecutor ajx_stub = (I_AdminJobExecutor) UnicastRemoteObject.exportObject(ajx, 0);
        // end of OLD exports

        oxctx_v2 = new com.openexchange.admin.rmi.impl.OXContext();
        OXContextInterface oxctx_stub_v2 = (OXContextInterface) UnicastRemoteObject.exportObject(oxctx_v2, 0);

        oxutil_v2 = new com.openexchange.admin.rmi.impl.OXUtil();
        OXUtilInterface oxutil_stub_v2 = (OXUtilInterface) UnicastRemoteObject.exportObject(oxutil_v2, 0);


        // bind OLD objects to registry
        registry.bind(I_OXContext.RMI_NAME, oxctx_stub);
        registry.bind(I_OXUtil.RMI_NAME, oxutil_stub);
        registry.bind(I_OXUser.RMI_NAME, oxuser_stub);
        registry.bind(I_OXGroup.RMI_NAME, oxgrp_stub);
        registry.bind(I_OXResource.RMI_NAME, oxres_stub);
        registry.bind(I_AdminJobExecutor.RMI_NAME, ajx_stub);

        // bind all NEW Objects to registry
        registry.bind(OXContextInterface.RMI_NAME, oxctx_stub_v2);
        registry.bind(OXUtilInterface.RMI_NAME, oxutil_stub_v2);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        if (null != registry) {
            registry.unbind(OXContextInterface.RMI_NAME);
            registry.unbind(OXUtilInterface.RMI_NAME);
            
            registry.unbind(I_OXContext.RMI_NAME);
            registry.unbind(I_OXUtil.RMI_NAME);
            registry.unbind(I_OXUser.RMI_NAME);
            registry.unbind(I_OXGroup.RMI_NAME);
            registry.unbind(I_OXResource.RMI_NAME);
            registry.unbind(I_AdminJobExecutor.RMI_NAME);
        }
    }

}
