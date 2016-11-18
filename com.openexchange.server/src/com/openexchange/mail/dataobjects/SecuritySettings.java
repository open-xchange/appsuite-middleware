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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.dataobjects;

/**
 * {@link SecuritySettings} - (Immutable) Security settings for mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class SecuritySettings {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a <code>SecuritySettings</code> instance */
    public static final class Builder {

        private boolean encrypt;
        private boolean sign;
        private String authentication;

        /**
         * Initializes a new {@link DefaultDoveAdmCommand.Builder} with optional identifier default to <code>"1"</code>.
         */
        Builder() {
            super();
        }

        /**
         * Sets the encrypt flag.
         *
         * @param encrypt The encrypt flag
         * @return This builder
         */
        public Builder encrypt(boolean encrypt) {
            this.encrypt = encrypt;
            return this;
        }

        /**
         * Sets the sign flag.
         *
         * @param encrypt The sign flag
         * @return This builder
         */
        public Builder sign(boolean sign) {
            this.sign = sign;
            return this;
        }

        /**
         * Sets the authentication string.
         *
         * @param authentication The authentication string
         * @return This builder
         */
        public Builder authentication(String authentication) {
            this.authentication = authentication;
            return this;
        }

        /**
         * Builds the <code>SecuritySettings</code> instance from this builder's arguments.
         *
         * @return The <code>SecuritySettings</code> instance
         */
        public SecuritySettings build() {
            return new SecuritySettings(encrypt, sign, authentication);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final boolean encrypt;
    private final boolean sign;
    private final String authentication;

    /**
     * Initializes a new {@link SecuritySettings}.
     */
    SecuritySettings(boolean encrypt, boolean sign, String authentication) {
        super();
        this.encrypt = encrypt;
        this.sign = sign;
        this.authentication = authentication;
    }

    /**
     * Checks if any considerable settings have been applied.
     *
     * @return <code>true</code> if any considerable settings have been applied; otherwise <code>false</code>
     */
    public boolean anythingSet() {
        if (encrypt) {
            return true;
        }
        if (sign) {
            return true;
        }
        if (null != authentication) {
            return true;
        }
        return false;
    }

    /**
     * Gets the encrypt flag
     *
     * @return The encryptv
     */
    public boolean isEncrypt() {
        return encrypt;
    }

    /**
     * Gets the sign flag
     *
     * @return The sign flag
     */
    public boolean isSign() {
        return sign;
    }

    /**
     * Gets the authentication string
     *
     * @return The authentication string
     */
    public String getAuthentication() {
        return authentication;
    }

}
