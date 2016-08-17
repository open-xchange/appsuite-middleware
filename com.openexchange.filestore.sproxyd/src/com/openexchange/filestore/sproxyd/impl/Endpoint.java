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

package com.openexchange.filestore.sproxyd.impl;

import java.util.UUID;
import com.openexchange.java.util.UUIDs;

/**
 * Represents a sproxyd endpoint.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Endpoint {

    private final String baseUrl;

    private final int contextId;

    private final int userId;

    /**
     * Initializes a new {@link Endpoint}.
     *
     * @param baseUrl The base URL including the namespace for OX files, e.g. <code>http://ring12.example.com/proxy/ox/</code>;
     *                must always end with a trailing slash
     * @param contextId The context ID
     * @param userId The user ID
     */
    public Endpoint(String baseUrl, int contextId, int userId) {
        super();
        this.baseUrl = baseUrl;
        this.contextId = contextId;
        this.userId = userId;
    }

    /**
     * Gets the URL for the according context or user store, e.g. <code>http://ring12.example.com/proxy/ox/1337/0/</code>.
     *
     * @return The URL; always with trailing slash
     */
    public String getFullUrl() {
        return baseUrl + contextId + '/' + userId + '/';
    }

    /**
     * Gets the URL for the given object, e.g. <code>http://ring12.example.com/proxy/ox/1337/0/411615f4a607432fa2e12cc18b8c5f9c</code>.
     *
     * @return The URL; always without trailing slash
     */
    public String getObjectUrl(UUID id) {
        return getFullUrl() + UUIDs.getUnformattedString(id);
    }

    /**
     * gets the base URL without the context or user specific sub-path , e.g. <code>http://ring12.example.com/proxy/ox/</code>.
     *
     * @return The URL; always with trailing slash
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String toString() {
        return getFullUrl();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
        result = prime * result + contextId;
        result = prime * result + userId;
        return result;
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
        Endpoint other = (Endpoint) obj;
        if (baseUrl == null) {
            if (other.baseUrl != null) {
                return false;
            }
        } else if (!baseUrl.equals(other.baseUrl)) {
            return false;
        }
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

}
