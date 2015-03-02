package com.openexchange.saml.osgi;

import java.security.Provider;
import java.security.Security;
import javax.xml.parsers.DocumentBuilderFactory;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleServiceTrackerCustomizer;
import com.openexchange.saml.DefaultConfig;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLServiceProvider;
import com.openexchange.saml.Services;
import com.openexchange.saml.spi.ServiceProviderCustomizer;

public class Activator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private SAMLServiceProvider serviceProvider;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.saml...");
        Services.setServiceLookup(this);

        ConfigurationService configService = getService(ConfigurationService.class);
        boolean enabled = configService.getBoolProperty("com.openexchange.saml.sp.enabled", false);
        if (enabled) {
            OpenSAML openSAML = initOpenSAML();
            serviceProvider = new SAMLServiceProvider(DefaultConfig.init(configService), openSAML);
            serviceProvider.init();

            trackService(HostnameService.class);
            track(ServiceProviderCustomizer.class, new SimpleServiceTrackerCustomizer<ServiceProviderCustomizer>() {
                @Override
                public ServiceProviderCustomizer addingService(ServiceReference<ServiceProviderCustomizer> reference) {
                    ServiceProviderCustomizer customizer = context.getService(reference);
                    if (customizer != null) {
                        serviceProvider.setCustomizer(customizer);
                    }

                    return customizer;
                }

                @Override
                public void removedService(ServiceReference<ServiceProviderCustomizer> reference, ServiceProviderCustomizer service) {
                    serviceProvider.setCustomizer(null);
                }

                @Override
                public void modifiedService(ServiceReference<ServiceProviderCustomizer> reference, ServiceProviderCustomizer service) {}
            });

            openTrackers();
        } else {
            LOG.info("SAML 2.0 support is disabled by configuration. Skipping initialization...");
        }
    }

    private OpenSAML initOpenSAML() throws BundleException {
        if (!Configuration.validateJCEProviders()) {
            LOG.error("The necessary JCE providers for OpenSAML could not be found. SAML 2.0 integration will be disabled!");
            throw new BundleException("The necessary JCE providers for OpenSAML could not be found.", BundleException.ACTIVATOR_ERROR);
        }

        LOG.info("OpenSAML will use {} as API for XML processing", DocumentBuilderFactory.newInstance().getClass().getName());
        for (Provider jceProvider : Security.getProviders()) {
            LOG.info("OpenSAML found {} as potential JCE provider", jceProvider.getInfo());
        }

        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            LOG.error("Error while bootstrapping OpenSAML library", e);
            throw new BundleException("Error while bootstrapping OpenSAML library", BundleException.ACTIVATOR_ERROR, e);
        }
        return new OpenSAML();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle com.openexchange.saml...");
        serviceProvider = null;
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
