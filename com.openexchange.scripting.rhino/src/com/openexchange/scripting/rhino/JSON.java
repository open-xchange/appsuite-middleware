package com.openexchange.scripting.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSON {
	public static String stringify(Object o) {
		try {
			Context cx = Context.enter();
			Scriptable privateScope = cx.newObject(SharedScope.SHARED_SCOPE);
			privateScope.setParentScope(null);
			privateScope.setPrototype(SharedScope.SHARED_SCOPE);
			ScriptableObject.putProperty(privateScope, "obj", o);
			String json = (String) cx.evaluateString(privateScope, "JSON.stringify(obj);", "<serialize>", 1, null);
			return json;
		} finally {
			Context.exit();
		}
	}
}
