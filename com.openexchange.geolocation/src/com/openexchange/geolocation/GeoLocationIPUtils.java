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

package com.openexchange.geolocation;

import java.math.BigInteger;
import java.net.InetAddress;
import com.openexchange.java.util.Pair;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv6.IPv6Address;

/**
 * {@link GeoLocationIPUtils}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class GeoLocationIPUtils {

    /**
     * Converts the specified IP address to its numerical representation
     *
     * @param ipAddress The IP address to convert
     * @return The number representation of the IP address
     */
    public static BigInteger convertIp(InetAddress ipAddress) {
        IPAddressString addressString = new IPAddressString(ipAddress.getHostAddress());
        if (addressString.isIPv4() || addressString.getAddress().isIPv4Convertible()) {
            return new BigInteger(Long.toString(addressString.getAddress().toIPv4().longValue()));
        }
        return addressString.getAddress().toIPv6().getValue();
    }

    /**
     * Converts the specified IP address from its numerical representation
     * to a string.
     *
     * @param ipAddress The numerical representation of the IP address
     * @return The string version of the IP.
     */
    public static String convertIp(BigInteger ipAddress) {
        IPAddress address = new IPv6Address(ipAddress);
        if (address.isIPv4Convertible()) {
            return address.toIPv4().toNormalizedString();
        }
        return address.toAddressString().toNormalizedString();
    }

    /**
     * Retrieves the lower and upper numerical representations of the
     * IP range from the specified CIDR
     * 
     * @param cidr The CIDR
     * @return A {@link Pair} with the lower and upper values of the IP range denoted by the specified CIDR
     */
    public static Pair<BigInteger, BigInteger> getIPv6Range(String cidr) {
        IPAddressString ipv6Str = new IPAddressString(cidr);
        IPAddress address = ipv6Str.getAddress();
        return new Pair<>(address.getLower().getValue(), address.getUpper().getValue());
    }
}
