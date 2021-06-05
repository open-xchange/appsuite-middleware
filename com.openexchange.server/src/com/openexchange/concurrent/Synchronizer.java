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

package com.openexchange.concurrent;

import java.util.concurrent.locks.Lock;

/**
 * {@link Synchronizer} - Methods to synchronize/unsynchronize access to implementing object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Synchronizer extends Synchronizable {

    /**
     * Acquires the invoking resource.
     * <p>
     * <code>
     * &nbsp;final Lock lock = <b>acquire();</b><br>
     * &nbsp;try {<br>
     * &nbsp;&nbsp;...<br>
     * &nbsp;} finally {<br>
     * &nbsp;&nbsp;release(lock);<br>
     * &nbsp;}<br>
     * </code>
     *
     * @return A lock if synchronized access was enabled via {@link #synchronize()}; otherwise <code>null</code>
     */
    public Lock acquire();

    /**
     * Releases the invoking resource.
     * <p>
     * This method properly deals with the possibility that previously called {@link #acquire()} returned <code>null</code>. Thus it is safe
     * to just pass the reference to this method as it is.
     * <p>
     * <code>
     * &nbsp;final Lock lock = acquire();<br>
     * &nbsp;try {<br>
     * &nbsp;&nbsp;...<br>
     * &nbsp;} finally {<br>
     * &nbsp;&nbsp;<b>// May be null</b><br>
     * &nbsp;&nbsp;<b>release(lock);</b><br>
     * &nbsp;}<br>
     * </code>
     *
     * @param lock The lock previously obtained by {@link #acquire()}.
     */
    public void release(Lock lock);
}
