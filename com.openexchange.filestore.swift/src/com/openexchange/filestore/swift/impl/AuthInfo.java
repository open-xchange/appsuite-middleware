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

package com.openexchange.filestore.swift.impl;

import com.openexchange.filestore.swift.impl.token.TokenParser;
import com.openexchange.java.Strings;

/**
 * {@link AuthInfo} - The authentication information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class AuthInfo {

    /** The authentication type */
    public static enum Type {
        /**
         * Authentication through passing a Rackspace API key.
         */
        RACKSPACE_API_KEY("raxkey", -1, TokenParser.TOKEN_PARSER_V2),
        /**
         * Password-based authentication according to <a href="http://developer.openstack.org/api-ref-identity-v2.html">Identity API v2</a>.
         */
        PASSWORD_V2("password.v2", 2, TokenParser.TOKEN_PARSER_V2),
        /**
         * Password-based authentication according to <a href="http://developer.openstack.org/api-ref-identity-v3.html">Identity API v3</a>.
         */
        PASSWORD_V3("password.v3", 3, TokenParser.TOKEN_PARSER_V3),
        ;

        private final String id;
        private final int version;
        private final TokenParser parser;

        private Type(String id, int version, TokenParser parser) {
            this.id = id;
            this.version = version;
            this.parser = parser;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the token parser
         *
         * @return The token parser
         */
        public TokenParser getParser() {
            return parser;
        }

        /**
         * Gets the version of the Identity API to use
         *
         * @return The version of the Identity API
         */
        public int getVersion() {
            return version;
        }

        /**
         * Gets the type for specified type identifier
         *
         * @param type The type identifier
         * @return The associated type or <code>null</code>
         */
        public static Type typeFor(String type) {
            if (Strings.isEmpty(type)) {
                return null;
            }

            String lookMeUp = Strings.asciiLowerCase(type.trim());
            if (RACKSPACE_API_KEY.id.equals(lookMeUp)) {
                // Rackspace API key authentication: "raxkey"
                return RACKSPACE_API_KEY;
            }

            if (lookMeUp.indexOf('.') <= 0) {
                // Contains no version identifier: "password"
                return PASSWORD_V2;
            }

            // Contains version identifier; e.g. "password.v2"
            for (Type t : Type.values()) {
                if (t.id.equals(lookMeUp)) {
                    return t;
                }
            }

            return null;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------- //

    private final String value;
    private final Type type;
    private final String tenantName;
    private final String domain;
    private final String identityUrl;

    /**
     * Initializes a new {@link AuthInfo}.
     */
    public AuthInfo(String value, Type type, String tenantName, String domain, String identityUrl) {
        super();
        this.value = value;
        this.type = type;
        this.tenantName = tenantName;
        this.domain = domain;
        this.identityUrl = identityUrl;
    }

    /**
     * Gets the domain.
     *
     * @return The domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the tenant name
     *
     * @return The tenant name
     */
    public String getTenantName() {
        return tenantName;
    }

    /**
     * Gets the URL for the Identity API end-point.
     *
     * @return The URL for the Identity API end-point
     */
    public String getIdentityUrl() {
        return identityUrl;
    }

    /**
     * Gets the value.
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the type.
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

}
