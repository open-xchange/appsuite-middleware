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

package com.openexchange.lock;

import java.util.concurrent.TimeUnit;

/**
 * {@link AccessControl} - Limits the number of concurrent access to a resource.
 * <pre>
 * AccessControl accessControl = lockService.getAccessControlFor(...);
 * try {
 *     accessControl.acquireGrant();
 *      ...
 * } catch (InterruptedException e) {
 *     Thread.currentThread().interrupt();
 *     throw ...
 * } finally {
 *    accessControl.close();
 * }
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface AccessControl extends AutoCloseable {

    /**
     * Acquires a grant from this access control; waiting for an available grant if needed.
     *
     * @throws InterruptedException If interrupted while waiting for a grant
     */
    void acquireGrant() throws InterruptedException;

    /**
     * Attempts to immediately acquire a grant from this access control; not waiting for an available grant.
     *
     * @return <code>true</code> if grant has been acquired; otherwise <code>false</code>
     */
    boolean tryAcquireGrant();

    /**
     * Attempts to immediately acquire a grant from this access control; waiting for up to the specified timeout,
     * for available grant becoming available.
     *
     * @return <code>true</code> if grant has been acquired in time; otherwise <code>false</code>
     * @throws InterruptedException If interrupted while waiting for a grant
     */
    boolean tryAcquireGrant(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Releases this access control assuming that a grant was successfully acquired before.
     * <p>
     * This is the same as calling {@link #close()} or {@link #release(boolean)} with <code>true</code>.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * May only be invoked one time per thread!<br>
     * Do not call in case {@link #tryAcquireGrant()} was invoked and returned <code>false</code>
     * </div>
     * <p>
     *
     * @return <code>true</code> if released; otherwise <code>false</code>
     */
    boolean release();

    /**
     * Releases this access control.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">May only be invoked one time per thread!</div>
     * <p>
     *
     * @param acquired <code>true</code> if a grant was acquired; otherwise <code>false</code>
     * @return <code>true</code> if released; otherwise <code>false</code>
     */
    boolean release(boolean acquired);

}
