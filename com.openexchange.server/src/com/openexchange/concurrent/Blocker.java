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


/**
 * {@link Blocker} - Methods to block/unblock access to implementing object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Blocker extends Blockable {

    /**
     * Acquires the invoking resource and blocks if object's access is blocked.
     * <p>
     * <code>
     * &nbsp;<b>blocker.acquire();</b><br>
     * &nbsp;try {<br>
     * &nbsp;&nbsp;...<br>
     * &nbsp;} finally {<br>
     * &nbsp;&nbsp;blocker.release(lock);<br>
     * &nbsp;}<br>
     * </code>
     */
    public void acquire();

    /**
     * Releases the invoking resource.
     * <p>
     * <code>
     * &nbsp;blocker.acquire();<br>
     * &nbsp;try {<br>
     * &nbsp;&nbsp;...<br>
     * &nbsp;} finally {<br>
     * &nbsp;&nbsp;<b>blocker.release(lock);</b><br>
     * &nbsp;}<br>
     * </code>
     */
    public void release();
}
