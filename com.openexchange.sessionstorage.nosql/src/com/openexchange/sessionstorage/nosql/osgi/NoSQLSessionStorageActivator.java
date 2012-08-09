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

package com.openexchange.sessionstorage.nosql.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.nosql.cassandra.EmbeddedCassandraService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.nosql.NoSQLSessionStorageConfiguration;
import com.openexchange.sessionstorage.nosql.NoSQLSessionStorageService;
import com.openexchange.sessionstorage.nosql.exceptions.OXNoSQLSessionStorageExceptionCodes;

/**
 * {@link NoSQLSessionStorageActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class NoSQLSessionStorageActivator extends HousekeepingActivator {

    private final Class<?>[] needed = { ConfigurationService.class, EmbeddedCassandraService.class, CryptoService.class };
    private NoSQLSessionStorageService service;
    private ConfigurationService configService;
    private CryptoService cryptoService;
    private ServiceRegistry registry;
    private final Log log = LogFactory.getLog(NoSQLSessionStorageActivator.class);

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void startBundle() throws Exception {
        log.info("Starting bundle: com.openexchange.sessionstorage.loxandra");
        configService = getService(ConfigurationService.class);
        cryptoService = getService(CryptoService.class);
        registry = NoSQLServiceRegistry.getRegistry();
        registry.addService(ConfigurationService.class, configService);
        registry.addService(CryptoService.class, cryptoService);
        boolean enabled = configService.getBoolProperty("com.openexchange.sessionstorage.nosql.enabled", false);
        if (enabled) {
            String host = configService.getProperty("com.openexchange.sessionstorage.nosql.host", "localhost");
            int port = configService.getIntProperty("com.openexchange.sessionstorage.nosql.port", 9160);
            String keyspace = configService.getProperty("com.openexchange.sessionstorage.nosql.keyspace", "ox");
            String cf_name = configService.getProperty("com.openexchange.sessionstorage.nosql.cfname", "sessionstorage");
            String encryptionKey = configService.getProperty("com.openexchange.sessionstorage.nosql.encryptionKey");
            if (encryptionKey == null) {
                OXException e = OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_NO_ENCRYPTION_KEY.create();
                log.error(e.getMessage(), e);
                throw e;
            }
            NoSQLSessionStorageConfiguration config = new NoSQLSessionStorageConfiguration(host, port, keyspace, cf_name, encryptionKey, cryptoService);
            service = new NoSQLSessionStorageService(config);
            registerService(SessionStorageService.class, service);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stopBundle() throws Exception {
        log.info("Stopping bundle: com.openexchange.sessionstorage.loxandra");
        if (service != null) {
            service.cleanUp();
            unregisterServices();
        }
        closeTrackers();
        cleanUp();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return needed;
    }

}
