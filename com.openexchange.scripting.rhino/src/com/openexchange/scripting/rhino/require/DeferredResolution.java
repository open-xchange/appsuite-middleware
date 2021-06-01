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

    public static void resolve(DependencyResolver resolver) {
        LOCK.lock();
        try {
            Context cx = Context.enter();
            Iterator<DeferredResolution> iterator = new LinkedList<DeferredResolution>(outstanding).iterator();
            while (iterator.hasNext()) {
                DeferredResolution next = iterator.next();
                if (resolver.knows(next.id)) {
                    Callback callback = next.callback;
                    if (callback != null) {
                        callback.handle(resolver.get(next.id, cx, next.scope));
                    }
                }
            }
        } finally {
            try {
                Context.exit();
            } finally {
                LOCK.unlock();
            }
        }
    }

    private final String id;
    private final Scriptable scope;
    private volatile Callback callback;

    public DeferredResolution(String id, Scriptable scope) {
        this.id = id;
        this.scope = scope;
        outstanding.add(this);
    }

    public void done(Callback callback) {
        this.callback = callback;
    }

    public static void awaitResolution(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args, final Function function, final Callback cb, final boolean[] executionGuard) {
        for (int i = 0; i < args.length; i++) {
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
