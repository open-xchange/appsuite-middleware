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

package com.openexchange.oauth.provider.impl;

import java.util.Date;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthResourceService;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagement;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.grant.OAuthGrant;
import com.openexchange.oauth.provider.impl.grant.OAuthGrantImpl;
import com.openexchange.oauth.provider.impl.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.impl.grant.StoredGrant;
import com.openexchange.oauth.provider.tools.UserizedToken;

/**
 * {@link OAuthResourceServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthResourceServiceImpl implements OAuthResourceService {

    /*
     * From https://tools.ietf.org/html/rfc6750#section-2.1:
     * The syntax for Bearer credentials is as follows:
     * b64token = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
     * credentials = "Bearer" 1*SP b64token
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\x41-\\x5a\\x61-\\x7a\\x30-\\x39-._~+/]+=*");

    private final ClientManagement clientManagement;

    private final OAuthGrantStorage grantStorage;

    public OAuthResourceServiceImpl(ClientManagement clientManagement, OAuthGrantStorage grantStorage) {
        super();
        this.clientManagement = clientManagement;
        this.grantStorage = grantStorage;
    }

    @Override
    public OAuthGrant validate(String accessToken) throws OXException {
        if (!TOKEN_PATTERN.matcher(accessToken).matches() || !UserizedToken.isValid(accessToken)) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_MALFORMED);
        }

        StoredGrant grant = grantStorage.getGrantByAccessToken(UserizedToken.parse(accessToken));
        if (grant == null) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
        }

        if (grant.getExpirationDate().before(new Date())) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED);
        }

        return new OAuthGrantImpl(grant);
    }

    @Override
    public Client getClient(OAuthGrant grant) throws OXException {
        try {
            Client client = clientManagement.getClientById(grant.getClientId());
            if (client == null) {
                throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
            }

            return client;
        } catch (ClientManagementException e) {
            if (e.getReason() == com.openexchange.oauth.provider.client.ClientManagementException.Reason.INVALID_CLIENT_ID) {
                throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
            }

            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
