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
