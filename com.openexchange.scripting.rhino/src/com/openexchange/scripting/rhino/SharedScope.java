package com.openexchange.scripting.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class SharedScope {
	public static final ScriptableObject SHARED_SCOPE;
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
}
