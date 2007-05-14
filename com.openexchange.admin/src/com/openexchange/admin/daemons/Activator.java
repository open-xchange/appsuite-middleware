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

import java.security.Permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.plugins.OXUserPluginInterface;

public class Activator implements BundleActivator {

    private static Log log = LogFactory.getLog(AdminDaemon.class);

    private AdminDaemon daemon = null;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        if (null == System.getSecurityManager()) {
            System.setSecurityManager(new SecurityManager() {
                public void checkPermission(Permission perm) {
                }

                public void checkPermission(Permission perm, Object context) {
                }
            });
        }

        this.daemon = new AdminDaemon();
        log.info("Starting Admindaemon...");
        this.daemon.registerBundleListener(context);
        this.daemon.initCache(context);

        this.daemon.initRMI(this.getClass().getClassLoader(), context);
        if(log.isInfoEnabled()){
        log.info("Version: " + Version.MAJOR + "." + Version.MINOR + "." + Version.PATCH);
        log.info("Name: " + Version.NAME);
        log.info("Build: " + Version.BUILD);
        }
        log.info("Admindaemon successfully started.");

        // The listener which is called if a new plugin is registered
        ServiceListener sl = new ServiceListener() {
            public void serviceChanged(ServiceEvent ev) {
                if(log.isInfoEnabled()){
                log.info("Service: " + ev.getServiceReference().getBundle().getSymbolicName() + ", " + ev.getType());
                }

                switch (ev.getType()) {
                    case ServiceEvent.REGISTERED: {
                        // At first we call our own methods inside the new registered plugin...
                        if(log.isInfoEnabled()){
                        log.info(ev.getServiceReference().getBundle().getSymbolicName() + " registered service");
                        }
                        // Code which is executed if a new plugin is registered
//                        TestClass test = (TestClass) context.getService(sr);
//                        test.testmethod();
                    }
                        break;
                    default:
                        break;
                }
            }

        };

        String filter = "(objectclass=" + OXUserPluginInterface.class.getName() + ")";

        try {
            context.addServiceListener(sl, filter);
//            ServiceReference[] srl = context.getServiceReferences(null, filter);
//            for (int i = 0; srl != null && i < srl.length; i++) {
//                sl.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, srl[i]));
//            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        log.info("Stopping RMI...");
        this.daemon.unregisterRMI();
        log.info("Thanks for using Open-Xchange AdminDaemon");
    }

}
