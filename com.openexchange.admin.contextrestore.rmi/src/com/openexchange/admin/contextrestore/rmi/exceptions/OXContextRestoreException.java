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

package com.openexchange.admin.contextrestore.rmi.exceptions;

import java.util.IllegalFormatException;

/**
 * OXContextRestore exception class
 *
 */
public class OXContextRestoreException extends Exception {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 2597458638173191174L;

    public enum Code {
        /**
         * The version tables are incompatible
         */
        VERSION_TABLES_INCOMPATIBLE("The version tables are incompatible"),

        /**
         * No version information found in dump
         */
        NO_VERSION_INFORMATION_FOUND("No version information found in dump"),

        /**
         * Couldn't convert pool value
         */
        COULD_NOT_CONVERT_POOL_VALUE("Couldn't convert pool value"),

        /**
         * No entries in version table
         */
        NO_ENTRIES_IN_VERSION_TABLE("No entries in version table"),

        /**
         * Error during database operation: %s
         */
        DATABASE_OPERATION_ERROR("Error during database operation: %s"),

        /**
         * Error during rollback: %s
         */
        ROLLBACK_ERROR("Error during rollback: %s"),

        /**
         * No values found for the filestore in the database
         */
        NO_FILESTORE_VALUE("No values found for the filestore in the database"),

        /**
         * None of the files contained a configdb database dump, but this is essential for the restore process
         */
        NO_CONFIGDB_FOUND("None of the files contained a configdb database dump, but this is essential for the restore process"),

        /**
         * None of the files contained a userdata database dump, but this is essential for the restore process
         */
        NO_USER_DATA_DB_FOUND("None of the files contained a userdata database dump, but this is essential for the restore process"),

        /**
         * An IO Exception has occurred, see the log files for details
         */
        IO_EXCEPTION("An IO Exception has occurred, see the log files for details"),

        /**
         * The context id wasn't found in the context_server2db_pool table
         */
        CONTEXT_NOT_FOUND_IN_POOL_MAPPING("The context id wasn't found in the context_server2db_pool table"),

        /**
         * The updateTask tables are incompatible
         */
        UPDATE_TASK_TABLES_INCOMPATIBLE("The updateTask tables are incompatible"),

        /**
         * No updateTask information found in dump
         */
        NO_UPDATE_TASK_INFORMATION_FOUND("No updateTask information found in dump"),

        /**
         * No entries in updateTask table
         */
        NO_ENTRIES_IN_UPDATE_TASK_TABLE("No entries in updateTask table"),

        /**
         * An unexpected error occurred: %s
         */
        UNEXPECTED_ERROR("An unexpected error occurred: %s"),

        /**
         * One of the given filenames cannot be found
         */
        USERDB_FILE_NOT_FOUND("Temp file for user database values cannot be found"),

        /**
         * One of the given filenames cannot be found
         */
        CONFIGDB_FILE_NOT_FOUND("Temp file for confidb values cannot be found"),

        /**
         * The context with identifier '%1$s' is the last context in the database schema and thus it cannot be deleted/restored.
         */
        LAST_CONTEXT_IN_SCHEMA("The context with identifier '%1$s' is the last context in the database schema and thus it cannot be deleted/restored.")
        ;

        private final String text;

        /**
         * @param text
         */
        private Code(String text) {
            this.text = text;
        }

        public final String getText() {
            return text;
        }
    }

    private String[] msgArgs;

    /**
     * Default constructor
     *
     */
    public OXContextRestoreException(final Code code) {
        super(code.getText());
    }

    /**
     * Constructor with parameters
     *
     */
    public OXContextRestoreException(final Code code, final String... msgArgs) {
        super(code.getText());
        this.msgArgs = msgArgs;
    }

    /**
     * Constructor with parameters
     *
     */
    public OXContextRestoreException(final Code code, final Throwable cause, final String... msgArgs) {
        super(code.getText(), cause);
        this.msgArgs = msgArgs;
    }

    @Override
    public String toString() {
        if (null != this.msgArgs) {
            final String message = super.getMessage();
            if (null != message) {
                try {
                    return String.format(message, (Object[]) this.msgArgs);
                } catch (IllegalFormatException e) {
                    System.err.println("Illegal message format:" + e.getMessage());
                }
            }
            return super.toString();
        }
        return super.toString();
    }

    @Override
    public String getMessage() {
        if (null != this.msgArgs) {
            final String message = super.getMessage();
            if (null != message) {
                try {
                    return String.format(message, (Object[]) this.msgArgs);
                } catch (IllegalFormatException e) {
                    System.err.println("Illegal message format:" + e.getMessage());
                }
            }
            return super.getMessage();
        }
        return super.getMessage();
    }

}
