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

package com.openexchange.push.udp;

/**
 * {@link AbstractPushObject} - Abstract push object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractPushObject {

    private final int contextId;

    private final boolean remote;

    /**
     * Initializes a new {@link AbstractPushObject}.
     */
    protected AbstractPushObject(final int contextId, final boolean remote) {
        super();
        this.contextId = contextId;
        this.remote = remote;
    }

    /**
     * Gets the context ID.
     *
     * @return The context ID
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Checks if this push object was remotely received.
     * <p>
     * If remotely received this push object must not be further distributed among linked hosts.
     *
     * @return <code>true</code> if this push object was remotely received; otherwise <code>false</code>
     */
    public boolean isRemote() {
        return remote;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractPushObject)) {
            return false;
        }
        final AbstractPushObject other = (AbstractPushObject) obj;
        if (contextId != other.contextId) {
            return false;
        }
        return true;
    }

}
