/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package com.openexchange.admin.daemons;

import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.OXAdminCoreInterface;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXAdminCoreImpl;
import com.openexchange.admin.rmi.impl.OXTaskMgmtImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.Permission;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.PropertyHandler.PropertyFiles;

public class AdminDaemon {

    private static final Log log = LogFactory.getLog(AdminDaemon.class);

    private static PropertyHandler prop = null;
    private AdminCache cache = null;
    private static Registry registry = null;
    private static ArrayList<Bundle> bundlelist = null;
    
    private static com.openexchange.admin.rmi.impl.OXUser oxuser_v2 = null;
    private static com.openexchange.admin.rmi.impl.OXGroup oxgrp_v2 = null;
    private static com.openexchange.admin.rmi.impl.OXResource oxres_v2 = null;
    private static com.openexchange.admin.rmi.impl.OXLogin oxlogin_v2 = null;
    private static OXAdminCoreImpl oxadmincore = null;
    private static OXTaskMgmtImpl oxtaskmgmt = null;

    public class LocalServerFactory implements RMIServerSocketFactory {
        public ServerSocket createServerSocket(final int port) throws IOException {
            String hostname_property = null;
            try {
                hostname_property = ClientAdminThread.cache.getProperties().getString(PropertyFiles.ADMIN, AdminProperties.Prop.BIND_ADDRESS);
            } catch (InvalidDataException e) {
                log.fatal("Invalid data in config file", e);
                System.exit(1);
            }
            if ("0".equalsIgnoreCase(hostname_property)) {
                if(log.isInfoEnabled()){
                    log.info("Admindaemon will listen on all network devices!");
                }
                return new ServerSocket(port, 0, null);
            } else {
                if(log.isInfoEnabled()){
                    log.info("Admindaemon will listen on "+hostname_property+"!");
                }
                return new ServerSocket(port, 0, InetAddress.getByName(hostname_property));
            }
        }
    }

    public void registerBundleListener(final BundleContext context) {
        final BundleListener bl = new BundleListener() {
            public void bundleChanged(final BundleEvent event) {
                if (event.getType() == BundleEvent.STARTED) {
                    bundlelist.add(event.getBundle());
                } else if (event.getType() == BundleEvent.STOPPED) {
                    bundlelist.remove(event.getBundle());
                }
                if(log.isInfoEnabled()){
                log.info(event.getBundle().getSymbolicName() + " changed to " + event.getType());
                }
            }
        };
        context.addBundleListener(bl);
    }

    public void initCache(final BundleContext context) {
        try {
            bundlelist = new ArrayList<Bundle>();
            this.cache = new AdminCache();
            this.cache.initCache();
            ClientAdminThread.cache = this.cache;
            prop = this.cache.getProperties();
            log.info("Cache and Pools initialized!");
        } catch (final InvalidDataException e) {
            log.fatal("Invalid data in config file", e);
            shutdown();
        }
    }

    public void initRMI(final ClassLoader loader, final BundleContext context) {

        try {
            if (null == System.getSecurityManager()) {
                System.setSecurityManager(new SecurityManager() {
                    @Override
                    public void checkPermission(final Permission perm) {
                    }

                    @Override
                    public void checkPermission(final Permission perm, final Object context) {
                    }
                });
            }
            Thread.currentThread().setContextClassLoader(loader);
            
            final int rmi_port = prop.getInt(PropertyFiles.RMI, AdminProperties.RMI.RMI_PORT);
            try {
                // Use SslRMIServerSocketFactory for SSL here
                registry = LocateRegistry.createRegistry(rmi_port, RMISocketFactory.getDefaultSocketFactory(), new LocalServerFactory());
            } catch (final RemoteException e) {
                // if a registry has be already created in this osgi framework
                // we just need to get it from the port (normally this happens
                // on restarting
                registry = LocateRegistry.getRegistry(ClientAdminThread.cache.getProperties().getString(PropertyFiles.ADMIN, AdminProperties.Prop.BIND_ADDRESS), rmi_port);
            }

            // Now export all NEW Objects
            oxuser_v2 = new com.openexchange.admin.rmi.impl.OXUser(context);
            final OXUserInterface oxuser_stub_v2 = (OXUserInterface) UnicastRemoteObject.exportObject(oxuser_v2, 0);

            oxgrp_v2 = new com.openexchange.admin.rmi.impl.OXGroup(context);
            final OXGroupInterface oxgrp_stub_v2 = (OXGroupInterface) UnicastRemoteObject.exportObject(oxgrp_v2, 0);

            oxres_v2 = new com.openexchange.admin.rmi.impl.OXResource(context);
            final OXResourceInterface oxres_stub_v2 = (OXResourceInterface) UnicastRemoteObject.exportObject(oxres_v2, 0);
            
            oxlogin_v2 = new com.openexchange.admin.rmi.impl.OXLogin(context);
            final OXLoginInterface oxlogin_stub_v2 = (OXLoginInterface)UnicastRemoteObject.exportObject(oxlogin_v2, 0);
            
            oxadmincore = new OXAdminCoreImpl(context);
            final OXAdminCoreInterface oxadmincore_stub = (OXAdminCoreInterface)UnicastRemoteObject.exportObject(oxadmincore, 0);
            
            oxtaskmgmt = new OXTaskMgmtImpl();
            final OXTaskMgmtInterface oxtaskmgmt_stub = (OXTaskMgmtInterface) UnicastRemoteObject.exportObject(oxtaskmgmt, 0);
            // END of NEW export

            // bind all NEW Objects to registry
    	    registry.bind(OXUserInterface.RMI_NAME, oxuser_stub_v2);
    	    registry.bind(OXGroupInterface.RMI_NAME, oxgrp_stub_v2);
    	    registry.bind(OXResourceInterface.RMI_NAME, oxres_stub_v2);
    	    registry.bind(OXLoginInterface.RMI_NAME, oxlogin_stub_v2);
    	    registry.bind(OXAdminCoreInterface.RMI_NAME, oxadmincore_stub);
    	    registry.bind(OXTaskMgmtInterface.RMI_NAME, oxtaskmgmt_stub);
    	} catch (final RemoteException e) {
            log.fatal("Error creating RMI registry!",e);
            shutdown();
        } catch (final AlreadyBoundException e) {
            log.fatal("One RMI name is already bound!", e);
            shutdown();
        } catch (final StorageException e) {
            log.fatal("Error while creating one instance for RMI interface", e);
            shutdown();
        } catch (final InvalidDataException e) {
            log.fatal("Invalid data in config file", e);
            shutdown();
        }
    }

    public void unregisterRMI() {
        try {
            registry.unbind(OXUserInterface.RMI_NAME);
            registry.unbind(OXGroupInterface.RMI_NAME);
            registry.unbind(OXResourceInterface.RMI_NAME);
            registry.unbind(OXLoginInterface.RMI_NAME);
            registry.unbind(OXAdminCoreInterface.RMI_NAME);
            registry.unbind(OXTaskMgmtInterface.RMI_NAME);
        } catch (final AccessException e) {
            log.error("Error unregistering RMI", e);
        } catch (final RemoteException e) {
            log.error("Error unregistering RMI", e);
        } catch (final NotBoundException e) {
            log.error("Error unregistering RMI", e);
        }
    }

    public static final Registry getRegistry() {
        return registry;
    }

    public static PropertyHandler getProp() {
        return prop;
    }
    
    /**
     * This method must now be called to shutdown the whole server
     */
    public static void shutdown() {
        System.exit(1);
    }

    public static final ArrayList<Bundle> getBundlelist() {
        return bundlelist;
    }
}
