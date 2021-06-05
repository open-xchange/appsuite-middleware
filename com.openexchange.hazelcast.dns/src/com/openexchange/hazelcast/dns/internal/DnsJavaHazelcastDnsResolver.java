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

package com.openexchange.hazelcast.dns.internal;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.dns.HazelcastDnsResolver;
import com.openexchange.hazelcast.dns.HazelcastDnsResolverConfig;
import com.openexchange.java.Strings;


/**
 * {@link DnsJavaHazelcastDnsResolver} - The Hazelcast DNS resolver using <a href="https://github.com/dnsjava/dnsjava">dnsjava</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DnsJavaHazelcastDnsResolver implements HazelcastDnsResolver {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DnsJavaHazelcastDnsResolver.class);
    }

    private final SimpleResolver resolver;

    /**
     * Initializes a new {@link DnsJavaHazelcastDnsResolver}.
     *
     * @param config The configuration apply
     * @throws OXException If initialization fails
     */
    public DnsJavaHazelcastDnsResolver(HazelcastDnsResolverConfig config) throws OXException {
        super();
        try {
            SimpleResolver resolver = Strings.isEmpty(config.getResolverHost()) ? new SimpleResolver() : new SimpleResolver(config.getResolverHost());
            if (config.getResolverPort() > 0) {
                resolver.setPort(config.getResolverPort());
            }
            this.resolver = resolver;
        } catch (UnknownHostException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

    @Override
    public InetSocketAddress getAddress() {
        return resolver.getAddress();
    }

    @Override
    public List<String> resolveByName(Collection<String> domainNames) throws OXException {
        if (domainNames == null || domainNames.isEmpty()) {
            return Collections.emptyList();
        }

        // Set for collecting all host addresses
        Set<InetAddress> addresses = null;

        // Iterate domain names and acquire host addresses for each domain name
        for (String domainName : domainNames) {
            if (Strings.isNotEmpty(domainName)) {
                List<InetAddress> inetAddresses = resolveByName(domainName);
                if (inetAddresses == null) {
                    // Something went wrong
                    return Collections.emptyList();
                }

                if (!inetAddresses.isEmpty()) {
                    if (addresses == null) {
                        addresses = new LinkedHashSet<>(inetAddresses);
                    } else {
                        addresses.addAll(inetAddresses);
                    }
                }
            }
        }

        // Check if none added
        if (addresses == null) {
            return Collections.emptyList();
        }

        // Compile list from superset
        List<String> hostAddresses = new ArrayList<>(addresses.size());
        for (InetAddress address : addresses) {
            hostAddresses.add(address.getHostAddress());
        }
        return hostAddresses;
    }

    private List<InetAddress> resolveByName(String domainName) throws OXException {
        // Initialize look-up
        Lookup lookup = initLookup(domainName);

        // Resolve...
        Record[] records = lookup.run();
        if (records == null) {
            int result = lookup.getResult();
            switch (result) {
                case Lookup.HOST_NOT_FOUND:
                    // Signal no host addresses only for given domain name
                    LoggerHolder.LOG.warn("Host not found. Encountered \"{}\" for domain name \"{}\". Assuming no available host addresses for that domain name.", getErrorOrEmptyString(lookup), domainName);
                    return Collections.emptyList();
                case Lookup.TRY_AGAIN:
                    // Abort complete look-up through returning null
                    LoggerHolder.LOG.warn("Try again. Encountered \"{}\" for domain name \"{}\". Aborting complete look-up; assuming no available host addresses for all domain names.", getErrorOrEmptyString(lookup), domainName);
                    return null;
                case Lookup.UNRECOVERABLE:
                    // Abort complete look-up through returning null
                    LoggerHolder.LOG.warn("Unrecoverable error. Encountered \"{}\" for domain name \"{}\". Aborting complete look-up; assuming no available host addresses for all domain names.", getErrorOrEmptyString(lookup), domainName);
                    return null;
                default:
                    // Signal no host addresses only for given domain name
                    String errorString = getErrorOrEmptyString(lookup);
                    if (Strings.isEmpty(errorString)) {
                        LoggerHolder.LOG.warn("Unknown result. Failed to resolve host addresses for domain name \"{}\". Assuming no available host addresses for that domain name.", domainName);
                    } else {
                        LoggerHolder.LOG.warn("Unknown result. Encountered \"{}\" for domain name \"{}\". Failed to resolve host addresses for domain name \"{}\". Assuming no available host addresses for that domain name.", errorString, domainName);
                    }
                    return Collections.emptyList();
            }
        }

        // Collect records of interest
        List<InetAddress> addresses = new ArrayList<>();
        for (Record record : records) {
            if (record instanceof ARecord) {
                ARecord aRecord = (ARecord) record;
                addresses.add(aRecord.getAddress());
            } else if (record instanceof AAAARecord) {
                AAAARecord aaaaRecord = (AAAARecord) record;
                addresses.add(aaaaRecord.getAddress());
            }
        }
        return addresses;
    }

    private Lookup initLookup(String domainName) throws OXException {
        try {
            // Initialize look-up
            Lookup lookup = new Lookup(domainName, org.xbill.DNS.Type.ANY);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            return lookup;
        } catch (TextParseException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

    private String getErrorOrEmptyString(Lookup lookup) {
        try {
            return lookup.getErrorString();
        } catch (IllegalStateException e) {
            // Unknown result
            LoggerHolder.LOG.trace("Unkown result", e);
            return "";
        }
    }

}
