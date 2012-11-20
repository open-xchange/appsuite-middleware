package com.openexchange.capabilities.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

public class CapabilitiesActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		final CapabilityServiceImpl capService = new CapabilityServiceImpl(this, context);
		registerService(CapabilityService.class, capService);
		
		track(Capability.class, new SimpleRegistryListener<Capability>() {

			@Override
			public void added(ServiceReference<Capability> ref,
					Capability service) {
				capService.getCapability(service.getId()).learnFrom(service);
			}

			@Override
			public void removed(ServiceReference<Capability> ref,
					Capability service) {
				
			}
			
		});
		
	}


}
