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

package com.openexchange.caching.internal;

import java.util.Arrays;
import com.openexchange.caching.CacheKey;

/**
 * {@link CacheKeyImpl} - A cache key that consists of a context ID and an unique (serializable) identifier of any type.
 */
public class CacheKeyImpl implements CacheKey {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -3144968305668671430L;

    /**
     * Unique identifier of the context.
     */
    private final int contextId;

    /**
     * Object keys of the cached object.
     */
    private final String[] keyObjs;

    /**
     * Hash code of the context specific object.
     */
    private final int hash;

    /**
     * Initializes a new {@link CacheKeyImpl}
     *
     * @param contextId The context ID
     * @param objectId The object ID
     */
    public CacheKeyImpl(final int contextId, final int objectId) {
        this(contextId, String.valueOf(objectId));
    }

    /**
     * Initializes a new {@link CacheKeyImpl}
     *
     * @param contextId The context ID
     * @param key A string to identify the cached object
     * @throws IllegalArgumentException If specified key is <code>null</code>
     */
    public CacheKeyImpl(final int contextId, final String key) {
        this(contextId, new String[] { key });
    }

    /**
     * Initializes a new {@link CacheKeyImpl}.
     *
     * @param contextId The context ID
     * @param keys Strings to identify the cached object.
     * @throws IllegalArgumentException If specified keys are <code>null</code>
     */
    public CacheKeyImpl(final int contextId, final String... keys) {
        super();
        if (null == keys) {
            throw new IllegalArgumentException("keys are null");
        }
        this.contextId = contextId;
        keyObjs = keys;
        // Generate hash
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        for (final String key : keys) {
            result = prime * result + ((key == null) ? 0 : key.hashCode());
        }
        hash = result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CacheKeyImpl)) {
            return false;
        }
        final CacheKeyImpl other = (CacheKeyImpl) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (!Arrays.equals(keyObjs, other.keyObjs)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append('[');
        sb.append(contextId);
        for (final String item : keyObjs) {
            sb.append(',').append(null == item ? "null" : item.toString());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String[] getKeys() {
        final String[] retval = new String[keyObjs.length];
        System.arraycopy(keyObjs, 0, retval, 0, keyObjs.length);
        return retval;
    }
}
