package com.openexchange.scripting.rhino.apibridge;


import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.scripting.rhino.require.DependencyResolver;
import com.openexchange.scripting.rhino.require.JSConverter;
import com.openexchange.scripting.rhino.require.ResolveEnhancement;

public class APIBridgeActivator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigViewFactory.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(ResolveEnhancement.class, new ResolveEnhancement() {

			@Override
			public void enhance(DependencyResolver resolver,
					JSConverter jsConverter) {
				resolver.remember("httpAPI", jsConverter.toJS(new HTTPAPISupport(APIBridgeActivator.this)));
			}

		});
	}


}
