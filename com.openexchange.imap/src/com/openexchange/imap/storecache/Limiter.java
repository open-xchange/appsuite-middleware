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

package com.openexchange.imap.storecache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Limiter} - A simple limiter backed by a {@link AtomicInteger}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Limiter {

    private final int max;
    private final AtomicInteger cur;

    /**
     * Initializes a new {@link Limiter}.
     */
    public Limiter(int max) {
        super();
        this.max = max;
        cur = new AtomicInteger(max);
    }

    /**
     * Acquires a permit.
     *
     * @return <code>true</code> if successfully acquired; otherwise <code>false</code>
     */
    public boolean acquire() {
        int i;
        do {
            i = cur.get();
            if (i <= 0) {
                return false;
            }
        } while (!cur.compareAndSet(i, i - 1));
        return true;
    }

    /**
     * Releases previously obtained permit.
     */
    public void release() {
        int i;
        do {
            i = cur.get();
            if (i >= max) {
                return;
            }
        } while (!cur.compareAndSet(i, i + 1));
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(super.toString());
        builder.append(" [max=").append(max).append(", ");
        if (cur != null) {
            builder.append("cur=").append(cur.get());
        }
        builder.append("]");
        return builder.toString();
    }

}
