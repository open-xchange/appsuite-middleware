package com.openexchange.eav.storage.memory.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.storage.memory.EAVErrorMessage;
import com.openexchange.eav.storage.memory.InMemoryStorage;
import com.openexchange.exceptions.osgi.ComponentRegistration;

public class Activator implements BundleActivator {

	private ComponentRegistration componentRegistration;

    public void start(BundleContext context) throws Exception {
	    context.registerService(EAVStorage.class.getName(), new InMemoryStorage(), null);
	    componentRegistration = new ComponentRegistration(context, "EAV-MEM", "com.openexchange.eav.storage.memory", EAVErrorMessage.FACTORY);
	    
	}

	public void stop(BundleContext context) throws Exception {
	    componentRegistration.unregister();
	}

}
