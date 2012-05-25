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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rmi;

import java.lang.reflect.Field;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.rmi.exceptions.OXRMIExceptionCodes;
import com.openexchange.rmi.osgi.RMIActivator;

/**
 * {@link RMIRegistry}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RMIRegistry {

    private static Log log = LogFactory.getLog(RMIRegistry.class);

    private static Registry registry = null;

    private static Lock lock = new ReentrantLock();

    /**
     * Initializes a new {@link RMIRegistry}.
     */
    private RMIRegistry() {
        super();
    }

    public static Registry getRMIRegistry() throws OXException {
        lock.lock();
        try {
            if (registry == null) {
                registry = createRMIRegistry();
            }
            return registry;
        } finally {
            lock.unlock();
        }
    }

    private static Registry createRMIRegistry() throws OXException {
        lock.lock();
        try {
            ConfigurationService configService = RMIActivator.getServiceRegistry().getService(ConfigurationService.class);
            int port = configService.getIntProperty("com.openexchange.rmi.port", 1099);
            String hostname = configService.getProperty("com.openexchange.rmi.host", "localhost");
            if (registry == null) {
                registry = LocateRegistry.createRegistry(port, RMISocketFactory.getDefaultSocketFactory(), new LocalServerFactory(hostname));
            }
            return registry;
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw OXRMIExceptionCodes.RMI_CREATE_REGISTRY_FAILED.create(e);
        } finally {
            lock.unlock();
        }
    }

    public static String findRMIName(ServiceReference<Remote> reference, Remote r) {
        Object name = reference.getProperty("RMIName");
        if (name != null) {
            return (String) name;
        }
        try {
            Field field = r.getClass().getField("RMI_NAME");
            return (String) field.get(r);
        } catch (SecurityException e) {
            return r.getClass().getSimpleName();
        } catch (NoSuchFieldException e) {
            return r.getClass().getSimpleName();
        } catch (IllegalArgumentException e) {
            return r.getClass().getSimpleName();
        } catch (IllegalAccessException e) {
            return r.getClass().getSimpleName();
        }
    }

}
