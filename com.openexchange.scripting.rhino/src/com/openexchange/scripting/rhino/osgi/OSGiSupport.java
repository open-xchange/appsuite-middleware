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

package com.openexchange.scripting.rhino.osgi;


import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class OSGiSupport {
	private final BundleContext context;
	private final Scriptable baseScope;

	public OSGiSupport(BundleContext context, Scriptable baseScope) {
		super();
		this.context = context;
		this.baseScope = baseScope;
	}

	public void services(String[] dependencies, Function function) {
		List<Expectation> all = new ArrayList<Expectation>();
		for(String dep : dependencies) {
			all.add(new Expectation(context, dep, function.getParentScope(), function, all));
		}

		for (Expectation expectation : all) {
			expectation.start();
		}
	}

	public OSGiSupport register(String service, final Scriptable implementation) {
		try {
			Context cx = Context.enter();

			Scriptable scope = cx.newObject(baseScope);
			scope.setParentScope(null);
			scope.setPrototype(baseScope);
			ScriptableObject.putProperty(scope, "impl", implementation);
			Object adapter = ((Wrapper)cx.evaluateString(scope, "new Packages."+service+"(impl);", "<internal>", 1, null)).unwrap();
			context.registerService(service, adapter, null); //TODO: Properties
		} finally {
			Context.exit();
		}
		return this;
	}

	private static final class Expectation {

		private ServiceTracker<Object, Object> tracker = null;
		Object service = null;
		private List<Expectation> otherExpectations;
		BundleContext context;
		private Function function;
		private Scriptable scope;
		private boolean tracking;

		public Expectation(BundleContext context, String serviceType, Scriptable scope, Function function, List<Expectation> otherExpectations) {
			this.context = context;
			this.otherExpectations = otherExpectations;
			final Expectation that = this;
			this.tracker = new ServiceTracker<>(context, serviceType, new ServiceTrackerCustomizer<Object, Object>() {

				@Override
				public Object addingService(ServiceReference<Object> reference) {
					that.service = that.context.getService(reference);
					that.resolve();
					return that.service;
				}

				@Override
				public void modifiedService(ServiceReference<Object> reference, Object service) {
					// Nothing to do

				}

				@Override
				public void removedService(ServiceReference<Object> reference, Object service) {
				    that.context.ungetService(reference);
				}
			});
			this.scope = scope;
			this.function = function;
		}

		protected void resolve() {
			Object[] args = new Object[otherExpectations.size()];
			int i = 0;
			for (Expectation e : otherExpectations) {
				if (!e.isSatisfied()) {
					return;
				}
                args[i++] = e.service;
                e.stop();
			}
			try {
				Context cx = Context.enter();
				function.call(cx, scope, scope, args);
			} finally {
				Context.exit();
			}

		}

		public boolean isSatisfied() {
			return service != null;
		}

		public void start() {
			if (tracking) {
				return;
			}
			tracker.open();
			tracking = true;
		}

		public void stop() {
			if (!tracking) {
				return;
			}
			tracker.close();
			tracking = false;
		}
	}
}
