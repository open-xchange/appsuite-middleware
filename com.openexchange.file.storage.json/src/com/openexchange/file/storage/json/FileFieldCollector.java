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

package com.openexchange.file.storage.json;

import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.customizer.file.AdditionalFileField;

/**
 * {@link FileFieldCollector} - A collector for registered instances of <code>AdditionalFileField</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface FileFieldCollector {

    /** The empty collector instance */
    public static final FileFieldCollector EMPTY = new FileFieldCollector() {

        @Override
        public List<AdditionalFileField> getFields(int[] columnIDs) {
            return Collections.emptyList();
        }

        @Override
        public List<AdditionalFileField> getFields() {
            return Collections.emptyList();
        }

        @Override
        public AdditionalFileField getField(String columnNumberOrName) {
            return null;
        }

        @Override
        public AdditionalFileField getField(int columnID) {
            return null;
        }
    };

    /**
     * Gets all additionally registered file fields.
     *
     * @return The fields, or an empty list if there are none
     */
    List<AdditionalFileField> getFields();

    /**
     * Gets an additionally registered file field by its numerical column identifier.
     *
     * @param columnID the column identifier
     * @return The field, or <code>null</code> if not found
     */
    AdditionalFileField getField(int columnID);

    /**
     * Gets the additionally registered file fields by their numerical column identifiers, leaving out unknown column identifiers.
     *
     * @param columnIDs The column identifiers
     * @return The additionally registered file fields, with unknown columns missing in the result
     */
    List<AdditionalFileField> getFields(int[] columnIDs);

    /**
     * Gets an additionally registered file field by its numerical column identifier or field name.
     *
     * @param columnNumberOrName A string representation of the column identifier, or the field name
     * @return The field, or <code>null</code> if not found
     */
    AdditionalFileField getField(String columnNumberOrName);

}
