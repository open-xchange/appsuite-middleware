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

package com.openexchange.messaging.generic.internet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingExceptionCodes;

/**
 * {@link MimeAddressMessagingHeader} - A MIME address header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeAddressMessagingHeader implements MessagingAddressHeader {

    /**
     * Parse the given comma-separated sequence of addresses. Addresses must follow RFC822 syntax.
     *
     * @param name The header name
     * @param addressList The comma-separated sequence of addresses
     * @return The parsed address headers
     * @throws OXException If parsing fails
     */
    public static Collection<MimeAddressMessagingHeader> parseRFC822(final String name, final String addressList) throws OXException {
        try {
            final InternetAddress[] internetAddresses = QuotedInternetAddress.parse(addressList);
            final List<MimeAddressMessagingHeader> retval = new ArrayList<MimeAddressMessagingHeader>(internetAddresses.length);
            for (final InternetAddress internetAddresse : internetAddresses) {
                retval.add(new MimeAddressMessagingHeader(name, (QuotedInternetAddress) internetAddresse));
            }
            return retval;
        } catch (AddressException e) {
            throw MessagingExceptionCodes.ADDRESS_ERROR.create(e, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Initializes a new {@link MimeAddressMessagingHeader} interpreted in RFC822 format.
     *
     * @param name The header name
     * @param address The address in RFC822 format
     * @return A new {@link MimeAddressMessagingHeader} interpreted in RFC822 format
     * @throws OXException If specified address cannot be parsed
     */
    public static MimeAddressMessagingHeader valueOfRFC822(final String name, final String address) throws OXException {
        try {
            return new MimeAddressMessagingHeader(name, new QuotedInternetAddress(address));
        } catch (AddressException e) {
            throw MessagingExceptionCodes.ADDRESS_ERROR.create(e, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Initializes a new plain {@link MimeAddressMessagingHeader} with no specific format.
     *
     * @param name The header name
     * @param personal The (optional) personal part of the address; may be <code>null</code>
     * @param address The address with no specific format
     * @return A new plain {@link MimeAddressMessagingHeader} with no specific format
     */
    public static MimeAddressMessagingHeader valueOfPlain(final String name, final String personal, final String address) {
        return new MimeAddressMessagingHeader(name, personal, address);
    }

    /*-
     * Member section
     */

    private final QuotedInternetAddress internetAddress;

    private final String name;

    private MimeAddressMessagingHeader(final String name, final String personal, final String address) {
        super();
        if (null == address) {
            throw new IllegalArgumentException("Address is null.");
        }
        this.name = name;
        internetAddress = new QuotedInternetAddress();
        try {
            internetAddress.setPersonal(personal, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Cannot occur
        }
        internetAddress.setAddress(address);
    }

    private MimeAddressMessagingHeader(final String name, final QuotedInternetAddress internetAddress) {
        super();
        if (null == internetAddress) {
            throw new IllegalArgumentException("Internet address is null.");
        }
        this.name = name;
        this.internetAddress = internetAddress;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Convert this address into a RFC 822 / RFC 2047 encoded address. The resulting string contains only US-ASCII characters, and hence is
     * mail-safe.
     *
     * @return The RFC 822 / RFC 2047 encoded address
     */
    @Override
    public String getValue() {
        return internetAddress.toString();
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.ADDRESS;
    }

    /**
     * Gets the properly formatted address.
     *
     * @return The properly formatted address
     */
    public String getUnicodeValue() {
        return internetAddress.toUnicodeString();
    }

    @Override
    public String getPersonal() {
        return internetAddress.getPersonal();

    }

    @Override
    public String getAddress() {
        return internetAddress.getAddress();
    }

    @Override
    public void setAddress(final String address) throws OXException {
        if (null == address) {
            final IllegalArgumentException e = new IllegalArgumentException("Address is null.");
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        internetAddress.setAddress(address);
    }

    @Override
    public void setPersonal(final String personal) {
        try {
            internetAddress.setPersonal(personal, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Cannot occur
        }
    }

}
