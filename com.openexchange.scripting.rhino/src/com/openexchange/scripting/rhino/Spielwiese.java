package com.openexchange.scripting.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Spielwiese {
	public static void main(String[] args) {
		try {
			Context cx = Context.enter();
			ScriptableObject scope = cx.initStandardObjects();
			Object obj = cx.evaluateString(scope, "var a = 1; a;", "<test>", 1, null);
			
			Scriptable scope2 = cx.newObject(scope);
			scope2.setParentScope(null);
			scope2.setPrototype(scope);
			ScriptableObject.putProperty(scope2, "obj", obj);
			System.out.println(cx.evaluateString(scope2, "JSON.stringify(obj);", "<serialize>", 1, null));
			
		} finally {
			Context.exit();
		}
	}
}
