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

package com.openexchange.http.requestwatcher.osgi.services;

/**
 * {@link RequestTrace} - A fast {@link Throwable} implementation representing a tracked request's trace.
 * <p>
 * The {@link #fillInStackTrace()} simply returns the instance itself.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RequestTrace extends Throwable {

    private static final long serialVersionUID = -3023507467815652875L;

    /**
     * Initializes a new {@link RequestTrace}.
     *
     * @param age The current age
     * @param maxAge The age threshold
     * @param threadName The thread name
     */
    public RequestTrace(String age, String maxAge, String threadName) {
        super(new StringBuffer(96).append("tracked request (age=").append(age).append(", max-age=").append(maxAge).append(", thread-name=").append(threadName).append(')').toString());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
