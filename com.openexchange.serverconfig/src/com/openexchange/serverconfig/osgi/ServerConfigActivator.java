package com.openexchange.serverconfig.osgi;

import java.util.Collections;
import java.util.List;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.serverconfig.ServerConfigMatcherService;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.serverconfig.ServerConfigServicesLookup;
import com.openexchange.serverconfig.impl.ServerConfigServiceImpl;
import com.openexchange.serverconfig.impl.values.Capabilities;
import com.openexchange.serverconfig.impl.values.ForcedHttpsValue;
import com.openexchange.serverconfig.impl.values.Hosts;
import com.openexchange.serverconfig.impl.values.Languages;
import com.openexchange.serverconfig.impl.values.ServerVersion;

public class ServerConfigActivator extends HousekeepingActivator {

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {ConfigurationService.class, ConfigViewFactory.class, CapabilityService.class, SimpleConverter.class};
    }

    @Override
    protected void startBundle() throws Exception {
        
        final NearRegistryServiceTracker<ServerConfigMatcherService> matcherTracker = new NearRegistryServiceTracker<ServerConfigMatcherService>(
            context,
            ServerConfigMatcherService.class);
        rememberTracker(matcherTracker);

        
        final NearRegistryServiceTracker<ComputedServerConfigValueService> computedValueTracker = new NearRegistryServiceTracker<ComputedServerConfigValueService>(
            context,
            ComputedServerConfigValueService.class);
        rememberTracker(computedValueTracker);
        
        openTrackers();
        
        ServerConfigServicesLookup serverConfigServicesLookup = new ServerConfigServicesLookup() {

            @Override
            public List<ServerConfigMatcherService> getMatchers() {
                return Collections.unmodifiableList(matcherTracker.getServiceList());
            }

            @Override
            public List<ComputedServerConfigValueService> getComputed() {
                return Collections.unmodifiableList(computedValueTracker.getServiceList());
            }

        };
        
        // Register the services that add computed values during creation of the server config
        registerService(ComputedServerConfigValueService.class, new ForcedHttpsValue(this));
        registerService(ComputedServerConfigValueService.class, new Hosts());
        registerService(ComputedServerConfigValueService.class, new Languages(this));
        registerService(ComputedServerConfigValueService.class, new ServerVersion());
        registerService(ComputedServerConfigValueService.class, new Capabilities(this));
        
        // The actual config service
        ServerConfigServiceImpl serverConfigServiceImpl = new ServerConfigServiceImpl(this, serverConfigServicesLookup);
        registerService(ServerConfigService.class, serverConfigServiceImpl);
    }

}
