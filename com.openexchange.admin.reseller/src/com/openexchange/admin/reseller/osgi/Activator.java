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

package com.openexchange.admin.reseller.osgi;

import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.impl.OXReseller;
import com.openexchange.admin.reseller.rmi.impl.OXResellerContextImpl;
import com.openexchange.admin.reseller.rmi.impl.OXResellerUserImpl;
import com.openexchange.admin.reseller.rmi.impl.ResellerAuth;
import com.openexchange.admin.reseller.tools.AdminCacheExtended;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);
    
    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        try {
            initCache();

            final OXReseller reseller = new OXReseller();
            OXResellerInterface reseller_stub = (OXResellerInterface) UnicastRemoteObject.exportObject(reseller, 0);
            registerService(Remote.class, reseller_stub, null);
            LOG.info("RMI Interface for reseller bundle bound to RMI registry");

            final Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("name", "BasicAuthenticator");
            LOG.info(BasicAuthenticatorPluginInterface.class.getName());
            registerService(BasicAuthenticatorPluginInterface.class, new ResellerAuth(), props);
            
            props.clear();
            props.put("name", "OXContext");
            LOG.info(OXContextPluginInterface.class.getName());
            registerService(OXContextPluginInterface.class, new OXResellerContextImpl(), props);
            
            props.clear();
            props.put("name", "OXUser");
            LOG.info(OXUserPluginInterface.class.getName());
            registerService(OXUserPluginInterface.class, new OXResellerUserImpl(), props);
            
            track(DatabaseService.class, new DatabaseServiceCustomizer(context, ClientAdminThreadExtended.cache.getPool()));
            openTrackers();
            
        } catch (final StorageException e) {
            LOG.fatal("Error while creating one instance for RMI interface", e);
            throw e;
        } catch (final OXGenericException e) {
            LOG.fatal(e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        closeTrackers();
        unregisterServices();
    }

    private void initCache() throws OXGenericException {
        final AdminCacheExtended cache = new AdminCacheExtended();
        cache.initCache();
        cache.initCacheExtended();
        ClientAdminThreadExtended.cache = cache;
        LOG.info("ResellerBundle: Cache and Pools initialized!");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }
}
