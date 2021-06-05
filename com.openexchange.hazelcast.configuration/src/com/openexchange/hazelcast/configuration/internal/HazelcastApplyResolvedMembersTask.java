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

package com.openexchange.hazelcast.configuration.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.openexchange.hazelcast.dns.HazelcastDnsResolver;
import com.openexchange.java.Strings;

/**
 * {@link HazelcastApplyResolvedMembersTask} - A {@link Runnable} implementation that queries host addresses from given DNS resolver and
 * applies them to active Hazelcast configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class HazelcastApplyResolvedMembersTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastApplyResolvedMembersTask.class);

    private final Collection<String> domainNames;
    private final HazelcastDnsResolver dnsResolver;
    private final AtomicReference<Set<String>> previousHostAddresses;
    private final Config config;
    private Object hostAddressHolder; // Utility object for logging to avoid unnecessary name service reverse lookup
    private Object hostPortHolder;    // Utility object for logging
    private Object domainNamesHolder; // Utility object for logging

    /**
     * Initializes a new {@link HazelcastApplyResolvedMembersTask}.
     *
     * @param domainNames The domain names to resolve to host addresses
     * @param dnsResolver The DNS resolved to use
     * @param initialHostAddresses The initial host addresses resolved by given DNS resolver
     * @param config The active Hazelcast configuration
     */
    public HazelcastApplyResolvedMembersTask(Collection<String> domainNames, HazelcastDnsResolver dnsResolver, Set<String> initialHostAddresses, Config config) {
        super();
        this.domainNames = domainNames;
        this.dnsResolver = dnsResolver;
        this.previousHostAddresses = new AtomicReference<Set<String>>(initialHostAddresses);
        this.config = config;
        hostAddressHolder = null;
        hostPortHolder = null;
        domainNamesHolder = null;
    }

    @Override
    public void run() {
        try {
            // Query addresses
            List<String> hostAddresses = dnsResolver.resolveByName(domainNames);
            if (hostAddresses.isEmpty()) {
                LOG.info("No members resolved for {} from DNS server {} at port {}. Leaving Hazelcast TCP/IP network configuration unchanged.", getDomainNamesHolder(), getHostAddressHolder(), getHostPortHolder());
            } else {
                Set<String> currentHostAddresses = new LinkedHashSet<>(hostAddresses);
                if (isNotEqual(currentHostAddresses, previousHostAddresses.get())) {
                    TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
                    tcpIpConfig.clear();
                    for (String hostAddress : hostAddresses) {
                        if (Strings.isNotEmpty(hostAddress)) {
                            tcpIpConfig.addMember(hostAddress);
                        }
                    }
                    LOG.info("Applied changed members for {} as resolved by DNS server {} at port {} to Hazelcast TCP/IP network configuration.", getDomainNamesHolder(), getHostAddressHolder(), getHostPortHolder());
                    previousHostAddresses.set(currentHostAddresses);
                } else {
                    LOG.debug("No members changed for {} as indicated by DNS server {} at port {}. Leaving Hazelcast TCP/IP network configuration unchanged.", getDomainNamesHolder(), getHostAddressHolder(), getHostPortHolder());
                }
            }
        } catch (Exception e) {
            LOG.error("DNS server {} at port {} failed resolving host addresses for {}. Leaving Hazelcast TCP/IP network configuration unchanged.", getHostAddressHolder(), getHostPortHolder(), getDomainNamesHolder(), e);
        }
    }

    private Object getHostAddressHolder() {
        // Does not need to be thread-safe
        Object hostAddressHolder = this.hostAddressHolder;
        if (hostAddressHolder == null) {
            HazelcastDnsResolver dnsResolver = this.dnsResolver;
            hostAddressHolder = new Object() {

                @Override
                public String toString() {
                    return dnsResolver.getAddress().getAddress().getHostAddress().toString();
                }
            };
            this.hostAddressHolder = hostAddressHolder;
        }
        return hostAddressHolder;
    }

    private Object getHostPortHolder() {
        // Does not need to be thread-safe
        Object hostPortHolder = this.hostPortHolder;
        if (hostPortHolder == null) {
            HazelcastDnsResolver dnsResolver = this.dnsResolver;
            hostPortHolder = new Object() {

                @Override
                public String toString() {
                    return Integer.toString(dnsResolver.getAddress().getPort());
                }
            };
            this.hostPortHolder = hostPortHolder;
        }
        return hostPortHolder;
    }

    private Object getDomainNamesHolder() {
        // Does not need to be thread-safe
        Object domainNamesHolder = this.domainNamesHolder;
        if (domainNamesHolder == null) {
            Collection<String> domainNames = this.domainNames;
            if (domainNames.size() > 1) {
                domainNamesHolder = new Object() {

                    @Override
                    public String toString() {
                        Iterator<String> it = domainNames.iterator();
                        StringBuilder sb = new StringBuilder("domain names \"").append(it.next()); // first
                        while (it.hasNext()) { // others
                            sb.append(", ").append(it.next());
                        }
                        sb.append('"');
                        return sb.toString();
                    }
                };
            } else {
                domainNamesHolder = new Object() {

                    @Override
                    public String toString() {
                        return new StringBuilder("domain name \"").append(domainNames.iterator().next()).append('"').toString();
                    }
                };
            }
            this.domainNamesHolder = domainNamesHolder;
        }
        return domainNamesHolder;
    }

    private static boolean isNotEqual(Set<String> currentHostAddresses, Set<String> previousHostAddresses) {
        return false == isEqual(currentHostAddresses, previousHostAddresses);
    }

    private static boolean isEqual(Set<String> currentHostAddresses, Set<String> previousHostAddresses) {
        if (currentHostAddresses.size() != previousHostAddresses.size()) {
            return false;
        }
        try {
            return currentHostAddresses.containsAll(previousHostAddresses);
        } catch (ClassCastException | NullPointerException e) {
            LOG.trace("", e);
            return false;
        }
    }

}
