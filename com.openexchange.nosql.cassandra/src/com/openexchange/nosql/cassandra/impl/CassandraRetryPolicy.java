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

package com.openexchange.nosql.cassandra.impl;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.FallthroughRetryPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;

/**
 * {@link CassandraRetryPolicy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CassandraRetryPolicy {
    /**
     * The default retry policy.
     * <p/>
     * This policy retries queries in only two cases:
     * <ul>
     * <li>On a read timeout, if enough replicas replied but data was not retrieved.</li>
     * <li>On a write timeout, if we timeout while writing the distributed log used by batch statements.</li>
     * </ul>
     * <p/>
     * This retry policy is conservative in that it will never retry with a
     * different consistency level than the one of the initial operation.
     * <p/>
     * In some cases, it may be convenient to use a more aggressive retry policy
     * like {@link #downgradingConsistencyRetryPolicy}.
     */
    defaultRetryPolicy(DefaultRetryPolicy.INSTANCE),
    /**
     * A retry policy that sometimes retries with a lower consistency level than
     * the one initially requested.
     * <p/>
     * <b>BEWARE</b>: this policy may retry queries using a lower consistency
     * level than the one initially requested. By doing so, it may break
     * consistency guarantees. In other words, if you use this retry policy,
     * there are cases (for more information see {@link DowngradingConsistencyRetryPolicy})
     * where a read at {@code QUORUM} <b>may not</b> see a preceding write at
     * {@code QUORUM}. Do not use this policy unless you have understood the cases
     * where this can happen and are ok with that. It is also highly recommended to
     * always enable the logRetryPolicy to log the occurrences of such consistency breaks.
     */
    downgradingConsistencyRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE),
    /**
     * A retry policy that never retries (nor ignores).
     */
    fallthroughRetryPolicy(FallthroughRetryPolicy.INSTANCE),
    ;

    private final RetryPolicy retryPolicy;

    /**
     * Initialises a new {@link CassandraRetryPolicy}.
     */
    private CassandraRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * Gets the retryPolicy
     *
     * @return The retryPolicy
     */
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Gets a logging retry policy
     * 
     * @return A logging retry policy
     */
    public RetryPolicy getLoggingRetryPolicy() {
        return new LoggingRetryPolicy(retryPolicy);
    }
}
