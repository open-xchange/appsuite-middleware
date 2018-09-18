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
package com.openexchange.oidc.state.impl;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
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
            hazelcast.getMap(HAZELCAST_AUTHREQUEST_INFO_MAP).put(authenticationRequestInfo.getState(), new PortableAuthenticationRequest(authenticationRequestInfo), ttl , timeUnit);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(HAZELCAST_AUTHREQUEST_INFO_MAP, e);
        }
    }

    @Override
    public AuthenticationRequestInfo getAndRemoveAuthenticationInfo(String state) throws OXException {
        LOG.trace("getAndRemoveAuthenticationInfo(state: {})", state);
        PortableAuthenticationRequest portable = null;
        try {
            portable = (PortableAuthenticationRequest) hazelcast.getMap(HAZELCAST_AUTHREQUEST_INFO_MAP).remove(state);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(HAZELCAST_AUTHREQUEST_INFO_MAP, e);
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
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(HAZELCAST_LOGOUT_REQUEST_INFO_MAP, e);
        }
    }

    @Override
    public LogoutRequestInfo getAndRemoveLogoutRequestInfo(String state) throws OXException {
        LOG.trace("getAndRemoveLogoutRequestInfo(state: {})", state);
        PortableLogoutRequest portableLogoutRequest = null;
        try {
            portableLogoutRequest = (PortableLogoutRequest) hazelcast.getMap(HAZELCAST_LOGOUT_REQUEST_INFO_MAP).remove(state);
        } catch (RuntimeException e) {
            throw OIDCExceptionCode.HAZELCAST_EXCEPTION.create(HAZELCAST_LOGOUT_REQUEST_INFO_MAP, e);
        }
        if (null == portableLogoutRequest) {
            return null;
        }
        return portableLogoutRequest.getDelegate();
    }

}
