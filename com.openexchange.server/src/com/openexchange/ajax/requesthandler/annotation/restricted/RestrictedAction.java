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

package com.openexchange.ajax.requesthandler.annotation.restricted;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link RestrictedAction}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RestrictedAction {

    /**
     * Grants access to any valid request
     */
    public static final String GRANT_ALL = "*";

    /**
     * This action requires the user to have used full password, not restricted session
     */
    public static final String REQUIRES_FULL_AUTH = "requires_full_auth";

    /**
     * Indicates the {@link Type} of restriction. {@link Type#READ}, {@link Type#WRITE}, or {@link Type#ALL}
     *
     * Defaults to {@link Type#ALL}
     *
     * @return The type
     */
    Type type() default Type.ALL;

    /**
     * Indicates the required module permission. Defaults to {@link RestrictedAction#GRANT_ALL}
     *
     * @return the module
     */
    String module() default GRANT_ALL;

    /**
     * Indicates whether a custom scope check is necessary or not.
     * See {@link OAuthScopeCheck} for details.
     *
     * Defaults to <code>false</code>
     */
    boolean hasCustomOAuthScopeCheck() default false;

    /**
     * {@link Type} - specifies the type of restriction.
     *
     * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
     * @since v7.10.4
     */
    public enum Type {

        READ("read_"),
        WRITE("write_"),
        ALL("");

        private String prefix;

        /**
         * Initializes a new {@link Type}.
         *
         * @param prefix The prefix used to create the scope (e.g.: <code>read_</code> -> <code>read_mails</code>)
         */
        private Type(String prefix) {
            this.prefix = prefix;
        }

        /**
         * Creates the scope for the given module with this restriction type
         *
         * @param module The module identifier to create the scope for
         * @return the created scope
         */
        public String getScope(String module) {
            return prefix + module;
        }

        /**
         * Gets the module of this scope
         *
         * @param scope The scope
         * @return The module part of the scope or the scope as it is if it not contains the prefix of this part
         */
        public String getModule(String scope) {
            return scope.startsWith(prefix) ? scope.substring(prefix.length()) : scope;
        }
    }
}
