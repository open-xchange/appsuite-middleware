package com.openexchange.scripting.rhino.apibridge;

import org.mozilla.javascript.Scriptable;

public class HTTPAPISupport {

	private final APIBridgeActivator apiBridgeActivator;

	public HTTPAPISupport(APIBridgeActivator apiBridgeActivator) {
		this.apiBridgeActivator = apiBridgeActivator;
	}

	public void defineModule(String module, Scriptable scriptable) {
		this.apiBridgeActivator.registerModule(new ScriptableActionFactory(scriptable), module);
	}

}