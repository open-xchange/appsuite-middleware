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
package com.openexchange.oidc.state.impl;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.hz.PortableAuthenticationRequest;
import com.openexchange.oidc.hz.PortableLogoutRequest;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.LogoutRequestInfo;
import com.openexchange.oidc.state.StateManagement;

/**
 * Contains and manages all current client states in {@link Hazelcast}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class CoreStateManagement implements StateManagement {

    private static final Logger LOG = LoggerFactory.getLogger(CoreStateManagement.class);

    private static final String HAZELCAST_AUTHREQUEST_INFO_MAP = "oidcAuthInfos";
    private static final String HAZELCAST_LOGOUT_REQUEST_INFO_MAP = "oidcLogoutInfos";

    private final HazelcastInstance hazelcast;

    public CoreStateManagement(HazelcastInstance hazelcast) {
        super();
        this.hazelcast = hazelcast;
    }

    @Override
    public void addAuthenticationRequest(AuthenticationRequestInfo authenticationRequestInfo, long ttl, TimeUnit timeUnit) throws OXException {
        LOG.trace("addAuthenticationRequest(AuthenticationRequestInfo: {})", authenticationRequestInfo.getState());
        try {
            hazelcast.getMap(HAZELCAST_AUTHREQUEST_INFO_MAP).set(authenticationRequestInfo.getState(), new PortableAuthenticationRequest(authenticationRequestInfo), ttl , timeUnit);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(e, HAZELCAST_AUTHREQUEST_INFO_MAP);
        }
    }

    @Override
    public AuthenticationRequestInfo getAndRemoveAuthenticationInfo(String state) throws OXException {
        LOG.trace("getAndRemoveAuthenticationInfo(state: {})", state);
        if (Strings.isEmpty(state)) {
            return null;
        }
        PortableAuthenticationRequest portable = null;
        try {
            portable = (PortableAuthenticationRequest) hazelcast.getMap(HAZELCAST_AUTHREQUEST_INFO_MAP).remove(state);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(e, HAZELCAST_AUTHREQUEST_INFO_MAP);
        }

        if (null == portable) {
            return null;
        }
        return portable.getDelegate();
    }

    @Override
    public void addLogoutRequest(LogoutRequestInfo logoutRequestInfo, long ttl, TimeUnit timeUnit) throws OXException {
        LOG.trace("addLogoutRequest({})", logoutRequestInfo.getState());
        try {
            hazelcast.getMap(HAZELCAST_LOGOUT_REQUEST_INFO_MAP).put(logoutRequestInfo.getState(), new PortableLogoutRequest(logoutRequestInfo), ttl , timeUnit);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(e, HAZELCAST_LOGOUT_REQUEST_INFO_MAP);
        }
    }

    @Override
    public LogoutRequestInfo getAndRemoveLogoutRequestInfo(String state) throws OXException {
        LOG.trace("getAndRemoveLogoutRequestInfo(state: {})", state);
        PortableLogoutRequest portableLogoutRequest = null;
        try {
            portableLogoutRequest = (PortableLogoutRequest) hazelcast.getMap(HAZELCAST_LOGOUT_REQUEST_INFO_MAP).remove(state);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(e, HAZELCAST_LOGOUT_REQUEST_INFO_MAP);
        }
        if (null == portableLogoutRequest) {
            return null;
        }
        return portableLogoutRequest.getDelegate();
    }

}
