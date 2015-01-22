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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.authcode;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang.RandomStringUtils;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.provider.AuthorizationCodeService;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.DefaultOAuthToken;
import com.openexchange.oauth.provider.OAuthToken;
import com.openexchange.oauth.provider.Scope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HzAuthorizationCodeService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class HzAuthorizationCodeService extends AbstractAuthorizationCodeService {

    /**
     * The name for the associated Hazelcast map.
     */
    public static final String HZ_MAP_NAME = "authcode";

    // -------------------------------------------------------------------------------------------------------

    private final String mapName;
    private final AtomicBoolean notActive;
    private final AuthorizationCodeService fallback;

    /**
     * Initializes a new {@link HzAuthorizationCodeService}.
     */
    public HzAuthorizationCodeService(String mapName, AuthorizationCodeService fallback, ServiceLookup services) {
        super(services);
        this.mapName = mapName;
        this.fallback = fallback;
        notActive = new AtomicBoolean();
    }

    private IMap<String, String> map() throws OXException {
        try {
            HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
            if (null == hzInstance) {
                throw ServiceExceptionCode.absentService(HazelcastInstance.class);
            }
            return hzInstance.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (HazelcastException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        notActive.set(true);
    }

    @Override
    public String generateAuthorizationCodeFor(String clientId, Scope scope) throws OXException {
        if (notActive.get()) {
            return fallback.generateAuthorizationCodeFor(clientId, scope);
        }

        // Get Hazelcast map
        IMap<String, String> map = map();

        // Continue...
        String authCode = RandomStringUtils.randomAlphabetic(64);
        long now = System.nanoTime();
        map.put(authCode, generateValue(now, clientId, scope));
        return authCode;
    }

    @Override
    public OAuthToken redeemAuthCode(Client client, String authCode) throws OXException {
        if (notActive.get()) {
            return fallback.redeemAuthCode(client, authCode);
        }

        // Get Hazelcast map
        IMap<String, String> map = map();
        long now = System.nanoTime();
        String value = map.remove(authCode);

        if (null == value) {
            return null;
        }

        // Check if valid
        if (false == validValue(value, now, client.getId())) {
            return null;
        }

        // Valid
        DefaultOAuthToken token = new DefaultOAuthToken();
        token.setScope(parseScopeFromValue(value));
        // TODO: Set its attributes
        return token;
    }

}
