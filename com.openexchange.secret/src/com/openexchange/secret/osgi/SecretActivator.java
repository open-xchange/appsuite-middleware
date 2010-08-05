package com.openexchange.secret.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.impl.SessionSecretService;

public class SecretActivator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
	    context.registerService(SecretService.class.getName(), new SessionSecretService(), null);
	}

	public void stop(BundleContext context) throws Exception {
	}

}
