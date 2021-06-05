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

package com.openexchange.jslob;

import java.io.Serializable;

/**
 * {@link JSlobId} - The JSlob identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobId implements Serializable {

    private static final long serialVersionUID = -1733920133244012391L;

    private final int user;
    private final int context;
    private final String serviceId;
    private final String id;
    private final int hashCode;

    /**
     * Initializes a new {@link JSlobId}.
     *
     * @param serviceId The JSlob service identifier
     * @param id The JSlob identifier
     * @param user The user identifier
     * @param context The context identifier
     */
    public JSlobId(final String serviceId, final String id, final int user, final int context) {
        super();
        this.id = id;
        this.serviceId = serviceId;
        this.user = user;
        this.context = context;
        // Hash code
        final int prime = 31;
        int result = 1;
        result = prime * result + context;
        result = prime * result + user;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JSlobId)) {
            return false;
        }
        final JSlobId other = (JSlobId) obj;
        if (context != other.context) {
            return false;
        }
        if (user != other.user) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        } else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUser() {
        return user;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContext() {
        return context;
    }

    /**
     * Gets the JSlob service identifier.
     *
     * @return The JSlob service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets the JSlob identifier.
     *
     * @return The JSlob identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(96);
        builder.append("{user=").append(user).append(", context=").append(context);
        if (serviceId != null) {
            builder.append(", serviceId=").append(serviceId);
        }
        if (id != null) {
            builder.append(", id=").append(id);
        }
        builder.append('}');
        return builder.toString();
    }

}
