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

package com.openexchange.sessiond.impl;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.NumberRange;
import org.apache.commons.lang.math.Range;
import org.apache.commons.validator.routines.InetAddressValidator;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.IPAddressUtil;
import edazdarevic.commons.net.CIDRUtils;

/**
 * {@link IPRange} - An IP range of either IPv4 or IPv6 addresses.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IPRange {

    private final Map<String, Boolean> cache;
    private final Range ipv4Range;
    private final IPv6AddressRange ipv6Range;

    /**
     * Initializes a new {@link IPRange}.
     * 
     * @param ipv4Range The IPv4 address range
     * @param ipv6Range The IPv6 address range
     */
    public IPRange(final Range ipv4Range, final Range ipv6Range) {
        super();
        this.ipv4Range = ipv4Range;
        if (ipv6Range != null) {
            IPv6AddressRange v6Range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromBigInteger((BigInteger) ipv6Range.getMinimumNumber()), IPv6Address.fromBigInteger((BigInteger) ipv6Range.getMaximumNumber()));
            this.ipv6Range = v6Range;
        } else {
            this.ipv6Range = null;
        }
        cache = new ConcurrentHashMap<String, Boolean>(512, 0.9f, 1);
    }

    /**
     * Checks if specified IP address is covered by configured IP range.
     *
     * @param ipAddress The IP address to check
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(String ipAddress) {
        // Check for cached entry
        {
            Boolean cached = cache.get(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }

        // Calculate...
        byte[] octets = IPAddressUtil.textToNumericFormatV4(ipAddress);
        if (null != octets) {
            /*
             * IPv4
             */
            boolean ret = null != ipv4Range && ipv4Range.containsLong(ipToLong(octets));
            cache.put(ipAddress, Boolean.valueOf(ret));
            return ret;
        }
        return containsIPv6(ipAddress);
    }

    /**
     * Checks if specified IPv4 octets are covered by configured IP range.
     *
     * @param octets The IPv4 octets to check
     * @param ipAddress The octets' IPv4 string representation; might be <code>null</code>
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsIPv4(byte[] octets, String ipAddress) {
        // Check for cached entry
        if (null != ipAddress) {
            Boolean cached = cache.get(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }

        if (null != octets) {
            boolean ret = null != ipv4Range && ipv4Range.containsLong(ipToLong(octets));
            if (null != ipAddress) {
                cache.put(ipAddress, Boolean.valueOf(ret));
            }
            return ret;
        }
        return false;
    }

    /**
     * Checks if specified IPv6 octets are covered by configured IP range.
     *
     * @param octets The IPv6 octets to check
     * @param ipAddress The octets' IPv6 string representation; might be <code>null</code>
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsIPv6(byte[] octets, String ipAddress) {
        return contains(ipAddress);
    }

    public boolean containsIPv6(String ipAddress) {
        if (null != ipAddress) {
            Boolean cached = cache.get(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }
        IPv6Address fromString = IPv6Address.fromString(ipAddress);
        boolean ret = null != ipv6Range && ipv6Range.contains(fromString);
        if (null != ipAddress) {
            cache.put(ipAddress, Boolean.valueOf(ret));
        }
        return ret;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (null != ipv4Range) {
            for (final byte b : longToIP(ipv4Range.getMinimumLong())) {
                sb.append(b < 0 ? 256 + b : b);
                sb.append('.');
            }
            sb.setCharAt(sb.length() - 1, '-');
            for (final byte b : longToIP(ipv4Range.getMaximumLong())) {
                sb.append(b < 0 ? 256 + b : b);
                sb.append('.');
            }
            sb.setLength(sb.length() - 1);
        }
        if (null != ipv6Range) {
            sb.append(ipv6Range.toString());
        }
        return sb.toString();
    }

    /**
     * Parses specified string to an IP range.
     *
     * @param string The string to parse
     * @return The resulting IP range or <code>null</code> if passed string is empty
     * @throws IllegalArgumentException If parsing fails
     */
    public static IPRange parseRange(final String string) {
        if (com.openexchange.java.Strings.isEmpty(string)) {
            return null;
        }
        if (string.indexOf('-') > 0) {  // Range with '-'
            return handleWithSlash(string);
        } else if (string.indexOf('/') > 0) { // Range as CIDR
            return handleCIDR(string);
        }
        return handleSingleAddress(string);
    }

    private static IPRange handleSingleAddress(String address) {
        if (InetAddressValidator.getInstance().isValidInet4Address(address)) {
            byte[] octets = IPAddressUtil.textToNumericFormatV4(address);
            return new IPRange(new LongRange(ipToLong(octets), ipToLong(octets)), null);
        }
        final IPv6Address iPv6Address = IPv6Address.fromString(address);
        return new IPRange(null, new NumberRange(iPv6Address.toBigInteger(), iPv6Address.toBigInteger()));

    }

    private static IPRange handleCIDR(String cidrRange) {
        try {
            CIDRUtils cidrUtils = new CIDRUtils(cidrRange);
            InetAddress startAddress = cidrUtils.getStartAddress();
            InetAddress endAddress = cidrUtils.getEndAddress();
            if (InetAddressValidator.getInstance().isValidInet4Address(startAddress.getHostAddress())) { //handle v4
                byte[] octetsStart = IPAddressUtil.textToNumericFormatV4(startAddress.getHostName());
                byte[] octetsEnd = IPAddressUtil.textToNumericFormatV4(endAddress.getHostName());

                final LongRange ipv4Range = new LongRange(ipToLong(octetsStart), ipToLong(octetsEnd));
                return new IPRange(ipv4Range, null);
            }

            final NumberRange ipv6Range = new NumberRange(IPv6Address.fromString(startAddress.toString().replaceAll("/", "")).toBigInteger(), IPv6Address.fromString(endAddress.toString().replaceAll("/", "")).toBigInteger());
            return new IPRange(null, ipv6Range);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            //LOG.
        }
        return null;
    }

    private static IPRange handleWithSlash(String range) {
        final String[] addresses = range.split("\\s*-\\s*");

        if (InetAddressValidator.getInstance().isValidInet4Address(addresses[0])) {
            byte[] octetsStart = IPAddressUtil.textToNumericFormatV4(addresses[0]);
            final byte[] octetsEnd = IPAddressUtil.textToNumericFormatV4(addresses[1]);
            final LongRange ipv4Range = new LongRange(ipToLong(octetsStart), ipToLong(octetsEnd));

            return new IPRange(ipv4Range, null);
        }
        final IPv6AddressRange rangeObj = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(addresses[0]), IPv6Address.fromString(addresses[1]));

        final NumberRange ipv6Range = new NumberRange(rangeObj.getFirst().toBigInteger(), rangeObj.getLast().toBigInteger());
        return new IPRange(null, ipv6Range);
    }

    private static long ipToLong(final byte[] octets) {
        long result = 0;
        for (int i = 0; i < octets.length; i++) {
            result |= octets[i] & 0xff;
            if (i < octets.length - 1) {
                result <<= 8;
            }
        }
        return result;
    }

    private static byte[] longToIP(long value) {
        final List<Byte> retval = new ArrayList<Byte>();
        while (value != 0) {
            retval.add(Byte.valueOf((byte) (value & 0xff)));
            value >>= 8;
        }
        Collections.reverse(retval);
        return Autoboxing.B2b(retval);
    }
}
