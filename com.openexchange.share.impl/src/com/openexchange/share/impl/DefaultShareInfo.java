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

package com.openexchange.share.impl;

import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link DefaultShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultShareInfo extends ResolvedGuestShare implements ShareInfo {

    private final Share share;

    /**
     * Initializes a new {@link DefaultShareInfo}.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestUser The guest user
     * @param share The share
     * @throws OXException
     */
    public DefaultShareInfo(ServiceLookup services, int contextID, User guestUser, Share share) throws OXException {
        super(services, contextID, guestUser, Collections.singletonList(share));
        this.share = share;
    }

    @Override
    public Share getShare() {
        return share;
    }

    @Override
    public String getToken() throws OXException {
        return super.getToken(share.getTarget());
    }

    @Override
    public String getEmailAddress() {
        if (RecipientType.GUEST == getRecipientType()) {
            return guestUser.getMail();
        }
        return null;
    }

    @Override
    public String getPassword() throws OXException {
        if (AuthenticationMode.ANONYMOUS_PASSWORD == getAuthentication()) {
            String cryptedPassword = guestUser.getUserPassword();
            if (false == Strings.isEmpty(cryptedPassword)) {
                return services.getService(ShareCryptoService.class).decrypt(cryptedPassword);
            }
        }
        return null;
    }

    @Override
    public RecipientType getRecipientType() {
        switch (getAuthentication()) {
        case ANONYMOUS:
        case ANONYMOUS_PASSWORD:
            return RecipientType.ANONYMOUS;
        case GUEST_PASSWORD:
            return RecipientType.GUEST;
        default:
            throw new UnsupportedOperationException("Unknown authentication mode: " + getAuthentication());
        }
    }

}
