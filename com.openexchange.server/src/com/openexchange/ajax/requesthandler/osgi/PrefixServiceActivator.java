package com.openexchange.ajax.requesthandler.osgi;

import com.openexchange.ajax.requesthandler.DefaultDispatcherPrefixService;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.services.ServerServiceRegistry;

public class PrefixServiceActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigurationService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		try {
			final String prefix;
	        {
	            String tmp = getService(ConfigurationService.class).getProperty("com.openexchange.dispatcher.prefix", "/ajax/").trim();
	            if (tmp.charAt(0) != '/') {
	                tmp = '/' + tmp;
	            }
	            if (!tmp.endsWith("/")) {
	                tmp = tmp + '/';
	            }
	            prefix = tmp;
	        }
	        DispatcherServlet.setPrefix(prefix);
	        final DispatcherPrefixService prefixService = DefaultDispatcherPrefixService.getInstance();
	        ServerServiceRegistry.getInstance().addService(DispatcherPrefixService.class, prefixService);
	        ImageMatcher.setPrefixService(prefixService);
	        registerService(DispatcherPrefixService.class, prefixService);
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

}
