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

package com.openexchange.oauth.provider.impl.authcode;

import java.util.concurrent.atomic.AtomicBoolean;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
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

    /**
     * Handles the specified exception
     * 
     * @param e The exception to handle
     */
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
