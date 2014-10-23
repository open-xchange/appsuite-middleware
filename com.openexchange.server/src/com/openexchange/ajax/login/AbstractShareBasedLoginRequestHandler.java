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

package com.openexchange.ajax.login;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import java.io.IOException;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginRampUpService;
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
import com.openexchange.share.ShareList;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.Share;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;


/**
 * {@link AbstractShareBasedLoginRequestHandler} - The abstract login request handler for share-based login requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractShareBasedLoginRequestHandler extends AbstractLoginRequestHandler {

    /** The login configuration */
    protected final LoginConfiguration conf;

    /**
     * Initializes a new {@link AbstractShareBasedLoginRequestHandler}.
     *
     * @param conf The login configuration
     * @param rampUpServices The ramp-up services
     */
    protected AbstractShareBasedLoginRequestHandler(LoginConfiguration conf, Set<LoginRampUpService> rampUpServices) {
        super(rampUpServices);
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

    /**
     * Performs the login for this share-based login request.
     *
     * @param httpRequest The HTTP request
     * @param httpResponse The HTTP response
     * @throws IOException If an I/O error occors
     * @throws OXException If an Open-Xchange Server error occurs
     */
    protected void doLogin(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException, OXException {
        final LoginConfiguration conf = this.conf;
        LoginClosure loginClosure = new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req) throws OXException {
                try {
                    // Get the share's token & target
                    String token = req.getParameter("share");
                    String targetPath = req.getParameter("target");
                    if (null == token) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create("share");
                    }

                    // Get the ShareService to obtain associated share
                    ShareService shareService = ServerServiceRegistry.getInstance().getService(ShareService.class);
                    if (null == shareService) {
                        throw ServiceExceptionCode.absentService(ShareService.class);
                    }

                    // Get the share
                    final ShareList share = shareService.resolveToken(token);
                    if (null == share) {
                        throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
                    }
                    final Share target = Strings.isEmpty(targetPath) ? null : share.resolveTarget(targetPath);

                    // Check for matching authentication mode
                    if (false == checkAuthenticationMode(share.getAuthentication())) {
                        throw INVALID_CREDENTIALS.create();
                    }

                    BasicAuthenticationService basicService = Authentication.getBasicService();
                    if (null == basicService) {
                        throw ServiceExceptionCode.absentService(BasicAuthenticationService.class);
                    }

                    // Get the login info from HTTP request
                    LoginInfo loginInfo = getLoginInfoFrom(share, httpRequest);

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
                    User user = authenticateUser(share, loginInfo, context);

                    // Pass to basic authentication service in case more handling needed
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
                    LoginRequestImpl request = LoginTools.parseLogin(httpRequest, loginInfo.getUsername(), loginInfo.getPassword(), false, conf.getDefaultClient(), conf.isCookieForceHTTPS(), false);
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
                        {
                            ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                            boolean tranzient = null == service || service.getBoolProperty("com.openexchange.share.transientSessions", true);
                            request.setTransient(tranzient);
                        }
                        session = sessiondService.addSession(new AddSessionParameterImpl(loginInfo.getUsername(), request, user, context));
                        if (null == session) {
                            // Session could not be created
                            throw LoginExceptionCodes.UNKNOWN.create("Session could not be created.");
                        }
                        session.setParameter(Session.PARAM_GUEST, Boolean.TRUE);
                        if (SessionEnhancement.class.isInstance(authenticated)) {
                            ((SessionEnhancement) authenticated).enhanceSession(session);
                        }
                        LogProperties.putSessionProperties(session);
                    }

                    // Generate the login result
                    LoginResultImpl retval = new AbstractJsonEnhancingLoginResult() {

                        @Override
                        protected void doEnhanceJson(JSONObject jLoginResult) throws OXException, JSONException {
                            int module = null != target ? target.getModule() : share.getCommonModule();
                            if (0 != module) {
                                Module folderModule = Module.getForFolderConstant(module);
                                jLoginResult.put("module", null != folderModule ? folderModule.getName() : String.valueOf(module));
                            }
                            String folder = null != target ? target.getFolder() : share.getCommonFolder();
                            jLoginResult.putOpt("folder", folder);
                            String item = null != target ? target.getItem() :
                                null != share.getTargets() && 1 == share.getTargets().size() ? share.getTargets().get(0).getItem() : null;
                            jLoginResult.putOpt("item", item);
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
                } catch (RuntimeException e) {
                    throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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

    /**
     * Checks the share's authentication mode against performed login
     *
     * @param authenticationMode The authentication mode to check
     * @return <code>true</code> if authentication mode mmatches; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     */
    protected abstract boolean checkAuthenticationMode(AuthenticationMode authenticationMode) throws OXException;

    /**
     * Gets the appropriate share's login information from given HTTP request
     *
     * @param share The associated share
     * @param httpRequest The HTTP request
     * @return The login information
     * @throws OXException If login information cannot be returned
     */
    protected abstract LoginInfo getLoginInfoFrom(ShareList share, HttpServletRequest httpRequest) throws OXException;

    /**
     * Authenticates the user associated with specified share using given login information.
     *
     * @param share The share
     * @param loginInfo The login information
     * @param context The context associated with the share
     * @return The authenticated user
     * @throws OXException If authentication fails
     */
    protected abstract User authenticateUser(ShareList share, LoginInfo loginInfo, Context context) throws OXException;

}
