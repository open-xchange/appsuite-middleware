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

package com.openexchange.drive.impl.internal.throttle;

import com.openexchange.tools.session.ServerSession;


/**
 * {@link TokenBucket}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface TokenBucket {

    /**
     * Acquires the given number of tokens for this session from the bucket, blocking until all are available.
     *
     * @param session The session
     * @param count The number of tokens to acquire
     * @throws InterruptedException If the current thread is interrupted
     */
    void takeBlocking(ServerSession session, int count) throws InterruptedException;

    /**
     * Acquires the given number of tokens for this session from the bucket, if they are available, and returns immediately, with the value
     * <code>true</code>, reducing the number of available tokens by the given amount. If insufficient tokens are available, this method
     * will return immediately with the value <code>false</code> and the number of available permits is unchanged.
     *
     * @param session The session
     * @param count The number of tokens to acquire
     * @return <code>true</code> if the tokens were acquired, <code>false</code>, otherwise
     */
    boolean tryTake(ServerSession session, int count);

}
