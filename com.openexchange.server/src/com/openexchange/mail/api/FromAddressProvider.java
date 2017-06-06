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

package com.openexchange.mail.api;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link FromAddressProvider} - Provides the <code>"From"</code> address to use for reply/forward.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class FromAddressProvider {

    /** The provider type */
    public static enum Type {
        /**
         * No <code>"From"</code> address is supposed to be set
         */
        NONE,
        /**
         * <code>"From"</code> address is supposed to be detected by mail account identifier
         */
        DETECT_BY_ACCOUNT_ID,
        /**
         * <code>"From"</code> address is specified by provider
         */
        SPECIFIED,
        ;
    }

    private static final FromAddressProvider NONE_PROVIDER = new FromAddressProvider(null, Type.NONE);

    /**
     * Gets the provider indicating to use no <code>"From"</code> address
     *
     * @return The none provider
     */
    public static FromAddressProvider none() {
        return NONE_PROVIDER;
    }

    private static final FromAddressProvider DETECT_BY_ACCOUNT_ID_PROVIDER = new FromAddressProvider(null, Type.DETECT_BY_ACCOUNT_ID);

    /**
     * Gets the provider indicating to detect <code>"From"</code> address by mail account identifier
     *
     * @return The detecting provider
     */
    public static FromAddressProvider byAccountId() {
        return DETECT_BY_ACCOUNT_ID_PROVIDER;
    }

    /**
     * Gets the provider for specified address
     *
     * @param address The address to use
     * @return The appropriate provider
     * @throws OXException If address is no valid Internet Address
     */
    public static FromAddressProvider providerFor(String address) throws OXException {
        if (Strings.isEmpty(address)) {
            return null;
        }
        try {
            return new FromAddressProvider(new QuotedInternetAddress(address), Type.SPECIFIED);
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    // --------------------------------------------------------------------------------------------------------------

    private final InternetAddress fromAddress;
    private final Type type;

    /**
     * Initializes a new {@link FromAddressProvider}.
     */
    FromAddressProvider(InternetAddress fromAddress, Type type) {
        super();
        this.fromAddress = fromAddress;
        this.type = type;
    }

    /**
     * Gets the <code>"From"</code> address
     *
     * @return The <code>"From"</code> address
     */
    public InternetAddress getFromAddress() {
        return fromAddress;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Checks if this provider is of given type.
     *
     * @param type The type to check against
     * @return <code>true</code> if provider is of given type; otherwise <code>false</code>
     */
    public boolean isOfType(Type type) {
        return this.type == type;
    }

    /**
     * Checks for non provider.
     */
    public boolean isNone() {
        return this.type == Type.NONE;
    }

    /**
     * Checks for detect-by provider.
     */
    public boolean isDetectBy() {
        return this.type == Type.DETECT_BY_ACCOUNT_ID;
    }

    /**
     * Checks for specifying provider.
     */
    public boolean isSpecified() {
        return this.type == Type.SPECIFIED;
    }

}
