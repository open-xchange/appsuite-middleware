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

package com.openexchange.ajax.session.actions;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.java.Strings;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginRequest extends AbstractRequest<LoginResponse> {

    private static final String PARAM_PASSWORD = "password";

    private static final String PARAM_NAME = "name";

    private static final String PARAM_TOKEN = "token";

    private static final String PARAM_SECRET = "secret";

    private static final String PARAM_LANGUAGE = "language";

    private final boolean failOnError;

    public static final class GuestCredentials {
        private final String login;
        private final String password;

        /**
         * Use the given guest login but omit the password and skip setting one
         * @param login
         */
        public GuestCredentials(String login) {
            super();
            this.login = login;
            password = null;
        }

        /**
         * Use the given guest login and password
         * @param login
         * @param password
         */
        public GuestCredentials(String login, String password) {
            super();
            this.login = login;
            this.password = password;
        }
    }

    /**
     * Creates a new guest login request used to access a share.
     *
     * @param share The share's token
     * @param target The share target path, or <code>null</code> if not specified
     * @param credentials The guest credentials
     * @param client The client identifier; may be <code>null</code>
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     * @return The login request
     */
    public static LoginRequest createGuestLoginRequest(String share, String target, GuestCredentials credentials, String client, boolean failOnError) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, "guest"));
        parameters.add(new URLParameter("share", share));
        if (null != target) {
            parameters.add(new URLParameter("target", target));
        }
        if (null != client && !client.isEmpty()) {
            parameters.add(new URLParameter(LoginFields.CLIENT_PARAM, client));
        }
        parameters.add(new FieldParameter(PARAM_NAME, credentials.login));
        if (Strings.isEmpty(credentials.password)) {
            parameters.add(new FieldParameter(PARAM_PASSWORD, ""));
        } else {
            parameters.add(new FieldParameter(PARAM_PASSWORD, credentials.password));
        }
        return new LoginRequest(parameters.toArray(new Parameter[parameters.size()]), failOnError);
    }

    public static LoginRequest createGuestLoginRequest(String share, String target, GuestCredentials credentials, boolean failOnError) {
        return createGuestLoginRequest(share, target, credentials, null, failOnError);
    }

    /**
     * Creates a new anonymous login request used to access a share.
     *
     * @param share The share's token
     * @param target The share target path, or <code>null</code> if not specified
     * @param password The password
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     * @return The login request
     */
    public static LoginRequest createAnonymousLoginRequest(String share, String target, String password, boolean failOnError) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, "anonymous"));
        parameters.add(new URLParameter("share", share));
        if (null != target) {
            parameters.add(new URLParameter("target", target));
        }
        parameters.add(new FieldParameter(PARAM_PASSWORD, password));
        return new LoginRequest(parameters.toArray(new Parameter[parameters.size()]), failOnError);
    }

    /**
     * Constructor for Login with token. Initializes a new {@link LoginRequest}.
     */
    public LoginRequest(TokenLoginParameters parameters, boolean failOnError) {
        this(new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, LoginServlet.ACTION_REDEEM_TOKEN),
            new URLParameter(LoginFields.AUTHID_PARAM, parameters.getAuthId()),
            new URLParameter(LoginFields.CLIENT_PARAM, parameters.getClient()),
            new URLParameter(LoginFields.VERSION_PARAM, parameters.getVersion()),
            new FieldParameter(PARAM_TOKEN, parameters.getToken()),
            new FieldParameter(PARAM_SECRET, parameters.getSecret())
        }, failOnError);
    }

    public LoginRequest(TokenLoginParameters parameters) {
        this(parameters, true);
    }

    public LoginRequest(String login, String password, String authId, String client, String version) {
        this(login, password, authId, client, version, true);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, boolean failOnError) {
        this(new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN),
            new URLParameter(LoginFields.AUTHID_PARAM, authId),
            new URLParameter(LoginFields.CLIENT_PARAM, client),
            new URLParameter(LoginFields.VERSION_PARAM, version),
            new FieldParameter(PARAM_NAME, login),
            new FieldParameter(PARAM_PASSWORD, password)
        }, failOnError);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, boolean failOnError, boolean passwordInURL) {
        this(new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN),
            new URLParameter(LoginFields.AUTHID_PARAM, authId),
            new URLParameter(LoginFields.CLIENT_PARAM, client),
            new URLParameter(LoginFields.VERSION_PARAM, version),
            new URLParameter(LoginFields.PASSWORD_PARAM, password),
            new FieldParameter(PARAM_NAME, login),
            new FieldParameter(PARAM_PASSWORD, password)
        }, failOnError);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, String language, boolean failOnError) {
        this(new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN),
            new URLParameter(LoginFields.AUTHID_PARAM, authId),
            new URLParameter(LoginFields.CLIENT_PARAM, client),
            new URLParameter(LoginFields.VERSION_PARAM, version),
            new FieldParameter(PARAM_NAME, login),
            new FieldParameter(PARAM_PASSWORD, password),
            new FieldParameter(PARAM_LANGUAGE, language)
        }, failOnError);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, String language, boolean storeLanguage, boolean failOnError) {
        this(new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN),
            new URLParameter(LoginFields.AUTHID_PARAM, authId),
            new URLParameter(LoginFields.CLIENT_PARAM, client),
            new URLParameter(LoginFields.VERSION_PARAM, version),
            new FieldParameter(PARAM_NAME, login),
            new FieldParameter(PARAM_PASSWORD, password),
            new FieldParameter(PARAM_LANGUAGE, language),
            new FieldParameter(LoginFields.STORE_LANGUAGE, String.valueOf(storeLanguage))
        }, failOnError);
    }

    /**
     * Initializes a new {@link LoginRequest}.
     *
     * @param parameters The request parameters
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     */
    protected LoginRequest(Parameter[] parameters, boolean failOnError) {
        super(parameters);
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponseParser getParser() {
        return new LoginResponseParser(failOnError);
    }

    public static class TokenLoginParameters {

        String token, secret, authId, client, version;

        public TokenLoginParameters(String token, String secret, String authId, String client, String version) {
            super();
            this.token = token;
            this.secret = secret;
            this.authId = authId;
            this.client = client;
            this.version = version;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getAuthId() {
            return authId;
        }

        public void setAuthId(String authId) {
            this.authId = authId;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
