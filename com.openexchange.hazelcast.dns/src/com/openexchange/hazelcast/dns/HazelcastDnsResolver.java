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

package com.openexchange.hazelcast.dns;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link HazelcastDnsResolver} - Resolves given domain names to a superset of host addresses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface HazelcastDnsResolver {

    /**
     * Gets the destination address associated with this DNS resolver.
     * <p>
     * Resolve requests will be sent to this address.
     *
     * @return The destination address associated with this resolver
     */
    InetSocketAddress getAddress();

    /**
     * Resolves specified domain names to a (super-)set of host addresses.
     *
     * @param domainNames The domain names
     * @return The (superset of) resolved host addresses or an empty list if DNS failed to resolve domain names
     * @throws OXException If domain names cannot be resolved
     */
    default List<String> resolveByName(String... domainNames) throws OXException {
        return domainNames == null || domainNames.length == 0 ? Collections.emptyList() : resolveByName(Arrays.asList(domainNames));
    }

    /**
     * Resolves specified domain names to a (super-)set of host addresses.
     *
     * @param domainNames The domain names
     * @return The (superset of) resolved host addresses or an empty list if DNS failed to resolve domain names
     * @throws OXException If domain names cannot be resolved
     */
    List<String> resolveByName(Collection<String> domainNames) throws OXException;

}
