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
         *
         * @param login
         */
        public GuestCredentials(String login) {
            super();
            this.login = login;
            password = null;
        }

        /**
         * Use the given guest login and password
         *
         * @param login
         * @param password
         */
        public GuestCredentials(String login, String password) {
            super();
            this.login = login;
            this.password = password;
        }

        /**
         * Gets the password
         *
         * @return The password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Gets the login
         *
         * @return The login
         */
        public String getLogin() {
            return login;
        }
    }

    /**
     * Creates a new guest login request used to access a share.
     *
     * @param share The share's token
     * @param target The share target path, or <code>null</code> if not specified
     * @param credentials The guest credentials
     * @param client The client identifier; may be <code>null</code>
     * @param staySignedIn
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     * @return The login request
     */
    public static LoginRequest createGuestLoginRequest(String share, String target, GuestCredentials credentials, String client, boolean staySignedIn, boolean failOnError) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new URLParameter(AJAXServlet.PARAMETER_ACTION, "guest"));
        parameters.add(new URLParameter("share", share));
        if (null != target) {
            parameters.add(new URLParameter("target", target));
        }
        if (null != client && !client.isEmpty()) {
            parameters.add(new URLParameter(LoginFields.CLIENT_PARAM, client));
        }
        parameters.add(new FieldParameter(PARAM_NAME, credentials.getLogin()));
        if (Strings.isEmpty(credentials.getPassword())) {
            parameters.add(new FieldParameter(PARAM_PASSWORD, ""));
        } else {
            parameters.add(new FieldParameter(PARAM_PASSWORD, credentials.getPassword()));
        }
        parameters.add(new URLParameter("staySignedIn", staySignedIn));
        return new LoginRequest(parameters.toArray(new Parameter[parameters.size()]), failOnError);
    }

    public static LoginRequest createGuestLoginRequest(String share, String target, GuestCredentials credentials, boolean failOnError) {
        return createGuestLoginRequest(share, target, credentials, null, false, failOnError);
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
        this(new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, LoginServlet.ACTION_REDEEM_TOKEN), new URLParameter(LoginFields.AUTHID_PARAM, parameters.getAuthId()), new URLParameter(LoginFields.CLIENT_PARAM, parameters.getClient()), new URLParameter(LoginFields.VERSION_PARAM, parameters.getVersion()), new FieldParameter(PARAM_TOKEN, parameters.getToken()), new FieldParameter(PARAM_SECRET, parameters.getSecret())
        }, failOnError);
    }

    public LoginRequest(TokenLoginParameters parameters) {
        this(parameters, true);
    }

    public LoginRequest(String login, String password, String authId, String client, String version) {
        this(login, password, authId, client, version, true);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, boolean failOnError) {
        this(new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN), new URLParameter(LoginFields.AUTHID_PARAM, authId), new URLParameter(LoginFields.CLIENT_PARAM, client), new URLParameter(LoginFields.VERSION_PARAM, version), new FieldParameter(PARAM_NAME, login), new FieldParameter(PARAM_PASSWORD, password)
        }, failOnError);
    }

    @SuppressWarnings("unused")
    public LoginRequest(String login, String password, String authId, String client, String version, boolean failOnError, boolean passwordInURL) {
        this(new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN), new URLParameter(LoginFields.AUTHID_PARAM, authId), new URLParameter(LoginFields.CLIENT_PARAM, client), new URLParameter(LoginFields.VERSION_PARAM, version), new URLParameter(LoginFields.PASSWORD_PARAM, password), new FieldParameter(PARAM_NAME, login), new FieldParameter(PARAM_PASSWORD, password) }, failOnError);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, String language, boolean failOnError) {
        this(new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN), new URLParameter(LoginFields.AUTHID_PARAM, authId), new URLParameter(LoginFields.CLIENT_PARAM, client), new URLParameter(LoginFields.VERSION_PARAM, version), new FieldParameter(PARAM_NAME, login), new FieldParameter(PARAM_PASSWORD, password), new FieldParameter(PARAM_LANGUAGE, language)
        }, failOnError);
    }

    public LoginRequest(String login, String password, String authId, String client, String version, String language, boolean storeLanguage, boolean failOnError) {
        this(new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN), new URLParameter(LoginFields.AUTHID_PARAM, authId), new URLParameter(LoginFields.CLIENT_PARAM, client), new URLParameter(LoginFields.VERSION_PARAM, version), new FieldParameter(PARAM_NAME, login), new FieldParameter(PARAM_PASSWORD, password), new FieldParameter(PARAM_LANGUAGE, language), new FieldParameter(LoginFields.STORE_LANGUAGE, String.valueOf(storeLanguage))
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
