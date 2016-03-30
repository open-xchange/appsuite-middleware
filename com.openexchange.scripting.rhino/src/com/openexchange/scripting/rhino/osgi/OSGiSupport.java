/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

	public OSGiSupport register(String service, final Scriptable implementation) throws ClassNotFoundException {
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
		private ServiceTracker tracker = null;
		private Object service = null;
		private List<Expectation> otherExpectations;
		private BundleContext context;
		private Function function;
		private Scriptable scope;
		private boolean tracking;

		public Expectation(BundleContext context, String serviceType, Scriptable scope, Function function, List<Expectation> otherExpectations) {
			this.context = context;
			this.otherExpectations = otherExpectations;
			final Expectation that = this;
			this.tracker = new ServiceTracker(context, serviceType, new ServiceTrackerCustomizer() {

				@Override
				public Object addingService(ServiceReference reference) {
					that.service = that.context.getService(reference);
					that.resolve();
					return that.service;
				}

				@Override
				public void modifiedService(ServiceReference reference,
						Object service) {
					// Nothing to do

				}

				@Override
				public void removedService(ServiceReference reference,
						Object service) {
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
				} else {
					args[i++] = e.service;
					e.stop();
				}
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
