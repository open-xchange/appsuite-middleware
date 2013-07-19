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

package com.openexchange.tokenlogin.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.test.mock.MockFactory;
import com.openexchange.test.mock.test.AbstractMockTest;
import com.openexchange.test.mock.util.MockDefaultValues;
import com.openexchange.test.mock.util.MockUtils;
import com.openexchange.tokenlogin.DefaultTokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginSecret;


/**
 * Unit tests for {@link TokenLoginServiceImpl}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
@PrepareForTest({ Services.class })
public class TokenLoginServiceImplTest extends AbstractMockTest {

    /**
     * Instance to test
     */
    private TokenLoginServiceImpl tokenLoginServiceImpl = null;

    /**
     * Idle time required for the constructor
     */
    private int maxIdleTime = 1000;

    /**
     * Fake token for the tests
     */
    private String token = "067e61623b6f4ae2a1712470b63dff00";

    /**
     * Mock of the {@link ConfigurationService}
     */
    private ConfigurationService configService;

    /**
     * Mock of the {@link Session}
     */
    private Session session;

    /**
     * Mock of the {@link ContextService}
     */
    private ContextService contextService;

    /**
     * Mock of the {@link SessiondService}
     */
    private SessiondService sessiondService;

    /**
     * A temporary folder that could be used by each mock.
     */
    @Rule
    protected TemporaryFolder folder = new TemporaryFolder();

    /**
     * Secret token to check
     */
    private static String SECRET_TOKEN = "1234-56789-98765-4321";

    /**
     * Content of the secret file
     */
    private String secretFileContent = "#\n# Listing of known Web Application secrets followed by an optional semicolon-separated parameter list\n# e.g. 1254654698621354; accessPasword=true\n# Dummy entry\n" + SECRET_TOKEN + "; accessPassword=true\n";

    @Override
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Services.class);

        this.configService = MockFactory.getMock(ConfigurationService.class);
        this.session = MockFactory.getMock(Session.class);
        this.contextService = MockFactory.getMock(ContextService.class);
        this.sessiondService = MockFactory.getMock(SessiondService.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_configServiceNull_ThrowException() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, null);
    }

    @Test
    public void testConstructor_parameterProperlyFilled_ReturnServiceImpl() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        Assert.assertNotNull(this.tokenLoginServiceImpl);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcquireToken_SessionParameterNull_ThrowException() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        this.tokenLoginServiceImpl.acquireToken(null);
    }

    @Test
    public void testAcquireToken_tokenNull_ReturnNewToken() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        String localToken = this.tokenLoginServiceImpl.acquireToken(this.session);

        Mockito.verify(this.session, Mockito.times(1)).getSessionID();
        Assert.assertNotNull(localToken);
    }

    @Test
    public void testAcquireToken_FindToken_ReturnToken() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        MockUtils.injectValueIntoPrivateField(this.tokenLoginServiceImpl, "sessionId2token", createSessionId2Token());

        String localToken = this.tokenLoginServiceImpl.acquireToken(this.session);

        Mockito.verify(this.session, Mockito.times(1)).getSessionID();
        Assert.assertNotNull(localToken);
    }

    @Test(expected = OXException.class)
    public void testRedeemToken_secretIsEmptyOrNotInMap_ThrowsOXException() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {

            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return null;
            }
        };

        this.tokenLoginServiceImpl.redeemToken(this.token, "appSecret", "optClientId", "optAuthId", "optHash");
    }

    @Test(expected = OXException.class)
    public void testRedeemToken_SessiondServiceNull_ThrowsOXException() throws OXException {
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(null);

        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(maxIdleTime, configService) {

            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        };

        this.tokenLoginServiceImpl.redeemToken(this.token, "appSecret", "optClientId", "optAuthId", "optHash");
    }

    @Test(expected = OXException.class)
    public void testRedeemToken_ContextServiceNull_ThrowsOXException() throws OXException {
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(null);

        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {

            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        };

        this.tokenLoginServiceImpl.redeemToken(this.token, "appSecret", "optClientId", "optAuthId", "optHash");
    }

    @Test(expected = OXException.class)
    public void testRedeemToken_NoSessionIdAvailableAndNoSuchToken_ThrowsOXException() throws OXException {
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {

            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        };

        this.tokenLoginServiceImpl.redeemToken(this.token, "appSecret", "optClientId", "optAuthId", "optHash");
    }

    @Test(expected = OXException.class)
    public void testRedeemToken_NotAbleToCreateNewSession_ThrowsOXException() throws OXException {
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {
            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        };

        MockUtils.injectValueIntoPrivateField(this.tokenLoginServiceImpl, "token2sessionId", createToken2SessionId());

        this.tokenLoginServiceImpl.redeemToken(this.token, "appSecret", "optClientId", "optAuthId", "optHash");
    }

    @Test
    public void testRedeemToken_EverythingFine_ReturnSession() throws OXException {
        PowerMockito.when(this.sessiondService.getSession(MockDefaultValues.DEFAULT_SESSION_ID)).thenReturn(this.session);
        PowerMockito.when(this.sessiondService.addSession((AddSessionParameter) Mockito.anyObject())).thenReturn(this.session);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {
            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        };

        MockUtils.injectValueIntoPrivateField(this.tokenLoginServiceImpl, "token2sessionId", createToken2SessionId());

        Session returnedSession = this.tokenLoginServiceImpl.redeemToken(this.token, "appSecret", "optClientId", "optAuthId", "optHash");

        Assert.assertNotNull(returnedSession);
        Mockito.verify(sessiondService, Mockito.times(1)).addSession((AddSessionParameter) Mockito.anyObject());
    }

    @Test
    public void testInitSecrets_SecretFileNull_ReturnEmptyMap() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        Map<String, TokenLoginSecret> secrets = this.tokenLoginServiceImpl.initSecrets(null);

        Assert.assertNotNull(secrets);
        Assert.assertEquals(0, secrets.size());
    }

    @Test
    public void testInitSecrets_CorrectInput_ReturnCorrectSize() throws OXException, IOException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        File file = folder.newFile("tokenlogin-secrets");
        FileUtils.writeStringToFile(file, secretFileContent, true);

        Map<String, TokenLoginSecret> initSecrets = this.tokenLoginServiceImpl.initSecrets(file);

        Assert.assertEquals(1, initSecrets.size());
    }

    @Test
    public void testInitSecrets_CorrectInput_ReturnCorrectContent() throws OXException, IOException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        File file = folder.newFile("tokenlogin-secrets");
        FileUtils.writeStringToFile(file, secretFileContent, true);

        Map<String, TokenLoginSecret> initSecrets = this.tokenLoginServiceImpl.initSecrets(file);

        Assert.assertNotNull(initSecrets.get(SECRET_TOKEN));
        Assert.assertEquals(SECRET_TOKEN, initSecrets.get(SECRET_TOKEN).getSecret());
    }

    @Test
    public void testInitSecrets_CorrectInput_ReturnAccessPasswordTrue() throws OXException, IOException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        File file = folder.newFile("tokenlogin-secrets");
        FileUtils.writeStringToFile(file, secretFileContent, true);

        Map<String, TokenLoginSecret> initSecrets = this.tokenLoginServiceImpl.initSecrets(file);

        Assert.assertEquals(true, initSecrets.get(SECRET_TOKEN).getParameters().get("accessPassword"));
    }

    @Test
    public void testInitSecrets_TextInput_ReturnTextInputPasswordFalse() throws OXException, IOException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        String secret = "this should not be returned as correct";
        File file = folder.newFile("tokenlogin-secrets");
        FileUtils.writeStringToFile(file, secret, true);

        Map<String, TokenLoginSecret> initSecrets = this.tokenLoginServiceImpl.initSecrets(file);

        Assert.assertEquals(1, initSecrets.size());
        Assert.assertEquals(secret, initSecrets.get(secret).getSecret());
    }

    @Test
    public void testInitSecrets_TextInput_ReturnTextInputPasswordTrue() throws OXException, IOException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        String secret = "this should not be returned as correct";
        String password = "; accessPassword=true";
        File file = folder.newFile("tokenlogin-secrets");
        FileUtils.writeStringToFile(file, secret + password, true);

        Map<String, TokenLoginSecret> initSecrets = this.tokenLoginServiceImpl.initSecrets(file);

        Assert.assertEquals(true, initSecrets.get(secret).getParameters().get("accessPassword"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTokenFor_SessionNull_ThrowException() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);

        this.tokenLoginServiceImpl.removeTokenFor(null);
    }

    @Test
    public void testRemoveTokenFor_TokenNotAvailable_TokenStillInMap() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);
        MockUtils.injectValueIntoPrivateField(this.tokenLoginServiceImpl, "token2sessionId", createToken2SessionId());

        this.tokenLoginServiceImpl.removeTokenFor(this.session);

        Mockito.verify(this.session, Mockito.times(1)).getSessionID();
        ConcurrentLinkedHashMap<String, String> token2sessionMap = (ConcurrentLinkedHashMap<String, String>) MockUtils.getValueFromField(
            this.tokenLoginServiceImpl,
            "token2sessionId");
        Assert.assertNotNull(token2sessionMap);
        Assert.assertEquals(1, token2sessionMap.size());
    }

    @Test
    public void testRemoveTokenFor_TokenNotAvailadfdble_Return() throws OXException {
        this.tokenLoginServiceImpl = new TokenLoginServiceImpl(this.maxIdleTime, this.configService);
        MockUtils.injectValueIntoPrivateField(this.tokenLoginServiceImpl, "token2sessionId", createToken2SessionId());
        MockUtils.injectValueIntoPrivateField(this.tokenLoginServiceImpl, "sessionId2token", createSessionId2Token());

        this.tokenLoginServiceImpl.removeTokenFor(this.session);

        ConcurrentLinkedHashMap<String, String> token2sessionMap = (ConcurrentLinkedHashMap<String, String>) MockUtils.getValueFromField(
            this.tokenLoginServiceImpl,
            "token2sessionId");
        Assert.assertNotNull(token2sessionMap);
        Assert.assertEquals(0, token2sessionMap.size());
    }

    /**
     * Creates the token-session id map
     * 
     * @return {@link ConcurrentMap<String, String>} with the desired mapping
     */
    private ConcurrentMap<String, String> createToken2SessionId() {
        ConcurrentMap<String, String> token2sessionId = new ConcurrentLinkedHashMap<String, String>(
            1024,
            0.75f,
            16,
            Integer.MAX_VALUE,
            new IdleExpirationPolicy(this.maxIdleTime));
        token2sessionId.put(this.token, MockDefaultValues.DEFAULT_SESSION_ID);

        return token2sessionId;
    }

    /**
     * Creates the session id-token map
     * 
     * @return {@link ConcurrentMap<String, String>} with the desired mapping
     */
    private ConcurrentMap<String, String> createSessionId2Token() {
        ConcurrentMap<String, String> sessionId2Token = new ConcurrentLinkedHashMap<String, String>(
            1024,
            0.75f,
            16,
            Integer.MAX_VALUE,
            new IdleExpirationPolicy(this.maxIdleTime));
        sessionId2Token.put(MockDefaultValues.DEFAULT_SESSION_ID, this.token);

        return sessionId2Token;
    }
}
