package com.openexchange.scripting.rhino.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import com.openexchange.scripting.rhino.require.RequireSupport;
import com.openexchange.scripting.rhino.require.ResolveEnhancement;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.server.osgiservice.SimpleRegistryListener;

public class Activator extends HousekeepingActivator {


	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		LookForScriptsListener listener = new LookForScriptsListener();
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getState() == Bundle.ACTIVE) {
				listener.runStartScripts(bundle);
			}
		}

		context.addBundleListener(listener);
		track(ResolveEnhancement.class, new SimpleRegistryListener<ResolveEnhancement>() {

			@Override
			public void added(ServiceReference<ResolveEnhancement> ref,
					ResolveEnhancement service) {
				RequireSupport.addResolveEnhancement(service);

			}

			@Override
			public void removed(ServiceReference<ResolveEnhancement> ref,
					ResolveEnhancement service) {
				//RequireSupport.resolveEnhancements.remove(service);
				// TODO
			}
		});

		openTrackers();
	}


}
