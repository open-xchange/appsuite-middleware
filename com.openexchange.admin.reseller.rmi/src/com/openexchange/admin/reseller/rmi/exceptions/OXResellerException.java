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

package com.openexchange.admin.reseller.rmi.exceptions;


/**
 * @author choeger
 *
 */
public class OXResellerException extends Exception {

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
         * Maximum overall number of users reached: %1$s
         */
        MAXIMUM_OVERALL_NUMBER_OF_CONTEXT_REACHED("Maximum overall number of users reached: %1$s"),
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
        MODULE_ACCESS_RESTRICTIONS_IN_USE("The following restrictions are going to be removed, but still are in use: %1$s"),
        /**
         * Subadmin %s is not allowed to create subadmins
         */
        SUBADMIN_NOT_ALLOWED_TO_CREATE_SUBADMIN("Subadmin %s is not allowed to create subadmins"),
        /**
         * Subsubadmins cannot change restrictions
         */
        SUBSUBADMIN_NOT_ALLOWED_TO_CHANGE_RESTRICTIONS("Subsubadmins cannot change restrictions"),
        /**
         * Maximum number of subadmins per subadmin reached: %1$s
         */
        MAXIMUM_NUMBER_OF_SUBADMIN_PER_SUBADMIN_REACHED("Maximum number of subadmins per subadmin reached: %1$s"),
        /**
         * Subadmin %1$s does not belong to %2$s
         */
        SUBADMIN_DOES_NOT_BELONG_TO_SUBADMIN("Subadmin %1$s does not belong to %2$s"),
        /**
         * Subadmins cannot change parentId
         */
        SUBAMIN_NOT_ALLOWED_TO_CHANGE_PARENTID("Subadmins cannot change parentId"),
        /**
         * Cannot change parentId to id of subsubadmin
         */
        CANNOT_SET_PARENTID_TO_SUBSUBADMIN("Cannot change parentId to id of subsubadmin");

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
     * Initializes a new {@link OXResellerException}.
     * @param code
     */
    public OXResellerException(final Code code) {
        super(code.getText());
    }

    /**
     * Initializes a new {@link OXResellerException}.
     * @param code
     */
    public OXResellerException(final Code code, final String... args) {
        super(String.format(code.getText(), (Object[])args));
    }

    /**
     * Initializes a new {@link OXResellerException}.
     * @param arg0
     */
    public OXResellerException(Throwable arg0) {
        super(arg0);
    }

}
