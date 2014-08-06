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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.google;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.session.Session;



/**
 * {@link MockGoogleService}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class MockGoogleService implements OAuthService {

    private final OAuthServiceMetaData serviceMetadata;

    public MockGoogleService() {
        this.serviceMetadata = null;
    }

    public MockGoogleService(OAuthServiceMetaData serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void deleteAccount(final int accountId, final int user, final int contextId) throws OXException {
        // Nothing to do

    }

    @Override
    public OAuthAccount getAccount(final int accountId, final Session session, final int user, final int contextId) throws OXException {
        return null;
    }

    @Override
    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OAuthAccount> getAccounts(Session session, int user, int contextId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OAuthAccount> getAccounts(String serviceMetaData, Session session, int user, int contextId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OAuthInteraction initOAuth(String serviceMetaData, String callbackUrl, String currentHost, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateAccount(int accountId, Map<String, Object> arguments, int user, int contextId) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public OAuthAccount updateAccount(int accountId, String serviceMetaData, OAuthInteractionType type, Map<String, Object> arguments, int user, int contextId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OAuthAccount getDefaultAccount(API api, Session session) throws OXException {
        return new OAuthAccount() {

            @Override
            public String getDisplayName() {
                return "";
            }

            @Override
            public int getId() {
                return 1;
            }

            @Override
            public OAuthServiceMetaData getMetaData() {
                return serviceMetadata;
            }

            @Override
            public String getSecret() {
                return "";
            }

            @Override
            public String getToken() {
                return "";
            }

            @Override
            public API getAPI() {
                return API.GOOGLE;
            }

        };
    }
}
