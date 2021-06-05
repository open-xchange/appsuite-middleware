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

package com.openexchange.sessionstorage.hazelcast;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


/**
 * {@link AcquiredLatch}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AcquiredLatch {

    /** The associated latch */
    public final CountDownLatch latch;

    /** The thread owning this instance */
    public final Thread owner;

    /** The reference to resulting object */
    public final AtomicReference<Object> result;

    /**
     * Initializes a new {@link AcquiredLatch}.
     *
     * @param owner The thread owning this instance
     * @param latch The associated latch
     */
    public AcquiredLatch(Thread owner, CountDownLatch latch) {
        super();
        this.owner = owner;
        this.latch = latch;
        result = new AtomicReference<Object>();
    }

}
