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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link DefaultGuestInfo}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultGuestInfo implements GuestInfo {

    private final ServiceLookup services;

    private final User guestUser;

    private final int contextID;

    private final String token;

    /**
     * Initializes a new {@link DefaultGuestInfo}.
     *
     * @param services A servie lookup reference
     * @param contextID The identifier of the context this guest user belongs to
     * @param guestUser The guest user
     */
    public DefaultGuestInfo(ServiceLookup services, int contextID, User guestUser) throws OXException {
        this(services, guestUser, new ShareToken(contextID, guestUser));
    }

    /**
     * Initializes a new {@link DefaultGuestInfo}.
     *
     * @param services A servie lookup reference
     * @param guestUser The guest user
     * @param shareToken The share token
     */
    public DefaultGuestInfo(ServiceLookup services, User guestUser, ShareToken shareToken) {
        super();
        this.services = services;
        this.guestUser = guestUser;
        this.contextID = shareToken.getContextID();
        this.token = shareToken.getToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationMode getAuthentication() {
        return ShareTool.getAuthenticationMode(guestUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseToken() {
        return token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailAddress() {
        if (RecipientType.GUEST == getRecipientType()) {
            return guestUser.getMail();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGuestID() {
        return guestUser.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public int getCreatedBy() {
        return guestUser.getCreatedBy();
    }
}
