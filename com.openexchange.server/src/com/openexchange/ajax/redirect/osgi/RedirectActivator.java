package com.openexchange.ajax.redirect.osgi;

import org.osgi.service.http.HttpService;

import com.openexchange.ajax.redirect.RedirectServlet;
import com.openexchange.osgi.HousekeepingActivator;

public class RedirectActivator extends HousekeepingActivator{

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{HttpService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		HttpService service = getService(HttpService.class);
		service.registerServlet("/ajax/redirect", new RedirectServlet(), null, null);
	}
	
	@Override
	protected void stopBundle() throws Exception {
		HttpService service = getService(HttpService.class);
		service.unregister("/ajax/redirect");
		super.stopBundle();
	}

}
