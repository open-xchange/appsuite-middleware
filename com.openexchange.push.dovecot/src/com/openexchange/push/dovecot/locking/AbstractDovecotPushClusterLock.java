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

package com.openexchange.push.dovecot.locking;

import java.util.concurrent.Future;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractDovecotPushClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractDovecotPushClusterLock implements DovecotPushClusterLock {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractDovecotPushClusterLock}.
     */
    protected AbstractDovecotPushClusterLock(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Generate an appropriate value for given time stamp and session identifier pair
     *
     * @param millis The time stamp
     * @param sessionInfo The session info
     * @return The value
     */
    protected String generateValue(long millis, SessionInfo sessionInfo) {
        if (sessionInfo.isPermanent()) {
            return Long.toString(millis);
        }
        return new StringBuilder(32).append(millis).append('?').append(sessionInfo.getCompositeId()).toString();
    }

    /**
     * Checks validity of passed value in comparison to given time stamp (and session).
     *
     * @param value The value to check
     * @param now The current time stamp milliseconds
     * @param hzInstance The Hazelcast instance
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    protected boolean validValue(String value, long now) {
        return (now - parseMillisFromValue(value) <= TIMEOUT_MILLIS);
    }

    /**
     * Parses the time stamp milliseconds from given value
     *
     * @param value The value
     * @return The milliseconds
     */
    protected long parseMillisFromValue(String value) {
        int pos = value.indexOf('?');
        return Long.parseLong(pos > 0 ? value.substring(0, pos) : value);
    }

    /**
     * Cancels given {@link Future} safely
     *
     * @param future The {@code Future} to cancel
     */
    public static <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

}
