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

package com.openexchange.contactcollector.internal;

import java.util.Collection;
import javax.mail.internet.InternetAddress;
import com.openexchange.session.Session;

/**
 * {@link MemorizerTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemorizerTask {

    private final Collection<InternetAddress> addresses;
    private final boolean incrementUseCount;
    private final Session session;

    /**
     * Initializes a new {@link MemorizerTask}.
     *
     * @param address The addresses to memorize
     * @param incrementUseCount Whether use-count is supposed to be incremented
     * @param session The associated session
     */
    public MemorizerTask(Collection<InternetAddress> addresses, boolean incrementUseCount, Session session) {
        super();
        this.addresses = addresses;
        this.incrementUseCount = incrementUseCount;
        this.session = session;
    }

    /**
     * Checks whether use-count is supposed to be incremented.
     *
     * @return <code>true</code> to increment; otherwise <code>false</code>
     */
    public boolean isIncrementUseCount() {
        return incrementUseCount;
    }

    /**
     * Gets the addresses
     *
     * @return The addresses
     */
    public Collection<InternetAddress> getAddresses() {
        return addresses;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addresses == null) ? 0 : addresses.hashCode());
        result = prime * result + session.getContextId();
        result = prime * result + session.getUserId();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemorizerTask)) {
            return false;
        }
        final MemorizerTask other = (MemorizerTask) obj;
        if (addresses == null) {
            if (other.addresses != null) {
                return false;
            }
        } else if (!addresses.equals(other.addresses)) {
            return false;
        }
        if (session.getContextId() != other.session.getContextId()) {
            return false;
        }
        if (session.getUserId() != other.session.getUserId()) {
            return false;
        }
        return true;
    }

}
