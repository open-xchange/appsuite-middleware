package com.openexchange.admin;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.dataSource.I_AdminJobExecutor;
import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXGroup;
import com.openexchange.admin.dataSource.I_OXResource;
import com.openexchange.admin.dataSource.I_OXUser;
import com.openexchange.admin.dataSource.I_OXUtil;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.AdminJobExecutorInterface;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.monitoring.MonitorAgent;

public class Activator implements BundleActivator {

//    private static Log log = LogFactory.getLog(Activator.class);
    
    private static Registry registry = null;
//
    private static com.openexchange.admin.dataSource.impl.OXContext oxctx = null;
    private static com.openexchange.admin.dataSource.impl.OXUtil oxutil = null;
    private static com.openexchange.admin.dataSource.impl.OXUser oxuser = null;
    private static com.openexchange.admin.dataSource.impl.OXGroup oxgrp = null;
    private static com.openexchange.admin.dataSource.impl.OXResource oxres = null;
    private static com.openexchange.admin.dataSource.impl.AdminJobExecutor ajx = null;

    private static com.openexchange.admin.rmi.impl.OXContext oxctx_v2 = null;
    private static com.openexchange.admin.rmi.impl.OXUtil oxutil_v2 = null;
    private static com.openexchange.admin.rmi.impl.AdminJobExecutor ajx_v2 = null;

    private static MonitorAgent moni = null;
    private static PropertyHandler prop = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
//        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        PropertyHandler prop = AdminDaemon.getProp();
        int rmi_port = prop.getRmiProp(AdminProperties.RMI.RMI_PORT, 1099);
        registry = LocateRegistry.getRegistry(rmi_port);

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
        ClientAdminThreadExtended.ajx = ajx;
        I_AdminJobExecutor ajx_stub = (I_AdminJobExecutor) UnicastRemoteObject.exportObject(ajx, 0);
        // end of OLD exports

        oxctx_v2 = new com.openexchange.admin.rmi.impl.OXContext();
        OXContextInterface oxctx_stub_v2 = (OXContextInterface) UnicastRemoteObject.exportObject(oxctx_v2, 0);

        oxutil_v2 = new com.openexchange.admin.rmi.impl.OXUtil();
        OXUtilInterface oxutil_stub_v2 = (OXUtilInterface) UnicastRemoteObject.exportObject(oxutil_v2, 0);

        ajx_v2 = new com.openexchange.admin.rmi.impl.AdminJobExecutor();
        ClientAdminThreadExtended.ajx = ajx_v2;
        AdminJobExecutorInterface ajx_stub_v2 = (AdminJobExecutorInterface) UnicastRemoteObject.exportObject(ajx_v2, 0);

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
        registry.bind(AdminJobExecutorInterface.RMI_NAME, ajx_stub_v2);

//        startJMX();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
//        stopJMX();
        if (null != registry) {
            registry.unbind(OXContextInterface.RMI_NAME);
            registry.unbind(OXUtilInterface.RMI_NAME);
            registry.unbind(AdminJobExecutorInterface.RMI_NAME);

            registry.unbind(I_OXContext.RMI_NAME);
            registry.unbind(I_OXUtil.RMI_NAME);
            registry.unbind(I_OXUser.RMI_NAME);
            registry.unbind(I_OXGroup.RMI_NAME);
            registry.unbind(I_OXResource.RMI_NAME);
            registry.unbind(I_AdminJobExecutor.RMI_NAME);
        }
    }

    private void startJMX() {
        int jmx_port = Integer.parseInt(prop.getProp("JMX_PORT", "9998"));
        moni = new MonitorAgent(jmx_port);
        moni.start();

        String servername = prop.getProp(AdminProperties.Prop.SERVER_NAME, "local");
//        log.info("Admindaemon Name: " + servername);
    }
    
    private void stopJMX() {
        moni.stop();
    }
}
