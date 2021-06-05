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

package com.openexchange.framework.request;

import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.UserContextSession;

/**
 * A {@link RequestContext} holds contextual information about the current HTTP request.
 * A request context is always bound to the scope of a single request. It must not be
 * passed to asynchronous tasks whose lifetime is not determined by the request which
 * caused their scheduling. For the same reason instances of this object cannot be considered
 * thread-safe.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@NotThreadSafe
public interface RequestContext {

    /**
     * Gets the host data.
     *
     * @return The host data; never <code>null</code>
     */
    HostData getHostData();

    /**
     * Gets the value of the request's user agent header.
     *
     * @return The user agent
     */
    String getUserAgent();

    /**
     * Gets the session associated with this request context.
     *
     * @return The session
     */
    UserContextSession getSession();

}
