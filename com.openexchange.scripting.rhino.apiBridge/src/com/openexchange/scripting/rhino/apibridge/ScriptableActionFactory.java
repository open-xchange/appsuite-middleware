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

	private Scriptable scriptable;
	
	public ScriptableActionFactory(Scriptable scriptable) {
		this.scriptable = scriptable;
	}
	
	@Override
	public AJAXActionService createActionService(String action)
			throws OXException {
		Object object = scriptable.get(action, scriptable);
		if (object == null || object == Undefined.instance) {
			return null;
		}
		if (object instanceof Function) {
			return new FunctionAction(scriptable, (Function) object);
		}
		AJAXRequestResult retval = adapt(object);
		if (retval != null) {
			return new StaticAction(retval);
		}
		return null;
	}
	
	
	public static AJAXRequestResult adapt(Object object) throws OXException {
		if (object == Undefined.instance) {
			return new AJAXRequestResult(null, "null");
		}
		if (object instanceof Wrapper) {
			object = ((Wrapper)object).unwrap();
		}
		if (object instanceof AJAXRequestResult) {
			return (AJAXRequestResult) object;
		}
		if (object instanceof String) {
			return new AJAXRequestResult((String)object, "string");
		}
		// Next try JSON Serialization
		try {
			Context cx = Context.enter();
			Scriptable privateScope = cx.newObject(SharedScope.SHARED_SCOPE);
			privateScope.setParentScope(null);
			privateScope.setPrototype(SharedScope.SHARED_SCOPE);
			ScriptableObject.putProperty(privateScope, "obj", object);
			String json = (String) cx.evaluateString(privateScope, "JSON.stringify(obj);", "<serialize>", 1, null);
			Object jsonResult = new JSONObject("{a : "+json+"}").get("a");
			return new AJAXRequestResult(jsonResult, "json");
			
		} catch (JSONException e) {
			throw new OXException();
		} catch (ClassCastException e) {
			return new AJAXRequestResult(object.toString(), "string");
		} finally {
			Context.exit();
		}
		
	}


	private static final class StaticAction implements AJAXActionService{

		private AJAXRequestResult retval;

		public StaticAction(AJAXRequestResult retval) {
			this.retval = retval;
		}

		@Override
		public AJAXRequestResult perform(AJAXRequestData requestData,
				ServerSession session) throws OXException {
			return retval;
		}
	}

	private static final class FunctionAction implements AJAXActionService{

		private Function function;
		private Scriptable scriptable;

		public FunctionAction(Scriptable scriptable, Function function) {
			this.scriptable = scriptable;
			this.function = function;
		}

		@Override
		public AJAXRequestResult perform(AJAXRequestData requestData,
				ServerSession session) throws OXException {
			try {
				Context cx = Context.enter();
				Object result = function.call(cx, function.getParentScope(), scriptable, new Object[]{requestData, session});
				return adapt(result);
			} finally {
				Context.exit();
			}
		}
	}

}
