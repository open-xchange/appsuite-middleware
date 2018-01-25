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

package com.openexchange.oidc.state;

import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * Manager of all client states, that try to login or logout with OpenID features.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface StateManagement {

    /**
     * Adds an AuthenticationRequestInfo object to be managed.
     *
     * @param authenticationRequestInfo - The {@link AuthenticationRequestInfo} to be managed by hazelcast
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     */
    void addAuthenticationRequest(AuthenticationRequestInfo authenticationRequestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Load the {@link AuthenticationRequestInfo} which is identified by the state.
     *
     * @param state The state to identify the {@link AuthenticationRequestInfo}
     * @return The {@link AuthenticationRequestInfo}
     */
    AuthenticationRequestInfo getAndRemoveAuthenticationInfo(String state) throws OXException;

    /**
     * Add the given {@link LogoutRequestInfo} to a hazelcast IMap, which is managed
     * by hazelcast and enables every node in the cluster to handle Logout requests.
     *
     * @param logoutRequestInfo - The {@link LogoutRequestInfo} to managed by hazelcast
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     */
    void addLogoutRequest(LogoutRequestInfo logoutRequestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Load the stored {@link LogoutRequestInfo} from hazelcasts {@link IMap} by
     * passing the state, which identifies the {@link LogoutRequestInfo}.
     *
     * @param state The identifier of the {@link LogoutRequestInfo}
     * @return The {@link LogoutRequestInfo}
     */
    LogoutRequestInfo getAndRemoveLogoutRequestInfo(String state) throws OXException;

}
