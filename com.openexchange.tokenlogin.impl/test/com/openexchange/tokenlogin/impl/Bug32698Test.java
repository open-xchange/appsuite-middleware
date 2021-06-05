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

package com.openexchange.tokenlogin.impl;

import static com.openexchange.java.Autoboxing.I;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.hazelcast.map.IMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tokenlogin.DefaultTokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginSecret;

/**
 * Unit tests for {@link Bug32698Test}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since 7.6
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, IMap.class, TokenLoginServiceImpl.class })
public class Bug32698Test {

    /**
     * Instance to test
     */
    private TokenLoginServiceImpl tokenLoginServiceImpl;

    /**
     * Instance to test
     */
    private TokenLoginServiceImpl tokenLoginServiceImpl2;

    /**
     * Idle time required for the constructor
     */
    private final int maxIdleTime = 1000;

    /**
     * Mock of the {@link ConfigurationService}
     */
    @Mock
    private ConfigurationService configService;

    /**
     * Mock of the {@link Session}
     */
    @Mock
    private Session session;

    /**
     * Mock of the {@link ContextService}
     */
    @Mock
    private ContextService contextService;

    /**
     * Mock of the {@link SessiondService}
     */
    @Mock
    private SessiondService sessiondService;

    /**
     * Mock of the {@link ObfuscatorService}
     */
    @Mock
    private ObfuscatorService obfuscatorService;

    /**
     * Mock of the {@link IMap}
     */
    @Mock
    private IMap<String, String> tokenIMap;

    /**
     * Mock of the {@link IMap}
     */
    @Mock
    private IMap<String, String> SessionIMap;

    private final ConcurrentMap<String, String> myMap1 = new ConcurrentHashMap<String, String>();

    private final ConcurrentMap<String, String> myMap2 = new ConcurrentHashMap<String, String>();

    @SuppressWarnings("synthetic-access")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);

        // BEHAVIOUR
        PowerMockito.when(I(session.getContextId())).thenReturn(I(424242669));
        PowerMockito.when(session.getSessionID()).thenReturn("8a07c5a2e4974a75ae70bd9a36198f03");

        PowerMockito.when(tokenIMap, method(IMap.class, "put", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap1.put((String) args[0], (String) args[1]);
            }
        });
        PowerMockito.when(tokenIMap, method(IMap.class, "get", String.class)).withArguments(anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap1.get(args[0]);
            }
        });
        PowerMockito.when(tokenIMap, method(IMap.class, "remove", String.class)).withArguments(anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap1.remove(args[0]);
            }
        });
        PowerMockito.when(tokenIMap, method(IMap.class, "putIfAbsent", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap1.putIfAbsent((String) args[0], (String) args[1]);
            }
        });

        PowerMockito.when(SessionIMap, method(IMap.class, "put", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap2.put((String) args[0], (String) args[1]);
            }
        });
        PowerMockito.when(SessionIMap, method(IMap.class, "get", String.class)).withArguments(anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap2.get(args[0]);
            }
        });
        PowerMockito.when(SessionIMap, method(IMap.class, "remove", String.class)).withArguments(anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap2.remove(args[0]);
            }
        });
        PowerMockito.when(SessionIMap, method(IMap.class, "putIfAbsent", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap2.putIfAbsent((String) args[0], (String) args[1]);
            }
        });
        PowerMockito.when(SessionIMap, method(IMap.class, "putIfAbsent", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return myMap2.putIfAbsent((String) args[0], (String) args[1]);
            }
        });
        PowerMockito.when(obfuscatorService, method(ObfuscatorService.class, "obfuscate", String.class)).withArguments(anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return Base64.getEncoder().encodeToString(((String) args[0]).getBytes());
            }
        });
        PowerMockito.when(obfuscatorService, method(ObfuscatorService.class, "unobfuscate", String.class)).withArguments(anyString()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return new String(Base64.getDecoder().decode((String) args[0]));
            }
        });
    }

    @Test
    public void testTokenIsDifferentAfterUsingItOnOtherOx() throws Exception {
        PowerMockito.when(this.sessiondService.getSession(ArgumentMatchers.anyString())).thenReturn(this.session);
        PowerMockito.when(this.sessiondService.peekSession(ArgumentMatchers.anyString())).thenReturn(this.session);
        PowerMockito.when(this.sessiondService.addSession(ArgumentMatchers.any(AddSessionParameter.class))).thenReturn(this.session);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);
        PowerMockito.when(Services.getService(ObfuscatorService.class)).thenReturn(this.obfuscatorService);

        configureTokenLoginServices();
        this.tokenLoginServiceImpl.changeBackingMapToHz();
        this.tokenLoginServiceImpl2.changeBackingMapToHz();

        String localToken = this.tokenLoginServiceImpl.acquireToken(this.session);
        Session returnedSession = this.tokenLoginServiceImpl2.redeemToken(localToken, "appSecret", "optClientId", "optAuthId", "optHash", "optClientIp", "optUserAgent");
        Assert.assertNotNull(returnedSession);
        String localToken2 = this.tokenLoginServiceImpl.acquireToken(this.session);
        Assert.assertFalse(localToken.equals(localToken2));

        Mockito.verify(sessiondService, Mockito.times(1)).addSession(ArgumentMatchers.any(AddSessionParameter.class));
    }

    @Test(expected = OXException.class)
    public void testTokenIsInvalidated() throws Exception {
        PowerMockito.when(this.sessiondService.getSession(ArgumentMatchers.anyString())).thenReturn(this.session);
        PowerMockito.when(this.sessiondService.addSession(ArgumentMatchers.any(AddSessionParameter.class))).thenReturn(this.session);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        configureTokenLoginServices();
        this.tokenLoginServiceImpl.changeBackingMapToHz();
        this.tokenLoginServiceImpl2.changeBackingMapToHz();

        String localToken = this.tokenLoginServiceImpl.acquireToken(this.session);
        Session returnedSession = this.tokenLoginServiceImpl2.redeemToken(localToken, "appSecret", "optClientId", "optAuthId", "optHash", "optClientIp", "optUserAgent");
        Assert.assertNotNull(returnedSession);
        Session returnedSession2 = this.tokenLoginServiceImpl.redeemToken(localToken, "appSecret", "optClientId", "optAuthId", "optHash", "optClientIp", "optUserAgent");

        Assert.assertFalse(returnedSession.equals(returnedSession2));

        Mockito.verify(sessiondService, Mockito.times(1)).addSession(ArgumentMatchers.any(AddSessionParameter.class));
    }

    /**
     * @throws OXException
     * @throws Exception
     */
    @SuppressWarnings("synthetic-access")
    private void configureTokenLoginServices() throws OXException, Exception {

        this.tokenLoginServiceImpl = PowerMockito.spy(new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {

            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        });
        this.tokenLoginServiceImpl2 = PowerMockito.spy(new TokenLoginServiceImpl(this.maxIdleTime, this.configService) {

            @Override
            public TokenLoginSecret getTokenLoginSecret(String secret) {
                return new DefaultTokenLoginSecret();
            }
        });

        this.tokenLoginServiceImpl.setBackingHzMapName("sessionId2tokenMapName");
        this.tokenLoginServiceImpl2.setBackingHzMapName("sessionId2tokenMapName");

        PowerMockito.when(tokenLoginServiceImpl, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(org.mockito.ArgumentMatchers.eq("token2sessionIdMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return tokenIMap;
            }
        });
        PowerMockito.when(tokenLoginServiceImpl, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(org.mockito.ArgumentMatchers.eq("sessionId2tokenMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return SessionIMap;
            }
        });
        PowerMockito.when(tokenLoginServiceImpl2, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(org.mockito.ArgumentMatchers.eq("token2sessionIdMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return tokenIMap;
            }
        });
        PowerMockito.when(tokenLoginServiceImpl2, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(org.mockito.ArgumentMatchers.eq("sessionId2tokenMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return SessionIMap;
            }
        });
    }

}
