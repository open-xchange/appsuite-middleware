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

package com.openexchange.ajax.requesthandler.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DefaultDispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidRequestException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.exceptions.OAuthRequestException;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.SimOAuthResourceService;
import com.openexchange.oauth.provider.resourceserver.SimOAuthResourceService.TestGrant;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;


/**
 * {@link OAuthDispatcherServletTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthDispatcherServletTest {

    private static final AJAXRequestResult RESULT = new AJAXRequestResult(new Response(new JSONObject()));
    static {
        try {
            ((Response) RESULT.getResultObject()).setData(new JSONObject("{'ok':true}"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @OAuthModule
    private static final class TestFactory implements AJAXActionServiceFactory {

        @OAuthAction(OAuthAction.GRANT_ALL)
        private final class GrantAllAction implements AJAXActionService {
            @Override
            public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
                return RESULT;
            }
        }

        @OAuthAction("r_test")
        private final class ReadAction implements AJAXActionService {
            @Override
            public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
                return RESULT;
            }
        }

        @OAuthAction("w_test")
        private final class WriteAction implements AJAXActionService {
            @Override
            public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
                return RESULT;
            }
        }

        @OAuthAction(OAuthAction.CUSTOM)
        private final class CustomAction implements AJAXActionService {
            @Override
            public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
                return RESULT;
            }

            @OAuthScopeCheck
            public boolean checkScope(AJAXRequestData requestData, ServerSession session, OAuthAccess access) {
                return access.getScope().has("r_test") && access.getScope().has("w_test");
            }
        }

        private final Map<String, AJAXActionService> services = new HashMap<>();

        private TestFactory() {
            super();
            services.put("read", new ReadAction());
            services.put("write", new WriteAction());
            services.put("readwrite", new CustomAction());
            services.put("unprivileged", new GrantAllAction());
        }

        @Override
        public Collection<?> getSupportedServices() {
            return null;
        }

        @Override
        public AJAXActionService createActionService(String action) throws OXException {
            return services.get(action);
        }
    }

    private OAuthDispatcherServlet servlet;
    private SimHttpServletRequest request;
    private SimHttpServletResponse response;
    private ByteArrayOutputStream responseStream;
    private SimOAuthResourceService resourceService;
    private String readToken;
    private String writeToken;
    private String readWriteToken;
    private String expiredToken;

    @BeforeClass
    public static void beforeClass() {
        DefaultDispatcher dispatcher = new DefaultDispatcher();
        dispatcher.register("test", new TestFactory());
        dispatcher.addAnnotationProcessor(new OAuthAnnotationProcessor());
        DispatcherServlet.setDispatcher(dispatcher);
        DispatcherServlet.registerRenderer(new APIResponseRenderer());
        ServerConfig.getInstance().initialize(new SimConfigurationService());
        DispatcherPrefixService dispatcherPrefixService = new DispatcherPrefixService() {
            @Override
            public String getPrefix() {
                return "/ajax/";
            }
        };
        ServerServiceRegistry.getInstance().addService(DispatcherPrefixService.class, dispatcherPrefixService);
        Dispatchers.setDispatcherPrefixService(dispatcherPrefixService);
    }

    @Before
    public void setUp() throws Exception {
        resourceService = new SimOAuthResourceService(new SimOAuthResourceService.SessionProvider() {
            @Override
            public Session createSession(TestGrant grant) {
                SimServerSession simServerSession = new SimServerSession(grant.getContextId(), grant.getUserId());
                simServerSession.setParameter(LogProperties.Name.DATABASE_SCHEMA.getName(), "db1");
                return simServerSession;
            }
        });
        TestGrant readToken = new TestGrant(1, 3, UUIDs.getUnformattedStringFromRandom(), UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), Scope.newInstance("r_test"));
        resourceService.addToken(readToken);
        this.readToken = readToken.getAccessToken();

        TestGrant writeToken = new TestGrant(1, 3, UUIDs.getUnformattedStringFromRandom(), UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), Scope.newInstance("w_test"));
        resourceService.addToken(writeToken);
        this.writeToken = writeToken.getAccessToken();

        TestGrant readWriteToken = new TestGrant(1, 3, UUIDs.getUnformattedStringFromRandom(), UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), Scope.newInstance("r_test", "w_test"));
        resourceService.addToken(readWriteToken);
        this.readWriteToken = readWriteToken.getAccessToken();

        TestGrant expiredToken = new TestGrant(1, 3, UUIDs.getUnformattedStringFromRandom(), UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() - 1L), Scope.newInstance("r_test"));
        resourceService.addToken(expiredToken);
        this.expiredToken = expiredToken.getAccessToken();

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(com.openexchange.oauth.provider.resourceserver.OAuthResourceService.class, resourceService);
        servlet = new OAuthDispatcherServlet(serviceLookup, "/ajax/");
        request = new SimHttpServletRequest();
        request.setMethod("GET");

        responseStream = new ByteArrayOutputStream();
        response = new SimHttpServletResponse();
        response.setCharacterEncoding("UTF-8");
        response.setOutputStream(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                responseStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // Ignore
            }
        });
    }

    private void prepareRequest(String action, String accessToken) {
        request.setServerName("appsuite.example.com");
        request.setServerPort(80);
        request.setRequestURI("/ajax/" + "oauth/modules/test");
        request.setQueryString("action=" + action);
        request.setParameter("action", action);
        request.setContextPath("");
        request.setInputStream(new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // Ignore
            }
        });
        if (accessToken != null) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
    }

    @Test
    public void testMissingToken() throws Exception {
        prepareRequest("read", null);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals("Bearer", response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertEquals(0, responseStream.size());
    }

    @Test
    public void testMalformedToken() throws Exception {
        prepareRequest("read", "?!$");
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String challenge = response.getHeader(HttpHeaders.WWW_AUTHENTICATE);
        OAuthInvalidTokenException expectedException = new OAuthInvalidTokenException(Reason.TOKEN_MALFORMED);
        assertEquals("Bearer,error=\"invalid_token\",error_description=\"" + expectedException.getErrorDescription() + "\"", challenge);
        assertErrorResponse(expectedException);
    }

    @Test
    public void testUnknownToken() throws Exception {
        prepareRequest("read", "idontexist");
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String challenge = response.getHeader(HttpHeaders.WWW_AUTHENTICATE);
        OAuthInvalidTokenException expectedException = new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
        assertEquals("Bearer,error=\"invalid_token\",error_description=\"" + expectedException.getErrorDescription() + "\"", challenge);
        assertErrorResponse(expectedException);
    }

    @Test
    public void testExpiredToken() throws Exception {
        prepareRequest("read", expiredToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String challenge = response.getHeader(HttpHeaders.WWW_AUTHENTICATE);
        OAuthInvalidTokenException expectedException = new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED);
        assertEquals("Bearer,error=\"invalid_token\",error_description=\"" + expectedException.getErrorDescription() + "\"", challenge);
        assertErrorResponse(expectedException);
    }

    @Test
    public void testInsufficientScope1() throws Exception {
        prepareRequest("write", readToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_FORBIDDEN);
        OAuthInsufficientScopeException expectedException = new OAuthInsufficientScopeException("w_test");
        assertErrorResponse(expectedException);
    }

    @Test
    public void testInsufficientScope2() throws Exception {
        prepareRequest("readwrite", writeToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_FORBIDDEN);
        OAuthInsufficientScopeException expectedException = new OAuthInsufficientScopeException();
        assertErrorResponse(expectedException);
    }

    @Test
    public void testCustomScopeCheck2() throws Exception {
        prepareRequest("readwrite", readToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_FORBIDDEN);
        OAuthInsufficientScopeException expectedException = new OAuthInsufficientScopeException();
        assertErrorResponse(expectedException);
    }

    @Test
    public void testCustomScopeCheck3() throws Exception {
        prepareRequest("readwrite", writeToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_FORBIDDEN);
        OAuthInsufficientScopeException expectedException = new OAuthInsufficientScopeException();
        assertErrorResponse(expectedException);
    }

    @Test
    public void testCustomScopeCheck4() throws Exception {
        prepareRequest("readwrite", readWriteToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGrantAllScope2() throws Exception {
        prepareRequest("unprivileged", readToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testScope1() throws Exception {
        prepareRequest("read", readToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testScope2() throws Exception {
        prepareRequest("write", writeToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testScope3() throws Exception {
        prepareRequest("readwrite", readWriteToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testScope4() throws Exception {
        prepareRequest("read", readWriteToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testScope5() throws Exception {
        prepareRequest("write", readWriteToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_OK);
    }

    private void assertStatus(int statusCode) {
        assertEquals(statusCode, response.getStatus());
    }

    private void assertErrorResponse(OAuthRequestException e) throws JSONException {
        assertEquals("application/json;charset=UTF-8", response.getHeader(HttpHeaders.CONTENT_TYPE));
        JSONObject json = JSONObject.parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(responseStream.toByteArray())))).toObject();
        assertNotNull(json);

        if (e instanceof OAuthInvalidTokenException) {
            assertEquals("invalid_token", json.get("error"));
        } else if (e instanceof OAuthInsufficientScopeException) {
            assertEquals("insufficient_scope", json.get("error"));
            String requiredScope = ((OAuthInsufficientScopeException) e).getScope();
            if (requiredScope != null) {
                assertEquals(requiredScope, json.get("scope"));
            }
        } else if (e instanceof OAuthInvalidRequestException) {
            assertEquals("invalid_request", json.get("error"));
        } else {
            fail("Unknown exception: " + e.getClass().getName());
        }

        String errorDescription = e.getErrorDescription();
        if (errorDescription != null) {
            assertEquals(errorDescription, json.get("error_description"));
        }
    }

}
