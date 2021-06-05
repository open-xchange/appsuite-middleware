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

package com.openexchange.chronos.schedjoules.api;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link SchedJoulesPageField}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesPageField {
    URL("url"),
    PAGE_SECTIONS("page_sections"),
    ITEM("item"),
    ITEMS("items"),
    ITEM_ID("item_id");

    private final String fieldName;

    /**
     * Initialises a new {@link SchedJoulesPageField}.
     */
    private SchedJoulesPageField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the fieldName
     *
     * @return The fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns an unmodifiable {@link Set} with a stringified representation
     * of the specified {@link SchedJoulesPageField} fields.
     * 
     * @param fields the {@link SchedJoulesPageField}s
     * @return An unmodifiable {@link Set} with the stringified representation of the {@link SchedJoulesPageField}s
     */
    public static Set<String> toSring(Set<SchedJoulesPageField> fields) {
        Set<String> stringFields = new HashSet<>();
        for (SchedJoulesPageField field : fields) {
            stringFields.add(field.getFieldName());
        }
        return stringFields;
    }
}
