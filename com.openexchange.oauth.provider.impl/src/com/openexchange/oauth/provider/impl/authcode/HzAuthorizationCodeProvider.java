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

package com.openexchange.oauth.provider.impl.authcode;

import java.util.concurrent.atomic.AtomicBoolean;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.authcode.portable.PortableAuthCodeInfo;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.server.ServiceExceptionCode;


/**
 * {@link HzAuthorizationCodeProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class HzAuthorizationCodeProvider extends AbstractAuthorizationCodeProvider {

    /**
     * The name for the associated Hazelcast map.
     */
    public static final String HZ_MAP_NAME = "authcode";

    // -------------------------------------------------------------------------------------------------------

    private final String mapName;
    private final AtomicBoolean notActive;
    private final HazelcastInstance hazelcast;

    /**
     * Initializes a new {@link HzAuthorizationCodeProvider}.
     */
    public HzAuthorizationCodeProvider(String mapName, HazelcastInstance hazelcast) {
        super();
        this.mapName = mapName;
        this.hazelcast = hazelcast;
        notActive = new AtomicBoolean();
    }

    private IMap<String, PortableAuthCodeInfo> map() throws OXException {
        try {
            return hazelcast.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            return null;
        } catch (HazelcastException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        notActive.set(true);
    }

    @Override
    protected void put(AuthCodeInfo authCodeInfo) throws OXException {
        if (notActive.get()) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        // Get Hazelcast map
        IMap<String, PortableAuthCodeInfo> map = map();
        if (null == map) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        // Continue...
        map.put(authCodeInfo.getAuthCode(), new PortableAuthCodeInfo(authCodeInfo.getClientId(), authCodeInfo.getRedirectURI(), authCodeInfo.getScope(), authCodeInfo.getUserId(), authCodeInfo.getContextId(), authCodeInfo.getTimestamp()));
    }

    @Override
    public AuthCodeInfo remove(String authCode) throws OXException {
        if (notActive.get()) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        // Get Hazelcast map
        IMap<String, PortableAuthCodeInfo> map = map();
        if (null == map) {
            return null;
        }

        PortableAuthCodeInfo value = map.remove(authCode);
        if (null == value) {
            return null;
        }

        // Check if valid
        int contextId = value.getContextId();
        int userId = value.getUserId();
        AuthCodeInfo authCodeInfo = new AuthCodeInfo(authCode, value.getClientId(), value.getRedirectURI(), Scope.parseScope(value.getScope()), userId, contextId, value.getNanos());
        return authCodeInfo;
    }

}
