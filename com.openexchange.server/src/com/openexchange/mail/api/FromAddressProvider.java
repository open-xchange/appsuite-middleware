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
