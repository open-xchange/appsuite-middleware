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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;

/**
 * {@link Props} - The log properties associated with a certain {@link Thread thread}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Props {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(Props.class);

    private final Map<String, Object> map;

    /**
     * Initializes a new {@link Props}.
     * 
     * @param map The backing map
     */
    protected Props(final Map<String, Object> map) {
        super();
        this.map = map;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Gets the backing map
     * 
     * @return The backing map
     */
    public Map<String, Object> getMap() {
        return map;
    }

    /**
     * Checks for presence of associated property.
     * 
     * @param name The property name
     * @return <code>true</code> if present; otherwise <code>false</code>
     */
    public boolean contains(final String name) {
        return map.containsKey(name);
    }

    /**
     * Gets the property associated with given name.
     * 
     * @param name The property name
     * @return The property value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final String name) {
        try {
            return (V) map.get(name);
        } catch (final ClassCastException e) {
            LOG.warn("Type mismatch", e);
            return null;
        }
    }

    /**
     * Puts specified mapping. Any existing mapping is overwritten.
     * 
     * @param name The property name
     * @param value The property value
     */
    public <V> void put(final String key, final V value) {
        map.put(key, value);
    }

    /**
     * Removes the property associated with given name.
     * 
     * @param name The property name
     */
    public void remove(final String name) {
        map.remove(name);
    }

	/**
	 * Creates a shallow copy of this log properties.
	 * 
	 * @return The shallow copy
	 */
	public Props copy() {
		return new Props(new HashMap<String, Object>(map));
	}

}
