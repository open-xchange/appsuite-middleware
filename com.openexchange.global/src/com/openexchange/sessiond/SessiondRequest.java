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

package com.openexchange.sessiond;

import java.util.concurrent.CountDownLatch;

/**
 * {@link SessiondRequest} - Represents a request which is executed partly asynchronously.
 * <p>
 * Ensure request is fully completed by invoking {@link #awaitCompletion()}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondRequest<V> {

    private final V value;
    private volatile CountDownLatch latch;

    /**
     * Initializes a new {@link SessiondRequest}.
     */
    public SessiondRequest(final V value) {
        super();
        this.value = value;
    }

    /**
     * Initializes a new {@link SessiondRequest}.
     */
    public SessiondRequest(final V value, final CountDownLatch latch) {
        super();
        this.value = value;
        this.latch = latch;
    }

    /**
     * Sets the latch.
     * 
     * @param latch The latch to set
     * @return This request with given latch applied
     * @throws IllegalStateException If latch is already applied
     */
    public SessiondRequest<V> setLatch(final CountDownLatch latch) {
        if (null != this.latch) {
            throw new IllegalStateException("Latch already set.");
        }
        this.latch = latch;
        return this;
    }

    /**
     * Awaits completion of this request.
     * 
     * @throws InterruptedException If waiting is interrupted
     */
    public void awaitCompletion() throws InterruptedException {
        final CountDownLatch latch = this.latch;
        if (null != latch) {
            latch.await();
        }
    }

    /**
     * Gets the value (immediately available).
     * 
     * @return The value
     */
    public V getValue() {
        return value;
    }

}
