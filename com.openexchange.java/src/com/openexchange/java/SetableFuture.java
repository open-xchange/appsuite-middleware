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

package com.openexchange.java;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * {@link SetableFuture} - A {@link FutureTask} allowing to set result object and exception.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SetableFuture<V> extends FutureTask<V> implements Future<V> {

    /**
     * Initializes a new {@link SetableFuture}.
     */
    public SetableFuture() {
        super(new Callable<V>() {

            @Override
            public V call() throws Exception {
                return null;
            }
        });
    }

    /**
     * Initializes a new {@link SetableFuture}.
     *
     * @param value The value
     */
    public SetableFuture(final V value) {
        super(new Callable<V>() {

            @Override
            public V call() throws Exception {
                return null;
            }
        });
        set(value);
    }

    @Override
    public void set(final V v) {
        super.set(v);
    }

    @Override
    public void setException(final Throwable t) {
        super.setException(t);
    }

}
