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

package com.openexchange.admin;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;

public class PluginStarter {

    private static Log LOG = LogFactory.getLog(PluginStarter.class);

    private static com.openexchange.admin.rmi.impl.OXContext oxctx_v2 = null;

    private static com.openexchange.admin.rmi.impl.OXUtil oxutil_v2 = null;

    private static PropertyHandlerExtended prop = null;

    private BundleContext context;

    private final List<ServiceRegistration<Remote>> services = new ArrayList<ServiceRegistration<Remote>>();

    public PluginStarter() {
        super();
    }

    public void start(final BundleContext context, final ConfigurationService service) throws RemoteException, AlreadyBoundException, StorageException, OXGenericException {
        try {
            this.context = context;
            initCache(service);

            LogProperties.putLogProperty("__configurationService", service);

            // Create all OLD Objects and bind export them
            oxctx_v2 = new com.openexchange.admin.rmi.impl.OXContext(context);
            final OXContextInterface oxctx_stub_v2 = (OXContextInterface) UnicastRemoteObject.exportObject(oxctx_v2, 0);

            oxutil_v2 = new com.openexchange.admin.rmi.impl.OXUtil();
            final OXUtilInterface oxutil_stub_v2 = (OXUtilInterface) UnicastRemoteObject.exportObject(oxutil_v2, 0);

            // bind all NEW Objects to registry
            services.add(context.registerService(Remote.class, oxctx_stub_v2, null));
            services.add(context.registerService(Remote.class, oxutil_stub_v2, null));

            // startJMX();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading context implementation: " + prop.getProp(PropertyHandlerExtended.CONTEXT_STORAGE, null));
                LOG.debug("Loading util implementation: " + prop.getProp(PropertyHandlerExtended.UTIL_STORAGE, null));
            }
        } catch (final RemoteException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            LOG.fatal("Error while creating one instance for RMI interface", e);
            throw e;
        } finally {
            LogProperties.putLogProperty("__configurationService", null);
        }
    }

    public void stop() throws AccessException, RemoteException, NotBoundException {
        for (ServiceRegistration<Remote> registration : services) {
            context.ungetService(registration.getReference());
        }
    }

    private void initCache(final ConfigurationService service) throws OXGenericException {
        final AdminCacheExtended cache = new AdminCacheExtended();
        cache.initCache(service);
        cache.initCacheExtended();
        ClientAdminThreadExtended.cache = cache;
        prop = cache.getProperties();
        LOG.info("Cache and Pools initialized!");
    }
}
