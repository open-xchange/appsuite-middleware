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

package com.openexchange.config.osgi;

import java.rmi.Remote;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Reloadable;
import com.openexchange.config.VariablesProvider;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.config.internal.ConfigProviderServiceImpl;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.config.mbean.ConfigReloadMBean;
import com.openexchange.config.mbean.ConfigReloadMBeanImpl;
import com.openexchange.config.osgi.console.ReloadConfigurationCommandProvider;
import com.openexchange.config.rmi.RemoteConfigurationService;
import com.openexchange.config.rmi.impl.RemoteConfigurationServiceImpl;
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

/**
 * {@link ConfigActivator} - Activator for <code>com.openexchange.configread</code> bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigActivator extends HousekeepingActivator {

    private ServiceReference<ManagedService> managedServiceReference;

    /**
     * Default constructor
     */
    public ConfigActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigActivator.class);
        logger.info("starting bundle: com.openexchange.configread");
        try {
            RankingAwareNearRegistryServiceTracker<VariablesProvider> variablesProviderTracker = new RankingAwareNearRegistryServiceTracker<>(context, VariablesProvider.class);
            rememberTracker(variablesProviderTracker);
            com.openexchange.config.utils.TokenReplacingReader.setVariablesProviderListing(variablesProviderTracker);

            ConfigProviderTracker configProviderServiceTracker = new ConfigProviderTracker(context);
            ConfigurationImpl configService = new ConfigurationImpl(configProviderServiceTracker.getReinitQueue());
            ConfigurationImpl.setConfigReference(configService);
            registerService(ConfigurationService.class, configService, null);

            {
                Dictionary<String, Object> properties = new Hashtable<>(2);
                properties.put("scope", ConfigViewScope.SERVER.getScopeName());
                ConfigProviderServiceImpl configProviderServiceImpl = new ConfigProviderServiceImpl(configService);
                configService.setConfigProviderServiceImpl(configProviderServiceImpl);
                registerService(ConfigProviderService.class, configProviderServiceImpl, properties);
            }

            // Web Console stuff
            {
                Collection<ServiceReference<ManagedService>> serviceReferences = context.getServiceReferences(ManagedService.class, null);
                boolean found = false;
                for (ServiceReference<ManagedService> reference : serviceReferences) {
                    if ("org.apache.felix.webconsole.internal.servlet.OsgiManager".equals(reference.getProperty(Constants.SERVICE_PID))) {
                        found = true;
                        ManagedService managedService = context.getService(reference);
                        ManagedServiceTracker.configureWebConsole(managedService, configService);
                        managedServiceReference = reference;
                        break;
                    }
                }

                if (!found) {
                    rememberTracker(new ManagedServiceTracker(context, configService));
                }
            }

            // Register RMI stub
            {
                Dictionary<String, Object> props = new Hashtable<>(2);
                props.put("RMIName", RemoteConfigurationService.RMI_NAME);
                registerService(Remote.class, new RemoteConfigurationServiceImpl(configService), props);
            }

            // Register shell command
            registerService(CommandProvider.class, new ReloadConfigurationCommandProvider(configService));

            // Add & open service trackers
            track(Reloadable.class, new ReloadableServiceTracker(context, configService));
            track(ForcedReloadable.class, new ForcedReloadableServiceTracker(context, configService));
            track(ManagementService.class, new HousekeepingManagementTracker(context, ConfigReloadMBean.class.getName(), ConfigReloadMBean.DOMAIN, new ConfigReloadMBeanImpl(ConfigReloadMBean.class, configService)));
            track(ConfigProviderService.class, configProviderServiceTracker);
            openTrackers();
        } catch (Exception e) {
            logger.error("failed starting bundle: com.openexchange.configread", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigActivator.class);
        logger.info("stopping bundle: com.openexchange.configread");
        try {

            ServiceReference<ManagedService> reference = managedServiceReference;
            if (null != reference) {
                context.ungetService(reference);
                managedServiceReference = null;
            }

            super.stopBundle();
            ConfigurationImpl.setConfigReference(null);
            com.openexchange.config.utils.TokenReplacingReader.setVariablesProviderListing(null);
        } catch (Exception e) {
            logger.error("failed stopping bundle: com.openexchange.configread", e);
            throw e;
        }
    }

}
