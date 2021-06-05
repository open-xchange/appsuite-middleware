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

package com.openexchange.scripting.rhino.apibridge;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.scripting.rhino.SharedScope;
import com.openexchange.tools.session.ServerSession;

public class ScriptableActionFactory implements AJAXActionServiceFactory {

	private final Scriptable scriptable;

	public ScriptableActionFactory(final Scriptable scriptable) {
		this.scriptable = scriptable;
	}

	@Override
	public AJAXActionService createActionService(final String action)
			throws OXException {
		final Object object = scriptable.get(action, scriptable);
		if (object == null || object == Undefined.instance) {
			return null;
		}
		if (object instanceof Function) {
			return new FunctionAction(scriptable, (Function) object);
		}
		final AJAXRequestResult retval = adapt(object);
		if (retval != null) {
			return new StaticAction(retval);
		}
		return null;
	}

	public static AJAXRequestResult adapt(Object object) throws OXException {
	    Object obj = object;
		if (obj == Undefined.instance) {
			return new AJAXRequestResult(null, "native");
		}
		if (obj instanceof Wrapper) {
			obj = ((Wrapper)obj).unwrap();
		}
		if (obj instanceof AJAXRequestResult) {
			return (AJAXRequestResult) obj;
		}
		if (obj instanceof String) {
			return new AJAXRequestResult(obj, "string");
		}
		// Next try JSON Serialization
		try {
			final Context cx = Context.enter();
			final Scriptable privateScope = cx.newObject(SharedScope.SHARED_SCOPE);
			privateScope.setParentScope(null);
			privateScope.setPrototype(SharedScope.SHARED_SCOPE);
			ScriptableObject.putProperty(privateScope, "obj", obj);
			final String json = (String) cx.evaluateString(privateScope, "JSON.stringify(obj);", "<serialize>", 1, null);
			final Object jsonResult = new JSONObject("{a : "+json+"}").get("a");
			return new AJAXRequestResult(jsonResult, "json");

		} catch (JSONException e) {
			throw new OXException(e);
		} catch (@SuppressWarnings("unused") final ClassCastException e) {
			return new AJAXRequestResult(obj.toString(), "string");
		} finally {
			Context.exit();
		}

	}


	private static final class StaticAction implements AJAXActionService{

		private final AJAXRequestResult retval;

		public StaticAction(final AJAXRequestResult retval) {
			this.retval = retval;
		}

		@Override
		public AJAXRequestResult perform(final AJAXRequestData requestData,
				final ServerSession session) throws OXException {
			return retval;
		}
	}

	private static final class FunctionAction implements AJAXActionService{

		private final Function function;
		private final Scriptable scriptable;

		public FunctionAction(final Scriptable scriptable, final Function function) {
			this.scriptable = scriptable;
			this.function = function;
		}

		@Override
		public AJAXRequestResult perform(final AJAXRequestData requestData,
				final ServerSession session) throws OXException {
			try {
				final Context cx = Context.enter();
				final Object result = function.call(cx, function.getParentScope(), scriptable, new Object[]{requestData, session});
				return adapt(result);
			} finally {
				Context.exit();
			}
		}
	}

}
