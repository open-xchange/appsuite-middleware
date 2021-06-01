/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.filestore.sproxyd.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.sproxyd.SproxydFileStorageFactory;
import com.openexchange.filestore.sproxyd.groupware.SproxydConvertToUtf8mb4;
import com.openexchange.filestore.sproxyd.groupware.SproxydCreateTableService;
import com.openexchange.filestore.sproxyd.groupware.SproxydCreateTableTask;
import com.openexchange.filestore.sproxyd.groupware.SproxydDeleteListener;
import com.openexchange.filestore.sproxyd.http.SproxydHttpClientConfig;
import com.openexchange.filestore.sproxyd.rmi.SproxydRemoteManagement;
import com.openexchange.filestore.sproxyd.rmi.impl.SproxydRemoteImpl;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.WildcardHttpClientConfigProvider;
import com.openexchange.timer.TimerService;

/**
 * {@link SproxydActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SproxydActivator extends HousekeepingActivator {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydActivator.class);

    /**
     * Initializes a new {@link SproxydActivator}.
     */
    public SproxydActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DatabaseService.class, TimerService.class, HttpClientService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.filestore.sproxyd");

        // Trackers
        trackService(ContextService.class);
        openTrackers();

        // Register update task, create table job and delete listener
        registerService(CreateTableService.class, new SproxydCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new SproxydCreateTableTask(), new SproxydConvertToUtf8mb4()));
        registerService(DeleteListener.class, new SproxydDeleteListener());

        // Register factory
        registerService(FileStorageProvider.class, new SproxydFileStorageFactory(this));

        // Register RMI
        {
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put("RMIName", SproxydRemoteManagement.RMI_NAME);
            registerService(Remote.class, new SproxydRemoteImpl(this), props);
        }

        // Register HTTP client config
        registerService(WildcardHttpClientConfigProvider.class, new SproxydHttpClientConfig(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.filestore.sproxyd");
        super.stopBundle();
    }
}
