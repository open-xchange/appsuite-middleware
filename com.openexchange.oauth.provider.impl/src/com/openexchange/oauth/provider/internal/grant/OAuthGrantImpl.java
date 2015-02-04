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

package com.openexchange.oauth.provider.internal.grant;

import java.util.Date;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.OAuthGrant;
import com.openexchange.oauth.provider.Scopes;
import com.openexchange.oauth.provider.internal.authcode.AuthCodeInfo;


/**
 * {@link OAuthGrantImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthGrantImpl implements OAuthGrant {

    private final String clientId;

    private final int contextId;

    private final int userId;

    private Scopes scopes;

    private String accessToken;

    private String refreshToken;

    private Date expirationDate;


    public OAuthGrantImpl(AuthCodeInfo authCodeInfo, String accessToken, String refreshToken, Date expirationDate) {
        super();
        clientId = authCodeInfo.getClientId();
        contextId = authCodeInfo.getContextId();
        userId = authCodeInfo.getUserId();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationDate = expirationDate;
        scopes = DefaultScopes.parseScope(authCodeInfo.getScope());
    }

    /**
     * Copy constructor
     */
    public OAuthGrantImpl(OAuthGrant grant) {
        super();
        clientId = grant.getClientId();
        contextId = grant.getContextId();
        userId = grant.getUserId();
        accessToken = grant.getAccessToken();
        refreshToken = grant.getRefreshToken();
        expirationDate = grant.getExpirationDate();
        scopes = grant.getScopes();
    }


    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public Scopes getScopes() {
        return scopes;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setScopes(DefaultScopes scopes) {
        this.scopes = scopes;
    }

}
