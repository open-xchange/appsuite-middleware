package com.openexchange.scripting.rhino.osgi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import static com.openexchange.scripting.rhino.SharedScope.*;

import com.openexchange.scripting.rhino.require.RequireSupport;

public class LookForScriptsListener implements BundleListener {

	
	
	@Override
	public void bundleChanged(BundleEvent event) {
		switch(event.getType()) {
		case BundleEvent.STARTED: runStartScripts(event.getBundle());
		}
	}

	private void runStartScripts(final Bundle bundle) {
		URL entry = bundle.getEntry("/main.js");
		if (entry == null) {
			return;
		}
		Reader r = null;
		try {
			r = new InputStreamReader(entry.openStream(), "UTF-8");
			Context cx = Context.enter();
			Scriptable serviceScope = cx.newObject(SHARED_SCOPE);
			serviceScope.setParentScope(null);
			serviceScope.setPrototype(SHARED_SCOPE);
			
			HashMap<String, Object> additionalModules = new HashMap<String, Object>();
			additionalModules.put("osgi", new OSGiSupport(bundle.getBundleContext(), SHARED_SCOPE));
			
			RequireSupport.initialize(serviceScope, cx, new BundleJSBundle(bundle), additionalModules);
			
			cx.evaluateReader(serviceScope, r, bundle.getSymbolicName()+"/main.js", 1, null);
			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Context.exit();
		}
	}

}
