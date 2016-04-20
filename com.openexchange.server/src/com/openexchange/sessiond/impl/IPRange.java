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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.Range;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.IPAddressUtil;


/**
 * {@link IPRange} - An IP range of either IPv4 or IPv6 addresses.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IPRange {

    private final Map<String, Boolean> cache;
    private final Range ipv4Range;
    private final Range ipv6Range;

    /**
     * Initializes a new {@link IPRange}.
     * @param ipv4Range The IPv4 address range
     * @param ipv6Range The IPv6 address range
     */
    public IPRange(final Range ipv4Range, final Range ipv6Range) {
        super();
        this.ipv4Range = ipv4Range;
        this.ipv6Range = ipv6Range;
        cache = new ConcurrentHashMap<String, Boolean>(512);
    }

    /**
     * Gets the IP range for IPv4 addresses
     *
     * @return The IPv4 address range
     */
    public Range getIpv4Range() {
        return ipv4Range;
    }

    /**
     * Gets the IP range for IPv6 addresses
     *
     * @return The IPv6 address range
     */
    public Range getIpv6Range() {
        return ipv6Range;
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
        /*
         * IPv6
         */
        octets = IPAddressUtil.textToNumericFormatV6(ipAddress);
        if (null == octets) {
            throw new IllegalArgumentException("Not an IP address: " + ipAddress);
        }
        boolean ret = null != ipv6Range && ipv6Range.containsNumber(ipToBigInteger(octets));
        cache.put(ipAddress, Boolean.valueOf(ret));
        return ret;
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
        // Check for cached entry
        if (null != ipAddress) {
            Boolean cached = cache.get(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }

        if (null != octets) {
            boolean ret = null != ipv6Range && ipv6Range.containsNumber(ipToBigInteger(octets));
            if (null != ipAddress) {
                cache.put(ipAddress, Boolean.valueOf(ret));
            }
            return ret;
        }
        return false;
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
            for (final byte b : longToIP(ipv6Range.getMinimumLong())) {
                sb.append(b < 0 ? 256 + b : b);
                sb.append('.');
            }
            sb.setCharAt(sb.length() - 1, '-');
            for (final byte b : longToIP(ipv6Range.getMaximumLong())) {
                sb.append(b < 0 ? 256 + b : b);
                sb.append('.');
            }
            sb.setLength(sb.length() - 1);
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
        if(string.indexOf('-') > 0) {
            final String[] addresses = string.split("\\s*-\\s*");
            // Try IPv4 first
            byte[] octetsStart = IPAddressUtil.textToNumericFormatV4(addresses[0]);
            if (null == octetsStart) {
                // IPv6
                octetsStart = IPAddressUtil.textToNumericFormatV6(addresses[0]);
                if (null == octetsStart) {
                    throw new IllegalArgumentException("Not an IP address range: " + string);
                }
                final byte[] octetsEnd = IPAddressUtil.textToNumericFormatV6(addresses[1]);
                if (null == octetsEnd) {
                    throw new IllegalArgumentException("Not an IPv6 address: " + addresses[1]);
                }
                final LongRange ipv6Range = new LongRange(ipToBigInteger(octetsStart), ipToBigInteger(octetsEnd));
                return new IPRange(null, ipv6Range);
            }
            // IPv4
            final byte[] octetsEnd = IPAddressUtil.textToNumericFormatV4(addresses[1]);
            if (null == octetsEnd) {
                throw new IllegalArgumentException("Not an IPv4 address: " + addresses[1]);
            }
            final LongRange ipv4Range = new LongRange(ipToLong(octetsStart), ipToLong(octetsEnd));
            return new IPRange(ipv4Range, null);
        }
        // Try IPv4 first
        byte[] octets = IPAddressUtil.textToNumericFormatV4(string);
        if (null == octets) {
            // IPv6
            octets = IPAddressUtil.textToNumericFormatV6(string);
            if (null == octets) {
                throw new IllegalArgumentException("Not an IP address: " + string);
            }
            final byte[] octetsEnd = new byte[16];
            int i;
            boolean bool = true;
            for (i = 0; bool && i < octetsEnd.length; i++) {
                bool = (octets[i] == 0);
                if (!bool) {
                    octetsEnd[i] = octets[i];
                }
            }
            while (i < octetsEnd.length) {
                octetsEnd[i++] = (byte) 255;
            }
            return new IPRange(null, new LongRange(ipToBigInteger(octets), ipToBigInteger(octetsEnd)));
        }
        // IPv4
        return new IPRange(new LongRange(ipToLong(octets), ipToLong(octets)), null);
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

    private static BigInteger ipToBigInteger(final byte[] octets) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < octets.length; i++) {
            result = result.or(BigInteger.valueOf(octets[i]).and(BigInteger.valueOf(0xff)));
            if (i < octets.length - 1) {
                result = result.shiftLeft(8);
            }
        }
        return result;
    }

    private static byte[] longToIP(long value) {
        final List<Byte> retval = new ArrayList<Byte>();
        while (value != 0) {
            retval.add(Byte.valueOf((byte)(value & 0xff)));
            value >>= 8;
        }
        Collections.reverse(retval);
        return Autoboxing.B2b(retval);
    }
}
