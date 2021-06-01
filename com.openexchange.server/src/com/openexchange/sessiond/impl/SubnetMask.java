
package com.openexchange.sessiond.impl;

import java.math.BigInteger;
import java.util.regex.Pattern;
import org.apache.commons.net.util.SubnetUtils;
import com.openexchange.net.IPAddressUtil;

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

/**
 * {@link SubnetMask}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class SubnetMask {

    private static final Pattern ipv4Dotted = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    private static final Pattern ipv4CIDR = Pattern.compile("/(\\d{1,2})");

    private static final Pattern ipv6CIDR = Pattern.compile("/(\\d{1,3})");

    private String v4Mask;

    private BigInteger v6Mask;

    private boolean v4CIDR;

    public SubnetMask(String v4, String v6) {
        if (v4 != null) {
            if (ipv4Dotted.matcher(v4).matches()) {
                this.v4Mask = v4;
            } else if (ipv4CIDR.matcher(v4).matches()) {
                this.v4Mask = v4;
                v4CIDR = true;
            } else if (v4.trim().equals("")) {
                // Leave empty
            } else {
                throw new IllegalArgumentException(v4 + " is neither a valid CIDR nor a valid dotted representation of an IPv4 subnet mask.");
            }
        }

        if (v6 != null) {
            if (ipv6CIDR.matcher(v6).matches()) {
                v6Mask = BigInteger.valueOf(2).pow(128).subtract(BigInteger.valueOf(2).pow(128 - Integer.parseInt(v6.substring(1))));
            } else if (v6.trim().equals("")) {
                // Leave empty
            } else {
                throw new IllegalArgumentException(v6 + " is not a valid CIDR representation of an IPv6 subnet mask.");
            }
        }
    }

    public boolean areInSameSubnet(String firstIP, String secondIP) {
        if (firstIP == null || secondIP == null) {
            return false;
        }

        if (v4Mask != null) {
            if (ipv4Dotted.matcher(firstIP).matches() && ipv4Dotted.matcher(secondIP).matches()) {
                SubnetUtils subnet = v4CIDR ? new SubnetUtils(firstIP + v4Mask) : new SubnetUtils(firstIP, v4Mask);
                subnet.setInclusiveHostCount(true);
                return subnet.getInfo().isInRange(secondIP);
            }
        }

        if (v6Mask != null) {
            byte[] firstV6Octets = IPAddressUtil.textToNumericFormatV6(firstIP);
            if (firstV6Octets == null) {
                return false;
            }
            byte[] secondV6Octets = IPAddressUtil.textToNumericFormatV6(secondIP);
            if (secondV6Octets == null) {
                return false;
            }
            BigInteger firstV6 = ipToBigIntegerV6(firstV6Octets);
            BigInteger secondV6 = ipToBigIntegerV6(secondV6Octets);

            return firstV6.and(v6Mask).equals(secondV6.and(v6Mask));
        }
        return false;
    }

    private static BigInteger ipToBigIntegerV6(final byte[] octets) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < octets.length; i++) {
            result = result.or(BigInteger.valueOf(octets[i]).and(BigInteger.valueOf(0xff)));
            if (i < octets.length - 1) {
                result = result.shiftLeft(8);
            }
        }
        return result;
    }

}
