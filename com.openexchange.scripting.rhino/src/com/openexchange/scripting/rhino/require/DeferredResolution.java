package com.openexchange.scripting.rhino.require;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;


public class DeferredResolution {

	private static final LinkedList<DeferredResolution> outstanding = new LinkedList<DeferredResolution>();
	private static final Lock LOCK = new ReentrantLock();

	public static void resolve(JSBundle bundle, DependencyResolver resolver) {

		try {
			LOCK.lock();
			Context cx = Context.enter();
			Iterator<DeferredResolution> iterator = new LinkedList<DeferredResolution>(outstanding).iterator();
			while(iterator.hasNext()) {
				DeferredResolution next = iterator.next();
				if (resolver.knows(next.id) && next.callback != null) {
					next.callback.handle(resolver.get(next.id, cx, next.scope));
				}
			}
		} finally {
			Context.exit();
			LOCK.unlock();

		}
	}



	private final String id;
	private final Scriptable scope;
	private Callback callback;

	public DeferredResolution(String id, Scriptable scope) {
		this.id = id;
		this.scope = scope;
		outstanding.add(this);
	}

	public void done(Callback callback) {
		this.callback = callback;
	}

	public static void awaitResolution(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args, final Function function, final Callback cb, final boolean[] executionGuard) {
		for(int i = 0; i < args.length; i++) {
			Object o = args[i];
			if (o instanceof DeferredResolution) {
				final int index = i;
				DeferredResolution resolution = (DeferredResolution) o;
				resolution.done(new Callback() {

					@Override
					public void handle(Object o) {
						args[index] = o;
						awaitResolution(cx, scope, thisObj, args, function, cb, executionGuard);
					}

				});
				return;
			}
		}
		if (!executionGuard[0]) {
			executionGuard[0] = true;
			Object retval = function.call(cx, scope, thisObj, args);
			if (cb != null) {
				cb.handle(retval);
			}
		}
	}


}
