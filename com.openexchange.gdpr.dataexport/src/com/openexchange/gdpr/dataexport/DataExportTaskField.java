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

package com.openexchange.gdpr.dataexport;

import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;
import java.util.Map;

/**
 * {@link DataExportTaskField} - An enumeration of query-able task fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public enum DataExportTaskField {

    /**
     * The time stamp when task has been lastly processed.
     */
    LAST_PROCESSED_STAMP("lastProcessedStamp"),
    /**
     * The current state of a task's processing
     */
    STATE("state"),
    /**
     * The file storage identifier
     */
    FILE_STORAGE_ID("fileStorageId")

    ;

    private final String identifier;

    private DataExportTaskField(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static Map<String, DataExportTaskField> FIELDS;
    static {
        DataExportTaskField[] allFields = DataExportTaskField.values();
        ImmutableMap.Builder<String, DataExportTaskField> map = ImmutableMap.builderWithExpectedSize(allFields.length);
        for (DataExportTaskField mf : allFields) {
            map.put(Strings.asciiLowerCase(mf.identifier), mf);
        }
        FIELDS = map.build();
    }

    /**
     * Gets the message field for given identifier.
     *
     * @param identifier The identifier
     * @return The associated message field or <code>null</code>
     */
    public static DataExportTaskField DataExportTaskFieldFor(String identifier) {
        return Strings.isEmpty(identifier) ? null : FIELDS.get(Strings.asciiLowerCase(identifier));
    }

    /**
     *  Gets the message fields for given identifiers.
     *
     * @param identifiers The identifiers
     * @return The associated message fields;<br>
     *         <b>Attention</b>: Array element is <code>null</code> if identifier could not be mapped to a message field
     */
    public static DataExportTaskField[] DataExportTaskFieldsFor(String... identifiers) {
        if (null == identifiers) {
            return new DataExportTaskField[0];
        }

        int length = identifiers.length;
        if (length <= 0) {
            return new DataExportTaskField[0];
        }

        DataExportTaskField[] retval = new DataExportTaskField[length];
        for (int i = length; i-- > 0;) {
            retval[i] = DataExportTaskFieldFor(identifiers[i]);
        }
        return retval;
    }

    /**
     * Adds specified field to array if not already contained.
     *
     * @param fields The fields to check
     * @param field The field to add
     * @return The fields with given element contained
     */
    public static DataExportTaskField[] addDataExportTaskFieldIfAbsent(DataExportTaskField[] fields, DataExportTaskField field) {
        if (fields == null) {
            return null;
        }
        if (field == null) {
            return fields;
        }

        for (DataExportTaskField mf : fields) {
            if (mf == field) {
                // Already contained
                return fields;
            }
        }

        DataExportTaskField[] newFields = new DataExportTaskField[fields.length + 1];
        System.arraycopy(fields, 0, newFields, 0, fields.length);
        newFields[fields.length] = field;
        return newFields;
    }

    /**
     * Checks if given field is contained in array.
     *
     * @param fields The fields
     * @param field The field to check for
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public static boolean isContained(DataExportTaskField[] fields, DataExportTaskField field) {
        if (fields == null) {
            return false;
        }
        if (field == null) {
            return false;
        }

        for (DataExportTaskField mf : fields) {
            if (mf == field) {
                // Already contained
                return true;
            }
        }
        return false;
    }
}
