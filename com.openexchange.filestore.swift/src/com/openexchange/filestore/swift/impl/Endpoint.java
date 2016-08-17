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

package com.openexchange.filestore.swift.impl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.filestore.swift.impl.token.Token;
import com.openexchange.java.util.UUIDs;

/**
 * Represents a Swift end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Endpoint {

    private final AtomicReference<Token> tokenRef;
    private final String endpointUri;
    private final String baseUri;
    private final Object lock;
    private final int hash;

    /**
     * Initializes a new {@link Endpoint}.
     *
     * @param baseUri The base URI including version and tenant, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456"</code>.
     * @param containerName The container name; e.g. <code>"MyContainer"</code>
     * @param lock The tenant-scoped lock associated with this end-point
     */
    Endpoint(String baseUri, String containerName, Object lock) {
        super();
        this.endpointUri = baseUri + '/' + containerName;
        this.baseUri = baseUri;
        this.lock = lock;
        tokenRef = new AtomicReference<Token>();
        hash = 31 * 1 + endpointUri.hashCode();
    }

    private StringBuilder constructUri() {
        return new StringBuilder(endpointUri).append('/');
    }

    /**
     * Gets the lock associated with end-point's base URI (tenant scope).
     * <p>
     * Every end-point with the same base URI refers to the same lock instance.
     *
     * @return The lock
     */
    public Object getLock() {
        return lock;
    }

    /**
     * Gets the token currently in use for this end-point.
     *
     * @return The token or <code>null</code>
     */
    public Token getToken() {
        return tokenRef.get();
    }

    /**
     * Sets a token to use for this end-point.
     *
     * @param token The token to set
     */
    public void setToken(Token token) {
        tokenRef.set(token);
    }

    /**
     * Invalidates the current token to use for this end-point.
     */
    public void invalidateToken() {
        tokenRef.set(null);
    }

    /**
     * Gets the base URI for this end-point, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456"</code>.
     *
     * @return The URI; always without trailing slash
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Gets the identifier associated with this end-point's base URI.<br>
     * (which is the HEX presentation for the MD5 sum of the base URI)
     *
     * @return The identifier
     */
    public String getId() {
        return Utils.getMD5Sum(baseUri);
    }

    /**
     * Gets the URI for the container, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456/MyContainer"</code>.
     *
     * @return The URI; always without trailing slash
     */
    public String getContainerUri() {
        return endpointUri;
    }

    /**
     * Gets the URI for the given object, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456/MyContainer/57462ctxstore/411615f4a607432fa2e12cc18b8c5f9c"</code>.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param id The object identifier
     * @return The URI; always without trailing slash
     */
    public String getObjectUri(String prefix, UUID id) {
        return getObjectUri(prefix, UUIDs.getUnformattedString(id));
    }

    /**
     * Gets the URI for the given object, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456/MyContainer/57462ctxstore/411615f4a607432fa2e12cc18b8c5f9c"</code>.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param id The object identifier
     * @return The URI; always without trailing slash
     */
    public String getObjectUri(String prefix, String id) {
        return constructUri().append(Utils.addPrefix(prefix, id)).toString();
    }

    /**
     * Gets the URI for the given object, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456/MyContainer/57462ctxstore/411615f4a607432fa2e12cc18b8c5f9c"</code>.
     *
     * @param prefixedName The already prefixed object name; e.g. <code>"57462ctxstore/411615f4a607432fa2e12cc18b8c5f9c"</code>
     * @return The URI; always without trailing slash
     */
    public String getObjectUri(String prefixedName) {
        return constructUri().append(prefixedName).toString();
    }

    @Override
    public String toString() {
        return endpointUri;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Endpoint)) {
            return false;
        }
        Endpoint other = (Endpoint) obj;
        if (endpointUri == null) {
            if (other.endpointUri != null) {
                return false;
            }
        } else if (!endpointUri.equals(other.endpointUri)) {
            return false;
        }
        return true;
    }

}
