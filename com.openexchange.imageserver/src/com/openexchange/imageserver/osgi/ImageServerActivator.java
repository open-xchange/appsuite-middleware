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

package com.openexchange.imageserver.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.imageserver.api.ImageServerConfig;
import com.openexchange.imageserver.api.ImageServerDescriptor;
import com.openexchange.osgi.HousekeepingActivator;

//=============================================================================
public class ImageServerActivator extends HousekeepingActivator {

    //-------------------------------------------------------------------------
    private static final Logger LOG = LoggerFactory.getLogger(ImageServerActivator.class);

    private static final String SERVICE_NAME = "Open-Xchange FileServer";

    public ImageServerActivator() {
        super();
    }

    //-------------------------------------------------------------------------
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class
        };
    }

    //-------------------------------------------------------------------------
    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] {};
    }

    //-------------------------------------------------------------------------
    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: " + SERVICE_NAME);

            initConfig();
            startFileServer();

            openTrackers();
        } catch (Throwable e) {
            LOG.error("... starting bundle: " + SERVICE_NAME + " failed", e);
            throw new RuntimeException(e);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: " + SERVICE_NAME);
    }

    // - Implementation --------------------------------------------------------

    //    //-------------------------------------------------------------------------
    //    private void impl_initDBPool ()
    //      throws Exception {
    //      LOG.info("init DB Pool ...");
    //
    //      final String sPU = "imageserverregistry";
    //
    //      final PersistenceUnit aPU = new PersistenceUnit ();
    //      aPU.setName(sPU);
    //      aPU.setProvider(MysqlProvider.class.getName());
    //      aPU.addEntity(CSRegistryEntity.class.getName());
    //      aPU.setProperty(PersistenceUnitConst.JDBC_DRIVER, "com.mysql.jdbc.Driver");
    //      aPU.setProperty(PersistenceUnitConst.JDBC_CONNECTIONURL, "jdbc:mysql://localhost/csregistry");
    //      aPU.setUser("root");
    //      aPU.setPassword("secret");
    //
    //      final IPersistenceUnitRegistry iPURegistry = PersistenceUnitRegistry.get();
    //      final DBPool aDBPool = new DBPool ();
    //
    //      iPURegistry.addPersistenceUnits(aPU);
    //      aDBPool.setPersistenceUnitRegistry(iPURegistry);
    //
    //      final IDB iDB = aDBPool.getDbForPersistenceUnit(sPU);
    //    }

    //-------------------------------------------------------------------------
    private void initConfig() throws Exception {
        LOG.info("init Config Items ...");

        final ConfigurationService config = getService(ConfigurationService.class);

        // TODO (KA): read config
    }

    //-------------------------------------------------------------------------
    private void startFileServer() throws Exception {
        LOG.info("init FileServer ...");

        // TODO (KA): impl.
        // addService(FileServer.class, m_fileServer = FileServer.create());
    }

    //-------------------------------------------------------------------------
    private void createOrUpdateFileServerDB() throws Exception {
        LOG.info("create or update DCS DB ...");

        final ImageServerConfig fileServerConfig = ImageServerConfig.get();
        final ImageServerDescriptor fileServerDescriptor = new ImageServerDescriptor();

        fileServerDescriptor.m_serviceName = SERVICE_NAME;
        fileServerDescriptor.m_dbType = fileServerConfig.getDBType();
        fileServerDescriptor.m_dbHost = fileServerConfig.getDBHost();
        fileServerDescriptor.m_dbPort = fileServerConfig.getDBPort();
        fileServerDescriptor.m_dbUserName = fileServerConfig.getDBUserName();
        fileServerDescriptor.m_dbPassword = fileServerConfig.getDBUserPassword();

        // TODO (KA): 0impl.
        // addService(FileServer.class, m_fileServer = FileServer.create(fileServerDescriptor));
    }
}
