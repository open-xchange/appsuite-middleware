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

package com.openexchange.jslob.storage.db.util;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.storage.db.Services;

/**
 * {@link DelayedStoreOp} - A delayed store operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DelayedStoreOp implements Delayed {

    /** The poison element */
    public static final DelayedStoreOp POISON = new DelayedStoreOp(null, null, null, true);

    /**
     * The delay for pooled messages: <code>30sec</code>
     */
    private static volatile Long delayMsec;

    private static long delayMsec() {
        Long tmp = delayMsec;
        if (null == tmp) {
            synchronized (DelayedStoreOp.class) {
                tmp = delayMsec;
                if (null == tmp) {
                    ConfigurationService cs = Services.getService(ConfigurationService.class);
                    if (null == cs) {
                        return 30000L;
                    }
                    tmp = Long.valueOf(cs.getProperty("com.openexchange.jslob.storage.delayMsec", "30000").trim());
                    delayMsec = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    // --------------------------------------------------------------------------------- //

    /** The identifier */
    public final String id;

    /** The group name */
    public final String group;

    /** The JSlob identifier */
    public final JSlobId jSlobId;

    private final boolean poison;
    private final long expiryTimeMillis;
    private final int hash;

    /**
     * Initializes a new {@link DelayedStoreOp}.
     */
    public DelayedStoreOp(String id, String group, JSlobId jSlobId) {
        this(id, group, jSlobId, false);
    }

    /**
     * Initializes a new {@link DelayedStoreOp}.
     */
    private DelayedStoreOp(String id, String group, JSlobId jSlobId, boolean poison) {
        super();
        expiryTimeMillis = poison ? 0L : (System.currentTimeMillis() + delayMsec());
        this.poison = poison;
        this.id = id;
        this.group = group;
        this.jSlobId = jSlobId;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jSlobId == null) ? 0 : jSlobId.hashCode());
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DelayedStoreOp)) {
            return false;
        }
        DelayedStoreOp other = (DelayedStoreOp) obj;
        if (jSlobId == null) {
            if (other.jSlobId != null) {
                return false;
            }
        } else if (!jSlobId.equals(other.jSlobId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Delayed o) {
        if (poison) {
            return -1;
        }
        long thisStamp = expiryTimeMillis;
        long otherStamp = ((DelayedStoreOp) o).expiryTimeMillis;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return poison ? -1L : (unit.convert(expiryTimeMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("DelayedStoreOp [");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (group != null) {
            builder.append("group=").append(group);
        }
        builder.append("]");
        return builder.toString();
    }

}
