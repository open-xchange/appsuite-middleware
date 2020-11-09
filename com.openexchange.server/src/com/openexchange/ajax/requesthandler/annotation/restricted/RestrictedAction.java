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
