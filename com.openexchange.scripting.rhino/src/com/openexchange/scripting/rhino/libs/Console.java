package com.openexchange.scripting.rhino.libs;

import org.apache.commons.logging.Log;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import com.openexchange.scripting.rhino.JSON;

public class Console {

	public static void initialize(Scriptable scope, String name) {
		try {
			Context.enter();
			ScriptableObject.putProperty(scope, "console", Context.javaToJS(new Console(name), scope));
		} finally {
			Context.exit();
		}

	}

	private Log log = null;

	public Console(String def) {
		log = com.openexchange.log.LogFactory.getLog(def);
	}

	public void log(Object... values) {
		log.info(toString(values));
	}

	public void warn(Object... values) {
		log.warn(toString(values));
	}

	public void debug(Object... values) {
		log.debug(toString(values));
	}

	public void error(Object... values) {
		log.error(toString(values));
	}

	public void fatal(Object... values) {
		log.fatal(toString(values));
	}

	public void info(Object... values) {
		log.info(toString(values));
	}

	private String toString(Object[] values) {
		if (values == null || values.length == 0) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		for (Object o : values) {
			if (o instanceof Wrapper) {
				o = ((Wrapper) o).unwrap();
			}
			if (o instanceof Scriptable) {
				b.append(JSON.stringify(o));
			}  else if (o instanceof Undefined) {
				b.append("undefined");
			} else {
				b.append(o);
			}
			b.append(", ");
		}
		b.setLength(b.length()-2);
		return b.toString();
	}

}
