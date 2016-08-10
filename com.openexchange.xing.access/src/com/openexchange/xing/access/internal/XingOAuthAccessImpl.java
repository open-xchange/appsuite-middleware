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

package com.openexchange.xing.access.internal;

import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.session.Session;
import com.openexchange.xing.User;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingServerException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.session.AccessTokenPair;
import com.openexchange.xing.session.AppKeyPair;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link XingOAuthAccessImpl} - Initializes and provides XING OAuth access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class XingOAuthAccessImpl implements XingOAuthAccess, OAuthAccess {

    /**
     * The XING user identifier.
     */
    private String xingUserId;

    /**
     * The XING user's full name
     */
    private String xingUserName;

    /**
     * The Web-authenticating session.
     */
    private WebAuthSession webAuthSession;

    /**
     * The XING API reference.
     */
    private XingAPI<WebAuthSession> xingApi;

    /**
     * Initialises a new {@link XingOAuthAccessImpl}.
     *
     * @param session The users session
     * @param oauthAccount The associated OAuth account
     * @throws OXException If connect attempt fails
     */
    public XingOAuthAccessImpl(final Session session, final OAuthAccount oauthAccount) throws OXException {
        this(session, oauthAccount.getToken(), oauthAccount.getSecret());
    }

    /**
     * Initialises a new {@link XingOAuthAccessImpl}.
     * 
     * @param session
     * @param token
     * @param secret
     * @throws OXException
     */
    public XingOAuthAccessImpl(final Session session, final String token, final String secret) throws OXException {
        super();
        try {
            final OAuthServiceMetaData xingOAuthServiceMetaData = Services.getService(OAuthServiceMetaData.class);
            final AppKeyPair appKeys = new AppKeyPair(xingOAuthServiceMetaData.getAPIKey(session), xingOAuthServiceMetaData.getAPISecret(session));
            webAuthSession = new WebAuthSession(appKeys, new AccessTokenPair(token, secret));
            xingApi = new XingAPI<WebAuthSession>(webAuthSession);
            // Get account information
            final User accountInfo = xingApi.userInfo();
            xingUserId = accountInfo.getId();
            xingUserName = accountInfo.getDisplayName();
        } catch (final XingUnlinkedException e) {
            throw XingExceptionCodes.UNLINKED_ERROR.create();
        } catch (final XingServerException e) {
            if (e.getError() == XingServerException._404_NOT_FOUND) {
                throw XingExceptionCodes.XING_SERVER_UNAVAILABLE.create(e, new Object[0]);
            }
            throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
        } catch (final XingException e) {
            throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw XingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the XING API reference.
     *
     * @return The XING API reference
     */
    @Override
    public XingAPI<WebAuthSession> getXingAPI() {
        return xingApi;
    }

    /**
     * Disposes this XING OAuth access.
     */
    @Override
    public void dispose() {
        // So far nothing known that needs to be disposed
    }

    /**
     * Gets the XING user identifier.
     *
     * @return The XING user identifier
     */
    @Override
    public String getXingUserId() {
        return xingUserId;
    }

    /**
     * Gets the XING user's display name.
     *
     * @return The XING user's display name.
     */
    @Override
    public String getXingUserName() {
        return xingUserName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#initialise()
     */
    @Override
    public void initialise() throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#revoke()
     */
    @Override
    public void revoke() throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ensureNotExpired()
     */
    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getOAuthAccount()
     */
    @Override
    public OAuthAccount getOAuthAccount() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ping()
     */
    @Override
    public boolean ping() throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getClient()
     */
    @Override
    public OAuthClient<?> getClient() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getAccountId()
     */
    @Override
    public int getAccountId() throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }
}
