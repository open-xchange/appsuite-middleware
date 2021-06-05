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

package com.openexchange.imap.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Counter} - Counter to acquire incremental positive <code>long</code> values.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class Counter {

    private final AtomicLong count;

    /**
     * Initializes a new {@link RunLoopManager.RoundRobinLoadBalancer}.
     */
    public Counter() {
        super();
        count = new AtomicLong();
    }

    /**
     * Gets the next positive <code>long</code> value
     *
     * @return The next <code>long</code> value
     */
    public long nextLong() {
        long cur;
        long next;
        do {
            cur = count.get();
            next = cur + 1;
            if (next < 0) {
                next = 0;
            }
        } while (!count.compareAndSet(cur, next));
        return next;
    }

}