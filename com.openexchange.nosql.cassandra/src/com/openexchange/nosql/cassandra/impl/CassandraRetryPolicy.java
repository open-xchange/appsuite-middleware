/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
