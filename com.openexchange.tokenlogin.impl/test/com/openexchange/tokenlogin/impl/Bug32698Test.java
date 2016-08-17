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

package com.openexchange.tokenlogin.impl;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.hazelcast.core.IMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);

        // BEHAVIOUR
        PowerMockito.when(session.getContextId()).thenReturn(424242669);
        PowerMockito.when(session.getSessionID()).thenReturn("8a07c5a2e4974a75ae70bd9a36198f03");

        PowerMockito.when(tokenIMap, method(IMap.class, "put", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(
            new Answer<String>() {

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
        PowerMockito.when(tokenIMap, method(IMap.class, "putIfAbsent", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(
            new Answer<String>() {

                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    return myMap1.putIfAbsent((String) args[0], (String) args[1]);
                }
            });

        PowerMockito.when(SessionIMap, method(IMap.class, "put", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(
            new Answer<String>() {

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
        PowerMockito.when(SessionIMap, method(IMap.class, "putIfAbsent", String.class, String.class)).withArguments(anyString(), anyString()).thenAnswer(
            new Answer<String>() {

                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    return myMap2.putIfAbsent((String) args[0], (String) args[1]);
                }
            });
    }

    @Test
    public void testTokenIsDifferentAfterUsingItOnOtherOx() throws Exception {
        PowerMockito.when(this.sessiondService.getSession(Matchers.anyString())).thenReturn(this.session);
        PowerMockito.when(this.sessiondService.addSession((AddSessionParameter) Mockito.anyObject())).thenReturn(this.session);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        configureTokenLoginServices();
        this.tokenLoginServiceImpl.changeBackingMapToHz();
        this.tokenLoginServiceImpl2.changeBackingMapToHz();

        String localToken = this.tokenLoginServiceImpl.acquireToken(this.session);
        Session returnedSession = this.tokenLoginServiceImpl2.redeemToken(localToken, "appSecret", "optClientId", "optAuthId", "optHash", "optClientIp");
        Assert.assertNotNull(returnedSession);
        String localToken2 = this.tokenLoginServiceImpl.acquireToken(this.session);
        Assert.assertFalse(localToken.equals(localToken2));

        Mockito.verify(sessiondService, Mockito.times(1)).addSession((AddSessionParameter) Mockito.anyObject());
    }

    @Test(expected = OXException.class)
    public void testTokenIsInvalidated() throws Exception {
        PowerMockito.when(this.sessiondService.getSession(Matchers.anyString())).thenReturn(this.session);
        PowerMockito.when(this.sessiondService.addSession((AddSessionParameter) Mockito.anyObject())).thenReturn(this.session);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(this.sessiondService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        configureTokenLoginServices();
        this.tokenLoginServiceImpl.changeBackingMapToHz();
        this.tokenLoginServiceImpl2.changeBackingMapToHz();

        String localToken = this.tokenLoginServiceImpl.acquireToken(this.session);
        Session returnedSession = this.tokenLoginServiceImpl2.redeemToken(localToken, "appSecret", "optClientId", "optAuthId", "optHash", "optClientIp");
        Assert.assertNotNull(returnedSession);
        Session returnedSession2 = this.tokenLoginServiceImpl.redeemToken(localToken, "appSecret", "optClientId", "optAuthId", "optHash", "optClientIp");

        Assert.assertFalse(returnedSession.equals(returnedSession2));

        Mockito.verify(sessiondService, Mockito.times(1)).addSession((AddSessionParameter) Mockito.anyObject());
    }

    /**
     * @throws OXException
     * @throws Exception
     */
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

        this.tokenLoginServiceImpl.setSessionId2tokenHzMapName("sessionId2tokenMapName");
        this.tokenLoginServiceImpl.setToken2sessionIdMapNameHzMapName("token2sessionIdMapName");
        this.tokenLoginServiceImpl2.setSessionId2tokenHzMapName("sessionId2tokenMapName");
        this.tokenLoginServiceImpl2.setToken2sessionIdMapNameHzMapName("token2sessionIdMapName");

        PowerMockito.when(tokenLoginServiceImpl, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(
            org.mockito.Matchers.eq("token2sessionIdMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return tokenIMap;
            }
        });
        PowerMockito.when(tokenLoginServiceImpl, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(
            org.mockito.Matchers.eq("sessionId2tokenMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return SessionIMap;
            }
        });
        PowerMockito.when(tokenLoginServiceImpl2, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(
            org.mockito.Matchers.eq("token2sessionIdMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return tokenIMap;
            }
        });
        PowerMockito.when(tokenLoginServiceImpl2, method(TokenLoginServiceImpl.class, "hzMap", String.class)).withArguments(
            org.mockito.Matchers.eq("sessionId2tokenMapName")).thenAnswer(new Answer<IMap<String, String>>() {

            @Override
            public IMap<String, String> answer(InvocationOnMock invocation) throws Throwable {
                return SessionIMap;
            }
        });
    }

}
