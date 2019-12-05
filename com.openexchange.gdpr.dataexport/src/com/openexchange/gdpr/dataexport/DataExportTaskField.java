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
