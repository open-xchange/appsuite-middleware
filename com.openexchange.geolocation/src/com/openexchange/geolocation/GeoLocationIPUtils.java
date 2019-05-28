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

package com.openexchange.geolocation;

import java.math.BigInteger;
import java.net.InetAddress;
import com.openexchange.exception.OXException;
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
     * @throws OXException if the specified IP address is invalid
     */
    public static BigInteger convertIp(InetAddress ipAddress) throws OXException {
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
        return new Pair<BigInteger, BigInteger>(address.getLower().getValue(), address.getUpper().getValue());
    }
}
