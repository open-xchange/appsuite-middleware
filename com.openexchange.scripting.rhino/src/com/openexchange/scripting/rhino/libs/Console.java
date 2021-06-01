/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.scripting.rhino.libs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.slf4j.LoggerFactory;
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

	private org.slf4j.Logger log = null;

	public Console(String def) {
		log = LoggerFactory.getLogger(def);
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
		log.error(toString(values));
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
