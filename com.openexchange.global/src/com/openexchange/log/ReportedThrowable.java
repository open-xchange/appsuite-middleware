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

package com.openexchange.log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link ReportedThrowable} - A reported {@link Throwable} instance along with optional properties.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReportedThrowable {

    private final Throwable throwable;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link ReportedThrowable}.
     * 
     * @param throwable The {@code Throwable} instance
     */
    public ReportedThrowable(final Throwable throwable) {
        this(throwable, null);
    }

    /**
     * Initializes a new {@link ReportedThrowable}.
     * 
     * @param throwable The {@code Throwable} instance
     * @param properties The optional properties; pass <code>null</code> to ignore
     */
    public ReportedThrowable(final Throwable throwable, final Map<String, Object> properties) {
        super();
        this.throwable = throwable;
        this.properties = null == properties ? null : new HashMap<String, Object>(properties);
    }

    /**
     * Gets the <code>Throwable</code> instance.
     * 
     * @return The <code>Throwable</code> instance
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Gets the property associated with given name.
     * 
     * @param name The name
     * @return The property value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V getProperty(final String name) {
        final Map<String, Object> properties = this.properties;
        return null == properties ? null : (V) properties.get(name);
    }

    /**
     * Gets the property names.
     * 
     * @return The property names
     */
    public Collection<String> getPropertyNames() {
        final Map<String, Object> properties = this.properties;
        return null == properties ? Collections.<String> emptyList() : properties.keySet();
    }

    /**
     * A {@link Set} view of the properties. The set is backed by the properties' map, so changes to the map are reflected in the set, and
     * vice-versa.
     * 
     * @return The entry set
     */
    public Set<Entry<String, Object>> entrySet() {
        final Map<String, Object> properties = this.properties;
        return null == properties ? Collections.<Entry<String, Object>> emptySet() : properties.entrySet();
    }

}
