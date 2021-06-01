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

package com.openexchange.authentication.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.idn.IDNA;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.DefaultLoginInfo;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * Provides the static methods to do the authentication.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Authentication {

    private static final AtomicReference<AuthenticationService> SERVICE_REF = new AtomicReference<AuthenticationService>();
    private static final AtomicReference<BasicAuthenticationService> BASIC_SERVICE_REF = new AtomicReference<BasicAuthenticationService>();

    /**
     * Prevent initialization.
     */
    private Authentication() {
        super();
    }

    /**
     * Performs a login using an authentication service.
     *
     * @param login entered login.
     * @param pass entered password.
     * @param properties The optional properties
     * @return The resolved login information for the context as well as for the user
     * @throws OXException If something with the login info is wrong or needed service is absent
     */
    public static Authenticated login(String login, String pass, Map<String, Object> properties) throws OXException {
        AuthenticationService auth = SERVICE_REF.get();
        if (null == auth) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( AuthenticationService.class.getName());
        }

        return login(new DefaultLoginInfo(login, pass, properties), auth);
    }

    /**
     * Performs a login using given authentication service and login info.
     *
     * @param loginInfo The login info
     * @param authenticationService The authentication service
     * @return The resolved login information for the context as well as for the user
     * @throws OXException If something with the login info is wrong
     */
    public static Authenticated login(LoginInfo loginInfo, AuthenticationService authenticationService) throws OXException {
        try {
            return authenticationService.handleLoginInfo(loginInfo);
        } catch (OXException e) {
            // Check for possible ACE/puny-code notation
            String idn = checkAceNotation(loginInfo, e);

            // Retry with IDN representation
            return authenticationService.handleLoginInfo(new DefaultLoginInfo(idn, loginInfo.getPassword(), loginInfo.getProperties()));
        }
    }

    /**
     * Performs an auto-login using an authentication service.
     *
     * @param login entered login.
     * @param pass entered password.
     * @param properties The optional properties
     * @return The resolved login information for the context as well as for the user
     * @throws OXException If something with the login info is wrong or needed service is absent
     */
    public static Authenticated autologin(String login, String pass, Map<String, Object> properties) throws OXException {
        AuthenticationService auth = SERVICE_REF.get();
        if (null == auth) {
            return null;
        }

        return autologin(new DefaultLoginInfo(login, pass, properties), auth);
    }

    /**
     * Performs an auto-login using given authentication service and login info.
     *
     * @param loginInfo The login info
     * @param authenticationService The authentication service
     * @return The resolved login information for the context as well as for the user
     * @throws OXException If something with the login info is wrong or needed service is absent
     */
    public static Authenticated autologin(LoginInfo loginInfo, AuthenticationService authenticationService) throws OXException {
        try {
            return authenticationService.handleAutoLoginInfo(loginInfo);
        } catch (OXException e) {
            // Check for possible ACE/puny-code notation
            String idn = checkAceNotation(loginInfo, e);

            return authenticationService.handleAutoLoginInfo(new DefaultLoginInfo(idn, loginInfo.getPassword(), loginInfo.getProperties()));
        }
    }

    /**
     * Checks if specified failed login info provides an ACE login string, to which an alternative IDN notation is available.
     *
     * @param loginInfo The login info to check
     * @param e The login failure
     * @return The IDN notation
     * @throws OXException If no alternative IDN notation is available or it does not need to be checked
     */
    private static String checkAceNotation(LoginInfo loginInfo, OXException e) throws OXException {
        if (false == LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.equals(e)) {
            throw e;
        }

        LoginConfiguration loginConfiguration = LoginServlet.getLoginConfiguration();
        if ((null == loginConfiguration) || (false == loginConfiguration.isCheckPunyCodeLoginString())) {
            throw e;
        }

        String userName = loginInfo.getUsername();
        if (userName.indexOf("xn--") < 0) {
            throw e;
        }

        String idn = IDNA.toIDN(userName);
        if (userName.equals(idn)) {
            throw e;
        }

        return idn;
    }

    /**
     * Gets the registered {@code AuthenticationService}.
     *
     * @return The registered {@code AuthenticationService} or <code>null</code>
     */
    public static AuthenticationService getService() {
        return SERVICE_REF.get();
    }

    public static boolean setService(AuthenticationService service) {
        return SERVICE_REF.compareAndSet(null, service);
    }

    public static boolean dropService(AuthenticationService service) {
        return SERVICE_REF.compareAndSet(service, null);
    }

    /**
     * Gets the registered {@link BasicAuthenticationService}.
     *
     * @return The registered {@link BasicAuthenticationService} or <code>null</code>
     */
    public static BasicAuthenticationService getBasicService() {
        return BASIC_SERVICE_REF.get();
    }

    public static void setBasicService(BasicAuthenticationService service) {
        BASIC_SERVICE_REF.set(service);
    }

}
