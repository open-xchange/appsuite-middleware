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

import static com.openexchange.ajax.ConfigMenu.convert2JS;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.Multiple;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.ResultCode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AbstractLoginRequestHandler}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractLoginRequestHandler implements LoginRequestHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractLoginRequestHandler.class);

    private final Set<LoginRampUpService> rampUpServices;

    /**
     * Initializes a new {@link AbstractLoginRequestHandler}.
     *
     * @param rampUpServices The optional ramp-up services
     */
    protected AbstractLoginRequestHandler(final Set<LoginRampUpService> rampUpServices) {
        this.rampUpServices = rampUpServices;
    }

    /**
     * @return a boolean value indicated if an auto login should proceed afterwards
     */
    public boolean loginOperation(final HttpServletRequest req, final HttpServletResponse resp, final LoginClosure login, LoginConfiguration conf) throws IOException, OXException {
        Tools.disableCaching(resp);
        resp.setContentType(LoginServlet.CONTENTTYPE_JAVASCRIPT);

        // Perform the login
        final Response response = new Response();
        LoginResult result = null;
        try {
            // Do the login...
            result = login.doLogin(req);
            if (null == result) {
                return true;
            }

            // The associated session
            final Session session = result.getSession();

            // Add session log properties
            LogProperties.putSessionProperties(session);

            // Request modules
            final Future<Object> optModules = getModulesAsync(session, req);

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

            // Remember User-Agent
            session.setParameter("user-agent", req.getHeader("user-agent"));

            // Write response
            final JSONObject json = new JSONObject(8);
            LoginWriter.write(result, json);

            // Handle initial multiple
            final String multipleRequest = req.getParameter("multiple");
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            if (multipleRequest != null) {
                final JSONArray dataArray = new JSONArray(multipleRequest);
                if (dataArray.length() > 0) {
                    JSONArray responses = Multiple.perform(dataArray, req, serverSession);
                    json.put("multiple", responses);
                } else {
                    json.put("multiple", new JSONArray(0));
                }
            }

            if (null != optModules) {
                // Append "config/modules"
                try {
                    final Object oModules = optModules.get();
                    if (null != oModules) {
                        json.put("modules", oModules);
                    }
                } catch (final InterruptedException e) {
                    // Keep interrupted state
                    Thread.currentThread().interrupt();
                    throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                } catch (final ExecutionException e) {
                    // Cannot occur
                    final Throwable cause = e.getCause();
                    LOG.warn("Modules could not be added to login JSON response", cause);
                }
            }

            // Perform Client Specific Ramp-Up
            performRampUp(req, json, serverSession);

            // Set response
            response.setData(json);
        } catch (final OXException e) {
            if (AjaxExceptionCodes.PREFIX.equals(e.getPrefix())) {
                throw e;
            }
            if (LoginExceptionCodes.NOT_SUPPORTED.equals(e)) {
                // Rethrow according to previous behavior
                LOG.debug("", e);
                throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
            }
            if (LoginExceptionCodes.REDIRECT.equals(e)) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            response.setException(e);
        } catch (final JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }
        try {
            if (response.hasError() || null == result) {
                final Locale locale;
                {
                    final String sLocale = req.getParameter("language");
                    if (null == sLocale) {
                        locale = bestGuessLocale(result, req);
                    } else {
                        final Locale loc = LocaleTools.getLocale(sLocale);
                        locale = null == loc ? bestGuessLocale(result, req) : loc;
                    }
                }
                ResponseWriter.write(response, resp.getWriter(), locale);
                return false;
            }
            final Session session = result.getSession();
            // Store associated session
            SessionServlet.rememberSession(req, new ServerSessionAdapter(session));
            LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);
            // Login response is unfortunately not conform to default responses.
            if (req.getParameter("callback") != null && LoginServlet.ACTION_LOGIN.equals(req.getParameter("action"))) {
                APIResponseRenderer.writeResponse(response, LoginServlet.ACTION_LOGIN, req, resp);
            } else {
                ((JSONObject) response.getData()).write(resp.getWriter());
            }
        } catch (final JSONException e) {
            if (e.getCause() instanceof IOException) {
                // Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                // throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                throw (IOException) e.getCause();
            }
            LOG.error(LoginServlet.RESPONSE_ERROR, e);
            LoginServlet.sendError(resp);
            return false;
        }
        return false;
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
        if (force || Boolean.parseBoolean(req.getParameter("rampup"))) {
            final Set<LoginRampUpService> rampUpServices = this.rampUpServices;
            if (rampUpServices != null) {
                try {
                    String client = session.getClient();
                    for (LoginRampUpService rampUpService : rampUpServices) {
                        if (rampUpService.contributesTo(client)) {
                            JSONObject contribution = rampUpService.getContribution(session, AJAXRequestDataTools.getInstance().parseRequest(req, false, false, session, ""));
                            json.put("rampup", contribution);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                }
            }
        }
    }

    private Locale bestGuessLocale(LoginResult result, final HttpServletRequest req) {
        final Locale locale;
        if (null == result) {
            locale = Tools.getLocaleByAcceptLanguage(req, null);
        } else {
            final User user = result.getUser();
            if (null == user) {
                locale = Tools.getLocaleByAcceptLanguage(req, null);
            } else {
                locale = user.getLocale();
            }
        }
        return locale;
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
        return ThreadPools.getThreadPool().submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    final Setting setting = ConfigTree.getInstance().getSettingByPath(modules);
                    SettingStorage.getInstance(session).readValues(setting);
                    return convert2JS(setting);
                } catch (final OXException e) {
                    LOG.warn("Modules could not be added to login JSON response", e);
                } catch (final JSONException e) {
                    LOG.warn("Modules could not be added to login JSON response", e);
                } catch (final Exception e) {
                    LOG.warn("Modules could not be added to login JSON response", e);
                }
                return null;
            }
        });
    }

}
