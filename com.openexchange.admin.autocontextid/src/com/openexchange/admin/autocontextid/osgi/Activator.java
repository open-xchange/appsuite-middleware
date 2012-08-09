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

package com.openexchange.admin.autocontextid.osgi;

import java.sql.SQLException;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import com.openexchange.admin.autocontextid.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.autocontextid.rmi.impl.OXAutoCIDContextImpl;
import com.openexchange.admin.autocontextid.tools.AdminCacheExtended;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    @Override
    public void startBundle() throws Exception {
        try {
            ConfigurationService service = getService(ConfigurationService.class);
            AdminCache.compareAndSet(null, service);
            initCache(service);

            final Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("name", "OXContext");
            LOG.info(OXContextPluginInterface.class.getName());
            registerService(OXContextPluginInterface.class, new OXAutoCIDContextImpl(), props);

        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final OXGenericException e) {
            LOG.fatal(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        unregisterServices();
    }

    private void initCache(final ConfigurationService service) throws SQLException, OXGenericException {
        final AdminCacheExtended cache = new AdminCacheExtended();
        cache.initCache(service);
        cache.initCacheExtended();
        cache.initIDGenerator();
        ClientAdminThreadExtended.cache = cache;
        LOG.info("AutocontextID Bundle: Cache and Pools initialized!");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }
}
