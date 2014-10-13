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

package com.openexchange.ajax.login;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.ResultCode;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.authorization.Authorization;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.AbstractJsonEnhancingLoginResult;
import com.openexchange.login.internal.AddSessionParameterImpl;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.user.UserService;

/**
 * {@link AnonymousLogin}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AnonymousLogin extends AbstractLoginRequestHandler {

    private final LoginConfiguration conf;

    /**
     * Initializes a new {@link AnonymousLogin}.
     *
     * @param login
     */
    public AnonymousLogin(LoginConfiguration conf, Set<LoginRampUpService> rampUp) {
        super(rampUp);
        this.conf = conf;
    }

    @Override
    public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Look-up necessary credentials
        try {
            doLogin(req, resp);
        } catch (final OXException e) {
            LoginServlet.logAndSendException(resp, e);
        }
    }

    private void doLogin(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException, OXException {
        final LoginConfiguration conf = this.conf;
        LoginClosure loginClosure = new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req) throws OXException {
                try {
                    // Get the share's token
                    String token = req.getParameter("share");
                    if (null == token) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create("share");
                    }

                    // Get the ShareService to obtain associated share
                    ShareService shareService = ServerServiceRegistry.getInstance().getService(ShareService.class);
                    if (null == shareService) {
                        throw ServiceExceptionCode.absentService(ShareService.class);
                    }

                    // Get the share
                    final Share share = shareService.resolveToken(token);
                    if (null == share) {
                        throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
                    }

                    // Check for matching authentication mode
                    if (AuthenticationMode.ANONYMOUS_PASSWORD != share.getAuthentication()) {
                        throw INVALID_CREDENTIALS.create();
                    }

                    BasicAuthenticationService basicService = Authentication.getBasicService();
                    if (null == basicService) {
                        throw ServiceExceptionCode.absentService(BasicAuthenticationService.class);
                    }

                    // Get the login info from JSON body
                    LoginInfo loginInfo;
                    {
                        final String pass;

                        String body = AJAXServlet.getBody(httpRequest);
                        if (null == body) {
                            // By parameters
                            pass = httpRequest.getParameter(LoginFields.PASSWORD_PARAM);
                            if (Strings.isEmpty(pass)) {
                                throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.PASSWORD_PARAM);
                            }
                        } else {
                            // By request body
                            JSONObject jBody = new JSONObject(body);
                            pass = jBody.getString("password");
                        }

                        loginInfo = new LoginInfo() {
                            @Override
                            public String getPassword() {
                                return pass;
                            }
                            @Override
                            public String getUsername() {
                                return "anonymous";
                            }
                            @Override
                            public Map<String, Object> getProperties() {
                                return new HashMap<String, Object>(1);
                            }
                        };
                    }

                    // Resolve context
                    Context context;
                    {
                        ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
                        if (null == contextService) {
                            throw ServiceExceptionCode.absentService(ContextService.class);
                        }

                        context = contextService.getContext(share.getContextID());
                    }

                    // Resolve & authenticate user
                    User user;
                    {
                        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
                        if (null == userService) {
                            throw ServiceExceptionCode.absentService(UserService.class);
                        }

                        ShareCryptoService cryptoService = ServerServiceRegistry.getInstance().getService(ShareCryptoService.class);
                        if (null == cryptoService) {
                            throw ServiceExceptionCode.absentService(ShareCryptoService.class);
                        }

                        user = userService.getUser(share.getGuest(), context);
                        String decryptedPassword = cryptoService.decrypt(user.getUserPassword());

                        if (!decryptedPassword.equals(loginInfo.getPassword())) {
                            throw INVALID_CREDENTIALS.create();
                        }
                    }

                    Authenticated  authenticated = basicService.handleLoginInfo(share.getGuest(), share.getContextID());
                    if (null == authenticated) {
                        return null;
                    }

                    // Checks if something is deactivated.
                    AuthorizationService authService = Authorization.getService();
                    if (null == authService) {
                        throw ServiceExceptionCode.absentService(AuthorizationService.class);
                    }
                    authService.authorizeUser(context, user);

                    // Parse & check the HTTP request
                    LoginRequest request = LoginTools.parseLogin(httpRequest, loginInfo.getUsername(), loginInfo.getPassword(), false, conf.getDefaultClient(), conf.isCookieForceHTTPS(), false);
                    LoginPerformer.sanityChecks(request);
                    LoginPerformer.checkClient(request, user, context);

                    // Create session
                    Session session;
                    {
                        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
                        if (null == sessiondService) {
                            sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                            if (null == sessiondService) {
                                // Giving up...
                                throw ServiceExceptionCode.absentService(SessiondService.class);
                            }
                        }
                        session = sessiondService.addSession(new AddSessionParameterImpl(loginInfo.getUsername(), request, user, context));
                        if (null == session) {
                            // Session could not be created
                            throw LoginExceptionCodes.UNKNOWN.create("Session could not be created.");
                        }
                        if (SessionEnhancement.class.isInstance(authenticated)) {
                            ((SessionEnhancement) authenticated).enhanceSession(session);
                        }
                        LogProperties.putSessionProperties(session);
                    }

                    // Generate the login result
                    LoginResultImpl retval = new AbstractJsonEnhancingLoginResult() {

                        @Override
                        protected void doEnhanceJson(JSONObject jLoginResult) throws OXException, JSONException {
                            jLoginResult.put("folder", share.getFolder());

                            jLoginResult.put("module", share.getModule());

                            String item = share.getItem();
                            if (null != item) {
                                jLoginResult.put("item", item);
                            }
                        }
                    };
                    retval.setContext(context);
                    retval.setUser(user);
                    retval.setRequest(request);
                    retval.setServerToken((String) session.getParameter(LoginFields.SERVER_TOKEN));
                    retval.setSession(session);
                    if (authenticated instanceof ResponseEnhancement) {
                        final ResponseEnhancement responseEnhancement = (ResponseEnhancement) authenticated;
                        retval.setHeaders(responseEnhancement.getHeaders());
                        retval.setCookies(responseEnhancement.getCookies());
                        retval.setRedirect(responseEnhancement.getRedirect());
                        final ResultCode code = responseEnhancement.getCode();
                        retval.setCode(code);
                        if (ResultCode.REDIRECT.equals(code) || ResultCode.FAILED.equals(code)) {
                            return retval;
                        }
                    }

                    // Trigger registered login handlers
                    LoginPerformer.triggerLoginHandlers(retval);
                    return retval;
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                } catch (IOException e) {
                    throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        };

        LoginCookiesSetter cookiesSetter = new LoginCookiesSetter() {

            @Override
            public void setLoginCookies(Session session, HttpServletRequest httpRequest, HttpServletResponse httpResponse, LoginConfiguration loginConfiguration) throws OXException {
                Cookie cookie = new Cookie(LoginServlet.SECRET_PREFIX + session.getHash(), session.getSecret());
                cookie.setPath("/");
                String serverName = httpRequest.getServerName();
                if (httpRequest.isSecure() || (conf.isCookieForceHTTPS() && !Cookies.isLocalLan(serverName))) {
                    cookie.setSecure(true);
                }
                /*
                 * A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero
                 * value causes the cookie to be deleted.
                 */
                cookie.setMaxAge(-1);
                final String domain = getDomainValue(serverName);
                if (null != domain) {
                    cookie.setDomain(domain);
                }
                httpResponse.addCookie(cookie);

                String altId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
                if (null != altId) {
                    cookie = new Cookie(LoginServlet.getPublicSessionCookieName(httpRequest), altId);
                    cookie.setPath("/");
                    if (httpRequest.isSecure() || (conf.isCookieForceHTTPS() && !Cookies.isLocalLan(serverName))) {
                        cookie.setSecure(true);
                    }
                    /*
                     * A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero
                     * value causes the cookie to be deleted.
                     */
                    cookie.setMaxAge(-1);
                    if (null != domain) {
                        cookie.setDomain(domain);
                    }
                    httpResponse.addCookie(cookie);
                }
            }
        };

        // Do the login operation
        loginOperation(httpRequest, httpResponse, loginClosure, cookiesSetter, conf);
    }
}
