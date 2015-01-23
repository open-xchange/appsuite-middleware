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

package com.openexchange.oauth.provider.internal;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.OAuthGrant;
import com.openexchange.oauth.provider.OAuthResourceService;
import com.openexchange.oauth.provider.internal.authcode.AuthCodeInfo;


/**
 * {@link GrantAllResourceService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GrantAllResourceService implements OAuthResourceService {

    @Override
    public OAuthGrant validate(String accessToken) throws OXException {
        AuthCodeInfo authCodeInfo = new AuthCodeInfo("1234", "r_contacts", 84, 424242669, System.nanoTime() + 3600 * 1000 * 1000 * 1000);
        return new OAuthGrantImpl(authCodeInfo, accessToken, UUIDs.getUnformattedStringFromRandom(), new Date(System.currentTimeMillis() + 3600 * 1000));
    }

    @Override
    public Client getClient(OAuthGrant token) throws OXException {
        return new Client() {

            @Override
            public boolean hasRedirectURI(String uri) {
                return true;
            }

            @Override
            public String getSecret() {
                return "2345";
            }

            @Override
            public String getName() {
                return "Example App";
            }

            @Override
            public String getId() {
                return "1234";
            }

            @Override
            public String getDescription() {
                return "An OAuth example app";
            }
        };
    }

}
