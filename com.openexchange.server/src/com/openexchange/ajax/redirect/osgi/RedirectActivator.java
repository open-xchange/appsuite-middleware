package com.openexchange.ajax.redirect.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.ajax.redirect.RedirectServlet;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.services.ServerServiceRegistry;

public class RedirectActivator extends HousekeepingActivator{

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{HttpService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final HttpService service = getService(HttpService.class);
		service.registerServlet(ServerServiceRegistry.getInstance().getService(DispatcherPrefixService.class).getPrefix() + "redirect", new RedirectServlet(), null, null);
	}
	
	@Override
	protected void stopBundle() throws Exception {
		final HttpService service = getService(HttpService.class);
		service.unregister(ServerServiceRegistry.getInstance().getService(DispatcherPrefixService.class).getPrefix() + "redirect");
		super.stopBundle();
	}

}
