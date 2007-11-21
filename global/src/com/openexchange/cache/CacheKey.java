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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.cache;

import java.io.Serializable;

import com.openexchange.groupware.contexts.Context;

/**
 * Cache key class. The key consists of the context and the
 * unique identifier of the user. This class relies on that objects implementing
 * Context define the {@link java.lang.Object#equals(java.lang.Object)} method
 * and the {@link java.lang.Object#hashCode()} method.
 */
public class CacheKey implements Serializable {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -3144968305668671430L;

    /**
     * Unique identifier of the context.
     */
    private final int contextId;

    /**
     * Object key of the cached object.
     */
    private final Serializable keyObj;

    /**
     * Hash code of the context specific object.
     */
    private final int hash;

    /**
     * Default constructor.
     * @param context Context.
     * @param objectId Unique identifier of the cached object.
     */
    public CacheKey(final Context context, final int objectId) {
        this(context.getContextId(), Integer.valueOf(objectId));
    }

    /**
     * Constructor using context and any object.
     * @param context Context.
     * @param obj any object for identifying the cached object.
     */
    public CacheKey(final Context context, final Serializable obj) {
        this(context.getContextId(), obj);
    }

    /**
     * Constructor using any object.
     * @param contextId unique identifier of the context.
     * @param obj Any object for identifying the cached object.
     */
    public CacheKey(final int contextId, final Serializable obj) {
        super();
        this.contextId = contextId;
        this.keyObj = obj;
        hash = obj.hashCode() ^ contextId;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
	public boolean equals(final Object obj) {
        if (!(obj instanceof CacheKey)) {
            return false;
        }
        final CacheKey other = (CacheKey) obj;
        return contextId == other.contextId && keyObj.equals(other.keyObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public int hashCode() {
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public String toString() {
        return "CacheKey context=" + contextId + " | key=" + keyObj.toString();
    }
}
