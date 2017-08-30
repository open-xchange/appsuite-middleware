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
package com.openexchange.oidc;

import com.openexchange.config.lean.Property;


/**
 * Backend specific properties.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public enum OIDCBackendProperty implements Property {
    clientId(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    redirectURIInit(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    redirectURIAuth(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    authorizationEndpoint(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    tokenEndpoint(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    clientSecret(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    jwkSetEndpoint(OIDCProperty.PREFIX, OIDCProperty.EMPTY), 
    jwsAlgorithm(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    scope(OIDCProperty.PREFIX,"openid"),
    issuer(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    responseType(OIDCProperty.PREFIX, "code"), 
    userInfoEndpoint(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    logoutEndpoint(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    redirectURIPostSSOLogout(OIDCProperty.PREFIX, OIDCProperty.EMPTY), 
    ssoLogout(OIDCProperty.PREFIX, false), 
    redirectURILogout(OIDCProperty.PREFIX, OIDCProperty.EMPTY),
    autologinCookieMode(OIDCProperty.PREFIX, "off"),
    storeOAuthTokens(OIDCProperty.PREFIX, false), 
    oauthRefreshTime(OIDCProperty.PREFIX, 60000);
    
    private final String fqn;
    private final Object defaultValue;
    
    private OIDCBackendProperty(String fqn, Object defaultValue) {
        this.fqn = fqn;
        this.defaultValue = defaultValue;
    }
    
    @Override
    public String getFQPropertyName() {
        return fqn + name();
    }

    @Override
    public <T> T getDefaultValue(Class<T> clazz) {
        if (defaultValue.getClass().isAssignableFrom(clazz)) {
            return clazz.cast(defaultValue);
        }
        throw new IllegalArgumentException("The object cannot be converted to the specified type '" + clazz.getCanonicalName() + "'");

    }

}
