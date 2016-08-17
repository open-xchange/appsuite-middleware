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

package com.openexchange.saml.impl.hz;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
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
