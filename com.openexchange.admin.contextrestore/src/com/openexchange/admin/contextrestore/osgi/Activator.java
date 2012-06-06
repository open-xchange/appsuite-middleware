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

package com.openexchange.admin.contextrestore.osgi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.admin.contextrestore.rmi.OXContextRestoreInterface;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXContext;
import com.openexchange.log.LogFactory;

public class Activator implements BundleActivator {
    
    private static Log log = LogFactory.getLog(Activator.class);
    
    private static OXContextRestore contextRestore = null;
    
    private static OXContextInterface ox_ctx = null;
    
    private ServiceRegistration<Remote> registration = null;

    public void start(final BundleContext context) throws Exception {
        try {
            ox_ctx = new OXContext(context);
            contextRestore = new OXContextRestore();
            final OXContextRestoreInterface oxctxrest_stub = (OXContextRestoreInterface) UnicastRemoteObject.exportObject(contextRestore, 0);

            // bind all NEW Objects to registry
            registration = context.registerService(Remote.class, oxctxrest_stub, null);
            log.info("RMI Interface for context restore bound to RMI registry");
        } catch (final RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.fatal("Error while creating one instance for RMI interface", e);
            throw e;
        }
    }

    public void stop(final BundleContext context) throws Exception {
        context.ungetService(registration.getReference());
    }

    public static final OXContextInterface getContextInterface() {
        return ox_ctx;
    }
    
}
