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

package com.openexchange.gdpr.dataexport;

/**
 * {@link HostInfo} - Basic host information used for link generation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class HostInfo {

    private final String host;
    private final boolean secure;

    /**
     * Initializes a new {@link HostInfo}.
     *
     * @param host The requested host name
     * @param secure Indicates if the request was made via HTTPS
     */
    public HostInfo(String host, boolean secure) {
        super();
        this.host = host;
        this.secure = secure;
    }

    /**
     * Gets the requested host.
     *
     * @return The host
     */
    public String getHost() {
        return host;
    }

    /**
     * Checks whether incoming call used HTTPS or not.
     *
     * @return <code>true</code> for HTTPS; otherwise <code>false</code>
     */
    public boolean isSecure() {
        return secure;
    }

}
