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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.ConfigMenu.convert2JS;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.Multiple;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.ResultCode;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginJsonEnhancer;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.multifactor.MultifactorChecker;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.servlet.Constants;
import com.openexchange.session.Session;
import com.openexchange.session.ThreadLocalSessionHolder;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.Key;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.servlet.ratelimit.RateLimiter;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link AbstractLoginRequestHandler}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractLoginRequestHandler implements LoginRequestHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractLoginRequestHandler.class);

    private static final String USER_AGENT = "user-agent";

    private static volatile Integer maxLoginRate;

    private static int maxLoginRate() {
        Integer tmp = maxLoginRate;
        if (null == tmp) {
            synchronized (RateLimiter.class) {
                tmp = maxLoginRate;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 50;
                    }
                    tmp = Integer.valueOf(service.getProperty("com.openexchange.ajax.login.maxRate", "50"));
                    maxLoginRate = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Integer maxLoginRateTimeWindow;

    private static int maxLoginRateTimeWindow() {
        Integer tmp = maxLoginRateTimeWindow;
        if (null == tmp) {
            synchronized (RateLimiter.class) {
                tmp = maxLoginRateTimeWindow;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 300000;
                    }
                    tmp = Integer.valueOf(service.getProperty("com.openexchange.ajax.login.maxRateTimeWindow", "300000"));
                    maxLoginRateTimeWindow = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private final Set<LoginRampUpService> rampUpServices;

    /**
     * Initializes a new {@link AbstractLoginRequestHandler}.
     *
     * @param rampUpServices The optional ramp-up services
     */
    protected AbstractLoginRequestHandler(final Set<LoginRampUpService> rampUpServices) {
        super();
        this.rampUpServices = rampUpServices;
    }

    /**
     * Invokes given {@link LoginClosure}'s {@link LoginClosure#doLogin(HttpServletRequest) doLogin()} method to obtain a session.
     * <p>
     * Having the session further ramp-up operations are performed and finally an appropriate JSON result object is composed that gets written to passed HTTP response.
     *
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @param login The login closure to invoke
     * @param conf The login configuration
     * @param requestContext The request's context
     * @return <code>true</code> if an auto-login should proceed afterwards; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    protected boolean loginOperation(HttpServletRequest req, HttpServletResponse resp, LoginClosure login, LoginConfiguration conf, LoginRequestContext requestContext) throws IOException, OXException {
        return loginOperation(req, resp, login, null, conf, requestContext);
    }

    /**
     * Invokes given {@link LoginClosure}'s {@link LoginClosure#doLogin(HttpServletRequest) doLogin()} method to obtain a session.
     * <p>
     * Having the session further ramp-up operations are performed and finally an appropriate JSON result object is composed that gets written to passed HTTP response.
     *
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @param login The login closure to invoke
     * @param conf The login configuration
     * @param requestContext The request's context
     * @return <code>true</code> if an auto-login should proceed afterwards; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    protected boolean loginOperation(HttpServletRequest req, HttpServletResponse resp, LoginClosure login, LoginCookiesSetter cookiesSetter, LoginConfiguration conf, LoginRequestContext requestContext) throws IOException, OXException {
        Tools.disableCaching(resp);
        AJAXServlet.setDefaultContentType(resp);

        // Perform the login
        final Response response = new Response();
        LoginResult result = null;
        ServerSession serverSession = null;
        try {
            // Do the login...
            {
                int rate = maxLoginRate();
                int timeWindow = maxLoginRateTimeWindow();
                if (rate <= 0 || timeWindow <= 0) {
                    // No rate limit enabled
                    result = login.doLogin(req);
                    if (null == result) {
                        return true;
                    }
                } else {
                    Key rateLimitKey = new Key(req, req.getHeader(USER_AGENT), "__login.failed");
                    boolean doubleRate = true;
                    try {
                        // Optionally consume one permit
                        boolean consumed = RateLimiter.optRateLimitFor(rateLimitKey, rate, timeWindow, req);
                        try {
                            result = login.doLogin(req);
                            if (null == result) {
                                return true;
                            }
                            // Successful login (so far) -- clean rate limit trace
                            RateLimiter.removeRateLimit(rateLimitKey);
                        } catch (OXException e) {
                            if (!consumed && LoginExceptionCodes.INVALID_CREDENTIALS.equals(e)) {
                                // Consume one permit
                                doubleRate = false;
                                RateLimiter.checkRateLimitFor(rateLimitKey, rate, timeWindow, req);
                            }
                            throw e;
                        }
                    } catch (RateLimitedException rateLimitExceeded) {
                        // Double the rate
                        if (doubleRate) {
                            RateLimiter.doubleRateLimitWindow(rateLimitKey, maxLoginRateTimeWindow() * 2);
                        }
                        // Mark optional HTTP session as rate-limited
                        HttpSession optionalHttpSession = req.getSession(false);
                        if (optionalHttpSession != null) {
                            optionalHttpSession.setAttribute(Constants.HTTP_SESSION_ATTR_RATE_LIMITED, Boolean.TRUE);
                        }
                        throw rateLimitExceeded;
                    }
                }
            }

            // The associated session
            final Session session = result.getSession();

            // Add session log properties
            LogProperties.putSessionProperties(session);

            // Add headers and cookies from login result
            LoginServlet.addHeadersAndCookies(result, resp);

            // Check result code
            {
                final ResultCode code = result.getCode();
                if (null != code) {
                    switch (code) {
                        case FAILED:
                            return true;
                        case REDIRECT:
                            throw LoginExceptionCodes.REDIRECT.create(result.getRedirect());
                        default:
                            break;
                    }
                }
            }

            // Request modules
            Future<Object> optModules = getModulesAsync(session, req);

            // Remember User-Agent
            session.setParameter(USER_AGENT, req.getHeader(USER_AGENT));
            serverSession = ServerSessionAdapter.valueOf(session);

            // Trigger client-specific ramp-up
            Future<JSONObject> optRampUp = rampUpAsync(serverSession, req);

            // Write response
            JSONObject json = new JSONObject(13);
            LoginWriter.write(result, json);
            if (result instanceof LoginJsonEnhancer) {
                ((LoginJsonEnhancer) result).enhanceJson(json);
            }

            // Handle initial multiple
            {
                String multipleRequest = req.getParameter("multiple");
                if (multipleRequest != null) {
                    final JSONArray dataArray = new JSONArray(multipleRequest);
                    if (dataArray.length() > 0) {
                        JSONArray responses = Multiple.perform(dataArray, req, serverSession);
                        json.put("multiple", responses);
                    } else {
                        json.put("multiple", new JSONArray(0));
                    }
                }
            }

            // Await client-specific ramp-up and add to JSON object
            if (null != optRampUp) {
                int timeoutSecs = getLoginRampUpTimeout(session);
                try {
                    JSONObject jsonObject = optRampUp.get(timeoutSecs, TimeUnit.SECONDS);
                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                } catch (InterruptedException e) {
                    // Keep interrupted state
                    optRampUp.cancel(true);
                    Thread.currentThread().interrupt();
                    throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                } catch (TimeoutException e) {
                    optRampUp.cancel(true);
                    if (LOG.isDebugEnabled()) {
                        LOG.warn("Ramp-up information could not be added to login JSON response within {} seconds. Skipping...", Integer.valueOf(timeoutSecs), e);
                    } else {
                        LOG.warn("Ramp-up information could not be added to login JSON response within {} seconds. Skipping...", Integer.valueOf(timeoutSecs));
                    }
                } catch (ExecutionException e) {
                    // Cannot occur
                    final Throwable cause = e.getCause();
                    LOG.warn("Ramp-up information could not be added to login JSON response", cause);
                }
            }

            // Add modules information to JSON object
            if (null != optModules) {
                // Append "config/modules"
                try {
                    final Object oModules = optModules.get();
                    if (null != oModules) {
                        json.put("modules", oModules);
                    }
                } catch (InterruptedException e) {
                    // Keep interrupted state
                    Thread.currentThread().interrupt();
                    throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                } catch (ExecutionException e) {
                    // Cannot occur
                    final Throwable cause = e.getCause();
                    LOG.warn("Modules could not be added to login JSON response", cause);
                }
            }

            // Set response
            response.setData(json);
        } catch (OXException e) {
            if (AjaxExceptionCodes.PREFIX.equals(e.getPrefix())) {
                throw e;
            }
            if (LoginExceptionCodes.NOT_SUPPORTED.equals(e)) {
                // Rethrow according to previous behavior
                LOG.debug("", e);
                throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
            }
            if (LoginExceptionCodes.REDIRECT.equals(e) || LoginExceptionCodes.AUTHENTICATION_DISABLED.equals(e) || LoginExceptionCodes.INVALID_CREDENTIALS.equals(e)) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
            requestContext.getMetricProvider().recordException(e);
        } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }

        try {
            if (response.hasError() || null == result) {
                ResponseWriter.write(response, resp.getWriter(), extractLocale(req, result));
                requestContext.getMetricProvider().recordException(response.getException());
                return false;
            }

            Session session = result.getSession();
            // Store associated session
            if (null == serverSession) {
                serverSession = new ServerSessionAdapter(session);
            }
            SessionUtility.rememberSession(req, serverSession);
            ThreadLocalSessionHolder.getInstance().setSession(serverSession);

            // Set cookies
            if (null == cookiesSetter) {
                // Create/re-write secret cookie
                LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);

                // Create/re-write session cookie
                LoginServlet.writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
            } else {
                cookiesSetter.setLoginCookies(session, req, resp, conf);
            }

            // Login response is unfortunately not conform to default responses.
            if (req.getParameter("callback") != null && LoginServlet.ACTION_LOGIN.equals(req.getParameter("action"))) {
                APIResponseRenderer.writeResponse(response, LoginServlet.ACTION_LOGIN, req, resp);
            } else {
                ((JSONObject) response.getData()).write(resp.getWriter());
            }
        } catch (JSONException e) {
            if (e.getCause() instanceof IOException) {
                // Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                // throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                throw (IOException) e.getCause();
            }
            LOG.error(LoginServlet.RESPONSE_ERROR, e);
            LoginServlet.sendError(resp);
            requestContext.getMetricProvider().recordHTTPStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return false;
        } finally {
            ThreadLocalSessionHolder.getInstance().clear();
        }

        requestContext.getMetricProvider().recordSuccess();
        return false;
    }

    private static int getLoginRampUpTimeout(Session session) {
        String propertyName = "com.openexchange.ajax.login.rampup.timeoutSeconds";
        int defaultValue = 10;
        try {
            ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
            ConfigView view = factory.getView(session.getUserId(), session.getContextId());
            return ConfigViews.getDefinedIntPropertyFrom(propertyName, defaultValue, view);
        } catch (Exception e) {
            LOG.warn("Failed to obtain value for property \"{}\". Returning default value of {} instead.", propertyName, I(defaultValue), e);
            return defaultValue;
        }
    }

    /**
     * Performs the ramp-up.
     *
     * @param req The HTTP request
     * @param json The JSON object to contribute to
     * @param session The associated session
     * @throws OXException If an Open-Xchange error occurred
     * @throws IOException If an I/O error occurred
     */
    protected void performRampUp(HttpServletRequest req, JSONObject json, ServerSession session) throws OXException, IOException {
        performRampUp(req, json, session, false);
    }

    /**
     * (Possibly enforced) Performs the ramp-up.
     *
     * @param req The HTTP request
     * @param json The JSON object to contribute to
     * @param session The associated session
     * @param force <code>true</code> to enforce; otherwise <code>false</code> to check for presence of <code>"...&rampup=true"</code> URL parameter
     * @throws OXException If an Open-Xchange error occurred
     * @throws IOException If an I/O error occurred
     */
    protected void performRampUp(HttpServletRequest req, JSONObject json, ServerSession session, boolean force) throws OXException, IOException {
        if (null != session && (force || Boolean.parseBoolean(req.getParameter("rampup")))) {
            final Set<LoginRampUpService> rampUpServices = this.rampUpServices;
            if (rampUpServices == null) {
                LOG.warn("Missing ramp-up services. Seems that there were issues during login servlet registration.");
                return;
            }

            try {
                String client = session.getClient();
                {
                    String clientOverride = req.getParameter("rampUpFor");
                    if (clientOverride != null) {
                        client = clientOverride;
                    }
                }
                if (MultifactorChecker.requiresMultifactor(session)) {
                    client = "multifactor";
                }
                for (LoginRampUpService rampUpService : rampUpServices) {
                    if (rampUpService.contributesTo(client)) {
                        JSONObject contribution = rampUpService.getContribution(session, AJAXRequestDataTools.getInstance().parseRequest(req, false, false, session, ""));
                        json.put("rampup", contribution);
                        return;
                    }
                }
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }
    }

    /**
     * Extracts the locale from the specified request. First tries the {@link LoginFields#LANGUAGE_PARAM},
     * then the {@link LoginFields#LOCALE_PARAM} and finally a best guess according to the <code>Accept-Language</code>
     * header if set.
     *
     * @param req The request
     * @param result The result
     * @return The {@link Locale}
     */
    private Locale extractLocale(HttpServletRequest req, LoginResult result) {
        String sLanguage = req.getParameter(LoginFields.LANGUAGE_PARAM);
        if (null == sLanguage) {
            sLanguage = req.getParameter(LoginFields.LOCALE_PARAM);
            if (null == sLanguage) {
                return bestGuessLocale(result, req);
            }
            return LocaleTools.getLocale(sLanguage);
        }
        Locale loc = LocaleTools.getLocale(sLanguage);
        return null == loc ? bestGuessLocale(result, req) : loc;
    }

    /**
     * Tries to determine the {@link Locale} based on the <code>Accept-Language</code> header
     *
     * @param result the result
     * @param req The request
     * @return The {@link Locale}
     */
    private Locale bestGuessLocale(LoginResult result, final HttpServletRequest req) {
        if (null == result) {
            return Tools.getLocaleByAcceptLanguage(req, null);
        }
        User user = result.getUser();
        return null == user ? Tools.getLocaleByAcceptLanguage(req, null) : user.getLocale();
    }

    /**
     * Asynchronously retrieves modules.
     *
     * @param session The associated session
     * @param req The request
     * @return The resulting object or <code>null</code>
     */
    public Future<Object> getModulesAsync(final Session session, final HttpServletRequest req) {
        final String modules = "modules";
        if (!LoginServlet.parseBoolean(req.getParameter(modules))) {
            return null;
        }
        // Submit task
        final org.slf4j.Logger logger = LOG;
        return ThreadPools.getThreadPool().submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    final Setting setting = ConfigTree.getInstance().getSettingByPath(modules);
                    SettingStorage.getInstance(session).readValues(setting);
                    return convert2JS(setting);
                } catch (OXException e) {
                    logger.warn("Modules could not be added to login JSON response", e);
                } catch (JSONException e) {
                    logger.warn("Modules could not be added to login JSON response", e);
                } catch (Exception e) {
                    logger.warn("Modules could not be added to login JSON response", e);
                }
                return null;
            }
        });
    }

    /**
     * Asynchronously triggers ramp-up.
     *
     * @param serverSession The associated session
     * @param req The request
     * @return The resulting object or <code>null</code>
     */
    public Future<JSONObject> rampUpAsync(final ServerSession serverSession, final HttpServletRequest req) {
        AbstractTask<JSONObject> task = new AbstractTask<JSONObject>() {

            @Override
            public JSONObject call() throws OXException, IOException {
                JSONObject json = new JSONObject(2);
                performRampUp(req, json, serverSession);
                return json;
            }
        };
        return ThreadPools.getThreadPool().submit(task);
    }

}
