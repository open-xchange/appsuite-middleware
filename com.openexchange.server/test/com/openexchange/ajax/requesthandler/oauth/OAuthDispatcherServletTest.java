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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
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
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.oauth.provider.DefaultScope;
import com.openexchange.oauth.provider.DefaultToken;
import com.openexchange.oauth.provider.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.OAuthInvalidRequestException;
import com.openexchange.oauth.provider.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthRequestException;
import com.openexchange.oauth.provider.OAuthToken;
import com.openexchange.oauth.provider.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.SimOAuthProvider;
import com.openexchange.server.SimpleServiceLookup;
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

        @OAuthAction
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

        @OAuthAction("rw_test")
        private final class ReadWriteAction implements AJAXActionService {
            @Override
            public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
                return RESULT;
            }
        }

        private final Map<String, AJAXActionService> services = new HashMap<>();

        private TestFactory() {
            super();
            services.put("read", new ReadAction());
            services.put("write", new WriteAction());
            services.put("readwrite", new ReadWriteAction());
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
    private SimOAuthProvider provider;
    private String readToken;
    private String writeToken;
    private String readWriteToken;
    private String expiredToken;
    private String scopelessToken;

    @BeforeClass
    public static void beforeClass() {
        DefaultDispatcher dispatcher = new DefaultDispatcher();
        dispatcher.register("test", new TestFactory());
        dispatcher.addAnnotationProcessor(new OAuthAnnotationProcessor());
        DispatcherServlet.setDispatcher(dispatcher);
        DispatcherServlet.registerRenderer(new APIResponseRenderer());
        ServerConfig.getInstance().initialize(new SimConfigurationService());
    }

    @Before
    public void setUp() throws Exception {
        provider = new SimOAuthProvider();
        DefaultToken readToken = new DefaultToken(1, 3, UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), new DefaultScope("r_test"));
        provider.addToken(readToken);
        this.readToken = readToken.getToken();

        DefaultToken writeToken = new DefaultToken(1, 3, UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), new DefaultScope("w_test"));
        provider.addToken(writeToken);
        this.writeToken = writeToken.getToken();

        DefaultToken readWriteToken = new DefaultToken(1, 3, UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), new DefaultScope("rw_test"));
        provider.addToken(readWriteToken);
        this.readWriteToken = readWriteToken.getToken();

        DefaultToken expiredToken = new DefaultToken(1, 3, UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() - 1L), new DefaultScope("rw_test"));
        provider.addToken(expiredToken);
        this.expiredToken = expiredToken.getToken();

        DefaultToken scopelessToken = new DefaultToken(1, 3, UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000L), new DefaultScope());
        provider.addToken(scopelessToken);
        this.scopelessToken = scopelessToken.getToken();

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(OAuthProviderService.class, provider);
        servlet = new OAuthDispatcherServlet(serviceLookup, new OAuthSessionManager() {
            @Override
            public ServerSession getSession(HttpServletRequest httpRequest, OAuthToken accessToken) throws OXException {
                SimServerSession simServerSession = new SimServerSession(accessToken.getContextID(), accessToken.getUserID());
                simServerSession.setParameter(LogProperties.Name.DATABASE_SCHEMA.getName(), "oxdb1");
                simServerSession.setParameter("com.openexchange.oauth.token", accessToken);
                return simServerSession;
            }
        });
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
        });
    }

    private void prepareRequest(String action, String accessToken) {
        request.setServerName("appsuite.example.com");
        request.setServerPort(80);
        request.setRequestURI(Dispatcher.PREFIX.get() + "oauth/modules/test");
        request.setQueryString("action=" + action);
        request.setParameter("action", action);
        request.setContextPath("");
        request.setInputStream(new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return -1;
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
        OAuthInsufficientScopeException expectedException = new OAuthInsufficientScopeException("rw_test");
        assertErrorResponse(expectedException);
    }

    @Test
    public void testInsufficientScope3() throws Exception {
        prepareRequest("readwrite", scopelessToken);
        servlet.service(request, response);
        assertStatus(HttpServletResponse.SC_FORBIDDEN);
        OAuthInsufficientScopeException expectedException = new OAuthInsufficientScopeException("rw_test");
        assertErrorResponse(expectedException);
    }

    @Test
    public void testGrantAllScope1() throws Exception {
        prepareRequest("unprivileged", scopelessToken);
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
