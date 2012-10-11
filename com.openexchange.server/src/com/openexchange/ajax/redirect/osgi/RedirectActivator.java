package com.openexchange.ajax.redirect.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.ajax.redirect.RedirectServlet;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;

public class RedirectActivator extends HousekeepingActivator{

    private volatile String alias;

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{HttpService.class, DispatcherPrefixService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final HttpService service = getService(HttpService.class);
		final String alias = getService(DispatcherPrefixService.class).getPrefix() + "redirect";
		this.alias = alias;
        service.registerServlet(alias, new RedirectServlet(), null, null);
	}
	
	@Override
	protected void stopBundle() throws Exception {
		final HttpService service = getService(HttpService.class);
		if (null != service) {
            final String alias = this.alias;
            if (null != alias) {
                service.unregister(alias);
                this.alias = null;
            }
        }
        super.stopBundle();
	}

}
