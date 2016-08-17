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

package com.openexchange.mail.json.parser;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.mail.attachment.storage.StoreOperation;

/**
 * {@link AttachmentHandlerProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class AttachmentHandlerProperties {

    private final StoreOperation storeOperation;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link AttachmentHandlerProperties}.
     */
    public AttachmentHandlerProperties(StoreOperation storeOperation) {
        super();
        this.storeOperation = storeOperation;
        properties = new HashMap<String, Object>(6, 0.9F);
    }

    /**
     * Gets the store operation
     *
     * @return The store operation
     */
    public StoreOperation getStoreOperation() {
        return storeOperation;
    }

    /**
     * Gets the number of properties
     *
     * @return The number of properties
     */
    public int size() {
        return properties.size();
    }

    /**
     * Checks if there are no properties
     *
     * @return <code>true</code> if there are no properies; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * Checks if specified property is contained.
     *
     * @param name The property name
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsProperty(Object name) {
        return properties.containsKey(name);
    }

    /**
     * Gets the denoted property
     *
     * @param name The property name
     * @return The property value or <code>null</code> if there is no such property
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Puts specified property.
     *
     * @param name The property name
     * @param value The property value
     */
    public void putProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Removes denoted property.
     *
     * @param name The property name
     * @return The removed property or <code>null</code>
     */
    public Object removeProperty(String name) {
        return properties.remove(name);
    }

    /**
     * Clears all properties.
     */
    public void clear() {
        properties.clear();
    }

}
