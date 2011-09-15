package com.openexchange.scripting.rhino.osgi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.openexchange.scripting.rhino.require.RequireSupport;

public class LookForScriptsListener implements BundleListener {

	private static final ScriptableObject SHARED_SCOPE;
	static {
		Context cx;
		try {
			 cx = Context.enter();
			 SHARED_SCOPE = cx.initStandardObjects(null, true);

			// Force the LiveConnect stuff to be loaded. 
			String loadMe = "RegExp; getClass; java; Packages; JavaAdapter;";
			cx.evaluateString(SHARED_SCOPE , loadMe, "lazyLoad", 0, null);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public void bundleChanged(BundleEvent event) {
		switch(event.getType()) {
		case BundleEvent.STARTED: runStartScripts(event.getBundle());
		}
	}

	private void runStartScripts(Bundle bundle) {
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
			RequireSupport.initialize(serviceScope, cx, bundle);
			
			cx.evaluateReader(serviceScope, r, entry.toExternalForm(), 1, null);
			
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
