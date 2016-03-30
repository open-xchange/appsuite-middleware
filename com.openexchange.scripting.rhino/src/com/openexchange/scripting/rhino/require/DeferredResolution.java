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
