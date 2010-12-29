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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.oauth.internal;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.DBPoolingException;
import com.openexchange.id.SimIDGenerator;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.SimOAuthServiceMetaDataRegistry;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * The {@link OAuthServiceImplDBTest} tests the DB interaction of the OAuthServiceImpl class, with the OAuth interactions 
 * taken out through subclassing. The OAuth interactions are tested elsewhere.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthServiceImplDBTest extends SQLTestCase {
    
    private OAuthServiceImpl oauth;
    
    private Map<String, Object> lastArguments;
    private OAuthInteractionType lastType;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        SimOAuthServiceMetaDataRegistry registry = new SimOAuthServiceMetaDataRegistry();
        
        registry.addService(new OAuthServiceMetaData() {

            public String getAPIKey() {
                return "apiKey";
            }

            public String getAPISecret() {
                return "apiSecret";
            }

            public String getAuthorizationURL(OAuthToken token) {
                return "http://www.myService.invalid/initiateHandshake?token="+token.getToken()+"&secret="+token.getSecret();
            }

            public String getDisplayName() {
                return "The cool oauthService";
            }

            public String getId() {
                return "com.openexchange.test";
            }
            
        });
        
        oauth = new OAuthServiceImpl(getDBProvider(), new SimIDGenerator(), registry) {
            @Override
            protected void obtainToken(OAuthInteractionType type, Map<String, Object> arguments, DefaultOAuthAccount account) {
                lastArguments = arguments;
                lastType = type;
                account.setToken("myAccessToken");
                account.setSecret("myAccessSecret");
            }
        };
    }
    
    // Success Cases
    
    public void testCreateAccount() throws OAuthException, DBPoolingException, SQLException {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(OAuthConstants.ARGUMENT_DISPLAY_NAME, "Test OAuthAccount");
        arguments.put(OAuthConstants.ARGUMENT_PIN, "pin");
        arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new OAuthToken() {

            public String getSecret() {
                return "requestSecret";
            }

            public String getToken() {
                return "requestToken";
            }});
        
        
        OAuthAccount authAccount = oauth.createAccount("com.openexchange.test", OAuthInteractionType.OUT_OF_BAND, arguments, 23, 1);
        
        assertNotNull(authAccount);
        assertEquals("Test OAuthAccount", authAccount.getDisplayName());
        assertTrue(authAccount.getId() != 0);
        assertNotNull(authAccount.getMetaData());
        assertEquals("com.openexchange.test", authAccount.getMetaData().getId());
        assertEquals("myAccessToken", authAccount.getToken());
        assertEquals("myAccessSecret", authAccount.getSecret());
        
        assertEntry("oauthAccount", "id", authAccount.getId(), "displayName", authAccount.getDisplayName(), "accessToken", authAccount.getToken(), "accessSecret", authAccount.getSecret(), "serviceId", authAccount.getMetaData().getId());
        
    }
    
    public void testDefaultDisplayName() {
        
    }
    
    public void testGetAccount() {
        
    }
    
    public void testGetAccountsForUser() {
        
    }
    
    public void testGetAccountsForUserAndService() {
        
    }

    public void testUpdateAccount() {
        
    }
    
    public void testDeleteAccount() {
        
    }
    
    // Error Cases

    public void testUnknownAccountMetadataOnCreate() {
        
    }
}
