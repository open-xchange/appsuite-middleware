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

package com.openexchange.saml.impl.hz;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;
import com.openexchange.saml.state.StateManagement;


/**
 * Hazelcast-based implementation of {@link StateManagement}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class HzStateManagement implements StateManagement {

    private final HazelcastInstance hazelcast;

    /**
     * Initializes a new {@link HzStateManagement}.
     * @param hazelcast
     */
    public HzStateManagement(HazelcastInstance hazelcast) {
        super();
        this.hazelcast = hazelcast;
    }

    @Override
    public String addAuthnRequestInfo(AuthnRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException {
        String id = generateID();
        getAuthnRequestInfoMap().put(id, new PortableAuthnRequestInfo(requestInfo), ttl, timeUnit);
        return id;
    }

    @Override
    public AuthnRequestInfo removeAuthnRequestInfo(String id) throws OXException {
        PortableAuthnRequestInfo portable = getAuthnRequestInfoMap().remove(id);
        if (portable == null) {
            return null;
        }

        return portable.getDelegate();
    }

    @Override
    public void addAuthnResponseID(String responseID, long ttl, TimeUnit timeUnit) throws OXException {
        /*
         * We use a map here because of several limitations of ISet
         */
        getAuthnResponseIDMap().put(responseID, responseID, ttl, timeUnit);
    }

    @Override
    public boolean hasAuthnResponseID(String responseID) throws OXException {
        return getAuthnResponseIDMap().containsKey(responseID);
    }

    @Override
    public String addLogoutRequestInfo(LogoutRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException {
        String id = generateID();
        getLogoutRequestInfoMap().put(id, new PortableLogoutRequestInfo(requestInfo), ttl, timeUnit);
        return id;
    }

    @Override
    public LogoutRequestInfo removeLogoutRequestInfo(String id) throws OXException {
        PortableLogoutRequestInfo portable = getLogoutRequestInfoMap().remove(id);
        if (portable == null) {
            return null;
        }

        return portable.getDelegate();
    }

    private IMap<String, PortableAuthnRequestInfo> getAuthnRequestInfoMap() {
        IMap<String, PortableAuthnRequestInfo> hzMap = hazelcast.getMap("samlAuthnRequestInfos-1");
        return hzMap;
    }

    private IMap<String, PortableLogoutRequestInfo> getLogoutRequestInfoMap() {
        IMap<String, PortableLogoutRequestInfo> hzMap = hazelcast.getMap("samlLogoutRequestInfos-1");
        return hzMap;
    }

    private IMap<String, String> getAuthnResponseIDMap() {
        IMap<String, String> hzMap = hazelcast.getMap("samlAuthnResponseIDs-1");
        return hzMap;
    }

    private static String generateID() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

}
