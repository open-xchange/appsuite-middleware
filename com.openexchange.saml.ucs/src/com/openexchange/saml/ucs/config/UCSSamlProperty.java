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

package com.openexchange.saml.ucs.config;

import com.openexchange.config.lean.Property;

/**
 * {@link UCSSamlProperty}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.10.1
 */
public enum UCSSamlProperty implements Property {
    /**
     * If UCS SAML should be enabled or disabled
     */
    enabled(false),

    /**
     * The id inside the saml authnResponse which holds the userinformation
     */
    id(""),

    /**
     * URL of where the users are redirected after logout
     */
    logoutRedirectUrl(""),

    /**
     * The URL to redirect to in case the SAML back-end fails to look up the authenticated user.
     * When left empty or not set, an HTTP 500 error page is sent instead.
     */
    failureRedirect(""),

    /**
     * The URL to redirect to in case the SAML back-end has an error, when the user logs out.
     * When left empty or not set, the value of com.openexchange.saml.ucs.failure.redirect is used.
     */
    logoutFailureRedirect(""),

    /**
     * The full path to a Java keyStore containing the IdPs certificate.
     *
     * Default: <empty>
     */
    keyStore(""),

    /**
     * Password to open the keyStore.
     *
     * Default: <empty>
     */
    keyStorePass(""),

    /**
     * The alias of the IdP certificate entry within the keyStore.
     *
     * Default: <empty>
     */
    certAlias(""),

    /**
     * The alias of the signingKey entry within the keyStore.
     *
     * Default: <empty>
     */
    signingKeyAlias(""),

    /**
     * The password of the signingKey entry within the keyStore.
     *
     * Default: <empty>
     */
    signingKeyPassword(""),

    /**
     * The alias of the decryptionKey entry within the keyStore.
     *
     * Default: <empty>
     */
    decryptionKeyAlias(""),

    /**
     * The password of the decryptionKey entry within the keystore.
     *
     * Default: <empty>
     */
    decryptionKeyPassword("");

    private final Object defaultValue;

    private static final String PREFIX = "com.openexchange.saml.ucs.";

    /**
     * Initializes a new {@link UCSSamlProperty}.
     */
    private UCSSamlProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the fully qualified name for the property
     *
     * @return the fully qualified name for the property
     */
    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    /**
     * Returns the default value of this property
     *
     * @return the default value of this property
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
