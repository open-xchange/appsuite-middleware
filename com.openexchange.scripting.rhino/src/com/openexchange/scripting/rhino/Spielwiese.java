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

package com.openexchange.scripting.rhino;

import org.mozilla.javascript.Context;
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
