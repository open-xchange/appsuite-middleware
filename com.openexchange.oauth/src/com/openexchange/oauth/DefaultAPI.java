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

package com.openexchange.oauth;

/**
 * {@link DefaultAPI} - The default API implementation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class DefaultAPI implements API {

    private final String serviceId;
    private final String displayName;
    private final int hash;
    private final String capability;
    private final String url;

    /**
     * Initializes a new {@link DefaultAPI}.
     *
     * @param serviceId The service identifier
     * @param displayName The API's name
     * @param aliases The optional aliases of the provider
     */
    public DefaultAPI(String serviceId, String displayName, String capability, String url) {
        this.serviceId = serviceId;
        this.displayName = displayName;
        this.capability = capability;
        this.url = url;

        int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((capability == null) ? 0 : capability.hashCode());
        hash = result;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getCapability() {
        return capability;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultAPI other = (DefaultAPI) obj;
        if (capability == null) {
            if (other.capability != null) {
                return false;
            }
        } else if (!capability.equals(other.capability)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        } else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(48);
        builder.append("[");
        if (serviceId != null) {
            builder.append("serviceId=").append(serviceId).append(", ");
        }
        if (displayName != null) {
            builder.append("displayName=").append(displayName);
        }
        if (capability != null) {
            builder.append("capability=").append(capability);
        }
        if (url != null) {
            builder.append("url=").append(url);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
