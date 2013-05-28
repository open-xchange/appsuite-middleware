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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.oauth.msn;

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
 * {@link MockOAuthService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class MockOAuthService implements OAuthService {

	private final OAuthServiceMetaData serviceMetadata;

	public MockOAuthService() {
		this.serviceMetadata = null;
	}

	public MockOAuthService(OAuthServiceMetaData serviceMetadata) {
		this.serviceMetadata = serviceMetadata;
	}


    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#createAccount(java.lang.String, java.util.Map, int, int)
     */
    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#createAccount(java.lang.String, com.openexchange.oauth.OAuthInteractionType, java.util.Map, int, int)
     */
    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#deleteAccount(int, int, int)
     */
    @Override
    public void deleteAccount(final int accountId, final int user, final int contextId) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#getAccount(int, java.lang.String, int, int)
     */
    @Override
    public OAuthAccount getAccount(final int accountId, final Session session, final int user, final int contextId) throws OXException {
        return new OAuthAccount() {

            @Override
            public String getDisplayName() {
                // Nothing to do
                return null;
            }

            @Override
            public int getId() {
                // Nothing to do
                return 0;
            }

            @Override
            public OAuthServiceMetaData getMetaData() {
                return serviceMetadata;
            }

            @Override
            public String getSecret() {

                return "{\"callback\":\"http://www.open-xchange.com/ajax/oauth/accounts?action=create&respondWithHTML=true&session=740241c6c23b43e69e6426b2fb7f0dd5&displayName=Outlook&serviceId=com.openexchange.oauth.msn&uuid=74296db7-888c-4783-a4fd-9095bc7d0ede\"}";
            }

            @Override
            public String getToken() {
                final String wrap_refresh_token = "ClyCHj05JG8QKLMzxG2pHYkZBswmH5Zpa!JqRSCRi*4NW6rkSPWkDRQowDh55THcaL4CyMrDGQBkPMmdR0o5zKzYKpV0MSSkZCPjFssBWEP5hDZL5t2OrroA*91CJtF6N2Bl2!Rz9*eXz*OqrnZj0ugj4f9OCLNm!QDHD84T18*Cw6*2MSmWwd6XQRKAPzwblGah0N*ADmLHVdvjZuudyupTCOZZ2hY8wT3U1sznF7F*uyvEZIu6dMrAzpTRWQ05z3s66RXoWSbmL0Rj1SH5Cpq6xA0JMji1z46ag5x4tYVzXFybpqiETOZIZ*hIomHmGjq*s5!s0dzI1qefpQklutBOSOe8GEbQ!PgfWiWRSqTRW9lSaIrTRaHB6XJdNZDTRMq*PRW!aIbpqDAyQAq9uMHHYGlagLnyFI9b4YUSPfpVnv5T5ZO0Jym86g6EadWULoYuW5exIqiDfNixZTuG927pC3bpiR*RfJPhm6PdsrlgSSp5v6ABiesTdEpPgWJwww8dIhAkVM3O44aHzf6BAncqICO5z*MnZDTGkMTpJghHJz9j1M4sdjqF5b6z3RX5yg$$";
                return wrap_refresh_token;
            }

			@Override
			public API getAPI() {
				// Nothing to do
				return null;
			}

        };
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#getAccounts(java.lang.String, int, int)
     */
    @Override
    public List<OAuthAccount> getAccounts(final Session session, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#getAccounts(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<OAuthAccount> getAccounts(final String serviceMetaData, final Session session, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#getMetaDataRegistry()
     */
    @Override
    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#initOAuth(java.lang.String, java.lang.String)
     */
    @Override
    public OAuthInteraction initOAuth(final String serviceMetaData, final String callbackUrl, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#updateAccount(int, java.util.Map, int, int)
     */
    @Override
    public void updateAccount(final int accountId, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.OAuthService#updateAccount(int, java.lang.String, com.openexchange.oauth.OAuthInteractionType, java.util.Map, int, int)
     */
    @Override
    public OAuthAccount updateAccount(final int accountId, final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        // Nothing to do
        return null;
    }

	@Override
	public OAuthAccount getDefaultAccount(API api, Session session) {
		// Nothing to do
		return null;
	}

}
