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

package com.openexchange.admin.autocontextid.rmi.exceptions;


/**
 * @author choeger
 *
 */
public class OXAutoCIDException extends Exception {

    public enum Code {
        /**
         * Unable to load available restrictions from database
         */
        UNABLE_TO_LOAD_AVAILABLE_RESTRICTIONS_FROM_DATABASE("Unable to load available restrictions from database"),
        /**
         * Reseller admin already exists: %1$s
         */
        RESELLER_ADMIN_EXISTS("Reseller admin already exists: %1$s"),
        /**
         * Reseller admin does not exist: %1$s
         */
        RESELLER_ADMIN_NOT_EXIST("Reseller admin does not exist: %1$s"),
        /**
         * ContextID %1$s does not belong to %2$s
         */
        CONTEXT_DOES_NOT_BELONG("ContextID %1$s does not belong to %2$s"),
        /**
         * Maximum overall context quota reached: %1$s
         */
        MAXIMUM_OVERALL_CONTEXT_QUOTA("Maximum overall context quota reached: %1$s"),
        /**
         * Maximum number of contexts reached: %1$s
         */
        MAXIMUM_NUMBER_CONTEXT_REACHED("Maximum number of contexts reached: %1$s"),
        /**
         * Maximum overall number of contexts reached: %1$s
         */
        MAXIMUM_OVERALL_NUMBER_OF_CONTEXT_REACHED("Maximum overall number of contexts reached: %1$s"),
        /**
         * Maximum overall number of users reached: %1$s
         */
        MAXIMUM_OVERALL_NUMBER_OF_USERS_REACHED("Maximum overall number of users reached: %1$s"),
        /**
         * Maximum overall number of users by moduleaccess reached: %1$s
         */
        MAXIMUM_OVERALL_NUMBER_OF_USERS_BY_MODULEACCESS_REACHED("Maximum overall number of users by moduleaccess reached: %1$s"),
        /**
         * Maximum number of users per context reached: %1$s
         */
        MAXIMUM_NUMBER_OF_USERS_PER_CONTEXT_REACHED("Maximum number of users per context reached: %1$s"),
        /**
         * Maximum number of users by moduleaccess per context reached: %1$s
         */
        MAXIMUM_NUMBER_OF_USERS_BY_MODULEACCESS_PER_CONTEXT_REACHED("Maximum number of users by moduleaccess per context reached: %1$s"),
        /**
         * Unable to delete %1$s, still owns Context(s)
         */
        UNABLE_TO_DELETE("Unable to delete %1$s, still owns Context(s)"),
        /**
         * Database already contains restrictions.
         */
        DATABASE_ALREADY_CONTAINS_RESTRICTIONS("Database already contains restrictions."),
        /**
         * No restrictions available to %1$s.
         */
        NO_RESTRICTIONS_AVAILABLE_TO("No restrictions available to %1$s."),
        /**
         * Either add, edit or remove restrictions
         */
        EITHER_ADD_EDIT_OR_REMOVE("Either add, edit or remove restrictions"),
        /**
         * The element %1$s is not contained in the current restrictions and thus cannot be edited
         */
        RESTRICTION_NOT_CONTAINED("The element %1$s is not contained in the current restrictions and thus cannot be edited"),
        /**
         * "The element %1$s is already contained"
         */
        RESTRICTION_ALREADY_CONTAINED("The element %1$s is already contained"),
        /**
         * UserModuleAccess must not be null
         */
        MODULE_ACCESS_NOT_NULL("UserModuleAccess must not be null"),
        /**
         * The following restrictions are going to be removed, but still are in use: %1$s
         */
        MODULE_ACCESS_RESTRICTIONS_IN_USE("The following restrictions are going to be removed, but still are in use: %1$s");


        private final String text;

        private Code(final String text) {
            this.text = text;
        }

        public final String getText() {
            return text;
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = -3749789561669512300L;

    /**
     * Initializes a new {@link OXAutoCIDException}.
     * @param code
     */
    public OXAutoCIDException(final Code code) {
        super(code.getText());
    }

    /**
     * Initializes a new {@link OXAutoCIDException}.
     * @param code
     */
    public OXAutoCIDException(final Code code, final String... args) {
        super(String.format(code.getText(), (Object[])args));
    }

    /**
     * Initializes a new {@link OXAutoCIDException}.
     * @param arg0
     */
    public OXAutoCIDException(Throwable arg0) {
        super(arg0);
    }

}
