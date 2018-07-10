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

package com.openexchange.http.grizzly.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;
import com.openexchange.java.Strings;
import com.openexchange.net.IPRange;

/**
 * {@link IPTools} - Detects the first IP that isn't one of our known proxies and represents our new remoteIP.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IPTools {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(IPTools.class);

    /**
     * Initializes a new {@link IPTools}.
     */
    private IPTools() {
        super();
    }

    public final static String COMMA_SEPARATOR = ",";

    /**
     * Detects the first IP that isn't one of our known proxies and represents our new remoteIP. This is done by removing all known proxies
     * from the list of forwarded-for header beginning from the right side of the list. The rightmost leftover IP is then seen as our new
     * remote IP as it represents the first IP not known to us. <h4>Example:</h4>
     *
     * <pre>
     * remotes = 192.168.32.50, 192.168.33.225, 192.168.33.224
     * known = 192.168.33.225, 192.168.33.224
     * remoteIP = 192.168.32.50
     * </pre>
     *
     * @param forwardedIPs A String containing the forwarded IPs separated by comma
     * @param knownProxies A List of {@link IPRange}s containing the known proxies
     * @return The first IP that isn't a known proxy address. The remote IP or <code>null</code> if no valid remote IP could be found or an IP address is malformed
     */
    public static String getRemoteIP(String forwardedIPs, Collection<IPRange> knownProxies) {
        if (Strings.isEmpty(forwardedIPs)) {
            return null;
        }

        String remoteIP = null;
        String previousIP = null;
        try {
            // Split & iterate in reverse order until first remote IP occurs
            Collection<IPRange> knownProxiesToUse = null == knownProxies ? Collections.<IPRange> emptyList() : knownProxies;
            String[] ips = Strings.splitByComma(forwardedIPs);
            for (int i = ips.length; null == remoteIP && i-- > 0;) {
                previousIP = ips[i];
                boolean unknownByProxies = true;
                for (Iterator<IPRange> it = knownProxiesToUse.iterator(); unknownByProxies && it.hasNext();) {
                    if (it.next().contains(previousIP)) {
                        // IP is within the range. So it is a known proxy
                        unknownByProxies = false;
                    }
                }
                if (unknownByProxies) {
                    // IP address is not covered by any known proxy. Therefore consider it as the remote IP address of requesting client
                    remoteIP = previousIP;
                }
            }
        } catch (IllegalArgumentException e) {
            /*
             * If the IP is malformed the underlying framework triggered through range.contains()
             * will throw an IllegalArgumentException
             */
            LOG.debug("{} is not a valid IP. Discarding search for remote IP.", previousIP, e);
            return null;
        }

        // Don't return invalid IPs
        if (null != remoteIP && !InetAddresses.isInetAddress(remoteIP)) {
            LOG.debug("{} is not a valid IP. Discarding that candidate for remote IP.", remoteIP);
            return null;
        }

        return remoteIP;
    }

    /**
     * Takes a String of separated values, splits it at the separator, trims the split values and returns them as List.
     *
     * @param input String of separated values
     * @param separator the separator as regular expression used to split the input around this separator
     * @return the split and trimmed input as List or an empty list
     * @throws IllegalArgumentException if input or the separator are missing
     * @throws PatternSyntaxException - if the regular expression's syntax of separator is invalid
     */
    public static List<String> splitAndTrim(String input, String separator) {
        return Strings.splitAndTrim(input, separator);
    }

    /**
     * Takes a List of Strings representing IP addresses filters out the erroneous ones.
     *
     * @param ipList a List of Strings representing IP addresses
     * @return the list of erroneous IPs or the empty list meaning that all IPs are valid
     */
    public static List<String> filterErroneousIPs(List<String> ipList) {
        List<String> erroneousIPs = new ArrayList<String>(ipList.size());
        for (String ip : ipList) {
            if (!InetAddresses.isInetAddress(ip)) {
                erroneousIPs.add(ip);
            }
        }
        return erroneousIPs;
    }

    /**
     * Takes a {@link String} of comma separated IPs and transforms them into {@link IPRange}s.
     * In case a IP is malformed a warning is logged and an empty list is returned.
     *
     * @param ips A comma separated {@link String} containing the IPs
     * @return An immutable {@link List} containing the IPs as {@link IPRange}s or in case of missing or malformed IP {@link Collections#emptyList()}
     */
    public static List<IPRange> filterIP(String ips) {
        if (Strings.isEmpty(ips)) {
            return Collections.emptyList();
        }

        List<String> candidates = splitAndTrim(ips, COMMA_SEPARATOR);
        ImmutableList.Builder<IPRange> proxies = ImmutableList.builder();
        for (String ip : candidates) {
            IPRange range = IPRange.parseRange(ip);
            if (null == range) {
                LOG.warn("Falling back to empty list as \"com.openexchange.server.knownProxies\" contains malformed IP {}", ip);
                return Collections.emptyList();
            }
            proxies.add(range);
        }
        return proxies.build();
    }

}
