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

package com.openexchange.websockets;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link SendControl} - Receives call-backs for message transmission results.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface SendControl {

    /**
     * Awaits until message transmission has been completed; either successfully or not.
     *
     * @throws InterruptedException If the current thread was interrupted while waiting
     */
    void awaitDone() throws InterruptedException;

    /**
     * Awaits for at most the given time until message transmission has been completed; either successfully or not.
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit of the timeout argument
     * @throws InterruptedException If the current thread was interrupted while waiting
     * @throws TimeoutException If the wait timed out
     */
    void awaitDone(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Checks if message transmission completed; either successfully or due to a failure.
     *
     * @return {@code true} if this message transmission completed; otherwise <code>false</code>
     */
    boolean isDone();

    /**
     * Attempts to cancel message transmission.
     * <p>
     * After this method returns, subsequent calls to {@link #isDone} will always return <tt>true</tt>.
     *
     * @param mayInterruptIfRunning Whether the transferring thread should be interrupted
     * @return <tt>false</tt> if the message transfer could not be cancelled, <tt>true</tt> otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

}
