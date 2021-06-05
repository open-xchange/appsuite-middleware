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

package com.openexchange.mail.mime;

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link PlainTextAddress} - A plain-text internet address without a personal part.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PlainTextAddress extends InternetAddress {

    private static final long serialVersionUID = -3276144799717449603L;

    private static final String TYPE = "rfc822";

    /**
     * Creates a newly allocated array of {@link PlainTextAddress} generated from specified addresses.
     *
     * @param addresses The source addresses as an array of {@link String}
     * @return A newly allocated array of {@link PlainTextAddress}
     */
    public static PlainTextAddress[] getAddresses(String[] addresses) {
        if ((addresses == null) || (addresses.length == 0)) {
            return new PlainTextAddress[0];
        }
        final PlainTextAddress[] retval = new PlainTextAddress[addresses.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new PlainTextAddress(addresses[i]);
        }
        return retval;
    }

    /**
     * Parses specified address list (split by comma) and generates <code>PlainTextAddress</code> instances for each "token".
     *
     * @param addressList The address list
     * @return The <code>PlainTextAddress</code> instances
     */
    public static InternetAddress[] parseAddresses(String addressList) {
        if (Strings.isEmpty(addressList)) {
            return new PlainTextAddress[0];
        }
        String[] addrs = addressList.indexOf(',') >= 0 ? Strings.splitByCommaNotInQuotes(addressList) : new String[] { addressList };
        List<InternetAddress> l = new ArrayList<InternetAddress>(addrs.length);
        for (String addr : addrs) {
            l.add(new PlainTextAddress(addr));
        }
        return l.toArray(new InternetAddress[l.size()]);
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    private final String plainAddress;
    private final int hashCode;

    /**
     * Constructs a new {@link PlainTextAddress}.
     *
     * @param address The plain text address
     */
    public PlainTextAddress(String address) {
        this.plainAddress = QuotedInternetAddress.init(MimeMessageUtility.decodeMultiEncodedHeader(address));
        hashCode = Strings.asciiLowerCase(address).hashCode();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return plainAddress;
    }

    @Override
    public String getAddress() {
        return plainAddress;
    }

    @Override
    public String getPersonal() {
        return null;
    }

    @Override
    public boolean equals(Object address) {
        if (address instanceof InternetAddress) {
            final InternetAddress ia = (InternetAddress) address;
            return this.plainAddress.equalsIgnoreCase(ia.getAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toUnicodeString() {
        return plainAddress;
    }

}
