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

package com.openexchange.threadpool.internal;

import java.util.concurrent.Callable;

/**
 * Callable for the single thread master thread creator executor service to create a new thread.
 *
 * This is necessary the ensure that threads for the thread pool are always created with the correct OSGi thread context class loader. See
 * bug 26072 for threads in the thread pool created with the wrong thread context class loader.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ThreadCreateCallable implements Callable<CustomThread> {

    private final Runnable runnable;
    private final String threadName;

    ThreadCreateCallable(Runnable runnable, String threadName) {
        super();
        this.runnable = runnable;
        this.threadName = threadName;
    }

    @Override
    public CustomThread call() {
        return MasterThreadFactory.newCustomThread(runnable, threadName);
    }
}
