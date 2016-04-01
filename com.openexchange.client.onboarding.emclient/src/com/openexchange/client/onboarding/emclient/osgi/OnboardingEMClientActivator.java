
package com.openexchange.client.onboarding.emclient.osgi;

import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.emclient.OnboardingEMClientProvider;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * 
 * {@link OnboardingEMClientActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class OnboardingEMClientActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DriveWindowsClientOnboardingActivator}.
     */
    public OnboardingEMClientActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OnboardingEMClientActivator.class);
        logger.info("Starting bundle \"{}\"...", context.getBundle().getSymbolicName());
        registerService(OnboardingProvider.class, new OnboardingEMClientProvider());
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OnboardingEMClientActivator.class);
        logger.info("Stopping bundle \"{}\"...", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
