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

package com.openexchange.login;

import com.openexchange.configuration.InitProperty;
import com.openexchange.sessiond.SessiondProperty;

/**
 * {@link ConfigurationProperty}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ConfigurationProperty implements InitProperty {

    /**
     * Configures if some user is able to reenter his existing session after closing the browser tab or the complete browser. Setting this
     * to true may be a security risk for clients running on unsafe computers. If this is configured to true, check that the parameter
     * client contains the same identifier the UI sends as client parameter on normal login request. Otherwise the backend will not be able
     * to rediscover the users session after closing the browser tab.
     */
    HTTP_AUTH_AUTOLOGIN("com.openexchange.ajax.login.http-auth.autologin", Boolean.FALSE.toString()),

    /**
     * Every client tells the backend through the client parameter on the login request his identy. This is not possible when using the HTTP
     * Authorization Header based login. So the client identifier for that request is defined here. It must be the same identifier that the
     * web frontend uses, if you set com.openexchange.cookie.hash to calculate and want the previously configured autologin to work.
     * <p>
     * Identifier for web UI is: <code>com.openexchange.ox.gui.dhtml</code>
     */
    HTTP_AUTH_CLIENT("com.openexchange.ajax.login.http-auth.client", "open-xchange-appsuite"),

    /**
     * The version of the client when using the HTTP Authorization Header based login. This should not be the normal web frontend version
     * because a different version can be used to distinguish logins through HTTP Authorization Header and normal login request.
     */
    HTTP_AUTH_VERSION("com.openexchange.ajax.login.http-auth.version", "HTTP Auth"),

    /**
     * Configures which error page should be used for the login. The error page is only applied for the HTML Form login and the HTTP
     * Authorization Header login. All other login related requests provide normal JSON responses in error cases. The built-in error page
     * shows the error message for 5 seconds and then redirects to the referrer page.
     */
    ERROR_PAGE_TEMPLATE("com.openexchange.ajax.login.errorPageTemplate", null),

    /**
     * Configures whether an insecure login is allowed. Meaning if local IP and/or user-agent strings are replaced in associated user
     * session on login redirect or login redeem requests. To create a session from a server for some client you have to pass the clients IP
     * address when creating the session.
     * WARNING! Setting this parameter to true may result in users seeing a different users content if the infrastructure around OX does not
     * work correctly.
     */
    INSECURE("com.openexchange.ajax.login.insecure", Boolean.FALSE.toString()),

    /**
     * Whether autologin is allowed or not.
     */
    SESSIOND_AUTOLOGIN(SessiondProperty.SESSIOND_AUTOLOGIN.getPropertyName(), SessiondProperty.SESSIOND_AUTOLOGIN.getDefaultValue()),
    NO_IP_CHECK_RANGE("com.openexchange.noIPCheckRange", null),

    /**
     * This option has only an effect if com.openexchange.ajax.login.insecure is configured to true.
     * This option allows to enable the IP check for /ajax/login?action=redirect requests. This request is mostly used to create a session
     * without using the OX web UI login screen. The previous behavior allowed to change the IP for this request. Configure this option to
     * false to have an IP check during this request. Additionally you can white list IP addresses from that an IP change is still allowed.
     * This is useful if other systems in the infrastructure around OX want to create the session.
     */
    REDIRECT_IP_CHANGE_ALLOWED("com.openexchange.ajax.login.redirect.changeIPAllowed", Boolean.TRUE.toString()),
    DISABLE_TRIM_LOGIN("com.openexchange.login.disableTrimLogin", Boolean.FALSE.toString()),

    /**
     * This option should not be presented in some configuration file. It is a hidden option especially for the integration purposes where
     * the integrating software is not capable doing secure login attempts.
     * This option makes the authId parameter for the formLogin action optional if it is configured to <code>true</code>.
     */
    FORM_LOGIN_WITHOUT_AUTHID("com.openexchange.login.formLoginWithoutAuthId", Boolean.FALSE.toString()),

    /**
     * The Random-Token is a one time token with a limited lifetime, which is used to initiate sessions through 3rd party applications or
     * websites. It is a UUID, generated by the backend via default Java UUID implementation. This token is deprecated and subject to
     * change. Setting this to false will prevent the random token from being written as part of the login response and prevent logins via
     * the random token
     */
    RANDOM_TOKEN("com.openexchange.ajax.login.randomToken", Boolean.FALSE.toString()),

    /**
     * Checks if specified login string appears to be in ACE/puny-code notation (see RFC3490 section 4.1); e.g. "someone@xn--mller-kva.de".
     * <p>
     * If set to <code>true</code> and ACE notation is detected, a failed login attempt is retried using IDN representation;
     * e.g. "someone@m&uuml;ller.de". Default is <code>false</code>.
     */
    CHECK_PUNY_CODE_LOGIN("com.openexchange.ajax.login.checkPunyCodeLoginString", Boolean.FALSE.toString())

    ;

    private final String propertyName;

    private final String defaultValue;

    private ConfigurationProperty(final String propertyName, final String defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}
